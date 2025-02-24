package me.kuwg.clarity.ast.nodes.clazz.virtual;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.PreInterpretable;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.VirtualFunctionDeclarationNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class VirtualClassDeclarationNode extends ASTNode implements PreInterpretable {
    private String name;
    private String inheritedClass;
    private String fileName;
    private List<FunctionDeclarationNode> constructors;
    private List<VirtualFunctionDeclarationNode> virtualFunctions;
    private BlockNode body;

    public VirtualClassDeclarationNode(final String name, final String inheritedClass, final String fileName, final List<FunctionDeclarationNode> constructors, final List<VirtualFunctionDeclarationNode> virtualFunctions, final BlockNode body) {
        this.name = name;
        this.inheritedClass = inheritedClass;
        this.fileName = fileName;
        this.constructors = constructors;
        this.virtualFunctions = virtualFunctions;
        this.body = body;
    }

    public VirtualClassDeclarationNode() {
    }

    @Override
    public BlockNode getBlock() {
        return body;
    }

    public List<FunctionDeclarationNode> getConstructors() {
        return constructors;
    }

    public String getFileName() {
        return fileName;
    }

    public String getInheritedClass() {
        return inheritedClass;
    }

    public String getName() {
        return name;
    }

    public List<VirtualFunctionDeclarationNode> getVirtualFunctions() {
        return virtualFunctions;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Virtual Class: ").append(name).append("\n");

        if (inheritedClass != null) {
            sb.append(indent).append("    ").append("Inherits: ").append(inheritedClass).append("\n");
        }
        sb.append(indent).append("    ").append("File: ").append(fileName).append("\n");
        if (!constructors.isEmpty()) {
            sb.append(indent).append("    ").append("Constructors:\n");
            for (final FunctionDeclarationNode constructor : constructors) {
                constructor.print(sb, indent + "        -");
            }
        } else {
            sb.append(indent).append("    Constructor: None\n");
        }

        if (!virtualFunctions.isEmpty()) {
            sb.append(indent).append("    Virtual Functions:\n");
            for (final VirtualFunctionDeclarationNode constructor : virtualFunctions) {
                constructor.print(sb, indent + "        -");
            }
        } else {
            sb.append(indent).append("    Virtual Functions: None\n");
        }

        sb.append(indent).append("    ").append("Class Body:\n");
        body.print(sb, indent + "        ");
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeString(inheritedClass == null ? "null" : inheritedClass);
        out.writeString(fileName);
        out.writeNodeList(constructors);
        out.writeNodeList(virtualFunctions);
        out.writeNode(body);
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.name = in.readString();
        this.inheritedClass = in.readString();
        if (this.inheritedClass.equals("null")) this.inheritedClass = null;
        this.fileName = in.readString();
        this.constructors = in.readNodeListNoCast(version);
        this.virtualFunctions = in.readNodeListNoCast(version);
        this.body = (BlockNode) in.readNode(version);
    }
}
