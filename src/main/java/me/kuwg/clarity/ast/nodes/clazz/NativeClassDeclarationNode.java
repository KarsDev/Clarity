package me.kuwg.clarity.ast.nodes.clazz;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class NativeClassDeclarationNode extends ASTNode {

    private String name;
    private boolean isConstant;
    private String inheritedClass;
    private String fileName;
    private List<FunctionDeclarationNode> constructors;
    private BlockNode body;

    public NativeClassDeclarationNode(final String name, final boolean isConstant, final String inheritedClass, final String fileName, List<FunctionDeclarationNode> constructors, final BlockNode body) {
        this.name = name;
        this.isConstant = isConstant;
        this.inheritedClass = inheritedClass;
        this.fileName = fileName;
        this.constructors = constructors;
        this.body = body;
    }

    public NativeClassDeclarationNode() {
    }

    public final String getName() {
        return name;
    }

    public final boolean isConstant() {
        return isConstant;
    }

    public final String getInheritedClass() {
        return inheritedClass;
    }

    public final String getFileName() {
        return fileName;
    }

    public final List<FunctionDeclarationNode> getConstructors() {
        return constructors;
    }

    public final BlockNode getBody() {
        return body;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Native Class: ").append(name).append("\n");

        sb.append(indent).append("    ").append("Constant: ").append(isConstant);

        if (inheritedClass != null) {
            sb.append(indent).append("    ").append("Inherits: ").append(inheritedClass).append("\n");
        }
        sb.append(indent).append(indent).append("File: ").append(fileName).append("\n");

        if (constructors.size() != 0) {
            sb.append(indent).append("    ").append("Constructors:\n");
            for (final FunctionDeclarationNode constructor : constructors) {
                constructor.print(sb, indent + "        -");
            }
        } else {
            sb.append(indent).append("    Constructor: None\n");
        }

        sb.append(indent).append("    Class Body:\n");
        body.print(sb, indent + "        ");
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeBoolean(isConstant);
        out.writeString(inheritedClass != null ? inheritedClass : "null");
        out.writeString(fileName);
        out.writeNodeList(constructors);
        out.writeNode(body);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.name = in.readString();
        this.isConstant = in.readBoolean();
        this.inheritedClass = in.readString();
        if (this.inheritedClass.equals("null")) this.inheritedClass = null;
        this.fileName = in.readString();
        this.constructors = in.readNodeListNoCast();
        this.body = (BlockNode) in.readNode();
    }
}
