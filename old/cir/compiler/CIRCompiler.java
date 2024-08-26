package me.kuwg.clarity.cir.compiler;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.block.ReturnNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassInstantiationNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.call.FunctionCallNode;
import me.kuwg.clarity.ast.nodes.function.call.LocalFunctionCallNode;
import me.kuwg.clarity.ast.nodes.function.call.ObjectFunctionCallNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.call.NativeFunctionCallNode;
import me.kuwg.clarity.ast.nodes.literal.DecimalNode;
import me.kuwg.clarity.ast.nodes.literal.IntegerNode;
import me.kuwg.clarity.ast.nodes.literal.LiteralNode;
import me.kuwg.clarity.ast.nodes.reference.ContextReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.get.LocalVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.ObjectVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.VariableReferenceNode;

import java.util.List;
import java.util.Stack;
import java.util.StringJoiner;

import static me.kuwg.clarity.cir.interpreter.CIRCommand.*;

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
        } else if (node instanceof ClassDeclarationNode) {
            generateIRFromClassDeclaration((ClassDeclarationNode) node);
        } else if (node instanceof ContextReferenceNode) {
            generateIRFromContextReference((ContextReferenceNode) node);
        } else if (node instanceof VariableReassignmentNode) {
            generateIRFromVariableReassignment((VariableReassignmentNode) node);
        } else if (node instanceof LocalFunctionCallNode) {
            generateIRFromLocalFunctionCall((LocalFunctionCallNode) node);
        } else if (node instanceof LocalVariableReferenceNode) {
            generateIRFromLocalVariableReference((LocalVariableReferenceNode) node);
        } else if (node instanceof ClassInstantiationNode) {
            generateIRFromClassInstantiation((ClassInstantiationNode) node);
        }  else if (node instanceof ObjectFunctionCallNode) {
            generateIRFromObjectFunctionCall((ObjectFunctionCallNode) node);
        } else if (node instanceof ObjectVariableReferenceNode) {
            generateIRFromObjectVariableReference((ObjectVariableReferenceNode) node);
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
            irBuilder.append(STORE).append(" ").append(node.getName()).append("\n");
        } else {
            irBuilder.append(DECLARE).append(" ").append(node.getName()).append("\n");
        }
    }

    private void generateIRFromFunctionDeclaration(FunctionDeclarationNode node) {
        clearTempVars();
        if (node instanceof MainFunctionDeclarationNode) {
            irBuilder.append(FUNC).append(" main\n");
        } else {
            irBuilder.append(FUNC).append(" ").append(node.getFunctionName()).append("\n");
        }
        generateIRFromAST(node.getBlock());
        irBuilder.append(STOP).append("\n\n");
    }

    private void generateIRFromNativeFunctionCall(NativeFunctionCallNode node) {
        StringJoiner joiner = new StringJoiner(" ");
        List<ASTNode> params = node.getParams();
        for (ASTNode param : params) {
            generateIRFromAST(param);
            joiner.add(tempVarStack.peek());
        }
        irBuilder.append(SYSCALL).append(" ").append(node.getName()).append(" ")
                .append(joiner).append("\n");
    }

    private void generateIRFromFunctionCall(FunctionCallNode node) {
        StringJoiner joiner = new StringJoiner(" ");
        List<ASTNode> params = node.getParams();
        for (ASTNode param : params) {
            generateIRFromAST(param);
            joiner.add(tempVarStack.peek());  // Use the last temp variable used
        }
        irBuilder.append(CALL).append(" ").append(node.getFunctionName()).append(" ")
                .append(joiner).append("\n");
    }

    private void generateIRFromBinaryExpression(BinaryExpressionNode node) {
        generateIRFromAST(node.getLeft());
        String leftTempVar = tempVarStack.peek(); // Get the last used temp variable

        generateIRFromAST(node.getRight());
        String rightTempVar = tempVarStack.peek(); // Get the last used temp variable

        String resultTempVar = generateTempVar();
        irBuilder.append(OP).append(" ").append(node.getOperator())
                .append(" ").append(leftTempVar)
                .append(" ").append(rightTempVar)
                .append(" ").append(resultTempVar)
                .append("\n");

        tempVarStack.push(resultTempVar);
    }

    private void generateIRFromIntegerNode(IntegerNode node) {
        String tempVar = generateTempVar();
        irBuilder.append(CONST).append(" ").append(node.getValue()).append("\n");
        tempVarStack.push(tempVar); // Track the last temp variable used
    }

    private void generateIRFromDecimalNode(DecimalNode node) {
        String tempVar = generateTempVar();
        irBuilder.append(CONST).append(" ").append(node.getValue()).append("\n");
        tempVarStack.push(tempVar); // Track the last temp variable used
    }

    private void generateIRFromLiteralNode(LiteralNode node) {
        String tempVar = generateTempVar();
        irBuilder.append(CONST).append(" ").append(node.getValue()).append("\n");
        tempVarStack.push(tempVar);
    }

    private void generateIRFromVariableReference(VariableReferenceNode node) {
        irBuilder.append(LOAD).append(" ").append(node.getName()).append("\n");
        tempVarStack.push(node.getName());
    }

    private void generateIRFromReturn(ReturnNode node) {
        generateIRFromAST(node.getValue());
        irBuilder.append(RETURN).append(" ").append(tempVarStack.peek()).append("\n");
    }

    private void generateIRFromClassDeclaration(ClassDeclarationNode node) {
        irBuilder.append(CLASS).append(" ").append(node.getName()).append("\n");

        if (node.getConstructor() != null) {
            generateIRFromFunctionDeclaration(node.getConstructor());
        }

        generateIRFromAST(node.getBody());

        irBuilder.append(CLASSEND).append("\n\n");
    }

    private void generateIRFromContextReference(ContextReferenceNode node) {
        irBuilder.append(CONTEXT).append(" ").append(node.getType()).append(" ");
    }

    private void generateIRFromVariableReassignment(VariableReassignmentNode node) {
        generateIRFromAST(node.getValue());
        irBuilder.append(STORE).append(" ").append(node.getName()).append("\n");
    }

    private void generateIRFromLocalFunctionCall(LocalFunctionCallNode node) {
        StringJoiner joiner = new StringJoiner(" ");
        List<ASTNode> params = node.getParams();

        for (ASTNode param : params) {
            generateIRFromAST(param);
            joiner.add(tempVarStack.peek());
        }

        irBuilder.append(CALL).append(" ").append(node.getFunctionName()).append(" ")
                .append(joiner).append("\n");

        String resultTempVar = generateTempVar();
        tempVarStack.push(resultTempVar);
    }

    private void generateIRFromLocalVariableReference(LocalVariableReferenceNode node) {
        irBuilder.append(LOAD).append(" ").append(node.getName()).append("\n");
        tempVarStack.push(node.getName());
    }

    private void generateIRFromClassInstantiation(ClassInstantiationNode node) {
        StringJoiner joiner = new StringJoiner(" ");
        List<ASTNode> params = node.getParams();

        if (params != null) {
            for (ASTNode param : params) {
                generateIRFromAST(param);
                joiner.add(tempVarStack.peek());
            }
        }

        irBuilder.append(NEW).append(" ").append(node.getName()).append(" ")
                .append(joiner).append("\n");

        String resultTempVar = generateTempVar();
        tempVarStack.push(resultTempVar);
    }

    private void generateIRFromObjectFunctionCall(ObjectFunctionCallNode node) {
        irBuilder.append(LOAD).append(" ").append(node.getCaller()).append("\n");
        tempVarStack.push(node.getCaller());

        StringJoiner joiner = new StringJoiner(" ");
        List<ASTNode> params = node.getParams();
        for (ASTNode param : params) {
            generateIRFromAST(param);
            joiner.add(tempVarStack.peek());
        }

        irBuilder.append(CALL).append(" ").append(node.getCaller()).append("#").append(node.getCalled()).append(" ")
                .append(joiner).append("\n");

        String resultTempVar = generateTempVar();
        tempVarStack.push(resultTempVar);
    }

    private void generateIRFromObjectVariableReference(ObjectVariableReferenceNode node) {
        irBuilder.append(LOAD).append(" ").append(node.getCaller()).append("\n");
        tempVarStack.push(node.getCaller());

        String tempVar = generateTempVar();
        irBuilder.append(GET).append(" ").append(tempVar).append(" ").append(node.getCaller()).append("#").append(node.getCalled()).append("\n");

        tempVarStack.push(tempVar);
    }

    private void clearTempVars() {
        tempVarStack.clear();
    }

    private String generateTempVar() {
        return String.valueOf(tempVarCounter++);
    }
}
