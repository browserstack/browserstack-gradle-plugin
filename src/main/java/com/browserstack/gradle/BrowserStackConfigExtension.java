package com.browserstack.gradle;

import java.util.HashMap;

// This class is for getting browserstack configuration from gradle file.
public class BrowserStackConfigExtension {

  private String username = System.getenv("BROWSERSTACK_USERNAME");
  private String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");

  // Internal dev/staging override: BROWSERSTACK_STAGING_ENV points the App-Automate REST calls
  // (Espresso / App-Live / App-Automate uploads + build) at a named staging env instead of prod,
  // mirroring the BrowserStack SDK's BROWSERSTACK_STAGING_ENV convention. See SDK-6948.
  private String stagingEnv = System.getenv("BROWSERSTACK_STAGING_ENV");
  // Optional explicit host override from the gradle DSL (wins over stagingEnv / prod when set).
  private String host;

  private String configFilePath;
  private String customId;

  /**
   * Enables debugging with more verbose logs
   */
  private boolean isDebug = false;

  public String getUsername() {
    return username;
  }

  public String getAccessKey() {
    return accessKey;
  }

  // Resolves the App-Automate API host. An explicit DSL host wins; otherwise
  // BROWSERSTACK_STAGING_ENV localizes to a staging env (a bare namespace token ->
  // https://api-cloud-<env>.bsstag.com; a value containing a dot is treated as a full host and
  // used verbatim as https://<value>); otherwise the production default. Mirrors the BrowserStack
  // SDK binary's config-server host resolution.
  public String getHost() {
    if (host != null && !host.trim().isEmpty()) {
      return host;
    }
    if (stagingEnv == null || stagingEnv.trim().isEmpty()) {
      return Constants.BROWSERSTACK_API_HOST;
    }
    String env = stagingEnv.trim();
    if (env.contains(".")) {
      return "https://" + env;
    }
    return "https://api-cloud-" + env + ".bsstag.com";
  }

  public String getConfigFilePath() {
    return configFilePath;
  }

  public String getCustomId() {
    return customId;
  }

  public boolean isDebug() {
    return isDebug;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setConfigFilePath(String filePath) {
    this.configFilePath = filePath;
  }

  public void setCustomId(String customId) {
    this.customId = customId;
  }

  public void setDebug(boolean debug) {
    isDebug = debug;
  }
}
