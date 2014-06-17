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
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.recommender.AllSimilarItemsCandidateItemsStrategy;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorization;
import org.apache.mahout.cf.taste.impl.recommender.svd.FilePersistenceStrategy;
import org.apache.mahout.cf.taste.impl.recommender.svd.PersistenceStrategy;
import org.apache.mahout.cf.taste.impl.similarity.file.FileItemSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.CandidateItemsStrategy;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.plista.kornakapi.KornakapiRecommender;
import org.plista.kornakapi.core.cluster.StreamingKMeansClassifierModel;
import org.plista.kornakapi.core.config.RecommenderConfig;
import org.plista.kornakapi.core.recommender.CachingAllUnknownItemsCandidateItemsStrategy;
import org.plista.kornakapi.core.recommender.FoldingFactorizationBasedRecommender;
import org.plista.kornakapi.core.recommender.StreamingKMeansClassifierRecommender;
import org.plista.kornakapi.core.config.Configuration;
import org.plista.kornakapi.core.config.FactorizationbasedRecommenderConfig;
import org.plista.kornakapi.core.config.ItembasedRecommenderConfig;
import org.plista.kornakapi.core.config.StreamingKMeansClustererConfig;
import org.plista.kornakapi.core.recommender.ItemSimilarityBasedRecommender;
import org.plista.kornakapi.core.storage.CandidateCacheStorageDecorator;
import org.plista.kornakapi.core.storage.MySqlMaxPersistentStorage;
import org.plista.kornakapi.core.storage.MySqlStorage;
import org.plista.kornakapi.core.storage.Storage;
import org.plista.kornakapi.core.training.FactorizationbasedInMemoryTrainer;
import org.plista.kornakapi.core.training.MultithreadedItembasedInMemoryTrainer;
import org.plista.kornakapi.core.training.StreamingKMeansClustererTrainer;
import org.plista.kornakapi.core.training.Trainer;
import org.plista.kornakapi.core.training.TrainingScheduler;
import org.plista.kornakapi.core.training.preferencechanges.DelegatingPreferenceChangeListener;
import org.plista.kornakapi.core.training.preferencechanges.InMemoryPreferenceChangeListener;
import org.plista.kornakapi.web.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/** servlet context listener to initialize/shut down the application */
//模型初始化
public class BigBangServletContextListener implements ServletContextListener {

  private static final String CONFIG_PROPERTY = "kornakapi.conf";

  private static final Logger log = LoggerFactory.getLogger(BigBangServletContextListener.class);
  


