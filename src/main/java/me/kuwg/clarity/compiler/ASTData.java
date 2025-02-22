package me.kuwg.clarity.compiler;

import me.kuwg.clarity.ast.nodes.block.*;
import me.kuwg.clarity.ast.nodes.clazz.ClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassInstantiationNode;
import me.kuwg.clarity.ast.nodes.clazz.NativeClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.annotation.AnnotationDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.annotation.AnnotationUseNode;
import me.kuwg.clarity.ast.nodes.clazz.cast.NativeCastNode;
import me.kuwg.clarity.ast.nodes.clazz.envm.EnumDeclarationNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.call.*;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.ast.nodes.function.declare.ReflectedNativeFunctionDeclaration;
import me.kuwg.clarity.ast.nodes.include.IncludeNode;
import me.kuwg.clarity.ast.nodes.literal.*;
import me.kuwg.clarity.ast.nodes.member.MemberFunctionCallNode;
import me.kuwg.clarity.ast.nodes.statements.*;
import me.kuwg.clarity.ast.nodes.variable.assign.LocalVariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.assign.ObjectVariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.get.LocalVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.ObjectVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.VariableReferenceNode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hi12167pies, NotKuwg
 */
public final class ASTData {
    // Data for VarInt and VarLong
    public static final int SEGMENT_BITS = 0x7F;
    public static final int CONTINUE_BIT = 0x80;

    public static final Map<Class<? extends ASTNodeCompiler>, Integer> NODE_TO_ID = new HashMap<>();
    public static final Map<Integer, Class<? extends ASTNodeCompiler>> ID_TO_NODE = new HashMap<>();

    static {
        // Null
        registerNode(null, 0x00);
        registerNode(NullNode.class, 0x01);

        // Block (0x100)
        registerNode(BlockNode.class, 0x100);
        registerNode(ReturnNode.class, 0x101);
        registerNode(ConditionedReturnNode.class, 0x102);
        registerNode(BreakNode.class, 0x103);
        registerNode(ContinueNode.class, 0x104);
        registerNode(AsyncBlockNode.class, 0x105);
        registerNode(RaiseNode.class, 0x106);
        registerNode(StaticBlockNode.class, 0x107);
        registerNode(AwaitBlockNode.class, 0x108);
        registerNode(TryExceptBlock.class, 0x109);
        registerNode(LambdaBlockNode.class, 0x110);

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
        registerNode(ReflectedNativeFunctionDeclaration.class, 0x308);
        registerNode(MemberFunctionCallNode.class, 0x309);
        registerNode(AwaitFunctionCallNode.class, 0x310);

        // Variable (0x400)
        registerNode(VariableDeclarationNode.class, 0x400);
        registerNode(VariableReferenceNode.class, 0x401);
        registerNode(VariableReassignmentNode.class, 0x402);
        registerNode(ObjectVariableReferenceNode.class, 0x403);
        registerNode(ObjectVariableReassignmentNode.class, 0x404);
        registerNode(LocalVariableReferenceNode.class, 0x405);
        registerNode(LocalVariableReassignmentNode.class, 0x406);

        // Literal (0x500)
        registerNode(LiteralNode.class, 0x500);
        registerNode(IntegerNode.class, 0x501);
        registerNode(DecimalNode.class, 0x502);
        registerNode(VoidNode.class, 0x503);
        registerNode(ArrayNode.class, 0x504);
        registerNode(BooleanNode.class, 0x505);

        // Inclusion (0x600)
        registerNode(IncludeNode.class, 0x600);

        // Class (0x700)
        registerNode(ClassDeclarationNode.class, 0x700);
        registerNode(ClassInstantiationNode.class, 0x701);
        registerNode(NativeClassDeclarationNode.class, 0x702);
        registerNode(NativeCastNode.class, 0x703);
        registerNode(EnumDeclarationNode.class, 0x704);
        registerNode(AnnotationDeclarationNode.class, 0x705);
        registerNode(AnnotationUseNode.class, 0x706);

        // FREE 0x800

        // Statements (0x900)
        registerNode(IfNode.class, 0x900);
        registerNode(WhileNode.class, 0x901);
        registerNode(ForNode.class, 0x902);
        registerNode(AssertNode.class, 0x902);
        registerNode(ForeachNode.class, 0x903);
        registerNode(IsNode.class, 0x904);
        registerNode(DeleteVariableNode.class, 0x905);
        registerNode(DeleteFunctionNode.class, 0x906);
        registerNode(TernaryOperatorNode.class, 0x69);
    }

    private static void registerNode(Class<? extends ASTNodeCompiler> clazz, int id) {
        NODE_TO_ID.put(clazz, id);
        ID_TO_NODE.put(id, clazz);
    }

    public static int getIdFromNode(Class<? extends ASTNodeCompiler> clazz) {
        return NODE_TO_ID.getOrDefault(clazz, -1);
    }

    public static Class<? extends ASTNodeCompiler> getClassFromId(int id) {
        return ID_TO_NODE.getOrDefault(id, null);
    }
}
