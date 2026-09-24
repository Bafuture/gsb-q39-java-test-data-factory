package com.gsb.testdata;

import java.io.Serializable;
import java.util.function.BiConsumer;

/**
 * A serializable setter reference such as {@code User::setName}. Because it is
 * serializable, the library can recover the referenced property name from the
 * lambda's {@link java.lang.invoke.SerializedLambda} form.
 */
@FunctionalInterface
public interface FieldRef<T, V> extends BiConsumer<T, V>, Serializable {
}
