package me.kuwg.clarity.compiler;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;


public class ASTSaver {
    private final AST ast;

    public ASTSaver(final AST ast) {
        this.ast = ast;
    }

    public void save(File file) throws IOException {
        GZIPOutputStream gzipOut = new GZIPOutputStream(Files.newOutputStream(file.toPath()));
        ASTOutputStream out = new ASTOutputStream(gzipOut);

        out.writeNode(ast.getRoot());

        out.close();
        gzipOut.close();
    }
}
