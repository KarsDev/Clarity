package me.kuwg.clarity.ast.nodes.clazz.envm;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnumDeclarationNode extends ASTNode {

    private String name;
    private boolean isConstant;
    private String fileName;
    private List<EnumValueNode> enumValues = new ArrayList<>();

    public EnumDeclarationNode(final String name, final boolean isConstant, final String fileName, final List<EnumValueNode> enumValues) {
        this.name = name;
        this.isConstant = isConstant;
        this.fileName = fileName;
        this.enumValues = enumValues;
    }

    public EnumDeclarationNode() {
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

    public final List<EnumValueNode> getEnumValues() {
        return enumValues;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Enum: ").append(name).append("\n");
        sb.append(indent).append("    ").append("Constant: ").append(isConstant).append("\n");
        sb.append(indent).append("    ").append("File: ").append(fileName).append("\n");

        if (!enumValues.isEmpty()) {
            sb.append(indent).append("    ").append("Values:\n");
            for (EnumValueNode value : enumValues) {
                value.print(sb, indent + "        -");
            }
        } else {
            sb.append(indent).append("    ").append("Values: None\n");
        }
    }

    @Override
    protected void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeString(name);
        out.writeBoolean(isConstant);
        out.writeString(fileName);
        out.writeNodeList(enumValues, version);
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.name = in.readString();
        this.isConstant = in.readBoolean();
        this.fileName = in.readString();
        this.enumValues = in.readNodeListNoCast(version);
    }

    public static class EnumValueNode extends ASTNode {

        private String name;
        private ASTNode value;

        public EnumValueNode(final String name, final ASTNode value) {
            this.name = name;
            this.value = value;
        }

        public EnumValueNode() {
        }

        public final String getName() {
            return name;
        }

        public final ASTNode getValue() {
            return value;
        }

        @Override
        public void print(final StringBuilder sb, final String indent) {
            sb.append(indent).append("EnumValue: ").append(name);
            if (value != null) {
                sb.append(" = ");
                value.print(sb, "");
            }
            sb.append("\n");
        }

        @Override
        protected void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
            out.writeString(name);
            out.writeNode(value, version);
        }

        @Override
        protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
            this.name = in.readString();
            this.value = in.readNode(version);
        }
    }
}
