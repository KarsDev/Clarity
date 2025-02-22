package me.kuwg.clarity.ast.nodes.statements;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class TernaryOperatorNode extends ASTNode {

    private ASTNode condition;
    private ASTNode trueBranch;
    private ASTNode falseBranch;

    public TernaryOperatorNode(final ASTNode condition, final ASTNode trueBranch, final ASTNode falseBranch) {
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    public TernaryOperatorNode() {
        super();
    }

    public final ASTNode getCondition() {
        return condition;
    }

    public final ASTNode getTrueBranch() {
        return trueBranch;
    }

    public final ASTNode getFalseBranch() {
        return falseBranch;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("TernaryOperatorNode:\n");
        sb.append(indent).append("  Condition:\n");
        if (condition != null) {
            condition.print(sb, indent + "    ");
        }
        sb.append(indent).append("  TrueBranch:\n");
        if (trueBranch != null) {
            trueBranch.print(sb, indent + "    ");
        }
        sb.append(indent).append("  FalseBranch:\n");
        if (falseBranch != null) {
            falseBranch.print(sb, indent + "    ");
        }
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        out.writeNode(condition);
        out.writeNode(trueBranch);
        out.writeNode(falseBranch);
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.condition = in.readNode(version);
        this.trueBranch = in.readNode(version);
        this.falseBranch = in.readNode(version);
    }
}