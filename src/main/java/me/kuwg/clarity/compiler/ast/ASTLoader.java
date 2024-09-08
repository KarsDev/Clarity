package me.kuwg.clarity.compiler.ast;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.ast.stream.ASTInputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class ASTLoader {
    private final Path path;

    public ASTLoader(final File file) {
        this.path = file.toPath();
    }

    public ASTLoader(final Path path) {
        this.path = path;
    }

    public AST load() throws IOException {
        try (final GZIPInputStream gzipIn = new GZIPInputStream(Files.newInputStream(path))) {
            final ASTInputStream stream = new ASTInputStream(gzipIn);
            final BlockNode rootNode = (BlockNode) stream.readNode();
            stream.close();
            return new AST(rootNode);
        }
    }
}