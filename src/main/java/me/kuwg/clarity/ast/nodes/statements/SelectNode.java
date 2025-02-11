package me.kuwg.clarity.ast.nodes.statements;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.PreInterpretable;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class SelectNode extends ASTNode implements PreInterpretable {

    private ASTNode condition;
    private List<WhenNode> cases;
    private BlockNode defaultBlock;

    public SelectNode(final ASTNode condition, final List<WhenNode> cases, final BlockNode defaultBlock) {
        this.condition = condition;
        this.cases = cases;
        this.defaultBlock = defaultBlock;
    }

    public SelectNode() {
    }

    public final ASTNode getCondition() {
        return condition;
    }

    public final List<WhenNode> getCases() {
        return cases;
    }

    public final BlockNode getBlock() {
        return defaultBlock;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("SelectNode:\n");
        sb.append(indent).append("  Condition:\n");
        if (condition != null) {
            condition.print(sb, indent + "    ");
        }
        sb.append(indent).append("  Cases:\n");
        if (cases != null) {
            for (WhenNode whenNode : cases) {
                whenNode.print(sb, indent + "    ");
            }
        }
        sb.append(indent).append("  Default Block:\n");
        if (defaultBlock != null) {
            defaultBlock.print(sb, indent + "    ");
        }
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        out.writeNode(condition);
        out.writeNodeList(cases);
        out.writeNode(defaultBlock);
    }

    @Override
    protected void load0(final ASTInputStream in) throws IOException {
        this.condition = in.readNode();
        this.cases = in.readNodeListNoCast();
        this.defaultBlock = (BlockNode) in.readNode();
    }

    public static class WhenNode extends ASTNode implements PreInterpretable {

        private ASTNode whenExpression;
        private BlockNode block;

        public WhenNode(final ASTNode whenExpression, final BlockNode block) {
            this.whenExpression = whenExpression;
            this.block = block;
        }

        public WhenNode() {
            super();
        }

        public final ASTNode getWhenExpression() {
            return whenExpression;
        }

        public final BlockNode getBlock() {
            return block;
        }

        @Override
        public void print(final StringBuilder sb, final String indent) {
            sb.append(indent).append("CaseNode:\n");
            sb.append(indent).append("  When Expression:\n");
            if (whenExpression != null) {
                whenExpression.print(sb, indent + "    ");
            }
            sb.append(indent).append("  Block:\n");
            if (block != null) {
                block.print(sb, indent + "    ");
            }
        }

        @Override
        protected void save0(final ASTOutputStream out) throws IOException {
            out.writeNode(whenExpression);
            out.writeNode(block);
        }

        @Override
        protected void load0(final ASTInputStream in) throws IOException {
            this.whenExpression = in.readNode();
            this.block = (BlockNode) in.readNode();
        }
    }
}
