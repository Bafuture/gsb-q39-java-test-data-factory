package com.gsb.testdata;

import static org.assertj.core.api.Assertions.assertThat;

import com.gsb.testdata.fixtures.User;
import com.gsb.testdata.junit.Fixture;
import com.gsb.testdata.junit.TestDataExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** JUnit 5 parameter injection via {@link TestDataExtension}. */
@ExtendWith(TestDataExtension.class)
class JunitExtensionTest {

  static Template<User> userTemplate = TestData.template(User.class)
      .set(User::setName, "Injected")
      .build();

  @Test
  void injectsAutoConstructedObject(@Fixture User user) {
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isNotBlank();
    assertThat(user.getAddress()).isNotNull();
  }

  @Test
  void injectsFromNamedTemplate(@Fixture("userTemplate") User user) {
    assertThat(user.getName()).isEqualTo("Injected");
  }

  @Test
  void seededInjectionIsReproducible(
      @Fixture(value = "userTemplate", seed = 123L) User first,
      @Fixture(value = "userTemplate", seed = 123L) User second) {
    assertThat(first)
        .usingRecursiveComparison()
        .isEqualTo(second);
  }
}
