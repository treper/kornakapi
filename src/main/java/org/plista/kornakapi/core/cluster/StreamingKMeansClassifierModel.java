package org.plista.kornakapi.core.cluster;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.math.Centroid;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.neighborhood.UpdatableSearcher;
import org.plista.kornakapi.core.cluster.MySqlDataExtractor.StreamingKMeansDataObject;

public class StreamingKMeansClassifierModel {

	private UpdatableSearcher centroids = null;
	
	private double maxWeight = 0;
	private double meanVolume=0;
	private HashMap<Long, RandomAccessSparseVector> itemID2Vector = new HashMap<Long, RandomAccessSparseVector>();
	private FastIDSet allItems = null;
	private HashMap<Long, FastIDSet> userItemIds = null;
	private FastIDSet userids = null;
	private int dim=0;
	
/**
 * Method that updates the model if new centroids are callculated
 * @param data
 * @param centroids
 */
	public void updateCentroids (StreamingKMeansDataObject data, UpdatableSearcher centroids){
		this.allItems = data.getAllItems();
		this.userItemIds = data.getUserItemIDs();		
		this.userids = data.getUserIDs();
		this.dim = data.getDim();		
		this.centroids = centroids;
		System.out.print("Computed "+centroids.size()+ " clusters \n");	
		Iterator<Vector> iter =centroids.iterator();
		while(iter.hasNext()){
			Centroid cent = (Centroid) iter.next();
			double weight =cent.getWeight();
			if(weight > maxWeight){
				maxWeight = weight;
			}
		}
		System.out.print("maxWeight= " +maxWeight + "\n");
		iter =centroids.iterator();
		int i = 0;
		while(iter.hasNext()){		
			Centroid cent = (Centroid) iter.next();
			meanVolume += cent.getWeight()/maxWeight* cent.getNumNonZeroElements();
			i++;
			System.out.print("Weight= " +cent.getWeight()+ ", l2norm= " +cent.norm(2) + " num non zero elems= "+cent.getNumNonZeroElements() + " Volume= " + (cent.getWeight()/maxWeight)* cent.getNumNonZeroElements());
			System.out.print("\n");	
		}
		meanVolume = meanVolume/i;
		this.itemID2Vector.clear();
	}
	public UpdatableSearcher getCentroids(){
		return this.centroids;
	}
	
	public double getMaxWeight(){
		return this.maxWeight;
	}
	
	public double getMeanVolume(){
		return this.meanVolume;
	}
	
	public void setVector(long itemID, RandomAccessSparseVector itemVector){
		itemID2Vector.put(itemID, itemVector);
	}
	
	/**
	 * checks if vector of itemID is allready computed in hashmap, computes it if not and returns it
	 * @param itemId
	 * @return
	 * @throws IOException
	 */
	public RandomAccessSparseVector getVector(long itemId) throws IOException{
		if(itemID2Vector.containsKey(itemId)){
			System.out.print("Get item from chache");
			return this.itemID2Vector.get(itemId);
		}else{
			System.out.print("Create new Item");
			RandomAccessSparseVector itemVector = createVector(itemId);
			setVector(itemId, itemVector);
			return itemVector;
		}
		
	}

	/**
	 * Returns the RandomAccessSparseVector of an item id
	 * @param itemId
	 * @return RandomAccessSparseVector
	 * @throws IOException 
	 */
	private RandomAccessSparseVector createVector(long itemId) throws IOException{
		RandomAccessSparseVector itemVector = new RandomAccessSparseVector(dim, dim);
		int i = 0;
		boolean isRated = false;
 		for(long userid : userids.toArray()){ 			
 			FastIDSet itemIds = userItemIds.get(userid);
 			if(itemIds.contains(itemId)){
 				itemVector.set(i, 1);
 				isRated = true;
 			}
 			i++;
 		}	
 		if(isRated){
 			return itemVector;
 		}else{
 			throw new IOException("Item unknown");
 		}
	}
	
}
