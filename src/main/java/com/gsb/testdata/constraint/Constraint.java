package com.gsb.testdata.constraint;

import com.gsb.testdata.gen.FieldInfo;
import com.gsb.testdata.gen.GenContext;

import java.util.List;

/**
 * A field constraint. Implementations must validate their own configuration in
 * their constructor (fail fast at template-definition time) and know how to
 * produce compliant values plus boundary values for parameterized tests.
 */
public interface Constraint {

    /** Whether this constraint can be applied to a field of the given type. */
    boolean supports(Class<?> fieldType);

    /** Produce a value satisfying this constraint. */
    Object generate(FieldInfo field, GenContext ctx);

    /** Boundary values (min, max, null, critical lengths/sizes) for parameterized tests. */
    List<Object> boundaryValues(FieldInfo field, GenContext ctx);
}
