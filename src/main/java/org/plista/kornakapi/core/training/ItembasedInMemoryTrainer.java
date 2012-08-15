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

package org.plista.kornakapi.core.training;

import com.google.common.io.Closeables;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.plista.kornakapi.core.config.ItembasedRecommenderConfig;
import org.plista.kornakapi.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ItembasedInMemoryTrainer implements Trainer {

  private final ItembasedRecommenderConfig conf;

  private static final Logger log = LoggerFactory.getLogger(ItembasedInMemoryTrainer.class);

  public ItembasedInMemoryTrainer(ItembasedRecommenderConfig conf) {
    this.conf = conf;
  }

  //TODO enable multithreading for this
  @Override
  public void train(File modelDirectory, Storage storage, Recommender recommender) throws IOException {

    BufferedWriter writer = null;

    try {
      DataModel inmemoryData = storage.trainingData();

      ItemSimilarity similarity = (ItemSimilarity) Class.forName(conf.getSimilarityClass())
          .getConstructor(DataModel.class).newInstance(inmemoryData);

      ItemBasedRecommender trainer = new GenericItemBasedRecommender(inmemoryData, similarity);

      writer = new BufferedWriter(new FileWriter(new File(modelDirectory, conf.getName() + ".model")));

      int itemsProcessed = 0;

      LongPrimitiveIterator itemIDs = inmemoryData.getItemIDs();
      while (itemIDs.hasNext()) {
        long itemID = itemIDs.nextLong();
        List<RecommendedItem> similarItems = trainer.mostSimilarItems(itemID, conf.getSimilarItemsPerItem());
        for (RecommendedItem similarItem : similarItems) {
          writer.write(String.valueOf(itemID));
          writer.write(",");
          writer.write(String.valueOf(similarItem.getItemID()));
          writer.write(",");
          writer.write(String.valueOf(similarItem.getValue()));
          writer.write("\n");
        }

        if (++itemsProcessed % 1000 == 0 && log.isInfoEnabled()) {
          log.info("similarities for {} items computed", itemsProcessed);
        }
      }
      if (log.isInfoEnabled()) {
        log.info("similarities for {} items computed. done.", itemsProcessed);
      }
      writer.flush();

      recommender.refresh(null);

    } catch (Exception e) {
      throw new IOException(e);
    } finally {
      Closeables.closeQuietly(writer);
    }
  }
}
