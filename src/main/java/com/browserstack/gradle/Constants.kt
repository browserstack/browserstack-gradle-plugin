package com.browserstack.gradle

object Constants {
    const val BROWSERSTACK_API_HOST = "https://api-cloud.browserstack.com"
    const val APP_LIVE_HOST = "https://app-live.browserstack.com"
    const val APP_AUTOMATE_HOST = "https://app-automate.browserstack.com"

    const val BUILD_PATH = "/app-automate/espresso/build"
    const val APP_AUTOMATE_UPLOAD_PATH = "/app-automate/upload"
    const val APP_LIVE_UPLOAD_PATH = "/app-live/upload"
    const val TEST_SUITE_UPLOAD_PATH = "/app-automate/espresso/test-suite"

    const val DEFAULT_VIDEO = true
    const val DEFAULT_DEVICE_LOGS = true
    const val DEFAULT_NETWORK_LOGS = false
    const val DEFAULT_LOCAL = false
    const val APP_SEARCH_MAX_DEPTH = 10
}
