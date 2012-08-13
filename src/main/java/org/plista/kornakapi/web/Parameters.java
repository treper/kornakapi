package org.plista.kornakapi.web;

public class Parameters {

  public static final String METHOD = "method";
  public static final String USER_ID = "userID";
  public static final String ITEM_ID = "itemID";
  public static final String VALUE = "value";
  public static final String HOW_MANY = "howMany";
  public static final String FILE = "file";
  public static final String BATCH_SIZE = "batchSize";

  public static final int DEFAULT_HOW_MANY = 10;
  public static final int DEFAULT_BATCH_SIZE = 10000;

  private Parameters() {
    throw new IllegalStateException();
  }
}
