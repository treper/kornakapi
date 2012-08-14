package org.plista.kornakapi.web;

import org.apache.mahout.cf.taste.recommender.Recommender;
import org.plista.kornakapi.core.config.Configuration;
import org.plista.kornakapi.core.storage.Storage;
import org.plista.kornakapi.core.training.Trainer;

import java.util.Map;

public class Components {

  private final Configuration conf;
  private final Storage storage;
  private final Map<String, Recommender> recommenders;
  private final Map<String, Trainer> trainers;

  public Components(Configuration conf, Storage storage, Map<String, Recommender> recommenders,
      Map<String, Trainer> trainers) {
    this.conf = conf;
    this.storage = storage;
    this.recommenders = recommenders;
    this.trainers = trainers;
  }

  public Configuration getConfiguration() {
    return conf;
  }

  public Recommender recommender(String name) {
    return recommenders.get(name);
  }

  public Trainer trainer(String name) {
    return trainers.get(name);
  }

  public Storage storage() {
    return storage;
  }
}
