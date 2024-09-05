package me.kuwg.clarity.compiler.cir;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.function.call.DefaultNativeFunctionCallNode;
import me.kuwg.clarity.register.Register;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public enum DefaultNativeFunctionHandler {
    PRINTLN() {
        @Override
        public void compile(final List<ASTNode> params, final CIRCompiler compiler) throws IOException {
            compiler.addInclude("<iostream>");
            compiler.writeWT("std::cout << ");
            for (int i = 0, paramsSize = params.size(); i < paramsSize; i++) {
                final ASTNode param = params.get(i);
                compiler.compileNode(param);
                if (i + 1 < paramsSize) {
                    compiler.write(" << ");
                } else compiler.write(" << std::endl");
            }
            compiler.write(";\n");
        }

        @Override
        String getName() {
            return "println";
        }
    };


    public static final Stream<DefaultNativeFunctionHandler> VALUES = Arrays.stream(values());

    public static void handle(final DefaultNativeFunctionCallNode node, final CIRCompiler compiler) throws IOException {
        final DefaultNativeFunctionHandler handler = fromValue(node.getName());
        if (handler == null) {
            Register.throwException("Unknown compiled native function: " + node.getName(), node.getLine());
            return;
        }
        handler.compile(node.getParams(), compiler);
    }

    public static DefaultNativeFunctionHandler fromValue(final String s) {
        return VALUES.filter(value -> value.getName().equals(s)).findFirst().orElse(null);
    }

    abstract void compile(final List<ASTNode> params, final CIRCompiler compiler) throws IOException;

    abstract String getName();
}
