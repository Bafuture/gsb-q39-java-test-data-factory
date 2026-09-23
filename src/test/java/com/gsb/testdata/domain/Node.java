package com.gsb.testdata.domain;

/** Self-referencing type used to verify the nesting depth guard. */
public class Node {
    private String label;
    private Node next;

    public Node() {
    }

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
