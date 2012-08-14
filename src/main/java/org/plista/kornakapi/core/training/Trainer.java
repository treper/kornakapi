package org.plista.kornakapi.core.training;

import org.apache.mahout.cf.taste.recommender.Recommender;
import org.plista.kornakapi.core.storage.Storage;

import java.io.File;
import java.io.IOException;

public interface Trainer {

  void train(File modelDirectory, Storage storage, Recommender recommender) throws IOException;
}
