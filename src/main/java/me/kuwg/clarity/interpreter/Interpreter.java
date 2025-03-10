package me.kuwg.clarity.interpreter;

import me.kuwg.clarity.Clarity;
import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.PreInterpretable;
import me.kuwg.clarity.ast.nodes.block.*;
import me.kuwg.clarity.ast.nodes.clazz.ClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassInstantiationNode;
import me.kuwg.clarity.ast.nodes.clazz.NativeClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.annotation.AnnotationDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.annotation.AnnotationUseNode;
import me.kuwg.clarity.ast.nodes.clazz.cast.CastType;
import me.kuwg.clarity.ast.nodes.clazz.cast.NativeCastNode;
import me.kuwg.clarity.ast.nodes.clazz.envm.EnumDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.virtual.VirtualClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.call.*;
import me.kuwg.clarity.ast.nodes.function.declare.*;
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
import me.kuwg.clarity.debug.MethodTimingRegistry;
import me.kuwg.clarity.debug.PerformanceHistogram;
import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.interpreter.definition.*;
import me.kuwg.clarity.interpreter.natf.NativeFunctionNode;
import me.kuwg.clarity.library.natives.ClarityNativeFunction;
import me.kuwg.clarity.library.objects.ObjectType;
import me.kuwg.clarity.library.objects.VoidObject;
import me.kuwg.clarity.library.objects.types.ClassObject;
import me.kuwg.clarity.library.objects.types.LambdaObject;
import me.kuwg.clarity.library.privilege.Privileges;
import me.kuwg.clarity.nmh.NativeMethodHandler;
import me.kuwg.clarity.register.Register;
import me.kuwg.clarity.register.Register.RegisterElementType;
import me.kuwg.clarity.token.Tokenizer;
import me.kuwg.clarity.util.Debugging;
import me.kuwg.clarity.util.StillTesting;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static me.kuwg.clarity.ast.nodes.clazz.cast.CastType.*;
import static me.kuwg.clarity.interpreter.definition.BreakValue.BREAK;
import static me.kuwg.clarity.interpreter.definition.ContinueValue.CONTINUE;
import static me.kuwg.clarity.library.objects.VoidObject.VOID_OBJECT;
import static me.kuwg.clarity.library.objects.VoidObject.VOID_RETURN;
import static me.kuwg.clarity.register.Register.RegisterElementType.*;

public final class Interpreter {
    private static final ExecutorService ASYNC_POOL = Executors.newCachedThreadPool();

    private final AST ast;
    private final NativeMethodHandler nmh;
    private final Context general;
    private final ExemptionHandler exemptionHandler;

    @Debugging
    public Interpreter(final AST ast) {
        // optimization is useless
        this.ast = ast;
        this.nmh = new NativeMethodHandler();
        this.general = new Context();
        this.exemptionHandler = new ExemptionHandler();
    }

    public Context general() {
        return general;
    }

    public int interpret() {

        MainFunctionDeclarationNode main = null;

        for (final ASTNode node : ast.getRoot()) {
            if (node instanceof MainFunctionDeclarationNode) {
                if (main != null) {
                    except("Found multiple main functions, at line: " + main.getLine() + " and " + node.getLine());
                    throw new RuntimeException();
                }
                main = (MainFunctionDeclarationNode) node;
                ast.getRoot().getChildren().remove(node);
            } else {
                if (preInterpret(node, ast.getRoot(), general)) {
                    ast.getRoot().getChildren().remove(node);
                }
            }
        }
        final int ret;

        if (main != null) {

            Register.register(RegisterElementType.FUNCALL, "main", main.getLine(), "none");

            final Object result = interpretNode(main.getBlock(), general);


            if (result == VOID_OBJECT || result == VOID_RETURN || result == null) {
                ret = 0;
            } else if (!(result instanceof Long)) {
                System.err.println("[WARNING] Main function does not return an integer");
                ret = 1;
            } else {
                ret = ((Long) result).intValue();
            }
        } else {
            if (interpretNode(ast.getRoot(), general) != VOID_OBJECT) {
                except("Unexpected return without main function");
                ret = 1;
            } else {
                ret = 0;
            }

        }

        checkExemption();

        if (Clarity.SPEED_INFO) PerformanceHistogram.showHistogram();

        return ret;
    }

