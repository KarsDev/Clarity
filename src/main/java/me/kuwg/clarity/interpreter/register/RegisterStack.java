package me.kuwg.clarity.interpreter.register;

import java.util.Arrays;

public final class RegisterStack {

    private final Register.RegisterElement[] elements;
    private final int maxSize;
    private int size = 0;

    public RegisterStack(final int maxSize) {
        if (maxSize <= 0) throw new IllegalArgumentException("Max size must be positive");

        this.maxSize = maxSize;
        this.elements = new Register.RegisterElement[maxSize];
    }

    public void push(final Register.RegisterElement element) {
        if (size >= maxSize) {
            System.arraycopy(elements, 1, elements, 0, maxSize - 1);
            elements[maxSize - 1] = element;
        } else {
            elements[size++] = element;
        }
    }

    public Register.RegisterElement pop() {
        if (size == 0) throw new IllegalStateException("Stack is empty");
        final Register.RegisterElement element = elements[--size];
        elements[size] = null;
        return element;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public Register.RegisterElement at(final int i) {
        return elements[i];
    }

    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOf(elements, size));
    }
}
