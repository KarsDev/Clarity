package me.kuwg.clarity.ast.nodes.variable.assign;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class VariableDeclarationNode extends ASTNode {
    private String name;
    private boolean isConstant, isStatic;
    private ASTNode value;

    public VariableDeclarationNode(final String name, final ASTNode value, final boolean isConstant,  final boolean isStatic) {
        this.name = name;
        this.isConstant = isConstant;
        this.isStatic = isStatic;
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

    public final boolean isConstant() {
        return isConstant;
    }

    public final boolean isStatic() {
        return isStatic;
    }

    @Override
    public String toString() {
        return "VariableDeclarationNode{" +
                "name='" + name + '\'' +
                ", isConstant=" + isConstant +
                ", isStatic=" + isStatic +
                ", value=" + value +
                '}';
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Variable Declaration:\n");

        sb.append(indent).append("  Name: ").append(name).append("\n");

        if (isConstant) {
            sb.append(indent).append("  Type: Constant\n");
        }
        if (isStatic) {
            sb.append(indent).append("  Type: Static\n");
        }

        sb.append(indent).append("  Value:\n");
        if (value != null) {
            value.print(sb, indent + "    ");
        } else {
            sb.append(indent).append("    (no value assigned)\n");
        }
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeNode(value);
        out.writeBoolean(isConstant);
        out.writeBoolean(isStatic);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.name = in.readString();
        this.value = in.readNode();
        this.isConstant = in.readBoolean();
        this.isStatic = in.readBoolean();
    }
}
