package me.kuwg.clarity.cir.compiler;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.block.ReturnNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.call.FunctionCallNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.call.NativeFunctionCallNode;
import me.kuwg.clarity.ast.nodes.literal.DecimalNode;
import me.kuwg.clarity.ast.nodes.literal.IntegerNode;
import me.kuwg.clarity.ast.nodes.literal.LiteralNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.get.VariableReferenceNode;

import java.util.List;
import java.util.Stack;
import java.util.StringJoiner;

public class CIRCompiler {
    private final AST ast;
    private final StringBuilder irBuilder = new StringBuilder();
    private int tempVarCounter = 0;
    private final Stack<String> tempVarStack = new Stack<>();

    public CIRCompiler(final AST ast) {
        this.ast = ast;
    }

    public String compile() {
        generateIRFromAST(ast.getRoot());
        return irBuilder.toString();
    }

    private void generateIRFromAST(ASTNode node) {
        if (node instanceof BlockNode) {
            generateIRFromBlock((BlockNode) node);
        } else if (node instanceof VariableDeclarationNode) {
            generateIRFromVariableDeclaration((VariableDeclarationNode) node);
        } else if (node instanceof FunctionDeclarationNode) {
            generateIRFromFunctionDeclaration((FunctionDeclarationNode) node);
        } else if (node instanceof NativeFunctionCallNode) {
            generateIRFromNativeFunctionCall((NativeFunctionCallNode) node);
        } else if (node instanceof FunctionCallNode) {
            generateIRFromFunctionCall((FunctionCallNode) node);
        } else if (node instanceof BinaryExpressionNode) {
            generateIRFromBinaryExpression((BinaryExpressionNode) node);
        } else if (node instanceof IntegerNode) {
            generateIRFromIntegerNode((IntegerNode) node);
        } else if (node instanceof DecimalNode) {
            generateIRFromDecimalNode((DecimalNode) node);
        } else if (node instanceof LiteralNode) {
            generateIRFromLiteralNode((LiteralNode) node);
        } else if (node instanceof VariableReferenceNode) {
            generateIRFromVariableReference((VariableReferenceNode) node);
        } else if (node instanceof ReturnNode) {
            generateIRFromReturn((ReturnNode) node);
        } else {
            throw new UnsupportedOperationException("Unsupported node: " + node);
        }
    }

    private void generateIRFromBlock(BlockNode block) {
        for (ASTNode child : block.getChildren()) {
            generateIRFromAST(child);
        }
    }

    private void generateIRFromVariableDeclaration(VariableDeclarationNode node) {
        if (node.getValue() != null) {
            generateIRFromAST(node.getValue());
            irBuilder.append("STORE ").append(node.getName()).append("\n");
        } else {
            irBuilder.append("DECLARE ").append(node.getName()).append("\n");
        }
    }

    private void generateIRFromFunctionDeclaration(FunctionDeclarationNode node) {
        clearTempVars();
        if (node instanceof MainFunctionDeclarationNode) {
            irBuilder.append("FUNC main:\n");
        } else {
            irBuilder.append("FUNC ").append(node.getFunctionName()).append(":\n");
        }
        generateIRFromAST(node.getBlock());
        irBuilder.append("STOP\n\n");
    }

    private void generateIRFromNativeFunctionCall(NativeFunctionCallNode node) {
        StringJoiner joiner = new StringJoiner(" ");
        List<ASTNode> params = node.getParams();
        for (ASTNode param : params) {
            generateIRFromAST(param);
            joiner.add(tempVarStack.peek());
        }
        irBuilder.append("SYSCALL").append(" ").append(node.getName()).append(" ")
                .append(joiner).append("\n");
    }

    private void generateIRFromFunctionCall(FunctionCallNode node) {
        StringJoiner joiner = new StringJoiner(" ");
        List<ASTNode> params = node.getParams();
        for (ASTNode param : params) {
            generateIRFromAST(param);
            joiner.add(tempVarStack.peek());  // Use the last temp variable used
        }
        irBuilder.append("CALL ").append(node.getFunctionName()).append(" ")
                .append(joiner).append("\n");
    }

    private void generateIRFromBinaryExpression(BinaryExpressionNode node) {
        generateIRFromAST(node.getLeft());
        String leftTempVar = tempVarStack.peek(); // Get the last used temp variable

        generateIRFromAST(node.getRight());
        String rightTempVar = tempVarStack.peek(); // Get the last used temp variable

        String resultTempVar = generateTempVar();
        irBuilder.append("OP ").append(node.getOperator())
                .append(" ").append(leftTempVar)
                .append(", ").append(rightTempVar)
                .append(", ").append(resultTempVar)
                .append("\n");

        tempVarStack.push(resultTempVar); // Push the result temp variable onto the stack
    }

    private void generateIRFromIntegerNode(IntegerNode node) {
        String tempVar = generateTempVar();
        irBuilder.append("CONST ").append(node.getValue()).append("\n");
        tempVarStack.push(tempVar); // Track the last temp variable used
    }

    private void generateIRFromDecimalNode(DecimalNode node) {
        String tempVar = generateTempVar();
        irBuilder.append("CONST ").append(node.getValue()).append("\n");
        tempVarStack.push(tempVar); // Track the last temp variable used
    }

    private void generateIRFromLiteralNode(LiteralNode node) {
        String tempVar = generateTempVar();
        irBuilder.append("CONST \"").append(node.getValue()).append("\"\n");
        tempVarStack.push(tempVar); // Track the last temp variable used
    }

    private void generateIRFromVariableReference(VariableReferenceNode node) {
        irBuilder.append("LOAD ").append(node.getName()).append("\n");
        tempVarStack.push(node.getName()); // Track the last variable referenced
    }

    private void generateIRFromReturn(ReturnNode node) {
        generateIRFromAST(node.getValue());  // Generate IR for the return value
        irBuilder.append("RETURN ").append(tempVarStack.peek()).append("\n"); // Return the last temp variable
    }

    private void clearTempVars() {
        tempVarStack.clear();
    }

    private String generateTempVar() {
        return "T" + (tempVarCounter++);
    }
}
