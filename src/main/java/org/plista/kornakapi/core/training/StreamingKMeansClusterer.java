
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
import org.plista.kornakapi.core.cluster.MySqlDataExtractor;
import org.plista.kornakapi.core.cluster.StreamingKMeansClassifierModel;
import org.plista.kornakapi.core.config.StorageConfiguration;
import org.plista.kornakapi.core.config.StreamingKMeansClustererConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamingKMeansClusterer extends AbstractTrainer{

	private  StorageConfiguration storageConfiguration = null;
	private StreamingKMeansClustererConfig conf = null;
	private static final Logger log = LoggerFactory.getLogger(FactorizationbasedInMemoryTrainer.class);

	private MySqlDataExtractor extractor;
	private StreamingKMeansClassifierModel model;
	

	public StreamingKMeansClusterer(StorageConfiguration storageConfiguration, StreamingKMeansClustererConfig conf, MySqlDataExtractor extractor, StreamingKMeansClassifierModel model) throws IOException {
		super(conf);
		this.storageConfiguration = storageConfiguration;
		this.conf = conf;
		this.extractor = extractor;
		this.model = model;
		this.doTrain(null, null, 0);
	}

	@Override
	protected void doTrain(File targetFile, DataModel inmemoryData,
			int numProcessors) throws IOException {
		/**
		 * class to calculate clusters
		 */
		int clusters = conf.getDesiredNumCluster();
		long cutoff = conf.getDistanceCutoff();
		UpdatableSearcher centroids = null;
		UpdatableSearcher searcher = new FastProjectionSearch(new ManhattanDistanceMeasure(), 10, 10);
		StreamingKMeans clusterer = new StreamingKMeans(searcher, clusters,cutoff);
		Matrix data = extractor.getData();
		centroids = clusterer.cluster(data);		
		this.model.updateCentroids(centroids);
		
	}

}

