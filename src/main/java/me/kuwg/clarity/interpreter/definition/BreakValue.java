package me.kuwg.clarity.interpreter.definition;

public class BreakValue {
    public static final BreakValue BREAK = new BreakValue();

    private BreakValue() {
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }
}
