package me.kuwg.clarity.ast.nodes.statements;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class AssertNode extends ASTNode {
    private ASTNode condition;
    private ASTNode orElse;

    public AssertNode(final ASTNode condition, final ASTNode orElse) {
        this.condition = condition;
        this.orElse = orElse;
    }

    public AssertNode() {
    }

    public final ASTNode getCondition() {
        return condition;
    }

    public final ASTNode getOrElse() {
        return orElse;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Assert:\n");
        sb.append(indent).append("  Condition: ");
        if (condition != null) {
            condition.print(sb, indent + "    ");
        } else {
            sb.append("null\n");
        }
        sb.append(indent).append("  Or Else: ");
        if (orElse != null) {
            orElse.print(sb, indent + "    ");
        } else {
            sb.append("null\n");
        }
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        out.writeNode(condition);
        final boolean b = orElse != null;
        out.writeBoolean(b);
        if (b) out.writeNode(orElse);
    }

    @Override
    protected void load0(final ASTInputStream in) throws IOException {
        this.condition = in.readNode();
        final boolean b = in.readBoolean();
        if (b) this.orElse = in.readNode();
    }
}
