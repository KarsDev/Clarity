package me.kuwg.clarity.ast.nodes.expression;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableReassignmentNode;
import me.kuwg.clarity.compiler.ast.stream.ASTInputStream;
import me.kuwg.clarity.compiler.ast.stream.ASTOutputStream;

import java.io.IOException;

public class BinaryExpressionNode extends ASTNode {

    private ASTNode left;
    private String operator;
    private ASTNode right;

    public BinaryExpressionNode(final ASTNode left, final String operator, final ASTNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public BinaryExpressionNode() {
        super();
    }

    public final ASTNode getLeft() {
        return left;
    }

    public final String getOperator() {
        return operator;
    }

    public final ASTNode getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "BinaryExpressionNode{" +
                "left=" + left +
                ", operator='" + operator + '\'' +
                ", right=" + right +
                '}';
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Binary Expression:\n");

        sb.append(indent).append("  Left Operand:\n");
        if (left != null) {
            left.print(sb, indent + "    ");
        } else {
            sb.append(indent).append("    (no left operand)\n");
        }

        sb.append(indent).append("  Operator: ").append(operator).append("\n");

        sb.append(indent).append("  Right Operand:\n");
        if (right != null) {
            right.print(sb, indent + "    ");
        } else {
            sb.append(indent).append("    (no right operand)\n");
        }
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeNode(left);
        out.writeString(operator);
        out.writeNode(right);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.left = in.readNode();
        this.operator = in.readString();
        this.right = in.readNode();
    }
}