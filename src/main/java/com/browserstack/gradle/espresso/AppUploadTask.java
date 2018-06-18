package com.browserstack.gradle.espresso;

import org.gradle.api.tasks.TaskAction;
import com.browserstack.gradle.Constants;

public class AppUploadTask extends BrowserStackTask {

  private void displayTestURL(String app_url){
    String app_hashed_id = app_url.substring(5);
    System.out.println("Start testing at " + Constants.APP_LIVE_HOST + "/#app_hashed_id=" + app_hashed_id);
  }

  @TaskAction
  void uploadAndExecuteTest() throws Exception {
    verifyParams();
    locateApks();
    String app_url = uploadApp(Constants.APP_LIVE_UPLOAD_PATH);
    displayTestURL(app_url);
  }
}
