package me.kuwg.clarity.library.objects;

import me.kuwg.clarity.register.Register;

/**
 * An abstract class representing a special type of object in the Clarity language.
 * This class is used to denote void or non-returning values.
 */
@SuppressWarnings("StaticInitializerReferencesSubClass")
public abstract class VoidObject extends ObjectType {

    /**
     * Protected constructor to prevent direct instantiation of this class.
     */
    protected VoidObject() {
    }

    /**
     * Checks if this instance is equal to another object.
     * Two instances are considered equal if the other object is an instance of
     * {@link VoidImpl} and is the same as the static {@link #VOID_OBJECT} instance.
     *
     * @param obj The object to compare with.
     * @return {@code true} if the object is equal to {@link #VOID_OBJECT}; {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        return obj.getClass() == VoidImpl.class && obj == VOID_OBJECT;
    }

    /**
     * Returns a string representation of this void object.
     *
     * @return A string "VOID" representing this void object.
     */
    @Override
    public String toString() {
        return "VOID";
    }

    /**
     * A static instance of {@link VoidObject} representing a general void value.
     */
    public static final VoidObject VOID_OBJECT = new VoidImpl();

    /**
     * A static instance of {@link VoidObject} used specifically for return values.
     */
    public static final VoidObject VOID_RETURN = new VoidReturn();

    /**
     * A private static class representing an implementation of {@link VoidObject}.
     * This class is used internally to provide the instance for {@link #VOID_OBJECT}.
     */
    private static class VoidImpl extends VoidObject {
        @Override
        public String toString() {
            return "VOIDOBJECT";
        }
    }

    /**
     * A public static class extending {@link VoidImpl} to represent a specific type of void object
     * used for return values.
     */
    public static class VoidReturn extends VoidImpl {
        @Override
        public String toString() {
            return "VOIDRETURN";
        }
    }
}
