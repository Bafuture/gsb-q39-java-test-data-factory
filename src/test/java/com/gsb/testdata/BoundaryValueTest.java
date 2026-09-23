package com.gsb.testdata;

import com.gsb.testdata.constraint.Constraints;
import com.gsb.testdata.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 5) Boundary values: min, max, just-outside, empty/null, critical lengths and sizes. */
class BoundaryValueTest {

    private final Template<User> template = DataFactory.template(User.class)
            .seed(1L)
            .constrain("age", Constraints.range(18, 60))
            .constrain("name", Constraints.text().minLength(3).maxLength(8).prefix("u").build())
            .constrain("tags", Constraints.size(1, 3))
            .build();

    @Test
    void numericBoundariesIncludeMinMaxAndJustOutside() {
        List<User> users = template.boundaryValues("age");

        assertThat(users).extracting(User::getAge)
                .contains(18, 60, 17, 61)
                .containsNull();
    }

    @Test
    void stringBoundariesIncludeCriticalLengthsAndNull() {
        List<User> users = template.boundaryValues("name");

        List<Integer> lengths = users.stream()
                .map(User::getName)
                .filter(Objects::nonNull)
                .map(String::length)
                .toList();
        assertThat(lengths).contains(3, 8, 2, 9, 0);
        assertThat(users).extracting(User::getName).containsNull();
        // valid-length boundary values still honor the prefix
        assertThat(users.stream()
                .map(User::getName)
                .filter(n -> n != null && (n.length() == 3 || n.length() == 8)))
                .allSatisfy(n -> assertThat(n).startsWith("u"));
    }

    @Test
    void collectionBoundariesIncludeCriticalSizesAndNull() {
        List<User> users = template.boundaryValues("tags");

        List<Integer> sizes = users.stream()
                .map(User::getTags)
                .filter(Objects::nonNull)
                .map(List::size)
                .toList();
        assertThat(sizes).contains(1, 3, 0, 4);
        assertThat(users).extracting(User::getTags).containsNull();
    }

    @Test
    void unconstrainedFieldFallsBackToTypeBoundaries() {
        Template<User> plain = DataFactory.template(User.class).seed(9L).build();

        List<User> users = plain.boundaryValues("email");

        assertThat(users).extracting(User::getEmail).contains("").containsNull();
    }

    @Test
    void unknownFieldFailsFast() {
        assertThatThrownBy(() -> template.boundaryValues("nope")).isInstanceOf(TestDataException.class);
    }
}
