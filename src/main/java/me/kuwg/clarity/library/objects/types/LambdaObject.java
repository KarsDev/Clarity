package me.kuwg.clarity.library.objects.types;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.interpreter.context.Context;

import java.util.List;

/**
 * Represents a LambdaObject within the Clarity library.
 * A LambdaObject encapsulates the parameters, the associated block of code,
 * and the execution context in which the lambda is defined. This is used to represent
 * lambda functions or closures in a dynamic interpreter environment.
 */
public final class LambdaObject {

    /**
     * The list of parameters for the lambda. Each parameter is represented as a {@link ParameterNode}.
     */
    private final List<ParameterNode> params;

    /**
     * The block of code (function body) associated with the lambda.
     */
    private final BlockNode block;

    /**
     * The execution context in which this lambda is defined.
     */
    private final Context context;

    /**
     * Constructs a new LambdaObject.
     *
     * @param params  The list of parameters for the lambda.
     * @param block   The block of code associated with the lambda.
     * @param context The execution context in which the lambda is defined.
     */
    public LambdaObject(final List<ParameterNode> params, final BlockNode block, final Context context) {
        this.params = params;
        this.block = block;
        this.context = context;
    }

    /**
     * Returns the block of code associated with this lambda.
     *
     * @return The {@link BlockNode} representing the lambda's code.
     */
    public BlockNode getBlock() {
        return block;
    }

    /**
     * Returns the list of parameters for this lambda.
     *
     * @return A {@link List} of {@link ParameterNode} objects representing the lambda's parameters.
     */
    public List<ParameterNode> getParams() {
        return params;
    }

    /**
     * Returns the execution context associated with this lambda.
     *
     * @return The {@link Context} in which the lambda is defined.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Generates a string representation of the LambdaObject, including its parameters.
     * Each parameter is displayed with its name and whether it is a lambda itself.
     *
     * @return A string representing the LambdaObject in the format "LambdaObject(param1, param2 (lambda), ...)".
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LambdaObject(");
        for (int i = 0; i < params.size(); i++) {
            final ParameterNode param = params.get(i);
            builder.append(param.getName())
                    .append(param.isLambda() ? " (lambda)" : "")
                    .append(i + 1 < params.size() ? ", " : "");
        }
        builder.append(")");
        return builder.toString();
    }
}