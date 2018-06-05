package com.browserstack.gradle.espresso;

import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.Input;
import java.net.HttpURLConnection;

import com.browserstack.json.JSONObject;
import com.browserstack.httputils.HttpUtils;
import com.browserstack.gradle.Constants;

public class EspressoTask extends BrowserStackTask {

    @Input
    private String testSuite;

    @Input
    private String[] devices;

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
      return params.toString();
    }

    private void uploadTestSuite() throws Exception {
        try {
            HttpURLConnection con = HttpUtils.sendPost(getHost() + Constants.TEST_SUITE_UPLOAD_PATH, basicAuth(), null, getTestApkPath().toString());
            int responseCode = con.getResponseCode();
            System.out.println("TestSuite upload Response Code : " + responseCode);

            JSONObject response = new JSONObject(HttpUtils.getResponse(con, responseCode));

            if(responseCode == 200){
                testSuite = (String) response.get("test_url");
            } else {
                throw new Exception("TestSuite upload failed");
            }

        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private String executeTest() throws Exception {
        try {
            HttpURLConnection con = HttpUtils.sendPost(getHost() + Constants.BUILD_PATH, basicAuth(), constructBuildParams(), null);
            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            JSONObject response = new JSONObject(HttpUtils.getResponse(con, responseCode));
            return response.getString("build_id");
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }
    
    public void verifyParams() throws Exception {
      super.verifyParams();  
      if(devices == null) {
            throw new Exception("`devices` is compulsory");
        }
    }

    private void displayDashboardURL(String build_id){
      System.out.println("View build status at " + Constants.APP_AUTOMATE_HOST + "/builds/" + build_id);
    }

    @TaskAction
    void uploadAndExecuteTest() throws Exception {
        verifyParams();
        locateApks();
        uploadApp(Constants.APP_AUTOMATE_UPLOAD_PATH);
        uploadTestSuite();
        String build_id = executeTest();
        displayDashboardURL(build_id);
    }
}
