package com.browserstack.espresso;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class EspressoPlugin implements Plugin<Project> {
  public void apply(Project project) {
    project.getTasks().create("runOnBrowserstack", EspressoRun.class, (task) -> {
      task.dependsOn("assembleDebug", "assembleDebugAndroidTest");

      task.setHost("https://api.browserstack.com");

      task.setLocal(false);
      task.setVideo(true);
      task.setDeviceLogs(true);

      task.setClasses(new String[0]);
      task.setAnnotations(new String[0]);
      task.setPackages(new String[0]);
      task.setSizes(new String[0]);
    });
  }
}