  @Override
  public void contextInitialized(ServletContextEvent event) {
    try {

      String configFileLocation = System.getProperty(CONFIG_PROPERTY);
      Preconditions.checkState(configFileLocation != null, "configuration file not set!");

      File configFile = new File(configFileLocation);
      Preconditions.checkState(configFile.exists() && configFile.canRead(),
          "configuration file not found or not readable");

      Configuration conf = Configuration.fromXML(Files.toString(configFile, Charsets.UTF_8));

      Preconditions.checkState(conf.getNumProcessorsForTraining() > 0, "need at least one processor for training!");

      
      Storage storage;
      if(conf.getMaxPersistence()){
    	  storage = new CandidateCacheStorageDecorator(new MySqlMaxPersistentStorage(conf.getStorageConfiguration()));
      }else{
    	  storage = new CandidateCacheStorageDecorator(new MySqlStorage(conf.getStorageConfiguration()));
      }
      

	DataModel persistentData = storage.recommenderData();//mysql storage

      Map<String, KornakapiRecommender> recommenders = Maps.newHashMap();
      Map<String, Trainer> trainers = Maps.newHashMap();

      TrainingScheduler scheduler = new TrainingScheduler();//quatz任务调度器
      DelegatingPreferenceChangeListener preferenceChangeListener = new DelegatingPreferenceChangeListener();

      for (ItembasedRecommenderConfig itembasedConf : conf.getItembasedRecommenders()) {

        String name = itembasedConf.getName();

        File modelFile = modelFile(conf, name);//模型文件格式为name.model

        if (!modelFile.exists()) {
          boolean created = modelFile.createNewFile();
          if (!created) {
            throw new IllegalStateException("Cannot create file in model directory" + conf.getModelDirectory());
          }
        }

          //文件记录的item相似度,mahout里的GenericItemSimilarity用了一个嵌套map记录相似度,并有item相似的index,相互index
        ItemSimilarity itemSimilarity = new FileItemSimilarity(modelFile);
        AllSimilarItemsCandidateItemsStrategy allSimilarItemsStrategy =
            new AllSimilarItemsCandidateItemsStrategy(itemSimilarity);
          //初始化一个recommender
        KornakapiRecommender recommender = new ItemSimilarityBasedRecommender(persistentData, itemSimilarity,
            allSimilarItemsStrategy, allSimilarItemsStrategy);

          //添加recommender索引
        recommenders.put(name, recommender);
          //添加训练任务
        trainers.put(name, new MultithreadedItembasedInMemoryTrainer(itembasedConf));

        String cronExpression = itembasedConf.getRetrainCronExpression();
        if (cronExpression == null) {
          scheduler.addRecommenderTrainingJob(name);
        } else {//添加定时训练任务
          scheduler.addRecommenderTrainingJobWithCronSchedule(name, cronExpression);
        }

        if (itembasedConf.getRetrainAfterPreferenceChanges() !=
            RecommenderConfig.DONT_RETRAIN_ON_PREFERENCE_CHANGES) {
          preferenceChangeListener.addDelegate(new InMemoryPreferenceChangeListener(scheduler, name,
              itembasedConf.getRetrainAfterPreferenceChanges()));
        }

        log.info("Created ItemBasedRecommender [{}] using similarity [{}] and [{}] similar items per item",
            new Object[] { name, itembasedConf.getSimilarityClass(), itembasedConf.getSimilarItemsPerItem() });
      }     
     
      for (StreamingKMeansClustererConfig streamingKMeansClustererConf : conf.getStreamingKMeansClusterer()) {
      
    	  String name = streamingKMeansClustererConf.getName();
    	  
          File modelFile = new File(conf.getModelDirectory(), name + ".model");

          PersistenceStrategy persistence = new FilePersistenceStrategy(modelFile);

          if (!modelFile.exists()) {
            createEmptyFactorization(persistence);
          }
          StreamingKMeansClassifierModel model = new StreamingKMeansClassifierModel(conf.getStorageConfiguration()); 
          StreamingKMeansClustererTrainer clusterer = new StreamingKMeansClustererTrainer( streamingKMeansClustererConf, model);
          trainers.put(name,clusterer);
          
          StreamingKMeansClassifierRecommender recommender = new StreamingKMeansClassifierRecommender(model);
          recommenders.put(name, recommender);
          
          String cronExpression = streamingKMeansClustererConf.getRetrainCronExpression();
          if (cronExpression == null) {
            scheduler.addRecommenderTrainingJob(name);
          } else {//添加定时训练任务
            scheduler.addRecommenderTrainingJobWithCronSchedule(name, cronExpression);
          }
          
          if (streamingKMeansClustererConf.getRetrainAfterPreferenceChanges() !=
                  RecommenderConfig.DONT_RETRAIN_ON_PREFERENCE_CHANGES) {
                preferenceChangeListener.addDelegate(new InMemoryPreferenceChangeListener(scheduler, name,
                		streamingKMeansClustererConf.getRetrainAfterPreferenceChanges()));
              }
          
          log.info("Created StreamingKMeansClusterer [{}] with [{}] minclusters and [{}] cutoff distance",
              new Object[] { name, streamingKMeansClustererConf.getDesiredNumCluster(), streamingKMeansClustererConf.getDistanceCutoff()}); 
      }
   
      for (FactorizationbasedRecommenderConfig factorizationbasedConf : conf.getFactorizationbasedRecommenders()) {

        String name = factorizationbasedConf.getName();

        File modelFile = new File(conf.getModelDirectory(), name + ".model");

        PersistenceStrategy persistence = new FilePersistenceStrategy(modelFile);

        if (!modelFile.exists()) {
          createEmptyFactorization(persistence);
        }

        CandidateItemsStrategy allUnknownItemsStrategy =
            new CachingAllUnknownItemsCandidateItemsStrategy(persistentData);

        FoldingFactorizationBasedRecommender svdRecommender = new FoldingFactorizationBasedRecommender(persistentData,
            allUnknownItemsStrategy, persistence);

        recommenders.put(name, svdRecommender);
        trainers.put(name, new FactorizationbasedInMemoryTrainer(factorizationbasedConf));

        String cronExpression = factorizationbasedConf.getRetrainCronExpression();
        if (cronExpression == null) {
          scheduler.addRecommenderTrainingJob(name);
        } else {//添加定时训练任务
          scheduler.addRecommenderTrainingJobWithCronSchedule(name, cronExpression);
        }

        if (factorizationbasedConf.getRetrainAfterPreferenceChanges() !=
            RecommenderConfig.DONT_RETRAIN_ON_PREFERENCE_CHANGES) {
          preferenceChangeListener.addDelegate(new InMemoryPreferenceChangeListener(scheduler, name,
              factorizationbasedConf.getRetrainAfterPreferenceChanges()));
        }

        log.info("Created FactorizationBasedRecommender [{}] using [{}] features and [{}] iterations",
            new Object[] { name, factorizationbasedConf.getNumberOfFeatures(),
                factorizationbasedConf.getNumberOfIterations() });
      }

      Components.init(conf, storage, recommenders, trainers, scheduler, preferenceChangeListener);//singleton

      scheduler.start();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void createEmptyFactorization(PersistenceStrategy strategy) throws IOException {
    strategy.maybePersist(new Factorization(new FastByIDMap<Integer>(0), new FastByIDMap<Integer>(0),
        new double[0][0], new double[0][0]));
  }

  private File modelFile(Configuration conf, String recommenderName) {
    return new File(conf.getModelDirectory(), recommenderName + ".model");
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    Components components = Components.instance();

    Closeables.closeQuietly(components.storage());
    Closeables.closeQuietly(components.scheduler());
  }
}
