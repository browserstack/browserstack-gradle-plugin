package com.browserstack.gradle;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.ApplicationVariant;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;

public class BrowserStackPlugin implements Plugin<Project> {

    private static final String DEFAULT_GROUP = "BrowserStack";

    public void apply(Project project) {

        BrowserStackConfigExtension browserStackConfigExtension = project.getExtensions()
                .create("browserStackConfig", BrowserStackConfigExtension.class);

        // Get android appExtension
        AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");

        // Get all application variants or flavour combinations.
        DomainObjectSet<ApplicationVariant> appVariants = appExtension.getApplicationVariants();

        // Create tasks for each variant
        final Boolean[] isCLITaskCreated = new Boolean[1];
        isCLITaskCreated[0] = false;
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
                task.setGroup(DEFAULT_GROUP);
                task.setDescription("Uploads app / tests to AppAutomate and executes them");
                // Run Espresso tests without building the apk and test apk
                if (!project.hasProperty("skipBuildingApks")) {
                    task.dependsOn("assemble" + appVariantName, "assemble" + appVariantName + "AndroidTest");
                }
                task.setAppVariantBaseName(applicationVariant.getBaseName());
                task.setUsername(browserStackConfigExtension.getUsername());
                task.setAccessKey(browserStackConfigExtension.getAccessKey());
                task.setCustomId(browserStackConfigExtension.getCustomId());
                task.setConfigFilePath(browserStackConfigExtension.getConfigFilePath());
                task.setHost(Constants.BROWSERSTACK_API_HOST);
                task.setDebug(browserStackConfigExtension.isDebug());
            });

            project.getTasks().create("upload" + appVariantName + "ToBrowserstackAppLive", AppLiveUploadTask.class, (task) -> {
                task.setGroup(DEFAULT_GROUP);
                task.setDescription("Uploads app to AppLive");
                task.dependsOn("assemble" + appVariantName);
                task.setAppVariantBaseName(applicationVariant.getBaseName());
                task.setHost(Constants.BROWSERSTACK_API_HOST);
                task.setUsername(browserStackConfigExtension.getUsername());
                task.setAccessKey(browserStackConfigExtension.getAccessKey());
                task.setCustomId(browserStackConfigExtension.getCustomId());
                task.setDebug(browserStackConfigExtension.isDebug());
            });

            project.getTasks().create("upload" + appVariantName + "ToBrowserstackAppAutomate", AppAutomateUploadTask.class, (task) -> {
                task.setGroup(DEFAULT_GROUP);
                task.setDescription("Uploads app to AppAutomate");
                task.dependsOn("assemble" + appVariantName);
                task.setAppVariantBaseName(applicationVariant.getBaseName());
                task.setHost(Constants.BROWSERSTACK_API_HOST);
                task.setUsername(browserStackConfigExtension.getUsername());
                task.setAccessKey(browserStackConfigExtension.getAccessKey());
                task.setCustomId(browserStackConfigExtension.getCustomId());
                task.setDebug(browserStackConfigExtension.isDebug());
            });
            if (!isCLITaskCreated[0]) {
                project.getTasks().create("browserstackCLIWrapper", CLI.class, (task) -> {
                    task.setGroup(DEFAULT_GROUP);
                    task.setDescription("Just a wrapper on the Browserstack CLI. A way to run any Browserstack CLI command directly from gradle. \n" +
                            "\n" +
                            "\n" +
                            "For reference on Browserstack CLI please visit https://www.browserstack.com/app-automate/browserstack-cli\n" +
                            "\n" +
                            "\n" +
                            "Any CLI command passed in the custom option  -Pcommand will be executed and the results will be displayed on the terminal.\n" +
                            "\n" +
                            "\n" +
                            "For example:\n" +
                            "\n" +
                            "gradle browserstackCLIWrapper -Pcommand=”app-automate apps”\n" +
                            "\n" +
                            "The browserstack CLI command  app-automate apps  would run and the result will be displayed on the terminal.  ");

                    task.dependsOn("assemble" + appVariantName);
                    task.setAppVariantBaseName(applicationVariant.getBaseName());
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
        });
    }
}
