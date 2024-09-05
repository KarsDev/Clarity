package me.kuwg.clarity.compiler.cir;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.call.DefaultNativeFunctionCallNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.ast.nodes.literal.*;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.get.VariableReferenceNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CIRCompiler {

    private static final boolean IMPLEMENTED = false;

    protected final AST ast;

    protected final File output;
    protected final FileWriter writer;
    protected final List<String> includes;


    protected int tabs;

    public CIRCompiler(final AST ast, final File output) throws IOException {
        this.ast = ast;
        this.output = output;

        if (!output.exists()) {
            //noinspection ResultOfMethodCallIgnored
            output.createNewFile();
        }

        this.writer = new FileWriter(output);
        this.includes = new ArrayList<>();

        this.tabs = 0;
    }

    public void compile() throws IOException {

        if (!IMPLEMENTED) throw new UnsupportedOperationException("Compiling to c++ is still being implemented.");

        final BlockNode root = ast.getRoot();

        for (final ASTNode child : root) {
            compileNode(child);
        }

        writer.close();
        prependIncludes();
    }
    private void prependIncludes() throws IOException {
        final String originalContent = new String(Files.readAllBytes(output.toPath()));

        try (FileWriter writer = new FileWriter(output)) {
            // Write the includes at the start
            for (String include : includes) {
                writer.write("#include " + include + "\n");
            }

            writer.write("\n");

            writer.write(originalContent);
        }
    }

    protected void compileNode(final ASTNode node) throws IOException {
        if (node instanceof BlockNode) {
            compileBlockNode((BlockNode) node);
        } else if (node instanceof VariableDeclarationNode) {
            compileVariableDeclaration((VariableDeclarationNode) node);
        } else if (node instanceof IntegerNode) {
            write(((IntegerNode) node).getValue());
        } else if (node instanceof DecimalNode) {
            write(String.valueOf(((DecimalNode) node).getValue()));
        } else if (node instanceof LiteralNode) {
            write(((LiteralNode) node).getValue());
        } else if (node instanceof BooleanNode) {
            write(String.valueOf(((BooleanNode) node).getValue()));
        } else if (node instanceof MainFunctionDeclarationNode) {
            compileMainFunctionDeclaration((MainFunctionDeclarationNode) node);
        } else if (node instanceof FunctionDeclarationNode) {
            compileFunctionDeclaration((FunctionDeclarationNode) node);
        } else if (node instanceof DefaultNativeFunctionCallNode) {
            compileDefaultNativeFunctionCall((DefaultNativeFunctionCallNode) node);
        } else if (node instanceof VariableReferenceNode) {
            compileVariableReference((VariableReferenceNode) node);
        } else if (node instanceof BinaryExpressionNode) {
            compileBinaryExpression((BinaryExpressionNode) node);
        } else {
            throw new UnsupportedOperationException("Unknown node type: " + node.getClass().getName());
        }
    }

    protected void compileBlockNode(final BlockNode node) throws IOException {
        write("{\n");
        tabs++;
        for (final ASTNode child : node.getChildren()) {
            compileNode(child);
        }
        tabs--;
        write("}\n\n");
    }

    protected void compileVariableDeclaration(final VariableDeclarationNode node) throws IOException {
        writeWT("auto " + node.getName());

        final ASTNode val = node.getValue();

        if (!(val instanceof VoidNode)) {
            write(" = ");
            compileNode(val);
        }

        write(";\n");
    }

    protected void compileMainFunctionDeclaration(final MainFunctionDeclarationNode node) throws IOException {
        writeWT("int " + node.getFunctionName());
        write("(");
        final List<ParameterNode> parameterNodes = node.getParameterNodes();
        for (int i = 0, parameterNodesSize = parameterNodes.size(); i < parameterNodesSize; i++) {
            final ParameterNode param = parameterNodes.get(i);
            write("auto " + param.getName());

            if (i + 1 < parameterNodesSize) {
                write(", ");
            }
        }
        write(") ");
        compileBlockNode(node.getBlock());
    }

    protected void compileFunctionDeclaration(final FunctionDeclarationNode node) throws IOException {
        writeWT("auto " + node.getFunctionName());
        write("(");
        final List<ParameterNode> parameterNodes = node.getParameterNodes();
        for (int i = 0, parameterNodesSize = parameterNodes.size(); i < parameterNodesSize; i++) {
            final ParameterNode param = parameterNodes.get(i);
            write("auto " + param.getName());

            if (i + 1 < parameterNodesSize) {
                write(", ");
            }
        }
        write(") ");
        compileBlockNode(node.getBlock());
    }

    protected void compileDefaultNativeFunctionCall(final DefaultNativeFunctionCallNode node) throws IOException {
        DefaultNativeFunctionHandler.handle(node, this);
    }

    private void compileVariableReference(final VariableReferenceNode node) throws IOException {
        write(node.getName());
    }

    private void compileBinaryExpression(final BinaryExpressionNode node) throws IOException {
        if (node.getOperator().equals("^")) {
            addInclude("\"math.h\"");
            write("pow(");
            compileNode(node.getLeft());
            write(", ");
            compileNode(node.getRight());
            write(")");
            return;
        }
        compileNode(node.getLeft());
        write(" ");
        write(node.getOperator());
        write(" ");
        compileNode(node.getRight());
    }

    public String convertToCPPOperator(final String operator) {
        switch (operator) {
            case "||":
                return "||";
            case "&&":
                return "&&";
            case "|":
                return "|";
            case "^^":
                return "^";
            case "&":
                return "&";
            case "^":
                return  "unreachable";
            case "<":
            case "<=":
            case ">":
            case ">=":
            case "<<":
            case ">>":
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
            case "==":
            case "!=":
            default:
                return operator;
        }
    }









    protected void addInclude(final String s) {
        if (includes.contains(s)) return;
        includes.add(s);
    }

    protected void writeWT(final Object o) throws IOException {
        writer.write(IntStream.range(0, tabs).mapToObj(i -> "    ").collect(Collectors.joining()) + o);
    }

    protected <T> void write(final T o) throws IOException{
        writer.write(String.valueOf(o));
    }
}
