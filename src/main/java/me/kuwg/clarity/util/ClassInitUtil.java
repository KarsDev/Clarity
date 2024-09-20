package me.kuwg.clarity.util;

import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.interpreter.definition.ClassDefinition;
import me.kuwg.clarity.interpreter.definition.FunctionDefinition;
import me.kuwg.clarity.interpreter.types.ClassObject;
import me.kuwg.clarity.library.ObjectType;
import me.kuwg.clarity.register.Register;

import java.util.List;

import static me.kuwg.clarity.Clarity.INTERPRETER;
import static me.kuwg.clarity.library.VoidObject.VOID_OBJECT;

public class ClassInitUtil {

    public static ClassObject initClass(final String className, final List<Object> constructorParams, final Context context) {
        context.setCurrentClassName(className);

        final Context classContext = new Context(context);

        final ObjectType raw = context.getClass(className);
        if (raw == VOID_OBJECT) {
            INTERPRETER.except("Class not found: " + className, -404);
            return null;
        }

        final ClassDefinition definition = (ClassDefinition) raw;

        ClassObject inheritedObject = null;
        ClassDefinition currentDefinition = definition;

        while (currentDefinition.getInheritedClass() != null) {
            final ClassDefinition inheritedClass = currentDefinition.getInheritedClass();

            context.setCurrentClassName(inheritedClass.getName());
            INTERPRETER.interpretBlock(inheritedClass.getBody(), context);
            context.setCurrentClassName(className);

            final FunctionDefinition[] inheritedConstructors = inheritedClass.getConstructors();
            inheritedObject = INTERPRETER.interpretConstructors(inheritedObject, inheritedConstructors, constructorParams, classContext, inheritedClass.getName());
            classContext.mergeContext(inheritedObject.getContext());

            currentDefinition = inheritedClass;
        }

        final Object val = INTERPRETER.interpretBlock(definition.getBody(), classContext);

        if (val != VOID_OBJECT) INTERPRETER.except("Return in class body", -404);

        final ClassObject result = INTERPRETER.interpretConstructors(inheritedObject, definition.getConstructors(), constructorParams, classContext, className);
        context.setCurrentClassName(null);
        return result;
    }

}
