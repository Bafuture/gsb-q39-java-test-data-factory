package com.gsb.testdata.gen;

import com.gsb.testdata.TestDataException;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Generates legal default values for any field type: primitives, strings,
 * enums, time types, collections, maps, optionals, arrays and nested POJOs.
 * Nesting is bounded by {@link GenContext#maxDepth()}.
 */
public final class ValueGenerator {

    private static final Map<Class<?>, Function<Random, ?>> CUSTOM_GENERATORS = new ConcurrentHashMap<>();
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";

    private ValueGenerator() {
    }

    /** Extension point: register a custom default generator for a type. */
    public static <T> void registerGenerator(Class<T> type, Function<Random, T> generator) {
        CUSTOM_GENERATORS.put(type, generator);
    }

    public static Object generate(Type type, GenContext ctx) {
        if (type instanceof Class<?> clazz) {
            return generateClass(clazz, ctx);
        }
        if (type instanceof ParameterizedType parameterized) {
            return generateParameterized(parameterized, ctx);
        }
        if (type instanceof GenericArrayType arrayType) {
            Type component = arrayType.getGenericComponentType();
            if (component instanceof Class<?> componentClass) {
                return generateArray(componentClass, ctx);
            }
            return null;
        }
        if (type instanceof WildcardType wildcard) {
            return generate(wildcard.getUpperBounds()[0], ctx);
        }
        return null;
    }

    public static <T> T instantiate(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new TestDataException(clazz.getName() + " needs a no-arg constructor to be generated", e);
        } catch (ReflectiveOperationException e) {
            throw new TestDataException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    public static String randomString(Random random, int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }

    private static Object generateClass(Class<?> clazz, GenContext ctx) {
        Function<Random, ?> custom = CUSTOM_GENERATORS.get(clazz);
        if (custom != null) {
            return custom.apply(ctx.random());
        }
        Random random = ctx.random();
        if (clazz == String.class) return randomString(random, 5 + random.nextInt(10));
        if (clazz == boolean.class || clazz == Boolean.class) return random.nextBoolean();
        if (clazz == int.class || clazz == Integer.class) return random.nextInt(10_000);
        if (clazz == long.class || clazz == Long.class) return random.nextLong(1_000_000);
        if (clazz == double.class || clazz == Double.class) return random.nextDouble() * 10_000;
        if (clazz == float.class || clazz == Float.class) return random.nextFloat() * 10_000;
        if (clazz == short.class || clazz == Short.class) return (short) random.nextInt(Short.MAX_VALUE);
        if (clazz == byte.class || clazz == Byte.class) return (byte) random.nextInt(Byte.MAX_VALUE);
        if (clazz == char.class || clazz == Character.class) return (char) ('a' + random.nextInt(26));
        if (clazz == BigInteger.class) return BigInteger.valueOf(random.nextLong(1_000_000));
        if (clazz == BigDecimal.class) return BigDecimal.valueOf(random.nextDouble() * 10_000).setScale(2, RoundingMode.HALF_UP);
        if (clazz == UUID.class) return new UUID(random.nextLong(), random.nextLong());
        if (clazz == OptionalInt.class) return OptionalInt.of(random.nextInt(10_000));
        if (clazz == OptionalLong.class) return OptionalLong.of(random.nextLong(1_000_000));
        if (clazz == OptionalDouble.class) return OptionalDouble.of(random.nextDouble() * 10_000);
        if (clazz.isEnum()) {
            Object[] constants = clazz.getEnumConstants();
            return constants.length == 0 ? null : constants[random.nextInt(constants.length)];
        }
        Object temporal = generateTemporal(clazz, random);
        if (temporal != null) {
            return temporal;
        }
        if (clazz.isArray()) {
            return generateArray(clazz.getComponentType(), ctx);
        }
        if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
            // raw collection/map without generic info: empty container
            return emptyContainer(clazz);
        }
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            return null;
        }
        if (ctx.depth() >= ctx.maxDepth()) {
            return null;
        }
        return instantiateAndFill(clazz, ctx);
    }

    private static Object generateTemporal(Class<?> clazz, Random random) {
        if (clazz == LocalDate.class) return LocalDate.ofEpochDay(random.nextLong(-365_000L, 365_000L));
        if (clazz == LocalDateTime.class) return LocalDateTime.ofEpochSecond(random.nextLong(-1_000_000_000L, 1_000_000_000L), random.nextInt(1_000_000_000), ZoneOffset.UTC);
        if (clazz == LocalTime.class) return LocalTime.of(random.nextInt(24), random.nextInt(60), random.nextInt(60));
        if (clazz == Instant.class) return Instant.ofEpochMilli(random.nextLong(-31_536_000_000L, 31_536_000_000L));
        if (clazz == ZonedDateTime.class) return ZonedDateTime.ofInstant(Instant.ofEpochMilli(random.nextLong(-31_536_000_000L, 31_536_000_000L)), ZoneId.systemDefault());
        if (clazz == OffsetDateTime.class) return OffsetDateTime.ofInstant(Instant.ofEpochMilli(random.nextLong(-31_536_000_000L, 31_536_000_000L)), ZoneId.systemDefault());
        if (clazz == Year.class) return Year.of(1970 + random.nextInt(100));
        if (clazz == YearMonth.class) return YearMonth.of(1970 + random.nextInt(100), 1 + random.nextInt(12));
        if (clazz == Duration.class) return Duration.ofSeconds(random.nextLong(1_000_000));
        if (clazz == Period.class) return Period.ofDays(random.nextInt(1000));
        if (clazz == Date.class) return new Date(random.nextLong(-31_536_000_000L, 31_536_000_000L));
        return null;
    }

    private static Object generateParameterized(ParameterizedType type, GenContext ctx) {
        Type rawType = type.getRawType();
        Type[] arguments = type.getActualTypeArguments();
        Random random = ctx.random();
        if (rawType == Optional.class) {
            return Optional.ofNullable(generate(arguments[0], ctx));
        }
        if (rawType instanceof Class<?> rawClass) {
            if (ctx.depth() >= ctx.maxDepth()) {
                return emptyContainer(rawClass);
            }
            if (List.class.isAssignableFrom(rawClass) || rawClass == Collection.class || rawClass == Iterable.class) {
                int size = random.nextInt(4);
                List<Object> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    list.add(generate(arguments[0], ctx));
                }
                return list;
            }
            if (Set.class.isAssignableFrom(rawClass)) {
                int size = random.nextInt(4);
                Set<Object> set = new LinkedHashSet<>();
                for (int i = 0; i < size; i++) {
                    set.add(generate(arguments[0], ctx));
                }
                return set;
            }
            if (Map.class.isAssignableFrom(rawClass)) {
                int size = random.nextInt(4);
                Map<Object, Object> map = new LinkedHashMap<>();
                for (int i = 0; i < size; i++) {
                    map.put(generate(arguments[0], ctx), generate(arguments[1], ctx));
                }
                return map;
            }
        }
        return null;
    }

    private static Object generateArray(Class<?> componentType, GenContext ctx) {
        if (ctx.depth() >= ctx.maxDepth()) {
            return Array.newInstance(componentType, 0);
        }
        int size = ctx.random().nextInt(3);
        Object array = Array.newInstance(componentType, size);
        for (int i = 0; i < size; i++) {
            Array.set(array, i, generate(componentType, ctx));
        }
        return array;
    }

    private static Object emptyContainer(Class<?> rawClass) {
        if (Set.class.isAssignableFrom(rawClass)) return new LinkedHashSet<>();
        if (Map.class.isAssignableFrom(rawClass)) return new LinkedHashMap<>();
        return new ArrayList<>();
    }

    private static Object instantiateAndFill(Class<?> clazz, GenContext ctx) {
        Object instance = instantiate(clazz);
        GenContext nested = ctx.nested();
        for (Field field : Fields.all(clazz)) {
            try {
                field.setAccessible(true);
                field.set(instance, generate(field.getGenericType(), nested));
            } catch (IllegalAccessException e) {
                throw new TestDataException("Cannot set field " + field.getName() + " on " + clazz.getName(), e);
            }
        }
        return instance;
    }
}
