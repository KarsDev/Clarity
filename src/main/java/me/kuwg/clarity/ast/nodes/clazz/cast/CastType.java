package me.kuwg.clarity.ast.nodes.clazz.cast;

import me.kuwg.clarity.library.objects.VoidObject;
import me.kuwg.clarity.library.objects.types.ClassObject;
import me.kuwg.clarity.library.objects.types.LambdaObject;

@SuppressWarnings("StaticInitializerReferencesSubClass")
public class CastType {

    public static final CastType NUM = new NumCastType();
    public static final CastType FLOAT = new FloatCastType();
    public static final CastType INT = new IntCastType();
    public static final CastType STR = new StrCastType();
    public static final CastType ARR = new ArrCastType();
    public static final CastType BOOL = new BoolCastType();

    private String value;

    public CastType(final String value) {
        this.value = value;
    }

    public static CastType fromValue(final String s) {
        switch (s) {
            case "float":
                return new FloatCastType();
            case "int":
                return new IntCastType();
            case "arr":
                return new ArrCastType();
            case "str":
                return new StrCastType();
            case "bool":
                return new BoolCastType();
            case "void":
                return new VoidCastType();
            case "lambda":
                return new LambdaCastType();
            default:
                return null;
        }
    }

    public static CastType ofOrdinal(final int ordinal) {
        switch (ordinal) {
            case 0:
                return new StrCastType();
            case 1:
                return new FloatCastType();
            case 2:
                return new IntCastType();
            case 3:
                return new ArrCastType();
            case 4:
                return new ClassCastType(null);
            case 5:
                return new BoolCastType();
            case 6:
                return new VoidCastType();
            case 7:
                return new LambdaCastType();
            case 8:
                return new NumCastType();
            case 9:
                return new NullCastType();
            default:
                throw new IllegalArgumentException("Invalid ordinal: " + ordinal);
        }
    }

    public String getValue() {
        return value;
    }

    public CastType setValue(final String value) {
        throw new UnsupportedOperationException("setValue is not supported for this type");
    }

    public boolean is(final Object object) {
        throw new UnsupportedOperationException("is method must be implemented in subclasses");
    }

    public final int ordinal() {
        if (this instanceof StrCastType) {
            return 0;
        } else if (this instanceof FloatCastType) {
            return 1;
        } else if (this instanceof IntCastType) {
            return 2;
        } else if (this instanceof ArrCastType) {
            return 3;
        } else if (this instanceof ClassCastType) {
            return 4;
        } else if (this instanceof BoolCastType) {
            return 5;
        } else if (this instanceof VoidCastType) {
            return 6;
        } else if (this instanceof LambdaCastType) {
            return 7;
        } else if (this instanceof NumCastType) {
            return 8;
        } else if (this instanceof NullCastType) {
            return 9;
        }

        throw new IllegalArgumentException("Unsupported cast type: " + this.getClass().getSimpleName());
    }

    public final boolean isClass() {
        return this instanceof ClassCastType;
    }

    static final class StrCastType extends CastType {
        public StrCastType() {
            super(null);
        }

        @Override
        public boolean is(final Object object) {
            return object instanceof String;
        }
    }

    static final class FloatCastType extends CastType {
        public FloatCastType() {
            super(null);
        }

        @Override
        public boolean is(final Object object) {
            return object instanceof Double;
        }
    }

    static final class IntCastType extends CastType {
        public IntCastType() {
            super(null);
        }

        @Override
        public boolean is(final Object object) {
            return object instanceof Long;
        }
    }

    static final class ArrCastType extends CastType {
        public ArrCastType() {
            super(null);
        }

        @Override
        public boolean is(final Object object) {
            return object instanceof Object[];
        }
    }

    public static final class ClassCastType extends CastType {
        public ClassCastType(final String value) {
            super(value);
        }

        @Override
        public boolean is(final Object object) {
            if (!(object instanceof ClassObject)) return false;
            final ClassObject classObject = (ClassObject) object;
            return classObject.isInstance(this.getValue());
        }

        @Override
        public CastType setValue(final String value) {
            super.value = value;
            return this;
        }
    }

    static final class BoolCastType extends CastType {
        public BoolCastType() {
            super(null);
        }

        @Override
        public boolean is(final Object object) {
            return object instanceof Boolean;
        }
    }

    static final class VoidCastType extends CastType {
        public VoidCastType() {
            super(null);
        }

        @Override
        public boolean is(final Object object) {
            return object instanceof VoidObject;
        }
    }

    static final class LambdaCastType extends CastType {
        public LambdaCastType() {
            super(null);
        }

        @Override
        public boolean is(final Object object) {
            return object instanceof LambdaObject;
        }
    }

    static final class NumCastType extends CastType {
        public NumCastType() {
            super(null);
        }

        @Override
        public boolean is(final Object object) {
            return object instanceof Number;
        }
    }

    static final class NullCastType extends CastType {
        public NullCastType() {
            super(null);
        }

        @Override
        public boolean is(final Object object) {
            return object == null;
        }
    }
}