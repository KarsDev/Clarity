package me.kuwg.clarity.ast;

import me.kuwg.clarity.compiler.ASTNodeCompiler;

public abstract class ASTNode implements ASTNodeCompiler {
    public abstract void print(final StringBuilder sb, final String indent);
}
