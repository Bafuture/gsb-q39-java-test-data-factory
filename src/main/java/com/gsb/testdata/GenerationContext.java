package com.gsb.testdata;

import java.util.Map;
import java.util.Random;

/** Per-generation state: the seeded random, nesting depth and template registry. */
record GenerationContext(
    Random random, int depth, int maxDepth, Map<Class<?>, Template<?>> templates) {

  GenerationContext descend() {
    return new GenerationContext(random, depth + 1, maxDepth, templates);
  }
}
