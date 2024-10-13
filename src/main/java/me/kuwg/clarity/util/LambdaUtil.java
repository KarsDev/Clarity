package me.kuwg.clarity.util;

import me.kuwg.clarity.Clarity;
import me.kuwg.clarity.ast.nodes.block.LambdaBlockNode;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.interpreter.Interpreter;
import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.interpreter.definition.VariableDefinition;
import me.kuwg.clarity.library.objects.VoidObject;
import me.kuwg.clarity.library.objects.types.LambdaObject;
import me.kuwg.clarity.register.Register;

import java.util.Collections;
import java.util.List;

import static me.kuwg.clarity.library.objects.VoidObject.VOID_OBJECT;

/**
 * Utility class for handling lambda function calls in the Clarity interpreter.
 */
public class LambdaUtil {

    /**
     * Calls a lambda function with the provided parameters and context.
     *
     * <p>This method creates a new context for the lambda and defines the parameters as variables within that context.
     * It validates that each parameter matches the expected type, throwing an exception if a parameter expected to be
     * a lambda is not.</p>
     *
     * @param object  The {@link LambdaObject} representing the lambda function to call.
     * @param params  The list of parameters to pass to the lambda function.
     * @param context The context in which the lambda function is called, containing variable definitions and state.
     * @return The result of executing the lambda's block, or a {@link VoidObject} if an exception occurs.
     */
    public static Object callLambda(final LambdaObject object, final List<Object> params, final Context context) {
        final Context lambdaContext = new Context(context);

        for (int i = 0; i < params.size(); i++) {
            final Object obj = params.get(i);
            final ParameterNode param = object.getParams().get(i);
            if (param.isLambda() && !(obj instanceof LambdaBlockNode)) {
                Register.throwException("Expected lambda but found " + Interpreter.getParams(Collections.singletonList(params.get(i))));
                return VOID_OBJECT;
            }
            lambdaContext.defineVariable(param.getName(), new VariableDefinition(param.getName(), null, obj, false, false, false));
        }

        return Clarity.INTERPRETER.interpretBlock(object.getBlock(), lambdaContext);
    }

    /**
     * Calls a lambda function with the provided parameters using the general context.
     *
     * <p>This is a simplified overload of {@link #callLambda(LambdaObject, List, Context)} that uses the general
     * context from the interpreter.</p>
     *
     * @param object  The {@link LambdaObject} representing the lambda function to call.
     * @param params  The list of parameters to pass to the lambda function.
     * @return The result of executing the lambda's block, or a {@link VoidObject} if an exception occurs.
     */
    public static Object callLambda(final LambdaObject object, final List<Object> params) {
        return callLambda(object, params, Clarity.INTERPRETER.general());
    }
}