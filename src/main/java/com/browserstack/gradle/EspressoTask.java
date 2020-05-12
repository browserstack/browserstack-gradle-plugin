 package com.browserstack.gradle;

import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.Input;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.util.Map;
import java.nio.file.Path;
import com.browserstack.json.JSONObject;
import com.browserstack.httputils.HttpUtils;
import org.json.simple.parser.JSONParser;

 public class EspressoTask extends BrowserStackTask {
  @Input
  private String[] devices;

  private String testSuite;

  public String[] getDevices() {
    return devices;
  }

  public void setDevices(String[] devices) {
    this.devices = devices;
  }

  private String constructBuildParams() {
    JSONObject params = constructDefaultBuildParams();

    JSONParser jsonParser = new JSONParser();
    org.json.simple.JSONObject caps;

    try {
      Object obj = jsonParser.parse(new FileReader(getConfigFilePath()));

      params.put("testSuite", testSuite);
      params.put("devices", devices);

      org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) obj;
      caps = (org.json.simple.JSONObject) jsonObject.get("caps");

      for (Object o : caps.keySet()) {
        String key = (String) o;
        params.put(key, caps.get(key));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

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
