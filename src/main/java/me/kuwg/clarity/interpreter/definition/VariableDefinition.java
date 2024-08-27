package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.interpreter.types.VoidObject;
import me.kuwg.clarity.interpreter.types.ObjectType;

public class VariableDefinition extends ObjectType {

    private final String name;
    private Object value;
    private final boolean isConstant, isStatic;

    public VariableDefinition(final String name, final Object value, final boolean isConstant, final boolean isStatic) {
        this.name = name;
        this.value = value;
        this.isConstant = isConstant;
        this.isStatic = isStatic;
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

    public final boolean isConstant() {
        return isConstant;
    }

    public final boolean isStatic() {
        return isStatic;
    }

    @Override
    public String toString() {
        return "VariableDefinition{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", isConstant=" + isConstant +
                ", isStatic=" + isStatic +
                '}';
    }
}
