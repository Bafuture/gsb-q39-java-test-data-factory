package com.gsb.testdata;

import com.gsb.testdata.domain.Node;
import com.gsb.testdata.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 4) Collections & nesting: batch generation, nested auto-construction, depth guard. */
class CollectionAndNestedTest {

    @Test
    void createListGeneratesRequestedAmount() {
        Template<User> template = DataFactory.template(User.class).seed(5L).build();

        List<User> users = template.createList(5);

        assertThat(users).hasSize(5);
        assertThat(users).allSatisfy(u -> {
            assertThat(u.getName()).isNotBlank();
            assertThat(u.getAddress()).isNotNull();
        });
    }

    @Test
    void createListRejectsNegativeCount() {
        Template<User> template = DataFactory.template(User.class).build();
        assertThatThrownBy(() -> template.createList(-1)).isInstanceOf(TestDataException.class);
    }

    @Test
    void nestedObjectsAreAutoConstructed() {
        User user = DataFactory.template(User.class).build().create();

        assertThat(user.getAddress()).isNotNull();
        assertThat(user.getAddress().getCity()).isNotBlank();
        assertThat(user.getAddress().getStreet()).isNotBlank();
    }

    @Test
    void selfReferencingTypeTerminatesWithinDefaultDepth() {
        Node root = DataFactory.template(Node.class).build().create();

        int depth = 0;
        for (Node current = root; current != null; current = current.getNext()) {
            depth++;
            assertThat(depth).as("chain must terminate").isLessThanOrEqualTo(Template.DEFAULT_MAX_DEPTH + 1);
        }
        assertThat(depth).isGreaterThan(1);
    }

    @Test
    void customMaxDepthIsHonored() {
        User shallow = DataFactory.template(User.class).maxDepth(1).build().create();
        assertThat(shallow.getAddress()).isNull();

        User deeper = DataFactory.template(User.class).maxDepth(2).build().create();
        assertThat(deeper.getAddress()).isNotNull();
    }

    @Test
    void maxDepthMustBePositive() {
        assertThatThrownBy(() -> DataFactory.template(User.class).maxDepth(0))
                .isInstanceOf(TestDataException.class);
    }
}
