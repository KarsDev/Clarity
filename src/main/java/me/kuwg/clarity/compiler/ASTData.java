package me.kuwg.clarity.compiler;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.block.ReturnNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.ParameterNode;
import me.kuwg.clarity.ast.nodes.variable.VariableDeclarationNode;

import java.util.HashMap;
import java.util.Map;

public class ASTData {
    private static final Map<Class<? extends ASTNodeCompiler>, Integer> NODE_IDS = new HashMap<>();

    static {
        /** shut up intellij this isn't a javadoc comment
         0x100 = Block
         0x200 = Expression
         0x300 = Function
         0x400 = Variable
         0x500 = Literal

         Each node type has its own range, so you can add more
         **/

        // Block
        NODE_IDS.put(BlockNode.class, 0x100);
        NODE_IDS.put(ReturnNode.class, 0x101);

        // Expression
        NODE_IDS.put(BinaryExpressionNode.class, 0x200);

        // Function
        NODE_IDS.put(FunctionDeclarationNode.class, 0x300);
        NODE_IDS.put(MainFunctionDeclarationNode.class, 0x301);
        NODE_IDS.put(ParameterNode.class, 0x302);

        // Variable
        NODE_IDS.put(VariableDeclarationNode.class, 0x400);
    }

    public static int getNodeId(Class<? extends ASTNodeCompiler> clazz) {
        return NODE_IDS.getOrDefault(clazz, -1);
    }
}
