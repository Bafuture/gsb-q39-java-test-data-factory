package com.gsb.testdata;

/** Thrown when a constraint is internally inconsistent or incompatible with a field. */
public class InvalidConstraintException extends TestDataException {
    public InvalidConstraintException(String message) {
        super(message);
    }
}
