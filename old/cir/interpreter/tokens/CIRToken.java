package me.kuwg.clarity.cir.interpreter.tokens;

import me.kuwg.clarity.cir.interpreter.CIRCommand;

import java.util.Arrays;

public class CIRToken {

    private final CIRCommand command;
    private final String[] parameters;
    private final int line;

    public CIRToken(final CIRCommand command, final String[] parameters, final int line) {
        this.command = command;
        this.parameters = parameters;
        this.line = line;
    }

    public final CIRCommand command() {
        return command;
    }

    public final String[] params() {
        return parameters;
    }

    public final int line() {
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
