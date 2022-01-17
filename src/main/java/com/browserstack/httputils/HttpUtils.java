package com.browserstack.httputils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class HttpUtils {

    private static final String BOUNDARY = "----------------------------------",
                                DASHDASH = "--",
                                NEWLINE = "\r\n";

    private static final int BYTE_READ_BUFFER_SIZE = 8192;

    private static void writeApp(
            @NotNull DataOutputStream wr,
            @NotNull String appPath
    ) throws Exception {
        System.out.println("Attaching file: " + appPath);
        File file = new File(appPath);
        FileInputStream in = new FileInputStream(appPath);

        try {

            String contentDisposition = "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"";
            String contentType = "Content-Type: application/octet-stream";

            wr.writeBytes(DASHDASH);
            wr.writeBytes(BOUNDARY);
            wr.writeBytes(NEWLINE);
            wr.writeBytes(contentDisposition);
            wr.writeBytes(NEWLINE);
            wr.writeBytes(contentType);
            wr.writeBytes(NEWLINE);
            wr.writeBytes(NEWLINE);

            byte[] buff = new byte[BYTE_READ_BUFFER_SIZE];

            for(int bytes; (bytes = in.read(buff)) > 0; wr.write(buff)) {
                if (bytes < BYTE_READ_BUFFER_SIZE) {
                    buff = Arrays.copyOfRange(buff, 0, bytes);
                }
            }

            wr.writeBytes(NEWLINE);
            wr.writeBytes(DASHDASH);
            wr.writeBytes(BOUNDARY);
            wr.writeBytes(DASHDASH);
            wr.writeBytes(NEWLINE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
    }

    private static void writeCustomId(
            @NotNull DataOutputStream wr,
            @NotNull String customId
    ) throws Exception {
        try {
            System.out.println("Attaching custom id: " + customId);
            String contentDisposition = "Content-Disposition: form-data; name=\"custom_id\"";
            wr.writeBytes(DASHDASH);
            wr.writeBytes(BOUNDARY);
            wr.writeBytes(NEWLINE);
            wr.writeBytes(contentDisposition);
            wr.writeBytes(NEWLINE);
            wr.writeBytes(NEWLINE);
            wr.writeBytes(customId);
            wr.writeBytes(NEWLINE);

            wr.writeBytes(DASHDASH);
            wr.writeBytes(BOUNDARY);
            wr.writeBytes(DASHDASH);
            wr.writeBytes(NEWLINE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HttpURLConnection sendPostApp(
            @NotNull String url,
            @Nullable String authorization,
            @NotNull String appPath,
            @Nullable String customId
    ) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        if (authorization != null) {
            con.setRequestProperty("Authorization", authorization);
        }
        final String contentType = "multipart/form-data; boundary=" + BOUNDARY;
        con.setRequestProperty("Content-Type", contentType);
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        writeApp(wr, appPath);
        if (customId != null) {
            writeCustomId(wr, customId);
        }
        wr.flush();
        wr.close();
        return con;
    }

    public static HttpURLConnection sendPostBody(
            @NotNull String url,
            @Nullable String authorization,
            @NotNull String body
    ) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");

        if (authorization != null) {
            con.setRequestProperty("Authorization", authorization);
        }
        final String contentType = "application/json";
        con.setRequestProperty("Content-Type", contentType);
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(body);
        wr.flush();
        wr.close();
        return con;
    }

    public static String getResponse(HttpURLConnection con, int responseCode) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(responseCode == 200 ? con.getInputStream() : con.getErrorStream()));

        String inputLine;
        StringBuffer response = new StringBuffer();

        while((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());
        return response.toString();
    }

}