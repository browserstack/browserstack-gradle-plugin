package com.browserstack.httputils;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Random;

/**
 * Generates and wraps a boundary string for multipart requests.
 */
public class RequestBoundary {

    @NotNull
    private final String boundary;

    @NotNull
    public static RequestBoundary generate() {
        return new RequestBoundary(
                new BigInteger(35, new Random()).toString()
        );
    }

    private RequestBoundary(@NotNull String boundary) {
        this.boundary = boundary;
    }

    @NotNull
    public String getBoundary() {
        return boundary;
    }
}
