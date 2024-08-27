package me.kuwg.clarity.ast.nodes.literal;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class ArrayNode extends ASTNode {

    private List<ASTNode> nodes;

    public ArrayNode(final List<ASTNode> nodes) {
        this.nodes = nodes;
    }

    public ArrayNode() {
    }

    public final List<ASTNode> getNodes() {
        return nodes;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Array: ").append("\n");
        for (final ASTNode node : nodes) {
            node.print(sb, indent + "    ");
        }
    }

    @Override
    public void save(final ASTOutputStream out) throws IOException {
        out.writeNodeList(nodes);
    }

    @Override
    public void load(final ASTInputStream in) throws IOException {
        this.nodes = in.readNodeListNoCast();
    }
}
