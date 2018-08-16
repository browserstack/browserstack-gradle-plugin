package com.browserstack.gradle;

// This class is for getting browserstack configuration from gradle file.
public class BrowserStackConfigExtension {

  private String username = System.getenv("BROWSERSTACK_USERNAME");
  private String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");

  private String[] classes, annotations, packages, sizes, otherApps;

  private boolean video = Constants.DEFAULT_VIDEO;
  private boolean deviceLogs = Constants.DEFAULT_DEVICE_LOGS;
  private boolean local = Constants.DEFAULT_LOCAL;
  private boolean networkLogs = Constants.DEFAULT_NETWORK_LOGS;

  String[] devices;

  private String callbackURL, localIdentifier, networkProfile;

  public String getUsername() {
    return username;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String[] getClasses() {
    if (classes == null) {
      classes = new String[0];
    }
    return classes;
  }

  public String[] getAnnotations() {
    if (annotations == null) {
      annotations = new String[0];
    }
    return annotations;
  }

  public String[] getPackages() {
    if (packages == null) {
      packages = new String[0];
    }
    return packages;
  }

  public String[] getSizes() {
    if (sizes == null) {
      sizes = new String[0];
    }
    return sizes;
  }

  public String[] getOtherApps() {
    if (otherApps == null) {
      otherApps = new String[0];
    }
    return otherApps;
  }

  public boolean isVideo() {
    return video;
  }

  public boolean isDeviceLogs() {
    return deviceLogs;
  }

  public boolean isLocal() {
    return local;
  }

  public boolean isNetworkLogs() {
    return networkLogs;
  }

  public String[] getDevices() {
    return devices;
  }

  public String getCallbackURL() {
    return callbackURL;
  }

  public String getLocalIdentifier() {
    return localIdentifier;
  }

  public String getNetworkProfile() {
    return networkProfile;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public void setClasses(String[] classes) {
    this.classes = classes;
  }

  public void setAnnotations(String[] annotations) {
    this.annotations = annotations;
  }

  public void setPackages(String[] packages) {
    this.packages = packages;
  }

  public void setSizes(String[] sizes) {
    this.sizes = sizes;
  }

  public void setOtherApps(String[] otherApps) {
    this.otherApps = otherApps;
  }

  public void setVideo(boolean video) {
    this.video = video;
  }

  public void setDeviceLogs(boolean deviceLogs) {
    this.deviceLogs = deviceLogs;
  }

  public void setLocal(boolean local) {
    this.local = local;
  }

  public void setNetworkLogs(boolean networkLogs) {
    this.networkLogs = networkLogs;
  }

  public void setDevices(String[] devices) {
    this.devices = devices;
  }

  public void setCallbackURL(String callbackURL) {
    this.callbackURL = callbackURL;
  }

  public void setLocalIdentifier(String localIdentifier) {
    this.localIdentifier = localIdentifier;
  }

  public void setNetworkProfile(String networkProfile) {
    this.networkProfile = networkProfile;
  }
}
