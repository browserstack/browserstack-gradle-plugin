package com.browserstack.gradle

open class BrowserStackConfigExtension {

    var username: String = System.getenv("BROWSERSTACK_USERNAME")
    var accessKey: String = System.getenv("BROWSERSTACK_ACCESS_KEY")

    var classes: Array<String> = emptyArray()
    var annotations: Array<String?> = emptyArray()
    var packages: Array<String?> = emptyArray()
    var sizes: Array<String?> = emptyArray()
    var otherApps: Array<String?> = emptyArray()
    var devices: Array<String> = emptyArray()

    var video: Boolean = Constants.DEFAULT_VIDEO
    var deviceLogs: Boolean = Constants.DEFAULT_DEVICE_LOGS
    var local: Boolean = Constants.DEFAULT_LOCAL
    var networkLogs: Boolean = Constants.DEFAULT_NETWORK_LOGS

    var enableSpoonFramework: Boolean = false
    var allowDeviceMockServer: Boolean = false
    var disableAnimations: Boolean = false

    var callbackURL: String? = null
    var localIdentifier: String? = null
    var networkProfile: String? = null
    var timeZone: String? = null
    var customBuildName: String? = null
    var customBuildNotifyURL: String? = null
    var geoLocation: String? = null
    var language: String? = null
    var locale: String? = null
    var deviceOrientation: String? = null
    var projectName: String? = null

    var appStoreConfiguration: Array<String> = emptyArray()
        set(value) {
            require(value.isEmpty() || value.size == 2) { "appStoreConfiguration requires 0 or 2 entries" }
            field = value
        }
    val appStoreConfigurationMap: Map<String, String>
        get() {
            return if (appStoreConfiguration.isEmpty()) {
                emptyMap()
            } else {
                mapOf(
                    "username" to appStoreConfiguration[0],
                    "password" to appStoreConfiguration[1]
                )
            }
        }
}
