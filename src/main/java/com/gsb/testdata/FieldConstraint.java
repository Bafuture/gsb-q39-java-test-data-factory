package com.gsb.testdata;

import java.util.Collection;

/**
 * Immutable per-field constraint. Coherence is already guaranteed by
 * {@link ConstraintBuilder}; this class additionally validates concrete values
 * (fixed values and boundary values) against the declared rules.
 */
public final class FieldConstraint {

  private final Double min;
  private final Double max;
  private final Integer minLength;
  private final Integer maxLength;
  private final String prefix;
  private final Integer minSize;
  private final Integer maxSize;
  private final boolean nullable;

  FieldConstraint(ConstraintBuilder builder) {
    this.min = builder.min;
    this.max = builder.max;
    this.minLength = builder.minLength;
    this.maxLength = builder.maxLength;
    this.prefix = builder.prefix;
    this.minSize = builder.minSize;
    this.maxSize = builder.maxSize;
    this.nullable = builder.nullable;
  }

  public Double getMin() {
    return min;
  }

  public Double getMax() {
    return max;
  }

  public Integer getMinLength() {
    return minLength;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public String getPrefix() {
    return prefix;
  }

  public Integer getMinSize() {
    return minSize;
  }

  public Integer getMaxSize() {
    return maxSize;
  }

  public boolean isNullable() {
    return nullable;
  }

  /**
   * Validates a concrete value, throwing {@link TemplateConfigurationException}
   * describing the first violation found.
   */
  void checkValue(Object value, String fieldName) {
    if (value == null) {
      if (!nullable) {
        throw new TemplateConfigurationException(
            "Field '" + fieldName + "' is declared non-nullable but the value is null");
      }
      return;
    }
    if (value instanceof Number number) {
      double v = number.doubleValue();
      if (min != null && v < min) {
        throw violation(fieldName, value, ">= " + min);
      }
      if (max != null && v > max) {
        throw violation(fieldName, value, "<= " + max);
      }
    }
    if (value instanceof String text) {
      if (minLength != null && text.length() < minLength) {
        throw violation(fieldName, value, "length >= " + minLength);
      }
      if (maxLength != null && text.length() > maxLength) {
        throw violation(fieldName, value, "length <= " + maxLength);
      }
      if (prefix != null && !text.startsWith(prefix)) {
        throw violation(fieldName, value, "prefix '" + prefix + "'");
      }
    }
    if (value instanceof Collection<?> collection) {
      if (minSize != null && collection.size() < minSize) {
        throw violation(fieldName, value, "size >= " + minSize);
      }
      if (maxSize != null && collection.size() > maxSize) {
        throw violation(fieldName, value, "size <= " + maxSize);
      }
    }
  }

  private static TemplateConfigurationException violation(
      String fieldName, Object value, String rule) {
    return new TemplateConfigurationException(
        "Fixed value for field '" + fieldName + "' violates constraint: expected "
            + rule + " but was " + value);
  }
}
