package com.gsb.testdata;

import java.lang.reflect.Constructor;

/** Instantiates classes through their no-arg constructor. */
final class Instantiator {

  private Instantiator() {
  }

  static <T> T instantiate(Class<T> type) {
    try {
      Constructor<T> constructor = type.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (NoSuchMethodException e) {
      throw new TestDataException(
          "Cannot construct " + type.getName() + ": a no-arg constructor is required. "
              + "Add one, or supply the value explicitly via TemplateBuilder.set(...)");
    } catch (ReflectiveOperationException e) {
      throw new TestDataException("Failed to instantiate " + type.getName(), e);
    }
  }
}
