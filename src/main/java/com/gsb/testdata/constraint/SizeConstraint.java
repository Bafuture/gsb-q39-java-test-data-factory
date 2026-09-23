package com.gsb.testdata.constraint;

import com.gsb.testdata.InvalidConstraintException;
import com.gsb.testdata.TestDataException;
import com.gsb.testdata.gen.FieldInfo;
import com.gsb.testdata.gen.GenContext;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Collection/map/array size constraint: min &lt;= size &lt;= max. */
public final class SizeConstraint implements Constraint {

    private final int min;
    private final int max;

    public SizeConstraint(int min, int max) {
        if (min < 0) {
            throw new InvalidConstraintException("min size must be >= 0, got " + min);
        }
        if (min > max) {
            throw new InvalidConstraintException("min size " + min + " must not be greater than max size " + max);
        }
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean supports(Class<?> fieldType) {
        return fieldType.isArray()
                || Collection.class.isAssignableFrom(fieldType)
                || Map.class.isAssignableFrom(fieldType);
    }

    @Override
    public Object generate(FieldInfo field, GenContext ctx) {
        int size = min + ctx.random().nextInt(max - min + 1);
        return buildContainer(field, size, ctx);
    }

    @Override
    public List<Object> boundaryValues(FieldInfo field, GenContext ctx) {
        Set<Integer> sizes = new LinkedHashSet<>();
        sizes.add(min);
        sizes.add(max);
        if (min - 1 >= 0) {
            sizes.add(min - 1);
        }
        sizes.add(max + 1);
        sizes.add(0);
        List<Object> values = new ArrayList<>();
        for (int size : sizes) {
            values.add(buildContainer(field, size, ctx));
        }
        values.add(null);
        return values;
    }

    private Object buildContainer(FieldInfo field, int size, GenContext ctx) {
        Class<?> type = field.type();
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            Object array = Array.newInstance(componentType, size);
            for (int i = 0; i < size; i++) {
                Array.set(array, i, ctx.generate(componentType));
            }
            return array;
        }
        if (Map.class.isAssignableFrom(type)) {
            Type keyType = typeArgument(field.genericType(), 0);
            Type valueType = typeArgument(field.genericType(), 1);
            Map<Object, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < size; i++) {
                map.put(ctx.generate(keyType), ctx.generate(valueType));
            }
            return map;
        }
        Type elementType = typeArgument(field.genericType(), 0);
        Collection<Object> collection = Set.class.isAssignableFrom(type) ? new LinkedHashSet<>() : new ArrayList<>();
        for (int i = 0; i < size; i++) {
            collection.add(ctx.generate(elementType));
        }
        return collection;
    }

    private static Type typeArgument(Type genericType, int index) {
        if (genericType instanceof ParameterizedType parameterized) {
            Type[] arguments = parameterized.getActualTypeArguments();
            if (index < arguments.length) {
                return arguments[index];
            }
        }
        // raw types: fall back to strings so containers still get legal elements
        return String.class;
    }
}
