
package org.plista.kornakapi.core.training;


import java.io.File;
import java.io.IOException;

import org.apache.mahout.cf.taste.model.DataModel;
import org.plista.kornakapi.core.cluster.StreamingKMeansClassifierModel;
import org.plista.kornakapi.core.config.StreamingKMeansClustererConfig;
public class StreamingKMeansClustererTrainer extends AbstractTrainer{


	StramingKMeansClusterer clusterer;
	long start;
	private boolean firstTraining = true;
	private long clusterTimeWindow;
	

	public StreamingKMeansClustererTrainer(StreamingKMeansClustererConfig conf, StreamingKMeansClassifierModel model) throws IOException {
		super(conf);
		clusterer = new StramingKMeansClusterer(model, conf.getDesiredNumCluster(), conf.getDistanceCutoff());
		clusterTimeWindow = conf.getClusterTimeWindow();
	    start = System.currentTimeMillis();
		

	}

	@Override
	protected void doTrain(File targetFile, DataModel inmemoryData,
			int numProcessors) throws IOException {

		if(start - System.currentTimeMillis() > clusterTimeWindow || firstTraining){ //if 12 hours passed retrain hole model
			clusterer.cluster();
			start = System.currentTimeMillis();
			firstTraining = false;
		}else{
			clusterer.stream();
		}
				
	}

}

