package me.kuwg.clarity.cir.interpreter.definition;

import me.kuwg.clarity.interpreter.types.ObjectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CIRClassDefinition extends ObjectType {
    private final String name;
    private final CIRFunctionDefinition constructor;
    private final Map<String, CIRFunctionDefinition> methods;
    private final Map<String, Object> variables;

    public CIRClassDefinition(final String name, final CIRFunctionDefinition constructor, final Map<String, CIRFunctionDefinition> methods, final Map<String, Object> variables) {
        this.name = name;
        this.constructor = constructor;
        this.methods = methods;
        this.variables = variables;
    }

    public final String getName() {
        return name;
    }

    public final CIRFunctionDefinition getConstructor() {
        return constructor;
    }

    public final Map<String, CIRFunctionDefinition> getMethods() {
        return methods;
    }

    public final Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public String toString() {
        return "CIRClassDefinition{" +
                "name='" + name + '\'' +
                ", constructor=" + constructor +
                ", methods=" + methods +
                ", variables=" + variables +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CIRClassDefinition that = (CIRClassDefinition) o;
        return Objects.equals(name, that.name) && Objects.equals(constructor, that.constructor) && Objects.equals(methods, that.methods) && Objects.equals(variables, that.variables);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(constructor);
        result = 31 * result + Objects.hashCode(methods);
        result = 31 * result + Objects.hashCode(variables);
        return result;
    }
}
