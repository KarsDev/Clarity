package me.kuwg.clarity.ast.nodes.statements;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.PreInterpretable;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class ForeachNode extends ASTNode implements PreInterpretable {

    private String variable;
    private ASTNode list;
    private BlockNode block;

    public ForeachNode(final String variable, final ASTNode list, final BlockNode block) {
        this.variable = variable;
        this.list = list;
        this.block = block;
    }

    public ForeachNode() {
        super();
    }

    public final String getVariable() {
        return variable;
    }

    public final ASTNode getList() {
        return list;
    }

    public final BlockNode getBlock() {
        return block;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("ForeachNode:\n");
        sb.append(indent).append("  Variable: ").append(variable).append("\n");
        sb.append(indent).append("  List:\n");
        if (list != null) {
            list.print(sb, indent + "    ");
        }
        sb.append(indent).append("  Block:\n");
        if (block != null) {
            block.print(sb, indent + "    ");
        }
    }

    @Override
    protected void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeString(variable);
        out.writeNode(list, version);
        out.writeNode(block, version);
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.variable = in.readString();
        this.list = in.readNode(version);
        this.block = (BlockNode) in.readNode(version);
    }
}
