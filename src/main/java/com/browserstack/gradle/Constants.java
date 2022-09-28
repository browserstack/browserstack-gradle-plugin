package com.browserstack.gradle;

public class Constants {

    public static final String BROWSERSTACK_API_HOST = "https://api-cloud.browserstack.com",
                                APP_LIVE_HOST = "https://app-live.browserstack.com",
                                APP_AUTOMATE_HOST = "https://app-automate.browserstack.com",
                                BUILD_PATH = "/app-automate/espresso/v2/build",
                                APP_AUTOMATE_ESPRESSO_UPLOAD_PATH = "/app-automate/espresso/v2/app",
                                APP_AUTOMATE_UPLOAD_PATH = "/app-automate/upload",
                                APP_LIVE_UPLOAD_PATH = "/app-live/upload",
                                TEST_SUITE_UPLOAD_PATH = "/app-automate/espresso/v2/test-suite",
                                DEFAULT_NETWORK_PROFILE = null,
                                SYNC_CLI_VERSION = "3.0.0",
                                ARCH_64_BIT = "amd64",
                                ARCH_32_BIT = "386",
                                SYNC_CLI_DOWNLOAD_URL = "https://browserstack.com/browserstack-cli/download?";

    public static final boolean DEFAULT_VIDEO = true,
                                DEFAULT_DEVICE_LOGS = true,
                                DEFAULT_NETWORK_LOGS = false,
                                DEFAULT_LOCAL = false;

    public static final int APP_SEARCH_MAX_DEPTH = 10;

}
