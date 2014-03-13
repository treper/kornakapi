package org.plista.kornakapi.core.recommender;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.math.Centroid;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.neighborhood.UpdatableSearcher;
import org.apache.mahout.math.random.WeightedThing;
import org.plista.kornakapi.KornakapiRecommender;
import org.plista.kornakapi.core.cluster.MySqlDataExtractor;

public class StreamingKMeansClassifierRecommender implements KornakapiRecommender{
	
	private UpdatableSearcher centroids;
	private MySqlDataExtractor extractor;
	private long meanVolume = 0;
	
	public StreamingKMeansClassifierRecommender(UpdatableSearcher centroids,MySqlDataExtractor extractor ){
		this.centroids = centroids;
		this.extractor = extractor;
	}
	
	public void init(){
		Iterator<Vector> iter =centroids.iterator();
		double maxWeight = 0;

		while(iter.hasNext()){
			Centroid cent = (Centroid) iter.next();
			double weight =cent.getWeight();
			if(weight > maxWeight){
				maxWeight = weight;
			}
		}
		iter =centroids.iterator();
		int i = 0;
		while(iter.hasNext()){
			Centroid cent = (Centroid)iter.next();
			meanVolume += cent.getWeight()/maxWeight* cent.getNumNonZeroElements();
			i++;
		}
		meanVolume = meanVolume/i;
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
	public List<RecommendedItem> recommendToAnonymous(long[] itemIDs,
			int howMany, IDRescorer rescorer) throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public double recommendToAnonymous(long[] itemIDs) throws TasteException {
		long weight = 0;

		for(long itemId : itemIDs){
			WeightedThing<Vector> centroid= centroids.searchFirst(extractor.getVector(itemId), true);
			weight += centroid.getWeight();

		}
		weight = (weight/itemIDs.length)/meanVolume;
		return weight;
	}
	

}
