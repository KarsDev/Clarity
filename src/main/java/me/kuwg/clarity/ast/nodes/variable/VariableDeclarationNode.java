package me.kuwg.clarity.ast.nodes.variable;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

public class VariableDeclarationNode extends ASTNode {

    private final String name;
    private final ASTNode expression;


    public VariableDeclarationNode(final String name, final ASTNode expression) {
        this.name = name;
        this.expression = expression;
    }

    public final String getName() {
        return name;
    }

    public final ASTNode getExpression() {
        return expression;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Variable Declaration:\n");

        sb.append(indent).append("  Name: ").append(name).append("\n");

        sb.append(indent).append("  Expression:\n");
        if (expression != null) {
            expression.print(sb, indent + "    ");
        } else {
            sb.append(indent).append("    (no expression)\n");
        }
    }

    @Override
    public void save(final ASTOutputStream out) {

    }
}