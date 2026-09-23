package com.gsb.testdata.gen;

import com.gsb.testdata.TestDataException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

/** Numeric conversion and in-range generation shared by constraints and boundary values. */
public final class Numbers {

    private Numbers() {
    }

    /** @param wrappedType a non-primitive numeric wrapper (use {@link Fields#wrap} first) */
    public static Object convert(Class<?> wrappedType, BigDecimal value) {
        if (wrappedType == Integer.class) return value.intValue();
        if (wrappedType == Long.class) return value.longValue();
        if (wrappedType == Double.class) return value.doubleValue();
        if (wrappedType == Float.class) return value.floatValue();
        if (wrappedType == Short.class) return value.shortValue();
        if (wrappedType == Byte.class) return value.byteValue();
        if (wrappedType == BigDecimal.class) return value;
        if (wrappedType == BigInteger.class) return value.toBigInteger();
        throw new TestDataException("Not a numeric type: " + wrappedType.getName());
    }

    public static Object randomInRange(Class<?> wrappedType, BigDecimal min, BigDecimal max, Random random) {
        if (wrappedType == Double.class || wrappedType == Float.class || wrappedType == BigDecimal.class) {
            BigDecimal fraction = BigDecimal.valueOf(random.nextDouble());
            return convert(wrappedType, min.add(fraction.multiply(max.subtract(min))));
        }
        double lo = min.doubleValue();
        double hi = max.doubleValue();
        double sample = lo + Math.floor(random.nextDouble() * (hi - lo + 1.0));
        if (sample > hi) {
            sample = hi;
        }
        return convert(wrappedType, BigDecimal.valueOf((long) sample));
    }
}
