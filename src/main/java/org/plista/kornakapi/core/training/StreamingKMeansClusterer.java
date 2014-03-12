
package org.plista.kornakapi.core.training;


import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.clustering.streaming.cluster.StreamingKMeans;
import org.apache.mahout.common.distance.ManhattanDistanceMeasure;
import org.apache.mahout.math.Centroid;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.neighborhood.FastProjectionSearch;
import org.apache.mahout.math.neighborhood.UpdatableSearcher;
import org.plista.kornakapi.core.cluster.MySqlDataExtractor;
import org.plista.kornakapi.core.config.StorageConfiguration;
import org.plista.kornakapi.core.config.StreamingKMeansClustererConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamingKMeansClusterer extends AbstractTrainer{

	private  StorageConfiguration storageConfiguration = null;
	private StreamingKMeansClustererConfig conf = null;
	private static final Logger log = LoggerFactory.getLogger(FactorizationbasedInMemoryTrainer.class);
	

	public StreamingKMeansClusterer(StorageConfiguration storageConfiguration, StreamingKMeansClustererConfig conf) {
		super(conf);
		this.storageConfiguration = storageConfiguration;
		this.conf = conf;
	}

	@Override
	protected void doTrain(File targetFile, DataModel inmemoryData,
			int numProcessors) throws IOException {
		/**
		 * class to calculate clusters
		 */
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
		double maxWeight = 0;
		while(iter.hasNext()){
			Centroid cent = (Centroid) iter.next();
			double weight =cent.getWeight();
			if(weight > maxWeight){
				maxWeight = weight;
			}
		}
		System.out.print(maxWeight + "\n");
		iter =centroids.iterator();
		while(iter.hasNext()){
			
			Centroid cent = (Centroid) iter.next();
			if(cent.getWeight()>1){
				System.out.print("Weight= " +cent.getWeight()+ ", l2norm= " +cent.norm(2) + " num non zero elems= "+cent.getNumNonZeroElements() + " Volume= " + (cent.getWeight()/maxWeight)/ cent.getNumNonZeroElements());
				System.out.print("\n");
			}
			
		}
	}
}

