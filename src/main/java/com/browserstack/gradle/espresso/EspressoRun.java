package com.browserstack.gradle.espresso;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.Input;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

import com.browserstack.json.JSONObject;
import com.browserstack.httputils.HttpUtils;
import com.browserstack.gradle.Constants;

public class EspressoRun extends DefaultTask {

    @Input
    private String username, accessKey, app, testSuite, callbackURL, localIdentifier, host;

    private Path debugApkPath, testApkPath;

    @Input
    private String[] devices, classes, annotations, packages, sizes;

    @Input
    private boolean video, deviceLogs, local;

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

    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }

    public String[] getDevices() {
        return devices;
    }
    public void setDevices(String[] devices) {
        this.devices = devices;
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

    public String[] getSizes() {
        return sizes;
    }
    public void setSizes(String[] sizes) {
        this.sizes = sizes;
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

    public boolean getLocal() {
        return local;
    }
    public void setLocal(boolean local) {
        this.local = local;
    }

    private String constructBuildParams() {
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

    private String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + accessKey).getBytes());
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
            HttpURLConnection con = HttpUtils.sendPost(host + Constants.APP_UPLOAD_PATH, basicAuth(), null, debugApkPath.toString());
            int responseCode = con.getResponseCode();
            System.out.println("App upload Response Code : " + responseCode);

            JSONObject response = new JSONObject(HttpUtils.getResponse(con, responseCode));

            if(responseCode == 200){
                app = (String) response.get("app_url");
            } else {
                throw new Exception("App upload failed");
            }

        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private void uploadTestSuite() throws Exception {
        try {
            HttpURLConnection con = HttpUtils.sendPost(host + Constants.TEST_SUITE_UPLOAD_PATH, basicAuth(), null, testApkPath.toString());
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

    private void executeTest() throws Exception {
        try {
            HttpURLConnection con = HttpUtils.sendPost(host + Constants.BUILD_PATH, basicAuth(), constructBuildParams(), null);
            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            JSONObject response = new JSONObject(HttpUtils.getResponse(con, responseCode));
        } catch (Exception e){
            e.printStackTrace();
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
