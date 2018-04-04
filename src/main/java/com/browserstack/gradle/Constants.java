package com.browserstack.gradle;

public class Constants {

    public static final String BROWSERSTACK_API_HOST = "api.browserstack.com",
                                BUILD_PATH = "/app-automate/espresso/build",
                                APP_UPLOAD_PATH = "/app-automate/upload",
                                TEST_SUITE_UPLOAD_PATH = "/app-automate/espresso/test-suite";

    public static final boolean DEFAULT_VIDEO = true,
                                DEFAULT_DEVICE_LOGS = true,
                                DEFAULT_LOCAL = false;

    public static final int APP_SEARCH_MAX_DEPTH = 10;

}