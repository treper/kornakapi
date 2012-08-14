package org.plista.kornakapi.core.training;

import org.apache.mahout.cf.taste.impl.recommender.svd.ALSWRFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorization;
import org.apache.mahout.cf.taste.impl.recommender.svd.FilePersistenceStrategy;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.plista.kornakapi.core.config.FactorizationbasedRecommenderConfig;
import org.plista.kornakapi.core.storage.Storage;

import java.io.File;
import java.io.IOException;

public class FactorizationbasedInMemoryTrainer implements Trainer {

  private final FactorizationbasedRecommenderConfig conf;

  public FactorizationbasedInMemoryTrainer(FactorizationbasedRecommenderConfig conf) {
    this.conf = conf;
  }

  @Override
  public void train(File modelDirectory, Storage storage, Recommender recommender) throws IOException {

    try {

      ALSWRFactorizer factorizer = new ALSWRFactorizer(storage.trainingData(), conf.getNumberOfFeatures(),
          conf.getLambda(), conf.getNumberOfIterations(), conf.isUsesImplicitFeedback(), conf.getAlpha());

      Factorization factorization = factorizer.factorize();

      new FilePersistenceStrategy(new File(modelDirectory, conf.getName() + ".model")).maybePersist(factorization);

      recommender.refresh(null);

    } catch (Exception e) {
      throw new IOException(e);
    }
  }
}
