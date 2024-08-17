package me.kuwg.clarity.ast.nodes.block;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class BlockNode extends ASTNode implements Iterable<ASTNode> {

    private final List<ASTNode> children = new ArrayList<>();

    public void addChild(final ASTNode child) {
        children.add(child);
    }

    public final List<ASTNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "BlockNode{" +
                "children=" + children +
                '}';
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

    @Override
    public void load(final ASTInputStream in) throws IOException {
        this.children.clear();
        this.children.addAll(in.readNodeList());
    }

    @Override
    public Iterator<ASTNode> iterator() {
        return children.iterator();
    }

    @Override
    public void forEach(final Consumer<? super ASTNode> action) {
        children.forEach(action);
    }

    @Override
    public Spliterator<ASTNode> spliterator() {
        return children.spliterator();
    }
}
