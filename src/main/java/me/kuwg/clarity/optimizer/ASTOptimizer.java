package me.kuwg.clarity.optimizer;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.literal.*;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableDeclarationNode;
import me.kuwg.clarity.register.Register;

public class ASTOptimizer {
    private final AST ast;

    public ASTOptimizer(final AST ast) {
        this.ast = ast;
    }

    public AST optimize() {
        ast.getRoot().getChildren().replaceAll(this::optimizeNode);
        return ast;
    }

    private ASTNode optimizeNode(final ASTNode node) {
        if (node instanceof BinaryExpressionNode) return optimizeBinaryExpression((BinaryExpressionNode) node);
        if (node instanceof VariableDeclarationNode) return optimizeVariableDeclaration((VariableDeclarationNode) node);
        if (node instanceof FunctionDeclarationNode) return optimizeFunctionDeclaration((FunctionDeclarationNode) node);
        if (node instanceof BlockNode) return optimizeBlock((BlockNode) node);
        return node;
    }

    private ASTNode optimizeBinaryExpression(final BinaryExpressionNode node) {
        final ASTNode left = optimizeNode(node.getLeft());
        final String op = node.getOperator();
        final ASTNode right = optimizeNode(node.getRight());

        if (left instanceof AbstractNumberNode && right instanceof AbstractNumberNode) {
            return optimizeNumberOperation(left, op, right);
        } else if (left instanceof LiteralNode || right instanceof LiteralNode) {
            return optimizeStringOperation(left, op, right);
        } else if (left instanceof BooleanNode && right instanceof BooleanNode) {
            return optimizeBooleanOperation((BooleanNode) left, op, (BooleanNode) right);
        } else if (left instanceof NullNode || right instanceof NullNode) {
            return handleNullComparison(left, op, right, left.getLine());
        }

        return node;
    }

    private ASTNode optimizeVariableDeclaration(final VariableDeclarationNode node) {
        return new VariableDeclarationNode(node.getName(), node.getTypeDefault(), optimizeNode(node.getValue()), node.isConstant(), node.isStatic(), node.isLocal());
    }

    private Object parseValue(final ASTNode node) {
        if (node instanceof IntegerNode) return ((IntegerNode) node).getValue();
        if (node instanceof DecimalNode) return ((DecimalNode) node).getValue();
        if (node instanceof LiteralNode) return ((LiteralNode) node).getValue();
        return null;
    }

    private ASTNode optimizeNumberOperation(final ASTNode left, final String op, final ASTNode right) {
        final Number leftValue = (Number) parseValue(left);
        final Number rightValue = (Number) parseValue(right);

        if (leftValue instanceof Integer && rightValue instanceof Integer) {
            final int li = leftValue.intValue();
            final int ri = rightValue.intValue();
            int result;
            switch (op) {
                case "+":
                    result = li + ri;
                    break;
                case "-":
                    result = li - ri;
                    break;
                case "*":
                    result = li * ri;
                    break;
                case "/":
                    if (ri == 0) {
                        throw new ArithmeticException("Division by zero");
                    }
                    result = li / ri;
                    break;
                case "%":
                    if (ri == 0) {
                        throw new ArithmeticException("Modulo by zero");
                    }
                    result = li % ri;
                    break;
                case "^":
                    result = (int) Math.pow(li, ri);
                    break;
                case "<":
                    result = li < ri ? 1 : 0;
                    break;
                case ">":
                    result = li > ri ? 1 : 0;
                    break;
                case "<=":
                    result = li <= ri ? 1 : 0;
                    break;
                case ">=":
                    result = li >= ri ? 1 : 0;
                    break;
                case "==":
                    result = li == ri ? 1 : 0;
                    break;
                case "!=":
                    result = li != ri ? 1 : 0;
                    break;
                case ">>":
                    result = li >> ri;
                    break;
                case "<<":
                    result = li << ri;
                    break;
                case "&":
                    result = li & ri;
                    break;
                case "|":
                    result = li | ri;
                    break;
                case "^^":
                    result = li ^ ri;
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown operation: " + op);
            }
            return new IntegerNode(result);
        } else {
            final double ld = leftValue.doubleValue();
            final double rd = rightValue.doubleValue();
            int result;
            switch (op) {
                case "+":
                    result = (int) (ld + rd);
                    break;
                case "-":
                    result = (int) (ld - rd);
                    break;
                case "*":
                    result = (int) (ld * rd);
                    break;
                case "/":
                    if (rd == 0) {
                        throw new ArithmeticException("Division by zero");
                    }
                    result = (int) (ld / rd);
                    break;
                case "%":
                    if (rd == 0) {
                        throw new ArithmeticException("Modulo by zero");
                    }
                    result = (int) (ld % rd);
                    break;
                case "^":
                    result = (int) Math.pow(ld, rd);
                    break;
                case "<":
                    result = ld < rd ? 1 : 0;
                    break;
                case ">":
                    result = ld > rd ? 1 : 0;
                    break;
                case "<=":
                    result = ld <= rd ? 1 : 0;
                    break;
                case ">=":
                    result = ld >= rd ? 1 : 0;
                    break;
                case "==":
                    result = ld == rd ? 1 : 0;
                    break;
                case "!=":
                    result = ld != rd ? 1 : 0;
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown operation: " + op);
            }
            return new DecimalNode(result);
        }
    }

    private ASTNode optimizeStringOperation(final ASTNode left, final String op, final ASTNode right) {
        final String leftValue = String.valueOf(parseValue(left));
        final String rightValue = String.valueOf(parseValue(right));
        switch (op) {
            case "+":
                return new LiteralNode(leftValue + rightValue);
            case "==":
                return new BooleanNode(leftValue.equals(rightValue));
            case "!=":
                return new BooleanNode(!leftValue.equals(rightValue));
        }
        Register.throwException("Operator " + op + " is not supported for string operands.", left.getLine());
        return null;
    }

    private ASTNode optimizeBooleanOperation(final BooleanNode left, final String op, final BooleanNode right) {
        switch (op) {
            case "==":
                return new BooleanNode(left.getValue() == right.getValue());
            case "!=":
                return new BooleanNode(left.getValue() != right.getValue());
            case "^":
                return new BooleanNode(left.getValue() ^ right.getValue());
        }
        Register.throwException("Operator " + op + " is not supported for boolean operands.", left.getLine());
        return null;
    }

    private ASTNode handleNullComparison(final ASTNode left, final String op, final ASTNode right, final int line) {
        final Object leftValue = parseValue(left);
        final Object rightValue = parseValue(right);

        switch (op) {
            case "==":
                return new BooleanNode(leftValue == rightValue);
            case "!=":
                return new BooleanNode(leftValue != rightValue);
        }
        Register.throwException("Only operators available for null are '==' and '!='", line);
        return null;
    }

    private ASTNode optimizeFunctionDeclaration(final FunctionDeclarationNode node) {
        node.getBlock().getChildren().replaceAll(this::optimizeNode);
        return node;
    }

    private ASTNode optimizeBlock(final BlockNode node) {
        node.getChildren().replaceAll(this::optimizeNode);
        return node;
    }

}
