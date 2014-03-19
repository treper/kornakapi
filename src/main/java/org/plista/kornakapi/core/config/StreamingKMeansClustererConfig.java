package org.plista.kornakapi.core.config;


public class StreamingKMeansClustererConfig extends RecommenderConfig{	
	
	private int desiredNumClusters;
	
	private long distanceCutoff;
	
	private long clusterTimeWindow;
	
	
	public int getDesiredNumCluster(){
		return this.desiredNumClusters;
	}
	
	public long getDistanceCutoff(){
		return this.distanceCutoff;
	}
	
	public long getClusterTimeWindow(){
		return clusterTimeWindow;
	}
	public void setClusterTimeWindow(long clusterTimeWindow){
		this.clusterTimeWindow = clusterTimeWindow;
	}
}