    private boolean preInterpret(final ASTNode node, final BlockNode block, final Context context) {
        if (node instanceof FunctionDeclarationNode) {
            interpretFunctionDeclaration((FunctionDeclarationNode) node, context);
            return true;
        } else if (node instanceof ClassDeclarationNode) {
            final ClassDeclarationNode cdn = (ClassDeclarationNode) node;
            if (context.getClass(cdn.getName()) == VOID_OBJECT) interpretClassDeclaration(cdn, context);
            return true;
        } else if (node instanceof EnumDeclarationNode) {
            final EnumDeclarationNode cdn = (EnumDeclarationNode) node;
            if (context.getClass(cdn.getName()) == VOID_OBJECT) interpretEnumDeclaration(cdn, context);
            return true;
        } else if (node instanceof IncludeNode) {
            interpretInclude((IncludeNode) node, context);
            return true;
        } else if (node instanceof ReflectedNativeFunctionDeclaration) {
            final ReflectedNativeFunctionDeclaration reflected = (ReflectedNativeFunctionDeclaration) node;
            if (reflected.isStatic()) {
                interpretReflectedNativeFunctionDeclaration(reflected, context);
                return true;
            }
            return false;
        } else if (node instanceof AnnotationDeclarationNode) {
            interpretAnnotationDeclaration((AnnotationDeclarationNode) node, context);
            return true;
        } else if (node instanceof StaticBlockNode) {
            interpretStaticBlock((StaticBlockNode) node, context);
            return true;
        } else if (node instanceof DefaultNativeFunctionCallNode && Clarity.INFORMATION.getOption("loadnatives")) {
            DefaultNativeFunctionCallNode def = (DefaultNativeFunctionCallNode) node;

            final ClarityNativeFunction<?> natf = nmh.getDefault(def.getName());

            if (natf != null && natf.getName().equals(def.getName())) {
                final ASTNode newNode = new NativeFunctionNode(natf, def.getParams());
                final int index = block.getChildren().indexOf(node);
                block.getChildren().set(index, newNode);
            }

            return false;
        } else if (node instanceof PreInterpretable) {
            final BlockNode inside = ((PreInterpretable) node).getBlock();
            for (final ASTNode sub : inside) {
                if (preInterpret(sub, inside, context)) {
                    inside.getChildren().remove(sub);
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public Object interpretNode(final ASTNode node, final Context context) {
        if (Clarity.SPEED_INFO) return interpretNodeTiming(node, context);

        checkExemption();

        if (node instanceof BlockNode) return interpretBlock((BlockNode) node, context);
        else if (node instanceof VariableDeclarationNode) return interpretVariableDeclaration((VariableDeclarationNode) node, context);
        else if (node instanceof BinaryExpressionNode) return interpretBinaryExpression((BinaryExpressionNode) node, context);
        else if (node instanceof DefaultNativeFunctionCallNode) return interpretDefaultNativeFunctionCall((DefaultNativeFunctionCallNode) node, context);
        else if (node instanceof IntegerNode) return ((IntegerNode) node).getValue();
        else if (node instanceof DecimalNode) return ((DecimalNode) node).getValue();
        else if (node instanceof LiteralNode) return ((LiteralNode) node).getValue();
        else if (node instanceof VariableReferenceNode) return interpretVariableReference((VariableReferenceNode) node, context);
        else if (node instanceof FunctionCallNode) return interpretFunctionCall((FunctionCallNode) node, context);
        else if (node instanceof ReturnNode) return interpretReturn((ReturnNode) node, context);
        else if (node instanceof ClassInstantiationNode) return interpretClassInstantiation((ClassInstantiationNode) node, context);
        else if (node instanceof FunctionDeclarationNode) return interpretFunctionDeclaration((FunctionDeclarationNode) node, context);
        else if (node instanceof ClassDeclarationNode) return interpretClassDeclaration((ClassDeclarationNode) node, context);
        else if (node instanceof VariableReassignmentNode) return interpretVariableReassignment((VariableReassignmentNode) node, context);
        else if (node instanceof ObjectFunctionCallNode) return interpretObjectFunctionCall((ObjectFunctionCallNode) node, context);
        else if (node instanceof LocalVariableReferenceNode) return interpretLocalVariableReference((LocalVariableReferenceNode) node, context);
        else if (node instanceof LocalFunctionCallNode) return interpretLocalFunctionCall((LocalFunctionCallNode) node, context);
        else if (node instanceof ObjectVariableReferenceNode) return interpretObjectVariableReference((ObjectVariableReferenceNode) node, context);
        else if (node instanceof ObjectVariableReassignmentNode) return interpretObjectVariableReassignment((ObjectVariableReassignmentNode) node, context);
        else if (node instanceof VoidNode) return VOID_RETURN;
        else if (node instanceof PackagedNativeFunctionCallNode) return interpretPackagedNativeFunctionCall((PackagedNativeFunctionCallNode) node, context);
        else if (node instanceof ArrayNode) return interpretArray((ArrayNode) node, context);
        else if (node instanceof IfNode) return interpretIf((IfNode) node, context);
        else if (node instanceof NullNode) return null;
        else if (node instanceof ForNode) return interpretFor((ForNode) node, context);
        else if (node instanceof WhileNode) return interpretWhile((WhileNode) node, context);
        else if (node instanceof BooleanNode) return ((BooleanNode) node).getValue();
        else if (node instanceof ForeachNode) return interpretForeach((ForeachNode) node, context);
        else if (node instanceof ReflectedNativeFunctionDeclaration) return interpretReflectedNativeFunctionDeclaration((ReflectedNativeFunctionDeclaration) node, context);
        else if (node instanceof NativeClassDeclarationNode) return interpretNativeClassDeclaration((NativeClassDeclarationNode) node, context);
        else if (node instanceof LocalVariableReassignmentNode) return interpretLocalVariableReassignment((LocalVariableReassignmentNode) node, context);
        else if (node instanceof SelectNode) return interpretSelect((SelectNode) node, context);
        else if (node instanceof BreakNode) return BREAK;
        else if (node instanceof ContinueNode) return CONTINUE;
        else if (node instanceof NativeCastNode) return interpretNativeCast((NativeCastNode) node, context);
        else if (node instanceof ConditionedReturnNode) return interpretConditionedReturn((ConditionedReturnNode) node, context);
        else if (node instanceof MemberFunctionCallNode) return interpretMemberFunctionCall((MemberFunctionCallNode) node, context);
        else if (node instanceof AssertNode) return interpretAssert((AssertNode) node, context);
        else if (node instanceof IsNode) return interpretIs((IsNode) node, context);
        else if (node instanceof EnumDeclarationNode) return interpretEnumDeclaration((EnumDeclarationNode) node, context);
        else if (node instanceof AnnotationDeclarationNode) return interpretAnnotationDeclaration((AnnotationDeclarationNode) node, context);
        else if (node instanceof AnnotationUseNode) return interpretAnnotationUse((AnnotationUseNode) node, context);
        else if (node instanceof AsyncBlockNode) return interpretAsyncBlock((AsyncBlockNode) node, context);
        else if (node instanceof RaiseNode) return interpretRaise((RaiseNode) node, context);
        else if (node instanceof TryExceptBlock) return interpretTryExcept((TryExceptBlock) node, context);
        else if (node instanceof StaticBlockNode) return interpretStaticBlock((StaticBlockNode) node, context);
        else if (node instanceof TernaryOperatorNode) return interpretTernaryOperator((TernaryOperatorNode) node, context);
        else if (node instanceof LambdaBlockNode) return interpretLambdaBlock((LambdaBlockNode) node, context);
        else if (node instanceof DeleteVariableNode) return interpretDeleteVariable((DeleteVariableNode) node, context);
        else if (node instanceof DeleteFunctionNode) return interpretDeleteFunction((DeleteFunctionNode) node, context);
        else if (node instanceof AwaitBlockNode) return interpretAwaitBlock((AwaitBlockNode) node, context);
        else if (node instanceof AwaitFunctionCallNode) return interpretAwaitFunctionCall((AwaitFunctionCallNode) node, context);
        else if (node instanceof NativeFunctionNode) return interpretNativeFunction((NativeFunctionNode) node, context);
        else if (node instanceof LocalReferenceNode) return except("Still not working...", node.getLine());
        else if (node instanceof VirtualClassDeclarationNode) return interpretVirtualClassDeclaration((VirtualClassDeclarationNode) node, context);

        throw new UnsupportedOperationException("Unsupported node: " + (node == null ? "null" : node.getClass().getSimpleName()) + ", val=" + node);
    }

    private Object interpretNodeTiming(final ASTNode node, final Context context) {
        checkExemption();
        final long start = System.nanoTime(); // Start timing
        try {
            if (node instanceof BlockNode) {
                return interpretBlock((BlockNode) node, context);
            } else if (node instanceof VariableDeclarationNode) {
                return interpretVariableDeclaration((VariableDeclarationNode) node, context);
            } else if (node instanceof BinaryExpressionNode) {
                return interpretBinaryExpression((BinaryExpressionNode) node, context);
            } else if (node instanceof DefaultNativeFunctionCallNode) {
                return interpretDefaultNativeFunctionCall((DefaultNativeFunctionCallNode) node, context);
            } else if (node instanceof IntegerNode) {
                return ((IntegerNode) node).getValue();
            } else if (node instanceof DecimalNode) {
                return ((DecimalNode) node).getValue();
            } else if (node instanceof LiteralNode) {
                return ((LiteralNode) node).getValue();
            } else if (node instanceof VariableReferenceNode) {
                return interpretVariableReference((VariableReferenceNode) node, context);
            } else if (node instanceof FunctionCallNode) {
                return interpretFunctionCall((FunctionCallNode) node, context);
            } else if (node instanceof ReturnNode) {
                return interpretReturn((ReturnNode) node, context);
            } else if (node instanceof ClassInstantiationNode) {
                return interpretClassInstantiation((ClassInstantiationNode) node, context);
            } else if (node instanceof FunctionDeclarationNode) {
                return interpretFunctionDeclaration((FunctionDeclarationNode) node, context);
            } else if (node instanceof ClassDeclarationNode) {
                return interpretClassDeclaration((ClassDeclarationNode) node, context);
            } else if (node instanceof VariableReassignmentNode) {
                return interpretVariableReassignment((VariableReassignmentNode) node, context);
            } else if (node instanceof ObjectFunctionCallNode) {
                return interpretObjectFunctionCall((ObjectFunctionCallNode) node, context);
            } else if (node instanceof LocalVariableReferenceNode) {
                return interpretLocalVariableReference((LocalVariableReferenceNode) node, context);
            } else if (node instanceof LocalFunctionCallNode) {
                return interpretLocalFunctionCall((LocalFunctionCallNode) node, context);
            } else if (node instanceof ObjectVariableReferenceNode) {
                return interpretObjectVariableReference((ObjectVariableReferenceNode) node, context);
            } else if (node instanceof ObjectVariableReassignmentNode) {
                return interpretObjectVariableReassignment((ObjectVariableReassignmentNode) node, context);
            } else if (node instanceof VoidNode) {
                return VOID_RETURN;
            } else if (node instanceof PackagedNativeFunctionCallNode) {
                return interpretPackagedNativeFunctionCall((PackagedNativeFunctionCallNode) node, context);
            } else if (node instanceof ArrayNode) {
                return interpretArray((ArrayNode) node, context);
            } else if (node instanceof IfNode) {
                return interpretIf((IfNode) node, context);
            } else if (node instanceof NullNode) {
                return null;
            } else if (node instanceof ForNode) {
                return interpretFor((ForNode) node, context);
            } else if (node instanceof WhileNode) {
                return interpretWhile((WhileNode) node, context);
            } else if (node instanceof BooleanNode) {
                return ((BooleanNode) node).getValue();
            } else if (node instanceof ForeachNode) {
                return interpretForeach((ForeachNode) node, context);
            } else if (node instanceof ReflectedNativeFunctionDeclaration) {
                return interpretReflectedNativeFunctionDeclaration((ReflectedNativeFunctionDeclaration) node, context);
            } else if (node instanceof NativeClassDeclarationNode) {
                return interpretNativeClassDeclaration((NativeClassDeclarationNode) node, context);
            } else if (node instanceof LocalVariableReassignmentNode) {
                return interpretLocalVariableReassignment((LocalVariableReassignmentNode) node, context);
            } else if (node instanceof SelectNode) {
                return interpretSelect((SelectNode) node, context);
            } else if (node instanceof BreakNode) {
                return BREAK;
            } else if (node instanceof ContinueNode) {
                return CONTINUE;
            } else if (node instanceof NativeCastNode) {
                return interpretNativeCast((NativeCastNode) node, context);
            } else if (node instanceof ConditionedReturnNode) {
                return interpretConditionedReturn((ConditionedReturnNode) node, context);
            } else if (node instanceof MemberFunctionCallNode) {
                return interpretMemberFunctionCall((MemberFunctionCallNode) node, context);
            } else if (node instanceof AssertNode) {
                return interpretAssert((AssertNode) node, context);
            } else if (node instanceof IsNode) {
                return interpretIs((IsNode) node, context);
            } else if (node instanceof EnumDeclarationNode) {
                return interpretEnumDeclaration((EnumDeclarationNode) node, context);
            } else if (node instanceof AnnotationDeclarationNode) {
                return interpretAnnotationDeclaration((AnnotationDeclarationNode) node, context);
            } else if (node instanceof AnnotationUseNode) {
                return interpretAnnotationUse((AnnotationUseNode) node, context);
            } else if (node instanceof AsyncBlockNode) {
                return interpretAsyncBlock((AsyncBlockNode) node, context);
            } else if (node instanceof RaiseNode) {
                return interpretRaise((RaiseNode) node, context);
            } else if (node instanceof TryExceptBlock) {
                return interpretTryExcept((TryExceptBlock) node, context);
            } else if (node instanceof StaticBlockNode) {
                return interpretStaticBlock((StaticBlockNode) node, context);
            } else if (node instanceof TernaryOperatorNode) {
                return interpretTernaryOperator((TernaryOperatorNode) node, context);
            } else if (node instanceof LambdaBlockNode) {
                return interpretLambdaBlock((LambdaBlockNode) node, context);
            } else if (node instanceof DeleteVariableNode) {
                return interpretDeleteVariable((DeleteVariableNode) node, context);
            } else if (node instanceof DeleteFunctionNode) {
                return interpretDeleteFunction((DeleteFunctionNode) node, context);
            }  else if (node instanceof AwaitBlockNode) {
                return interpretAwaitBlock((AwaitBlockNode) node, context);
            } else if (node instanceof AwaitFunctionCallNode) {
                return interpretAwaitFunctionCall((AwaitFunctionCallNode) node, context);
            } else if (node instanceof NativeFunctionNode) {
                return interpretNativeFunction((NativeFunctionNode) node, context);
            } else if (node instanceof LocalReferenceNode) {
                return except("Still not working...", node.getLine());
            }
            throw new UnsupportedOperationException("Unsupported node: " + node.getClass().getSimpleName() + ", val=" + node);
        } finally {
            MethodTimingRegistry.register(node.getClass().getSimpleName(), System.nanoTime() - start);
        }
    }

    public Object interpretBlock(final BlockNode block, final Context context) {
        if (block == null || block.isEmpty()) {
            return VOID_OBJECT;
        }

        for (final ASTNode node : block) {
            final Object result = interpretNode(node, context);
            if (result instanceof ReturnValue) {
                return ((ReturnValue) result).getValue();
            } else if (result instanceof BreakValue) {
                return BREAK;
            } else if (result instanceof ContinueValue) {
                return CONTINUE;
            }
        }
        return VOID_OBJECT;
    }

    private Object interpretVariableDeclaration(final VariableDeclarationNode node, final Context context) {
        final ASTNode value = node.getValue();

        Object valueObj;

        if (value instanceof VoidNode) {
            valueObj = VOID_OBJECT;
        } else {
            valueObj = interpretNode(value, context);

            if (valueObj instanceof ReturnValue) valueObj = ((ReturnValue) valueObj).getValue();

            if (valueObj instanceof VoidObject) {
                except("Creating a void variable: " + node.getName(), node.getLine());
                return VOID_OBJECT;
            }

            if (checkTypes(node.getTypeDefault(), valueObj)) {
                except("Unexpected value in variable declaration: " + Interpreter.getAsCLRStr(valueObj) + ", expected " + node.getTypeDefault(), node.getLine());
            }
        }

        context.defineVariable(node.getName(), new VariableDefinition(node.getName(), node.getTypeDefault(), valueObj, node.isConstant(), node.isStatic(), node.isLocal()));

        return VOID_OBJECT;
    }

    private Object interpretFunctionDeclaration(final FunctionDeclarationNode node, final Context context) {
        context.defineFunction(node.getFunctionName(), new FunctionDefinition(node));
        return VOID_OBJECT;
    }

    private Object interpretClassDeclaration(final ClassDeclarationNode node, final Context context) {

        final String name = node.getName();

        final boolean sys = "System".equals(name);

        final String ocn = context.getCurrentClassName();

        context.setCurrentClassName(name);

        final ClassDefinition inheritedClass;

        if (node.getInheritedClass() != null) {
            final ObjectType type = context.getClass(node.getInheritedClass());
            if (!(type instanceof ClassDefinition)) {
                return except("Inherited class not found: " + node.getInheritedClass(), node.getLine());
            } else {
                inheritedClass = (ClassDefinition) type;
                if (inheritedClass.isConstant()) {
                    return except("Inheriting a const " + (inheritedClass.isNative() ? "native " : "") + "class: " + node.getInheritedClass(), node.getLine());
                }
            }
        } else {
            inheritedClass = null;
        }

        final VirtualClassDefinition extendedClass;

        if (node.getExtendedClass() != null) {
            final ObjectType type = context.getClass(node.getExtendedClass());
            if (!(type instanceof VirtualClassDefinition)) {
                if (type instanceof ClassDefinition) {
                    return except(node.getExtendedClass() + " is not a virtual class, so it can not be extended but just inherited.", node.getLine());
                }
                return except("Extended class not found: " + node.getExtendedClass(), node.getLine());
            }
            extendedClass = (VirtualClassDefinition) type;
        } else {
            extendedClass = null;
        }

        final ClassDefinition definition = new ClassDefinition(name, node.isConstant(), inheritedClass, extendedClass, getConstructors(node.getConstructors()), node.getBlock(),false);
        context.defineClass(name, definition);

        if (!context.getNatives().contains(node.getFileName())) Privileges.checkClassName(name, node.getLine());

        StaticBlockNode staticBlock = null;

        for (final ASTNode statement : definition.getBody()) {
            if (statement instanceof VariableDeclarationNode) {
                final VariableDeclarationNode declarationNode = (VariableDeclarationNode) statement;
                if (declarationNode.isStatic()) {
                    final String dName = declarationNode.getName();

                    final String dType = declarationNode.getTypeDefault();

                    final Object dValue;
                    if (sys && declarationNode.getName().equals("ARGS"))
                        dValue = Arrays.copyOfRange(Clarity.ARGS, Clarity.ASC, Clarity.ARGS.length);
                    else
                        dValue = declarationNode.getValue() == null ? null : interpretNode(declarationNode.getValue(), context);

                    final boolean dConst = declarationNode.isConstant();

                    final boolean dStatic = true;

                    final boolean dLocal = declarationNode.isLocal();

                    definition.staticVariables.put(dName, new VariableDefinition(dName, dType, dValue, dConst, dStatic, dLocal));
                }
            } else if (statement instanceof FunctionDeclarationNode) {
                final FunctionDeclarationNode declarationNode = (FunctionDeclarationNode) statement;
                final List<String> params = new ArrayList<>();

                declarationNode.getParameterNodes().forEach(param -> params.add(param.getName()));

                if (declarationNode.isStatic()) {
                    definition.staticFunctions.add(new FunctionDefinition(declarationNode.getFunctionName(), declarationNode.getTypeDefault(), true, declarationNode.isConst(), declarationNode.isLocal(), declarationNode.isAsync(), params, declarationNode.getBlock()));
                }
            } else if (statement instanceof ReflectedNativeFunctionDeclaration) {
                final Object o = interpretReflectedNativeFunctionDeclaration((ReflectedNativeFunctionDeclaration) statement, context);
                if (o instanceof FunctionDefinition) {
                    FunctionDefinition def = (FunctionDefinition) o;
                    definition.staticFunctions.add(def);
                }
            } else if (statement instanceof StaticBlockNode) {
                if (staticBlock != null) return except("More than one static block found in class " + definition.getName(), statement.getLine());
                staticBlock = (StaticBlockNode) statement;
            }
        }

        // static block is the last to get interpreted
        if (staticBlock != null) interpretStaticBlock(staticBlock, context);

        context.setCurrentClassName(ocn);

        return VOID_OBJECT;
    }

    private FunctionDefinition[] getConstructors(List<FunctionDeclarationNode> nodes) {
        final FunctionDefinition[] constructors = new FunctionDefinition[nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            constructors[i] = new FunctionDefinition(nodes.get(i));
        }

        return constructors;
    }

    private Object interpretBinaryExpression(final BinaryExpressionNode node, final Context context) {
        final Object leftValue = interpretNode(node.getLeft(), context);
        final Object rightValue = interpretNode(node.getRight(), context);

        final String operator = node.getOperator();

        if (leftValue == null || rightValue == null) {
            return handleNullComparison(leftValue, rightValue, operator, node.getLine());
        }

        if (leftValue instanceof Boolean && rightValue instanceof Boolean) {
            return evaluateBooleanOperation((Boolean) leftValue, (Boolean) rightValue, operator, node.getLine());
        }

        if (leftValue instanceof String || rightValue instanceof String) {
            return handleStringOperation(leftValue, rightValue, operator, node.getLine());
        }

        if (leftValue instanceof Number && rightValue instanceof Number) {
            return handleNumericOperation((Number) leftValue, (Number) rightValue, operator, node.getLine());
        }

        if (leftValue instanceof Boolean && rightValue instanceof Long) {
            return evaluateBooleanOperation((Boolean) leftValue, (long) rightValue == 1, operator, node.getLine());
        }

        if (leftValue instanceof ClassObject && rightValue instanceof ClassObject) {
            return evaluateClassObjectOperation((ClassObject) leftValue, (ClassObject) rightValue, operator, node.getLine());
        }

        except("Invalid operands for binary expression: " + leftValue.getClass().getSimpleName() + " " + operator + " " + rightValue.getClass().getSimpleName(), node.getLine());
        return VOID_OBJECT;
    }

    private Object handleNullComparison(final Object leftValue, final Object rightValue, final String operator, final int line) {
        switch (operator) {
            case "==":
                return leftValue == rightValue;
            case "!=":
                return leftValue != rightValue;
        }
        except("Only operators available for null are '==' and '!='", line);
        return VOID_OBJECT;
    }

    private Object handleStringOperation(final Object leftValue, final Object rightValue, final String operator, final int line) {
        switch (operator) {
            case "+":
                return leftValue.toString() + rightValue.toString();
            case "==":
                return leftValue.equals(rightValue);
            case "!=":
                return !leftValue.equals(rightValue);
        }
        except("Operator " + operator + " is not supported for string operands.", line);
        return VOID_OBJECT;
    }

    private Object handleNumericOperation(final Number leftNumber, final Number rightNumber, final String operator, final int line) {
        if (leftNumber instanceof Double || leftNumber instanceof Float || rightNumber instanceof Double || rightNumber instanceof Float) {
            return evaluateDoubleOperation(leftNumber.doubleValue(), rightNumber.doubleValue(), operator, line);
        } else {
            return evaluateIntegerOperation(leftNumber.longValue(), rightNumber.longValue(), operator, line);
        }
    }

    private Object evaluateBooleanOperation(final boolean left, final boolean right, final String operator, final int line) {
        switch (operator) {
            case "&&": return left && right;
            case "||": return left || right;
            case "==": return left == right;
            case "!=": return left != right;
            case "^^": return left ^ right;
            default: return except("Unsupported operator for booleans: " + operator, line);
        }
    }

    private Object evaluateDoubleOperation(final double left, final double right, final String operator, final int line) {
        switch (operator) {
            case "+": return left + right;
            case "-": return left - right;
            case "*": return left * right;
            case "/": return (right == 0) ? except("Division by zero", line) : left / right;
            case "%": return left % right;
            case "^": return Math.pow(left, right);
            case "<": return left < right;
            case ">": return left > right;
            case "<=": return left <= right;
            case ">=": return left >= right;
            case "==": return left == right;
            case "!=": return left != right;
            default: return except("Unsupported operator for floats: " + operator, line);
        }
    }

    private Object evaluateIntegerOperation(final long left, final long right, final String operator, final int line) {
        switch (operator) {
            case "+": return left + right;
            case "-": return left - right;
            case "*": return left * right;
            case "/": return (right == 0) ? except("Division by zero", line) : left / right;
            case "%": return (right == 0) ? except("Modulo by zero", line) : left % right;
            case "^": return Math.pow(left, right) % 1 == 0 ? (long) Math.pow(left, right) : Math.pow(left, right);
            case "<": return left < right;
            case ">": return left > right;
            case "<=": return left <= right;
            case ">=": return left >= right;
            case "==": return left == right;
            case "!=": return left != right;
            case ">>": return left >> right;
            case "<<": return left << right;
            case "&": return left & right;
            case "|": return left | right;
            case "^^": return left ^ right;
            case ">>>": return left >>> right;
            default: return except("Unsupported operator for integers: " + operator, line);
        }
    }

    private Object evaluateClassObjectOperation(final ClassObject left, final ClassObject right, final String operator, final int line) {
        switch (operator) {
            case "==": return left == right;
            case "!=": return left != right;
            default: return except("Unsupported operator for class objects: " + operator, line);
        }
    }


    private Object interpretDefaultNativeFunctionCall(final DefaultNativeFunctionCallNode node, final Context context) {
        final List<Object> params = new ArrayList<>(node.getParams().size());
        for (final ASTNode param : node.getParams()) {
            final Object added = interpretNode(param, context);
            if (added instanceof VoidObject) {
                except("Passing void as parameter", node.getLine());
                return VOID_OBJECT;
            }
            params.add(added);
        }
        Register.register(NATIVECALL, node.getName() + getParams(params), node.getLine(), context.getCurrentClassName());
        return nmh.callDefault(node.getName(), params);
    }

    private Object interpretVariableReference(final VariableReferenceNode node, final Context context) {
        final Object value = context.getVariable(node.getName());
        if (value instanceof VoidObject) {
            except("Referencing a non-defined variable: " + node.getName(), node.getLine());
        }
        return value;
    }

    private Object interpretFunctionCall(final FunctionCallNode node, Context context) {
        final String functionName = ((VariableReferenceNode) node.getCaller()).getName();
        context.setCurrentFunctionName(functionName);

        final List<Object> params = new ArrayList<>(node.getParams().size());
        for (final ASTNode param : node.getParams()) {
            final Object returned = interpretNode(param, context);
            if (returned == VOID_OBJECT) {
                return except("Passing void as a parameter function: " + param.getClass().getSimpleName() + ", fn: " + functionName, node.getLine());
            }
            params.add(returned);
        }

        final ObjectType type = context.getFunction(functionName, params.size());
        final FunctionDefinition definition;

        if (type == VOID_OBJECT) {
            ObjectType clazz = context.getClass(context.getCurrentClassName());
            if (clazz == null) {
                final Object maybeLambda = context.getVariable(functionName);
                if (maybeLambda instanceof LambdaObject) {
                    return handleLambdaFunctionCall(node, context, (LambdaObject) maybeLambda);
                }
                return except("Calling a function that doesn't exist: " + functionName + getParams(params), node.getLine());
            }

            definition = ((ClassDefinition) clazz).getStaticFunction(functionName, params.size());
            if (definition == null) {
                return except("Calling a function that doesn't exist: " + functionName + getParams(params), node.getLine());
            }
        } else {
            definition = (FunctionDefinition) type;
        }

        final int paramSize = params.size();
        final int expectedSize = definition.getParams().size();

        if (paramSize != expectedSize) {
            return except("Incorrect parameter count (" + paramSize + " vs " + expectedSize + ") in fn: " + functionName, node.getLine());
        }

        final Context functionContext = new Context(context.parentContext());
        final List<String> definitionParams = definition.getParams();
        for (int i = 0; i < definitionParams.size(); i++) {
            final String name = definitionParams.get(i);
            final Object value = params.get(i);
            functionContext.defineVariable(name, new VariableDefinition(name, null, value, false, false, false));
        }

        Register.register(FUNCALL, functionName + getParams(params), node.getLine(), context.getCurrentClassName());

        if (definition.isAsync()) {
            new Thread(() -> interpretBlock(definition.getBlock(), functionContext), "async:" + functionName).start();
            return VOID_OBJECT;
        }

        final Object result = interpretBlock(definition.getBlock(), functionContext);

        if (checkTypes(definition.getTypeDefault(), result)) {
            return except("Unexpected return: " + getAsCLRStr(result) + ", expected " + definition.getTypeDefault(), node.getLine());
        }

        context.setCurrentFunctionName(null);
        return result;
    }

    public static boolean checkTypes(final String typeDefault, final Object result) {
        final boolean match;
        if (typeDefault == null) {
            match = true;
        } else if (typeDefault.equals("num")) {
            match = result instanceof Number;
        } else if (result instanceof VoidObject) {
            match = typeDefault.equals("void");
        } else if (result instanceof String) {
            match = typeDefault.equals("str");
        } else if (result instanceof Long) {
            match = typeDefault.equals("int");
        } else if (result instanceof Double) {
            match = typeDefault.equals("float");
        } else if (result instanceof ClassObject) {
            match = typeDefault.equals(((ClassObject) result).getName());
        } else if (result instanceof Object[]) {
            match = typeDefault.equals("arr");
        } else if (result instanceof Boolean) {
            match = typeDefault.equals("bool");
        } else {
            throw new RuntimeException("unsupported return for type default: " + (result != null ? result.getClass().getSimpleName() : null));
        }

        return !match;
    }

    private Object interpretReturn(final ReturnNode node, final Context context) {
        final Object ret = interpretNode(node.getValue(), context);
        if (ret instanceof ReturnValue) return except("Return in return", node.getLine());
        return new ReturnValue(ret);
    }

    private Object interpretClassInstantiation(final ClassInstantiationNode node, final Context context) {
        final String name = node.getName();

        Register.register(CLASSINST, name, node.getLine(), context.getCurrentClassName() == null ? "none" : context.getCurrentClassName());

        final String ocn = context.getCurrentClassName();

        context.setCurrentClassName(name);

        // Create a new context for the class instantiation
        final Context classContext = new Context(context.parentContext());

        // Directly initialize the params list with the expected size if known
        final List<Object> params = new ArrayList<>(node.getParams().size());

        // Iterate over params and interpret them
        for (final ASTNode sub : node.getParams()) {
            final Object added = interpretNode(sub, context);
            if (added instanceof VoidObject) {
                return except("Passing void as parameter", node.getLine());
            }
            params.add(added);
        }

        // Retrieve the class definition from the context
        final ObjectType raw = context.getClass(name);
        if (raw == VOID_OBJECT) {
            return except("Class not found: " + name, node.getLine());
        }

        final ClassDefinition definition = (ClassDefinition) raw;

        // Initialize inheritedObject and currentDefinition
        ClassObject inheritedObject = null;
        ClassDefinition currentDefinition = definition;

        // Iterate through the inheritance chain
        while (currentDefinition.getInheritedClass() != null) {
            final ClassDefinition inheritedClass = currentDefinition.getInheritedClass();

            context.setCurrentClassName(inheritedClass.getName());
            interpretBlock(inheritedClass.getBody(), context);
            context.setCurrentClassName(name);

            final FunctionDefinition[] inheritedConstructors = inheritedClass.getConstructors();
            inheritedObject = interpretConstructors(inheritedObject, inheritedConstructors, params, classContext, inheritedClass.getName());
            classContext.mergeContext(inheritedObject.getContext());

            currentDefinition = inheritedClass;
        }

        // Interpret the class body
        final Object val = interpretBlock(definition.getBody(), classContext);
        if (val != VOID_OBJECT) {
            return except("Return in class body", node.getLine());
        }
        final ClassObject result = interpretConstructors(inheritedObject, definition.getConstructors(), params, classContext, name);
        context.setCurrentClassName(ocn);
        return result;
    }

    public ClassObject interpretConstructors(final ClassObject inherited, final FunctionDefinition[] constructors, final List<Object> params, final Context context, final String cn) {
        if (constructors.length == 0) {
            return new ClassObject(cn, inherited, new Context(context));
        }

        FunctionDefinition matchingConstructor = null;
        for (FunctionDefinition constructor : constructors) {
            if (constructor.getParams().size() == params.size()) {
                matchingConstructor = constructor;
                break;
            }
        }

        if (matchingConstructor == null) {
            except("No matching constructor found for class " + cn + " with " + params.size() + " parameters.");
            return new ClassObject(null, null, null); // for notnull errors
        }

        final List<String> constructorParams = matchingConstructor.getParams();

        final Context constructorContext = new Context(context);

        for (int i = 0; i < constructorParams.size(); i++) {
            if (params.get(i) instanceof VoidObject) {
                except("Passing void as parameter");
                return new ClassObject(null, null, null); // for notnull errors
            }
            constructorContext.defineVariable(constructorParams.get(i), new VariableDefinition(constructorParams.get(i), null, params.get(i), false, false, false));
        }

        final Object result = interpretBlock(matchingConstructor.getBlock(), constructorContext);

        if (result != VOID_OBJECT) {
            except("You can't return in a constructor!", matchingConstructor.getBlock().getLine());
            return new ClassObject(null, null, null);
        }

        return new ClassObject(cn, inherited, constructorContext);
    }

    private Object interpretVariableReassignment(final VariableReassignmentNode node, final Context context) {
        final Object result = interpretNode(node.getValue(), context);
        if (result instanceof VoidObject) return except("Reassigning variable with void value: " + node.getName(), node.getLine());
        context.setVariable(node.getName(), result);
        return result;
    }

    private Object interpretObjectFunctionCall(final ObjectFunctionCallNode node, final Context context) {
        final Object caller = context.getVariable(node.getCaller());
        if (caller instanceof Object[]) {
            Object resultArray = handleArrayFunctionCall(node, context, (Object[]) caller);
            if (resultArray instanceof Object[]) {
                context.setVariable(node.getCaller(), resultArray);
                return VOID_OBJECT;
            }
            return resultArray;
        } else if (caller instanceof String) {
            return handleStringFunctionCall(node, context, (String) caller);
        } else if (caller instanceof ClassObject) {
            return handleInstanceMethodCall(node, context, (ClassObject) caller);
        } else if (caller instanceof VoidObject) {
            return handleStaticFunctionCall(node, context);
        } else if (caller instanceof LambdaObject) {
            return handleLambdaFunctionCall(node, context, (LambdaObject) caller);
        } else {
            return except("You can't call a function out of a " + caller.getClass().getSimpleName(), node.getLine());
        }
    }

    private Object handleStaticFunctionCall(final ObjectFunctionCallNode node, final Context context) {
        final ObjectType rawDefinition = context.getClass(node.getCaller());
        if (rawDefinition instanceof VoidObject) {
            return except("Accessing a static function of a non-existent class: " + node.getCaller(), node.getLine());
        }

        final ClassDefinition classDefinition = (ClassDefinition) rawDefinition;

        final String ocn = context.getCurrentClassName();

        context.setCurrentClassName(classDefinition.getName());

        context.setCurrentFunctionName(node.getCalled());

        final FunctionDefinition definition = classDefinition.getStaticFunction(node.getCalled(), node.getParams().size());

        if (definition == null) {
            return except("Accessing a static function that does not exist: " + node.getCaller() + "#" + node.getCalled() + "(...)", node.getLine());
        }

        final List<Object> params = getFunctionParameters(node, context, definition.getParams().size());

        final Context functionContext = new Context(context);
        defineFunctionParameters(functionContext, definition, params);

        Register.register(STATICCALL, node.getCalled() + getParams(params), node.getLine(), context.getCurrentClassName());
        final Object result = interpretBlock(definition.getBlock(), functionContext);
        context.setCurrentClassName(ocn);
        context.setCurrentFunctionName(null);
        return result;
    }

    private Object handleStaticFunctionCall(final MemberFunctionCallNode node, final Context context) {
        final ObjectType rawDefinition = context.getClass(((VariableReferenceNode) node.getCaller()).getName());
        if (rawDefinition instanceof VoidObject) {
            return except("Accessing a static function of a non-existent class: " + node.getCaller(), node.getLine());
        }

        final ClassDefinition classDefinition = (ClassDefinition) rawDefinition;

        final String preName = context.getCurrentClassName();

        final String ocn = context.getCurrentClassName();

        context.setCurrentClassName(classDefinition.getName());
        context.setCurrentFunctionName(node.getName());

        final FunctionDefinition definition = classDefinition.getStaticFunction(node.getName(), node.getParams().size());

        if (definition == null) {
            return except("Accessing a static function that does not exist: " + node.getCaller() + "#" + node.getName() + "(...)", node.getLine());
        }

        final List<Object> params = getFunctionParameters(node, context, definition.getParams().size());

        final Context functionContext = new Context(context);
        defineFunctionParameters(functionContext, definition, params);

        if (definition.isLocal() && !classDefinition.getName().equals(preName)) {
            return except("Accessing a local function: " + definition.getName(), node.getLine());
        }

        Register.register(STATICCALL, node.getName() + getParams(params), node.getLine(), context.getCurrentClassName());
        final Object result = interpretBlock(definition.getBlock(), functionContext);
        context.setCurrentClassName(ocn);
        context.setCurrentFunctionName(null);
        return result;
    }

    private Object handleEnumValueFunctionCall(final MemberFunctionCallNode node, final EnumClassDefinition.EnumValue val) {
        if (!node.getParams().isEmpty()) {
            return except("All enum value functions have no params.", node.getLine());
        }

        switch (node.getName()) {
            case "value":
                return val.getValue();
            case "name":
                return val.getName();
            default:
                return except("Illegal function in array context: " + node.getName(), node.getLine());
        }
    }

    private Object handleArrayFunctionCall(final ASTNode raw, final Context context, final Object[] array) {
        final String fn;
        final List<Object> params;

        if (raw instanceof ObjectFunctionCallNode) {
            ObjectFunctionCallNode node = (ObjectFunctionCallNode) raw;
            fn = node.getCalled();
            params = getFunctionParameters(node, context, -1);
        } else if (raw instanceof MemberFunctionCallNode) {
            MemberFunctionCallNode node = (MemberFunctionCallNode) raw;
            fn = node.getName();
            params = getFunctionParameters(node, context, -1);
        } else {
            return VOID_OBJECT;
        }

        Register.register(ARRAYCALL, fn + getParams(params), raw.getLine(), context.getCurrentClassName());


        switch (fn) {
            case "at": {
                if (params.size() == 1 && params.get(0) instanceof Long) {
                    try {
                        return array[((Number) params.get(0)).intValue()];
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        return except("Array out of bounds: " + params.get(0), raw.getLine());
                    }
                }
            }
            case "size": {
                if (params.isEmpty()) {
                    return array.length;
                }
            }
            case "set": {
                if (params.size() == 2 && (params.get(0) instanceof Long || params.get(0) instanceof Integer)) {
                    try {
                        array[((Number) params.get(0)).intValue()] = params.get(1);
                    } catch (IndexOutOfBoundsException e) {
                        return except("Array index out of bounds: " + params.get(0), raw.getLine());
                    }
                    return VOID_OBJECT;
                }
            }
            case "setSize": {
                if (params.size() == 1 && params.get(0) instanceof Long) {
                    int newSize = ((Number) params.get(0)).intValue();
                    if (newSize < 0) {
                        return except("Negative array size: " + newSize, raw.getLine());
                    }
                    Object[] newArray = new Object[newSize];
                    System.arraycopy(array, 0, newArray, 0, Math.min(array.length, newSize));
                    return newArray;
                }
            }
            case "push": {
                final List<Object> list = new ArrayList<>(Arrays.asList(array));
                list.addAll(params);
                return list.toArray();
            }
            case "splice": {
                if (params.size() >= 2 && params.get(0) instanceof Long && params.get(1) instanceof Long) {
                    final int start = ((Long) params.get(0)).intValue();
                    final int deleteCount = ((Long) params.get(1)).intValue();
                    final List<Object> spliced = new ArrayList<>(Arrays.asList(array));
                    final int end = Math.min(start + deleteCount, spliced.size());
                    spliced.subList(start, end).clear();
                    if (params.size() > 2) spliced.addAll(start, params.subList(2, params.size()));
                    return spliced.toArray();
                }
            }
            default:
                return except("Illegal function in array context: " + fn + " with params " + getParams(params), raw.getLine());
        }
    }

    private Object handleStringFunctionCall(final ASTNode raw, final Context context, final String caller) {
        String fn;
        List<Object> params;

        if (raw instanceof ObjectFunctionCallNode) {
            ObjectFunctionCallNode node = (ObjectFunctionCallNode) raw;
            fn = node.getCalled();
            params = getFunctionParameters(node, context, -1);
            Register.register(STRINGCALL, fn + getParams(params), node.getLine(), context.getCurrentClassName());
        } else if (raw instanceof MemberFunctionCallNode) {
            MemberFunctionCallNode node = (MemberFunctionCallNode) raw;
            fn = node.getName();
            params = getFunctionParameters(node, context, -1);
            Register.register(STRINGCALL, fn + getParams(params), node.getLine(), context.getCurrentClassName());
        } else {
            return VOID_OBJECT;
        }

        switch (fn) {
            case "match":
                if (params.size() == 1 && params.get(0) instanceof String) {
                    return caller.matches((String) params.get(0));
                }
                break;
            case "subs":
                try {
                    if (params.size() == 1 && params.get(0) instanceof Long) {
                        return caller.substring(((Long) params.get(0)).intValue());
                    } else if (params.size() == 2 && params.get(0) instanceof Long && params.get(1) instanceof Long) {
                        return caller.substring(((Long) params.get(0)).intValue(), ((Long) params.get(1)).intValue());
                    }
                } catch (final IndexOutOfBoundsException e) {
                    return except("String index out of bounds: " + params, raw.getLine());
                }
                break;
            case "contains":
                if (params.size() == 1 && params.get(0) instanceof String) {
                    return caller.contains((String) params.get(0));
                }
                break;
            case "startsw":
                if (params.size() == 1 && params.get(0) instanceof String) {
                    return caller.startsWith((String) params.get(0));
                }
                break;
            case "endsw":
                if (params.size() == 1 && params.get(0) instanceof String) {
                    return caller.endsWith((String) params.get(0));
                }
                break;
            case "split":
                if (params.size() == 1 && params.get(0) instanceof String) {
                    return caller.split((String) params.get(0));
                }
                break;
            case "length":
                if (params.isEmpty()) {
                    return (long) caller.length();
                }
                break;
            case "lower":
                if (params.isEmpty()) {
                    return caller.toLowerCase();
                }
                break;
            case "upper":
                if (params.isEmpty()) {
                    return caller.toUpperCase();
                }
                break;
            case "charAt": {
                if (params.size() == 1 && params.get(0) instanceof Long) {
                    return String.valueOf(caller.charAt(((Long) params.get(0)).intValue()));
                }
                break;
            }
        }

        Register.throwException("Illegal function in string context: " + fn + " with params " + getParams(params), raw.getLine());
        return VOID_OBJECT;
    }

    private Object handleLambdaFunctionCall(final ASTNode raw, final Context context, final LambdaObject caller) {
        final String fn;
        final List<Object> params;

        if (raw instanceof ObjectFunctionCallNode) {
            ObjectFunctionCallNode node = (ObjectFunctionCallNode) raw;
            fn = node.getCalled();
            params = getFunctionParameters(node, context, -1);
        } else if (raw instanceof MemberFunctionCallNode) {
            MemberFunctionCallNode node = (MemberFunctionCallNode) raw;
            fn = node.getName();
            params = getFunctionParameters(node, context, -1);
        } else if (raw instanceof FunctionCallNode) {
            fn = "run";
            params = getFunctionParameters((FunctionCallNode) raw, context, -1);
        } else {
            return VOID_OBJECT;
        }

        Register.register(LAMBDACALL, fn + getParams(params), raw.getLine(), context.getCurrentClassName());

        if (!fn.equals("run")) {
            Register.throwException("Illegal function in lambda context: " + fn + " with params " + getParams(params), raw.getLine());
            return VOID_OBJECT;
        }

        final Context lambdaContext = new Context(context);

        if (params.size() != caller.getParams().size()) {
            except("Expected " + caller.getParams().size() + " params but found " + params.size(), raw.getLine());
        }

        for (int i = 0; i < params.size(); i++) {
            final Object obj = params.get(i);
            final ParameterNode param = caller.getParams().get(i);
            if (param.isLambda() && !(obj instanceof LambdaBlockNode)) {
                except("Expected lambda but found " + getParams(Collections.singletonList(params.get(i))), raw.getLine());
                return VOID_OBJECT;
            }
            lambdaContext.defineVariable(param.getName(), new VariableDefinition(param.getName(), null, obj, false, false, false));
        }

        return interpretBlock(caller.getBlock(), lambdaContext);
    }




    private Object handleInstanceMethodCall(final ObjectFunctionCallNode node, final Context context, final ClassObject classObject) {
        final String ocn = context.getCurrentClassName();

        context.setCurrentClassName(classObject.getName());
        context.setCurrentFunctionName(node.getCalled());

        // Attempt to find the function in the current class's context
        ObjectType rawDefinition = classObject.getContext().getFunction(node.getCalled(), node.getParams().size());

        // Check for function in parent classes if it is not found in the current class
        if (rawDefinition == VOID_OBJECT) {
            rawDefinition = findFunctionInInheritedClasses(node, context, classObject);
        }

        if (rawDefinition == VOID_OBJECT) {
            return except("Called a non-existent function: " + classObject.getName() + "#" + node.getCalled(), node.getLine());
        }

        FunctionDefinition definition = (FunctionDefinition) rawDefinition;
        final Context functionContext = new Context(classObject.getContext());

        final List<Object> params = getFunctionParameters(node, context, definition.getParams().size());

        defineFunctionParameters(functionContext, definition, params);

        Register.register(NATIVECALL, node.getCalled() + getParams(definition.getParams()), node.getLine(), context.getCurrentClassName());

        final Object result = interpretBlock(definition.getBlock(), functionContext);
        context.setCurrentClassName(ocn);
        context.setCurrentFunctionName(null);
        return result;
    }

    private ObjectType findFunctionInInheritedClasses(final ObjectFunctionCallNode node, final Context context, final ClassObject classObject) {
        ClassDefinition classDefinition = (ClassDefinition) context.getClass(classObject.getName());

        while (classDefinition != null && classDefinition.getInheritedClass() != null) {

            classDefinition = classDefinition.getInheritedClass();

            FunctionDefinition functionDefinition = null;

            for (final ASTNode astNode : classDefinition.getBody()) {
                if (astNode instanceof FunctionDeclarationNode) {
                    final FunctionDeclarationNode declaration = (FunctionDeclarationNode) astNode;
                    if (!declaration.getFunctionName().equals(node.getCalled()) && declaration.getParameterNodes().size() == node.getParams().size()) continue;
                    functionDefinition = new FunctionDefinition(declaration);
                    break;
                }
            }


            if (functionDefinition != null) {
                return functionDefinition;
            }
        }

        return VOID_OBJECT;
    }

    private List<Object> getFunctionParameters(final ObjectFunctionCallNode node, final Context context, int expectedSize) {
        final List<Object> params = new ArrayList<>();
        for (final ASTNode param : node.getParams()) {
            final Object returned = interpretNode(param, context);
            if (returned == VOID_OBJECT) {
                except("Passing void as a parameter function", node.getLine());
                return new ArrayList<>();
            }
            params.add(returned);
        }

        if (expectedSize != -1 && (params.size() > expectedSize || params.size() < expectedSize)) {
            except("Parameter size mismatch. Expected: " + expectedSize + ", Found: " + params.size(), node.getLine());
            return new ArrayList<>();
        }
        return params;
    }

    private void defineFunctionParameters(final Context functionContext, final FunctionDefinition definition, final List<Object> params) {
        List<String> definitionParams = definition.getParams();
        for (int i = 0; i < definitionParams.size(); i++) {
            final String name = definitionParams.get(i);
            final Object value = params.get(i);
            functionContext.defineVariable(name, new VariableDefinition(name, null, value, false, false, false));
        }
    }

    private Object interpretLocalVariableReference(final LocalVariableReferenceNode node, final Context context) {
        final Object ret = context.parentContext().getVariable(node.getName());
        if (ret == VOID_OBJECT) {
            return except("Referencing a non-created variable: " + node.getName(), node.getLine());
        }
        return ret;
    }

    private Object interpretLocalFunctionCall(final LocalFunctionCallNode node, final Context raw) {
        final Context context = raw.parentContext();
        final String functionName = node.getName();

        context.setCurrentFunctionName(functionName);

        final List<Object> params = new ArrayList<>();

        for (final ASTNode param : node.getParams()) {
            final Object returned = interpretNode(param, context);
            if (returned == VOID_OBJECT) {
                return except("Passing void as a parameter function: " + param.getClass().getSimpleName() + ", fn: " + functionName, node.getLine());
            }
            params.add(returned);
        }

        final ObjectType type = context.getFunction(functionName, node.getParams().size());

        if (type == VOID_OBJECT) {
            return except("Calling a local function that doesn't exist: " + functionName + getParams(params), node.getLine());
        }

        final FunctionDefinition definition = (FunctionDefinition) type;

        if (params.size() > definition.getParams().size()) {
            return except("Passing more parameters than needed (" + params.size() + ", " + definition.getParams().size() + ") in fn: " + functionName, node.getLine());
        } else if (params.size() < definition.getParams().size()) {
            return except("Passing less parameters than needed (" + params.size() + ", " + definition.getParams().size() + ") in fn: " + functionName, node.getLine());
        }

        final Context functionContext = new Context(context);

        List<String> definitionParams = definition.getParams();
        for (int i = 0; i < definitionParams.size(); i++) {
            final String name = definitionParams.get(i);
            final Object value = params.get(i);

            functionContext.defineVariable(name, new VariableDefinition(name, null, value, false, false, false));
        }

        Register.register(LOCALCALL, node.getName() + getParams(params), node.getLine(), context.getCurrentClassName());


        final Object result = interpretBlock(definition.getBlock(), functionContext);
        context.setCurrentFunctionName(null);
        return result;
    }

    private Object interpretObjectVariableReference(final ObjectVariableReferenceNode node, final Context context) {
        final Object callerObject = node.getCaller() instanceof VariableReferenceNode ?
                context.getVariable(((VariableReferenceNode) node.getCaller()).getName()) :
                interpretNode(node.getCaller(), context);

        final String calledObjectName = node.getCalled();

        if (callerObject == VOID_OBJECT) {
            final String className = ((VariableReferenceNode) node.getCaller()).getName();
            final ObjectType classDefinitionRaw = context.getClass(className);

            if (!(classDefinitionRaw instanceof ClassDefinition)) {
                return except("Accessing a static variable of a non-existent class: " + className, node.getLine());
            }

            final ClassDefinition classDefinition = (ClassDefinition) classDefinitionRaw;

            if (classDefinition instanceof EnumClassDefinition) {
                final EnumClassDefinition enumDefinition = (EnumClassDefinition) classDefinition;
                return enumDefinition.getValue(calledObjectName);
            }

            final VariableDefinition staticVariable = classDefinition.staticVariables.get(calledObjectName);

            if (staticVariable == null) {
                return except("Accessing a static variable that does not exist: " + className + "#" + calledObjectName, node.getLine());
            }

            if (staticVariable.isLocal()) {
                if (!classDefinition.getName().equals(context.getCurrentClassName())) {
                    return except("Accessing a local static variable: " + staticVariable.getName(), node.getLine());
                }
            }

            return staticVariable.getValue();
        }

        if (!(callerObject instanceof ClassObject)) {
            return except("Expected Class Object, but found " + (callerObject != null ? callerObject.getClass().getSimpleName() : "null") + ", while getting " + calledObjectName, node.getLine());
        }

        final ObjectType cvr = ((ClassObject) callerObject).getContext().getVariableDefinition(calledObjectName);

        if (cvr instanceof VoidObject) {
            return except("Accessing an instance variable that does not exist: " + node.getCaller() + "." + calledObjectName, node.getLine());
        }

        final VariableDefinition calledVariable = (VariableDefinition) cvr;

        if (calledVariable.isLocal()) {
            if (!calledVariable.getName().equals(context.getCurrentClassName())) {
                return except("Accessing a local variable: " + calledVariable.getName(), node.getLine());
            }
        }

        return calledVariable.getValue();
    }

    private Object interpretObjectVariableReassignment(final ObjectVariableReassignmentNode node, final Context context) {
        final Object callerObjectRaw;

        if (node.getCaller() instanceof VariableReferenceNode) {
            callerObjectRaw = context.getVariable(((VariableReferenceNode) node.getCaller()).getName());
        } else {
            final ObjectVariableReferenceNode ref = (ObjectVariableReferenceNode) node.getCaller();
            final Object callerObjRaw = interpretNode(ref.getCaller(), context);
            if (!(callerObjRaw instanceof ClassObject)) {
                return except("Expected Class Object, instead found " + getAsCLRStr(callerObjRaw), node.getLine());
            }
            final ClassObject callerClass = (ClassObject) callerObjRaw;
            callerObjectRaw = callerClass.getContext().getVariable(node.getCalled());
        }

        if (callerObjectRaw instanceof VoidObject) {
            if (!(node.getCaller() instanceof VariableReferenceNode)) {
                return except("Insufficient condition for static variable reassignment (as non-static)", node.getLine());
            }
            final ObjectType type = context.getClass(((VariableReferenceNode) node.getCaller()).getName());
            if (type instanceof ClassDefinition) {
                final ClassDefinition obj = (ClassDefinition) type;
                final VariableDefinition variable = obj.staticVariables.get(node.getCalled());
                if (variable == null) {
                    return except("Reassigning a variable that does not exist: " + node.getCalled() + " in class " + node.getCaller(), node.getLine());
                }
                final Object value = interpretNode(node.getValue(), context);

                if (value == VOID_OBJECT) return except("Cannot assign void to a variable.", node.getLine());
                variable.setValue(value);
                return VOID_OBJECT;
            }
        }

        if (!(callerObjectRaw instanceof ClassObject)) {
            return except("Getting variable of " + callerObjectRaw.getClass().getSimpleName() + ", expected Class Object", node.getLine());
        }

        final Object value = interpretNode(node.getValue(), context);

        if (value == VOID_OBJECT) return except("Cannot assign void to a variable.", node.getLine());

        ((ClassObject) callerObjectRaw).getContext().setVariable(node.getCalled(), value);
        return VOID_OBJECT;
    }


    private void interpretInclude(final IncludeNode node, final Context context) {
        if (node.isNative()) context.getNatives().add(node.getName());

        for (final ASTNode astnode : node.getBlock()) {
            if (preInterpret(astnode, node.getBlock(), context)) {
                node.getBlock().getChildren().remove(astnode);
                continue;
            }
            final Object result = interpretNode(astnode, context);

            if (result instanceof ReturnValue) {
                return;
            }
        }
    }

    private Object interpretPackagedNativeFunctionCall(final PackagedNativeFunctionCallNode node, final Context context) {
        final List<Object> params = new ArrayList<>();
        for (final ASTNode param : node.getParams()) params.add(interpretNode(param, context));

        Register.register(NATIVECALL,  node.getName() + getParams(params), node.getLine(), context.getCurrentClassName());


        final ObjectType rawCurrent =  context.getClass(context.getCurrentClassName());

        if (!(rawCurrent instanceof ClassDefinition)) {
            return nmh.callPackaged(node.getPackage(), node.getName(), context.getCurrentClassName(), params);
        }

        final ClassDefinition current = (ClassDefinition) rawCurrent;

        if (current.isNative()) {
            // return value cuz single block!
            final Object ret = nmh.callClassNative(current.getName(), node.getName(), params, context);
            if (ret == VOID_OBJECT) return ret;
            return new ReturnValue(ret);
        }
        return nmh.callPackaged(node.getPackage(), node.getName(), context.getCurrentClassName(), params);
    }

    private Object interpretArray(final ArrayNode node, final Context context) {
        final Object[] objects = new Object[node.getNodes().size()];

        List<ASTNode> nodes = node.getNodes();
        for (int i = 0, nodesSize = nodes.size(); i < nodesSize; i++) {
            final ASTNode astNode = nodes.get(i);
            final Object result = interpretNode(astNode, context);

            if (result == VOID_OBJECT) return except("Adding void as a object in an array.", astNode.getLine());

            objects[i] = result;
        }

        return objects;
    }

    public static String getParams(final List<?> objects) {
        final StringBuilder s = new StringBuilder("(");
        for (int i = 0, size = objects.size(); i < size; i++) {
            final Object o = objects.get(i);
            s.append(getAsCLRStr(o));
            if (i < size - 1) s.append(", ");
        }
        return s + ")";
    }

    public static String getAsCLRStr(final Object o) {
        if (o == null) {
            return "null";
        }
        if (o instanceof Long || o instanceof Integer) return "int";
        else if (o instanceof Double) return "float";
        else if (o instanceof Object[]) return "arr";
        else if (o instanceof String) return "str";
        else if (o instanceof VoidObject) return "void";
        else if (o instanceof LambdaObject) return "lambda";
        else return o.getClass().getSimpleName().toLowerCase();
    }

    private Object interpretIf(final IfNode node, final Context context) {
        final ASTNode expr = node.getCondition();
        final boolean condition = checkCondition(expr, context);

        if (condition) {
            Object val = interpretBlock(node.getIfBlock(), context);
            if (val != VOID_OBJECT)
                return new ReturnValue(val);
        } else {
            LABEL_LOOP:
            {
                for (final IfNode ifNode : node.getElseIfStatements()) {
                    if (checkCondition(ifNode.getCondition(), context)) {
                        Object val = interpretBlock(ifNode.getIfBlock(), context);
                        if (val != VOID_OBJECT)
                            return new ReturnValue(val);
                        break LABEL_LOOP;
                    }
                }
                if (node.getElseBlock() != null) {
                    Object val = interpretBlock(node.getElseBlock(), context);
                    if (val != VOID_OBJECT)
                        return new ReturnValue(val);
                }
            }
        }

        return VOID_OBJECT;
    }

    private boolean checkCondition(final ASTNode expr, final Context context) {
        final Object rawConditionValue = interpretNode(expr, context);
        final boolean condition;
        if (rawConditionValue instanceof Long || rawConditionValue instanceof Byte) {
            final long num = (long) rawConditionValue;
            if (num == 0) {
                condition = false;
            } else if (num == 1) {
                condition = true;
            } else {
                except("Integer condition must be 1 or 0", expr.getLine());
                condition = false;
            }
        } else if (!(rawConditionValue instanceof Boolean)) {
            except("Condition is not boolean", expr.getLine());
            condition = false;
        } else {
            condition = (boolean) rawConditionValue;
        }
        return condition;
    }

    private Object interpretFor(final ForNode node, final Context raw) {

        final Context FOR_CONTEXT = new Context(raw);
        Context BLOCK_CONTEXT = new Context(FOR_CONTEXT);

        if (node.getDeclaration() != null && interpretNode(node.getDeclaration(), FOR_CONTEXT) != VOID_OBJECT)
            return except("for declaration must be void return", node.getLine());

        final ASTNode condition = node.getCondition();

        while (node.getDeclaration() == null || checkCondition(condition, FOR_CONTEXT)) {
            final Object val = interpretBlock(node.getBlock(), BLOCK_CONTEXT);
            if (val == BREAK) {
                break;
            }
            if (val == CONTINUE) {
                continue;
            }
            if (val != VOID_OBJECT) {
                return new ReturnValue(val);
            }



            interpretNode(node.getIncrementation(), BLOCK_CONTEXT);

            BLOCK_CONTEXT = new Context(FOR_CONTEXT);
        }

        return VOID_OBJECT;
    }

    private Object interpretWhile(final WhileNode node, final Context context) {
        Context whileContext = new Context(context);

        final ASTNode condition = node.getCondition();


        while (checkCondition(condition, whileContext)) {
            final Object val = interpretBlock(node.getBlock(), whileContext);
            if (val == BREAK) {
                break;
            }
            if (val == CONTINUE) {
                continue;
            }
            if (val != VOID_OBJECT) {
                return new ReturnValue(val);
            }
            whileContext = new Context(context);
        }

        return VOID_OBJECT;
    }

    private Object interpretForeach(final ForeachNode node, final Context context) {

        Context forEachContext = new Context(context);

        final Object object = interpretNode(node.getList(), context);

        if (object == VOID_OBJECT) {
            return except("Void type not allowed in foreach", node.getLine());
        }
        if (object == null) {
            return except("Null list in foreach", node.getLine());
        }

        Object[] arr;

        if (object instanceof Object[]) {
            arr = (Object[]) object;
        } else if (object instanceof Long) {
            final long range = (long) object;
            long i = 0;

            while (i < range) {
                forEachContext.defineVariable(node.getVariable(), new VariableDefinition(node.getVariable(), null, i, false, false, false));
                final Object val = interpretBlock(node.getBlock(), forEachContext);
                if (val == BREAK) {
                    break;
                } else if (val == CONTINUE) {
                    continue;
                } else if (val != VOID_OBJECT) {
                    return new ReturnValue(val);
                }
                i++;

                forEachContext = new Context(context);
            }

            return VOID_OBJECT;
        } else if (object instanceof Double) {
            final double range = (double) object;
            double i = 0;
            while (i < range) {
                forEachContext.defineVariable(node.getVariable(), new VariableDefinition(node.getVariable(), null, VOID_OBJECT, false, false, false));
                final Object val = interpretBlock(node.getBlock(), forEachContext);
                if (val == BREAK) {
                    break;
                }
                if (val == CONTINUE) {
                    continue;
                }
                if (val != VOID_OBJECT) {
                    return new ReturnValue(val);
                }
                i++;
                forEachContext = new Context(context);

            }

            return VOID_OBJECT;
        } else {
            return except("Expected list, array, or integer in foreach, but got " + object.getClass().getSimpleName(), node.getLine());
        }

        forEachContext.defineVariable(node.getVariable(), new VariableDefinition(node.getVariable(), null, null, false, false, false));

        for (final Object o : arr) {
            forEachContext.setVariable(node.getVariable(), o);
            final Object val = interpretBlock(node.getBlock(), forEachContext);
            if (val == BREAK) {
                break;
            }
            if (val == CONTINUE) {
                continue;
            }
            if (val != VOID_OBJECT) {
                return new ReturnValue(val);
            }
            forEachContext = new Context(context);
            forEachContext.defineVariable(node.getVariable(), new VariableDefinition(node.getVariable(), null, null, false, false, false));
        }

        return VOID_OBJECT;
    }

    private Object interpretReflectedNativeFunctionDeclaration(final ReflectedNativeFunctionDeclaration node, final Context context) {
        final BlockNode block = new BlockNode();

        final List<ASTNode> nodes = new ArrayList<>();

        for (final ParameterNode parameterNode : node.getParams())
            nodes.add(new VariableReferenceNode(parameterNode.getName()));

        final String pkg = node.getFileName().substring(0, node.getFileName().length() - 3);

        final ASTNode astnode = new PackagedNativeFunctionCallNode(
                node.getName(),
                pkg.substring(0, 1).toUpperCase() + pkg.substring(1),
                nodes
        ).setLine(node.getLine());

        block.addChild(astnode);

        final List<String> params = new ArrayList<>();

        for (final ParameterNode param : node.getParams()) params.add(param.getName());

        final FunctionDefinition function = new FunctionDefinition(
                node.getName(),
                node.getTypeDefault(),
                node.isStatic(),
                node.isConst(),
                node.isLocal(),
                node.isAsync(),
                params,
                block
        );

        if (node.isStatic()) {
            final ObjectType obj = context.getClass(node.getFileName());
            if (obj != VOID_OBJECT) ((ClassDefinition) obj).staticFunctions.add(function);
            else return function;
        } else {
            context.defineFunction(node.getName(), function);
        }

        return VOID_OBJECT;
    }

    private Object interpretNativeClassDeclaration(final NativeClassDeclarationNode node, final Context context) {
        final String name = node.getName();
        final String ocn = context.getCurrentClassName();

        context.setCurrentClassName(name);

        // no need to check, native class do not have errors (we hope)
        final ClassDefinition inheritedClass = (ClassDefinition) context.getClass(node.getInheritedClass());

        final ClassDefinition definition = new ClassDefinition(
                name,
                node.isConstant(),
                inheritedClass,
                null, // natives can not extend
                getConstructors(node.getConstructors()),
                node.getBlock(),
                true
        );
        context.defineClass(name, definition);

        if (!context.getNatives().contains(node.getFileName())) Privileges.checkClassName(name, node.getLine());

        definition.getBody().forEach(statement -> {

            if (statement instanceof VariableDeclarationNode) {
                VariableDeclarationNode declarationNode = (VariableDeclarationNode) statement;
                if (declarationNode.isStatic()) {
                    definition.staticVariables.put(declarationNode.getName(), new VariableDefinition(declarationNode.getName(), declarationNode.getTypeDefault(), declarationNode.getValue() == null ? null : interpretNode(declarationNode.getValue(), context), declarationNode.isConstant(), true, declarationNode.isLocal()));
                }
            } else if (statement instanceof FunctionDeclarationNode) {
                FunctionDeclarationNode declarationNode = (FunctionDeclarationNode) statement;
                final List<String> params = new ArrayList<>();

                declarationNode.getParameterNodes().forEach(param -> params.add(param.getName()));

                if (declarationNode.isStatic()) {
                    definition.staticFunctions.add(new FunctionDefinition(declarationNode.getFunctionName(), declarationNode.getTypeDefault(), true, declarationNode.isConst(), declarationNode.isLocal(), declarationNode.isAsync(), params, declarationNode.getBlock()));
                }
            } else if (statement instanceof ReflectedNativeFunctionDeclaration) {
                final Object o = interpretReflectedNativeFunctionDeclaration((ReflectedNativeFunctionDeclaration) statement, context);
                if (o instanceof FunctionDefinition) {
                    FunctionDefinition def = (FunctionDefinition) o;
                    definition.staticFunctions.add(def);
                }

            }
        });

        context.setCurrentClassName(ocn);
        return VOID_OBJECT;
    }

    private Object interpretLocalVariableReassignment(final LocalVariableReassignmentNode node, final Context context) {
        final Context localContext = context.parentContext();
        final Object result = interpretNode(node.getValue(), context);
        if (result instanceof VoidObject) return except("Reassigning variable with void value: " + node.getName(), node.getLine());
        localContext.setVariable(node.getName(), result);
        return VOID_OBJECT;
    }

    private Object interpretSelect(final SelectNode node, final Context context) {

        final Object value = interpretNode(node.getCondition(), context);
        if (value == VOID_OBJECT) return except("Void condition in switch expression", node.getLine());

        boolean match = false;
        List<SelectNode.WhenNode> cases = node.getCases();
        for (final SelectNode.WhenNode whenCase : cases) {
            if (match || Objects.equals(value, interpretNode(whenCase.getWhenExpression(), context))) {
                final Object ret = interpretBlock(whenCase.getBlock(), context);
                match = true;
                if (ret == BREAK) {
                    return VOID_OBJECT;
                } else if (ret == CONTINUE) {
                    continue;
                }

                if (ret != VOID_OBJECT) {
                    return new ReturnValue(ret);
                }
            }
        }

        return interpretBlock(node.getBlock(), context);
    }

    private Object interpretNativeCast(final NativeCastNode node, final Context context) {
        final Object expression = interpretNode(node.getCasted(), context);

        if (expression == null) {
            return node.getType() == CastType.FLOAT ? 0d : 0;
        }

        if (node.getType().equals(STR)) {
            return castToStr(expression, node);
        } else if (node.getType().equals(FLOAT)) {
            return castToFloat(expression, node);
        } else if (node.getType().equals(INT)) {
            return castToInt(expression, node);
        } else if (node.getType().equals(ARR)) {
            return castToArr(expression, node);
        } else if (node.getType().equals(BOOL)) {
            return castToBool(expression, node);
        }
        return except("Unknown cast: " + node.getType().getClass().getSimpleName(), node.getLine());
    }

    private String castToStr(final Object expr, final NativeCastNode node) {
        if (expr instanceof String) {
            return (String) expr;
        }
        if (expr instanceof Long) {
            return Long.toString((Long) expr);
        }
        if (expr instanceof Double) {
            return Double.toString((Double) expr);
        }
        if (expr instanceof Boolean) {
            return Boolean.toString((Boolean) expr);
        }
        if (expr instanceof Object[]) {
            return Arrays.toString((Object[]) expr);
        }

        except("Could not cast to string", node.getLine());
        return "";
    }

    private Double castToFloat(final Object expression, final NativeCastNode node) {
        if (expression instanceof String) {
            return parseDoubleOrThrow((String) expression, node);
        }
        if (expression instanceof Long) {
            return ((Long) expression).doubleValue();
        }
        if (expression instanceof Double) {
            return (Double) expression;
        }

        except("Could not cast to float", node.getLine());
        return 0D;
    }

    private Long castToInt(final Object expression, final NativeCastNode node) {
        if (expression instanceof String) {
            return parseIntegerOrThrow((String) expression, node);
        }
        if (expression instanceof Double) {
            return ((Double) expression).longValue();
        }
        if (expression instanceof Long) {
            return (Long) expression;
        }

        except("Could not cast to int", node.getLine());
        return 0L;
    }

    private Object[] castToArr(final Object expression, final NativeCastNode node) {
        try {
            return (Object[]) expression;
        } catch (final ClassCastException ignore) {
            except("Could not cast to arr", node.getLine());
            return new Object[0];
        }
    }

    private Boolean castToBool(final Object expression, final NativeCastNode node) {
        try {
            if (expression instanceof Long) {
                final long val = (long) expression;
                if (val == 0) {
                    return false;
                } else if (val == 1) {
                    return true;
                }
            } else if (expression instanceof String) {
                final String val = (String) expression;
                if (val.equals("true")) {
                    return true;
                } else if (val.equals("false")) {
                    return false;
                }
            }
            return (boolean) expression;
        } catch (final ClassCastException ignore) {
            except("Could not cast to bool", node.getLine());
            return false;
        }
    }


    private Double parseDoubleOrThrow(final String expression,final  NativeCastNode node) {
        try {
            return Double.parseDouble(expression);
        } catch (final NumberFormatException e) {
            except("Could not cast to float", node.getLine());
            return 0D;
        }
    }

    private Long parseIntegerOrThrow(final String expression, final NativeCastNode node) {
        try {
            return Tokenizer.processNumber(expression).longValue();
        } catch (final NumberFormatException e) {
            if (expression.contains(".")) {
                try {
                    return (long) Tokenizer.processNumber(expression.split("\\.")[0]);
                } catch (final NumberFormatException ignored) {
                    except("Could not cast to int", node.getLine());
                }
            }
            except("Could not cast to int", node.getLine());
            return 0L;
        }
    }

    private Object interpretConditionedReturn(final ConditionedReturnNode node, final Context context) {
        final ASTNode condo = node.getCondition();

        final Object result = interpretNode(condo, context);

        final boolean apply;
        if (result instanceof Long) {
            final int val = ((Number) result).intValue();
            if (val == 0) {
                apply = false;
            } else if (val == 1) {
                apply = true;
            } else {
                return except("Conditioned return with int condition must be 0 or 1", node.getLine());
            }
        } else if (!(result instanceof Boolean)) {
            return except("Conditioned return without a boolean condition", node.getLine());
        } else {
            apply = (boolean) result;
        }

        return apply ? new ReturnValue(interpretNode(node.getValue(), context)) : VOID_OBJECT;

    }

    private Object interpretMemberFunctionCall(final MemberFunctionCallNode node, final Context context) {
        final Object caller = node.getCaller() instanceof VariableReferenceNode ?
                context.getVariable(((VariableReferenceNode) node.getCaller()).getName()) :
                interpretNode(node.getCaller(), context);

        if (caller == VOID_OBJECT) {
            final String className = ((VariableReferenceNode) node.getCaller()).getName();

            final ObjectType rawClassDefinition = context.getClass(className);

            if (!(rawClassDefinition instanceof ClassDefinition)) {
                return except("Class not found for static call: " + node.getName(), node.getLine());
            }

            final ClassDefinition classDefinition = (ClassDefinition) rawClassDefinition;

            if (classDefinition instanceof EnumClassDefinition) {
                final int params = node.getParams().size();
                final EnumClassDefinition ecd = (EnumClassDefinition) classDefinition;
                switch (node.getName()) {
                    case "values": {
                        if (params != 0) break;
                        return ecd.getValues().toArray(new EnumClassDefinition.EnumValue[0]);
                    }
                    case "index": {
                        if (params != 1) break;
                        final Object rawIndex = interpretNode(node.getParams().get(0), context);
                        if (!(rawIndex instanceof Long)) break;
                        return ecd.getValues().toArray(new EnumClassDefinition.EnumValue[0])[(int) rawIndex];
                    }
                    case "valueOf": {
                        if (params != 1) break;
                        final Object rawLiteral = interpretNode(node.getParams().get(0), context);
                        if (!(rawLiteral instanceof String)) break;
                        final String valueOfLiteral = (String) rawLiteral;
                        for (final EnumClassDefinition.EnumValue value : ecd.getValues()) {
                            if (value.getName().equals(valueOfLiteral)) return value;
                        }
                        return except("Enum value not found by name " + valueOfLiteral, node.getLine());
                    }
                    default: {
                        break;
                    }
                }

                final List<Object> paramsList = getFunctionParameters(node, context, params);

                return except("Static function not found: " + classDefinition.getName() + "#" + node.getName() + getParams(paramsList), node.getLine());
            }

            final FunctionDefinition definition = classDefinition.getStaticFunction(node.getName(), node.getParams().size());

            if (definition == null) {
                return except("Static function not found: " + classDefinition.getName() + "#" + node.getName(), node.getLine());
            }

            final Context functionContext = new Context(context);
            final List<Object> params = getFunctionParameters(node, context, definition.getParams().size());

            defineFunctionParameters(functionContext, definition, params);

            Register.register(NATIVECALL, node.getName() + getParams(definition.getParams()), node.getLine(), context.getCurrentClassName());

            final String preName = context.getCurrentClassName();
            final String ocn = context.getCurrentClassName();

            context.setCurrentClassName(classDefinition.getName());
            context.setCurrentFunctionName(node.getName());

            if (definition.isLocal() && !classDefinition.getName().equals(preName)) {
                return except("Accessing a local function: " + definition.getName(), node.getLine());
            }

            final Object result = interpretBlock(definition.getBlock(), functionContext);



            context.setCurrentClassName(ocn);
            context.setCurrentFunctionName(null);
            return result;
        }
        if (caller instanceof VoidObject) {
            return handleStaticFunctionCall(node, context);
        } else if (caller instanceof EnumClassDefinition.EnumValue) {
            return handleEnumValueFunctionCall(node, (EnumClassDefinition.EnumValue) caller);
        } else if (caller instanceof Object[]) {
            final Object resultArray = handleArrayFunctionCall(node, context, (Object[]) caller);
            if (resultArray instanceof Object[] && ((Object[]) resultArray).length != ((Object[]) caller).length) {
                if (!(node.getCaller() instanceof VariableReferenceNode)) {
                    return except("Expected variable reference", node.getLine());
                }
                context.setVariable(((VariableReferenceNode) node.getCaller()).getName(), resultArray);
                return VOID_OBJECT;
            }
            return resultArray;
        } else if (caller instanceof String) {
            return handleStringFunctionCall(node, context, (String) caller);
        } else if (caller instanceof LambdaObject) {
            return handleLambdaFunctionCall(node, context, (LambdaObject) caller);
        } else if (caller == null) {
            return except("You can't call a function out of null", node.getLine());
        } else if (!(caller instanceof ClassObject)) {
            return except("You can't call a function out of a " + caller.getClass().getSimpleName(), node.getLine());
        }

        final ClassObject object = (ClassObject) caller;

        final String objectName = object.getName();

        final ObjectType rawDefinition = object.getContext().getFunction(node.getName(), node.getParams().size());

        if (!(rawDefinition instanceof FunctionDefinition)) {
            return except("Instance function not found: " + objectName + "#" + node.getName() + getParams(getFunctionParameters(node, context, node.getParams().size())), node.getLine());
        }

        final FunctionDefinition definition = (FunctionDefinition) rawDefinition;

        if (definition.isLocal() && !object.getName().equals(context.getCurrentClassName())) {
            return except("Accessing a local function: " + definition.getName(), node.getLine());
        }

        final Context functionContext = new Context(object.getContext());
        final List<Object> params = getFunctionParameters(node, context, definition.getParams().size());

        defineFunctionParameters(functionContext, definition, params);

        Register.register(FUNCALL, node.getName() + getParams(definition.getParams()), node.getLine(), context.getCurrentClassName());
        final String ocn = context.getCurrentClassName();

        context.setCurrentClassName(objectName);
        context.setCurrentFunctionName(node.getName());

        final Object result = interpretBlock(definition.getBlock(), functionContext);

        context.setCurrentClassName(ocn);
        context.setCurrentFunctionName(null);

        return result;
    }

    private List<Object> getFunctionParameters(final MemberFunctionCallNode node, final Context context, int expectedSize) {
        final List<Object> params = new ArrayList<>();
        for (final ASTNode param : node.getParams()) {
            final Object returned = interpretNode(param, context);
            if (returned == VOID_OBJECT) {
                except("Passing void as a parameter function", node.getLine());
                return new ArrayList<>();
            }
            params.add(returned);
        }

        if (expectedSize != -1 && (params.size() > expectedSize || params.size() < expectedSize)) {
            except("Parameter size mismatch. Expected: " + expectedSize + ", Found: " + params.size(), node.getLine());
            return new ArrayList<>();
        }
        return params;
    }

    private List<Object> getFunctionParameters(final FunctionCallNode node, final Context context, int expectedSize) {
        final List<Object> params = new ArrayList<>();
        for (final ASTNode param : node.getParams()) {
            final Object returned = interpretNode(param, context);
            if (returned == VOID_OBJECT) {
                except("Passing void as a parameter function", node.getLine());
                return new ArrayList<>();
            }
            params.add(returned);
        }

        if (expectedSize != -1 && (params.size() > expectedSize || params.size() < expectedSize)) {
            except("Parameter size mismatch. Expected: " + expectedSize + ", Found: " + params.size(), node.getLine());
            return new ArrayList<>();
        }
        return params;
    }

    private Object interpretAssert(final AssertNode node, final Context context) {
        final Object result = interpretNode(node.getCondition(), context);
        final boolean apply;
        if (result instanceof Long) {
            final int val = ((Long) result).intValue();
            if (val == 0) {
                apply = false;
            } else if (val == 1) {
                apply = true;
            } else {
                return except("Assert condition with int must be 0 or 1", node.getLine());
            }
        } else if (!(result instanceof Boolean)) {
            return except("Assert condition must have a boolean value or int (0 or 1)", node.getLine());
        } else {
            apply = (boolean) result;
        }

        if (!apply) {
            final Object value = interpretNode(node.getOrElse(), context);
            if (value instanceof VoidObject) {
                return except("Assert result can not be void!", node.getLine());
            }
            return except(String.valueOf(value), node.getLine());
        }

        return VOID_OBJECT;
    }

    private boolean interpretIs(final IsNode node, final Context context) {
        return node.getType().is(interpretNode(node.getExpression(), context));
    }

    private Object interpretEnumDeclaration(final EnumDeclarationNode node, final Context context) {
        final String name = node.getName();

        final List<EnumClassDefinition.EnumValue> enumValues = new ArrayList<>();
        for (final EnumDeclarationNode.EnumValueNode val : node.getEnumValues()) {
            final String valName = val.getName();
            final ASTNode valObj = val.getValue();
            enumValues.add(new EnumClassDefinition.EnumValue(valName, interpretNode(valObj, context)));
        }

        final EnumClassDefinition definition = new EnumClassDefinition(name, node.isConstant(), enumValues);

        context.defineClass(name, definition);

        return VOID_OBJECT;
    }

    private Object interpretAnnotationDeclaration(final AnnotationDeclarationNode node, final Context context) {
        final AnnotationDefinition definition = new AnnotationDefinition(node.getName(), node.getAnnotationElements());
        context.defineAnnotation(definition.getName(), definition);
        return VOID_OBJECT;
    }

    private Object interpretAnnotationUse(final AnnotationUseNode node, final Context context) {
        /*
        Some day I'll implement this
        context.addCurrentAnnotationName(node.getName());

        final List<AnnotationDefinition.AnnotationValue> values = new ArrayList<>();
        for (final AnnotationUseNode.AnnotationValueAssign value : node.getValues()) {
            final Object objval = interpretNode(value.getValue(), context);
            if (objval instanceof VoidObject) except("Assigning annotation parameter as void", node.getLine());
            values.add(new AnnotationDefinition.AnnotationValue(value.getName(), objval));
        }

        interpretNode(node.getFollowing(), context);
        context.removeCurrentAnnotationName(node.getName());
        */
        final ObjectType rawDefinition = context.getAnnotation(node.getName());
        if (!(rawDefinition instanceof AnnotationDefinition)) {
            return except("Annotation not found: " + node.getName(), node.getLine());
        }

        final AnnotationDefinition annotation = (AnnotationDefinition) rawDefinition;
        if (annotation.getAnnotationElements().size() != node.getValues().size()) {
            return except("Not all required elements are declared in annotation: " + node.getName(), node.getLine());
        }
        return VOID_OBJECT;
    }

    private Object interpretAsyncBlock(final AsyncBlockNode node, final Context context) {
        final Object rawName = interpretNode(node.getName(), context);

        if (rawName instanceof VoidObject) {
            return except("Unexpected async thread name (void)", node.getLine());
        } else if (!(rawName instanceof String)) {
            return except("Unexpected async thread name (" + rawName + "), expected string", node.getLine());
        }
        new Thread(() -> interpretBlock(node.getBlock(), context), rawName.toString()).start();
        return VOID_OBJECT;
    }

    private Object interpretRaise(final RaiseNode node, final Context context) {
        final Object result = interpretNode(node.getException(), context);

        if (!(result instanceof String)) {
            return except("Expected string in exception raising", node.getLine());
        } else {
            return except((String) result, node.getLine());
        }
    }

    private Object interpretTryExcept(final TryExceptBlock node, final Context context) {
        final Context tryContext = new Context(context);

        final BlockNode tryBlock = node.getTryBlock();
        if (tryBlock == null || tryBlock.isEmpty()) {
            return VOID_OBJECT;
        }

        for (final ASTNode anode : tryBlock) {
            final Object result = interpretNode(anode, tryContext);

            if (exemptionHandler.changeIfGet()) {
                final BlockNode exceptBlock = node.getExceptBlock();
                final Context exceptContext = new Context(context);
                exceptContext.defineVariable(node.getExcepted(), new VariableDefinition(node.getExcepted(), "str", exemptionHandler.exemptMessage(), false, false, false));
                interpretBlock(exceptBlock, exceptContext);
                break;
            }

            if (result instanceof ReturnValue) {
                return ((ReturnValue) result).getValue();
            } else if (result instanceof BreakValue) {
                return BREAK;
            } else if (result instanceof ContinueValue) {
                return CONTINUE;
            }
        }
        return VOID_OBJECT;
    }

    private Object interpretStaticBlock(final StaticBlockNode node, final Context context) {
        Register.register(STATICINIT, "<static-block>", node.getLine(), context.getCurrentClassName());
        if (node.isAsync()) {
            new Thread(() -> {
                if (!(interpretBlock(node.getBlock(), context) instanceof VoidObject)) {
                    except("Return in static async block", node.getLine());
                }
            }, "<static-block>").start();
        } else {
            if (!(interpretBlock(node.getBlock(), context) instanceof VoidObject)) {
                except("Return in static block", node.getLine());
            }
        }

        return VOID_OBJECT;
    }

    private Object interpretTernaryOperator(final TernaryOperatorNode node, final Context context) {
        // ternary for ternary, ironic, isn't it?
        return checkCondition(node.getCondition(), context) ? interpretNode(node.getTrueBranch(), context) : interpretNode(node.getFalseBranch(), context);
    }

    private Object interpretLambdaBlock(final LambdaBlockNode node, final Context context) {
        return new LambdaObject(node.getParams(), node.getBlock(), context);
    }

    private Object interpretDeleteVariable(final DeleteVariableNode node, final Context context) {
        Register.register(VARDEL, node.getName(), node.getLine(), context.getCurrentClassName());
        context.deleteVariable(node.getName());
        return VOID_OBJECT;
    }

    private Object interpretDeleteFunction(final DeleteFunctionNode node, final Context context) {
        final Object params = interpretNode(node.getParams(), context);

        if (!(params instanceof Number)) {
            if (params == null) {
                return except("Parameter is not a num, instead it is null", node.getLine());
            }
            return except("Parameter is not a num, instead it is " + params.getClass().getSimpleName(), node.getLine());
        }

        Register.register(FUNDEL, node.getName() + "(" + params + ")", node.getLine(), context.getCurrentClassName());

        context.deleteFunction(node.getName(), ((Number) params).intValue());
        return VOID_OBJECT;
    }

    private Object interpretAwaitBlock(final AwaitBlockNode node, final Context context) {
        final CompletableFuture<Object> future = CompletableFuture.supplyAsync(
                () -> interpretBlock(node.getBlock(), context),
                ASYNC_POOL
        );
        try {
            return future.get();
        } catch (final InterruptedException | ExecutionException e) {
            return except("Unknown exception in await block", node.getLine());
        }
    }

    private Object interpretAwaitFunctionCall(final AwaitFunctionCallNode node, final Context context) {
        final FunctionCallNode call = node.getFunctionCallNode();

        final String functionName = ((VariableReferenceNode) call.getCaller()).getName();
        context.setCurrentFunctionName(functionName);

        final List<Object> params = new ArrayList<>(call.getParams().size());
        for (final ASTNode param : call.getParams()) {
            final Object returned = interpretNode(param, context);
            if (returned == VOID_OBJECT) {
                return except("Passing void as a parameter function: " + param.getClass().getSimpleName() + ", fn: " + functionName, call.getLine());
            }
            params.add(returned);
        }

        final ObjectType type = context.getFunction(functionName, params.size());
        final FunctionDefinition definition;

        if (type == VOID_OBJECT) {
            return except("Calling a function that doesn't exist: " + functionName + getParams(params) + ", please remember that lambdas are NOT supported here", call.getLine());
        }

        definition = (FunctionDefinition) type;


        final int paramSize = params.size();
        final int expectedSize = definition.getParams().size();

        if (paramSize != expectedSize) {
            return except("Incorrect parameter count (" + paramSize + " vs " + expectedSize + ") in fn: " + functionName, call.getLine());
        }

        final Context functionContext = new Context(context.parentContext());
        final List<String> definitionParams = definition.getParams();
        for (int i = 0; i < definitionParams.size(); i++) {
            final String name = definitionParams.get(i);
            final Object value = params.get(i);
            functionContext.defineVariable(name, new VariableDefinition(name, null, value, false, false, false));
        }

        Register.register(FUNCALL, functionName + getParams(params), call.getLine(), context.getCurrentClassName());

        final Object result;

        if (definition.isAsync()) {
            final CompletableFuture<Object> future = CompletableFuture.supplyAsync(
                    () -> interpretBlock(definition.getBlock(), functionContext),
                    ASYNC_POOL
            );

            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                return except("Unknown error while handling await function.", node.getLine());
            }
        } else {
            result = interpretBlock(definition.getBlock(), functionContext);
        }



        if (checkTypes(definition.getTypeDefault(), result)) {
            return except("Unexpected return: " + getAsCLRStr(result) + ", expected " + definition.getTypeDefault(), call.getLine());
        }

        context.setCurrentFunctionName(null);
        return result;
    }

    private Object interpretNativeFunction(final NativeFunctionNode node, final Context context) {
        final List<Object> params = new ArrayList<>(node.getParams().size());
        for (final ASTNode param : node.getParams()) {
            final Object added = interpretNode(param, context);
            if (added instanceof VoidObject) {
                return except("Passing void as parameter", node.getLine());
            }
            params.add(added);
        }

        final ClarityNativeFunction<?> fun = node.getFunction();

        Register.register(NATIVECALL, fun.getName() + getParams(params), node.getLine(), context.getCurrentClassName());

        if (fun.applies(fun.getName(), params)) {
            return fun.call(params);
        } else {
            return except("Clarity native function does not apply (try using -nlnat running flag)", node.getLine());
        }

    }

    @StillTesting
    private Object interpretVirtualClassDeclaration(final VirtualClassDeclarationNode node, final Context context) {
        final String ocn = context.getCurrentClassName();
        final String name = node.getName();
        context.setCurrentClassName(name);
        final ClassDefinition inheritedClass;
        final VirtualClassDefinition extendedClass;
        final FunctionDefinition[] constructors = getConstructors(node.getConstructors());
        final BlockNode body = node.getBlock();
        final VirtualFunctionDefinition[] virtualFunctions = new VirtualFunctionDefinition[node.getVirtualFunctions().size()];

        if (node.getExtendedClass() != null) {
            final ObjectType type = context.getClass(node.getExtendedClass());
            if (!(type instanceof VirtualClassDefinition)) {
                if (type instanceof ClassDefinition) {
                    return except(node.getExtendedClass() + " is not a virtual class, so it can not be extended but just inherited.", node.getLine());
                }
                return except("Extended class not found: " + node.getExtendedClass(), node.getLine());
            }
            extendedClass = (VirtualClassDefinition) type;
        } else {
            extendedClass = null;
        }

        if (node.getInheritedClass() != null) {
            final ObjectType type = context.getClass(node.getInheritedClass());
            if (!(type instanceof ClassDefinition)) {
                except("Inherited class not found: " + node.getInheritedClass(), node.getLine());
                return null;
            } else {
                inheritedClass = (ClassDefinition) type;
                if (inheritedClass.isConstant()) {
                    except("Inheriting a const " + (inheritedClass.isNative() ? "native " : "") + "class: " + node.getInheritedClass(), node.getLine());
                }
            }
        } else {
            inheritedClass = null;
        }

        final List<VirtualFunctionDeclarationNode> functions = node.getVirtualFunctions();
        for (int i = 0, functionsSize = functions.size(); i < functionsSize; i++) {
            final VirtualFunctionDeclarationNode virtual = functions.get(i);
            final VirtualFunctionDefinition vfd = new VirtualFunctionDefinition(
                    virtual.getFunctionName(),
                    virtual.getTypeDefault(),
                    virtual.isAsync(),
                    virtual.getParameterNodes().stream().map(ParameterNode::getName).collect(Collectors.toList())
            );

            virtualFunctions[i] = vfd;
        }


        final ClassDefinition definition = new VirtualClassDefinition(name, inheritedClass, extendedClass, constructors, body, virtualFunctions);

        context.defineClass(node.getName(), definition);


        if (!context.getNatives().contains(node.getFileName())) Privileges.checkClassName(name, node.getLine());

        StaticBlockNode staticBlock = null;

        for (final ASTNode statement : definition.getBody()) {
            if (statement instanceof VariableDeclarationNode) {
                final VariableDeclarationNode declarationNode = (VariableDeclarationNode) statement;
                if (declarationNode.isStatic()) {
                    final String dName = declarationNode.getName();

                    final String dType = declarationNode.getTypeDefault();

                    final Object dValue = declarationNode.getValue() == null ? null : interpretNode(declarationNode.getValue(), context);

                    final boolean dConst = declarationNode.isConstant();

                    final boolean dStatic = true;

                    final boolean dLocal = declarationNode.isLocal();

                    definition.staticVariables.put(dName, new VariableDefinition(dName, dType, dValue, dConst, dStatic, dLocal));
                }
            } else if (statement instanceof FunctionDeclarationNode) {
                final FunctionDeclarationNode declarationNode = (FunctionDeclarationNode) statement;
                final List<String> params = new ArrayList<>();

                declarationNode.getParameterNodes().forEach(param -> params.add(param.getName()));

                if (declarationNode.isStatic()) {
                    definition.staticFunctions.add(new FunctionDefinition(declarationNode.getFunctionName(), declarationNode.getTypeDefault(), true, declarationNode.isConst(), declarationNode.isLocal(), declarationNode.isAsync(), params, declarationNode.getBlock()));
                }
            } else if (statement instanceof ReflectedNativeFunctionDeclaration) {
                final Object o = interpretReflectedNativeFunctionDeclaration((ReflectedNativeFunctionDeclaration) statement, context);
                if (o instanceof FunctionDefinition) {
                    FunctionDefinition def = (FunctionDefinition) o;
                    definition.staticFunctions.add(def);
                }
            } else if (statement instanceof StaticBlockNode) {
                if (staticBlock != null) return except("More than one static block found in class " + definition.getName(), statement.getLine());
                staticBlock = (StaticBlockNode) statement;
            }
        }

        // static block is the last to get interpreted
        if (staticBlock != null) interpretStaticBlock(staticBlock, context);

        context.setCurrentClassName(ocn);

        return VOID_OBJECT;
    }

    /*
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
     */

    private void checkExemption() {
        exemptionHandler.checkExemption();
    }

    public Object except(final String message, final int line) {
        return exemptionHandler.except(message, line);
    }

    private void except(final String message) {
        exemptionHandler.except(message);
    }
}