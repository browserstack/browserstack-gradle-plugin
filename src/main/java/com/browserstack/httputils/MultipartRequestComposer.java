package com.browserstack.httputils;

import com.android.annotations.NonNull;
import com.browserstack.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a multipart request
 */
public class MultipartRequestComposer {

    private static final String KEY_FILE = "file";
    private static final String KEY_DATA = "data";

    @NotNull private final OutputWriter writer;
    @NotNull private final String boundary;
    @NotNull private final Map<String, Object> dataMap;

    public MultipartRequestComposer(@NotNull Builder builder) {
        this.writer = new OutputWriterBundle(builder.writers);
        this.boundary = builder.boundary.getBoundary();
        this.dataMap = builder.dataMap;
    }

    private MultipartRequestComposer() {
        // Cannot build from empty constructor
        throw new IllegalArgumentException();
    }

    public void write() throws IOException {
        writeRequest(dataMap);
    }

    private void writeRequest(
            @NotNull Map<String, Object> dataMap
    ) throws IOException {
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            writer.write(String.format("--%s\r\nContent-Disposition: form-data; ", boundary));
            if (entry.getValue() instanceof File) {
                final File entryAsFile = ((File) entry.getValue());
                final Path filePath = entryAsFile.toPath();
                final String contentType = "application/octet-stream";
                writer.write(
                        String.format(
                                "name=\"%s\"; filename=\"%s\"\r\nContent-Type: %s\r\n\r\n",
                                entry.getKey(),
                                entryAsFile.getName(),
                                contentType
                        )
                );
                writer.writeBytes(Files.readAllBytes(filePath));
                writer.write("\r\n");
            }
            if (entry.getValue() instanceof Map) {
                final Map<String, String> entryAsDataMap = ((Map<String, String>) entry.getValue());
                final JSONObject internalEntriesAsJson = new JSONObject();
                for (Map.Entry<String, String> internalEntry : entryAsDataMap.entrySet()) {
                    internalEntriesAsJson.put(internalEntry.getKey(), internalEntry.getValue());
                }
                writer.write(
                        String.format(
                                "name=\"%s\"\r\n\r\n%s\r\n",
                                entry.getKey(),
                                internalEntriesAsJson.toString()
                        )
                );
            }
            if (entry.getValue() instanceof String) {
                final String entryAsString = ((String) entry.getValue());
                writer.write(
                        String.format(
                                "name=\"%s\"\r\n\r\n%s\r\n",
                                entry.getKey(),
                                entryAsString
                        )
                );
            }
        }
        writer.write(
                String.format(
                        "--%s--",
                        boundary
                )
        );
    }

    @NotNull
    public String getBoundary() {
        return boundary;
    }

    public static class Builder {

        @NotNull private final RequestBoundary boundary;
        @NotNull private final List<OutputWriter> writers = new ArrayList<>();
        @NotNull private final Map<String, Object> dataMap = new HashMap<>();
        @NotNull private final Map<String, String> propertyMap = new HashMap<>();
        private boolean wrapProperiesAsInternalDataMap = false;

        public static Builder newInstance(
                @NotNull RequestBoundary requestBoundary
        ) {
            return new Builder(
                    requestBoundary
            );
        }

        private Builder(@NotNull RequestBoundary boundary) {
            this.boundary = boundary;
        }

        public Builder addWriter(@NotNull OutputWriter writer) {
            this.writers.add(writer);
            return this;
        }

        public Builder putFileFromPath(@NotNull String filePath) {
            final File file = new File(filePath);
            if (file.exists()) {
                this.dataMap.put(KEY_FILE, file);
            }
            return this;
        }

        public Builder putKeyValue(
                @NonNull String key,
                @Nullable String value
        ) {
            if (value != null) {
                this.propertyMap.put(key, value);
            }
            return this;
        }

        public Builder putProperties(
                @NonNull Map<String, String> properties
        ) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                if (entry.getValue() != null) {
                    this.propertyMap.put(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        /**
         * Indicates to either wrap all additional properties as an internal data map
         * or use as regular properties when forming a reuqest
         * @param isWrapEnabled is wrapping enabled
         * @return builder
         */
        public Builder wrapProperiesAsInternalDataMap(boolean isWrapEnabled) {
            this.wrapProperiesAsInternalDataMap = isWrapEnabled;
            return this;
        }

        public MultipartRequestComposer build() {
            if (wrapProperiesAsInternalDataMap) {
                this.dataMap.put(KEY_DATA, this.propertyMap);
            } else {
                this.dataMap.putAll(propertyMap);
            }
            return new MultipartRequestComposer(this);
        }
    }
}
