package org.plista.kornakapi.core.storage;

import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public interface Storage extends Closeable {

  DataModel trainingData() throws IOException;

  DataModel recommenderData() throws IOException;

  void setPreference(long userID, long itemID, float value) throws IOException;

  void batchSetPreferences(Iterator<Preference> preferences, int batchSize) throws IOException;
}
