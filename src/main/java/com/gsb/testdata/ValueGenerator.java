package com.gsb.testdata;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Generates deterministic (seed-driven) default values for any field type:
 * primitives and wrappers, strings, enums, collections, maps, arrays, common
 * date/time types and nested POJOs (with depth-limit protection).
 */
final class ValueGenerator {

  private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";

  Object generate(Field field, FieldConstraint constraint, GenerationContext ctx) {
    return generate(field.getGenericType(), field.getType(), constraint, ctx);
  }

  Object generate(Type genericType, Class<?> type, FieldConstraint c, GenerationContext ctx) {
    Random random = ctx.random();
    if (type == String.class) {
      return randomString(c, random);
    }
    if (type == boolean.class || type == Boolean.class) {
      return random.nextBoolean();
    }
    if (type == int.class || type == Integer.class) {
      return (int) randomLong(c, random, 1, 1_000);
    }
    if (type == long.class || type == Long.class) {
      return randomLong(c, random, 1, 100_000);
    }
    if (type == short.class || type == Short.class) {
      return (short) randomLong(c, random, 1, 1_000);
    }
    if (type == byte.class || type == Byte.class) {
      return (byte) randomLong(c, random, 1, 100);
    }
    if (type == double.class || type == Double.class) {
      return randomDouble(c, random, 0, 10_000);
    }
    if (type == float.class || type == Float.class) {
      return (float) randomDouble(c, random, 0, 10_000);
    }
    if (type == char.class || type == Character.class) {
      return (char) ('a' + random.nextInt(26));
    }
    if (type == BigDecimal.class) {
      return BigDecimal.valueOf(randomDouble(c, random, 0, 10_000))
          .setScale(2, RoundingMode.HALF_UP);
    }
    if (type == BigInteger.class) {
      return BigInteger.valueOf(randomLong(c, random, 1, 100_000));
    }
    if (type.isEnum()) {
      Object[] constants = type.getEnumConstants();
      return constants[random.nextInt(constants.length)];
    }
    if (type == LocalDate.class) {
      return LocalDate.ofEpochDay(random.nextLong(0, 20_000));
    }
    if (type == LocalTime.class) {
      return LocalTime.of(random.nextInt(24), random.nextInt(60), random.nextInt(60));
    }
    if (type == LocalDateTime.class) {
      return LocalDateTime.of(
          LocalDate.ofEpochDay(random.nextLong(0, 20_000)),
          LocalTime.of(random.nextInt(24), random.nextInt(60), random.nextInt(60)));
    }
    if (type == Instant.class) {
      return Instant.ofEpochMilli(random.nextLong(0, 2_000_000_000_000L));
    }
    if (type == ZonedDateTime.class) {
      return ZonedDateTime.ofInstant(
          Instant.ofEpochMilli(random.nextLong(0, 2_000_000_000_000L)), ZoneOffset.UTC);
    }
    if (type == Date.class) {
      return Date.from(Instant.ofEpochMilli(random.nextLong(0, 2_000_000_000_000L)));
    }
    if (type == UUID.class) {
      return new UUID(random.nextLong(), random.nextLong());
    }
    if (type.isArray()) {
      return randomArray(type.getComponentType(), c, ctx);
    }
    if (Collection.class.isAssignableFrom(type)) {
      return randomCollection(genericType, type, c, ctx);
    }
    if (Map.class.isAssignableFrom(type)) {
      return randomMap(genericType, type, c, ctx);
    }
    if (type == Object.class) {
      // Unresolvable generic element type: fall back to a random string.
      return randomString(null, random);
    }
    return nestedPojo(type, ctx);
  }

  private Object nestedPojo(Class<?> type, GenerationContext ctx) {
    if (ctx.depth() >= ctx.maxDepth()) {
      return null;
    }
    Template<?> nested = ctx.templates().get(type);
    if (nested != null) {
      return nested.createWith(ctx);
    }
    Object instance = Instantiator.instantiate(type);
    GenerationContext child = ctx.descend();
    for (Field field : Fields.allFields(type)) {
      Fields.set(instance, field, generate(field, null, child));
    }
    return instance;
  }

  private Object randomCollection(
      Type genericType, Class<?> type, FieldConstraint c, GenerationContext ctx) {
    int size = randomSize(c, ctx.random());
    return collectionOf(genericType, type, size, ctx);
  }

