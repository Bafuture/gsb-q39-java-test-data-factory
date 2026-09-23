package com.gsb.testdata;

/** Base runtime exception for all template/definition-time errors. */
public class TestDataException extends RuntimeException {
    public TestDataException(String message) {
        super(message);
    }

    public TestDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
