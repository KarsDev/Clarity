package me.kuwg.clarity.ast.nodes.function.call;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class DefaultNativeFunctionCallNode extends ASTNode {

    private String name;
    private List<ASTNode> params;

    public DefaultNativeFunctionCallNode(final String name, final List<ASTNode> params) {
        this.name = name;
        this.params = params;
    }

    public DefaultNativeFunctionCallNode() {
        super();
    }

    public final String getName() {
        return name;
    }

    public final List<ASTNode> getParams() {
        return params;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("DefaultNativeFunctionCall:\n");
        sb.append(indent).append("  Name: ").append(name).append("\n");
        sb.append(indent).append("  Parameters:\n");
        for (ASTNode param : params) {
            param.print(sb, indent + "    ");
        }
    }

    @Override
    public void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeString(name);
        out.writeNodeList(params, version);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.name = in.readString();
        this.params = in.readNodeListNoCast(version);
    }
}
