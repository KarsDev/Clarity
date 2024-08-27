package me.kuwg.clarity.ast.nodes.clazz;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class ClassDeclarationNode extends ASTNode {

    private String name;
    private FunctionDeclarationNode constructor;
    private BlockNode body;

    public ClassDeclarationNode(final String name, final FunctionDeclarationNode constructor, final BlockNode body) {
        this.name = name;
        this.constructor = constructor;
        this.body = body;
    }

    public ClassDeclarationNode() {
    }

    public final String getName() {
        return name;
    }

    public final FunctionDeclarationNode getConstructor() {
        return constructor;
    }

    public final BlockNode getBody() {
        return body;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Class: ").append(name).append("\n");

        if (constructor != null) {
            sb.append(indent).append("    ").append("Constructor:\n");
            constructor.print(sb, indent + "        ");
        } else {
            sb.append(indent).append("    Constructor: None\n");
        }

        sb.append(indent).append("    Class Body:\n");
        body.print(sb, indent + "        ");
    }

    @Override
    public void save(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeNode(constructor);
        out.writeNode(body);
    }

    @Override
    public void load(final ASTInputStream in) throws IOException {
        this.name = in.readString();
        this.constructor = (FunctionDeclarationNode) in.readNode();
        this.body = (BlockNode) in.readNode();
    }
}
