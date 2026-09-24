package com.gsb.testdata;

import static org.assertj.core.api.Assertions.assertThat;

import com.gsb.testdata.fixtures.User;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Same seed => identical data; no seed => different data per run. */
class SeedReproducibilityTest {

  @Test
  void sameSeedProducesIdenticalObjects() {
    Template<User> first = TestData.template(User.class, 42L).build();
    Template<User> second = TestData.template(User.class, 42L).build();

    assertThat(first.create())
        .usingRecursiveComparison()
        .isEqualTo(second.create());
  }

  @Test
  void sameSeedProducesIdenticalBatches() {
    Template<User> first = TestData.template(User.class, 7L).build();
    Template<User> second = TestData.template(User.class, 7L).build();

    List<User> batchA = first.create(10);
    List<User> batchB = second.create(10);

    assertThat(batchA)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyElementsOf(batchB);
  }

  @Test
  void differentSeedsProduceDifferentObjects() {
    Template<User> first = TestData.template(User.class, 1L).build();
    Template<User> second = TestData.template(User.class, 2L).build();

    assertThat(first.create())
        .usingRecursiveComparison()
        .isNotEqualTo(second.create());
  }

  @Test
  void withoutSeedDataDiffersBetweenRuns() {
    User first = TestData.template(User.class).build().create();
    User second = TestData.template(User.class).build().create();

    assertThat(first)
        .usingRecursiveComparison()
        .isNotEqualTo(second);
  }
}
