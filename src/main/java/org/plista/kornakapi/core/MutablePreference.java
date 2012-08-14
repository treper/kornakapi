package org.plista.kornakapi.core;

import org.apache.mahout.cf.taste.model.Preference;

public class MutablePreference implements Preference {

  private long userID;
  private long itemID;
  private float value;

  public void set(long userID, long itemID, float value) {
    this.userID = userID;
    this.itemID = itemID;
    setValue(value);
  }

  @Override
  public long getUserID() {
    return userID;
  }

  @Override
  public long getItemID() {
    return itemID;
  }

  @Override
  public float getValue() {
    return value;
  }

  @Override
  public void setValue(float value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "MutablePreference{" + "userID=" + userID + ", itemID=" + itemID + ", value=" + value + '}';
  }
}
