package com.browserstack.gradle;

import com.browserstack.httputils.HttpUtils;
import com.browserstack.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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

/**
 * Base task for BrowserStack operations. Handles credentials, app upload, and APK path resolution.
 * Subclasses implement specific actions (Espresso run, App Live upload, App Automate upload, CLI).
 */
public class BrowserStackTask extends DefaultTask {

  /** Extra property key for custom_id when uploading apps. */
  public static final String KEY_EXTRA_CUSTOM_ID = "custom_id";
  /** Map key for the main/debug APK path in locateApks result. */
  public static final String KEY_FILE_DEBUG = "debugApkPath";
  /** Map key for the test APK path in locateApks result. */
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

  @Input
  @Optional
  public String mainAPKPath;

  @Input
  @Optional
  public String testAPKPath;

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

  public String getCustomId() {
    return customId;
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

  public String getApp() {
    return app;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getCommand() { return command; }

  public void setCommand(String command) { this.command = command; }

  public String getMainAPKPath() { return mainAPKPath; }

  public void setMainAPKPath(String mainAPKPath) { this.mainAPKPath = mainAPKPath; }

  public String getTestAPKPath() {return testAPKPath; }

  public void setTestAPKPath(String testAPKPath) { this.testAPKPath = testAPKPath; }

  /**
   * Builds the default JSON params for BrowserStack API (app URL, source tag).
   *
   * @return params object for the build request
   */
  protected JSONObject constructDefaultBuildParams() { JSONObject params = new JSONObject();

    params.put("app", app);
    // For monitoring; not for external use.
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

  /**
   * Returns the path with the latest modification time from the list.
   *
   * @param paths list of file paths
   * @return the most recently modified path, or null if list is empty
   */
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

  private boolean isPathRelative(String apkPath){
    if(apkPath.startsWith("./")){
      return true;
    }
    return false;
  }
  private  String getAbsolutePath(String apkPath, String currentWorkingDirectory){
    if(isPathRelative(apkPath)){
      return currentWorkingDirectory + apkPath.substring(1);
    }
    return apkPath;
  }
  /**
   * Resolves main and test APK paths from mainAPKPath/testAPKPath or by scanning the project.
   *
   * @param ignoreTestPath if true, test APK may be null (e.g. for App Live upload)
   * @return map with KEY_FILE_DEBUG and KEY_FILE_TEST paths
   * @throws IOException if required APKs cannot be found
   */
  public Map<String, Path> locateApks(boolean ignoreTestPath) throws IOException {
    Path debugApkPath;
    Path testApkPath;
    String dir = System.getProperty("user.dir");
    List<Path> appApkFiles = new ArrayList<>();
    List<Path> testApkFiles = new ArrayList<>();
    // First element: true if main APK was from client path. Second: true if test APK was from client path.
    final Boolean[] isAPKFileCreated = {false, false};
    if(mainAPKPath != null){
      isAPKFileCreated[0] = true;
      try {
        Files.find(Paths.get(getAbsolutePath(mainAPKPath, dir)), 1, (filePath, fileAttr) -> isValidAPKFile(filePath, fileAttr))
                .forEach(f -> {
                  appApkFiles.add(f);
                });
      }catch (NoSuchFileException e ){
        throw new IOException("Invalid File Path: Please provide a valid main APK path");
      }
    }
    if(testAPKPath != null){
      isAPKFileCreated[1] = true;
      try {
        Files.find(Paths.get(getAbsolutePath(testAPKPath, dir)), 1, (filePath, fileAttr) -> isValidAPKFile(filePath, fileAttr))
                .forEach(f -> {
                  testApkFiles.add(f);
                });
      }catch(NoSuchFileException e ){
        throw new IOException("Invalid File Path: Please provide a valid test APK path");
      }
    }

    if(!isAPKFileCreated[0] || !isAPKFileCreated[1]) {
      Files.find(Paths.get(dir), Constants.APP_SEARCH_MAX_DEPTH, (filePath, fileAttr) -> isValidFile(filePath, fileAttr))
              .forEach(f -> {
                if (f.toString().endsWith("-androidTest.apk")) {
                  if(!isAPKFileCreated[1]) {
                    testApkFiles.add(f);
                  }
                } else if (!isAPKFileCreated[0]) {
                  appApkFiles.add(f);
                }
              });
    }
    debugApkPath = findMostRecentPath(appApkFiles);
    testApkPath = findMostRecentPath(testApkFiles);

    System.out.println("Most recent DebugApp apk: " + debugApkPath);
    System.out.println("Most recent TestApp apk: " + testApkPath);

    if (debugApkPath == null) {
      throw new IOException("unable to find DebugApp apk");
    }

    // Don't raise error for testApkPath if App Live task (ignoreTestPath true).
    if (!ignoreTestPath && testApkPath == null) {
      throw new IOException("unable to find TestApp apk");
    }
    Map<String, Path> apkFiles = new HashMap<>();
    apkFiles.put(KEY_FILE_DEBUG, debugApkPath);
    apkFiles.put(KEY_FILE_TEST, testApkPath);
    return apkFiles;
  }

  private boolean isValidFile(Path filePath, BasicFileAttributes fileAttr) {
    return isValidAPKFile(filePath, fileAttr) && filePath.getFileName().toString()
        .contains(appVariantBaseName);
  }
  private boolean isValidAPKFile(Path filePath, BasicFileAttributes fileAttr) {
    return fileAttr.isRegularFile() && filePath.toString().endsWith(".apk") ;
  }
}
