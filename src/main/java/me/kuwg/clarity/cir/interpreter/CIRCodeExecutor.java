package me.kuwg.clarity.cir.interpreter;

import me.kuwg.clarity.token.Token;

public class CIRCodeExecutor {

    private final Token[] tokens;
    private int current;

    public CIRCodeExecutor(final String[] lines) {
        this.tokens = tokenize(lines);
    }

    public int interpret() {

        return 0;
    }

    private Token[] tokenize(final String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];

            String[] parts = line.split(" ");

        }
        return null;
    }
}
