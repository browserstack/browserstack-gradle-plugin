# browserstack-gradle-plugin

This repository contains the source code for BrowserStack's Gradle plugin.


## PURPOSE

This plugin consists of two types of tasks:

1. Builds, uploads and starts Espresso tests on BrowserStack AppAutomate.
2. Builds and uploads apk to BrowserStack AppLive for manual testing.

## USAGE

### Add to build.gradle

#### Add plugin using the plugins DSL

```
plugins {
  id("com.browserstack.gradle") version "3.1.3"
}
```

#### Add plugin dependency using legacy plugin application

```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.browserstack.gradle:browserstack-gradle-plugin:3.1.3"
  }
}

apply plugin: "com.browserstack.gradle"
```

#### Add browserStackConfig parameters to module's build.gradle

```
browserStackConfig {
    username = "<browserstack_username>"
    accessKey = "<browserstack_access_key>"
    configFilePath = '<path/to/your/json/configFile>'
}
```

#### Sample Config file
```
{
  "devices": [
      "Google Pixel-7.1"
    ],
  "deviceLogs": true,
  "networkLogs": true,
  "project": "Awesome gradle plugin build",
  "shards": {
    "numberOfShards": 3
  }
}
```
> Note: To view the list of all supported parameters for Espresso tests on BrowserStack, visit complete list of API parameters section inside our [Espresso Get Started documentation](https://www.browserstack.com/app-automate/espresso/get-started)

### Tasks

#### Espresso test task
Builds, uploads and start Espresso tests on BrowserStack AppAutomate.

##### Gradle command

gradle clean execute${buildVariantName}TestsOnBrowserstack

For running tests on a project with no variants, you can simply run following command for building, uploading and running tests on debug apk:

```
gradle clean executeDebugTestsOnBrowserstack
```

And for projects with productFlavors, replace ${buildVariantName} with your build variant name, for example if your productFlavor name is "phone" and you want to test debug build type of this variant then command will be

```
gradle clean executePhoneDebugTestsOnBrowserstack

```

For running tests on a project without rebuilding apk and test suite, you can simply run following command for uploading and running tests on debug apk:

```
gradle executeDebugTestsOnBrowserstack -PskipBuildingApks=true
```

For specifying a config file from the command line, you can simply run the following command:

```
gradle clean executeDebugTestsOnBrowserstack --config-file='config-browserstack.json'
```
Note, this will override the entry within configFilePath.

##### Supported browserStackConfig parameters

    username: String
    accessKey: String
    configFilePath: String # Filepath that has capabilities specifed to run the build

#### Browserstack CLI task
Acts as a wrapper around the browserstack CLI and allows the operation of the CLI directly from gradle (Available from version 3.1.0)

##### Gradle command

`
gradle browserstackCLIWrapper -Pcommand="browserstack-cli-command-goes-here"
`

Example usage

```
gradle browserstackCLIWrapper -Pcommand="app-automate espresso run -a local-path-to-app-apk  -t local-path-to-test-suite-apk"
```

You can refer to the existing browserstack CLI documentation [here](https://www.browserstack.com/app-automate/browserstack-cli)

Any Browserstack CLI command can directly be passed to the -Pcommand parameter and it would execute the CLI command from gradle and push the output to stdout/terminal



> Note: username, accessKey and configFilePath are mandatory parameters. Visit https://www.browserstack.com/app-automate/espresso/get-started to get started with Espresso Tests on BrowserStack and also to know more about the above mentioned parameters.

> Note: List of supported devices and be found [here](https://api.browserstack.com/app-automate/espresso/devices.json) (basic auth required). For example :``` curl -u "$BROWSERSTACK_USERNAME:$BROWSERSTACK_ACCESS_KEY" https://api-cloud.browserstack.com/app-automate/devices.json ```

> Note: You can also set the values of username and accessKey in environment variables with names BROWSERSTACK_USERNAME and BROWSERSTACK_ACCESS_KEY, respectively. If you do this, then there is no need to set this parameters in browserStackConfig block.

> Note: From version 3.1.0 onwards, usage of the Browserstack CLI task will download and install the latest version of Browserstack CLI on your machine.

##### Internal steps

 1. Build debug and test apks, as dependencies are declared on `assemble${buildvariantName}` and `assemble${buildvariantName}AndroidTest` tasks.
 2. Find the latest apks in the app directory recursively.
 3. Upload both the apks on BrowserStack AppAutomate platform.
 4. Execute Espresso test using the uploaded apps on the devices mentioned.

#### Upload to AppLive task

##### Gradle command

gradle clean upload${buildVariantName}ToBrowserstackAppLive

For running tests on a project with no variants, you can simply run following command for uploading debug apk:

```
gradle clean uploadDebugToBrowserstackAppLive
```

And for projects with productFlavors, replace ${buildVariantName} with your build variant name, for example if your productFlavor name is "phone" and you want to upload debug build type of this variant then command will be gradle clean uploadPhoneDebugToBrowserstackAppLive.

##### Supported browserStackConfig parameters

    username: String
    accessKey: String

> Note: username and accessKey are mandatory parameters.

##### Internal steps

 1. Build debug and test apks, as dependencies are declared on `assemble${buildvariantName}` .
 2. Find the latest apk in the app directory recursively.
 3. Upload the apk on BrowserStack AppLive platform.


> Note: You can also set the values of username and accessKey in environment variables with names BROWSERSTACK_USERNAME and BROWSERSTACK_ACCESS_KEY, respectively. If you do this, then there is no need to set these parameters in browserStackConfig block.

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
