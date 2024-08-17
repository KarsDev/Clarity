package me.kuwg.clarity.compiler;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.block.ReturnNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassInstantiationNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.call.FunctionCallNode;
import me.kuwg.clarity.ast.nodes.function.call.LocalFunctionCallNode;
import me.kuwg.clarity.ast.nodes.function.call.ObjectFunctionCallNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.ast.nodes.include.IncludeNode;
import me.kuwg.clarity.ast.nodes.literal.DecimalNode;
import me.kuwg.clarity.ast.nodes.literal.IntegerNode;
import me.kuwg.clarity.ast.nodes.literal.LiteralNode;
import me.kuwg.clarity.ast.nodes.function.call.NativeFunctionCallNode;
import me.kuwg.clarity.ast.nodes.reference.ContextReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.assign.ObjectVariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.get.LocalVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.ObjectVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.VariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableReassignmentNode;

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
        NODE_IDS.put(NativeFunctionCallNode.class, 0x303);
        NODE_IDS.put(FunctionCallNode.class, 0x304);
        NODE_IDS.put(ObjectFunctionCallNode.class, 0x305);
        NODE_IDS.put(LocalFunctionCallNode.class, 0x306);

        // Variable (0x400)
        NODE_IDS.put(VariableDeclarationNode.class, 0x400);
        NODE_IDS.put(VariableReferenceNode.class, 0x401);
        NODE_IDS.put(VariableReassignmentNode.class, 0x402);
        NODE_IDS.put(ObjectVariableReferenceNode.class, 0x403);
        NODE_IDS.put(ObjectVariableReassignmentNode.class, 0x404);
        NODE_IDS.put(LocalVariableReferenceNode.class, 0x405);


        // Literal (0x500)
        NODE_IDS.put(LiteralNode.class, 0x500);
        NODE_IDS.put(IntegerNode.class, 0x501);
        NODE_IDS.put(DecimalNode.class, 0x502);

        // Inclusion (0x600)
        NODE_IDS.put(IncludeNode.class, 0x600);

        // Class (0x700)
        NODE_IDS.put(ClassDeclarationNode.class, 0x700);
        NODE_IDS.put(ClassInstantiationNode.class, 0x701);

        // Context (0x800)
        NODE_IDS.put(ContextReferenceNode.class, 0x800);
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
