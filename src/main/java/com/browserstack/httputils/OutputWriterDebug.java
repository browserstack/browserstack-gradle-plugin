package com.browserstack.httputils;

import java.io.IOException;

/**
 * Pipes messages to System.out
 * Mainly for debug purposes
 */
public class OutputWriterDebug implements OutputWriter {

    private final boolean isEnabled;

    private OutputWriterDebug(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public static OutputWriterDebug withDebugEnabled(boolean isEnabled) {
        return new OutputWriterDebug(isEnabled);
    }

    @Override
    public void write(String message) throws IOException {
        if (isEnabled) {
            System.out.print(message);
        }
    }

    @Override
    public void writeBytes(byte[] bytes) throws IOException {
        if (isEnabled) {
            System.out.print("<---Raw bytes--->");
        }
    }
}
