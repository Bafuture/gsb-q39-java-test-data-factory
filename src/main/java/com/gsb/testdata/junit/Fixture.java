package com.gsb.testdata.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test method parameter to be injected with a generated object by
 * {@link TestDataExtension}.
 *
 * <p>Without attributes, an automatic template is built for the parameter
 * type. {@link #value()} may name a static (or instance) field / no-arg method
 * on the test class that returns a {@code Template<T>}; {@link #seed()} pins
 * the random seed for reproducible injection.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Fixture {

  /** Name of a template field or no-arg method on the test class. */
  String value() default "";

  /** Seed for reproducible data; left unset for random data per run. */
  long seed() default Long.MIN_VALUE;
}
