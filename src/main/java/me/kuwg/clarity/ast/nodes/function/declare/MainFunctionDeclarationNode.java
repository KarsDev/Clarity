package me.kuwg.clarity.ast.nodes.function.declare;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.ArrayList;

public class MainFunctionDeclarationNode extends FunctionDeclarationNode {
    public MainFunctionDeclarationNode(final BlockNode body) {
        super("main", null, false, false, false, false, new ArrayList<>(), body);
    }

    public MainFunctionDeclarationNode() {
        super();
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeNode(super.block);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        super.functionName = "main";
        super.parameterNodes = new ArrayList<>();
        super.block = (BlockNode) in.readNode(version);
    }
}