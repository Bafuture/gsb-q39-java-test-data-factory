package com.gsb.testdata;

import java.io.Serializable;

/**
 * Serializable getter reference, e.g. {@code User::getName}, used to address
 * a field in a refactor-safe way.
 */
@FunctionalInterface
public interface FieldRef<T, R> extends Serializable {
    R get(T target);
}
