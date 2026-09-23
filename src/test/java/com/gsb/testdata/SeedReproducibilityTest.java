package com.gsb.testdata;

import com.gsb.testdata.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 2) Reproducible randomness: same seed -> identical data, no seed -> different data. */
class SeedReproducibilityTest {

    @Test
    void sameSeedProducesIdenticalObjects() {
        User first = DataFactory.template(User.class).seed(42L).build().create();
        User second = DataFactory.template(User.class).seed(42L).build().create();

        assertThat(first).usingRecursiveComparison().isEqualTo(second);
    }

    @Test
    void sameSeedProducesIdenticalSequences() {
        Template<User> first = DataFactory.template(User.class).seed(7L).build();
        Template<User> second = DataFactory.template(User.class).seed(7L).build();

        List<User> firstBatch = first.createList(10);
        List<User> secondBatch = second.createList(10);

        assertThat(firstBatch).usingRecursiveComparison().isEqualTo(secondBatch);
    }

    @Test
    void differentSeedsProduceDifferentData() {
        User first = DataFactory.template(User.class).seed(1L).build().create();
        User second = DataFactory.template(User.class).seed(2L).build().create();

        assertThat(first).usingRecursiveComparison().isNotEqualTo(second);
    }

    @Test
    void noSeedProducesDifferentDataAcrossRuns() {
        User first = DataFactory.template(User.class).build().create();
        User second = DataFactory.template(User.class).build().create();

        assertThat(first).usingRecursiveComparison().isNotEqualTo(second);
    }
}
