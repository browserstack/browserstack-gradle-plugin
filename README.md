# browserstack-gradle-plugin

This repository contains the source code for BrowserStack's Gradle plugin.

<br/>

>Note: For now this plugin supports Espresso tests.

## PURPOSE

This plugin consists of two types of tasks:

1. Builds, uploads and starts espresso tests on BrowserStack AppAutomate.
2. Builds and uploads apk to BrowserStack AppLive for manual testing.

## USAGE

### Add to build.gradle

#### Add plugin dependency to build.gradle

```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath 'com.browserstack.gradle:com.browserstack.gradle.gradle.plugin:2.3.0'
  }
}
```

#### Apply plugin to module's build.gradle

```
apply plugin: 'com.browserstack.gradle'
```

#### Add browserStackConfig parameters to module's build.gradle

```
browserStackConfig {
    username = "<browserstack_username>"
    accessKey = "<browserstack_access_key>"
    devices = ['Google Pixel-7.1']
}
```

### Tasks

#### Espresso test task
Builds, uploads and start espresso tests on BrowserStack AppAutomate.

##### Gradle command

gradle clean execute${buildVariantName}TestsOnBrowserstack

> Note: Replace ${buildVariantName} with your build variant name, for example if your productFlavor name is "phone" and you want to test debug build type of this variant then command will be gradle clean executePhoneDebugTestsOnBrowserstack.

##### Supported browserStackConfig parameters

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
    otherApps: String[]
    callbackURL: String
    networkProfile: String

> Note: username, accessKey and devices are compulsory parameters. Visit https://www.browserstack.com/app-automate/espresso/get-started to get started with Espresso Tests on BrowserStack and also to know more about the above mentioned parameters.

> Note: List of supported devices and be found [here](https://api.browserstack.com/app-automate/espresso/devices.json) (basic auth required). 

> Note: You can also set the values of username and accessKey in environment variables with names BROWSERSTACK_USERNAME and BROWSERSTACK_ACCESS_KEY, respectively. If you do this, then there is no need to set this parameters in browserStackConfig block.

##### Internal steps

 1. Build debug and test apks, as dependencies are declared on `assemble${buildvariantName}` and `assemble${buildvariantName}AndroidTest` tasks.
 2. Find the latest apks in the app directory recursively.
 3. Upload both the apks on BrowserStack AppAutomate platform.
 4. Execute Espresso test using the uploaded apps on the devices mentioned.

#### Upload to AppLive task

##### Gradle command

gradle clean upload${buildVariantName}ToBrowserstackAppLive

> Note: Replace ${buildVariantName} with your build variant name, for example if your productFlavor name is "phone" and you want to upload debug build type of this variant then command will be gradle clean uploadPhoneDebugToBrowserstackAppLive.

##### Supported browserStackConfig parameters

    username: String
    accessKey: String

> Note: username and accessKey are compulsory parameters.

##### Internal steps

 1. Build debug and test apks, as dependencies are declared on `assemble${buildvariantName}` .
 2. Find the latest apk in the app directory recursively.
 3. Upload the apk on BrowserStack AppLive platform.
 

> Note: You can also set the values of username and accessKey in environment variables with names BROWSERSTACK_USERNAME and BROWSERSTACK_ACCESS_KEY, respectively. If you do this, then there is no need to set this parameters in browserStackConfig block.

> Note: You can also see all possible tasks by running "gradle tasks -all"

### Development

Build the plugin

```
gradle clean build
```

To install the plugin into local maven repo

```
mvn install:install-file -Dfile=build/libs/browserstack-gradle-plugin-VERSION.jar -DgroupId=com.browserstack -DartifactId=gradle -Dversion=VERSION -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
```
