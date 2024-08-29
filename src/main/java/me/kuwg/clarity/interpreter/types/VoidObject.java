package me.kuwg.clarity.interpreter.types;

@SuppressWarnings("StaticInitializerReferencesSubClass")
public abstract class VoidObject extends ObjectType {

    protected VoidObject() {
    }

    @Override
    public boolean equals(final Object obj) {
        return obj.getClass() == VoidImpl.class && obj == VOID_OBJECT;
    }

    @Override
    public String toString() {
        return "VOID";
    }

    public static final VoidObject VOID_OBJECT = new VoidImpl();
    public static final VoidObject VOID_RETURN = new VoidReturn();

    private static class VoidImpl extends VoidObject {
        @Override
        public String toString() {
            return "VOIDOBJECT";
        }
    }
    public static class VoidReturn extends VoidImpl {
        @Override
        public String toString() {
            return "VOIDRETURN";
        }
    }
}
