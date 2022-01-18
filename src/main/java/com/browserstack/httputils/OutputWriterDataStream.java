package com.browserstack.httputils;

import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Pipes messages to {@link DataOutputStream}
 */
public class OutputWriterDataStream implements OutputWriter {

    @NotNull
    private final DataOutputStream wr;

    public OutputWriterDataStream(@NotNull DataOutputStream wr) {
        this.wr = wr;
    }

    @Override
    public void write(String message) throws IOException {
        wr.write(message.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void writeBytes(byte[] bytes) throws IOException {
        wr.write(bytes);
    }
}
