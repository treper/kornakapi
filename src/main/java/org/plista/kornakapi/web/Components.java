/**
 * Copyright 2012 plista GmbH  (http://www.plista.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 */

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
