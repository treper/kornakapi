package org.plista.kornakapi.core.recommender;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.math.Centroid;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.neighborhood.UpdatableSearcher;
import org.apache.mahout.math.random.WeightedThing;
import org.plista.kornakapi.KornakapiRecommender;
import org.plista.kornakapi.core.cluster.MySqlDataExtractor;
import org.plista.kornakapi.core.cluster.StreamingKMeansClassifierModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class StreamingKMeansClassifierRecommender implements KornakapiRecommender{
	

	private MySqlDataExtractor extractor;
	private static final Logger log = LoggerFactory.getLogger(StreamingKMeansClassifierRecommender.class);
	private StreamingKMeansClassifierModel model; 
	
	public StreamingKMeansClassifierRecommender(MySqlDataExtractor extractor, StreamingKMeansClassifierModel model ){
		this.extractor = extractor;
		this.model = model;
	}
	

	@Override
	public List<RecommendedItem> recommend(long userID, int howMany)
			throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RecommendedItem> recommend(long userID, int howMany,
			IDRescorer rescorer) throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float estimatePreference(long userID, long itemID)
			throws TasteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPreference(long userID, long itemID, float value)
			throws TasteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removePreference(long userID, long itemID)
			throws TasteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DataModel getDataModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		// TODO Auto-generated method stub
		
	}

	@Override

	public List<RecommendedItem>  recommendToAnonymous(long[] itemIDs,
			int howMany, IDRescorer rescorer) throws TasteException {

		List<RecommendedItem> result = Lists.newArrayListWithCapacity(itemIDs.length);
		if(model.getCentroids() == null){
			throw new TasteException("No centroids computed");
		}
		for(long itemId : itemIDs){
			WeightedThing<Vector> centroid;
			try {	
				centroid = model.getCentroids().searchFirst(extractor.getVector(itemId), true);
				System.out.print("MaxWeight" + model.getMaxWeight() + "meanVolume" + model.getMeanVolume()+ "Centroid weight"+ centroid.getWeight());
				float volume = ((float)centroid.getWeight()/(float)model.getMaxWeight()) ;
				/**
				 * TODO: new Version of mahout is supposed to allow acces on the centroid as a vector
				 * then volume should be normalized by ((Vector)centroid).getNumNonZeroElements
				 */
				GenericRecommendedItem item = new GenericRecommendedItem(itemId,  volume/(float)this.model.getMeanVolume());
				result.add(item);
			} catch (IOException e) {
			    if (log.isInfoEnabled()) {
			    	log.info("{}",  e.getMessage()); 			    			
			    }
			}
		}		
		return result;
	}
	

}
