package me.kuwg.clarity.cir.interpreter.tokens;

public class CIRToken {

    private final String command;
    private final String[] parameters;
    private final int line;

    public CIRToken(final String command, final String[] parameters, final int line) {
        this.command = command;
        this.parameters = parameters;
        this.line = line;
    }

    public final String getCommand() {
        return command;
    }

    public final String[] getParameters() {
        return parameters;
    }

    public final int getLine() {
        return line;
    }
}
