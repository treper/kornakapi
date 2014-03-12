package org.plista.kornakapi.core.cluster;

import java.io.File;
import java.io.IOException;

import org.plista.kornakapi.core.config.Configuration;
import org.plista.kornakapi.core.storage.CandidateCacheStorageDecorator;
import org.plista.kornakapi.core.storage.MySqlStorage;
import org.plista.kornakapi.core.storage.Storage;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class ExtractTest {
	
	public static void main(String [] args){
		
		String path = args[0];
		
		File configFile = new File(path);
		Configuration conf = null;
		try {
			conf = Configuration.fromXML(Files.toString(configFile, Charsets.UTF_8));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		StreamingKMeansClusterer clusterer = new StreamingKMeansClusterer(conf.getStorageConfiguration(), conf.getStreamingKMeansClusterer().iterator().next());
		try {
			clusterer.doTrain();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
