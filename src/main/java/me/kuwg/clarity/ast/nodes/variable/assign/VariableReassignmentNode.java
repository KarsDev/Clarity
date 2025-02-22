package me.kuwg.clarity.ast.nodes.variable.assign;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class VariableReassignmentNode extends ASTNode {

    private String name;
    private ASTNode value;

    public VariableReassignmentNode(final String name, final ASTNode value) {
        this.name = name;
        this.value = value;
    }

    public VariableReassignmentNode() {
    }

    public final String getName() {
        return name;
    }

    public final ASTNode getValue() {
        return value;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Variable Reassignment:\n");

        sb.append(indent).append("  Name: ").append(name).append("\n");

        sb.append(indent).append("  Value:\n");
        value.print(sb, indent + "    ");
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeNode(value);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.name = in.readString();
        this.value = in.readNode(version);
    }

    @Override
    public String toString() {
        return "VariableReassignmentNode{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
