package me.kuwg.clarity.ast.nodes.condition;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IfStatementNode extends ASTNode {

    private ASTNode condition;
    private BlockNode ifBlock;
    private List<IfStatementNode> elseIfStatements;
    private BlockNode elseBlock;

    public IfStatementNode(final ASTNode condition, final BlockNode ifBlock) {
        this.condition = condition;
        this.ifBlock = ifBlock;
        this.elseIfStatements = new ArrayList<>();
        this.elseBlock = null;
    }

    public IfStatementNode() {
    }

    public final ASTNode getCondition() {
        return condition;
    }

    public final BlockNode getIfBlock() {
        return ifBlock;
    }

    public final List<IfStatementNode> getElseIfStatements() {
        return elseIfStatements;
    }

    public final BlockNode getElseBlock() {
        return elseBlock;
    }

    public void addElseIfStatement(IfStatementNode elseIfBlock) {
        this.elseIfStatements.add(elseIfBlock);
    }

    public void setElseBlock(BlockNode elseBlock) {
        this.elseBlock = elseBlock;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {

    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {

    }

    @Override
    protected void load0(final ASTInputStream in) throws IOException {
    }
}
