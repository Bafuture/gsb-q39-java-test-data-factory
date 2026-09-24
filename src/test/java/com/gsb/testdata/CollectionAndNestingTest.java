package com.gsb.testdata;

import static org.assertj.core.api.Assertions.assertThat;

import com.gsb.testdata.fixtures.Address;
import com.gsb.testdata.fixtures.Node;
import com.gsb.testdata.fixtures.User;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Batch generation, nested auto-construction and depth-limit protection. */
class CollectionAndNestingTest {

  @Test
  void createsBatchesOfRequestedSize() {
    Template<User> template = TestData.template(User.class).build();

    List<User> users = template.create(10);

    assertThat(users).hasSize(10);
    assertThat(users).allSatisfy(user -> assertThat(user.getEmail()).isNotBlank());
  }

  @Test
  void nestedObjectsAreAutoConstructed() {
    User user = TestData.template(User.class).build().create();

    assertThat(user.getAddress()).isNotNull();
    assertThat(user.getAddress().getCity()).isNotBlank();
    assertThat(user.getAddress().getZip()).isNotBlank();
  }

  @Test
  void registeredNestedTemplateIsUsed() {
    Template<Address> addresses = TestData.template(Address.class)
        .set(Address::setCity, "Paris")
        .build();
    Template<User> users = TestData.template(User.class).use(addresses).build();

    assertThat(users.create().getAddress().getCity()).isEqualTo("Paris");
  }

  @Test
  void selfReferencingTypesStopAtDefaultMaxDepth() {
    Node root = TestData.template(Node.class).build().create();

    assertThat(root.getNext()).isNotNull();
    assertThat(root.getNext().getNext()).isNotNull();
    assertThat(root.getNext().getNext().getNext()).isNotNull();
    assertThat(root.getNext().getNext().getNext().getNext()).isNull();
  }

  @Test
  void customMaxDepthIsRespected() {
    Node root = TestData.template(Node.class).maxDepth(1).build().create();

    assertThat(root.getNext()).isNotNull();
    assertThat(root.getNext().getNext()).isNull();
  }
}
