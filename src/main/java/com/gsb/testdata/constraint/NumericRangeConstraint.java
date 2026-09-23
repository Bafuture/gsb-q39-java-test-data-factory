package com.gsb.testdata.constraint;

import com.gsb.testdata.InvalidConstraintException;
import com.gsb.testdata.gen.FieldInfo;
import com.gsb.testdata.gen.Fields;
import com.gsb.testdata.gen.GenContext;
import com.gsb.testdata.gen.Numbers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Numeric range constraint: min &lt;= value &lt;= max. */
public final class NumericRangeConstraint implements Constraint {

    private final BigDecimal min;
    private final BigDecimal max;

    public NumericRangeConstraint(Number min, Number max) {
        if (min == null || max == null) {
            throw new InvalidConstraintException("range bounds must not be null");
        }
        this.min = toBigDecimal(min);
        this.max = toBigDecimal(max);
        if (this.min.compareTo(this.max) > 0) {
            throw new InvalidConstraintException("range min " + min + " must not be greater than max " + max);
        }
    }

    @Override
    public boolean supports(Class<?> fieldType) {
        return Number.class.isAssignableFrom(Fields.wrap(fieldType));
    }

    @Override
    public Object generate(FieldInfo field, GenContext ctx) {
        return Numbers.randomInRange(Fields.wrap(field.type()), min, max, ctx.random());
    }

    @Override
    public List<Object> boundaryValues(FieldInfo field, GenContext ctx) {
        Class<?> wrapped = Fields.wrap(field.type());
        List<Object> values = new ArrayList<>();
        values.add(Numbers.convert(wrapped, min));
        values.add(Numbers.convert(wrapped, max));
        values.add(Numbers.convert(wrapped, min.subtract(BigDecimal.ONE)));
        values.add(Numbers.convert(wrapped, max.add(BigDecimal.ONE)));
        if (!field.type().isPrimitive()) {
            values.add(null);
        }
        return values;
    }

    private static BigDecimal toBigDecimal(Number number) {
        if (number instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (number instanceof Double || number instanceof Float) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.valueOf(number.longValue());
    }
}
