package me.kuwg.clarity.interpreter.definition;

public class ContinueValue {

    public static final ContinueValue CONTINUE = new ContinueValue();

    private ContinueValue() {
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }
}
