package me.kuwg.clarity.compiler;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;

/**
 * @author hi12167pies
 */
public final class ASTSaver {
    private final AST ast;

    public ASTSaver(final AST ast) {
        this.ast = ast;
    }

    public void save(final File file) throws IOException {
        try (final GZIPOutputStream gzipOut = new GZIPOutputStream(Files.newOutputStream(file.toPath()));
             ASTOutputStream out = new ASTOutputStream(gzipOut)) {
            out.writeNode(ast.getRoot());
        }
    }
}
