package org.plista.kornakapi.core.training;

import com.google.common.io.Closeables;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.plista.kornakapi.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ItembasedInMemoryTrainer implements Trainer {

  private static final Logger log = LoggerFactory.getLogger(ItembasedInMemoryTrainer.class);

  public void train(Storage storage) throws IOException {

    BufferedWriter writer = null;

    try {
      DataModel data = storage.trainingData();
      GenericItemBasedRecommender trainingRecommender =
          new GenericItemBasedRecommender(data, new LogLikelihoodSimilarity(data));

      writer = new BufferedWriter(new FileWriter("/home/ssc/Desktop/plista/models/itemSimilarities.csv"));

      int itemsProcessed = 0;

      LongPrimitiveIterator itemIDs = data.getItemIDs();
      while (itemIDs.hasNext()) {
        long itemID = itemIDs.nextLong();
        List<RecommendedItem> similarItems = trainingRecommender.mostSimilarItems(itemID, 10);
        for (RecommendedItem similarItem : similarItems) {
          writer.write(itemID + "," +  similarItem.getItemID() + "," + similarItem.getValue() + "\n");
        }

        if (++itemsProcessed % 1000 == 0 && log.isInfoEnabled()) {
          log.info("similarities for {} items computed", itemsProcessed);
        }
      }
      if (log.isInfoEnabled()) {
        log.info("similarities for {} items computed. done.", itemsProcessed);
      }

    } catch (TasteException e) {
      throw new IOException(e);
    } finally {
      Closeables.closeQuietly(writer);
    }
  }
}
