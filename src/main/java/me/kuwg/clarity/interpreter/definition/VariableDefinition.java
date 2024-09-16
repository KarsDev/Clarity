package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.ObjectType;

public class VariableDefinition extends ObjectType {

    private final String name;
    private final String typeDefault;
    private Object value;
    private final boolean isConstant, isStatic, isLocal;

    public VariableDefinition(final String name, final String typeDefault, final Object value, final boolean isConstant, final boolean isStatic, final boolean isLocal) {
        this.name = name;
        this.typeDefault = typeDefault;
        this.value = value;
        this.isConstant = isConstant;
        this.isStatic = isStatic;
        this.isLocal = isLocal;
    }

    public final String getName() {
        return name;
    }

    public final String getTypeDefault() {
        return typeDefault;
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

    public final boolean isLocal() {
        return isLocal;
    }

    @Override
    public String toString() {
        return "VariableDefinition{" +
                "isConstant=" + isConstant +
                ", name='" + name + '\'' +
                ", typeDefault='" + typeDefault + '\'' +
                ", value=" + value +
                ", isStatic=" + isStatic +
                ", isLocal=" + isLocal +
                '}';
    }
}
