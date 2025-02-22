package me.kuwg.clarity.ast;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public final class AST extends ASTNode {

    private final BlockNode root;

    public AST(final BlockNode root) {
        super();
        this.root = root;
    }

    public BlockNode getRoot() {
        return root;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        root.print(sb, indent);
    }

    @Override
    public void save0(final ASTOutputStream out) {
        throw new RuntimeException("Cannot save AST");
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        throw new RuntimeException("Cannot load AST");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        print(sb, "");
        return sb.toString();
    }
}