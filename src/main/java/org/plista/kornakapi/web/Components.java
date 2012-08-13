package org.plista.kornakapi.web;

import org.apache.mahout.cf.taste.recommender.Recommender;
import org.plista.kornakapi.core.storage.Storage;
import org.plista.kornakapi.core.training.Trainer;

import java.util.Map;

public class Components {

  private final Storage storage;
  private final Map<String, Recommender> recommenders;
  private final Map<String, Trainer> trainers;

  private static Components INSTANCE;

  public static synchronized void init(Storage storage, Map<String, Recommender> recommenders,
    Map<String, Trainer> trainers) {

    if (INSTANCE != null) {
      throw new IllegalStateException("Already initialized");
    }

    INSTANCE = new Components(storage, recommenders, trainers);
  }

  private Components(Storage storage, Map<String, Recommender> recommenders, Map<String, Trainer> trainers) {
    this.storage = storage;
    this.recommenders = recommenders;
    this.trainers = trainers;
  }

  public static Components instance() {
    return INSTANCE;
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
