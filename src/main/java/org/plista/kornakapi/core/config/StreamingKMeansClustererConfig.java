package org.plista.kornakapi.core.config;


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
