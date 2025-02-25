package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.ast.nodes.block.BlockNode;

import java.util.Arrays;

public class VirtualClassDefinition extends ClassDefinition {

    private final VirtualFunctionDefinition[] virtualFunctions;

    public VirtualClassDefinition(final String name, final ClassDefinition inheritedClass, final FunctionDefinition[] constructors, final BlockNode body, final VirtualFunctionDefinition[] virtualFunctions) {
        super(name, false, inheritedClass, constructors, body, false);
        this.virtualFunctions = virtualFunctions;
    }

    public VirtualFunctionDefinition[] getVirtualFunctions() {
        return virtualFunctions;
    }

    @Override
    public String toString() {
        return "VirtualClassDefinition{" +
                "virtualFunctions=" + Arrays.toString(virtualFunctions) +
                ", body=" + body +
                ", constructors=" + Arrays.toString(constructors) +
                ", inheritedClass=" + inheritedClass +
                ", isConstant=" + isConstant +
                ", isNative=" + isNative +
                ", name='" + name + '\'' +
                ", staticFunctions=" + staticFunctions +
                ", staticVariables=" + staticVariables +
                '}';
    }
}
