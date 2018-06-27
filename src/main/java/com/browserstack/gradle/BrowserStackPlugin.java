package com.browserstack.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import com.browserstack.json.JSONObject;
import com.browserstack.gradle.Constants;

public class BrowserStackPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getTasks().create("runDebugBuildOnBrowserstack", EspressoTask.class, (task) -> {
            task.dependsOn("assembleDebug", "assembleDebugAndroidTest");

            task.setHost(Constants.BROWSERSTACK_API_HOST);

            task.setLocal(Constants.DEFAULT_LOCAL);
            task.setVideo(Constants.DEFAULT_VIDEO);
            task.setDeviceLogs(Constants.DEFAULT_DEVICE_LOGS);
            task.setNetworkLogs(Constants.DEFAULT_NETWORK_LOGS);

            task.setClasses(new String[0]);
            task.setAnnotations(new String[0]);
            task.setPackages(new String[0]);
            task.setSizes(new String[0]);

            task.setAppStoreConfiguration(new JSONObject())

            task.setCallbackURL(null);
            task.setLocalIdentifier(null);
        });

        project.getTasks().create("uploadBuildToBrowserstackAppLive", AppUploadTask.class, (task) -> {
            task.dependsOn("assembleDebug");
            task.setHost(Constants.BROWSERSTACK_API_HOST);
        });
    }
}
