package me.kuwg.clarity.ast.nodes.clazz.cast;

public enum CastType {
    FLOAT, INT, ARR;

    public static CastType fromStreamByte(final byte val) {
        switch (val) {
            case 0x0:
                return FLOAT;

            case 0x1:
                return INT;

            case 0x2:
                return ARR;

            default:
                throw new RuntimeException("Unreachable");
        }
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

    public byte toStreamByte() {
        switch (this) {
            case FLOAT:
                return 0x0;

            case INT:
                return 0x1;

            case ARR:
                return 0x2;

            default:
                throw new RuntimeException("Unreachable");
        }
    }
}
