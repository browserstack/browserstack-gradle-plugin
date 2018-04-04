package com.browserstack.httputils;

import java.net.HttpURLConnection;
import java.net.URL;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;

import java.util.Arrays;

public class HttpUtils {

    private static final String BOUNDARY = "----------------------------------",
                                DASHDASH = "--",
                                NEWLINE = "\r\n";

    private static final int BYTE_READ_BUFFER_SIZE = 8192;

    private static void writeApp(DataOutputStream wr, String appPath) throws Exception {
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

    public static HttpURLConnection sendPost(String url, String authorization, String body, String appPath) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");

        if(authorization != null)
            con.setRequestProperty("Authorization", authorization);

        String contentType;
        if(appPath == null)
            contentType = "application/json";
        else
            contentType = "multipart/form-data; boundary=" + BOUNDARY;

        con.setRequestProperty("Content-Type", contentType);

        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());

        if(appPath == null) {
            wr.writeBytes(body);
        } else {
            writeApp(wr, appPath);
        }

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