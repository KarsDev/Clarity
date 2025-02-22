package me.kuwg.clarity.ast.nodes.clazz.annotation;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class AnnotationUseNode extends ASTNode {
    private String name;
    private List<AnnotationValueAssign> values;
    private ASTNode following;

    public AnnotationUseNode(final String name, final List<AnnotationValueAssign> values, final ASTNode following) {
        this.name = name;
        this.values = values;
        this.following = following;
    }

    public AnnotationUseNode() {
    }

    public final String getName() {
        return name;
    }

    public final List<AnnotationValueAssign> getValues() {
        return values;
    }

    public final ASTNode getFollowing() {
        return following;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Annotation Use: ").append(name).append("\n");
        if (values != null && !values.isEmpty()) {
            sb.append(indent).append("    ").append("Values:\n");
            for (AnnotationValueAssign value : values) {
                value.print(sb, indent + "        ");
            }
        } else {
            sb.append(indent).append("    ").append("Values: None\n");
        }
        if (following != null) {
            sb.append(indent).append("    ").append("Following Node:\n");
            following.print(sb, indent + "        ");
        } else {
            sb.append(indent).append("    ").append("Following Node: None\n");
        }
    }

    @Override
    protected void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeString(name);
        out.writeNodeList(values, version);
        out.writeNode(following, version);
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.name = in.readString();
        this.values = in.readNodeListNoCast(version);
        this.following = in.readNode(version);
    }

    public static class AnnotationValueAssign extends ASTNode {

        private String name;
        private ASTNode value;

        public AnnotationValueAssign(final String name, final ASTNode value) {
            this.name = name;
            this.value = value;
        }

        public AnnotationValueAssign() {
        }

        public final String getName() {
            return name;
        }

        public final ASTNode getValue() {
            return value;
        }

        @Override
        public void print(final StringBuilder sb, final String indent) {
            sb.append(indent).append("AnnotationElement: ").append(name)
                    .append(", Value: ").append(value).append("\n");
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