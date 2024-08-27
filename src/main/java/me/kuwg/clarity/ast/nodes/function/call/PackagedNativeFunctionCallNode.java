package me.kuwg.clarity.ast.nodes.function.call;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class PackagedNativeFunctionCallNode extends ASTNode {
    private String name;
    private String pkg;
    private List<ASTNode> params;

    public PackagedNativeFunctionCallNode(final String name, final String pkg, final List<ASTNode> params) {
        this.name = name;
        this.pkg = pkg;
        this.params = params;
    }

    public PackagedNativeFunctionCallNode() {
        super();
    }

    public final String getName() {
        return name;
    }

    public final String getPackage() {
        return pkg;
    }

    public final List<ASTNode> getParams() {
        return params;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("NativeFunctionCall:\n");
        sb.append(indent).append("  Name: ").append(name).append("\n");
        sb.append(indent).append("  Package: ").append(pkg).append("\n");
        sb.append(indent).append("  Parameters:\n");
        for (ASTNode param : params) {
            param.print(sb, indent + "    ");
        }
    }

    @Override
    public void save(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeString(pkg);
        out.writeNodeList(params);
    }

    @Override
    public void load(final ASTInputStream in) throws IOException {
        this.name = in.readString();
        this.pkg = in.readString();
        this.params = in.readNodeListNoCast();
    }
}
