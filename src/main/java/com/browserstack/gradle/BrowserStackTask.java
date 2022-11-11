package com.browserstack.gradle;

import com.browserstack.httputils.HttpUtils;
import com.browserstack.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.jetbrains.annotations.NotNull;
import org.gradle.api.tasks.Optional;


public class BrowserStackTask extends DefaultTask {

  public static final String KEY_EXTRA_CUSTOM_ID = "custom_id";
  public static final String KEY_FILE_DEBUG = "debugApkPath";
  public static final String KEY_FILE_TEST = "testApkPath";

  @Input
  protected String username, accessKey, customId;

  @Input
  private String app, host;

  protected boolean isDebug;

  private String appVariantBaseName = "debug";

  @Input
  @Optional
  public String command ;

  public void setAppVariantBaseName(String appVariantBaseName) {
    this.appVariantBaseName = appVariantBaseName;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public void setCustomId(String customId) {
    this.customId = customId;
  }

  public void setDebug(boolean debug) {
    isDebug = debug;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getCommand() { return command; }

  public void setCommand(String command) { this.command = command; }

  protected JSONObject constructDefaultBuildParams() { JSONObject params = new JSONObject();

    params.put("app", app);
    // for monitoring, not for external use
    params.put("browserstack.source", "gradlePlugin");

    return params;
  }

  /**
   * Uploads app and binds properties to it
   * @param wrapPropsAsInternal indicates if additional properties should be wrapped as internal data map
   * @param appUploadURLPath remote path to upload app to
   * @param debugApkPath app file path
   * @return raw request response
   * @throws IOException if uploading fails
   */
  public String uploadApp(
          boolean wrapPropsAsInternal,
          @NotNull String appUploadURLPath,
          @NotNull Path debugApkPath
  ) throws IOException {
    try {
      final Map<String, String> extraProperties = new HashMap<>();
      extraProperties.put(KEY_EXTRA_CUSTOM_ID, this.customId);
      HttpURLConnection con = HttpUtils.sendPostApp(
              isDebug,
              wrapPropsAsInternal,
              host + appUploadURLPath,
              basicAuth(),
              debugApkPath.toString(),
              extraProperties
      );
      int responseCode = con.getResponseCode();
      System.out.println("App upload Response Code : " + responseCode);

      JSONObject response = new JSONObject(HttpUtils.getResponse(con, responseCode));

      if (responseCode == 200) {
        app = (String) response.get("app_url");
        return app;
      } else {
        throw new IOException(
                String.format(
                        "App upload failed (%d): %s",
                        responseCode,
                        con.getResponseMessage()
                )
        );
      }
    } catch (IOException e) {
//      e.printStackTrace();
      throw e;
    }
  }

  public String basicAuth() {
    return "Basic " + Base64.getEncoder().encodeToString((username + ":" + accessKey).getBytes());
  }

  public static Path findMostRecentPath(List<Path> paths) {
    long mostRecentTime = 0L;
    Path mostRecentPath = null;
    for (Path p : paths) {
      if (p.toFile().lastModified() > mostRecentTime) {
        mostRecentTime = p.toFile().lastModified();
        mostRecentPath = p;
      }
    }
    return mostRecentPath;
  }

  public Map<String, Path> locateApks(boolean ignoreTestPath) throws IOException {
    Path debugApkPath;
    Path testApkPath;
    String dir = System.getProperty("user.dir");
    List<Path> appApkFiles = new ArrayList<>();
    List<Path> testApkFiles = new ArrayList<>();
    Files.find(Paths.get(dir), Constants.APP_SEARCH_MAX_DEPTH, (filePath, fileAttr) -> isValidFile(filePath, fileAttr))
        .forEach(f -> {

          if (f.toString().endsWith("-androidTest.apk")) {
            testApkFiles.add(f);
          } else {
            appApkFiles.add(f);
          }
        });
    debugApkPath = findMostRecentPath(appApkFiles);
    testApkPath = findMostRecentPath(testApkFiles);

    System.out.println("Most recent DebugApp apk: " + debugApkPath);
    System.out.println("Most recent TestApp apk: " + testApkPath);

    if (debugApkPath == null) {
      throw new IOException("unable to find DebugApp apk");
    }

    //Dont raise error for testApkPath if AppLive task
    if (!ignoreTestPath && testApkPath == null) {
      throw new IOException("unable to find TestApp apk");
    }
    Map<String, Path> apkFiles = new HashMap<>();
    apkFiles.put(KEY_FILE_DEBUG, debugApkPath);
    apkFiles.put(KEY_FILE_TEST, testApkPath);
    return apkFiles;
  }

  private boolean isValidFile(Path filePath, BasicFileAttributes fileAttr) {
    return fileAttr.isRegularFile() && filePath.toString().endsWith(".apk") && filePath.getFileName().toString()
        .contains(appVariantBaseName);
  }
}
