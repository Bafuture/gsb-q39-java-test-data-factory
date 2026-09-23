package com.gsb.testdata;

import java.io.Serializable;

/**
 * Serializable setter reference, e.g. {@code User::setName}, used to address
 * a field in a refactor-safe way.
 */
@FunctionalInterface
public interface SetterRef<T, V> extends Serializable {
    void set(T target, V value);
}
