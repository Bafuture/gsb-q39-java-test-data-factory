package com.gsb.testdata.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test method parameter to be injected with a generated instance by
 * {@link TestDataExtension}. Use {@link #seed()} for reproducible data.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Generated {

    /** Random seed; left unset, every run produces different data. */
    long seed() default Long.MIN_VALUE;
}
