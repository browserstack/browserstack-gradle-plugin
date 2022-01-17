package com.browserstack.gradle;

import java.util.HashMap;

// This class is for getting browserstack configuration from gradle file.
public class BrowserStackConfigExtension {

  private String username = System.getenv("BROWSERSTACK_USERNAME");
  private String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");

  private String configFilePath;
  private String customId;

  public String getUsername() {
    return username;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getConfigFilePath() {
    return configFilePath;
  }

  public String getCustomId() {
    return customId;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public void setConfigFilePath(String filePath) {
    this.configFilePath = filePath;
  }

  public void setCustomId(String customId) {
    this.customId = customId;
  }
}
