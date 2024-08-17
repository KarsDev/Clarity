package me.kuwg.clarity.cir.interpreter.tokens;

import java.util.Arrays;

public class CIRToken {

    private final String command;
    private final String[] parameters;
    private final int line;

    public CIRToken(final String command, final String[] parameters, final int line) {
        this.command = command;
        this.parameters = parameters;
        this.line = line;
    }

    public final String cmd() {
        return command;
    }

    public final String[] params() {
        return parameters;
    }

    public final int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "CIRToken{" +
                "command='" + command + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                ", line=" + line +
                '}';
    }
}
