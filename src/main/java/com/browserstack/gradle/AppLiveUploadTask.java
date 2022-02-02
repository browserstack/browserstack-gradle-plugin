package com.browserstack.gradle;

import org.gradle.api.tasks.TaskAction;
import com.browserstack.gradle.Constants;
import java.util.Map;
import java.nio.file.Path;

public class AppLiveUploadTask extends BrowserStackTask {

  private void displayTestURL(String app_url) {
    String app_hashed_id = app_url.substring(5);
    System.out.println("Start testing at " + Constants.APP_LIVE_HOST + "/#app_hashed_id=" + app_hashed_id);
  }

  public void verifyParams() throws Exception {
    String username = this.getUsername();
    String accessKey = this.getAccessKey();
    if (username == null || accessKey == null) {
      throw new Exception("`username`, `accessKey` are compulsory");
    }
  }

  @TaskAction
  void uploadAndExecuteTest() throws Exception {
    verifyParams();
    final boolean ignoreTestPath = true;
    final boolean wrapPropsAsInternal = true;
    Map<String, Path> apkFiles = locateApks(ignoreTestPath);
    String app_url = uploadApp(
            wrapPropsAsInternal,
            Constants.APP_LIVE_UPLOAD_PATH,
            apkFiles.get(BrowserStackTask.KEY_FILE_DEBUG)
    );
    displayTestURL(app_url);
  }
}
