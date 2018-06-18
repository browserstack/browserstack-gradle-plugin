package com.browserstack.gradle.espresso;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.net.HttpURLConnection;

import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

import com.browserstack.json.JSONObject;
import com.browserstack.httputils.HttpUtils;
import com.browserstack.gradle.Constants;


public class BrowserStackTask extends DefaultTask{

  private Path debugApkPath, testApkPath;
  
  @Input
  private String[] classes, annotations, packages, sizes;

  @Optional
  @Input
  private String callbackURL, localIdentifier;

  @Input
  private boolean video, deviceLogs, local, networkLogs;

  @Input
  private String username, accessKey, app, host;

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

    public String[] getClasses() {
        return classes;
    }
    public void setClasses(String[] classes) {
        this.classes = classes;
    }

    public String[] getAnnotations() {
        return annotations;
    }
    public void setAnnotations(String[] annotations) {
        this.annotations = annotations;
    }

    public String[] getPackages() {
        return packages;
    }
    public void setPackages(String[] packages) {
        this.packages = packages;
    }

    public Path getTestApkPath() {
        return testApkPath;
    }

    public String[] getSizes() {
        return sizes;
    }
    public void setSizes(String[] sizes) {
        this.sizes = sizes;
    }

    public String getCallbackURL() {
        return callbackURL;
    }
    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }

    public String getLocalIdentifier() {
        return localIdentifier;
    }
    public void setLocalIdentifier(String localIdentifier) {
        this.localIdentifier = localIdentifier;
    }

    public boolean getVideo() {
        return video;
    }
    public void setVideo(boolean video) {
        this.video = video;
    }

    public boolean getDeviceLogs() {
        return deviceLogs;
    }
    public void setDeviceLogs(boolean deviceLogs) {
        this.deviceLogs = deviceLogs;
    }

    public boolean getNetworkLogs() {
        return networkLogs;
    }
    public void setNetworkLogs(boolean networkLogs) {
        this.networkLogs = networkLogs;
    }

    public boolean getLocal() {
        return local;
    }
    public void setLocal(boolean local) {
        this.local = local;
    }


    protected JSONObject constructDefaultBuildParams() {
        JSONObject params = new JSONObject();

        params.put("app", app);
        params.put("video", video);
        params.put("deviceLogs", deviceLogs);
        params.put("networkLogs", networkLogs);

        params.put("class", classes);
        params.put("package", packages);
        params.put("size", sizes);
        params.put("annotation", annotations);

        params.put("local", local);
        params.put("localIdentifier", localIdentifier);

        params.put("callbackURL", callbackURL);
        // for monitoring, not for external use
        params.put("browserstack.source", "gradlePlugin");

        return params;
    }

    public String uploadApp(String appUploadPath) throws Exception {
        try {
            HttpURLConnection con = HttpUtils.sendPost(host + appUploadPath, basicAuth(), null, debugApkPath.toString());
            int responseCode = con.getResponseCode();
            System.out.println("App upload Response Code : " + responseCode);

            JSONObject response = new JSONObject(HttpUtils.getResponse(con, responseCode));

            if(responseCode == 200){
                app = (String) response.get("app_url");
                return app;
            } else {
                throw new Exception("App upload failed");
            }
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public  String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + accessKey).getBytes());
    }

    public  static Path findMostRecentPath(List<Path> paths) {
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

    public  void locateApks() throws Exception {
        String dir = System.getProperty("user.dir");
        List<Path> appApkFiles = new ArrayList<>();
        List<Path> testApkFiles = new ArrayList<>();

        Files.find(Paths.get(dir),
                Constants.APP_SEARCH_MAX_DEPTH,
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

        //Dont raise error for testApkPath if AppLive task
        if(testApkPath == null && !(this instanceof AppUploadTask)) {
            throw new Exception("unable to find TestApp apk");
        }
    }

    public  void verifyParams() throws Exception {
        if(username == null || accessKey == null) {
            throw new Exception("`username` and  `accessKey` are compulsory");
        }
    }

}
