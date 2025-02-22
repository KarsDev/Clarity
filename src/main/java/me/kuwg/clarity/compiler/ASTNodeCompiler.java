package me.kuwg.clarity.compiler;

import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

/**
 * @author hi12167pies
 */
public interface ASTNodeCompiler {
    void save(ASTOutputStream out, CompilerVersion version) throws IOException;
    void load(ASTInputStream in, CompilerVersion version) throws IOException;
}
