package me.kuwg.clarity.cir.interpreter;

import me.kuwg.clarity.cir.interpreter.tokens.CIRToken;
import me.kuwg.clarity.interpreter.context.Context;

public class CIRCodeExecutor {

    private final CIRToken[] tokens;
    private final Context generalContext;

    public CIRCodeExecutor(final String[] lines) {
        this.tokens = tokenize(lines);
        this.generalContext = new Context();
    }

    public int interpret() {
        preRead();
        return 0;
    }

    private void preRead() {

        for (final CIRToken token : tokens) {

            switch (token.cmd()) {
                case "CLASS": {
                    final String name = token.params()[0];
                }
            }

        }
    }

    private CIRToken[] tokenize(final String[] lines) {
        final CIRToken[] tokenized = new CIRToken[lines.length];

        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];

            if (line.trim().isEmpty()) continue;

            final String[] parts = line.split(" ");

            final String command = parts[0];

            final String[] parameters = new String[parts.length - 1];
            System.arraycopy(parts, 1, parameters, 0, parts.length - 1);
            tokenized[i] = new CIRToken(command, parameters, i + 1);
        }
        shiftArray(tokenized);
        return tokenized;
    }

    private <T> void shiftArray(final T[] array) {
        int writeIndex = 0;

        for (int readIndex = 0; readIndex < array.length; readIndex++) {
            if (array[readIndex] != null) {
                array[writeIndex] = array[readIndex];
                writeIndex++;
            }
        }

        while (writeIndex < array.length) {
            array[writeIndex] = null;
            writeIndex++;
        }
    }
}
