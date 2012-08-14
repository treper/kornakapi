package org.plista.kornakapi.core.storage;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.hadoop.metrics2.util.TryIterator;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.common.IOUtils;
import org.plista.kornakapi.core.config.StorageConfiguration;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

public class MySqlStorage implements Storage {

  private final BasicDataSource dataSource;
  private final JDBCDataModel dataModel;

  private static final String IMPORT_QUERY =
      "INSERT INTO taste_preferences (user_id, item_id, preference) VALUES (?, ?, ?) " +
      "ON DUPLICATE KEY UPDATE preference = VALUES(preference)";

  public MySqlStorage(StorageConfiguration storageConf) {

    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(storageConf.getJdbcDriverClass());
    dataSource.setUrl(storageConf.getJdbcUrl());
    dataSource.setUsername(storageConf.getUsername());
    dataSource.setPassword(storageConf.getPassword());

    this.dataSource = dataSource;
    /*dataSource.setMaxActive(cfg.asInt("datasourceMaxActive"));
    dataSource.setMinIdle(config().asInt("datasourceMinIdle"));
    dataSource.setInitialSize(config().asInt("datasourceInitialSize"));
    dataSource.setValidationQuery(config().asString("datasourceValidationQuery"));
    dataSource.setTestOnBorrow(config().asBoolean("datasourceTestOnBorrow"));
    dataSource.setTestOnReturn(config().asBoolean("datasourceTestOnReturn"));
    dataSource.setTestWhileIdle(config().asBoolean("datasourceTestWhileIdle"));
    dataSource.setTimeBetweenEvictionRunsMillis(config().asLong("datasourceTimeBetweenEvictionRunsMillis"));    */

    dataModel = new MySQLJDBCDataModel(dataSource);
  }

  @Override
  public DataModel trainingData() throws IOException {
    try {
      return new GenericDataModel(dataModel.exportWithPrefs());
    } catch (TasteException e) {
      throw new IOException(e);
    }
  }

  @Override
  public DataModel recommenderData() throws IOException {
    return dataModel;
  }

  @Override
  public void setPreference(long userID, long itemID, float value) throws IOException {
    try {
      dataModel.setPreference(userID, itemID, value);
    } catch (TasteException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void batchSetPreferences(Iterator<Preference> preferences, int batchSize) throws IOException {
    Connection conn = null;
    PreparedStatement stmt = null;

    try {
      conn = dataSource.getConnection();
      stmt = conn.prepareStatement(IMPORT_QUERY);

      int recordsQueued = 0;

      while (preferences.hasNext()) {
        Preference preference = preferences.next();
        stmt.setLong(1, preference.getUserID());
        stmt.setLong(2, preference.getItemID());
        stmt.setFloat(3, preference.getValue());
        stmt.addBatch();

        if (++recordsQueued % batchSize == 0) {
          stmt.executeBatch();
          System.out.println("Batch import "  + recordsQueued);
        }
      }

      if (recordsQueued % batchSize != 0) {
        stmt.executeBatch();
        System.out.println("Batch import "  + recordsQueued);
      }

    } catch (SQLException e) {
      throw new IOException(e);
    } finally {
      IOUtils.quietClose(stmt);
      IOUtils.quietClose(conn);
    }
  }

  @Override
  public void close() throws IOException {
    try {
      dataSource.close();
    } catch (SQLException e) {
      throw new IOException("Unable to close datasource", e);
    }
  }
}
