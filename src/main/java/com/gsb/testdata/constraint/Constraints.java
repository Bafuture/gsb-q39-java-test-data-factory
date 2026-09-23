package com.gsb.testdata.constraint;

/** Factory methods for the built-in constraints. */
public final class Constraints {

    private Constraints() {
    }

    /** Numeric range, inclusive: {@code range(18, 60)}. */
    public static Constraint range(Number min, Number max) {
        return new NumericRangeConstraint(min, max);
    }

    /** String length within [min, max]. */
    public static Constraint length(int min, int max) {
        return new StringConstraint(min, max, "");
    }

    /** String must start with the given prefix (default length: prefix length .. prefix length + 10). */
    public static Constraint prefix(String prefix) {
        if (prefix == null) {
            throw new com.gsb.testdata.InvalidConstraintException("prefix must not be null");
        }
        return new StringConstraint(prefix.length(), prefix.length() + 10, prefix);
    }

    /** Fluent string constraint: {@code text().minLength(3).maxLength(8).prefix("u").build()}. */
    public static StringConstraintBuilder text() {
        return new StringConstraintBuilder();
    }

    /** Collection/map/array size within [min, max]. */
    public static Constraint size(int min, int max) {
        return new SizeConstraint(min, max);
    }

    public static final class StringConstraintBuilder {
        private int minLength = 0;
        private int maxLength = 16;
        private String prefix = "";

        private StringConstraintBuilder() {
        }

        public StringConstraintBuilder minLength(int minLength) {
            this.minLength = minLength;
            return this;
        }

        public StringConstraintBuilder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public StringConstraintBuilder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Constraint build() {
            return new StringConstraint(minLength, maxLength, prefix);
        }
    }
}
