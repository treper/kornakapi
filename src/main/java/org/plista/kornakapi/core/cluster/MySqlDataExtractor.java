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
	private FastIDSet allItems;
	private HashMap<Long, FastIDSet> userItemIds;
	private FastIDSet userids;
	

	
	
	
	 public MySqlDataExtractor(StorageConfiguration storageConf){
			super(storageConf);
			this.init();
	  }
	 
	 public void init(){
		 	this.userids = this.getQuery(GET_USER);
		 	this.userItemIds = new HashMap<Long, FastIDSet>();
		 	this.allItems = new FastIDSet();
		 	this.dim = userids.size();
		 	for(long userid : userids.toArray()){
		 		String getUserItems = this.GET_USER_ITEMS_BASE + String.valueOf(userid);
		 		FastIDSet userItems = getQuery(getUserItems);
		 		allItems.addAll(userItems);
		 		userItemIds.put(userid, userItems);
		 	}
	 }
	
	 public Matrix getData(){		 	
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
		 return new SparseMatrix(n,dim,vectors);
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
	
	/**
	 * Returns the RandomAccessSparseVector of an item id
	 * @param itemId
	 * @return RandomAccessSparseVector
	 */
	public Vector getVector(long itemId){
		RandomAccessSparseVector itemVector = new RandomAccessSparseVector(dim, dim);
		int i = 0;
 		for(long userid : userids.toArray()){
 			
 			FastIDSet itemIds = userItemIds.get(userid);
 			if(itemIds.contains(itemId)){
 				itemVector.set(i, 1);
 			}
 			i++;
 		}		
		return itemVector;
	}


}
