package me.kuwg.clarity.interpreter.register;

import java.util.Arrays;

public final class RegisterStack {

    private final String[] elements;
    private final int maxSize;
    private int size = 0;

    public RegisterStack(final int maxSize) {
        if (maxSize <= 0) throw new IllegalArgumentException("Max size must be positive");

        this.maxSize = maxSize;
        this.elements = new String[maxSize];
    }

    public void push(final String element) {
        if (size >= maxSize) {
            System.arraycopy(elements, 1, elements, 0, maxSize - 1);
            elements[maxSize - 1] = element;
        } else {
            elements[size++] = element;
        }
    }

    public String pop() {
        if (size == 0) throw new IllegalStateException("Stack is empty");
        final String element = elements[--size];
        elements[size] = null;
        return element;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == maxSize;
    }

    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOf(elements, size));
    }
}
