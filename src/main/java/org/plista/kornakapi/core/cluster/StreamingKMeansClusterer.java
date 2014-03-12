
package org.plista.kornakapi.core.cluster;


import java.util.Iterator;

import org.apache.mahout.clustering.streaming.cluster.StreamingKMeans;
import org.apache.mahout.common.distance.ManhattanDistanceMeasure;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.neighborhood.FastProjectionSearch;
import org.apache.mahout.math.neighborhood.UpdatableSearcher;
import org.plista.kornakapi.core.config.StorageConfiguration;
import org.plista.kornakapi.core.config.StreamingKMeansClustererConfig;
import org.plista.kornakapi.core.training.FactorizationbasedInMemoryTrainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamingKMeansClusterer{

	private  StorageConfiguration storageConfiguration = null;
	private StreamingKMeansClustererConfig conf = null;
	private static final Logger log = LoggerFactory.getLogger(FactorizationbasedInMemoryTrainer.class);
	

	public StreamingKMeansClusterer(StorageConfiguration storageConfiguration, StreamingKMeansClustererConfig conf) {
		this.storageConfiguration = storageConfiguration;
		this.conf = conf;
	}


	public void doTrain() throws Exception {
		
		int clusters = conf.getDesiredNumCluster();
		long cutoff = conf.getDistanceCutoff();

		UpdatableSearcher searcher = new FastProjectionSearch(new ManhattanDistanceMeasure(), 10, 10);
		StreamingKMeans clusterer = new StreamingKMeans(searcher, clusters,cutoff);
		MySqlDataExtractor extractor = new MySqlDataExtractor(storageConfiguration);
		Matrix data = extractor.getData();
		extractor.close();
		UpdatableSearcher centroids = clusterer.cluster(data);		
		System.out.print("Computed "+centroids.size()+ " clusters \n");	
		Iterator<Vector> iter =centroids.iterator();
		while(iter.hasNext()){
			System.out.print(iter.next().size());
		}

	}


}