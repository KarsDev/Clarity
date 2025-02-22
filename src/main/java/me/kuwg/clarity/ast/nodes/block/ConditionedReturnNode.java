package me.kuwg.clarity.ast.nodes.block;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class ConditionedReturnNode extends ASTNode {
    private ASTNode value;
    private ASTNode condition;

    public ConditionedReturnNode(final ASTNode value, final ASTNode condition) {
        this.value = value;
        this.condition = condition;
    }

    public ConditionedReturnNode() {
        super();
    }

    public final ASTNode getValue() {
        return value;
    }

    public final ASTNode getCondition() {
        return condition;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Conditioned Return:\n");
        sb.append(indent).append(indent).append("Condition: ");

        condition.print(sb, indent + indent + indent);

        sb.append(indent).append(" ").append("Value: ");
        if (value != null) {
            value.print(sb, indent + "    ");
        } else {
            sb.append(indent).append("    None\n");
        }
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeNode(value);
        out.writeNode(condition);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.value = in.readNode(version);
        this.condition = in.readNode(version);
    }
}
