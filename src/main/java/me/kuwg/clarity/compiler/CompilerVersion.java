package me.kuwg.clarity.compiler;

import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public enum CompilerVersion {
    PRE, V1_0,

    ;

    public static final CompilerVersion LATEST = values()[values().length - 1];

    private static final CompilerVersion[] VALUES = values();

    /**
     * Array of 40 booleans.
     * There is just a chance of 1/2^40 (9.09E-9%) for PRE release and this to overwrite (1 in 110 billion).
     * This is way rarer than winning the Powerball jackpot TWICE (1 in 100 billion).
     * This is also much less probable to happen than being struck by an asteroid (1 in 74 billion).
     */
    private static final boolean[] VERSION_PREFIX = {
            true, false, true, false, true, false, false, false,
            true, false, false, false, false, true, true, false,
            true, false, true, true, true, false, false, true,
            false, false, false, true, true, false, true, true,
            true, false, true, true, false, false, true, true
    };

    public static CompilerVersion read(final ASTInputStream in) throws IOException {
        in.mark(VERSION_PREFIX.length);

        for (final boolean bit : VERSION_PREFIX) {
            if (in.readBoolean() != bit) {
                in.reset();
                return PRE;
            }
        }

        final int ordinal = in.readInt();
        try {
            return VALUES[ordinal];
        } catch (final IndexOutOfBoundsException e) {
            throw new RuntimeException("Version is too new, ordinal=" + ordinal, e);
        }
    }

    public static void write(final ASTOutputStream out) throws IOException {
        for (final boolean bit : VERSION_PREFIX) {
            out.writeBoolean(bit);
        }

        out.writeInt(LATEST.ordinal());
    }

    public boolean isNewerThan(final CompilerVersion other) {
        return this.ordinal() > other.ordinal();
    }

    public boolean isNewerThanOrEquals(final CompilerVersion other) {
        return this.ordinal() >= other.ordinal();
    }

    public boolean isOlderThan(final CompilerVersion other) {
        return this.ordinal() < other.ordinal();
    }

    public boolean isOlderThanOrEquals(final CompilerVersion other) {
        return this.ordinal() <= other.ordinal();
    }

    @Override
    public String toString() {
        return this == LATEST ? "Latest (" + name() + ")" : super.toString();
    }
}
