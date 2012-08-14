package org.plista.kornakapi.core;

import com.google.common.base.Preconditions;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.impl.recommender.TopItems;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorization;
import org.apache.mahout.cf.taste.impl.recommender.svd.PersistenceStrategy;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.CandidateItemsStrategy;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public final class FactorizationbasedRecommender extends AbstractRecommender {

  private Factorization factorization;
  private final PersistenceStrategy persistenceStrategy;
  private final RefreshHelper refreshHelper;

  private static final Logger log = LoggerFactory.getLogger(FactorizationbasedRecommender.class);

  public FactorizationbasedRecommender(DataModel dataModel, CandidateItemsStrategy candidateItemsStrategy,
                                       PersistenceStrategy persistenceStrategy) throws TasteException {
    super(dataModel, candidateItemsStrategy);

    this.persistenceStrategy = Preconditions.checkNotNull(persistenceStrategy);
    try {
      factorization = persistenceStrategy.load();
    } catch (IOException e) {
      throw new TasteException("Error loading factorization", e);
    }

    Preconditions.checkArgument(factorization != null, "PersistenceStrategy must provide an initial factorization");

    refreshHelper = new RefreshHelper(new Callable<Object>() {
      @Override
      public Object call() throws TasteException {
        reloadFactorization();
        return null;
      }
    });
    refreshHelper.addDependency(getDataModel());
    refreshHelper.addDependency(candidateItemsStrategy);
  }

  private void reloadFactorization() throws TasteException {
    try {
      factorization = Preconditions.checkNotNull(persistenceStrategy.load());
    } catch (IOException e) {
      throw new TasteException("Error reloading factorization", e);
    }
  }

  @Override
  public List<RecommendedItem> recommend(long userID, int howMany, IDRescorer rescorer) throws TasteException {
    Preconditions.checkArgument(howMany >= 1, "howMany must be at least 1");
    log.debug("Recommending items for user ID '{}'", userID);

    PreferenceArray preferencesFromUser = getDataModel().getPreferencesFromUser(userID);
    FastIDSet possibleItemIDs = getAllOtherItems(userID, preferencesFromUser);

    List<RecommendedItem> topItems = TopItems.getTopItems(howMany, possibleItemIDs.iterator(), rescorer,
        new Estimator(userID));
    log.debug("Recommendations are: {}", topItems);

    return topItems;
  }

  @Override
  public float estimatePreference(long userID, long itemID) throws TasteException {
    double[] userFeatures = factorization.getUserFeatures(userID);
    double[] itemFeatures = factorization.getItemFeatures(itemID);
    double estimate = 0;
    for (int feature = 0; feature < userFeatures.length; feature++) {
      estimate += userFeatures[feature] * itemFeatures[feature];
    }
    return (float) estimate;
  }

  private final class Estimator implements TopItems.Estimator<Long> {

    private final long theUserID;

    private Estimator(long theUserID) {
      this.theUserID = theUserID;
    }

    @Override
    public double estimate(Long itemID) throws TasteException {
      return estimatePreference(theUserID, itemID);
    }
  }

  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    refreshHelper.refresh(alreadyRefreshed);
  }
}
