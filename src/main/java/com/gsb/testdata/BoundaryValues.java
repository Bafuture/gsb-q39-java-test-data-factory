package com.gsb.testdata;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Computes boundary values (min/max/null/critical sizes) for a field. */
final class BoundaryValues {

  private BoundaryValues() {
  }

  static List<Object> forField(Field field, FieldConstraint c, Template<?> owner) {
    Class<?> type = field.getType();
    boolean nullable = c == null || c.isNullable();
    List<Object> boundaries = new ArrayList<>();

    if (isNumeric(type)) {
      if (c != null && c.getMin() != null) {
        boundaries.add(convert(c.getMin(), type));
      }
      if (c != null && c.getMax() != null) {
        boundaries.add(convert(c.getMax(), type));
      }
      if (nullable && !type.isPrimitive()) {
        boundaries.add(null);
      }
      return boundaries;
    }

    if (type == String.class) {
      String prefix = c != null && c.getPrefix() != null ? c.getPrefix() : "";
      int min = c != null && c.getMinLength() != null ? c.getMinLength() : 0;
      int max = c != null && c.getMaxLength() != null ? c.getMaxLength() : 0;
      if (nullable) {
        boundaries.add(null);
      }
      if (min == 0) {
        boundaries.add("");
      }
      int effectiveMin = Math.max(min, prefix.length());
      if (effectiveMin > 0) {
        boundaries.add(padded(prefix, effectiveMin, 'a'));
      }
      if (max > effectiveMin) {
        boundaries.add(padded(prefix, max, 'b'));
      }
      return boundaries;
    }

    if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)
        || type.isArray()) {
      int min = c != null && c.getMinSize() != null ? c.getMinSize() : 0;
      Integer max = c != null ? c.getMaxSize() : null;
      if (nullable) {
        boundaries.add(null);
      }
      if (min == 0) {
        boundaries.add(sized(field, 0, owner));
      }
      if (min > 0) {
        boundaries.add(sized(field, min, owner));
      }
      if (max != null && max > min) {
        boundaries.add(sized(field, max, owner));
      }
      return boundaries;
    }

    if (!type.isPrimitive() && nullable) {
      boundaries.add(null);
    }
    return boundaries;
  }

  /** Generates a collection/map/array of exactly {@code size} elements. */
  private static Object sized(Field field, int size, Template<?> owner) {
    FieldConstraint exact = new ConstraintBuilder().size(size, size).build();
    return owner.generator()
        .generate(field.getGenericType(), field.getType(), exact, owner.newContext());
  }

  private static String padded(String prefix, int length, char fill) {
    StringBuilder builder = new StringBuilder(prefix);
    while (builder.length() < length) {
      builder.append(fill);
    }
    return builder.toString();
  }

  private static boolean isNumeric(Class<?> type) {
    return type == byte.class || type == Byte.class
        || type == short.class || type == Short.class
        || type == int.class || type == Integer.class
        || type == long.class || type == Long.class
        || type == float.class || type == Float.class
        || type == double.class || type == Double.class;
  }

  private static Object convert(double value, Class<?> type) {
    if (type == byte.class || type == Byte.class) {
      return (byte) value;
    }
    if (type == short.class || type == Short.class) {
      return (short) value;
    }
    if (type == int.class || type == Integer.class) {
      return (int) value;
    }
    if (type == long.class || type == Long.class) {
      return (long) value;
    }
    if (type == float.class || type == Float.class) {
      return (float) value;
    }
    return value;
  }
}
