package me.kuwg.clarity.ast.nodes.block;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlockNode extends ASTNode {

    private final List<ASTNode> children = new ArrayList<>();

    public void addChild(final ASTNode child) {
        children.add(child);
    }

    public final List<ASTNode> getChildren() {
        return children;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("BlockNode\n");
        for (ASTNode child : children) {
            child.print(sb, indent + "    ");
        }
    }

    @Override
    public void save(final ASTOutputStream out) throws IOException {
        out.writeNodeList(children);
    }
}
