package me.kuwg.clarity.ast.nodes.literal;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class NullNode extends ASTNode {
    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Null");
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
    }
}
