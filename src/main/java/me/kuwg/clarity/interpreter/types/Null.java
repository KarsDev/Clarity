package me.kuwg.clarity.interpreter.types;

public abstract class Null extends ObjectType {

    protected Null() {
    }

    @Override
    public boolean equals(final Object obj) {
        return obj.getClass() == NullImpl.class && obj == NULL;
    }

    @Override
    public String toString() {
        return "null";
    }

    public static final Null NULL = new NullImpl();

    private static class NullImpl extends Null {
    }
}
