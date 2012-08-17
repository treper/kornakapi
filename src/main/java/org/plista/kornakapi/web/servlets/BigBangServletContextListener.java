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

package org.plista.kornakapi.web.servlets;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import org.apache.mahout.cf.taste.impl.recommender.AllSimilarItemsCandidateItemsStrategy;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.svd.FilePersistenceStrategy;
import org.apache.mahout.cf.taste.impl.recommender.svd.PersistenceStrategy;
import org.apache.mahout.cf.taste.impl.similarity.file.FileItemSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.CandidateItemsStrategy;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.plista.kornakapi.core.CachingAllUnknownItemsCandidateItemsStrategy;
import org.plista.kornakapi.core.FactorizationbasedRecommender;
import org.plista.kornakapi.core.config.Configuration;
import org.plista.kornakapi.core.config.FactorizationbasedRecommenderConfig;
import org.plista.kornakapi.core.config.ItembasedRecommenderConfig;
import org.plista.kornakapi.core.storage.MySqlStorage;
import org.plista.kornakapi.core.training.FactorizationbasedInMemoryTrainer;
import org.plista.kornakapi.core.training.MultithreadedItembasedInMemoryTrainer;
import org.plista.kornakapi.core.training.TrainAllRecommendersJob;
import org.plista.kornakapi.core.training.Trainer;
import org.plista.kornakapi.web.Components;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.util.Map;

public class BigBangServletContextListener implements ServletContextListener {

  private static final Logger log = LoggerFactory.getLogger(BigBangServletContextListener.class);

  @Override
  public void contextInitialized(ServletContextEvent event) {
    try {

      String xml = Files.toString(new File("/home/ssc/Desktop/plista/test.conf"), Charsets.UTF_8);
      Configuration conf = Configuration.fromXML(xml);

      MySqlStorage storage = new MySqlStorage(conf.getStorageConfiguration());

      DataModel persistentData = storage.recommenderData();

      Map<String, Recommender> recommenders = Maps.newHashMap();
      Map<String, Trainer> trainers = Maps.newHashMap();

      for (ItembasedRecommenderConfig itembasedConf : conf.getItembasedRecommenders()) {

        String name = itembasedConf.getName();
        ItemSimilarity itemSimilarity = new FileItemSimilarity(new File(conf.getModelDirectory(), name + ".model"));
        AllSimilarItemsCandidateItemsStrategy allSimilarItemsStrategy =
            new AllSimilarItemsCandidateItemsStrategy(itemSimilarity);
        ItemBasedRecommender recommender = new GenericItemBasedRecommender(persistentData, itemSimilarity,
            allSimilarItemsStrategy, allSimilarItemsStrategy);

        recommenders.put(name, recommender);
        trainers.put(name, new MultithreadedItembasedInMemoryTrainer(itembasedConf));

        log.info("Created ItemBasedRecommender [{}] using similarity [{}] and [{}] similar items per item",
            new Object[] { name, itembasedConf.getSimilarityClass(), itembasedConf.getSimilarItemsPerItem() });
      }

      for (FactorizationbasedRecommenderConfig factorizationbasedConf : conf.getFactorizationbasedRecommenders()) {

        String name = factorizationbasedConf.getName();

        PersistenceStrategy persistence = new FilePersistenceStrategy(new File(conf.getModelDirectory(),
            name + ".model"));

        CandidateItemsStrategy allUnknownItemsStrategy =
            new CachingAllUnknownItemsCandidateItemsStrategy(persistentData);

        FactorizationbasedRecommender svdRecommender = new FactorizationbasedRecommender(persistentData,
            allUnknownItemsStrategy, persistence);

        recommenders.put(name, svdRecommender);
        trainers.put(name, new FactorizationbasedInMemoryTrainer(factorizationbasedConf));

        log.info("Created FactorizationBasedRecommender [{}] using [{}] features and [{}] iterations",
            new Object[] { name, factorizationbasedConf.getNumberOfFeatures(),
                factorizationbasedConf.getNumberOfIterations() });
      }

      ThreadPool threadPool = new SimpleThreadPool(1, Thread.NORM_PRIORITY); threadPool.initialize();

      DirectSchedulerFactory schedulerFactory = DirectSchedulerFactory.getInstance();
      schedulerFactory.createScheduler(threadPool, new RAMJobStore());
      Scheduler scheduler = schedulerFactory.getScheduler();

      JobDetail job = JobBuilder.newJob(TrainAllRecommendersJob.class).build();

      // http://www.quartz-scheduler.org/documentation/quartz-2.1.x/tutorials/crontrigger
      CronTrigger trigger = TriggerBuilder.newTrigger()
          .withSchedule(CronScheduleBuilder.cronSchedule("0 0 1 * * ?"))
          .build();

      scheduler.scheduleJob(job, trigger);

      Components.init(conf, storage, recommenders, trainers, scheduler);

      scheduler.start();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    Components components = Components.instance();

    Closeables.closeQuietly(components.storage());
    try {
      components.scheduler().shutdown();
    } catch (SchedulerException e) {
      throw new IllegalStateException(e);
    }
  }
}
