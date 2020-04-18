package com.browserstack.gradle

import com.browserstack.httputils.HttpUtils
import com.browserstack.json.JSONObject
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path
import javax.inject.Inject

open class EspressoTask @Inject constructor(
    host: String,
    variantBaseName: String,
    @Input val extension: BrowserStackConfigExtension
) : BrowserStackTask(
    host = host,
    variantBaseName = variantBaseName,
    username = extension.username,
    accessKey = extension.accessKey
) {

    override fun verifyParams() {
        super.verifyParams()
        require(extension.devices.isNotEmpty()) { "You must specify at least one device (devices)" }
    }

    @TaskAction
    open fun uploadAndExecuteTest() {
        verifyParams()

        // First upload app
        val apkFiles = getApkCollection(findTestApk = true)
        val app = uploadApp(Constants.APP_AUTOMATE_UPLOAD_PATH, apkFiles.debugApk)

        // Then upload and execute test suite
        val testSuite = uploadTestSuite(apkFiles.testApk ?: error("testApk not found"))
        executeTestSuite(app, testSuite).also(::displayDashboardURL)
    }

    private fun constructBuildParams(app: String, testSuite: String): String {
        return constructDefaultBuildParams().apply {
            put("app", app)
            put("testSuite", testSuite)

            put("devices", extension.devices)
            put("class", extension.classes)
            put("package", extension.packages)
            put("size", extension.sizes)
            put("annotation", extension.annotations)
            put("otherApps", extension.otherApps)
            put("video", extension.video)
            put("deviceLogs", extension.deviceLogs)
            put("networkLogs", extension.networkLogs)
            put("local", extension.local)
            put("localIdentifier", extension.localIdentifier)
            put("networkProfile", extension.networkProfile)
            put("callbackURL", extension.callbackURL)
            put("timezone", extension.timeZone)
            put("appStoreConfiguration", extension.appStoreConfigurationMap)
            put("enableSpoonFramework", extension.enableSpoonFramework)
            put("disableAnimations", extension.disableAnimations)
            put("allowDeviceMockServer", extension.allowDeviceMockServer)
            put("customBuildName", extension.customBuildName)
            put("customBuildNotifyURL", extension.customBuildNotifyURL)
            put("project", extension.projectName)
            put("geoLocation", extension.geoLocation)
            put("language", extension.language)
            put("locale", extension.locale)
            put("deviceOrientation", extension.deviceOrientation)
        }.toString()
    }

    private fun uploadTestSuite(testApkPath: Path?): String {
        return try {
            val connection = HttpUtils.sendPost(
                host + Constants.TEST_SUITE_UPLOAD_PATH,
                basicAuth(),
                null,
                testApkPath.toString()
            )
            when (val responseCode =
                connection.responseCode.also { println("TestSuite upload Response Code: $it") }) {
                200 -> {
                    val response = JSONObject(HttpUtils.getResponse(connection, responseCode))
                    response.getString("test_url")
                }
                else -> throw Exception("TestSuite upload failed")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun executeTestSuite(app: String, testSuite: String): String {
        return try {
            val connection = HttpUtils.sendPost(
                host + Constants.BUILD_PATH,
                basicAuth(),
                constructBuildParams(app, testSuite),
                null
            )
            val responseCode = connection.responseCode.also { println("Response Code : $it") }
            val response = JSONObject(HttpUtils.getResponse(connection, responseCode))
            response.getString("build_id")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun displayDashboardURL(buildId: String) {
        println("View build status at ${Constants.APP_AUTOMATE_HOST}/builds/$buildId")
    }
}
