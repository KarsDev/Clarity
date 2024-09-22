package me.kuwg.clarity.ast.nodes.block;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class TryExceptBlock extends ASTNode {

    private BlockNode tryBlock;
    private String excepted;
    private BlockNode exceptBlock;

    public TryExceptBlock(final BlockNode tryBlock, final String excepted, final BlockNode exceptBlock) {
        this.tryBlock = tryBlock;
        this.excepted = excepted;
        this.exceptBlock = exceptBlock;
    }

    public TryExceptBlock() {
    }

    public final BlockNode getExceptBlock() {
        return exceptBlock;
    }

    public final String getExcepted() {
        return excepted;
    }

    public final BlockNode getTryBlock() {
        return tryBlock;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("TryExceptBlock\n");

        sb.append(indent).append("    ").append("Try Block:\n");
        if (tryBlock != null) {
            tryBlock.print(sb, indent + "        ");
        } else {
            sb.append(indent).append("        ").append("null\n");
        }

        sb.append(indent).append("    ").append("Excepted Exception: ");
        if (excepted != null) {
            sb.append(excepted).append("\n");
        } else {
            sb.append("null\n");
        }

        sb.append(indent).append("    ").append("Except Block:\n");
        if (exceptBlock != null) {
            exceptBlock.print(sb, indent + "        ");
        } else {
            sb.append(indent).append("        ").append("null\n");
        }
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        out.writeNode(tryBlock);
        out.writeString(String.valueOf(excepted));
        out.writeNode(exceptBlock);
    }

    @Override
    protected void load0(final ASTInputStream in) throws IOException {
        this.tryBlock = (BlockNode) in.readNode();
        this.excepted = in.readString();
        if (this.excepted.equals("null")) this.excepted = null;
        this.exceptBlock = (BlockNode) in.readNode();
    }

    @Override
    public String toString() {
        return "TryExceptBlock{" +
                "exceptBlock=" + exceptBlock +
                ", tryBlock=" + tryBlock +
                ", excepted='" + excepted + '\'' +
                '}';
    }
}
