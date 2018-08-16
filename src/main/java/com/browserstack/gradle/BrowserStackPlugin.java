package com.browserstack.gradle;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.ApplicationVariant;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BrowserStackPlugin implements Plugin<Project> {


  public void apply(Project project) {

    BrowserStackConfigExtension browserStackConfigExtension = project.getExtensions()
        .create("browserStackConfig", BrowserStackConfigExtension.class);

    // This will be deprecated
    project.getTasks().create("runDebugBuildOnBrowserstack", EspressoTask.class, (task) -> {
      System.out.println("WARNING: Task 'runDebugBuildOnBrowserstack' is deprecated now. Please use new task "
          + "'executeDebugTestsOnBrowserStack' instead of this. Please refer "
          + "https://github.com/browserstack/browserstack-gradle-plugin/blob/master/README.md for more details.");
      task.dependsOn("assembleDebug", "assembleDebugAndroidTest");

      task.setHost(Constants.BROWSERSTACK_API_HOST);

      task.setLocal(Constants.DEFAULT_LOCAL);
      task.setVideo(Constants.DEFAULT_VIDEO);
      task.setDeviceLogs(Constants.DEFAULT_DEVICE_LOGS);
      task.setNetworkLogs(Constants.DEFAULT_NETWORK_LOGS);
      task.setNetworkProfile(Constants.DEFAULT_NETWORK_PROFILE);

      task.setClasses(new String[0]);
      task.setAnnotations(new String[0]);
      task.setPackages(new String[0]);
      task.setSizes(new String[0]);

      task.setOtherApps(new String[0]);

      task.setCallbackURL(null);
      task.setLocalIdentifier(null);
    });

    // This will be deprecated
    project.getTasks().create("uploadBuildToBrowserstackAppLive", AppUploadTask.class, (task) -> {
      System.out.println("WARNING: Task 'uploadBuildToBrowserstackAppLive' is deprecated now. Please use new task "
          + "'uploadDebugToBrowserstackAppLive' instead of this. Please refer "
          + "https://github.com/browserstack/browserstack-gradle-plugin/blob/master/README.md for more details.");
      task.dependsOn("assembleDebug");
      task.setHost(Constants.BROWSERSTACK_API_HOST);
    });

    // Get android appExtension
    AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");

    // Get all application variants or flavour combinations.
    DomainObjectSet<ApplicationVariant> appVariants = appExtension.getApplicationVariants();

    // Create tasks for each variant
    appVariants.all(applicationVariant -> {
      String applicationVariantName = null;
      try {
        applicationVariantName = Tools.capitalize(applicationVariant.getName());
      } catch (Exception e) {
        return;
      }

      // Since we can't use an outer variable in lambda expression which is not final.
      final String appVariantName = applicationVariantName;

      project.getTasks().create("execute" + appVariantName + "TestsOnBrowserstack", EspressoTask.class, (task) -> {
        task.dependsOn("assemble" + appVariantName, "assemble" + appVariantName + "AndroidTest");

        task.setAppVariantBaseName(applicationVariant.getBaseName());

        task.setUsername(browserStackConfigExtension.getUsername());
        task.setAccessKey(browserStackConfigExtension.getAccessKey());
        task.setDevices(browserStackConfigExtension.getDevices());

        task.setHost(Constants.BROWSERSTACK_API_HOST);

        task.setLocal(browserStackConfigExtension.isLocal());
        task.setVideo(browserStackConfigExtension.isVideo());
        task.setDeviceLogs(browserStackConfigExtension.isDeviceLogs());
        task.setNetworkLogs(browserStackConfigExtension.isNetworkLogs());
        task.setNetworkProfile(browserStackConfigExtension.getNetworkProfile());

        task.setClasses(browserStackConfigExtension.getClasses());
        task.setAnnotations(browserStackConfigExtension.getAnnotations());
        task.setPackages(browserStackConfigExtension.getPackages());
        task.setSizes(browserStackConfigExtension.getSizes());

        task.setOtherApps(browserStackConfigExtension.getOtherApps());

        task.setCallbackURL(browserStackConfigExtension.getCallbackURL());
        task.setLocalIdentifier(browserStackConfigExtension.getLocalIdentifier());
      });

      project.getTasks().create("upload" + appVariantName + "ToBrowserstackAppLive", AppUploadTask.class, (task) -> {
        task.dependsOn("assemble" + appVariantName);

        task.setAppVariantBaseName(applicationVariant.getBaseName());

        task.setHost(Constants.BROWSERSTACK_API_HOST);

        task.setUsername(browserStackConfigExtension.getUsername());
        task.setAccessKey(browserStackConfigExtension.getAccessKey());

      });
    });
  }
}
