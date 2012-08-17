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

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.plista.kornakapi.core.config.ItembasedRecommenderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MultithreadedItembasedInMemoryTrainer extends AbstractTrainer {

  private final ItembasedRecommenderConfig conf;

  private static final Logger log = LoggerFactory.getLogger(MultithreadedItembasedInMemoryTrainer.class);

  public MultithreadedItembasedInMemoryTrainer(ItembasedRecommenderConfig conf) {
    super(conf);
    this.conf = conf;
  }

  @Override
  protected void doTrain(File targetFile, DataModel inmemoryData) throws IOException {
    BufferedWriter writer = null;
    int numProcessors = Runtime.getRuntime().availableProcessors();
    ExecutorService executorService = Executors.newFixedThreadPool(numProcessors);

    try {

      ItemSimilarity similarity = (ItemSimilarity) Class.forName(conf.getSimilarityClass())
          .getConstructor(DataModel.class).newInstance(inmemoryData);

      ItemBasedRecommender trainer = new GenericItemBasedRecommender(inmemoryData, similarity);

      writer = new BufferedWriter(new FileWriter(targetFile));

      int batchSize = 100;
      int numItems = inmemoryData.getNumItems();

      List<long[]> itemIDBatches = queueItemIDsInBatches(inmemoryData.getItemIDs(), numItems, batchSize);

      log.info("Queued {} items in {} batches", numItems, itemIDBatches.size());

      BlockingQueue<long[]> itemsIDsToProcess = new LinkedBlockingQueue<long[]>(itemIDBatches);
      BlockingQueue<String> output = new LinkedBlockingQueue<String>();

      AtomicInteger numActiveWorkers = new AtomicInteger(numProcessors - 1);
      for (int n = 0; n < numProcessors - 1; n++) {
        executorService.execute(new SimilarItemsWorker(n, itemsIDsToProcess, output, trainer,
            conf.getSimilarItemsPerItem(), numActiveWorkers));
      }
      executorService.execute(new OutputWriter(output, writer, numActiveWorkers));

    } catch (Exception e) {
      throw new IOException(e);
    } finally {
      executorService.shutdown();
      try {
        executorService.awaitTermination(6, TimeUnit.HOURS);
      } catch (InterruptedException e) {

      }
      Closeables.closeQuietly(writer);
    }
  }

  private List<long[]> queueItemIDsInBatches(LongPrimitiveIterator itemIDs, int numItems, int batchSize) {
    List<long[]> itemIDBatches = Lists.newArrayListWithCapacity(numItems / batchSize);

    long[] batch = new long[batchSize];
    int pos = 0;
    while (itemIDs.hasNext()) {
      if (pos == batchSize) {
        itemIDBatches.add(batch.clone());
        pos = 0;
      }
      batch[pos] = itemIDs.nextLong();
      pos++;
    }
    int nonQueuedItemIDs = batchSize - pos;
    if (nonQueuedItemIDs > 0) {
      long[] lastBatch = new long[nonQueuedItemIDs];
      System.arraycopy(batch, 0, lastBatch, 0, nonQueuedItemIDs);
      itemIDBatches.add(lastBatch);
    }
    return itemIDBatches;
  }

  static class OutputWriter implements Runnable {

    private final BlockingQueue<String> output;
    private final BufferedWriter writer;
    private final AtomicInteger numActiveWorkers;

    OutputWriter(BlockingQueue<String> output, BufferedWriter writer, AtomicInteger numActiveWorkers) {
      this.output = output;
      this.writer = writer;
      this.numActiveWorkers = numActiveWorkers;
    }

    @Override
    public void run() {
      while (numActiveWorkers.get() != 0) {
        try {
          String lines = output.poll(10, TimeUnit.MILLISECONDS);
          if (null != lines) {
            writer.write(lines);
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  static class SimilarItemsWorker implements Runnable {

    private final int number;
    private final BlockingQueue<long[]> itemIDBatches;
    private final BlockingQueue<String> output;
    private final ItemBasedRecommender trainer;
    private final int howMany;
    private final AtomicInteger numActiveWorkers;

    SimilarItemsWorker(int number, BlockingQueue<long[]> itemIDBatches, BlockingQueue<String> output,
        ItemBasedRecommender trainer, int howMany, AtomicInteger numActiveWorkers) {
      this.number = number;
      this.itemIDBatches = itemIDBatches;
      this.output = output;
      this.trainer = trainer;
      this.howMany = howMany;
      this.numActiveWorkers = numActiveWorkers;
    }

    @Override
    public void run() {
      int numBatchesProcessed = 0;
      while (!itemIDBatches.isEmpty()) {
        try {
          long[] itemIDBatch = itemIDBatches.take();
          StringBuilder lines = new StringBuilder();

          for (long itemID : itemIDBatch) {
            Iterable<RecommendedItem> similarItems = trainer.mostSimilarItems(itemID, howMany);

            for (RecommendedItem similarItem : similarItems) {
              lines.append(itemID).append(',').append(similarItem.getItemID())
                  .append(',').append(similarItem.getValue()).append('\n');
            }
          }

          output.offer(lines.toString());

          if (++numBatchesProcessed % 5 == 0) {
            log.info("worker {} processed {} batches", number, numBatchesProcessed);
          }

        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      log.info("worker {} processed {} batches. done.", number, numBatchesProcessed);
      numActiveWorkers.decrementAndGet();
    }
  }
}
