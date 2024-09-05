package me.kuwg.clarity.compiler.ast;

import me.kuwg.clarity.compiler.ast.stream.ASTInputStream;
import me.kuwg.clarity.compiler.ast.stream.ASTOutputStream;

import java.io.IOException;

public interface ASTNodeCompiler {
    void save(ASTOutputStream out) throws IOException;
    void load(ASTInputStream in) throws IOException;
}
