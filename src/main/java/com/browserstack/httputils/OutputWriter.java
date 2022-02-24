package com.browserstack.httputils;

import java.io.IOException;

/**
 * Generic writer for output message piping
 */
public interface OutputWriter {
    void write(String message) throws IOException;
    void writeBytes(byte[] bytes) throws IOException;
}
