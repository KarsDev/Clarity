package me.kuwg.clarity.ast.nodes.block;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.ast.stream.ASTInputStream;
import me.kuwg.clarity.compiler.ast.stream.ASTOutputStream;

import java.io.IOException;

public class ContinueNode extends ASTNode {
    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Continue");
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
    }

    @Override
    protected void load0(final ASTInputStream in) throws IOException {
    }
}
