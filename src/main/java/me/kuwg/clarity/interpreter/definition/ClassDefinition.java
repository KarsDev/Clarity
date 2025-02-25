package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.library.objects.ObjectType;

import java.util.*;

public class ClassDefinition extends ObjectType {

    public final Map<String, VariableDefinition> staticVariables = new HashMap<>();
    public final List<FunctionDefinition> staticFunctions = new ArrayList<>();

    protected final String name;
    protected final boolean isConstant;
    protected final ClassDefinition inheritedClass;
    protected final VirtualClassDefinition extendedClass;
    protected final FunctionDefinition[] constructors;
    protected final BlockNode body;
    protected final boolean isNative;

    public ClassDefinition(final String name, final boolean isConstant, final ClassDefinition inheritedClass, final VirtualClassDefinition extendedClass, final FunctionDefinition[] constructors, final BlockNode body, final boolean isNative) {
        this.name = name;
        this.isConstant = isConstant;
        this.inheritedClass = inheritedClass;
        this.extendedClass = extendedClass;
        this.constructors = constructors;
        this.body = body;
        this.isNative = isNative;
    }

    public final String getName() {
        return name;
    }

    public final boolean isConstant() {
        return isConstant;
    }

    public final ClassDefinition getInheritedClass() {
        return inheritedClass;
    }

    public VirtualClassDefinition getExtendedClass() {
        return extendedClass;
    }

    public final FunctionDefinition[] getConstructors() {
        return constructors;
    }

    public final BlockNode getBody() {
        return body;
    }

    public final boolean isNative() {
        return isNative;
    }

    public final FunctionDefinition getStaticFunction(final String name, final int params) {
        for (final FunctionDefinition staticFunction : staticFunctions) {
            if (staticFunction.getName().equals(name) && staticFunction.getParams().size() == params) {
                return staticFunction;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ClassDefinition{" +
                "body=" + body +
                ", staticVariables=" + staticVariables +
                ", staticFunctions=" + staticFunctions +
                ", name='" + name + '\'' +
                ", isConstant=" + isConstant +
                ", inheritedClass=" + inheritedClass +
                ", extendedClass=" + extendedClass +
                ", constructors=" + Arrays.toString(constructors) +
                ", isNative=" + isNative +
                '}';
    }
}
