package com.chronode.exception;

public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(final String message) {
        super(message);
    }
}
