package com.browserstack.gradle;

import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.*;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CLI extends BrowserStackTask {

    private Boolean isWindows;
    private String directory = System.getProperty("user.dir");
    private String fileName = "browserstack";
    private String downloadedFileName = "browserstackcli";
    private String arch;
    private String os;

    public void verifyParams() throws Exception {
        String username = this.getUsername();
        String accessKey = this.getAccessKey();
        if (username == null || accessKey == null || username == "" || accessKey == "" || username.equals("null")) {
            throw new Exception("`username`, `accessKey` and `configFilePath` are compulsory");
        }
    }

    private boolean initialize() {
        setOS();
        isWindows = isWindows();
        if (isWindows) {
            fileName = fileName + ".exe";
            downloadedFileName = downloadedFileName + ".exe";
            String wowArch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
            String realArch = wowArch != null && wowArch.endsWith("64")
                    || wow64Arch != null && wow64Arch.endsWith("64")
                    ? "64" : "32";
            if (realArch.equals("64")) {
                arch = Constants.ARCH_64_BIT;
            } else {
                arch = Constants.ARCH_32_BIT;
            }
        } else {
            if (System.getProperty("os.arch").contains("64")) {
                arch = Constants.ARCH_64_BIT;
            } else {
                arch = Constants.ARCH_32_BIT;
            }
        }
        if (!new File(directory, downloadedFileName).exists()) {
            install();
            try {
                givePermission();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private void setOS() {
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().startsWith("windows")) {
            os = "windows";
        } else if (osName.toLowerCase().startsWith("mac")) {
            os = "darwin";
        } else {
            os = "linux";
        }
    }

    @TaskAction
    void runCLICommands() throws Exception {
        verifyParams();
        if (!initialize()) {
            System.out.println("Something went wrong!!");
            return;
        }
        String s = "";
        try {
            authenticate();
            StringBuilder commandBuilder = getCommandPrefix();
            commandBuilder.append(" ").append(command);
            String finalCommand = commandBuilder.toString();
            Process process = runProcess(finalCommand);
            inheritIO(process.getInputStream(), System.out);
            inheritIO(process.getErrorStream(), System.err);
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void inheritIO(final InputStream src, final PrintStream dest) {
        new Thread(new Runnable() {
            public void run() {
                Scanner sc = new Scanner(src);
                while (sc.hasNextLine()) {
                    dest.println(sc.nextLine());
                }
            }
        }).start();
    }

    private StringBuilder getCommandPrefix() {
        StringBuilder commandBuilder = new StringBuilder("");
        if (isWindows) {
            commandBuilder.append(downloadedFileName);
        } else {
            commandBuilder.append("./");
            commandBuilder.append(downloadedFileName);
        }
        return commandBuilder;
    }

    private void authenticate() throws InterruptedException {
        StringBuilder commandBuilder = getCommandPrefix();
        commandBuilder.append(" authenticate --username=").append(this.getUsername()).append(" --access-key=").append(this.getAccessKey());
        Process process = runProcess(commandBuilder.toString());
        process.waitFor();
    }

    private Process runProcess(String command) {
        Process process = null;
        ProcessBuilder builder;
        List<String> list = new ArrayList<>();
        Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command);
        while (matcher.find())
            list.add(matcher.group(1)); // Add .replace("\"", "") to remove surrounding quotes.

        try {
            if (isWindows) {
                process = Runtime.getRuntime().exec(command.replace("\"", "").split(" "), null, new File(directory));
            } else {
                process = Runtime.getRuntime().exec(list.toArray(new String[0]), null, new File(directory));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return process;
    }

    private Boolean isWindows() {
        return os.equals("windows");
    }

    private void install() {
        try {
            String URL = generateDownloadURL();
            URL url = new URL(URL);
            InputStream in = url.openStream();
            FileOutputStream fos = new FileOutputStream(new File(directory + "/" + downloadedFileName));

            int length = -1;
            byte[] buffer = new byte[1024];// buffer for portion of data from
            // connection
            while ((length = in.read(buffer)) > -1) {
                fos.write(buffer, 0, length);
            }
            fos.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private String generateDownloadURL() {
        StringBuilder urlBuilder = new StringBuilder(Constants.SYNC_CLI_DOWNLOAD_URL);
        urlBuilder.append("arch=");
        urlBuilder.append(arch);
        urlBuilder.append("&file=");
        urlBuilder.append(fileName);
        urlBuilder.append("&os=");
        urlBuilder.append(os);
        urlBuilder.append("&version=");
        urlBuilder.append(Constants.SYNC_CLI_VERSION);
        return urlBuilder.toString();
    }

    private void givePermission() throws InterruptedException {
        if (!isWindows) {
            Process process = runProcess("chmod +x " + downloadedFileName);
            process.waitFor();
        }
    }
}
