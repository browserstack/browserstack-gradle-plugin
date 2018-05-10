# browserstack-gradle-plugin

This repository contains the source code for BrowserStack's Gradle plugin.

<br/>

>Note: For now this plugin supports Espresso tests.

## Espresso

Add to build.gradle:

    plugins {
        id: com.browserstack.gradle
    }

    runDebugBuildOnBrowserstack {
        username = "<username>"
        accessKey = "<accessKey>"
        devices = ['Google Pixel-7.1']
    }


Supported parameters:

    username: String
    accessKey: String
    devices: String[]
    video: boolean
    local: boolean
    localIdentifier: String
    deviceLogs: boolean
    networkLogs: boolean
    classes: String[]
    annotations: String[]
    packages: String[]
    sizes: String[]
    callbackURL: String


> Note: username, accessKey and devices are compulsory parameters. Visit https://www.browserstack.com/app-automate/espresso/get-started to get started with Espresso Tests on BrowserStack and also to know more about the above mentioned parameters.

> Note: List of supported devices and be found [here](https://api.browserstack.com/app-automate/espresso/devices.json) (basic auth required).

To run an Espresso test on BrowserStack using this plugin, use the following command:

    gradle runDebugBuildOnBrowserstack

This will execute the following:

 1. Build debug and test apks, as dependencies are declared on `assembleDebug` and `assembleDebugAndroidTest` tasks.
 2. Find the latest apks in the app directory recursively.
 3. Upload both the apks on BrowserStack platform.
 4. Execute Espresso test using the uploaded apps on the devices mentioned.
