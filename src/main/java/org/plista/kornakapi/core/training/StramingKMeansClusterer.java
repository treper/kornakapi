package org.plista.kornakapi.core.training;

import java.util.List;

import org.apache.mahout.clustering.streaming.cluster.StreamingKMeans;
import org.apache.mahout.common.distance.ManhattanDistanceMeasure;
import org.apache.mahout.math.Centroid;
import org.apache.mahout.math.neighborhood.FastProjectionSearch;
import org.apache.mahout.math.neighborhood.UpdatableSearcher;
import org.plista.kornakapi.core.cluster.StreamingKMeansClassifierModel;
import org.plista.kornakapi.core.storage.MySqlDataExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StramingKMeansClusterer {
	MySqlDataExtractor extractor;
	StreamingKMeansClassifierModel model;
	int clusters;
	long cutoff;
	StreamingKMeans clusterer;
	
	private static final Logger log = LoggerFactory.getLogger(StramingKMeansClusterer.class);
	
	public StramingKMeansClusterer(StreamingKMeansClassifierModel model,int clusters,long cutoff){
		this.model = model;
		this.clusters = clusters;
		this.cutoff = cutoff;
		UpdatableSearcher searcher = new FastProjectionSearch(new ManhattanDistanceMeasure(), 10, 10);	
		clusterer = new StreamingKMeans(searcher, clusters,cutoff);
	}
	/**
	 * retrain model
	 */
	public void cluster(){
	    long start = System.currentTimeMillis();
	    List<Centroid> data = model.getData();
		UpdatableSearcher centroids = clusterer.cluster(data);
		long estimateDuration = System.currentTimeMillis() - start;  
		this.model.updateCentroids(centroids);
		if (log.isInfoEnabled()) {
			log.info("Model trained in {} ms, created [{}] Clusters", estimateDuration, centroids.size());
		}
	}
	/**
	 * just stream new available data-points into old coordinate system
	 */
	public void stream(){
	    long start = System.currentTimeMillis();
		List<Centroid> data = model.getNewData();
		UpdatableSearcher centroids = clusterer.cluster(data);
		long estimateDuration = System.currentTimeMillis() - start;  
		this.model.updateCentroids(centroids);	
		if (log.isInfoEnabled()) {
			log.info("Model trained in {} ms, created [{}] Clusters", estimateDuration, centroids.size());
		}
	}
}
