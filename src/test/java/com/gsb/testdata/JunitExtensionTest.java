package com.gsb.testdata;

import com.gsb.testdata.domain.User;
import com.gsb.testdata.junit.Generated;
import com.gsb.testdata.junit.TestDataExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/** 6) JUnit 5 integration: generated objects are injected directly into test methods. */
@ExtendWith(TestDataExtension.class)
class JunitExtensionTest {

    @Test
    void injectsFullyPopulatedObject(@Generated User user) {
        assertThat(user.getName()).isNotBlank();
        assertThat(user.getAge()).isNotNull();
        assertThat(user.getAddress()).isNotNull();
        assertThat(user.getStatus()).isNotNull();
    }

    @Test
    void seededInjectionIsReproducible(@Generated(seed = 99L) User first, @Generated(seed = 99L) User second) {
        assertThat(first).usingRecursiveComparison().isEqualTo(second);
    }

    @Test
    void differentSeedsInjectDifferentData(@Generated(seed = 1L) User first, @Generated(seed = 2L) User second) {
        assertThat(first).usingRecursiveComparison().isNotEqualTo(second);
    }
}
