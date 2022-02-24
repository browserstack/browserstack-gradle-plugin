package com.browserstack.httputils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Bundles together multiple {@link OutputWriter} pipes
 */
public class OutputWriterBundle implements OutputWriter {

    @NotNull
    private final List<OutputWriter> writers;

    public OutputWriterBundle(@NotNull List<OutputWriter> writers) {
        this.writers = writers;
    }

    public OutputWriterBundle(@NotNull OutputWriter... writers) {
        this.writers = Arrays.asList(writers);
    }

    @Override
    public void write(String message) throws IOException {
        for (OutputWriter writer : writers) {
            writer.write(message);
        }
    }

    @Override
    public void writeBytes(byte[] bytes) throws IOException {
        for (OutputWriter writer : writers) {
            writer.writeBytes(bytes);
        }
    }
}
