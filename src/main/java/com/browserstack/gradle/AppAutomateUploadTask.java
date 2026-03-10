package com.browserstack.gradle;

import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Gradle task that uploads the debug APK to BrowserStack App Automate and prints the test URL.
 */
public class AppAutomateUploadTask extends BrowserStackTask {

  private void displayTestURL(String app_url) {
    String app_hashed_id = app_url.substring(5);
    System.out.println("Start testing at " + Constants.APP_AUTOMATE_HOST + "/#app_hashed_id=" + app_hashed_id);
  }

  /** Ensures username and accessKey are set (from extension or env). */
  public void verifyParams() throws Exception {
    String username = this.getUsername();
    String accessKey = this.getAccessKey();
    if (username == null || accessKey == null) {
      throw new Exception("`username`, `accessKey` are compulsory");
    }
  }

  /** Uploads the debug APK to App Automate and prints the session URL. */
  @TaskAction
  void upload() throws Exception {
    verifyParams();
    final boolean ignoreTestPath = true;
    final boolean wrapPropsAsInternal = false;
    Map<String, Path> apkFiles = locateApks(ignoreTestPath);
    String app_url = uploadApp(
            wrapPropsAsInternal,
            Constants.APP_AUTOMATE_UPLOAD_PATH,
            apkFiles.get(BrowserStackTask.KEY_FILE_DEBUG)
    );
    displayTestURL(app_url);
  }
}
