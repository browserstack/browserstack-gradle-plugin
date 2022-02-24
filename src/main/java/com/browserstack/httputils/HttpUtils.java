package com.browserstack.httputils;

import com.android.annotations.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpUtils {

    /**
     * Uploads a file and binds custom data
     * @param isDebug enabled debugging logs when forming a request:
     * @param wrapPropsAsInternalDataMap indicates extra properties to be wrapped into internal data map
     * @param url endpoint url
     * @param authorization authorization token
     * @param appPath raw file path
     * @param properties extra properties bind to request
     * @return connection
     * @throws IOException error connecting / sending request
     */
    public static HttpURLConnection sendPostApp(
            boolean isDebug,
            boolean wrapPropsAsInternalDataMap,
            @NotNull String url,
            @Nullable String authorization,
            @NotNull String appPath,
            @NonNull Map<String, String> properties
    ) throws IOException {
        final OutputWriterDebug debugWriter = OutputWriterDebug.withDebugEnabled(isDebug);
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
        debugWriter.write(String.format("Request method: %s\n", con.getRequestMethod()));
        debugWriter.write(String.format("Request properties: %s\n", con.getRequestProperties()));
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        final MultipartRequestComposer.Builder multipartRequestBuilder = MultipartRequestComposer.Builder
                .newInstance(requestBoundary)
                .addWriter(new OutputWriterDataStream(wr))
                .addWriter(debugWriter)
                .putFileFromPath(appPath)
                .putProperties(properties)
                .wrapProperiesAsInternalDataMap(wrapPropsAsInternalDataMap)
                ;
        multipartRequestBuilder
                .build()
                .write();
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

    public static String getResponse(HttpURLConnection con, int responseCode) throws IOException {
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