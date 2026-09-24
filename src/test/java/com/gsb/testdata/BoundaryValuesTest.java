package com.gsb.testdata;

import static org.assertj.core.api.Assertions.assertThat;

import com.gsb.testdata.fixtures.User;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Boundary-value sets for parameterized tests. */
class BoundaryValuesTest {

  private final Template<User> template = TestData.template(User.class)
      .constrain(User::setAge, c -> c.range(18, 60))
      .constrain(User::setName, c -> c.length(3, 8))
      .constrain(User::setTags, c -> c.size(0, 2))
      .build();

  private final List<User> boundaryValues = template.boundaryValues();

  @Test
  void includesNumericMinAndMax() {
    assertThat(boundaryValues).anyMatch(user -> user.getAge() == 18);
    assertThat(boundaryValues).anyMatch(user -> user.getAge() == 60);
  }

  @Test
  void includesNullAndCriticalLengthStrings() {
    assertThat(boundaryValues).anyMatch(user -> user.getName() == null);
    assertThat(boundaryValues).anyMatch(user -> user.getName() != null
        && user.getName().length() == 3);
    assertThat(boundaryValues).anyMatch(user -> user.getName() != null
        && user.getName().length() == 8);
  }

  @Test
  void includesEmptyAndMaxSizeCollections() {
    assertThat(boundaryValues).anyMatch(user -> user.getTags() != null
        && user.getTags().isEmpty());
    assertThat(boundaryValues).anyMatch(user -> user.getTags() != null
        && user.getTags().size() == 2);
  }

  @Test
  void everyGeneratedBoundaryValueStillSatisfiesConstraints() {
    assertThat(boundaryValues).allSatisfy(user -> {
      assertThat(user.getAge()).isBetween(18, 60);
      if (user.getName() != null) {
        assertThat(user.getName()).hasSizeBetween(3, 8);
      }
      if (user.getTags() != null) {
        assertThat(user.getTags()).hasSizeBetween(0, 2);
      }
    });
  }

  @Test
  void boundaryValuesAreReproducibleWithSeed() {
    Template<User> first = TestData.template(User.class, 99L)
        .constrain(User::setAge, c -> c.range(18, 60))
        .build();
    Template<User> second = TestData.template(User.class, 99L)
        .constrain(User::setAge, c -> c.range(18, 60))
        .build();

    assertThat(first.boundaryValues())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyElementsOf(second.boundaryValues());
  }
}
