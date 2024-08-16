package me.kuwg.clarity.ast.nodes.function;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class MainFunctionDeclarationNode extends ASTNode {
    private final BlockNode body;

    public MainFunctionDeclarationNode(final BlockNode body) {
        this.body = body;
    }

    public final BlockNode getBody() {
        return body;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Main Function:\n");
        body.print(sb, indent + "    ");
    }

    @Override
    public void save(final ASTOutputStream out) throws IOException {
        out.writeNode(body);
    }
}