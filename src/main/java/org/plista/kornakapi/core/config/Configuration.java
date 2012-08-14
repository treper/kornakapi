package org.plista.kornakapi.core.config;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;

import java.util.List;

public class Configuration {

  private String modelDirectory;
  private StorageConfiguration storageConfiguration;

  private List<ItembasedRecommenderConfig> itembasedRecommenders = Lists.newArrayList();
  private List<FactorizationbasedRecommenderConfig> factorizationbasedRecommenders = Lists.newArrayList();

  public static Configuration fromXML(String xml) {
    XStream serializer = new XStream();
    serializer.alias("configuration", Configuration.class);
    serializer.alias("itembasedRecommender", ItembasedRecommenderConfig.class);
    serializer.alias("factorizationbasedRecommender", FactorizationbasedRecommenderConfig.class);

    return (Configuration) serializer.fromXML(xml);
  }

  public String getModelDirectory() {
    return modelDirectory;
  }

  public List<ItembasedRecommenderConfig> getItembasedRecommenders() {
    return itembasedRecommenders;
  }

  public void addItembasedRecommender(ItembasedRecommenderConfig itembasedRecommender) {
    itembasedRecommenders.add(itembasedRecommender);
  }

  public List<FactorizationbasedRecommenderConfig> getFactorizationbasedRecommenders() {
    return factorizationbasedRecommenders;
  }

  public void addFactorizationbasedRecommender(FactorizationbasedRecommenderConfig factorizationbasedRecommender) {
    factorizationbasedRecommenders.add(factorizationbasedRecommender);
  }

  public void setModelDirectory(String modelDirectory) {
    this.modelDirectory = modelDirectory;
  }

  public StorageConfiguration getStorageConfiguration() {
    return storageConfiguration;
  }

  public void setStorageConfiguration(StorageConfiguration storageConfiguration) {
    this.storageConfiguration = storageConfiguration;
  }
}
