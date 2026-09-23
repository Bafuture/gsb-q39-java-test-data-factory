package com.gsb.testdata.gen;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/** Metadata about a field, passed to constraints during generation. */
public record FieldInfo(String name, Class<?> type, Type genericType) {

    public static FieldInfo of(Field field) {
        return new FieldInfo(field.getName(), field.getType(), field.getGenericType());
    }
}
