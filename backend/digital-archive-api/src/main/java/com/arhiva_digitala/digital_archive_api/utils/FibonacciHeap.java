package com.arhiva_digitala.digital_archive_api.utils; // Adjusted package

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class FibonacciHeap<T> {
    private Node<T> min;
    private int size;

    // Inner Node class - made public static
    public static class Node<T> {
        T data;
        double priority;
        Node<T> parent;
        Node<T> child;
        Node<T> left;
        Node<T> right;
        int degree;
        boolean marked;

        Node(T data, double priority) {
            this.data = data;
            this.priority = priority;
            this.degree = 0;
            this.marked = false;
            this.left = this;
            this.right = this;
        }

        // Added getter for convenience
        public T getData() {
            return data;
        }

        public double getPriority() {
            return priority;
        }
    }

    public FibonacciHeap() {
        min = null;
        size = 0;
    }

    // Changed return type to Node<T> and returns the created node
    public Node<T> insert(T data, double priority) {
        Node<T> node = new Node<>(data, priority);
        if (min == null) {
            min = node; // min.left and min.right are already self-referencing
        } else {
            // Add node to the root list (doubly linked circular list)
            node.left = min.left;
            node.right = min;
            min.left.right = node;
            min.left = node;

            if (priority < min.priority) {
                min = node;
            }
        }
        size++;
        return node;
    }

    public T extractMin() {
        if (min == null) {
            throw new NoSuchElementException("Heap is empty.");
        }

        Node<T> z = min;
        T extractedData = z.data;

        // Add children of z to the root list
        if (z.child != null) {
            Node<T> currentChild = z.child;
            do {
                Node<T> nextChild = currentChild.right;
                // Add currentChild to root list
                currentChild.left = min.left;
                currentChild.right = min;
                min.left.right = currentChild;
                min.left = currentChild;
                currentChild.parent = null; // Children become roots
                currentChild = nextChild;
            } while (currentChild != z.child); // Iterate through all children
        }

        // Remove z from the root list
        z.left.right = z.right;
        z.right.left = z.left;

        if (z == z.right) { // z was the only node in the root list (and no children added)
            min = null;
        } else {
            min = z.right; // Set a temporary min (arbitrary node in the list)
            consolidate();   // Consolidate to find the new actual min and restructure
        }

        size--;
        return extractedData;
    }

    // Helper to get the min node without extracting
    public Node<T> getMinNode() {
        return min;
    }

    public void decreaseKey(Node<T> node, double newPriority) {
        if (newPriority > node.priority) {
            throw new IllegalArgumentException("New priority is greater than current priority.");
        }
        if (min == null) {
             throw new IllegalStateException("Cannot decrease key on an empty heap.");
        }
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null.");
        }


        node.priority = newPriority;
        Node<T> parent = node.parent;

        if (parent != null && node.priority < parent.priority) {
            cut(node, parent);
            cascadingCut(parent);
        }

        if (node.priority < min.priority) {
            min = node;
        }
    }

    private void cut(Node<T> child, Node<T> parent) {
        // Remove child from parent's child list
        if (parent.child == child) { // child is the direct child pointed by parent.child
            if (child.right == child) { // child is the only child
                parent.child = null;
            } else {
                parent.child = child.right; // Make another child the direct child
            }
        }
        // Standard removal from doubly linked list
        child.left.right = child.right;
        child.right.left = child.left;
        parent.degree--;

        // Add child to the root list
        child.left = min.left;
        child.right = min;
        min.left.right = child;
        min.left = child;

        child.parent = null;
        child.marked = false;
    }

    private void cascadingCut(Node<T> node) {
        Node<T> parent = node.parent;
        if (parent != null) {
            if (!node.marked) {
                node.marked = true;
            } else {
                cut(node, parent);
                cascadingCut(parent);
            }
        }
    }

    private void consolidate() {
        if (min == null) return; // Nothing to consolidate

        // Max degree is O(log n). Phi = (1 + sqrt(5))/2
        int arraySize = (int) Math.floor(Math.log(size) / Math.log((1 + Math.sqrt(5)) / 2)) + 2;
        List<Node<T>> degreeTable = new ArrayList<>(arraySize);
        for (int i = 0; i < arraySize; i++) {
            degreeTable.add(null);
        }

        // For each node in the root list...
        List<Node<T>> rootNodes = new ArrayList<>();
        Node<T> current = min;
        Node<T> initialMin = min; // Keep track of start to avoid infinite loop on single node
        if (current == null) return;

        do {
            rootNodes.add(current);
            current = current.right;
        } while (current != initialMin && current != null);


        for (Node<T> w : rootNodes) {
            Node<T> x = w;
            int d = x.degree;

            // Ensure degreeTable is large enough
            while (d >= degreeTable.size()) {
                degreeTable.add(null);
            }

            while (degreeTable.get(d) != null) {
                Node<T> y = degreeTable.get(d); // Another node with same degree
                if (x.priority > y.priority) { // Ensure x is the one with smaller priority
                    Node<T> temp = x;
                    x = y;
                    y = temp;
                }
                link(y, x); // Make y a child of x
                degreeTable.set(d, null);
                d++;
                // Ensure degreeTable is large enough for next degree
                while (d >= degreeTable.size()) {
                    degreeTable.add(null);
                }
            }
            degreeTable.set(d, x);
        }

        // Rebuild the root list from the degree table
        min = null;
        for (int i = 0; i < degreeTable.size(); i++) {
            Node<T> nodeInTable = degreeTable.get(i);
            if (nodeInTable != null) {
                // Remove from whatever list it was in (it should be a root already)
                nodeInTable.left = nodeInTable;
                nodeInTable.right = nodeInTable;
                nodeInTable.parent = null; // Should already be null

                if (min == null) {
                    min = nodeInTable;
                } else {
                    // Add nodeInTable to the root list
                    nodeInTable.left = min.left;
                    nodeInTable.right = min;
                    min.left.right = nodeInTable;
                    min.left = nodeInTable;
                    if (nodeInTable.priority < min.priority) {
                        min = nodeInTable;
                    }
                }
            }
        }
    }

    private void link(Node<T> y, Node<T> x) {
        // Remove y from root list (it is being linked to x)
        y.left.right = y.right;
        y.right.left = y.left;

        // Make y a child of x
        y.parent = x;
        if (x.child == null) {
            x.child = y;
            y.right = y; // y is the only child, points to itself
            y.left = y;
        } else {
            // Add y to x's child list (doubly linked circular)
            y.left = x.child.left;
            y.right = x.child;
            x.child.left.right = y;
            x.child.left = y;
        }
        x.degree++;
        y.marked = false; // Children are initially unmarked after linking
    }

    public boolean isEmpty() {
        return min == null;
    }

    public int size() {
        return size;
    }
}
