package me.kuwg.clarity.compiler;

import me.kuwg.clarity.compiler.stream.ASTOutputStream;

public interface ASTNodeCompiler {
    void save(ASTOutputStream out);
}
