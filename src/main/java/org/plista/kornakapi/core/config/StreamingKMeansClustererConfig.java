package org.plista.kornakapi.core.config;

import org.apache.mahout.common.distance.DistanceMeasure;

public class StreamingKMeansClustererConfig extends RecommenderConfig{	
	
	private int desiredNumClusters;
	
	private long distanceCutoff;
	
	
	public int getDesiredNumCluster(){
		return this.desiredNumClusters;
	}
	
	public long getDistanceCutoff(){
		return this.distanceCutoff;
	}
}
