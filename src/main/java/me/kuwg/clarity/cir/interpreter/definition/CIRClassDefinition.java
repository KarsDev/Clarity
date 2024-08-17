package me.kuwg.clarity.cir.interpreter.definition;

import java.util.HashMap;
import java.util.Map;

public class CIRClassDefinition {
    private final String name;
    private final CIRFunctionDefinition constructor;
    private final Map<String, CIRFunctionDefinition> methods;
    private final Map<String, Object> properties;

    public CIRClassDefinition(String name, final CIRFunctionDefinition constructor, Map<String, CIRFunctionDefinition> methods) {
        this.name = name;
        this.constructor = constructor;
        this.methods = methods;
        this.properties = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<String, CIRFunctionDefinition> getMethods() {
        return methods;
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    public final Map<String, Object> getProperties() {
        return properties;
    }

    public CIRFunctionDefinition getMethod(String name) {
        return methods.get(name);
    }

    public final CIRFunctionDefinition getConstructor() {
        return constructor;
    }

    @Override
    public String toString() {
        return "CIRClassDefinition{" +
                "name='" + name + '\'' +
                ", constructor=" + constructor +
                ", methods=" + methods +
                ", properties=" + properties +
                '}';
    }
}
