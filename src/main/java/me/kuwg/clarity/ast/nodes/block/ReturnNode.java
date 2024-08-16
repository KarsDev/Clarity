package me.kuwg.clarity.ast.nodes.block;

import me.kuwg.clarity.ast.ASTNode;

public class ReturnNode extends ASTNode {
    private final ASTNode value;

    public ReturnNode(final ASTNode value) {
        this.value = value;
    }

    public final ASTNode getValue() {
        return value;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {

    }
}
