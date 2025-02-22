package me.kuwg.clarity.ast.nodes.block;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class RaiseNode extends ASTNode {
    private ASTNode exception;

    public RaiseNode(final ASTNode exception) {
        this.exception = exception;
    }

    public RaiseNode() {
    }

    public final ASTNode getException() {
        return exception;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Raise: ").append("\n");
        exception.print(sb, indent + "   ");
    }

    @Override
    protected void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeNode(exception, version);
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.exception = in.readNode(version);
    }
}
