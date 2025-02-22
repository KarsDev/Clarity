package me.kuwg.clarity.ast.nodes.block;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.PreInterpretable;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class LambdaBlockNode extends ASTNode implements PreInterpretable {

    private List<ParameterNode> params;
    private BlockNode block;

    public LambdaBlockNode(final List<ParameterNode> params, final BlockNode block) {
        this.params = params;
        this.block = block;
    }

    public LambdaBlockNode() {
    }

    public final BlockNode getBlock() {
        return block;
    }

    public final List<ParameterNode> getParams() {
        return params;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Lambda Block: ").append("\n");
        sb.append(indent).append("    ").append("Parameters: ").append("\n");
        for (final ParameterNode param : params) {
            param.print(sb, indent + "        ");
        }
        sb.append(indent).append("    ").append("Block: ").append("\n");
        block.print(sb, indent + "        ");
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        out.writeNodeList(params);
        out.writeNode(block);
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.params = in.readNodeListNoCast(version);
        this.block = (BlockNode) in.readNode(version);
    }
}
