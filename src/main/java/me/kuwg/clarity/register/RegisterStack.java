package me.kuwg.clarity.register;

import java.util.Arrays;

/**
 * A stack data structure for storing {@link Register.RegisterElement} objects, with a fixed maximum size.
 * <p>
 * The {@code RegisterStack} class is used to manage a limited-size stack of function and operation
 * calls within the Clarity interpreter. If the stack exceeds the maximum size, the oldest element
 * is removed to make room for new elements.
 * </p>
 */
public final class RegisterStack {

    /**
     * An array to store the elements in the stack.
     */
    private final Register.RegisterElement[] elements;

    /**
     * The maximum number of elements the stack can hold.
     */
    private final int maxSize;

    /**
     * The current size of the stack (i.e., the number of elements currently stored).
     */
    private int size = 0;

    /**
     * Constructs a new {@code RegisterStack} with the specified maximum size.
     *
     * @param maxSize The maximum number of elements the stack can hold. Must be greater than zero.
     * @throws IllegalArgumentException if {@code maxSize} is less than or equal to zero.
     */
    public RegisterStack(final int maxSize) {
        if (maxSize <= 0) throw new IllegalArgumentException("Max size must be positive");

        this.maxSize = maxSize;
        this.elements = new Register.RegisterElement[maxSize];
    }

    /**
     * Pushes a new element onto the stack.
     * <p>
     * If the stack is full (i.e., its size equals {@code maxSize}), the oldest element is removed,
     * and the new element is added to the top of the stack.
     * </p>
     *
     * @param element The {@link Register.RegisterElement} to push onto the stack.
     */
    public void push(final Register.RegisterElement element) {
        if (size >= maxSize) {
            // Shift all elements one position to the left to remove the oldest element
            System.arraycopy(elements, 1, elements, 0, maxSize - 1);
            elements[maxSize - 1] = element;
        } else {
            elements[size++] = element;
        }
    }

    /**
     * Pops the top element from the stack.
     * <p>
     * This method removes and returns the element at the top of the stack. If the stack is empty,
     * an {@link IllegalStateException} is thrown.
     * </p>
     *
     * @return The {@link Register.RegisterElement} at the top of the stack.
     * @throws IllegalStateException if the stack is empty.
     */
    public Register.RegisterElement pop() {
        if (size == 0) throw new IllegalStateException("Stack is empty");
        final Register.RegisterElement element = elements[--size];
        elements[size] = null;  // Clear the reference for garbage collection
        return element;
    }

    /**
     * Checks whether the stack is empty.
     *
     * @return {@code true} if the stack is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the current size of the stack.
     *
     * @return The number of elements currently in the stack.
     */
    public int size() {
        return size;
    }

    /**
     * Retrieves the element at the specified index in the stack.
     * <p>
     * This method does not remove the element from the stack.
     * </p>
     *
     * @param i The index of the element to retrieve, where 0 is the bottom of the stack
     *          and {@code size - 1} is the top.
     * @return The {@link Register.RegisterElement} at the specified index.
     */
    public Register.RegisterElement at(final int i) {
        return elements[i];
    }

    /**
     * Returns a string representation of the stack.
     * <p>
     * The string contains all elements currently in the stack, in the order they were pushed.
     * </p>
     *
     * @return A string representation of the current state of the stack.
     */
    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOf(elements, size));
    }
}