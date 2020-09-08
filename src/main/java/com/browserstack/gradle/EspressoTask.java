 package com.browserstack.gradle;

import org.gradle.api.tasks.TaskAction;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.util.Map;
import java.nio.file.Path;
import com.browserstack.json.JSONObject;
import com.browserstack.httputils.HttpUtils;
import org.json.simple.parser.JSONParser;
import org.gradle.api.tasks.Input;

public class EspressoTask extends BrowserStackTask {

  @Input
  private String configFilePath;

  private String testSuite;

  public void setConfigFilePath(String filePath) {
    this.configFilePath = filePath;
  }

  public String getConfigFilePath() {
    return configFilePath;
  }


  private String constructBuildParams() {
    JSONObject params = constructDefaultBuildParams();

    JSONParser jsonParser = new JSONParser();
    org.json.simple.JSONObject caps;

    try {
      Object obj = jsonParser.parse(new FileReader(getConfigFilePath()));
      org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) obj;

      params.put("testSuite", testSuite);

      for (Object o : jsonObject.keySet()) {
        String key = (String) o;
        params.put(key, jsonObject.get(key));
      }
    } catch (Exception e) {
        System.out.println("Config file parsing failed with below error: ");
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
        testSuite = (String) response.get("test_suite_url");
      } else {
        throw new Exception("TestSuite upload failed");
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private void executeTest() throws Exception {
    try {
      HttpURLConnection con = HttpUtils
          .sendPost(getHost() + Constants.BUILD_PATH, basicAuth(), constructBuildParams(), null);
      int responseCode = con.getResponseCode();
      System.out.println("Response Code : " + responseCode);

      JSONObject response = new JSONObject(HttpUtils.getResponse(con, responseCode));

      if (responseCode == 200) {
          String build_id = response.getString("build_id");
          displayDashboardURL(build_id);
          return;
      }

      return;

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private void displayDashboardURL(String build_id) {
    System.out.println("View build status at " + Constants.APP_AUTOMATE_HOST + "/builds/" + build_id);
  }

  public void verifyParams() throws Exception {
    String username = this.getUsername();
    String accessKey = this.getAccessKey();
    if (username == null || accessKey == null || configFilePath == null) {
      throw new Exception("`username`, `accessKey` and `configFilePath` are compulsory");
    }
  }

  @TaskAction
  void uploadAndExecuteTest() throws Exception {
    verifyParams();
    Map<String, Path> apkFiles = locateApks();
    uploadApp(Constants.APP_AUTOMATE_UPLOAD_PATH, apkFiles.get("debugApkPath"));
    uploadTestSuite(apkFiles.get("testApkPath"));
    executeTest();
  }
}
