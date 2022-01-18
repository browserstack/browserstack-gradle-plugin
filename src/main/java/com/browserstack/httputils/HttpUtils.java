package com.browserstack.httputils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {

    /**
     * Uploads a file and binds custom data
     * @param url endpoint url
     * @param authorization authorization token
     * @param appPath raw file path
     * @param customId optional 'custom_id'
     * @return connection
     * @throws IOException error connecting / sending request
     */
    public static HttpURLConnection sendPostApp(
            @NotNull String url,
            @Nullable String authorization,
            @NotNull String appPath,
            @Nullable String customId
    ) throws IOException {
        final RequestBoundary requestBoundary = RequestBoundary.generate();
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        if (authorization != null) {
            con.setRequestProperty("Authorization", authorization);
        }
        final String contentType = "multipart/form-data; boundary=" + requestBoundary.getBoundary();
        con.setRequestProperty("Content-Type", contentType);
        con.setDoOutput(true);
        System.out.printf("Request method: %s\n", con.getRequestMethod());
        System.out.printf("Request properties: %s\n", con.getRequestProperties());
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        final MultipartRequestComposer requestComposer = MultipartRequestComposer.Builder
                .newInstance(requestBoundary)
                .addWriter(new OutputWriterDataStream(wr))
                //.addWriter(new OutputWriterLogger()) // For debug purposes
                .putFileFromPath(appPath)
                .putCustomId(customId)
                .build();
        requestComposer.write();
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

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());
        return response.toString();
    }
}