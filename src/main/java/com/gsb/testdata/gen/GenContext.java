package com.gsb.testdata.gen;

import java.lang.reflect.Type;
import java.util.Random;

/** Carries the shared random source and the current nesting depth. */
public final class GenContext {

    private final Random random;
    private final int depth;
    private final int maxDepth;

    public GenContext(Random random, int depth, int maxDepth) {
        this.random = random;
        this.depth = depth;
        this.maxDepth = maxDepth;
    }

    public Random random() {
        return random;
    }

    public int depth() {
        return depth;
    }

    public int maxDepth() {
        return maxDepth;
    }

    public GenContext nested() {
        return new GenContext(random, depth + 1, maxDepth);
    }

    public Object generate(Type type) {
        return ValueGenerator.generate(type, this);
    }
}
