package me.kuwg.clarity.interpreter;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.literal.DecimalNode;
import me.kuwg.clarity.ast.nodes.literal.IntegerNode;
import me.kuwg.clarity.ast.nodes.literal.LiteralNode;
import me.kuwg.clarity.ast.nodes.nat.NativeFunctionCallNode;
import me.kuwg.clarity.ast.nodes.variable.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.VariableReferenceNode;
import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.interpreter.definition.FunctionDefinition;
import me.kuwg.clarity.interpreter.definition.VariableDefinition;
import me.kuwg.clarity.interpreter.exceptions.MultipleMainMethodsException;
import me.kuwg.clarity.interpreter.nmh.NativeMethodHandler;
import me.kuwg.clarity.interpreter.types.ReturnValue;

import java.util.ArrayList;
import java.util.List;

import static me.kuwg.clarity.interpreter.types.Null.NULL;

// all for building part
@SuppressWarnings("all")
public class Interpreter {
    private final AST ast;
    private final NativeMethodHandler nmh;
    private final Context general;

    public Interpreter(final AST ast) {
        this.ast = ast;
        this.nmh = new NativeMethodHandler();
        this.general = new Context();
    }

    public int interpret() {

        MainFunctionDeclarationNode main = null;

        for (final ASTNode node : ast.getRoot()) {
            if (node instanceof MainFunctionDeclarationNode) {
                if (main != null) throw new MultipleMainMethodsException();
                main = (MainFunctionDeclarationNode) node;
            }
            if (node instanceof FunctionDeclarationNode) {
                interpretFunctionDeclaration((FunctionDeclarationNode) node, general);
            }
        }

        if (main != null) {
            final Object result = interpretNode(main.getBlock(), general);

            if (result == NULL) {
                return 0;
            }
            else if (!(result instanceof Integer)) {
                System.err.println("[WARNING] Main function does not return an integer, but returns instead: " + result.getClass().getSimpleName());
                return 1;
            }
            else {
                return (int) result;
            }
        } else {
            ast.getRoot().forEach(node -> interpretNode(node, general));
            return 0;
        }

    }

    private Object interpretNode(final ASTNode node, final Context context) {
        if (node instanceof BlockNode) return interpretBlock((BlockNode) node, context);
        if (node instanceof VariableDeclarationNode) return interpretVariableDeclaration((VariableDeclarationNode) node, context);
        if (node instanceof BinaryExpressionNode) return interpretBinaryExpressionNode((BinaryExpressionNode) node, context);
        if (node instanceof NativeFunctionCallNode) return interpretNativeFunctionCall((NativeFunctionCallNode) node, context);
        if (node instanceof IntegerNode) return ((IntegerNode) node).getValue();
        if (node instanceof DecimalNode) return ((DecimalNode) node).getValue();
        if (node instanceof LiteralNode) return ((LiteralNode) node).getValue();
        if (node instanceof VariableReferenceNode) return interpretVariableReference((VariableReferenceNode) node, context);


        throw new UnsupportedOperationException("Unsupported node: " + node.getClass().getSimpleName());
    }

    private Object interpretBlock(final BlockNode block, final Context context) {
        Object result = null;
        for (final ASTNode node : block) {
            result = interpretNode(node, context);
            if (result instanceof ReturnValue) {
                return ((ReturnValue) result).getValue();
            }
        }

        return result;
    }

    private Object interpretVariableDeclaration(final VariableDeclarationNode node, final Context context) {
        context.defineVariable(node.getName(), new VariableDefinition(node.getName(), interpretNode(node.getValue(), context)));
        return null;
    }

    private Object interpretFunctionDeclaration(final FunctionDeclarationNode node, final Context context) {
        context.defineFunction(node.getFunctionName(), new FunctionDefinition(node));
        return null;
    }

    private Object interpretBinaryExpressionNode(final BinaryExpressionNode node, final Context context) {
        // Evaluate left and right nodes
        Object leftValue = interpretNode(node.getLeft(), context);
        Object rightValue = interpretNode(node.getRight(), context);

        // Ensure both values are numbers
        if (leftValue instanceof Number && rightValue instanceof Number) {
            Number leftNumber = (Number) leftValue;
            Number rightNumber = (Number) rightValue;
            String operator = node.getOperator();

            // Determine operand types
            boolean leftIsDouble = leftNumber instanceof Double;
            boolean rightIsDouble = rightNumber instanceof Double;

            // If either operand is a double, use double precision
            if (leftIsDouble || rightIsDouble) {
                double left = leftNumber.doubleValue();
                double right = rightNumber.doubleValue();

                return evaluateDoubleOperation(left, right, operator);
            } else { // Both operands are Integer
                int left = leftNumber.intValue();
                int right = rightNumber.intValue();

                return evaluateIntegerOperation(left, right, operator);
            }
        } else {
            throw new IllegalArgumentException("Invalid operands for binary expression: " + leftValue + " and " + rightValue);
        }
    }

    private Object evaluateDoubleOperation(double left, double right, String operator) {
        switch (operator) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
                return left * right;
            case "/":
                if (right == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return left / right;
            case "%":
                return left % right;
            case "<":
                return left < right;
            case ">":
                return left > right;
            case "<=":
                return left <= right;
            case ">=":
                return left >= right;
            case "==":
                return left == right;
            case "!=":
                return left != right;
            default:
                throw new UnsupportedOperationException("Unsupported operator: " + operator);
        }
    }

    private Object evaluateIntegerOperation(int left, int right, String operator) {
        switch (operator) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
                return left * right;
            case "/":
                if (right == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return left / right;
            case "%":
                return left % right;
            case "<":
                return left < right;
            case ">":
                return left > right;
            case "<=":
                return left <= right;
            case ">=":
                return left >= right;
            case "==":
                return left == right;
            case "!=":
                return left != right;
            default:
                throw new UnsupportedOperationException("Unsupported operator: " + operator);
        }
    }

    private Object interpretNativeFunctionCall(final NativeFunctionCallNode node, final Context context) {

        final List<Object> params = new ArrayList<>();

        for (final ASTNode param : node.getParams()) {
            params.add(interpretNode(param, context));
        }

        return nmh.call(node.getName(), params);
    }

    private Object interpretVariableReference(final VariableReferenceNode node, final Context context) {
        final Object ret = context.getVariable(node.getName());
        if (ret == NULL) {
            throw new IllegalStateException("Referencing a non-created variable: " + node.getName());
        }
        return ret;
    }
}
