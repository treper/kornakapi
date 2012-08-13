package org.plista.kornakapi.web.servlets;

import com.google.common.collect.Maps;
import org.apache.mahout.cf.taste.impl.recommender.AllSimilarItemsCandidateItemsStrategy;
import org.apache.mahout.cf.taste.impl.recommender.AllUnknownItemsCandidateItemsStrategy;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.svd.ALSWRFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.FilePersistenceStrategy;
import org.apache.mahout.cf.taste.impl.recommender.svd.PersistenceStrategy;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.impl.similarity.file.FileItemSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.plista.kornakapi.core.storage.MySqlStorage;
import org.plista.kornakapi.core.training.ItembasedInMemoryTrainer;
import org.plista.kornakapi.core.training.Trainer;
import org.plista.kornakapi.core.training.WeightedMatrixFactorizationInMemoryTrainer;
import org.plista.kornakapi.web.Components;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class BigBangFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    try {
      MySqlStorage storage = new MySqlStorage();

      DataModel inMemoryData = storage.trainingData();

      FileItemSimilarity itemSimilarity =
          new FileItemSimilarity(new File("/home/ssc/Desktop/plista/models/itemSimilarities.csv"));
      AllSimilarItemsCandidateItemsStrategy candidateItemsStrategy =
          new AllSimilarItemsCandidateItemsStrategy(itemSimilarity);
      GenericItemBasedRecommender recommender = new GenericItemBasedRecommender(storage.recommenderData(),
          itemSimilarity, candidateItemsStrategy, candidateItemsStrategy);

      PersistenceStrategy persistence =
          new FilePersistenceStrategy(new File("/home/ssc/Desktop/plista/models/weightedMatrixFactorization.bin"));

      SVDRecommender svdRecommender = new SVDRecommender(inMemoryData, new ALSWRFactorizer(inMemoryData, 5, 0.1, 10),
          new AllUnknownItemsCandidateItemsStrategy(), persistence);

      Map<String, Recommender> recommenders = Maps.newHashMap();

      recommenders.put("itembased", recommender);
      recommenders.put("svd", svdRecommender);

      Map<String, Trainer> trainers = Maps.newHashMap();

      trainers.put("itembased", new ItembasedInMemoryTrainer());
      trainers.put("svd", new WeightedMatrixFactorizationInMemoryTrainer());

      Components.init(storage, recommenders, trainers);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    filterChain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }
}
