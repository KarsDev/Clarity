package me.kuwg.clarity.optimizer;

import me.kuwg.clarity.Clarity;
import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.block.StaticBlockNode;
import me.kuwg.clarity.ast.nodes.block.TryExceptBlock;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.literal.*;
import me.kuwg.clarity.ast.nodes.statements.*;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableDeclarationNode;
import me.kuwg.clarity.register.Register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ASTOptimizer {
    private final AST ast;

    private final Map<String, ASTNode> commonSubexpressions;

    public ASTOptimizer(final AST ast) {
        this.ast = ast;
        this.commonSubexpressions = new HashMap<>();
    }

    public AST optimize() {
        if (Clarity.INFORMATION.getOption("optimize")) ast.getRoot().getChildren().replaceAll(this::optimizeNode);
        return ast;
    }

    private ASTNode optimizeNode(final ASTNode node) {
        if (node instanceof BinaryExpressionNode) return optimizeBinaryExpression((BinaryExpressionNode) node);
        if (node instanceof VariableDeclarationNode) return optimizeVariableDeclaration((VariableDeclarationNode) node);
        if (node instanceof FunctionDeclarationNode) return optimizeFunctionDeclaration((FunctionDeclarationNode) node);
        if (node instanceof BlockNode) return optimizeBlock((BlockNode) node);
        if (node instanceof TryExceptBlock) return optimizeTryExcept((TryExceptBlock) node);
        if (node instanceof WhileNode) return optimizeWhileNode((WhileNode) node);
        if (node instanceof IfNode) return optimizeIfNode((IfNode) node);
        if (node instanceof ForNode) return optimizeForNode((ForNode) node);
        if (node instanceof SelectNode) return optimizeSelectNode((SelectNode) node);
        if (node instanceof AssertNode) return optimizeAssertNode((AssertNode) node);
        if (node instanceof StaticBlockNode) return optimizeStaticBlock((StaticBlockNode) node);

        return node;
    }

    private ASTNode optimizeBinaryExpression(final BinaryExpressionNode node) {
        final ASTNode left = optimizeNode(node.getLeft());
        final String op = node.getOperator();
        final ASTNode right = optimizeNode(node.getRight());

        final String exprKey = generateExpressionKey(left, op, right);

        if (commonSubexpressions.containsKey(exprKey)) {
            return commonSubexpressions.get(exprKey);
        }

        final ASTNode optimizedNode;
        if (left instanceof AbstractNumberNode && right instanceof AbstractNumberNode) {
            optimizedNode = optimizeNumberOperation(left, op, right);
        } else if (left instanceof LiteralNode || right instanceof LiteralNode) {
            optimizedNode = optimizeStringOperation(left, op, right);
        } else if (left instanceof BooleanNode && right instanceof BooleanNode) {
            optimizedNode = optimizeBooleanOperation((BooleanNode) left, op, (BooleanNode) right);
        } else if (left instanceof NullNode || right instanceof NullNode) {
            optimizedNode = handleNullComparison(left, op, right, left.getLine());
        } else {
            optimizedNode = node;
        }

        commonSubexpressions.put(exprKey, optimizedNode);
        return optimizedNode;
    }

    private String generateExpressionKey(final ASTNode left, final String op, final ASTNode right) {
        return left.toString() + " " + op + " " + right.toString();
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

        if (leftValue instanceof Long && rightValue instanceof Long) {
            final long li = leftValue.longValue();
            final long ri = rightValue.longValue();
            final long result;
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
            final int result;
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
            case "||":
                return new BooleanNode(left.getValue() || right.getValue());
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

    private FunctionDeclarationNode optimizeFunctionDeclaration(final FunctionDeclarationNode node) {
        node.getBlock().getChildren().replaceAll(this::optimizeNode);
        return node;
    }

    private BlockNode optimizeBlock(final BlockNode node) {
        node.getChildren().replaceAll(this::optimizeNode);
        return node;
    }

    private TryExceptBlock optimizeTryExcept(final TryExceptBlock node) {
        node.getTryBlock().getChildren().replaceAll(this::optimizeNode);
        node.getExceptBlock().getChildren().replaceAll(this::optimizeNode);
        return node;
    }

    private WhileNode optimizeWhileNode(final WhileNode node) {
        final ASTNode optimizedCondition = optimizeNode(node.getCondition());
        final BlockNode optimizedBlock = (BlockNode) optimizeNode(node.getBlock());
        return new WhileNode(optimizedCondition, optimizedBlock);
    }

    private IfNode optimizeIfNode(final IfNode node) {
        final ASTNode optimizedCondition = optimizeNode(node.getCondition());
        final BlockNode optimizedIfBlock = (BlockNode) optimizeNode(node.getIfBlock());

        final List<IfNode> optimizedElseIfs = new ArrayList<>();
        for (final IfNode elseIf : node.getElseIfStatements()) {
            optimizedElseIfs.add((IfNode) optimizeNode(elseIf));
        }
        final BlockNode optimizedElseBlock = node.getElseBlock() != null ? (BlockNode) optimizeNode(node.getElseBlock()) : null;

        final IfNode optimizedIfNode = new IfNode(optimizedCondition, optimizedIfBlock);
        optimizedElseIfs.forEach(optimizedIfNode::addElseIfStatement);
        optimizedIfNode.setElseBlock(optimizedElseBlock);

        return optimizedIfNode;
    }

    private ForNode optimizeForNode(final ForNode node) {
        final ASTNode optimizedDeclaration = optimizeNode(node.getDeclaration());
        final ASTNode optimizedCondition = optimizeNode(node.getCondition());
        final ASTNode optimizedIncrementation = optimizeNode(node.getIncrementation());
        final BlockNode optimizedBlock = (BlockNode) optimizeNode(node.getBlock());
        return new ForNode(optimizedDeclaration, optimizedCondition, optimizedIncrementation, optimizedBlock);
    }

    private SelectNode optimizeSelectNode(final SelectNode node) {
        final ASTNode optimizedCondition = optimizeNode(node.getCondition());
        final List<SelectNode.WhenNode> optimizedCases = new ArrayList<>();
        for (final SelectNode.WhenNode whenNode : node.getCases()) {
            final ASTNode optimizedWhenExpression = optimizeNode(whenNode.getWhenExpression());
            final BlockNode optimizedBlock = (BlockNode) optimizeNode(whenNode.getBlock());
            optimizedCases.add(new SelectNode.WhenNode(optimizedWhenExpression, optimizedBlock));
        }
        final BlockNode optimizedDefaultBlock = node.getDefaultBlock() != null ? (BlockNode) optimizeNode(node.getDefaultBlock()) : null;
        return new SelectNode(optimizedCondition, optimizedCases, optimizedDefaultBlock);
    }

    private AssertNode optimizeAssertNode(final AssertNode node) {
        final ASTNode optimizedCondition = optimizeNode(node.getCondition());
        final ASTNode optimizedOrElse = node.getOrElse() != null ? optimizeNode(node.getOrElse()) : null;

        return new AssertNode(optimizedCondition, optimizedOrElse);
    }

    private StaticBlockNode optimizeStaticBlock(final StaticBlockNode node) {
        node.getBlock().getChildren().replaceAll(this::optimizeNode);
        return node;
    }

}