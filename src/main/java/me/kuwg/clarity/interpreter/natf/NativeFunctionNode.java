package me.kuwg.clarity.interpreter.natf;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;
import me.kuwg.clarity.library.natives.ClarityNativeFunction;

import java.io.IOException;
import java.util.List;

public class NativeFunctionNode extends ASTNode {

    private final ClarityNativeFunction<?> function;
    private final List<ASTNode> params;

    public NativeFunctionNode(final ClarityNativeFunction<?> function, final List<ASTNode> params) {
        this.function = function;
        this.params = params;
    }

    public ClarityNativeFunction<?> getFunction() {
        return function;
    }

    public List<ASTNode> getParams() {
        return params;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        throw new RuntimeException("Unsupported operation for native function node: \"print\"");
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        throw new RuntimeException("Unsupported operation for native function node: \"save\"");
    }

    @Override
    protected void load0(final ASTInputStream in) throws IOException {
        throw new RuntimeException("Unsupported operation for native function node: \"load\"");
    }
}
