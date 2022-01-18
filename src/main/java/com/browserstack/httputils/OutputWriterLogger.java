package com.browserstack.httputils;

import java.io.IOException;

/**
 * Pipes messages to System.out
 * Mainly for debug purposes
 */
public class OutputWriterLogger implements OutputWriter {

    @Override
    public void write(String message) throws IOException {
        System.out.print(message);
    }

    @Override
    public void writeBytes(byte[] bytes) throws IOException {
        System.out.print("<---Raw bytes--->");
    }
}
