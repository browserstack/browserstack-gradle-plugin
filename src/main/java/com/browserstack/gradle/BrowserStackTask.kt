package com.browserstack.gradle

import com.browserstack.httputils.HttpUtils
import com.browserstack.json.JSONObject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.ArrayList
import java.util.Base64
import java.util.function.BiPredicate

abstract class BrowserStackTask(
    @Input val host: String,
    @Input val variantBaseName: String,
    @Input val username: String? = null,
    @Input val accessKey: String? = null
) : DefaultTask() {

    protected open fun verifyParams() {
        require(!username.isNullOrBlank()) { "Missing mandatory username" }
        require(!accessKey.isNullOrBlank()) { "Missing mandatory accessKey" }
    }

    protected fun constructDefaultBuildParams(): JSONObject {
        return JSONObject().apply {
            // for monitoring, not for external use
            put("browserstack.source", "gradlePlugin")
        }
    }

    protected fun uploadApp(path: String, debugApkPath: Path): String {
        return try {
            val connection = HttpUtils.sendPost(
                host + path,
                basicAuth(),
                null,
                debugApkPath.toString()
            )
            when (val responseCode =
                connection.responseCode.also { println("App upload Response Code: $it") }) {
                200 -> {
                    val response = JSONObject(HttpUtils.getResponse(connection, responseCode))
                    response.getString("app_url").also { println("appUrl: $it") }
                }
                else -> throw Exception("App upload failed")
            }
        } catch (e: Exception) {
            e.printStackTrace();
            throw e
        }
    }

    protected fun basicAuth(): String {
        return "Basic " + Base64.getEncoder().encodeToString("$username:$accessKey".toByteArray())
    }

    protected fun getApkCollection(
        findTestApk: Boolean = false
    ): ApkCollection {
        val dir = System.getProperty("user.dir")
        val appApkFiles: MutableList<Path> = ArrayList()
        val testApkFiles: MutableList<Path> = ArrayList()

        Files.find(
            Paths.get(dir),
            Constants.APP_SEARCH_MAX_DEPTH,
            BiPredicate { filePath: Path, fileAttr: BasicFileAttributes ->
                isValidFile(filePath, fileAttr)
            }
        ).forEach { f: Path ->
            if (f.toString().endsWith("-androidTest.apk")) {
                testApkFiles.add(f)
            } else {
                appApkFiles.add(f)
            }
        }
        val debugApkPath =
            appApkFiles.findMostRecent().also { println("Most recent DebugApp apk: $it") }
                ?: error("Unable to find DebugApp apk")
        val testApkPath =
            testApkFiles.findMostRecent().also { println("Most recent TestApp apk: $it") }
                ?: run { if (findTestApk) error("unable to find TestApp apk") else null }

        return ApkCollection(
            debugApk = debugApkPath,
            testApk = testApkPath
        )
    }

    private fun isValidFile(
        filePath: Path,
        fileAttr: BasicFileAttributes
    ): Boolean {
        return fileAttr.isRegularFile && filePath.toString().endsWith(".apk") &&
            filePath.fileName.toString().contains(variantBaseName)
    }

    companion object {

        fun List<Path>.findMostRecent(): Path? = maxBy { it.toFile().lastModified() }

        data class ApkCollection(
            val debugApk: Path,
            val testApk: Path?
        )
    }
}
