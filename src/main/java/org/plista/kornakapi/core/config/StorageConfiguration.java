package org.plista.kornakapi.core.config;

public class StorageConfiguration {

  private String jdbcDriverClass;
  private String jdbcUrl;
  private String username;
  private String password;

  public String getJdbcDriverClass() {
    return jdbcDriverClass;
  }

  public void setJdbcDriverClass(String jdbcDriverClass) {
    this.jdbcDriverClass = jdbcDriverClass;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
