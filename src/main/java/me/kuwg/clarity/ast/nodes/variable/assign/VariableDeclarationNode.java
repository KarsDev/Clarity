package me.kuwg.clarity.ast.nodes.variable.assign;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.ast.stream.ASTInputStream;
import me.kuwg.clarity.compiler.ast.stream.ASTOutputStream;

import java.io.IOException;

public class VariableDeclarationNode extends ASTNode {
    private String name;
    private String typeDefault;
    private boolean isConstant, isStatic;
    private ASTNode value;

    public VariableDeclarationNode(final String name, final String typeDefault, final ASTNode value, final boolean isConstant,  final boolean isStatic) {
        this.name = name;
        this.typeDefault = !typeDefault.equals("var") ? typeDefault : null;
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

    public final String getTypeDefault() {
        return typeDefault;
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
                "isConstant=" + isConstant +
                ", name='" + name + '\'' +
                ", typeDefault='" + typeDefault + '\'' +
                ", isStatic=" + isStatic +
                ", value=" + value +
                '}';
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Variable Declaration: ").append(typeDefault != null ? "(" + typeDefault + ")\n" : "\n");

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
        out.writeString(String.valueOf(typeDefault));
        out.writeNode(value);
        out.writeBoolean(isConstant);
        out.writeBoolean(isStatic);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.name = in.readString();
        this.typeDefault = in.readString();
        if (this.typeDefault.equals("null")) this.typeDefault = null;
        this.value = in.readNode();
        this.isConstant = in.readBoolean();
        this.isStatic = in.readBoolean();
    }
}
