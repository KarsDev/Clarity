package me.kuwg.clarity.ast.nodes.function.call;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class AwaitFunctionCallNode extends ASTNode {

    private FunctionCallNode functionCallNode;

    public AwaitFunctionCallNode(final FunctionCallNode functionCallNode) {
        this.functionCallNode = functionCallNode;
    }

    public AwaitFunctionCallNode() {
    }

    public FunctionCallNode getFunctionCallNode() {
        return functionCallNode;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Await Function Call: ").append("\n");
        functionCallNode.print(sb, indent + indent);
    }


    @Override
    public void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeNode(functionCallNode, version);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.functionCallNode = (FunctionCallNode) in.readNode(version);
    }
}
