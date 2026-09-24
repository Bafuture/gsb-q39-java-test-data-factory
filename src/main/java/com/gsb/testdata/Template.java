package com.gsb.testdata;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An immutable, reusable object template. Instances are created through
 * {@link TestData#template(Class)} / {@link TestData#template(Class, long)}.
 *
 * <p>When a seed was supplied, every sequence of {@code create(...)} calls
 * produces exactly the same objects on every run. Without a seed each run
 * produces different data.
 */
public final class Template<T> {

  private final Class<T> type;
  private final Map<String, FieldSpec> specs;
  private final Long seed;
  private final int maxDepth;
  private final Map<Class<?>, Template<?>> nestedTemplates;
  private final AtomicLong invocationCounter = new AtomicLong();
  private final ValueGenerator generator = new ValueGenerator();

  Template(Class<T> type, Map<String, FieldSpec> specs, Long seed, int maxDepth,
      Map<Class<?>, Template<?>> nestedTemplates) {
    this.type = type;
    this.specs = Map.copyOf(specs);
    this.seed = seed;
    this.maxDepth = maxDepth;
    this.nestedTemplates = Map.copyOf(nestedTemplates);
  }

  public Class<T> type() {
    return type;
  }

  /** Generates a single instance. */
  public T create() {
    Random random = seed != null
        ? new Random(seed + invocationCounter.getAndIncrement())
        : new Random();
    return createWith(new GenerationContext(random, 0, maxDepth, nestedTemplates));
  }

  /** Generates {@code count} instances as a list. */
  public List<T> create(int count) {
    if (count < 0) {
      throw new IllegalArgumentException("count must be >= 0 but was " + count);
    }
    List<T> result = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      result.add(create());
    }
    return result;
  }

  /**
   * Generates a set of boundary-value instances for parameterized tests:
   * min/max of numeric ranges, null and critical lengths for strings, empty and
   * max-size collections. The first element is always a regular instance.
   */
  public List<T> boundaryValues() {
    List<T> result = new ArrayList<>();
    result.add(create());
    for (Field field : Fields.allFields(type)) {
      FieldSpec spec = specs.get(field.getName());
      if (spec != null && spec.hasFixedValue) {
        continue;
      }
      FieldConstraint constraint = spec != null ? spec.constraint : null;
      for (Object boundary : BoundaryValues.forField(field, constraint, this)) {
        T instance = create();
        Fields.set(instance, field, boundary);
        result.add(instance);
      }
    }
    return result;
  }

  /** Returns a copy of this template pinned to the given seed. */
  public Template<T> withSeed(long newSeed) {
    return new Template<>(type, specs, newSeed, maxDepth, nestedTemplates);
  }

  T createWith(GenerationContext ctx) {
    T instance = Instantiator.instantiate(type);
    for (Field field : Fields.allFields(type)) {
      FieldSpec spec = specs.get(field.getName());
      Object value = spec != null && spec.hasFixedValue
          ? spec.fixedValue
          : generator.generate(field, spec != null ? spec.constraint : null, ctx);
      Fields.set(instance, field, value);
    }
    return instance;
  }

  GenerationContext newContext() {
    Random random = seed != null
        ? new Random(seed + invocationCounter.getAndIncrement())
        : new Random();
    return new GenerationContext(random, 0, maxDepth, nestedTemplates);
  }

  ValueGenerator generator() {
    return generator;
  }
}
