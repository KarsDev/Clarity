package me.kuwg.clarity.ast.nodes.function;

import me.kuwg.clarity.ast.nodes.block.BlockNode;

import java.util.ArrayList;

public class MainFunctionDeclarationNode extends FunctionDeclarationNode {
    public MainFunctionDeclarationNode(final BlockNode body) {
        super("main", new ArrayList<>(), body);
    }
}