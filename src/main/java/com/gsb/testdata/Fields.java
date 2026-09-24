package com.gsb.testdata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Reflection helpers for locating and populating bean fields. */
final class Fields {

  private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = Map.of(
      boolean.class, Boolean.class,
      byte.class, Byte.class,
      short.class, Short.class,
      int.class, Integer.class,
      long.class, Long.class,
      float.class, Float.class,
      double.class, Double.class,
      char.class, Character.class);

  private Fields() {
  }

  static Field find(Class<?> type, String name) {
    for (Class<?> current = type; current != null && current != Object.class;
        current = current.getSuperclass()) {
      try {
        return current.getDeclaredField(name);
      } catch (NoSuchFieldException ignored) {
        // keep walking up the hierarchy
      }
    }
    throw new TemplateConfigurationException(
        "Unknown field '" + name + "' on " + type.getName());
  }

  /** All non-static, non-final instance fields, including inherited ones. */
  static List<Field> allFields(Class<?> type) {
    List<Field> fields = new ArrayList<>();
    for (Class<?> current = type; current != null && current != Object.class;
        current = current.getSuperclass()) {
      for (Field field : current.getDeclaredFields()) {
        if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())
            || Modifier.isFinal(field.getModifiers())) {
          continue;
        }
        fields.add(field);
      }
    }
    return fields;
  }

  static void set(Object target, Field field, Object value) {
    try {
      field.setAccessible(true);
      field.set(target, value);
    } catch (ReflectiveOperationException | IllegalArgumentException e) {
      throw new TestDataException(
          "Failed to set field '" + field.getName() + "' on "
              + target.getClass().getName() + " to " + value, e);
    }
  }

  static Class<?> wrap(Class<?> type) {
    return type.isPrimitive() ? PRIMITIVE_WRAPPERS.get(type) : type;
  }
}
