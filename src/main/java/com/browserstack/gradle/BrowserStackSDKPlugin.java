package com.browserstack.gradle;

import com.browserstack.json.JSONObject;

import java.io.*;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.Action;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.testing.Test;

import org.gradle.api.execution.TaskExecutionGraph;

/**
 * Gradle plugin for BrowserStack SDK integration. Writes gradle-m-config.json with project/task
 * context for the SDK and disables JUnit XML/HTML test reports when running under platformIndex.
 */
public class BrowserStackSDKPlugin implements Plugin<Project> {
    String fileSeparator = System.getProperty("file.separator");

    /**
     * Applies the plugin: adds doFirst to all tasks to write config, and whenReady disables test reports if needed.
     *
     * @param project the Gradle project
     */
    public void apply(Project project) {
        project.getTasks().withType(Task.class, task -> {
            task.doFirst(new Action<Task>() {
                @Override
                public void execute(Task t) {
                    if(System.getenv("platformIndex") != null) return;

                    org.gradle.StartParameter startParameter = project.getRootProject().getGradle().getStartParameter();

                    String fileSeparator = System.getProperty("file.separator");
                    String workingDirectoryPath = startParameter.getCurrentDir().toString();
                    String gradleConfigFile = "gradle-m-config.json";
                    String fullConfigFilePath = workingDirectoryPath + fileSeparator + gradleConfigFile;

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("projectDir", startParameter.getCurrentDir().toString());

                    org.gradle.TaskExecutionRequest taskExecutionRequest = startParameter.getTaskRequests().get(0);
                    jsonObject.put("taskArgs", taskExecutionRequest.getArgs());

                    jsonObject.put("gradleHome", startParameter.DEFAULT_GRADLE_USER_HOME.toString());
                    jsonObject.put("logLevel", startParameter.getLogLevel().toString());
                    jsonObject.put("systemPropertiesArgs", startParameter.getSystemPropertiesArgs());

                    try (FileWriter newFile = new FileWriter(fullConfigFilePath)) {
                        newFile.write(jsonObject.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        project.getGradle().getTaskGraph().whenReady(new Action<TaskExecutionGraph>() {
          @Override
          public void execute(TaskExecutionGraph taskGraph) {
            String customXmlResultsDir = project.getBuildDir().toString() + fileSeparator + "test-results-" + System.getenv("platformIndex");
            if (customXmlResultsDir != null && !customXmlResultsDir.isEmpty()) {
              TaskContainer tasks = project.getTasks();
              tasks.withType(Test.class, new Action<Test>() {
                @Override
                public void execute(Test test) {
                  try {
                    test.getReports().getJunitXml().getRequired().set(false);
                    test.getReports().getHtml().getRequired().set(false);
                  } catch (Throwable e) {}
                }
              });
            }
          }
        });
    }
}
