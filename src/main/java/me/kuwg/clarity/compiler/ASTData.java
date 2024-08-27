package me.kuwg.clarity.compiler;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.block.ReturnNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassInstantiationNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.call.*;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.ast.nodes.include.IncludeNode;
import me.kuwg.clarity.ast.nodes.literal.*;
import me.kuwg.clarity.ast.nodes.reference.ContextReferenceNode;
import me.kuwg.clarity.ast.nodes.statements.ForNode;
import me.kuwg.clarity.ast.nodes.statements.IfNode;
import me.kuwg.clarity.ast.nodes.statements.WhileNode;
import me.kuwg.clarity.ast.nodes.variable.assign.ObjectVariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.get.LocalVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.ObjectVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.VariableReferenceNode;

import java.util.HashMap;
import java.util.Map;

public class ASTData {
    // Data for VarInt and VarLong
    public static final int SEGMENT_BITS = 0x7F;
    public static final int CONTINUE_BIT = 0x80;

    private static final Map<Class<? extends ASTNodeCompiler>, Integer> NODE_IDS = new HashMap<>();
    private static final Map<Integer, Class<? extends ASTNodeCompiler>> ID_TO_NODE = new HashMap<>();

    static {
        // Null
        registerNode(null, 0x00);

        // Block (0x100)
        registerNode(BlockNode.class, 0x100);
        registerNode(ReturnNode.class, 0x101);

        // Expression (0x200)
        registerNode(BinaryExpressionNode.class, 0x200);

        // Function (0x300)
        registerNode(FunctionDeclarationNode.class, 0x300);
        registerNode(MainFunctionDeclarationNode.class, 0x301);
        registerNode(ParameterNode.class, 0x302);
        registerNode(DefaultNativeFunctionCallNode.class, 0x303);
        registerNode(FunctionCallNode.class, 0x304);
        registerNode(ObjectFunctionCallNode.class, 0x305);
        registerNode(LocalFunctionCallNode.class, 0x306);
        registerNode(PackagedNativeFunctionCallNode.class, 0x307);

        // Variable (0x400)
        registerNode(VariableDeclarationNode.class, 0x400);
        registerNode(VariableReferenceNode.class, 0x401);
        registerNode(VariableReassignmentNode.class, 0x402);
        registerNode(ObjectVariableReferenceNode.class, 0x403);
        registerNode(ObjectVariableReassignmentNode.class, 0x404);
        registerNode(LocalVariableReferenceNode.class, 0x405);

        // Literal (0x500)
        registerNode(LiteralNode.class, 0x500);
        registerNode(IntegerNode.class, 0x501);
        registerNode(DecimalNode.class, 0x502);
        registerNode(VoidNode.class, 0x503);
        registerNode(ArrayNode.class, 0x504);

        // Inclusion (0x600)
        registerNode(IncludeNode.class, 0x600);

        // Class (0x700)
        registerNode(ClassDeclarationNode.class, 0x700);
        registerNode(ClassInstantiationNode.class, 0x701);

        // Context (0x800)
        registerNode(ContextReferenceNode.class, 0x800);

        // Statements (0x900)
        registerNode(IfNode.class, 0x900);
        registerNode(WhileNode.class, 0x901);
        registerNode(ForNode.class, 0x902);
    }

    private static void registerNode(Class<? extends ASTNodeCompiler> clazz, int id) {
        NODE_IDS.put(clazz, id);
        ID_TO_NODE.put(id, clazz);
    }

    public static int getNodeId(Class<? extends ASTNodeCompiler> clazz) {
        return NODE_IDS.getOrDefault(clazz, -1);
    }

    public static Class<? extends ASTNodeCompiler> getClassFromId(int id) {
        return ID_TO_NODE.getOrDefault(id, null);
    }
}
