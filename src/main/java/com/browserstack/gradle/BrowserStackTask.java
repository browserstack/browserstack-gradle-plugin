package com.browserstack.gradle;

import com.android.annotations.NonNull;
import com.browserstack.httputils.HttpUtils;
import com.browserstack.json.JSONObject;
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
import org.jetbrains.annotations.Nullable;

public class BrowserStackTask extends DefaultTask {

  public static final String KEY_FILE_DEBUG = "debugApkPath";
  public static final String KEY_FILE_TEST = "testApkPath";

  @Input
  private String username, accessKey;

  @Input
  private String app, host;

  private String appVariantBaseName = "debug";

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

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  protected JSONObject constructDefaultBuildParams() {
    JSONObject params = new JSONObject();

    params.put("app", app);
    // for monitoring, not for external use
    params.put("browserstack.source", "gradlePlugin");

    return params;
  }

  public String uploadApp(
          @NotNull String appUploadURLPath,
          @NotNull Path debugApkPath
  ) throws Exception {
    try {
      HttpURLConnection con = HttpUtils.sendPost(host + appUploadURLPath, basicAuth(), null, debugApkPath.toString());
      int responseCode = con.getResponseCode();
      System.out.println("App upload Response Code : " + responseCode);

      JSONObject response = new JSONObject(HttpUtils.getResponse(con, responseCode));

      if (responseCode == 200) {
        app = (String) response.get("app_url");
        return app;
      } else {
        throw new Exception("App upload failed");
      }
    } catch (Exception e) {
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

  public Map<String, Path> locateApks(boolean ignoreTestPath) throws Exception {
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
      throw new Exception("unable to find DebugApp apk");
    }

    //Dont raise error for testApkPath if AppLive task
    if (!ignoreTestPath && testApkPath == null) {
      throw new Exception("unable to find TestApp apk");
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
