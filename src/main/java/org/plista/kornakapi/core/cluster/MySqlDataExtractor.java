package org.plista.kornakapi.core.cluster;



import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.common.IOUtils;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.SparseMatrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.plista.kornakapi.core.config.StorageConfiguration;
import org.plista.kornakapi.core.storage.MySqlStorage;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;

import com.google.common.io.Closeables;



public class MySqlDataExtractor extends MySqlStorage{
	

	private static final String GET_USER = "select user_id from (SELECT user_id, COUNT(user_id) AS nums FROM taste_preferences GROUP BY user_id ORDER BY nums DESC) as ns where nums > 20 && nums <30";
	private static final String test = "SELECT * FROM taste_preferences";
	private static String GET_USER_ITEMS_BASE = "SELECT item_id FROM taste_preferences WHERE user_id = ";
	private int dim;

	

	
	
	
	 public MySqlDataExtractor(StorageConfiguration storageConf){
			super(storageConf);
	  }
	 
	 public StreamingKMeansDataObject getData(){	
		 	FastIDSet userids = this.getQuery(GET_USER);
		 	HashMap<Long, FastIDSet> userItemIds = new HashMap<Long, FastIDSet>();
		 	FastIDSet allItems = new FastIDSet();
		 	this.dim = userids.size();
		 	for(long userid : userids.toArray()){
		 		String getUserItems = this.GET_USER_ITEMS_BASE + String.valueOf(userid);
		 		FastIDSet userItems = getQuery(getUserItems);
		 		allItems.addAll(userItems);
		 		userItemIds.put(userid, userItems);
		 	}
		 	HashMap<Integer, RandomAccessSparseVector> vectors = new HashMap<Integer, RandomAccessSparseVector>();
		 	int n = 0;
		 	for(long itemId : allItems.toArray()){
		 		RandomAccessSparseVector itemVector = new RandomAccessSparseVector(dim, dim);
				int i = 0;
		 		for(long userid : userids.toArray()){
		 			
		 			FastIDSet itemIds = userItemIds.get(userid);
		 			if(itemIds.contains(itemId)){
		 				itemVector.set(i, 1);
		 			}
		 			i++;
		 		}
		 		vectors.put(n, itemVector);
		 		n++;	 		
		 	}
		 	
		 return new StreamingKMeansDataObject(userids, userItemIds, allItems, new SparseMatrix(n,dim,vectors), dim);
	 }
	 
	 public class StreamingKMeansDataObject{
		 private FastIDSet userids;
		 private HashMap<Long, FastIDSet> userItemIds;
		 private FastIDSet allItems;
		 private int dim;
		 private SparseMatrix matrix;
		 
		 public StreamingKMeansDataObject(FastIDSet userids, HashMap<Long, FastIDSet> userItemIds, FastIDSet allItems, SparseMatrix matrix, int dim){
			 this.userids = userids;
			 this.userItemIds = userItemIds;
			 this.allItems = allItems;
			 this.dim = dim;
			 this.matrix = matrix;
		 }
		 
		 public FastIDSet getUserIDs(){
			 return this.userids;
		 }
		 public HashMap<Long, FastIDSet> getUserItemIDs(){
			 return this.userItemIds;
		 }
		 
		 public FastIDSet getAllItems(){
			 return this.allItems;
		 }
		 
		 public Matrix getMatrix(){
			 return this.matrix;
		 }
		 
		 public int getDim(){
			 return this.dim;
		 }
		 
	 }
	 
	public FastIDSet getQuery(String query){
		 Connection conn = null;
		 PreparedStatement stmt = null;
		 ResultSet rs = null;
		 FastIDSet candidates = new FastIDSet();
		 
		try {
		      conn = dataSource.getConnection();
		      stmt = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY,
		          ResultSet.CONCUR_READ_ONLY);
		      stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
		      stmt.setFetchSize(1000);
		      rs = stmt.executeQuery();
		      while (rs.next()) {
		        candidates.add(rs.getLong(1));
		      }
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
		      IOUtils.quietClose(stmt);
		      IOUtils.quietClose(conn);
		}
		return candidates;
	}
	



}
