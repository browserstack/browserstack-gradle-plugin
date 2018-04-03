package com.browserstack.espresso;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Base64;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import com.browserstack.json.*;

public class EspressoRun extends DefaultTask {
  private String username, accessKey, app, testSuite, callbackURL, localIdentifier, host;
  private Path debugApkPath, testApkPath;
  private String[] devices, classes, annotations, packages, sizes;
  private boolean video, deviceLogs, local;

  private static String boundary = "----------------------------------", dashdash = "--", newLine = "\r\n",
                        buildPath = "/app-automate/espresso/build", appUploadPath = "/app-automate/upload",
                        testSuiteUploadPath = "/app-automate/espresso/test-suite";

  private static int byteReadBufferSize = 8192, searchMaxDepth = 10;

  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }

  public String getAccessKey() { return accessKey; }
  public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

  public String getApp() { return app; }
  public void setApp(String app) { this.app = app; }

  public String getTestSuite() { return testSuite; }
  public void setTestSuite(String testSuite) { this.testSuite = testSuite; }

  public String getCallbackURL() { return callbackURL; }
  public void setCallbackURL(String callbackURL) { this.callbackURL = callbackURL; }

  public String getLocalIdentifier() { return localIdentifier; }
  public void setLocalIdentifier(String localIdentifier) { this.localIdentifier = localIdentifier; }

  public String getHost() { return host; }
  public void setHost(String host) { this.host = host; }

  public String[] getDevices() { return devices; }
  public void setDevices(String[] devices) { this.devices = devices; }

  public String[] getClasses() { return classes; }
  public void setClasses(String[] classes) { this.classes = classes; }

  public String[] getAnnotations() { return annotations; }
  public void setAnnotations(String[] annotations) { this.annotations = annotations; }

  public String[] getPackages() { return packages; }
  public void setPackages(String[] packages) { this.packages = packages; }

  public String[] getSizes() { return sizes; }
  public void setSizes(String[] sizes) { this.sizes = sizes; }

  public boolean getVideo() { return video; }
  public void setVideo(boolean video) { this.video = video; }

  public boolean getDeviceLogs() { return deviceLogs; }
  public void setDeviceLogs(boolean deviceLogs) { this.deviceLogs = deviceLogs; }

  public boolean getLocal() { return local; }
  public void setLocal(boolean local) { this.local = local; }

  private void writeApp(DataOutputStream wr, String appPath) throws Exception {
    File file = new File(appPath);
    FileInputStream in = new FileInputStream(appPath);

    try {

      String contentDisposition = "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"";
      String contentType = "Content-Type: application/octet-stream";

      wr.writeBytes(dashdash);
      wr.writeBytes(boundary);
      wr.writeBytes(newLine);
      wr.writeBytes(contentDisposition);
      wr.writeBytes(newLine);
      wr.writeBytes(contentType);
      wr.writeBytes(newLine);
      wr.writeBytes(newLine);

      byte[] buff = new byte[byteReadBufferSize];

      for(int bytes; (bytes = in.read(buff)) > 0; wr.write(buff)) {
        if (bytes < byteReadBufferSize) {
          buff = Arrays.copyOfRange(buff, 0, bytes);
        }
      }

      wr.writeBytes(newLine);
      wr.writeBytes(dashdash);
      wr.writeBytes(boundary);
      wr.writeBytes(dashdash);
      wr.writeBytes(newLine);
    } catch (Exception e) {

    } finally {
      in.close();
    }
  }

  private HttpURLConnection sendPost(String url, String body, JSONObject headers, String appPath) throws Exception {
    URL obj = new URL(url);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

    String encoded = Base64.getEncoder().encodeToString((username+":"+accessKey).getBytes());
    con.setRequestProperty("Authorization", "Basic "+encoded);

    con.setRequestMethod("POST");

    for (Object key : headers.keySet()) {
      String keyStr = (String)key;
      String value = (String)headers.get(keyStr);
      con.setRequestProperty(keyStr, value);
    }

    con.setDoOutput(true);
    DataOutputStream wr = new DataOutputStream(con.getOutputStream());

    if(appPath == null) {
      wr.writeBytes(body);
    } else {
      writeApp(wr, appPath);
    }

    wr.flush();
    wr.close();

    return con;
  }

  private JSONObject getResponse(HttpURLConnection con, int responseCode) throws Exception {
    BufferedReader in = new BufferedReader(new InputStreamReader(responseCode == 200 ? con.getInputStream() : con.getErrorStream()));

    String inputLine;
    StringBuffer response = new StringBuffer();

    while((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();

    System.out.println(response.toString());
    return new JSONObject(response.toString());
  }

  private String getBuildParams() {
    JSONObject params = new JSONObject();

    params.put("app", app);
    params.put("testSuite", testSuite);
    params.put("devices", devices);


    params.put("class", classes);
    params.put("package", packages);
    params.put("size", sizes);
    params.put("annotation", annotations);

    params.put("local", local);
    params.put("localIdentifier", localIdentifier);

    params.put("callbackURL", callbackURL);

    return params.toString();
  }

  private static Path findMostRecentPath(List<Path> paths) {
    long mostRecentTime = 0L;
    Path mostRecentPath = null;
    for (Path p: paths) {
      if (p.toFile().lastModified() > mostRecentTime) {
        mostRecentTime = p.toFile().lastModified();
        mostRecentPath = p;
      }
    }
    return mostRecentPath;
  }

  private void locateApks() throws Exception {
    String dir = System.getProperty("user.dir");
    List<Path> appApkFiles = new ArrayList<>();
    List<Path> testApkFiles = new ArrayList<>();

    Files.find(Paths.get(dir),
        searchMaxDepth,
        (filePath, fileAttr) -> fileAttr.isRegularFile() && filePath.toString().endsWith(".apk"))
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

    if(debugApkPath == null) {
      throw new Exception("unable to find DebugApp apk");
    }

    if(testApkPath == null) {
      throw new Exception("unable to find TestApp apk");
    }
  }

  private void verifyParams() throws Exception {
    if(username == null || accessKey == null || devices == null) {
      throw new Exception("`username`, `accessKey` and `devices` are compulsory");
    }
  }

  private void uploadApp() throws Exception {
    try {
      JSONObject headers = new JSONObject();
      headers.put("content-type", "multipart/form-data; boundary=" + boundary);

      HttpURLConnection con = sendPost(host + appUploadPath, null, headers, debugApkPath.toString());
      int responseCode = con.getResponseCode();
      System.out.println("App upload Response Code : " + responseCode);

      JSONObject response = getResponse(con, responseCode);

      if(responseCode == 200){
        app = (String) response.get("app_url");
      } else {
        throw new Exception("App upload failed");
      }

    } catch (Exception e){
      System.out.println(e.toString());
      throw e;
    }
  }

  private void uploadTestSuite() throws Exception {
    try {
      JSONObject headers = new JSONObject();
      headers.put("Content-Type", "MultiPart/Form-Data; boundary=" + boundary);


      HttpURLConnection con = sendPost(host + testSuiteUploadPath, null, headers, testApkPath.toString());
      int responseCode = con.getResponseCode();
      System.out.println("TestSuite upload Response Code : " + responseCode);

      JSONObject response = getResponse(con, responseCode);

      if(responseCode == 200){
        testSuite = (String) response.get("test_url");
      } else {
        throw new Exception("TestSuite upload failed");
      }

    } catch (Exception e){
      System.out.println(e.toString());
      throw e;
    }
  }

  private void executeTest() throws Exception {
    try {
      JSONObject headers = new JSONObject();
      headers.put("Content-Type", "application/json");

      HttpURLConnection con = sendPost(host + buildPath, getBuildParams(), headers, null);
      int responseCode = con.getResponseCode();
      System.out.println("Response Code : " + responseCode);

      JSONObject response = getResponse(con, responseCode);
    } catch (Exception e){
      System.out.println(e.toString());
      throw e;
    }
  }

  @TaskAction
  void uploadAndExecuteTest() throws Exception {
    verifyParams();
    locateApks();
    uploadApp();
    uploadTestSuite();
    executeTest();
  }
}
