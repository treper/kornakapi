package org.plista.kornakapi.core.config;

import com.thoughtworks.xstream.XStream;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConfigurationTest {

  @Test
  public void deserialize() {

    String xml =
        "<configuration>\n" +
        "  <modelDirectory>/tmp/models</modelDirectory>\n" +
        "  <storageConfiguration>\n" +
        "    <jdbcDriverClass>com.mysql.jdbc.Driver</jdbcDriverClass>\n" +
        "    <jdbcUrl>jdbc:mysql://localhost/plista</jdbcUrl>\n" +
        "    <username>root</username>\n" +
        "    <password>secret</password>\n" +
        "  </storageConfiguration>\n" +
        "  <itembasedRecommenders>\n" +
        "    <itembasedRecommender>\n" +
        "      <name>itembased</name>\n" +
        "      <similarityClass>org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity</similarityClass>\n" +
        "      <similarItemsPerItem>25</similarItemsPerItem>\n" +
        "    </itembasedRecommender>\n" +
        "  </itembasedRecommenders>\n" +
        "  <factorizationbasedRecommenders>\n" +
        "    <factorizationbasedRecommender>\n" +
        "      <name>weighted-mf</name>\n" +
        "      <usesImplicitFeedback>true</usesImplicitFeedback>\n" +
        "      <numberOfFeatures>10</numberOfFeatures>\n" +
        "      <numberOfIterations>10</numberOfIterations>\n" +
        "      <lambda>0.01</lambda>\n" +
        "      <alpha>40.0</alpha>\n" +
        "    </factorizationbasedRecommender>\n" +
        "  </factorizationbasedRecommenders>\n" +
        "</configuration>";

    Configuration conf = Configuration.fromXML(xml);

    assertEquals("/tmp/models", conf.getModelDirectory());

    StorageConfiguration storageConf = conf.getStorageConfiguration();
    assertNotNull(storageConf);
    assertEquals("com.mysql.jdbc.Driver", storageConf.getJdbcDriverClass());
    assertEquals("jdbc:mysql://localhost/plista", storageConf.getJdbcUrl());
    assertEquals("root", storageConf.getUsername());
    assertEquals("secret", storageConf.getPassword());

    List<ItembasedRecommenderConfig> itembasedRecommenders = conf.getItembasedRecommenders();
    assertNotNull(itembasedRecommenders);
    assertEquals(1, itembasedRecommenders.size());

    ItembasedRecommenderConfig itembasedRecommenderConf = itembasedRecommenders.get(0);
    assertEquals("itembased", itembasedRecommenderConf.getName());
    assertEquals(LogLikelihoodSimilarity.class.getName(), itembasedRecommenderConf.getSimilarityClass());
    assertEquals(25, itembasedRecommenderConf.getSimilarItemsPerItem());

    List<FactorizationbasedRecommenderConfig> factorizationbasedRecommenders = conf.getFactorizationbasedRecommenders();
    assertNotNull(factorizationbasedRecommenders);
    assertEquals(1, factorizationbasedRecommenders.size());

    FactorizationbasedRecommenderConfig factorizationbasedRecommenderConf = factorizationbasedRecommenders.get(0);
    assertEquals("weighted-mf", factorizationbasedRecommenderConf.getName());
    assertTrue(factorizationbasedRecommenderConf.isUsesImplicitFeedback());
    assertEquals(10, factorizationbasedRecommenderConf.getNumberOfFeatures());
    assertEquals(10, factorizationbasedRecommenderConf.getNumberOfIterations());
    assertEquals(0.01, factorizationbasedRecommenderConf.getLambda(), 0);
    assertEquals(40, factorizationbasedRecommenderConf.getAlpha(), 0);
  }

  @Test
  public void serialize() {

    StorageConfiguration storageConf = new StorageConfiguration();
    storageConf.setJdbcDriverClass("com.mysql.jdbc.Driver");
    storageConf.setJdbcUrl("jdbc:mysql://localhost/plista");
    storageConf.setUsername("root");
    storageConf.setPassword("secret");

    Configuration conf = new Configuration();

    conf.setModelDirectory("/tmp/models");
    conf.setStorageConfiguration(storageConf);

    ItembasedRecommenderConfig itembasedRecommenderConf = new ItembasedRecommenderConfig();
    itembasedRecommenderConf.setName("itembased");
    itembasedRecommenderConf.setSimilarityClass(LogLikelihoodSimilarity.class.getName());
    itembasedRecommenderConf.setSimilarItemsPerItem(25);

    conf.addItembasedRecommender(itembasedRecommenderConf);

    FactorizationbasedRecommenderConfig factorizationbasedRecommenderConf = new FactorizationbasedRecommenderConfig();
    factorizationbasedRecommenderConf.setName("weighted-mf");
    factorizationbasedRecommenderConf.setNumberOfFeatures(10);
    factorizationbasedRecommenderConf.setNumberOfIterations(10);
    factorizationbasedRecommenderConf.setUsesImplicitFeedback(true);
    factorizationbasedRecommenderConf.setLambda(0.01);
    factorizationbasedRecommenderConf.setAlpha(40);

    conf.addFactorizationbasedRecommender(factorizationbasedRecommenderConf);

    XStream serializer = new XStream();
    serializer.alias("configuration", Configuration.class);
    serializer.alias("itembasedRecommender", ItembasedRecommenderConfig.class);
    serializer.alias("factorizationbasedRecommender", FactorizationbasedRecommenderConfig.class);

    serializer.toXML(conf, System.out);
  }


}

