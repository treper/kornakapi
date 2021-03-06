package org.plista.kornakapi.core.training;

import java.io.File;
import java.io.IOException;

import org.plista.kornakapi.core.cluster.StreamingKMeansClassifierModel;
import org.plista.kornakapi.core.config.Configuration;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class ExtractTest {
	
	public static void main(String [] args){
		/**
		 * test class
		 */
		String path = args[0];
		System.out.print(path);
		
		File configFile = new File(path);
		System.out.print(configFile.canRead());
		Configuration conf = null;
		try {
			conf = Configuration.fromXML(Files.toString(configFile, Charsets.UTF_8));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StreamingKMeansClustererTrainer clusterer = null;
		StreamingKMeansClassifierModel model = new StreamingKMeansClassifierModel(conf.getStorageConfiguration());
		try {
			clusterer = new StreamingKMeansClustererTrainer(conf.getStreamingKMeansClusterer().iterator().next(), model);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			clusterer.doTrain(configFile, null, 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