  Object collectionOf(Type genericType, Class<?> type, int size, GenerationContext ctx) {
    Type elementType = typeArgument(genericType, 0);
    Collection<Object> collection = newCollection(type);
    int attempts = 0;
    while (collection.size() < size && attempts < size * 5 + 10) {
      attempts++;
      Object element = generate(elementType, rawClass(elementType), null, ctx);
      if (element != null) {
        collection.add(element);
      }
    }
    return collection;
  }

  private Object randomMap(Type genericType, Class<?> type, FieldConstraint c,
      GenerationContext ctx) {
    int size = randomSize(c, ctx.random());
    Type keyType = typeArgument(genericType, 0);
    Type valueType = typeArgument(genericType, 1);
    Map<Object, Object> map = new LinkedHashMap<>();
    int attempts = 0;
    while (map.size() < size && attempts < size * 5 + 10) {
      attempts++;
      Object key = generate(keyType, rawClass(keyType), null, ctx);
      Object value = generate(valueType, rawClass(valueType), null, ctx);
      if (key != null) {
        map.put(key, value);
      }
    }
    return map;
  }

  private Object randomArray(Class<?> componentType, FieldConstraint c, GenerationContext ctx) {
    int size = randomSize(c, ctx.random());
    Object array = Array.newInstance(componentType, size);
    for (int i = 0; i < size; i++) {
      Array.set(array, i, generate(componentType, componentType, null, ctx));
    }
    return array;
  }

  private static Collection<Object> newCollection(Class<?> type) {
    if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
      if (SortedSet.class.isAssignableFrom(type)) {
        return new TreeSet<>();
      }
      if (Set.class.isAssignableFrom(type)) {
        return new LinkedHashSet<>();
      }
      return new ArrayList<>();
    }
    @SuppressWarnings("unchecked")
    Collection<Object> collection =
        (Collection<Object>) Instantiator.instantiate((Class<? extends Collection<?>>) type);
    return collection;
  }

  private static int randomSize(FieldConstraint c, Random random) {
    int min = c != null && c.getMinSize() != null ? c.getMinSize() : 0;
    int max = c != null && c.getMaxSize() != null ? c.getMaxSize() : 3;
    return min + (max > min ? random.nextInt(max - min + 1) : 0);
  }

  private static long randomLong(FieldConstraint c, Random random, long defMin, long defMax) {
    long min = c != null && c.getMin() != null ? (long) Math.ceil(c.getMin()) : defMin;
    long max = c != null && c.getMax() != null ? (long) Math.floor(c.getMax()) : defMax;
    if (max <= min) {
      return min;
    }
    return random.nextLong(min, max + 1);
  }

  private static double randomDouble(FieldConstraint c, Random random,
      double defMin, double defMax) {
    double min = c != null && c.getMin() != null ? c.getMin() : defMin;
    double max = c != null && c.getMax() != null ? c.getMax() : defMax;
    if (max <= min) {
      return min;
    }
    return min + random.nextDouble() * (max - min);
  }

  private static String randomString(FieldConstraint c, Random random) {
    String prefix = c != null && c.getPrefix() != null ? c.getPrefix() : "";
    int min = c != null && c.getMinLength() != null ? c.getMinLength() : 1;
    int max = c != null && c.getMaxLength() != null ? c.getMaxLength() : 16;
    min = Math.max(min, prefix.length());
    int length = min + (max > min ? random.nextInt(max - min + 1) : 0);
    StringBuilder builder = new StringBuilder(prefix);
    while (builder.length() < length) {
      builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
    }
    return builder.toString();
  }

  private static Type typeArgument(Type genericType, int index) {
    if (genericType instanceof ParameterizedType parameterized) {
      Type[] arguments = parameterized.getActualTypeArguments();
      if (index < arguments.length) {
        return arguments[index];
      }
    }
    return Object.class;
  }

  private static Class<?> rawClass(Type type) {
    if (type instanceof Class<?> clazz) {
      return clazz;
    }
    if (type instanceof ParameterizedType parameterized
        && parameterized.getRawType() instanceof Class<?> clazz) {
      return clazz;
    }
    return Object.class;
  }
}
