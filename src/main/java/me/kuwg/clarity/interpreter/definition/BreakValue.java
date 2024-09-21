package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.library.objects.VoidObject;

public class BreakValue extends VoidObject {
    public static final BreakValue BREAK = new BreakValue();

    private BreakValue() {
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }
}
