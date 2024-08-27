package me.kuwg.clarity.interpreter.register;

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
        if (size >= maxSize) throw new IllegalStateException("Stack is full");
        elements[size++] = element;
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
}
