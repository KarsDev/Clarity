package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.library.objects.VoidObject;

public class ContinueValue extends VoidObject {
    public static final ContinueValue CONTINUE = new ContinueValue();

    private ContinueValue() {
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }
}
