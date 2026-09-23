package com.gsb.testdata;

import com.gsb.testdata.domain.User;
import com.gsb.testdata.junit.Generated;
import com.gsb.testdata.junit.TestDataExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/** 6b) JUnit 5 integration: a registered template customizes injected instances. */
@ExtendWith(TestDataExtension.class)
class RegisteredTemplateTest {

    @BeforeAll
    static void register() {
        TestDataExtension.registerTemplate(User.class,
                DataFactory.template(User.class).set(User::getName, "Registered").build());
    }

    @AfterAll
    static void unregister() {
        TestDataExtension.clearTemplates();
    }

    @Test
    void registeredTemplateIsUsedForInjection(@Generated User user) {
        assertThat(user.getName()).isEqualTo("Registered");
        assertThat(user.getAddress()).isNotNull();
    }
}
