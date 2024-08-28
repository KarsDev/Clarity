package me.kuwg.clarity.ast.nodes.function.declare;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class ReflectedNativeFunctionDeclaration extends ASTNode {

    private String name;
    private String fileName;
    private List<ParameterNode> params;
    private boolean isStatic;

    public ReflectedNativeFunctionDeclaration(final String name, final String className, final List<ParameterNode> params, final boolean isStatic) {
        this.name = name;
        this.fileName = className;
        this.params = params;
        this.isStatic = isStatic;
    }

    public ReflectedNativeFunctionDeclaration() {
    }

    public final String getName() {
        return name;
    }

    public final String getFileName() {
        return fileName;
    }

    public final List<ParameterNode> getParams() {
        return params;
    }

    public final boolean isStatic() {
        return isStatic;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Reflected Native Call: ").append(isStatic ? "static " : "").append(name).append("(");
        sb.append(indent).append(indent).append("File: ").append(fileName);
        if (params.isEmpty()) {
            sb.append(")");
        } else {
            sb.append("\n");
            final String paramIndent = indent + "    ";
            for (final ASTNode param : params) {
                param.print(sb, paramIndent);
                sb.append("\n");
            }
            sb.append(indent).append(")");
        }
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeString(fileName);
        out.writeNodeList(params);
        out.writeBoolean(isStatic);
    }

    @Override
    protected void load0(final ASTInputStream in) throws IOException {
        this.name = in.readString();
        this.fileName = in.readString();
        this.params = in.readNodeListNoCast();
        this.isStatic = in.readBoolean();
    }

    @Override
    public String toString() {
        return "ReflectedNativeFunctionDeclaration{" +
                "name='" + name + '\'' +
                ", fileName='" + fileName + '\'' +
                ", params=" + params +
                ", isStatic=" + isStatic +
                '}';
    }
}
