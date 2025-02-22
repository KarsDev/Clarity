package me.kuwg.clarity.ast.nodes.statements;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.clazz.cast.CastType;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class IsNode extends ASTNode {

    private ASTNode expression;
    private CastType type;

    public IsNode(final ASTNode expression, final CastType type) {
        this.expression = expression;
        this.type = type;
    }

    public IsNode() {
    }

    public final ASTNode getExpression() {
        return expression;
    }

    public final CastType getType() {
        return type;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("IsStatement:\n");
        sb.append(indent).append("  Expression: ");
        if (expression != null) {
            expression.print(sb, indent + "    ");
        } else {
            sb.append("null\n");
        }
        sb.append(indent).append("  Type: ").append(type != null ? type.toString() : "null").append("\n");
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        out.writeNode(expression);
        out.writeVarInt(type.ordinal());
        if (type.isClass()) out.writeString(type.getValue());
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.expression = in.readNode(version);
        this.type = CastType.ofOrdinal(in.readVarInt());
        if (this.type.isClass()) this.type.setValue(in.readString());
    }
}
