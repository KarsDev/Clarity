package me.kuwg.clarity.util;

import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.interpreter.definition.ClassDefinition;
import me.kuwg.clarity.interpreter.definition.FunctionDefinition;
import me.kuwg.clarity.library.objects.types.ClassObject;
import me.kuwg.clarity.library.objects.ObjectType;

import java.util.List;

import static me.kuwg.clarity.Clarity.INTERPRETER;
import static me.kuwg.clarity.library.objects.VoidObject.VOID_OBJECT;

/**
 * Utility class for initializing and constructing class objects in the Clarity interpreter.
 */
public class ClassInitUtil {

    /**
     * Initializes a class by its name, passing the provided constructor parameters and context.
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *     <li>Sets the current class name in the context.</li>
     *     <li>Retrieves the class definition from the context and checks for its existence.</li>
     *     <li>Handles class inheritance, interpreting inherited class bodies and constructors.</li>
     *     <li>Interprets the body of the target class and executes its constructors.</li>
     * </ul>
     *
     * @param className         The name of the class to be initialized.
     * @param constructorParams The list of constructor parameters to be passed to the class's constructor.
     * @param context           The context in which the class should be initialized, including variables and state.
     * @return The initialized {@link ClassObject} instance.
     * @throws IllegalStateException If the class is not found or if a return statement is encountered in the class body.
     */
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

    /**
     * Initializes a class by its name and constructor parameters using a general context.
     *
     * <p>This is a simplified overload of {@link #initClass(String, List, Context)} that uses the general context
     * from the interpreter.</p>
     *
     * @param className         The name of the class to be initialized.
     * @param constructorParams The list of constructor parameters to be passed to the class's constructor.
     * @return The initialized {@link ClassObject} instance.
     */
    public static ClassObject initClass(final String className, final List<Object> constructorParams) {
        return initClass(className, constructorParams, INTERPRETER.general());
    }
}