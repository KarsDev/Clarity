package me.kuwg.clarity.ast.nodes.clazz;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class ClassInstantiationNode extends ASTNode {

    private String name;
    private List<ASTNode> params;

    public ClassInstantiationNode(final String name, final List<ASTNode> params) {
        this.name = name;
        this.params = params;
    }

    public ClassInstantiationNode() {
    }

    public final String getName() {
        return name;
    }

    public final List<ASTNode> getParams() {
        return params;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Class Initialization: ").append(name).append("\n");

        sb.append(indent).append("Parameters: ");
        if (params == null || params.isEmpty()) {
            sb.append("None\n");
        } else {
            sb.append("\n");
            String paramIndent = indent + "    ";
            for (ASTNode param : params) {
                param.print(sb, paramIndent);
            }
        }
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeNodeList(params);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.name = in.readString();
        this.params = in.readNodeListNoCast();
    }

    @Override
    public String toString() {
        return "ClassInstantiationNode{" +
                "name='" + name + '\'' +
                ", params=" + params +
                '}';
    }
}
