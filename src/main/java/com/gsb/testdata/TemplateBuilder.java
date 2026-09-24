package com.gsb.testdata;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Collects fixed values, constraints and nested templates for one type.
 * All configuration mistakes (unknown fields, incoherent constraints, fixed
 * values violating constraints) are reported immediately, at template
 * definition time.
 */
public final class TemplateBuilder<T> {

  private final Class<T> type;
  private final Map<String, FieldSpec> specs = new LinkedHashMap<>();
  private final Map<Class<?>, Template<?>> nestedTemplates = new LinkedHashMap<>();
  private Long seed;
  private int maxDepth = 3;

  TemplateBuilder(Class<T> type, Long seed) {
    this.type = type;
    this.seed = seed;
  }

  /** Pins a field to a fixed value, referenced by a setter method reference. */
  public <V> TemplateBuilder<T> set(FieldRef<T, V> ref, V value) {
    return set(FieldRefs.propertyName(ref), value);
  }

  /** Pins a field to a fixed value, referenced by field name. */
  public TemplateBuilder<T> set(String fieldName, Object value) {
    Field field = Fields.find(type, fieldName);
    if (value != null && !Fields.wrap(field.getType()).isInstance(value)) {
      throw new TemplateConfigurationException(
          "Value " + value + " (" + value.getClass().getSimpleName()
              + ") is not assignable to field '" + fieldName + "' of type "
              + field.getType().getSimpleName());
    }
    FieldSpec spec = spec(fieldName);
    if (spec.constraint != null) {
      spec.constraint.checkValue(value, fieldName);
    }
    spec.fixedValue = value;
    spec.hasFixedValue = true;
    return this;
  }

  /** Declares a constraint for a field, referenced by a setter method reference. */
  public <V> TemplateBuilder<T> constrain(FieldRef<T, V> ref,
      Consumer<ConstraintBuilder> declaration) {
    return constrain(FieldRefs.propertyName(ref), declaration);
  }

  /** Declares a constraint for a field, referenced by field name. */
  public TemplateBuilder<T> constrain(String fieldName,
      Consumer<ConstraintBuilder> declaration) {
    Fields.find(type, fieldName);
    ConstraintBuilder builder = new ConstraintBuilder();
    declaration.accept(builder);
    FieldConstraint constraint = builder.build();
    FieldSpec spec = spec(fieldName);
    if (spec.hasFixedValue) {
      constraint.checkValue(spec.fixedValue, fieldName);
    }
    spec.constraint = constraint;
    return this;
  }

  /** Registers a template to use when generating nested objects of its type. */
  public TemplateBuilder<T> use(Template<?> nestedTemplate) {
    nestedTemplates.put(nestedTemplate.type(), nestedTemplate);
    return this;
  }

  /** Maximum depth for auto-constructed nested objects (default 3). */
  public TemplateBuilder<T> maxDepth(int maxDepth) {
    if (maxDepth < 1) {
      throw new TemplateConfigurationException(
          "maxDepth must be >= 1 but was " + maxDepth);
    }
    this.maxDepth = maxDepth;
    return this;
  }

  /** Pins the random seed, making every generated object reproducible. */
  public TemplateBuilder<T> seed(long seed) {
    this.seed = seed;
    return this;
  }

  /** Validates the whole configuration and builds the immutable template. */
  public Template<T> build() {
    specs.forEach((fieldName, spec) -> {
      if (spec.hasFixedValue && spec.constraint != null) {
        spec.constraint.checkValue(spec.fixedValue, fieldName);
      }
    });
    return new Template<>(type, specs, seed, maxDepth, nestedTemplates);
  }

  private FieldSpec spec(String fieldName) {
    return specs.computeIfAbsent(fieldName, name -> new FieldSpec());
  }
}
