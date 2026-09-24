package com.gsb.testdata;

/**
 * Fluent declaration of a {@link FieldConstraint}. Every method validates its
 * arguments immediately, so incoherent configuration fails while the template
 * is being defined instead of at generation time.
 */
public final class ConstraintBuilder {

  Double min;
  Double max;
  Integer minLength;
  Integer maxLength;
  String prefix;
  Integer minSize;
  Integer maxSize;
  boolean nullable = true;

  /** Numeric range (inclusive) for integral and floating-point fields. */
  public ConstraintBuilder range(double min, double max) {
    if (min > max) {
      throw new TemplateConfigurationException(
          "Invalid numeric range: min " + min + " > max " + max);
    }
    this.min = min;
    this.max = max;
    return this;
  }

  /** Allowed string length range (inclusive). */
  public ConstraintBuilder length(int minLength, int maxLength) {
    if (minLength < 0) {
      throw new TemplateConfigurationException(
          "Invalid string length: minLength must be >= 0 but was " + minLength);
    }
    if (minLength > maxLength) {
      throw new TemplateConfigurationException(
          "Invalid string length: minLength " + minLength + " > maxLength " + maxLength);
    }
    this.minLength = minLength;
    this.maxLength = maxLength;
    return this;
  }

  /** Required prefix for generated strings. */
  public ConstraintBuilder prefix(String prefix) {
    if (prefix == null) {
      throw new TemplateConfigurationException("Prefix must not be null");
    }
    this.prefix = prefix;
    return this;
  }

  /** Allowed collection/map/array size range (inclusive). */
  public ConstraintBuilder size(int minSize, int maxSize) {
    if (minSize < 0) {
      throw new TemplateConfigurationException(
          "Invalid collection size: minSize must be >= 0 but was " + minSize);
    }
    if (minSize > maxSize) {
      throw new TemplateConfigurationException(
          "Invalid collection size: minSize " + minSize + " > maxSize " + maxSize);
    }
    this.minSize = minSize;
    this.maxSize = maxSize;
    return this;
  }

  /** Whether {@code null} is an allowed boundary value for this field. */
  public ConstraintBuilder nullable(boolean nullable) {
    this.nullable = nullable;
    return this;
  }

  FieldConstraint build() {
    if (prefix != null && maxLength != null && prefix.length() > maxLength) {
      throw new TemplateConfigurationException(
          "Prefix '" + prefix + "' (length " + prefix.length()
              + ") is longer than maxLength " + maxLength);
    }
    return new FieldConstraint(this);
  }
}
