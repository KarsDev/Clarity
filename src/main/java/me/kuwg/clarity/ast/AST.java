package me.kuwg.clarity.ast;

import me.kuwg.clarity.ast.nodes.block.BlockNode;

public class AST extends ASTNode {

    private final BlockNode root;

    public AST(final BlockNode root) {
        this.root = root;
    }

    public final BlockNode getRoot() {
        return root;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        root.print(sb, indent);
    }
}
