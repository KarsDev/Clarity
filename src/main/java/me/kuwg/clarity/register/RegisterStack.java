package me.kuwg.clarity.register;

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
     * The index of the top element in the stack.
     */
    private int top = -1;

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
        // If the stack is full, the next element will overwrite the oldest one
        top = (top + 1) % maxSize;  // Move top pointer in a circular way
        elements[top] = element;
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
        if (top == -1) throw new IllegalStateException("Stack is empty");

        Register.RegisterElement element = elements[top];
        elements[top] = null;  // Clear reference for garbage collection
        top = (top - 1 + maxSize) % maxSize;  // Move top pointer circularly
        return element;
    }

    /**
     * Checks whether the stack is empty.
     *
     * @return {@code true} if the stack is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return top == -1;
    }

    /**
     * Returns the current size of the stack.
     *
     * @return The number of elements currently in the stack.
     */
    public int size() {
        return top + 1;
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
        if (i < 0 || i > top) throw new IndexOutOfBoundsException("Index out of bounds");
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
        if (top == -1) return "[]";  // Return empty string if stack is empty

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i <= top; i++) {
            sb.append(elements[i]);
            if (i < top) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}