package me.kuwg.clarity.interpreter.types;

public abstract class VoidObject extends ObjectType {

    protected VoidObject() {
    }

    @Override
    public boolean equals(final Object obj) {
        return obj.getClass() == VoidImpl.class && obj == VOID;
    }

    @Override
    public String toString() {
        return "VOID";
    }

    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final VoidObject VOID = new VoidImpl();

    private static class VoidImpl extends VoidObject {
    }
}
