package com.gsb.testdata;

import com.gsb.testdata.gen.ValueGenerator;

import java.util.Random;
import java.util.function.Function;

/** Entry point of the test-data library. */
public final class DataFactory {

    private DataFactory() {
    }

    /** Start defining a template for the given type. */
    public static <T> TemplateBuilder<T> template(Class<T> type) {
        return new TemplateBuilder<>(type);
    }

    /** Extension point: register a custom default generator for a type. */
    public static <T> void registerGenerator(Class<T> type, Function<Random, T> generator) {
        ValueGenerator.registerGenerator(type, generator);
    }
}
