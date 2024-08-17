package me.kuwg.clarity.cir.interpreter.definition;

import me.kuwg.clarity.interpreter.types.ObjectType;

import java.util.List;

public class CIRFunctionDefinition extends ObjectType {

    private final String name;
    private final List<String> params;
    private final List<String> instructions;

    public CIRFunctionDefinition(final String name, final List<String> params, final List<String> instructions) {
        this.name = name;
        this.params = params;
        this.instructions = instructions;
    }

    public final String getName() {
        return name;
    }

    public final List<String> getParams() {
        return params;
    }

    public final List<String> getInstructions() {
        return instructions;
    }

    @Override
    public String toString() {
        return "CIRFunctionDefinition{" +
                "name='" + name + '\'' +
                ", params=" + params +
                ", instructions=" + instructions +
                '}';
    }
}
