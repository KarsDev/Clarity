package me.kuwg.clarity.ast.nodes.clazz.cast;

public enum CastType {
    FLOAT(""),
    INT(""),
    ARR(""),
    CLASS("");

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

            default:
                return null;
        }
    }

    public static final CastType[] VALUES = values();
}
