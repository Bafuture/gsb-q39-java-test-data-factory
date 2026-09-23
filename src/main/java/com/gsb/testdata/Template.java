package com.gsb.testdata;

import com.gsb.testdata.constraint.Constraint;
import com.gsb.testdata.gen.FieldInfo;
import com.gsb.testdata.gen.Fields;
import com.gsb.testdata.gen.GenContext;
import com.gsb.testdata.gen.Numbers;
import com.gsb.testdata.gen.ValueGenerator;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A built, ready-to-use object template. Explicit values win over constraints,
 * constraints win over defaults. All randomness flows through one seeded
 * {@link Random}, so a seeded template is fully reproducible.
 */
public final class Template<T> {

    /** Default protection against unbounded nesting (e.g. self-referencing types). */
    public static final int DEFAULT_MAX_DEPTH = 4;

    private final Class<T> type;
    private final Map<String, Object> explicitValues;
    private final Map<String, Constraint> constraints;
    private final Random random;
    private final int maxDepth;

    Template(Class<T> type,
             Map<String, Object> explicitValues,
             Map<String, Constraint> constraints,
             Random random,
             int maxDepth) {
        this.type = type;
        this.explicitValues = explicitValues;
        this.constraints = constraints;
        this.random = random;
        this.maxDepth = maxDepth;
    }

    /** Create one instance. */
    public T create() {
        T instance = ValueGenerator.instantiate(type);
        GenContext fieldContext = new GenContext(random, 1, maxDepth);
        for (Field field : Fields.all(type)) {
            try {
                field.setAccessible(true);
                field.set(instance, valueFor(field, fieldContext));
            } catch (IllegalAccessException e) {
                throw new TestDataException("Cannot set field " + field.getName() + " on " + type.getName(), e);
            }
        }
        return instance;
    }

    /** Create {@code count} instances in one go. */
    public List<T> createList(int count) {
        if (count < 0) {
            throw new TestDataException("count must be >= 0, got " + count);
        }
        List<T> instances = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            instances.add(create());
        }
        return instances;
    }

    /**
     * Instances where the given field takes boundary values (min, max, just
     * outside the range, empty/null, critical lengths/sizes). All other fields
     * are filled as usual. Ideal for parameterized tests.
     */
    public List<T> boundaryValues(String fieldName) {
        Field field = Fields.find(type, fieldName);
        FieldInfo info = FieldInfo.of(field);
        GenContext ctx = new GenContext(random, 1, maxDepth);
        Constraint constraint = constraints.get(fieldName);
        List<Object> values = constraint != null
                ? constraint.boundaryValues(info, ctx)
                : defaultBoundaries(info);
        List<T> instances = new ArrayList<>(values.size());
        for (Object value : values) {
            if (value == null && field.getType().isPrimitive()) {
                continue;
            }
            T instance = create();
            try {
                field.setAccessible(true);
                field.set(instance, value);
            } catch (IllegalAccessException e) {
                throw new TestDataException("Cannot set field " + fieldName + " on " + type.getName(), e);
            }
            instances.add(instance);
        }
        return instances;
    }

    private Object valueFor(Field field, GenContext ctx) {
        if (explicitValues.containsKey(field.getName())) {
            return explicitValues.get(field.getName());
        }
        Constraint constraint = constraints.get(field.getName());
        if (constraint != null) {
            return constraint.generate(FieldInfo.of(field), ctx);
        }
        return ValueGenerator.generate(field.getGenericType(), ctx);
    }

    private static List<Object> defaultBoundaries(FieldInfo info) {
        Class<?> wrapped = Fields.wrap(info.type());
        List<Object> values = new ArrayList<>();
        if (Number.class.isAssignableFrom(wrapped)) {
            values.add(Numbers.convert(wrapped, BigDecimal.ZERO));
            values.add(Numbers.convert(wrapped, BigDecimal.ONE));
            values.add(Numbers.convert(wrapped, BigDecimal.ONE.negate()));
        } else if (wrapped == Boolean.class) {
            values.add(Boolean.TRUE);
            values.add(Boolean.FALSE);
        } else if (CharSequence.class.isAssignableFrom(wrapped)) {
            values.add("");
            values.add("a");
        } else if (info.type().isArray()) {
            values.add(java.lang.reflect.Array.newInstance(info.type().getComponentType(), 0));
        } else if (Collection.class.isAssignableFrom(wrapped)) {
            values.add(new ArrayList<>());
        } else if (Map.class.isAssignableFrom(wrapped)) {
            values.add(new java.util.LinkedHashMap<>());
        }
        if (!info.type().isPrimitive()) {
            values.add(null);
        }
        return values;
    }
}
