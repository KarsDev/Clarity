package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.interpreter.types.VoidObject;
import me.kuwg.clarity.interpreter.types.ObjectType;

public class VariableDefinition extends ObjectType {

    private final String name;
    private Object value;

    public VariableDefinition(final String name, final Object value) {
        this.name = name;
        this.value = value;
    }

    public final String getName() {
        return name;
    }

    public final Object getValue() {
        return value;
    }

    public final void setValue(final Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "VariableDefinition{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
