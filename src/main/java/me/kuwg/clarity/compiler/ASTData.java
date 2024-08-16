package me.kuwg.clarity.compiler;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.block.ReturnNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.ParameterNode;
import me.kuwg.clarity.ast.nodes.literal.DecimalNode;
import me.kuwg.clarity.ast.nodes.literal.IntegerNode;
import me.kuwg.clarity.ast.nodes.literal.LiteralNode;
import me.kuwg.clarity.ast.nodes.variable.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.VariableReferenceNode;

import java.util.HashMap;
import java.util.Map;

public class ASTData {
    private static final Map<Class<? extends ASTNodeCompiler>, Integer> NODE_IDS = new HashMap<>();

    static {
        // Block (0x100)
        NODE_IDS.put(BlockNode.class, 0x100);
        NODE_IDS.put(ReturnNode.class, 0x101);

        // Expression (0x200)
        NODE_IDS.put(BinaryExpressionNode.class, 0x200);

        // Function (0x300)
        NODE_IDS.put(FunctionDeclarationNode.class, 0x300);
        NODE_IDS.put(MainFunctionDeclarationNode.class, 0x301);
        NODE_IDS.put(ParameterNode.class, 0x302);

        // Variable (0x400)
        NODE_IDS.put(VariableDeclarationNode.class, 0x400);
        NODE_IDS.put(VariableReferenceNode.class, 0x401);

        // Literal (0x500)
        NODE_IDS.put(LiteralNode.class, 0x500);
        NODE_IDS.put(IntegerNode.class, 0x501);
        NODE_IDS.put(DecimalNode.class, 0x502);
    }

    public static int getNodeId(Class<? extends ASTNodeCompiler> clazz) {
        return NODE_IDS.getOrDefault(clazz, -1);
    }

    public static Class<? extends ASTNodeCompiler> getClassFromId(int id) {
        for (final Map.Entry<Class<? extends ASTNodeCompiler>, Integer> entry : NODE_IDS.entrySet()) {
            if (entry.getValue() == id) {
                return entry.getKey();
            }
        }
        return null;
    }
}
