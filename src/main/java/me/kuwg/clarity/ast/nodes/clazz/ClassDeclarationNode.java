package me.kuwg.clarity.ast.nodes.clazz;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class ClassDeclarationNode extends ASTNode {

    private String name;
    private String fileName;
    private FunctionDeclarationNode constructor;
    private BlockNode body;
    private String inheritedClass;

    public ClassDeclarationNode(final String name, final String inheritedClass, final String fileName, final FunctionDeclarationNode constructor, final BlockNode body) {
        this.name = name;
        this.fileName = fileName;
        this.constructor = constructor;
        this.body = body;
        this.inheritedClass = inheritedClass;
    }

    public ClassDeclarationNode() {
    }

    public final String getName() {
        return name;
    }

    public final String getFileName() {
        return fileName;
    }

    public final FunctionDeclarationNode getConstructor() {
        return constructor;
    }

    public final BlockNode getBody() {
        return body;
    }

    public final String getInheritedClass() {
        return inheritedClass;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Class: ").append(name).append("\n");
        if (inheritedClass != null) {
            sb.append(indent).append("    ").append("Inherits: ").append(inheritedClass).append("\n");
        }
        sb.append(indent).append("    ").append("File: ").append(fileName).append("\n");
        if (constructor != null) {
            sb.append(indent).append("    ").append("Constructor:\n");
            constructor.print(sb, indent + "        ");
        } else {
            sb.append(indent).append("    Constructor: None\n");
        }

        if (body != null) {
            sb.append(indent).append("    ").append("Class Body:\n");
            body.print(sb, indent + "        ");
        }
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeString(inheritedClass != null ? inheritedClass : "null");
        out.writeString(fileName);
        out.writeNode(constructor);
        out.writeNode(body);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.name = in.readString();
        this.inheritedClass = in.readString();
        if (this.inheritedClass.equals("null")) this.inheritedClass = null;
        this.fileName = in.readString();
        this.constructor = (FunctionDeclarationNode) in.readNode();
        this.body = (BlockNode) in.readNode();
    }
}
