package com.gsb.testdata.constraint;

import com.gsb.testdata.InvalidConstraintException;
import com.gsb.testdata.gen.FieldInfo;
import com.gsb.testdata.gen.GenContext;
import com.gsb.testdata.gen.ValueGenerator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** String constraint: length within [minLength, maxLength] and a mandatory prefix. */
public final class StringConstraint implements Constraint {

    private final int minLength;
    private final int maxLength;
    private final String prefix;

    public StringConstraint(int minLength, int maxLength, String prefix) {
        if (minLength < 0) {
            throw new InvalidConstraintException("minLength must be >= 0, got " + minLength);
        }
        if (minLength > maxLength) {
            throw new InvalidConstraintException("minLength " + minLength + " must not be greater than maxLength " + maxLength);
        }
        String effectivePrefix = prefix == null ? "" : prefix;
        if (effectivePrefix.length() > maxLength) {
            throw new InvalidConstraintException("prefix '" + effectivePrefix + "' is longer than maxLength " + maxLength);
        }
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.prefix = effectivePrefix;
    }

    @Override
    public boolean supports(Class<?> fieldType) {
        return CharSequence.class.isAssignableFrom(fieldType);
    }

    @Override
    public Object generate(FieldInfo field, GenContext ctx) {
        Random random = ctx.random();
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        return prefix + ValueGenerator.randomString(random, length - prefix.length());
    }

    @Override
    public List<Object> boundaryValues(FieldInfo field, GenContext ctx) {
        Random random = ctx.random();
        Set<Object> values = new LinkedHashSet<>();
        values.add(padTo(minLength, random));
        values.add(padTo(maxLength, random));
        if (minLength - 1 >= 0) {
            values.add(ValueGenerator.randomString(random, minLength - 1));
        }
        values.add(ValueGenerator.randomString(random, maxLength + 1));
        values.add("");
        values.add(null);
        return new ArrayList<>(values);
    }

    private String padTo(int length, Random random) {
        return prefix + ValueGenerator.randomString(random, Math.max(0, length - prefix.length()));
    }
}
