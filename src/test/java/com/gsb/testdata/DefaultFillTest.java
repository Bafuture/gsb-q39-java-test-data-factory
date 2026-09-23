package com.gsb.testdata;

import com.gsb.testdata.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 1) Default filling: every field type gets a legal value without any configuration. */
class DefaultFillTest {

    @Test
    void fillsAllFieldTypesWithLegalDefaults() {
        User user = DataFactory.template(User.class).build().create();

        assertThat(user.getName()).isNotBlank();
        assertThat(user.getAge()).isNotNull();
        assertThat(user.getEmail()).isNotBlank();
        assertThat(user.getScore()).isNotNull();
        assertThat(user.getTags()).isNotNull();
        assertThat(user.getScores()).isNotNull();
        assertThat(user.getStatus()).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void explicitValuesOverrideDefaults() {
        Template<User> template = DataFactory.template(User.class)
                .set("name", "Alice")
                .set(User::getEmail, "alice@example.com")
                .set(User::setActive, true)
                .build();

        User user = template.create();

        assertThat(user.getName()).isEqualTo("Alice");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.isActive()).isTrue();
        // untouched fields still get defaults
        assertThat(user.getTags()).isNotNull();
        assertThat(user.getStatus()).isNotNull();
    }

    @Test
    void explicitValueIsStableAcrossInstances() {
        Template<User> template = DataFactory.template(User.class)
                .set(User::getName, "Fixed")
                .build();

        assertThat(template.createList(3)).allSatisfy(u -> assertThat(u.getName()).isEqualTo("Fixed"));
    }
}
