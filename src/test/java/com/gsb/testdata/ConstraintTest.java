package com.gsb.testdata;

import com.gsb.testdata.constraint.Constraints;
import com.gsb.testdata.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 3) Constraints: generated values comply; bad configuration fails at template-definition time. */
class ConstraintTest {

    @Test
    void numericRangeIsRespected() {
        Template<User> template = DataFactory.template(User.class)
                .seed(1L)
                .constrain("age", Constraints.range(18, 60))
                .build();

        assertThat(template.createList(200))
                .allSatisfy(u -> assertThat(u.getAge()).isBetween(18, 60));
    }

    @Test
    void stringLengthAndPrefixAreRespected() {
        Template<User> template = DataFactory.template(User.class)
                .seed(2L)
                .constrain(User::getName, Constraints.text().minLength(5).maxLength(10).prefix("usr").build())
                .build();

        assertThat(template.createList(200)).allSatisfy(u -> {
            assertThat(u.getName()).startsWith("usr");
            assertThat(u.getName().length()).isBetween(5, 10);
        });
    }

    @Test
    void collectionSizeIsRespected() {
        Template<User> template = DataFactory.template(User.class)
                .seed(3L)
                .constrain("tags", Constraints.size(1, 3))
                .build();

        assertThat(template.createList(200))
                .allSatisfy(u -> assertThat(u.getTags()).hasSizeBetween(1, 3));
    }

    @Test
    void invertedRangeFailsAtDefinitionTime() {
        assertThatThrownBy(() -> Constraints.range(10, 1))
                .isInstanceOf(InvalidConstraintException.class);
    }

    @Test
    void invalidLengthFailsAtDefinitionTime() {
        assertThatThrownBy(() -> Constraints.length(-1, 5)).isInstanceOf(InvalidConstraintException.class);
        assertThatThrownBy(() -> Constraints.length(10, 5)).isInstanceOf(InvalidConstraintException.class);
        assertThatThrownBy(() -> Constraints.text().minLength(2).maxLength(1).build())
                .isInstanceOf(InvalidConstraintException.class);
    }

    @Test
    void prefixLongerThanMaxLengthFailsAtDefinitionTime() {
        assertThatThrownBy(() -> Constraints.text().maxLength(2).prefix("too-long").build())
                .isInstanceOf(InvalidConstraintException.class);
    }

    @Test
    void invalidSizeFailsAtDefinitionTime() {
        assertThatThrownBy(() -> Constraints.size(3, 1)).isInstanceOf(InvalidConstraintException.class);
        assertThatThrownBy(() -> Constraints.size(-1, 3)).isInstanceOf(InvalidConstraintException.class);
    }

    @Test
    void unknownFieldFailsAtDefinitionTime() {
        assertThatThrownBy(() -> DataFactory.template(User.class).constrain("noSuchField", Constraints.range(1, 2)))
                .isInstanceOf(TestDataException.class);
        assertThatThrownBy(() -> DataFactory.template(User.class).set("noSuchField", 1))
                .isInstanceOf(TestDataException.class);
    }

    @Test
    void constraintOnIncompatibleFieldTypeFailsAtDefinitionTime() {
        assertThatThrownBy(() -> DataFactory.template(User.class).constrain("name", Constraints.range(1, 2)))
                .isInstanceOf(InvalidConstraintException.class);
        assertThatThrownBy(() -> DataFactory.template(User.class).constrain("age", Constraints.size(1, 2)))
                .isInstanceOf(InvalidConstraintException.class);
    }

    @Test
    void incompatibleExplicitValueFailsAtDefinitionTime() {
        assertThatThrownBy(() -> DataFactory.template(User.class).set("age", "not-a-number"))
                .isInstanceOf(TestDataException.class);
    }
}
