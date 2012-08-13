package org.plista.kornakapi.core.training;

import org.apache.mahout.cf.taste.impl.recommender.svd.ALSWRFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorization;
import org.apache.mahout.cf.taste.impl.recommender.svd.FilePersistenceStrategy;
import org.apache.mahout.cf.taste.impl.recommender.svd.PersistenceStrategy;
import org.plista.kornakapi.core.storage.Storage;

import java.io.File;
import java.io.IOException;

public class WeightedMatrixFactorizationInMemoryTrainer implements Trainer {

  public void train(Storage storage) throws IOException {

    try {

      ALSWRFactorizer factorizer = new ALSWRFactorizer(storage.trainingData(), 5, 0.1, 10, true, 40);

      Factorization factorization = factorizer.factorize();

      PersistenceStrategy persistence =
          new FilePersistenceStrategy(new File("/home/ssc/Desktop/plista/models/weightedMatrixFactorization.bin"));

      persistence.maybePersist(factorization);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }
}
