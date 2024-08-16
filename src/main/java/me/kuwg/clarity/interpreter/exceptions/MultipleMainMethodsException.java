package me.kuwg.clarity.interpreter.exceptions;

public class MultipleMainMethodsException extends RuntimeException {
    public MultipleMainMethodsException() {
        super("Multiple main methods declared in src.");
    }
}
