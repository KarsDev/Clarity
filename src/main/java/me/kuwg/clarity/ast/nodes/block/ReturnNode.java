package me.kuwg.clarity.ast.nodes.block;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class ReturnNode extends ASTNode {
    private ASTNode value;

    public ReturnNode(final ASTNode value) {
        this.value = value;
    }

    public ReturnNode() {
        super();
    }

    public final ASTNode getValue() {
        return value;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Return:\n");
        if (value != null) {
            value.print(sb, indent + "    ");
        } else {
            sb.append(indent).append("    None\n");
        }
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeNode(value);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.value = in.readNode(version);
    }
}
