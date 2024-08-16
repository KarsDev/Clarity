package me.kuwg.clarity.compiler;

import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public interface ASTNodeCompiler {
    void save(ASTOutputStream out) throws IOException;
}
