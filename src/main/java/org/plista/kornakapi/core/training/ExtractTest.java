package org.plista.kornakapi.core.training;

import java.io.File;
import java.io.IOException;

import org.apache.mahout.math.Centroid;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.WeightedVector;
import org.plista.kornakapi.core.cluster.StreamingKMeansClassifierModel;
import org.plista.kornakapi.core.config.Configuration;
import org.plista.kornakapi.core.storage.MySqlDataExtractor;

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
		MySqlDataExtractor extractor = new MySqlDataExtractor(conf.getStorageConfiguration());
		StreamingKMeansClustererTrainer clusterer = null;
		StreamingKMeansClassifierModel model = new StreamingKMeansClassifierModel();
		try {
			clusterer = new StreamingKMeansClustererTrainer(conf.getStorageConfiguration(), conf.getStreamingKMeansClusterer().iterator().next(), model);
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
		RandomAccessSparseVector hans = new RandomAccessSparseVector(12, 23);
		WeightedVector peter = new WeightedVector(hans,1,1);  
		hans.set(5, 10);
		Centroid hanspeter = new Centroid(peter);
		clusterer.streamNewPoint(hanspeter);
	}

}
