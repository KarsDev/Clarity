package me.kuwg.clarity.library.objects.types;

import me.kuwg.clarity.Clarity;
import me.kuwg.clarity.library.objects.ObjectType;
import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.interpreter.definition.FunctionDefinition;

/**
 * Represents a class object within the Clarity framework. A ClassObject encapsulates
 * the name, inheritance hierarchy, and execution context of a class. It provides
 * mechanisms for checking if a class is an instance of another, as well as custom
 * string representation logic via the `toString` method. This class is used to simulate
 * and manipulate object-oriented class behavior in a dynamic interpreter environment.
 */
public class ClassObject {

    /**
     * The name of the class this object represents.
     */
    private final String name;

    /**
     * The inherited class object, if any. Represents the superclass of this class,
     * forming part of an inheritance chain.
     */
    private final ClassObject inherited;

    /**
     * The execution context associated with this class object. The context holds
     * the environment in which the class operates, including any function definitions
     * and variables available to the class.
     */
    private final Context context;

    /**
     * Constructs a new ClassObject.
     *
     * @param name      The name of the class.
     * @param inherited The superclass (inherited ClassObject) if applicable, or null if there is no superclass.
     * @param context   The execution context associated with this class.
     */
    public ClassObject(final String name, final ClassObject inherited, final Context context) {
        this.name = name;
        this.inherited = inherited;
        this.context = context;
    }

    /**
     * Returns the name of this class object.
     *
     * @return The name of the class as a String.
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the inherited class object (the superclass).
     *
     * @return The inherited ClassObject, or null if no class is inherited.
     */
    public final ClassObject getInherited() {
        return inherited;
    }

    /**
     * Returns the context associated with this class object. The context is an execution
     * environment that may contain function definitions, variables, and other relevant
     * data for class operations.
     *
     * @return The Context object associated with this class.
     */
    public final Context getContext() {
        return context;
    }

    /**
     * Generates a string representation of this ClassObject. If the class defines a "print"
     * function in its context, it invokes that function and returns the resulting string.
     * Otherwise, it returns a default string containing the class name.
     *
     * @return A string representation of the class. Either the result of the "print" function or
     *         a default formatted string "ClassObject@name".
     */
    @Override
    public String toString() {
        // Retrieves the "print" function from the context with 0 arguments.
        final ObjectType type = context.getFunction("print", 0);

        // Check if the retrieved object is a FunctionDefinition instance.
        if (type instanceof FunctionDefinition) {
            // Interpret the function's block and retrieve the result.
            final Object ret = Clarity.INTERPRETER.interpretNode(((FunctionDefinition) type).getBlock(), context);

            // If the interpreted result is a string, return it.
            if (ret instanceof String) {
                return (String) ret;
            }
        }

        // Default string representation if no "print" function is defined.
        return "ClassObject@" + name;
    }

    /**
     * Checks whether this class object or any of its inherited classes matches the specified class name.
     * It starts by comparing the current class name, and if no match is found, it continues up the
     * inheritance chain recursively until either a match is found or there are no more inherited classes.
     *
     * @param value The name of the class to check against.
     * @return true if this class or one of its inherited classes matches the given name, otherwise false.
     */
    public boolean isInstance(final String value) {
        // Directly compares the current class name with the provided value.
        if (name.equals(value)) return true;

        // Traverse up the inheritance hierarchy.
        ClassObject inherited = this.inherited;
        while (true) {
            if (inherited == null) return false;  // No more classes to check.
            if (inherited.getName().equals(value)) return true;  // Match found in inheritance chain.
            inherited = inherited.getInherited();  // Move up the hierarchy.
        }
    }
}