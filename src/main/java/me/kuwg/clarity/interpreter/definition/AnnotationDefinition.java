package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.ast.nodes.clazz.annotation.AnnotationDeclarationNode;
import me.kuwg.clarity.library.objects.ObjectType;

import java.util.List;

public class AnnotationDefinition extends ObjectType {

    private final String name;
    private final List<AnnotationDeclarationNode.AnnotationElement> annotationElements;

    public AnnotationDefinition(final String name, final List<AnnotationDeclarationNode.AnnotationElement> annotationElements) {
        this.name = name;
        this.annotationElements = annotationElements;
    }

    public final String getName() {
        return name;
    }

    public final List<AnnotationDeclarationNode.AnnotationElement> getAnnotationElements() {
        return annotationElements;
    }

    @Override
    public String toString() {
        return "Annotation@" + name;
    }
}
