package com.gsb.testdata.fixtures;

/** Self-referencing type used to verify nesting depth protection. */
public class Node {

  private String label;
  private Node next;

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Node getNext() {
    return next;
  }

  public void setNext(Node next) {
    this.next = next;
  }
}
