package me.kuwg.clarity.ast.nodes.clazz;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.PreInterpretable;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class ClassDeclarationNode extends ASTNode implements PreInterpretable {
    private String name;
    private boolean isConstant;
    private String fileName;
    private List<FunctionDeclarationNode> constructors;
    private BlockNode body;
    private String inheritedClass;
    private String extendedClass;

    public ClassDeclarationNode(final String name, final boolean isConstant, final String inheritedClass, final String extendedClass, final String fileName, List<FunctionDeclarationNode> constructors, final BlockNode body) {
        this.name = name;
        this.isConstant = isConstant;
        this.fileName = fileName;
        this.constructors = constructors;
        this.body = body;
        this.inheritedClass = inheritedClass;
        this.extendedClass = extendedClass;
    }

    public ClassDeclarationNode() {
    }

    public final String getName() {
        return name;
    }

    public final boolean isConstant() {
        return isConstant;
    }

    public final String getFileName() {
        return fileName;
    }

    public final List<FunctionDeclarationNode> getConstructors() {
        return constructors;
    }

    public final BlockNode getBlock() {
        return body;
    }

    public final String getInheritedClass() {
        return inheritedClass;
    }

    public String getExtendedClass() {
        return extendedClass;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Class: ").append(name).append("\n");

        sb.append(indent).append("    ").append("Constant: ").append(isConstant);

        if (inheritedClass != null) {
            sb.append(indent).append("    ").append("Inherits: ").append(inheritedClass).append("\n");
        }
        if (extendedClass != null) {
            sb.append(indent).append("    ").append("Extends: ").append(extendedClass).append("\n");
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

        if (body != null) {
            sb.append(indent).append("    ").append("Class Body:\n");
            body.print(sb, indent + "        ");
        }
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeBoolean(isConstant);
        out.writeString(inheritedClass != null ? inheritedClass : "null");
        out.writeString(extendedClass != null ? extendedClass : "null");
        out.writeString(fileName);
        out.writeNodeList(constructors);
        out.writeNode(body);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.name = in.readString();
        this.isConstant = in.readBoolean();
        this.inheritedClass = in.readString();
        if (this.inheritedClass.equals("null")) this.inheritedClass = null;
        if (version.isNewerThan(CompilerVersion.V1_1)) {
            this.extendedClass = in.readString();
            if (this.extendedClass.equals("null")) this.extendedClass = null;
        }
        this.fileName = in.readString();
        this.constructors = in.readNodeListNoCast(version);
        this.body = (BlockNode) in.readNode(version);
    }
}
