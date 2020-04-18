package com.browserstack.gradle

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class AppUploadTask @Inject constructor(
    host: String,
    variantBaseName: String,
    @Input val extension: BrowserStackConfigExtension
) : BrowserStackTask(
    host = host,
    variantBaseName = variantBaseName,
    username = extension.username,
    accessKey = extension.accessKey
) {

    @TaskAction
    fun uploadApk() {
        verifyParams()

        uploadApp(
            Constants.APP_LIVE_UPLOAD_PATH,
            getApkCollection().debugApk
        ).also(::displayTestURL)
    }

    private fun displayTestURL(appUrl: String) {
        val appHashedId = appUrl.removePrefix("bs://")
        val url = "${Constants.APP_LIVE_HOST}/#app_hashed_id=$appHashedId"
        println("Start testing at $url")
    }
}
