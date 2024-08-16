package me.kuwg.clarity.compiler;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.block.ReturnNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.ParameterNode;
import me.kuwg.clarity.ast.nodes.variable.VariableDeclarationNode;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;


public class ASTSaver {
    private final AST ast;

    public ASTSaver(final AST ast) {
        this.ast = ast;
    }

    public void save(File file) throws IOException {
        ASTOutputStream out = new ASTOutputStream(
                Files.newOutputStream(file.toPath()) // wtf intellij why not make new FileOutputStream
        );

        out.writeNode(ast.getRoot());

        out.close();
    }
}
