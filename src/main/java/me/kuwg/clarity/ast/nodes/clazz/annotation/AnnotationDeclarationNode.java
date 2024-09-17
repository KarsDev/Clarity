package me.kuwg.clarity.ast.nodes.clazz.annotation;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class AnnotationDeclarationNode extends ASTNode {

    private String name;
    private List<AnnotationElement> annotationElements;

    public AnnotationDeclarationNode(final String name, final List<AnnotationElement> annotationElements) {
        this.name = name;
        this.annotationElements = annotationElements;
    }

    public AnnotationDeclarationNode() {
    }

    public final String getName() {
        return name;
    }

    public final List<AnnotationElement> getAnnotationElements() {
        return annotationElements;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Annotation: ").append(name).append("\n");
        if (annotationElements != null && !annotationElements.isEmpty()) {
            sb.append(indent).append("    ").append("Elements:\n");
            for (AnnotationElement element : annotationElements) {
                sb.append(indent).append("        - Name: ").append(element.getName())
                        .append(", Default Value: ").append(element.getDef()).append("\n");
            }
        } else {
            sb.append(indent).append("    ").append("Elements: None\n");
        }
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeNodeList(annotationElements);
    }

    @Override
    protected void load0(final ASTInputStream in) throws IOException {
        this.name = in.readString();
        this.annotationElements = in.readNodeListNoCast();
    }

    public static class AnnotationElement extends ASTNode {

        private String name;
        private ASTNode def;

        public AnnotationElement(final String name, final ASTNode def) {
            this.name = name;
            this.def = def;
        }

        public AnnotationElement() {
        }

        public final String getName() {
            return name;
        }

        public final ASTNode getDef() {
            return def;
        }

        @Override
        public void print(final StringBuilder sb, final String indent) {
            sb.append(indent).append("AnnotationElement: ").append(name)
                    .append(", Default Value: ").append(def).append("\n");
        }

        @Override
        protected void save0(final ASTOutputStream out) throws IOException {
            out.writeString(name);
            out.writeNode(def);
        }

        @Override
        protected void load0(final ASTInputStream in) throws IOException {
            this.name = in.readString();
            this.def = in.readNode();
        }
    }
}
