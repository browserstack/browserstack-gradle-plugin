package com.browserstack.gradle;

import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.Input;
import java.net.HttpURLConnection;

import java.util.Map;
import org.gradle.api.tasks.Optional;
import java.nio.file.Path;
import com.browserstack.json.JSONObject;
import com.browserstack.httputils.HttpUtils;
import com.browserstack.gradle.Constants;

public class EspressoTask extends BrowserStackTask {

  @Input
  private String[] classes, annotations, packages, sizes, otherApps;

  @Input
  private boolean video, deviceLogs, local, networkLogs;

  private String testSuite;

  @Input
  private String[] devices;

  @Optional
  @Input
  private String callbackURL, localIdentifier, networkProfile;

  public String getCallbackURL() {
    return callbackURL;
  }

  public void setCallbackURL(String callbackURL) {
    this.callbackURL = callbackURL;
  }

  public String getLocalIdentifier() {
    return localIdentifier;
  }

  public void setLocalIdentifier(String localIdentifier) {
    this.localIdentifier = localIdentifier;
  }

  public boolean getLocal() {
    return local;
  }

  public void setLocal(boolean local) {
    this.local = local;
  }

  public String getNetworkProfile() {
    return networkProfile;
  }

  public void setNetworkProfile(String profile) {
    this.networkProfile = profile;
  }

  public String[] getClasses() {
    return classes;
  }

  public void setClasses(String[] classes) {
    this.classes = classes;
  }

  public String[] getAnnotations() {
    return annotations;
  }

  public void setAnnotations(String[] annotations) {
    this.annotations = annotations;
  }

  public String[] getPackages() {
    return packages;
  }

  public void setPackages(String[] packages) {
    this.packages = packages;
  }

  public String[] getSizes() {
    return sizes;
  }

  public void setSizes(String[] sizes) {
    this.sizes = sizes;
  }

  public String[] getOtherApps() {
    return otherApps;
  }

  public void setOtherApps(String[] otherApps) {
    this.otherApps = otherApps;
  }

  public boolean getVideo() {
    return video;
  }

  public void setVideo(boolean video) {
    this.video = video;
  }

  public boolean getDeviceLogs() {
    return deviceLogs;
  }

  public void setDeviceLogs(boolean deviceLogs) {
    this.deviceLogs = deviceLogs;
  }

  public boolean getNetworkLogs() {
    return networkLogs;
  }

  public void setNetworkLogs(boolean networkLogs) {
    this.networkLogs = networkLogs;
  }

  public String[] getDevices() {
    return devices;
  }

  public void setDevices(String[] devices) {
    this.devices = devices;
  }

  private String constructBuildParams() {
    JSONObject params = constructDefaultBuildParams();
    params.put("testSuite", testSuite);
    params.put("devices", devices);
    params.put("class", classes);
    params.put("package", packages);
    params.put("size", sizes);
    params.put("annotation", annotations);
    params.put("otherApps", otherApps);
    params.put("video", video);
    params.put("deviceLogs", deviceLogs);
    params.put("networkLogs", networkLogs);
    params.put("local", local);
    params.put("localIdentifier", localIdentifier);
    params.put("networkProfile", networkProfile);
    params.put("callbackURL", callbackURL);

    return params.toString();
  }

  private void uploadTestSuite(Path testApkPath) throws Exception {
    try {
      HttpURLConnection con = HttpUtils
          .sendPost(getHost() + Constants.TEST_SUITE_UPLOAD_PATH, basicAuth(), null, testApkPath.toString());
      int responseCode = con.getResponseCode();
      System.out.println("TestSuite upload Response Code : " + responseCode);

      JSONObject response = new JSONObject(HttpUtils.getResponse(con, responseCode));

      if (responseCode == 200) {
        testSuite = (String) response.get("test_url");
      } else {
        throw new Exception("TestSuite upload failed");
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private String executeTest() throws Exception {
    try {
      HttpURLConnection con = HttpUtils
          .sendPost(getHost() + Constants.BUILD_PATH, basicAuth(), constructBuildParams(), null);
      int responseCode = con.getResponseCode();
      System.out.println("Response Code : " + responseCode);

      JSONObject response = new JSONObject(HttpUtils.getResponse(con, responseCode));
      return response.getString("build_id");
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  public void verifyParams() throws Exception {
    super.verifyParams();
    if (devices == null) {
      throw new Exception("`devices` is mandatory");
    }
  }

  private void displayDashboardURL(String build_id) {
    System.out.println("View build status at " + Constants.APP_AUTOMATE_HOST + "/builds/" + build_id);
  }

  @TaskAction
  void uploadAndExecuteTest() throws Exception {
    verifyParams();
    Map<String, Path> apkFiles = locateApks();
    uploadApp(Constants.APP_AUTOMATE_UPLOAD_PATH, apkFiles.get("debugApkPath"));
    uploadTestSuite(apkFiles.get("testApkPath"));
    String build_id = executeTest();
    displayDashboardURL(build_id);
  }
}
