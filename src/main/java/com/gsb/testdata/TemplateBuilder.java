package com.gsb.testdata;

import com.gsb.testdata.constraint.Constraint;
import com.gsb.testdata.gen.Fields;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Fluent builder for {@link Template}. All configuration mistakes (unknown
 * fields, incompatible values, inconsistent constraints) are reported here,
 * at template-definition time, never during generation.
 */
public final class TemplateBuilder<T> {

    private final Class<T> type;
    private final Map<String, Object> explicitValues = new LinkedHashMap<>();
    private final Map<String, Constraint> constraints = new LinkedHashMap<>();
    private Long seed;
    private int maxDepth = Template.DEFAULT_MAX_DEPTH;

    TemplateBuilder(Class<T> type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    /** Pin a field to an explicit value, addressed by name. */
    public TemplateBuilder<T> set(String fieldName, Object value) {
        Field field = Fields.find(type, fieldName);
        checkAssignable(field, value);
        explicitValues.put(fieldName, value);
        return this;
    }

    /** Pin a field via getter reference, e.g. {@code set(User::getName, "Alice")}. */
    public <V> TemplateBuilder<T> set(FieldRef<T, V> ref, V value) {
        return set(FieldNames.fromLambda(ref), value);
    }

    /** Pin a field via setter reference, e.g. {@code set(User::setName, "Alice")}. */
    public <V> TemplateBuilder<T> set(SetterRef<T, V> ref, V value) {
        return set(FieldNames.fromLambda(ref), value);
    }

    /** Declare a constraint for a field, addressed by name. */
    public TemplateBuilder<T> constrain(String fieldName, Constraint constraint) {
        Objects.requireNonNull(constraint, "constraint");
        Field field = Fields.find(type, fieldName);
        if (!constraint.supports(field.getType())) {
            throw new InvalidConstraintException(
                    constraint.getClass().getSimpleName() + " does not support field '" + fieldName
                            + "' of type " + field.getType().getSimpleName() + " on " + type.getSimpleName());
        }
        constraints.put(fieldName, constraint);
        return this;
    }

    /** Declare a constraint via getter reference. */
    public <V> TemplateBuilder<T> constrain(FieldRef<T, V> ref, Constraint constraint) {
        return constrain(FieldNames.fromLambda(ref), constraint);
    }

    /** Fix the random seed: every generation run then produces identical data. */
    public TemplateBuilder<T> seed(long seed) {
        this.seed = seed;
        return this;
    }

    /** Maximum nesting depth for auto-constructed objects (default {@value Template#DEFAULT_MAX_DEPTH}). */
    public TemplateBuilder<T> maxDepth(int maxDepth) {
        if (maxDepth < 1) {
            throw new TestDataException("maxDepth must be >= 1, got " + maxDepth);
        }
        this.maxDepth = maxDepth;
        return this;
    }

    public Template<T> build() {
        Random random = seed != null ? new Random(seed) : new Random();
        return new Template<>(type, Map.copyOf(explicitValues), Map.copyOf(constraints), random, maxDepth);
    }

    private static void checkAssignable(Field field, Object value) {
        if (value == null) {
            if (field.getType().isPrimitive()) {
                throw new TestDataException("Cannot set null on primitive field '" + field.getName() + "'");
            }
            return;
        }
        Class<?> wrapped = Fields.wrap(field.getType());
        if (!wrapped.isInstance(value)) {
            throw new TestDataException("Value of type " + value.getClass().getSimpleName()
                    + " is not assignable to field '" + field.getName() + "' of type "
                    + field.getType().getSimpleName());
        }
    }
}
