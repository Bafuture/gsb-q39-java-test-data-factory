package com.gsb.testdata;

import static org.assertj.core.api.Assertions.assertThat;

import com.gsb.testdata.fixtures.User;
import org.junit.jupiter.api.Test;

/** Unset fields are populated with legal default values. */
class DefaultFillTest {

  @Test
  void fillsEveryUnsetFieldWithDefaults() {
    Template<User> template = TestData.template(User.class)
        .set(User::setName, "Alice")
        .build();

    User user = template.create();

    assertThat(user.getName()).isEqualTo("Alice");
    assertThat(user.getEmail()).isNotBlank();
    assertThat(user.getTags()).isNotNull();
    assertThat(user.getScores()).isNotNull();
    assertThat(user.getAddress()).isNotNull();
    assertThat(user.getAddress().getCity()).isNotBlank();
    assertThat(user.getStatus()).isNotNull();
    assertThat(user.getBirthday()).isNotNull();
  }

  @Test
  void supportsFieldNamesInsteadOfMethodReferences() {
    Template<User> template = TestData.template(User.class)
        .set("name", "Bob")
        .set("age", 33)
        .build();

    User user = template.create();

    assertThat(user.getName()).isEqualTo("Bob");
    assertThat(user.getAge()).isEqualTo(33);
  }

  @Test
  void fixedValuesAreKeptAcrossInstances() {
    Template<User> template = TestData.template(User.class)
        .set(User::setEmail, "fixed@example.com")
        .build();

    assertThat(template.create(5))
        .allSatisfy(user -> assertThat(user.getEmail()).isEqualTo("fixed@example.com"));
  }
}
