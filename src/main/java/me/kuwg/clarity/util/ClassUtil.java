package me.kuwg.clarity.util;

import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.interpreter.definition.ClassDefinition;
import me.kuwg.clarity.interpreter.definition.EnumClassDefinition;
import me.kuwg.clarity.interpreter.definition.FunctionDefinition;
import me.kuwg.clarity.library.objects.types.ClassObject;
import me.kuwg.clarity.library.objects.ObjectType;

import java.util.Arrays;

import static me.kuwg.clarity.Clarity.INTERPRETER;
import static me.kuwg.clarity.library.objects.VoidObject.VOID_OBJECT;

/**
 * Utility class for initializing and constructing class objects in the Clarity interpreter.
 */
public class ClassUtil {

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
     * @param context           The context in which the class should be initialized, including variables and state.
     * @param constructorParams The constructors' parameters to be passed to the class's constructor.
     * @return The initialized {@link ClassObject} instance.
     * @throws IllegalStateException If the class is not found or if a return statement is encountered in the class body.
     */
    public static ClassObject initClass(final String className, final Context context, final Object... constructorParams) {
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
            inheritedObject = INTERPRETER.interpretConstructors(inheritedObject, inheritedConstructors, Arrays.asList(constructorParams), classContext, inheritedClass.getName());
            classContext.mergeContext(inheritedObject.getContext());

            currentDefinition = inheritedClass;
        }

        final Object val = INTERPRETER.interpretBlock(definition.getBody(), classContext);

        if (val != VOID_OBJECT) INTERPRETER.except("Return in class body", -404);

        final ClassObject result = INTERPRETER.interpretConstructors(inheritedObject, definition.getConstructors(), Arrays.asList(constructorParams), classContext, className);
        context.setCurrentClassName(null);
        return result;
    }

    /**
     * Initializes a class by its name and constructor parameters using a general context.
     *
     * <p>This is a simplified overload of {@link #initClass(String, Object[], Context)} that uses the general context
     * from the interpreter.</p>
     *
     * @param className         The name of the class to be initialized.
     * @param constructorParams The list of constructor parameters to be passed to the class's constructor.
     * @return The initialized {@link ClassObject} instance.
     */
    public static ClassObject initClass(final String className, final Object... constructorParams) {
        return initClass(className, constructorParams, INTERPRETER.general());
    }

    /**
     * Retrieves the enum value for a given enum class and value name from the specified context.
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *     <li>Fetches the class type by name from the provided context.</li>
     *     <li>Checks if the class type is an {@link EnumClassDefinition}.</li>
     *     <li>Returns the corresponding enum value by name.</li>
     * </ul>
     *
     * @param className      The name of the enum class.
     * @param enumValueName  The name of the enum value to retrieve.
     * @param context        The context from which to retrieve the enum class definition.
     * @return The {@link EnumClassDefinition.EnumValue} corresponding to the given enum value name.
     * @throws IllegalStateException If the provided class is not an enum or if the class is not found.
     */
    public static EnumClassDefinition.EnumValue getEnumValue(final String className, final String enumValueName, final Context context) {
        final ObjectType rawType = context.getClass(className);
        if (!(rawType instanceof EnumClassDefinition)) {
            throw new IllegalStateException("The provided class is not an enum: " + className + ", instead it is: " + rawType.getClass().getSimpleName());
        }

        final EnumClassDefinition envm = (EnumClassDefinition) rawType;
        return envm.getValue(enumValueName);
    }

    /**
     * Retrieves the enum value for a given enum class and value name using the general context.
     *
     * <p>This is a simplified overload of {@link #getEnumValue(String, String, Context)} that uses the general context
     * from the interpreter.</p>
     *
     * @param className      The name of the enum class.
     * @param enumValueName  The name of the enum value to retrieve.
     * @return The {@link EnumClassDefinition.EnumValue} corresponding to the given enum value name.
     * @throws IllegalStateException If the provided class is not an enum or if the class is not found.
     */
    public static EnumClassDefinition.EnumValue getEnumValue(final String className, final String enumValueName) {
        return getEnumValue(className, enumValueName, INTERPRETER.general());
    }
}