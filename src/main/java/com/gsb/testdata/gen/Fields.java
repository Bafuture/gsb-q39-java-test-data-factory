package com.gsb.testdata.gen;

import com.gsb.testdata.TestDataException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Reflection helpers over a class hierarchy. */
public final class Fields {

    private Fields() {
    }

    public static Field find(Class<?> type, String name) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // try superclass
            }
        }
        throw new TestDataException("No field '" + name + "' on " + type.getName());
    }

    /** All non-static, non-final, non-synthetic instance fields, superclass fields included. */
    public static List<Field> all(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (field.isSynthetic() || Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                    continue;
                }
                fields.add(field);
            }
        }
        return fields;
    }

    public static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        return PRIMITIVE_WRAPPERS.getOrDefault(type, type);
    }

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            char.class, Character.class);
}
