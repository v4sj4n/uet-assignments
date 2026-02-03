package com.calendar.bst;

import com.calendar.model.Event;

class BSTNode {

    private Event event;
    private BSTNode left;
    private BSTNode right;

    BSTNode(Event event) {
        this.event = event;
        this.left = null;
        this.right = null;
    }

    Event getEvent() {
        return event;
    }

    void setEvent(Event event) {
        this.event = event;
    }

    BSTNode getLeft() {
        return left;
    }

    void setLeft(BSTNode left) {
        this.left = left;
    }

    BSTNode getRight() {
        return right;
    }

    void setRight(BSTNode right) {
        this.right = right;
    }

    boolean isLeaf() {
        return left == null && right == null;
    }

    boolean hasOneChild() {
        return (left == null) != (right == null);
    }

    boolean hasTwoChildren() {
        return left != null && right != null;
    }

    @Override
    public String toString() {
        return "BSTNode{event=" + event.toSimpleString() + "}";
    }
}
