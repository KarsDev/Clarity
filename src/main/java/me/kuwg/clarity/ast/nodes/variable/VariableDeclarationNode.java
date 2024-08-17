package me.kuwg.clarity.ast.nodes.variable;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class VariableDeclarationNode extends ASTNode {
    private String name;
    private ASTNode value;

    public VariableDeclarationNode(final String name, final ASTNode value) {
        this.name = name;
        this.value = value;
    }

    public VariableDeclarationNode() {
        super();
    }

    public final String getName() {
        return name;
    }

    public final ASTNode getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "VariableDeclarationNode{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Variable Declaration:\n");

        sb.append(indent).append("  Name: ").append(name).append("\n");

        sb.append(indent).append("  Value:\n");
        if (value != null) {
            value.print(sb, indent + "    ");
        } else {
            sb.append(indent).append("    (no value assigned)\n");
        }
    }

    @Override
    public void save(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeNode(value);
    }

    @Override
    public void load(final ASTInputStream in) throws IOException {
        this.name = in.readString();
        this.value = in.readNode();
    }
}