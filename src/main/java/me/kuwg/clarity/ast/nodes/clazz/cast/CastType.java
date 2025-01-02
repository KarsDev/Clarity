package me.kuwg.clarity.ast.nodes.clazz.cast;

import me.kuwg.clarity.library.objects.VoidObject;
import me.kuwg.clarity.library.objects.types.ClassObject;
import me.kuwg.clarity.library.objects.types.LambdaObject;

public enum CastType {
    STR(null) {
        @Override
        public boolean is(Object object) {
            return object instanceof String;
        }
    },
    FLOAT(null) {
        @Override
        public boolean is(Object object) {
            return object instanceof Double;
        }
    },
    INT(null) {
        @Override
        public boolean is(Object object) {
            return object instanceof Long;
        }
    },
    ARR(null) {
        @Override
        public boolean is(Object object) {
            return object instanceof Object[];
        }
    },
    CLASS("") {
        @Override
        public boolean is(Object object) {
            if (!(object instanceof ClassObject)) return false;
            ClassObject classObject = (ClassObject) object;
            return classObject.isInstance(this.getValue());
        }
    },
    BOOL(null) {
        @Override
        public boolean is(Object object) {
            return object instanceof Boolean;
        }
    },
    VOID(null) {
        @Override
        public boolean is(Object object) {
            return object instanceof VoidObject;
        }
    },
    LAMBDA(null) {
        @Override
        public boolean is(Object object) {
            return object instanceof LambdaObject; // Assuming LambdaObject is a type in your code
        }
    },
    NUM(null) {
        @Override
        public boolean is(final Object object) {
            return object instanceof Number;
        }
    },
    NULL(null) {
        @Override
        public boolean is(final Object object) {
            return object == null;
        }
    };

    private String value;

    CastType(String value) {
        this.value = value;
    }

    public final CastType setValue(final String value) {
        assert this == CLASS;
        this.value = value;
        return CLASS;
    }

    public final String getValue() {
        assert this == CLASS;
        return value;
    }

    public static CastType fromValue(final String s) {
        switch (s) {
            case "float":
                return FLOAT;

            case "int":
                return INT;

            case "arr":
                return ARR;

            case "str":
                return STR;

            case "bool":
                return BOOL;

            case "void":
                return VOID;

            case "lambda":
                return LAMBDA;

            default:
                return null;
        }
    }

    public static final CastType[] VALUES = values();

    public abstract boolean is(Object object);
}