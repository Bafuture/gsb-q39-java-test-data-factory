package com.gsb.testdata;

/**
 * Entry point of the test-data factory.
 *
 * <pre>{@code
 * Template<User> users = TestData.template(User.class, 42L)
 *     .set(User::setName, "Alice")
 *     .constrain(User::setAge, c -> c.range(18, 60))
 *     .build();
 * User user = users.create();
 * }</pre>
 */
public final class TestData {

  private TestData() {
  }

  /** Starts a template definition with non-reproducible (random) data. */
  public static <T> TemplateBuilder<T> template(Class<T> type) {
    return new TemplateBuilder<>(type, null);
  }

  /** Starts a template definition pinned to a reproducible random seed. */
  public static <T> TemplateBuilder<T> template(Class<T> type, long seed) {
    return new TemplateBuilder<>(type, seed);
  }
}
