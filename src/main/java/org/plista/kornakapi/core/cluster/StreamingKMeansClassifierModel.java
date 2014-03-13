package org.plista.kornakapi.core.cluster;

import java.util.Iterator;

import org.apache.mahout.math.Centroid;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.neighborhood.UpdatableSearcher;

public class StreamingKMeansClassifierModel {

	private UpdatableSearcher centroids = null;
	
	private double maxWeight = 0;
	private double meanVolume=0;
	
	public void updateCentroids (UpdatableSearcher centroids){
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
}
