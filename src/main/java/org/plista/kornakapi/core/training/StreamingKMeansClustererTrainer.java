
package org.plista.kornakapi.core.training;


import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.mahout.cf.taste.impl.recommender.svd.FilePersistenceStrategy;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.clustering.streaming.cluster.StreamingKMeans;
import org.apache.mahout.common.distance.ManhattanDistanceMeasure;
import org.apache.mahout.math.Centroid;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.neighborhood.FastProjectionSearch;
import org.apache.mahout.math.neighborhood.UpdatableSearcher;
import org.plista.kornakapi.core.cluster.StreamingKMeansClassifierModel;
import org.plista.kornakapi.core.config.StorageConfiguration;
import org.plista.kornakapi.core.config.StreamingKMeansClustererConfig;
import org.plista.kornakapi.core.storage.MySqlDataExtractor;
import org.plista.kornakapi.core.storage.MySqlDataExtractor.StreamingKMeansDataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamingKMeansClustererTrainer extends AbstractTrainer{


	private StreamingKMeansClustererConfig conf = null;
	private static final Logger log = LoggerFactory.getLogger(FactorizationbasedInMemoryTrainer.class);

	private StorageConfiguration storage;
	private StreamingKMeansClassifierModel model;
	int clusters;
	long cutoff;
	StreamingKMeans clusterer;
	

	public StreamingKMeansClustererTrainer(StorageConfiguration storage,StreamingKMeansClustererConfig conf, StreamingKMeansClassifierModel model) throws IOException {
		super(conf);
		this.conf = conf;
		this.storage = storage;
		this.model = model;
		clusters = conf.getDesiredNumCluster();
		cutoff = conf.getDistanceCutoff();
		UpdatableSearcher searcher = new FastProjectionSearch(new ManhattanDistanceMeasure(), 10, 10);
		clusterer = new StreamingKMeans(searcher, clusters,cutoff);
	}

	@Override
	protected void doTrain(File targetFile, DataModel inmemoryData,
			int numProcessors) throws IOException {
		/**
		 * class to calculate clusters
		 */
	    long start = System.currentTimeMillis();
		MySqlDataExtractor extractor = new MySqlDataExtractor(storage);
		UpdatableSearcher centroids = null;
		StreamingKMeansDataObject data = extractor.getData();
		extractor.close();
		this.model.setData(data);
		centroids = clusterer.cluster(data.getMatrix());		
		this.model.updateCentroids(centroids);
		long estimateDuration = System.currentTimeMillis() - start;  
		if (log.isInfoEnabled()) {
			log.info("Model trained in {} ms", estimateDuration);
		}
		
	}
	public void streamNewPoint(Centroid centerTheCentroid){
	    long start = System.currentTimeMillis();
		UpdatableSearcher centroids = clusterer.cluster(centerTheCentroid);
		long estimateDuration = System.currentTimeMillis() - start;  
		this.model.updateCentroids(centroids);
		if (log.isInfoEnabled()) {
			log.info("Model trained in {} ms, created [{}] Clusters", estimateDuration, centroids.size());
		}
	}
}

