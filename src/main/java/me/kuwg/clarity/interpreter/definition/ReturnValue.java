package me.kuwg.clarity.interpreter.definition;

public class ReturnValue {
    private final Object value;

    public ReturnValue(final Object value) {
        this.value = value;
    }

    public final Object getValue() {
        return value;
    }
}
