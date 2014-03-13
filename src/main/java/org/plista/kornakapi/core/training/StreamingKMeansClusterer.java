
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

public class StreamingKMeansClusterer extends AbstractTrainer{


	private StreamingKMeansClustererConfig conf = null;
	private static final Logger log = LoggerFactory.getLogger(FactorizationbasedInMemoryTrainer.class);

	private MySqlDataExtractor extractor;
	private StreamingKMeansClassifierModel model;
	

	public StreamingKMeansClusterer(StreamingKMeansClustererConfig conf, MySqlDataExtractor extractor, StreamingKMeansClassifierModel model) throws IOException {
		super(conf);
		this.conf = conf;
		this.extractor = extractor;
		this.model = model;
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
		StreamingKMeansDataObject data = extractor.getData();
		extractor.close();
		centroids = clusterer.cluster(data.getMatrix());		
		this.model.updateCentroids(data, centroids);
		
	}

}

