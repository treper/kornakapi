package org.plista.kornakapi.core.config;

public class ItembasedRecommenderConfig {

  private String name;
  private String similarityClass;
  private int similarItemsPerItem;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSimilarityClass() {
    return similarityClass;
  }

  public void setSimilarityClass(String similarityClass) {
    this.similarityClass = similarityClass;
  }

  public int getSimilarItemsPerItem() {
    return similarItemsPerItem;
  }

  public void setSimilarItemsPerItem(int similarItemsPerItem) {
    this.similarItemsPerItem = similarItemsPerItem;
  }
}
