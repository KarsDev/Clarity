package me.kuwg.clarity.ast.nodes.literal;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class VoidNode extends ASTNode {

    public VoidNode() {
        super();
    }


    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Void\n");
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
    }
}
