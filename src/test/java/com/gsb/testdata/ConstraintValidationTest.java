package com.gsb.testdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gsb.testdata.fixtures.User;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Generated values satisfy declared constraints; bad config fails at build time. */
class ConstraintValidationTest {

  @Test
  void generatedValuesSatisfyConstraints() {
    Template<User> template = TestData.template(User.class)
        .constrain(User::setAge, c -> c.range(18, 60))
        .constrain(User::setName, c -> c.length(5, 10).prefix("u-"))
        .constrain(User::setTags, c -> c.size(1, 3))
        .build();

    List<User> users = template.create(50);

    assertThat(users).allSatisfy(user -> {
      assertThat(user.getAge()).isBetween(18, 60);
      assertThat(user.getName()).startsWith("u-").hasSizeBetween(5, 10);
      assertThat(user.getTags()).hasSizeBetween(1, 3);
    });
  }

  @Test
  void invertedNumericRangeFailsImmediately() {
    assertThatThrownBy(() -> TestData.template(User.class)
        .constrain(User::setAge, c -> c.range(60, 18)))
        .isInstanceOf(TemplateConfigurationException.class)
        .hasMessageContaining("range");
  }

  @Test
  void invertedStringLengthFailsImmediately() {
    assertThatThrownBy(() -> TestData.template(User.class)
        .constrain(User::setName, c -> c.length(10, 5)))
        .isInstanceOf(TemplateConfigurationException.class);
  }

  @Test
  void prefixLongerThanMaxLengthFailsAtBuild() {
    assertThatThrownBy(() -> TestData.template(User.class)
        .constrain(User::setName, c -> c.length(1, 3).prefix("too-long-prefix")))
        .isInstanceOf(TemplateConfigurationException.class)
        .hasMessageContaining("Prefix");
  }

  @Test
  void fixedValueViolatingConstraintFailsAtBuildTime() {
    assertThatThrownBy(() -> TestData.template(User.class)
        .set(User::setAge, 10)
        .constrain(User::setAge, c -> c.range(18, 60))
        .build())
        .isInstanceOf(TemplateConfigurationException.class)
        .hasMessageContaining("age");
  }

  @Test
  void constraintDeclaredBeforeFixedValueIsAlsoEnforced() {
    assertThatThrownBy(() -> TestData.template(User.class)
        .constrain(User::setAge, c -> c.range(18, 60))
        .set(User::setAge, 10))
        .isInstanceOf(TemplateConfigurationException.class);
  }

  @Test
  void unknownFieldNameFailsImmediately() {
    assertThatThrownBy(() -> TestData.template(User.class).set("nope", 1))
        .isInstanceOf(TemplateConfigurationException.class)
        .hasMessageContaining("nope");
  }

  @Test
  void incompatibleFixedValueTypeFailsImmediately() {
    assertThatThrownBy(() -> TestData.template(User.class).set("age", "not-a-number"))
        .isInstanceOf(TemplateConfigurationException.class);
  }
}
