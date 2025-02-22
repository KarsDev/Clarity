package me.kuwg.clarity.compiler;

import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public enum CompilerVersion {
    PRE, V1_0,

    ;

    public static final CompilerVersion LATEST = values()[values().length - 1];

    private static final CompilerVersion[] VALUES = values();
    private static final boolean[] DECLARE = {
            true, false, true, false, true, false, false, false,
            true, false, false, false, false, true, true, false,
            true, false, true, true, true, false, false, true,
            false, false, false, true, true, false, true, true,
            true, false, true, true, false, false, true, true
    };

    @Override
    public String toString() {
        return this == LATEST ? "Latest (" + name() + ")" : super.toString();
    }

    public static CompilerVersion read(final ASTInputStream in) throws IOException {
        in.mark(DECLARE.length);

        for (final boolean bit : DECLARE) {
            if (in.readBoolean() != bit) {
                in.reset();
                return PRE;
            }
        }

        final int ordinal = in.readVarInt();
        return VALUES[ordinal];
    }

    public static void write(final ASTOutputStream out) throws IOException {
        for (final boolean bit : DECLARE) {
            out.writeBoolean(bit);
        }

        out.writeVarInt(LATEST.ordinal());
    }
}
