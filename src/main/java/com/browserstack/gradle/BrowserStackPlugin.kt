package com.browserstack.gradle

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class BrowserStackPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val appExtension = project.extensions.getByName("android") as AppExtension
        val bsConfigExtension =
            project.extensions.create("browserStackConfig", BrowserStackConfigExtension::class.java)

        // Create tasks for each variant
        appExtension.applicationVariants.all {
            val variant = this
            val variantName = variant.name.capitalize()

            project.tasks.register(
                "execute${variantName}TestsOnBrowserstack",
                EspressoTask::class.java,
                Constants.BROWSERSTACK_API_HOST,
                variant.baseName,
                bsConfigExtension
            ).configure {
                dependsOn("assemble$variantName", "assemble${variantName}AndroidTest")
            }

            project.tasks.register(
                "upload${variantName}ToBrowserstackAppLive",
                AppUploadTask::class.java,
                Constants.BROWSERSTACK_API_HOST,
                variant.baseName,
                bsConfigExtension
            ).configure {
                dependsOn("assemble$variantName")
            }
        }
    }
}
