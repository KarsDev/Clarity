package me.kuwg.clarity.ast.nodes.block;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class ReturnNode extends ASTNode {
    private ASTNode value;

    public ReturnNode(final ASTNode value) {
        this.value = value;
    }

    public ReturnNode() {
    }

    public final ASTNode getValue() {
        return value;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {

    }

    @Override
    public void save(final ASTOutputStream out) throws IOException {
        out.writeNode(value);
    }

    @Override
    public void load(final ASTInputStream in) throws IOException {
        this.value = in.readNode();
    }
}
