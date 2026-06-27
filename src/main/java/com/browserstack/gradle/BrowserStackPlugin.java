package com.browserstack.gradle;

import com.android.build.api.variant.ApplicationAndroidComponentsExtension;
import com.android.build.api.variant.ApplicationVariant;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Gradle plugin that registers BrowserStack tasks for each Android application variant.
 * Creates execute*TestsOnBrowserstack, upload*ToBrowserstackAppLive, upload*ToBrowserstackAppAutomate,
 * and browserstackCLIWrapper tasks. Requires the Android Application plugin to be applied first.
 */
public class BrowserStackPlugin implements Plugin<Project> {

    private static final String DEFAULT_GROUP = "BrowserStack";

    /**
     * Applies the plugin: creates browserStackConfig extension and per-variant BrowserStack tasks.
     *
     * @param project the Gradle project
     */
    public void apply(Project project) {

        BrowserStackConfigExtension browserStackConfigExtension = project.getExtensions()
                .create("browserStackConfig", BrowserStackConfigExtension.class);

        ApplicationAndroidComponentsExtension androidComponents = project.getExtensions()
                .getByType(ApplicationAndroidComponentsExtension.class);

        final Boolean[] isCLITaskCreated = new Boolean[1];
        isCLITaskCreated[0] = false;
        androidComponents.onVariants(androidComponents.selector(), new Action<ApplicationVariant>() {
            @Override
            public void execute(ApplicationVariant applicationVariant) {
                String applicationVariantName = null;
                try {
                    applicationVariantName = Tools.capitalize(applicationVariant.getName());
                } catch (Exception e) {
                    return;
                }

                final String appVariantName = applicationVariantName;
                project.getTasks().create("execute" + appVariantName + "TestsOnBrowserstack", EspressoTask.class,
                        (task) -> {
                            task.setGroup(DEFAULT_GROUP);
                            task.setDescription("Uploads app / tests to AppAutomate and executes them");
                            // Unless skipBuildingApks, depend on assemble tasks for app and test APKs.
                            if (!project.hasProperty("skipBuildingApks")
                                    || Boolean.parseBoolean(project.property("skipBuildingApks").toString()) == false) {
                                task.dependsOn("assemble" + appVariantName,
                                        "assemble" + appVariantName + "AndroidTest");
                            }
                            task.setAppVariantBaseName(applicationVariant.getName());
                            task.setUsername(browserStackConfigExtension.getUsername());
                            task.setAccessKey(browserStackConfigExtension.getAccessKey());
                            task.setCustomId(browserStackConfigExtension.getCustomId());
                            task.setConfigFilePath(browserStackConfigExtension.getConfigFilePath());
                            task.setHost(Constants.BROWSERSTACK_API_HOST);
                            task.setDebug(browserStackConfigExtension.isDebug());
                            if (project.hasProperty("mainAPKPath")) {
                                task.setMainAPKPath(project.property("mainAPKPath").toString());
                            }
                            if (project.hasProperty("testAPKPath")) {
                                task.setTestAPKPath(project.property("testAPKPath").toString());
                            }
                        });

                project.getTasks().create("upload" + appVariantName + "ToBrowserstackAppLive", AppLiveUploadTask.class,
                        (task) -> {
                            task.setGroup(DEFAULT_GROUP);
                            task.setDescription("Uploads app to AppLive");
                            task.dependsOn("assemble" + appVariantName);
                            task.setAppVariantBaseName(applicationVariant.getName());
                            task.setHost(Constants.BROWSERSTACK_API_HOST);
                            task.setUsername(browserStackConfigExtension.getUsername());
                            task.setAccessKey(browserStackConfigExtension.getAccessKey());
                            task.setCustomId(browserStackConfigExtension.getCustomId());
                            task.setDebug(browserStackConfigExtension.isDebug());
                        });

                project.getTasks().create("upload" + appVariantName + "ToBrowserstackAppAutomate",
                        AppAutomateUploadTask.class, (task) -> {
                            task.setGroup(DEFAULT_GROUP);
                            task.setDescription("Uploads app to AppAutomate");
                            task.dependsOn("assemble" + appVariantName);
                            task.setAppVariantBaseName(applicationVariant.getName());
                            task.setHost(Constants.BROWSERSTACK_API_HOST);
                            task.setUsername(browserStackConfigExtension.getUsername());
                            task.setAccessKey(browserStackConfigExtension.getAccessKey());
                            task.setCustomId(browserStackConfigExtension.getCustomId());
                            task.setDebug(browserStackConfigExtension.isDebug());
                        });
                if (!isCLITaskCreated[0]) {
                    project.getTasks().create("browserstackCLIWrapper", CLI.class, (task) -> {
                        task.setGroup(DEFAULT_GROUP);
                        task.setDescription(
                                "Just a wrapper on the Browserstack CLI. A way to run any Browserstack CLI command directly from gradle. \n"
                                        +
                                        "\n" +
                                        "\n" +
                                        "For reference on Browserstack CLI please visit https://www.browserstack.com/app-automate/browserstack-cli\n"
                                        +
                                        "\n" +
                                        "\n" +
                                        "Any CLI command passed in the custom option  -Pcommand will be executed and the results will be displayed on the terminal.\n"
                                        +
                                        "\n" +
                                        "\n" +
                                        "For example:\n" +
                                        "\n" +
                                        "gradle browserstackCLIWrapper -Pcommand=”app-automate apps”\n" +
                                        "\n" +
                                        "The browserstack CLI command  app-automate apps  would run and the result will be displayed on the terminal.  ");

                        task.dependsOn("assemble" + appVariantName);
                        task.setAppVariantBaseName(applicationVariant.getName());
                        task.setHost(Constants.BROWSERSTACK_API_HOST);
                        task.setUsername(browserStackConfigExtension.getUsername());
                        task.setAccessKey(browserStackConfigExtension.getAccessKey());
                        task.setCustomId(browserStackConfigExtension.getCustomId());
                        task.setDebug(browserStackConfigExtension.isDebug());
                        if (project.hasProperty("command")) {
                            System.out.println("Command found: " + project.findProperty("command").toString());
                            task.setCommand(project.property("command").toString());
                        }
                    });
                    isCLITaskCreated[0] = true;
                }
            }
        });
    }
}
