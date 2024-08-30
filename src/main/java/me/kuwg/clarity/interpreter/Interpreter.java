package me.kuwg.clarity.interpreter;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.block.BreakNode;
import me.kuwg.clarity.ast.nodes.block.ContinueNode;
import me.kuwg.clarity.ast.nodes.block.ReturnNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassInstantiationNode;
import me.kuwg.clarity.ast.nodes.clazz.NativeClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.cast.NativeCastNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.call.*;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.ast.nodes.function.declare.ReflectedNativeFunctionDeclaration;
import me.kuwg.clarity.ast.nodes.include.IncludeNode;
import me.kuwg.clarity.ast.nodes.literal.*;
import me.kuwg.clarity.ast.nodes.reference.ContextReferenceNode;
import me.kuwg.clarity.ast.nodes.statements.*;
import me.kuwg.clarity.ast.nodes.variable.assign.LocalVariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.assign.ObjectVariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.get.LocalVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.ObjectVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.VariableReferenceNode;
import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.interpreter.definition.*;
import me.kuwg.clarity.interpreter.exceptions.MultipleMainMethodsException;
import me.kuwg.clarity.register.Register;
import me.kuwg.clarity.interpreter.types.ClassObject;
import me.kuwg.clarity.interpreter.types.ObjectType;
import me.kuwg.clarity.interpreter.types.VoidObject;
import me.kuwg.clarity.nmh.NativeMethodHandler;
import me.kuwg.clarity.privilege.Privileges;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.kuwg.clarity.interpreter.definition.BreakValue.BREAK;
import static me.kuwg.clarity.interpreter.definition.ContinueValue.CONTINUE;
import static me.kuwg.clarity.interpreter.types.VoidObject.VOID_OBJECT;
import static me.kuwg.clarity.interpreter.types.VoidObject.VOID_RETURN;

public class Interpreter {

    private final AST ast;
    private final NativeMethodHandler nmh;
    private final Context general;

    public Interpreter(final AST ast) {
        this.ast = ast;
        this.nmh = new NativeMethodHandler();
        this.general = new Context();
    }

    public int interpret() {

        MainFunctionDeclarationNode main = null;

        for (final ASTNode node : ast.getRoot()) {
            if (node instanceof MainFunctionDeclarationNode) {
                if (main != null) throw new MultipleMainMethodsException();
                main = (MainFunctionDeclarationNode) node;
                ast.getRoot().getChildrens().remove(node);
            } else {
                preInterpret(node, ast.getRoot(), general);
            }

        }

        if (main != null) {

            Register.register(new Register.RegisterElement(Register.RegisterElementType.FUNCALL, "main", main.getLine(), "none"));

            final Object result = interpretNode(main.getBlock(), general);

            if (result == VOID_OBJECT || result == VOID_RETURN || result == null) {
                return 0;
            } else if (!(result instanceof Integer)) {
                System.err.println("[WARNING] Main function does not return an integer, but returns instead: " + result.getClass().getSimpleName());
                return 1;
            } else {
                return (int) result;
            }
        } else {
            ast.getRoot().forEach(node -> interpretNode(node, general));
            return 0;
        }

    }

    private void preInterpret(final ASTNode node, final BlockNode block, final Context context) {
        if (node instanceof FunctionDeclarationNode) {
            interpretFunctionDeclaration((FunctionDeclarationNode) node, context);
            block.getChildrens().remove(node);
        } else if (node instanceof ClassDeclarationNode) {
            final ClassDeclarationNode cdn = (ClassDeclarationNode) node;
            if (context.getClass(cdn.getName()) == VOID_OBJECT) interpretClassDeclaration(cdn, context);
            block.getChildrens().remove(node);
        } else if (node instanceof IncludeNode) {
            interpretInclude((IncludeNode) node, context);
            block.getChildrens().remove(node);
        } else if (node instanceof ReflectedNativeFunctionDeclaration) {
            final ReflectedNativeFunctionDeclaration reflected = (ReflectedNativeFunctionDeclaration) node;
            if (reflected.isStatic()) interpretReflectedNativeFunctionDeclaration(reflected, context);
        }
    }

    private Object interpretNode(final ASTNode node, final Context context) {
        if (node instanceof BlockNode) return interpretBlock((BlockNode) node, context);
        if (node instanceof VariableDeclarationNode) return interpretVariableDeclaration((VariableDeclarationNode) node, context);
        if (node instanceof BinaryExpressionNode) return interpretBinaryExpressionNode((BinaryExpressionNode) node, context);
        if (node instanceof DefaultNativeFunctionCallNode) return interpretDefaultNativeFunctionCall((DefaultNativeFunctionCallNode) node, context);
        if (node instanceof IntegerNode) return ((IntegerNode) node).getValue();
        if (node instanceof DecimalNode) return ((DecimalNode) node).getValue();
        if (node instanceof LiteralNode) return ((LiteralNode) node).getValue();
        if (node instanceof VariableReferenceNode) return interpretVariableReference((VariableReferenceNode) node, context);
        if (node instanceof FunctionCallNode) return interpretFunctionCall((FunctionCallNode) node, context);
        if (node instanceof ReturnNode) return interpretReturnNode((ReturnNode) node, context);
        if (node instanceof ClassInstantiationNode) return interpretClassInstantiation((ClassInstantiationNode) node, context);
        if (node instanceof FunctionDeclarationNode) return interpretFunctionDeclaration((FunctionDeclarationNode) node, context);
        if (node instanceof ClassDeclarationNode) return interpretClassDeclaration((ClassDeclarationNode) node, context);
        if (node instanceof ContextReferenceNode) return interpretContextReference(context);
        if (node instanceof VariableReassignmentNode) return interpretVariableReassignment((VariableReassignmentNode) node, context);
        if (node instanceof ObjectFunctionCallNode) return interpretObjectFunctionCall((ObjectFunctionCallNode) node, context);
        if (node instanceof LocalVariableReferenceNode) return interpretLocalVariableReferenceNode((LocalVariableReferenceNode) node, context);
        if (node instanceof LocalFunctionCallNode) return interpretLocalFunctionCallNode((LocalFunctionCallNode) node, context);
        if (node instanceof ObjectVariableReferenceNode) return interpretObjectVariableReference((ObjectVariableReferenceNode) node, context);
        if (node instanceof ObjectVariableReassignmentNode) return interpretObjectVariableReassignment((ObjectVariableReassignmentNode) node, context);
        if (node instanceof VoidNode) return VOID_RETURN;
        if (node instanceof IncludeNode) return interpretInclude((IncludeNode) node, context);
        if (node instanceof PackagedNativeFunctionCallNode) return interpretPackagedNativeFunctionCall((PackagedNativeFunctionCallNode) node, context);
        if (node instanceof ArrayNode) return interpretArray((ArrayNode) node, context);
        if (node instanceof IfNode) return interpretIf((IfNode) node, context);
        if (node instanceof NullNode) return null;
        if (node instanceof ForNode) return interpretFor((ForNode) node, context);
        if (node instanceof WhileNode) return interpretWhile((WhileNode) node, context);
        if (node instanceof BooleanNode) return ((BooleanNode) node).getValue();
        if (node instanceof ForeachNode) return interpretForeach((ForeachNode) node, context);
        if (node instanceof ReflectedNativeFunctionDeclaration) return interpretReflectedNativeFunctionDeclaration((ReflectedNativeFunctionDeclaration) node, context);
        if (node instanceof NativeClassDeclarationNode) return interpretNativeClassDeclaration((NativeClassDeclarationNode) node, context);
        if (node instanceof LocalVariableReassignmentNode) return VOID_OBJECT; // ignore
        if (node instanceof SelectNode) return interpretSelect((SelectNode) node, context);
        if (node instanceof BreakNode) return BREAK;
        if (node instanceof ContinueNode) return CONTINUE;
        if (node instanceof NativeCastNode) return interpretNativeCast((NativeCastNode) node, context);

        throw new UnsupportedOperationException("Unsupported node: " + (node == null ? "null" : node.getClass().getSimpleName()) + ", val=" + node);
    }

    private Object interpretBlock(final BlockNode block, final Context context) {
        if (block == null) {
            Register.throwException("Null block");
            return null;
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
        final VariableDefinition variableDefinition;
        if (node.getValue() instanceof VoidNode) {
            variableDefinition = new VariableDefinition(node.getName(), VOID_OBJECT, node.isConstant(), node.isStatic());
        } else {
            variableDefinition = new VariableDefinition(node.getName(), interpretNode(node.getValue(), context), node.isConstant(), node.isStatic());
            if (variableDefinition.getValue() instanceof VoidObject) Register.throwException("Creating a void variable: " + node.getName());
        }

        context.defineVariable(node.getName(), variableDefinition);
        return VOID_OBJECT;
    }

    private Object interpretFunctionDeclaration(final FunctionDeclarationNode node, final Context context) {
        context.defineFunction(node.getFunctionName(), new FunctionDefinition(node));
        return VOID_OBJECT;
    }

    private Object interpretClassDeclaration(final ClassDeclarationNode node, final Context context) {

        final String name = node.getName();
        context.setCurrentClassName(name);

        final ClassDefinition inheritedClass;

        final ObjectType type = context.getClass(node.getInheritedClass());

        if (node.getInheritedClass() != null) {
            if (!(type instanceof ClassDefinition)) {
                Register.throwException("Inherited class not found: " + node.getInheritedClass(), node.getLine());
                return null;
            } else {
                inheritedClass = (ClassDefinition) type;
                if (inheritedClass.isConstant()) {
                    Register.throwException("Inheriting a const " + (inheritedClass.isNative() ? "native " : "") + "class: " + node.getInheritedClass(), node.getLine());
                }
            }
        } else {
            inheritedClass = null;
        }

        final ClassDefinition definition = new ClassDefinition(name, node.isConstant(), inheritedClass, node.getConstructor() == null ? null : new FunctionDefinition(node.getConstructor()), node.getBody(), false);
        context.defineClass(name, definition);

        if (!context.getNatives().contains(node.getFileName())) Privileges.checkClassName(name, node.getLine());

        definition.getBody().forEach(statement -> {
            if (statement instanceof VariableDeclarationNode) {
                VariableDeclarationNode declarationNode = (VariableDeclarationNode) statement;
                if (declarationNode.isStatic()) {
                    definition.staticVariables.put(declarationNode.getName(), new VariableDefinition(declarationNode.getName(), declarationNode.getValue() == null ? null : interpretNode(declarationNode.getValue(), context), declarationNode.isConstant(), true));
                }
            } else if (statement instanceof FunctionDeclarationNode) {
                FunctionDeclarationNode declarationNode = (FunctionDeclarationNode) statement;
                final List<String> params = new ArrayList<>();

                declarationNode.getParameterNodes().forEach(param -> params.add(param.getName()));

                if (declarationNode.isStatic()) {
                    definition.staticFunctions.put(declarationNode.getFunctionName(), new FunctionDefinition(declarationNode.getFunctionName(), true, params, declarationNode.getBlock()));
                }
            } else if (statement instanceof ReflectedNativeFunctionDeclaration) {
                final Object o = interpretReflectedNativeFunctionDeclaration((ReflectedNativeFunctionDeclaration) statement, context);
                if (o instanceof FunctionDefinition) {
                    FunctionDefinition def = (FunctionDefinition) o;
                    definition.staticFunctions.put(def.getName(), def);
                }
            }
        });

        context.setCurrentClassName(null);

        return VOID_OBJECT;
    }

    private Object interpretBinaryExpressionNode(final BinaryExpressionNode node, final Context context) {
        final Object leftValue = interpretNode(node.getLeft(), context);
        final Object rightValue = interpretNode(node.getRight(), context);

        final String operator = node.getOperator();

        if (leftValue instanceof Boolean && rightValue instanceof Boolean) {
            return evaluateBooleanOperation((Boolean) leftValue, (Boolean) rightValue, operator, node.getLine());
        } else if (leftValue == null || rightValue == null) {
            if (!operator.equals("==")) {
                except("Only operator available for null is '=='", node.getLine());
                return null;
            }
            return leftValue == rightValue;
        } else if (leftValue instanceof String || rightValue instanceof String) {
            if (operator.equals("+")) {
                return leftValue + rightValue.toString();
            } else if (operator.equals("==")) {
                return leftValue.equals(rightValue);
            } else {
                except("Operator " + operator + " is not supported for string operands.", node.getLine());
            }
        } else if (leftValue instanceof Number && rightValue instanceof Number) {
            Number leftNumber = (Number) leftValue;
            Number rightNumber = (Number) rightValue;

            boolean leftIsDouble = leftNumber instanceof Double;
            boolean rightIsDouble = rightNumber instanceof Double;

            if (leftIsDouble || rightIsDouble) {
                double left = leftNumber.doubleValue();
                double right = rightNumber.doubleValue();

                return evaluateDoubleOperation(left, right, operator, node.getLine());
            } else {
                int left = leftNumber.intValue();
                int right = rightNumber.intValue();

                return evaluateIntegerOperation(left, right, operator, node.getLine());
            }
        }

        except("Invalid operands for binary expression: " + leftValue.getClass().getSimpleName() + " and " + rightValue.getClass().getSimpleName(), node.getLine());
        return null;
    }

    private Object evaluateBooleanOperation(boolean left, boolean right, String operator, final int line) {
        switch (operator) {
            case "&&":
                return left && right;
            case "||":
                return left || right;
            case "==":
                return left == right;
            case "!=":
                return left != right;
            default:
                except("Unsupported operator for boolean operands: " + operator, line);
                return null;
        }
    }

    private Object evaluateDoubleOperation(double left, double right, String operator, final int line) {
        switch (operator) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
                return left * right;
            case "/":
                if (right == 0) {
                    except("Division by zero", line);
                }
                return left / right;
            case "%":
                return left % right;
            case "^":
                return Math.pow(left, right);
            case "<":
                return left < right;
            case ">":
                return left > right;
            case "<=":
                return left <= right;
            case ">=":
                return left >= right;
            case "==":
                return left == right;
            case "!=":
                return left != right;
            default:
                except("Unsupported operator: " + operator, line);
                return null;
        }
    }

    private Object evaluateIntegerOperation(int left, int right, String operator, final int line) {
        switch (operator) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
                return left * right;
            case "/":
                if (right == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return left / right;
            case "%":
                return left % right;
            case "^":
                final double pow = Math.pow(left, right);

                if (pow % 1 == 0) return (int) pow;

                return pow % 1 == 0 ? (int) pow : pow;
            case "<":
                return left < right;
            case ">":
                return left > right;
            case "<=":
                return left <= right;
            case ">=":
                return left >= right;
            case "==":
                return left == right;
            case "!=":
                return left != right;
            default:
                except("Unsupported operator: " + operator, line);
                return null;
        }
    }

    private Object interpretDefaultNativeFunctionCall(final DefaultNativeFunctionCallNode node, final Context context) {
        final List<Object> params = new ArrayList<>();
        for (final ASTNode param : node.getParams()) {
            final Object added = interpretNode(param, context);
            if (added instanceof VoidObject) {
                Register.throwException("Passing void as parameter");
            }
            params.add(added);
        }
        Register.register(new Register.RegisterElement(Register.RegisterElementType.NATIVECALL, node.getName() + getParams(params), node.getLine(), context.getCurrentClassName()));
        return nmh.callDefault(node.getName(), params);
    }

    private Object interpretVariableReference(final VariableReferenceNode node, final Context context) {
        final Object ret = context.getVariable(node.getName());
        if (ret == VOID_OBJECT) {
            except("Referencing a non-created variable: " + node.getName(), node.getLine());
        }
        return ret;
    }

    private Object interpretFunctionCall(final FunctionCallNode node, Context context) {

        final String functionName = node.getName();

        context.setCurrentFunctionName(functionName);

        final ObjectType type = context.getFunction(functionName, node.getParams().size());

        if (type == VOID_OBJECT) {
            except("Calling a function that doesn't exist: " + functionName, node.getLine());
            return null;
        }

        final FunctionDefinition definition = (FunctionDefinition) type;

        final List<Object> params = new ArrayList<>();

        for (final ASTNode param : node.getParams()) {
            final Object returned = interpretNode(param, context);
            if (returned == VOID_OBJECT) {
                except("Passing void as a parameter function: " + param.getClass().getSimpleName() + ", fn: " + functionName, node.getLine());
            }
            params.add(returned);
        }

        if (params.size() > definition.getParams().size()) {
            except("Passing more parameters than needed (" + params.size() + ", " + definition.getParams().size() + ") in fn: " + functionName, node.getLine());
        } else if (params.size() < definition.getParams().size()) {
            except("Passing less parameters than needed (" + params.size() + ", " + definition.getParams().size() + ") in fn: " + functionName, node.getLine());
        }

        final Context functionContext = new Context(context);

        List<String> definitionParams = definition.getParams();
        for (int i = 0; i < definitionParams.size(); i++) {
            final String name = definitionParams.get(i);
            final Object value = params.get(i);
            if (value instanceof VoidObject) {
                Register.throwException("Passing void as parameter");
            }
            functionContext.defineVariable(name, new VariableDefinition(name, value, false, false));
        }

        Register.register(new Register.RegisterElement(Register.RegisterElementType.FUNCALL, node.getName() + getParams(params), node.getLine(), context.getCurrentClassName()));

        final Object result = interpretBlock(definition.getBlock(), functionContext);
        context.setCurrentFunctionName(null);
        return result;
    }

    private Object interpretReturnNode(final ReturnNode node, final Context context) {
        return interpretNode(node.getValue(), context);
    }

    private Object interpretClassInstantiation(final ClassInstantiationNode node, final Context context) {
        final String name = node.getName();

        context.setCurrentClassName(name);

        Register.register(new Register.RegisterElement(Register.RegisterElementType.CLASSINST, name, node.getLine(), context.getCurrentClassName()));

        final Context classContext = new Context(context);

        final List<Object> params = new ArrayList<>();

        for (final ASTNode sub : node.getParams()) {
            final Object added = interpretNode(sub, context);
            if (added instanceof VoidObject) {
                Register.throwException("Passing void as parameter");
            }
            params.add(added);
        }

        final ObjectType raw = context.getClass(name);

        if (raw == VOID_OBJECT) {
            except("Class not found: " + name, node.getLine());
            return null;
        }

        final ClassDefinition definition = (ClassDefinition) raw;
        Object val = interpretBlock(definition.getBody(), classContext);

        if (val != VOID_OBJECT)
            except("Return in class body", node.getLine());

        final ClassDefinition inheritedClass = definition.getInheritedClass();

        if (inheritedClass != null) {
            final FunctionDefinition inheritedConstructor = inheritedClass.getConstructor();
            final ClassObject inheritedObject = interpretConstructor(inheritedConstructor, params, classContext, inheritedClass.getName());

            classContext.mergeContext(inheritedObject.getContext());
        }

        final Object result = interpretConstructor(definition.getConstructor(), params, classContext, name);
        context.setCurrentClassName(null);

        return result;
    }

    private ClassObject interpretConstructor(final FunctionDefinition constructor, final List<Object> params, final Context context, final String cn) {
        if (constructor == null) {
            return new ClassObject(cn, new Context(context));
        }

        if (constructor.getParams().size() != params.size()) {
            Register.throwException("Params length do not match for initialization of class " + cn);
        }

        List<String> constructorParams = constructor.getParams();

        final Context constructorContext = new Context(context);

        for (int i = 0; i < constructorParams.size(); i++) {
            if (params.get(i) instanceof VoidObject) {
                Register.throwException("Passing void as parameter");
            }
            constructorContext.defineVariable(constructorParams.get(i), new VariableDefinition(constructorParams.get(i), params.get(i), false, false));
        }

        final Object result = interpretBlock(constructor.getBlock(), constructorContext);
        if (result != VOID_OBJECT) except("You can't return in a constructor!", constructor.getBlock().getLine());
        return new ClassObject(cn, constructorContext);
    }

    private Object interpretContextReference(final Context context) {
        return context.parentContext();
    }

    private Object interpretVariableReassignment(final VariableReassignmentNode node, final Context context) {
        final Object result = interpretNode(node.getValue(), context);
        if (result instanceof VoidObject) Register.throwException("Reassigning variable with void value: " + node.getName(), node.getLine());
        context.setVariable(node.getName(), result);
        return VOID_OBJECT;
    }

    private Object interpretObjectFunctionCall(final ObjectFunctionCallNode node, final Context context) {
        final Object caller = context.getVariable(node.getCaller());
        if (caller instanceof VoidObject) {
            return handleStaticFunctionCall(node, context);
        } else if (caller instanceof Object[]) {
            Object resultArray = handleArrayFunctionCall(node, context, (Object[]) caller);
            if (resultArray instanceof Object[]) {
                context.setVariable(node.getCaller(), resultArray);
                return VOID_OBJECT;
            }
            return resultArray;
        } else if (caller instanceof ClassObject) {
            return handleInstanceMethodCall(node, context, (ClassObject) caller);
        } else {
            except("You can't call a function out of a " + caller.getClass().getSimpleName(), node.getLine());
            return null;
        }
    }

    private Object handleStaticFunctionCall(final ObjectFunctionCallNode node, final Context context) {
        final ObjectType rawDefinition = context.getClass(node.getCaller());
        if (rawDefinition instanceof VoidObject) {
            except("Accessing a static function of a non-existent class: " + node.getCaller(), node.getLine());
            return null;
        }

        final ClassDefinition classDefinition = (ClassDefinition) rawDefinition;

        context.setCurrentClassName(classDefinition.getName());
        context.setCurrentFunctionName(node.getCalled());

        final FunctionDefinition definition = classDefinition.staticFunctions.get(node.getCalled());

        if (definition == null) {
            except("Accessing a static function that does not exist: " + node.getCaller() + "#" + node.getCalled() + "(...)", node.getLine());
            return null;
        }

        final List<Object> params = getFunctionParameters(node, context, definition.getParams().size());
        if (params == null) return null;

        final Context functionContext = new Context(context);
        defineFunctionParameters(functionContext, definition, params);

        Register.register(new Register.RegisterElement(Register.RegisterElementType.STATICCALL, node.getCalled() + getParams(params), node.getLine(), context.getCurrentClassName()));
        final Object result = interpretBlock(definition.getBlock(), functionContext);
        context.setCurrentClassName(null);
        context.setCurrentFunctionName(null);
        return result;
    }

    private Object handleArrayFunctionCall(final ObjectFunctionCallNode node, final Context context, final Object[] array) {
        final List<Object> params = getFunctionParameters(node, context, -1);
        if (params == null) return null;

        final String fn = node.getCalled();

        Register.register(new Register.RegisterElement(Register.RegisterElementType.ARRAYCALL, fn + getParams(params), node.getLine(), context.getCurrentClassName()));

        switch (fn) {
            case "at":
                if (params.size() == 1 && params.get(0) instanceof Integer) {
                    try {
                        return array[(int) params.get(0)];
                    } catch (final ArrayIndexOutOfBoundsException ex) {
                        except("Array out of bounds: " + params.get(0), node.getLine());
                    }
                }
            case "size":
                if (params.isEmpty()) {
                    return array.length;
                }
            case "set":
                if (params.size() == 2 && params.get(0) instanceof Integer) {
                    array[(int) params.get(0)] = params.get(1);
                    return VOID_OBJECT;
                }
            case "setSize":
                if (params.size() == 1 && params.get(0) instanceof Integer) {
                    int newSize = (int) params.get(0);
                    if (newSize < 0) except("Negative array size: " + newSize, node.getLine());
                    Object[] newArray = new Object[newSize];
                    System.arraycopy(array, 0, newArray, 0, Math.min(array.length, newSize));
                    return newArray;
                }
            default:
                except("Illegal function in array context: " + fn + " with params " + params, node.getLine());
                return null;
        }
    }

    private Object handleInstanceMethodCall(final ObjectFunctionCallNode node, final Context context, final ClassObject classObject) {
        context.setCurrentClassName(classObject.getName());
        context.setCurrentFunctionName(node.getCalled());

        // Attempt to find the function in the current class's context
        ObjectType rawDefinition = classObject.getContext().getFunction(node.getCalled(), node.getParams().size());

        // Check for function in parent classes if it is not found in the current class
        if (rawDefinition == VOID_OBJECT) {
            rawDefinition = findFunctionInInheritedClasses(node, context, classObject);
        }

        if (rawDefinition == VOID_OBJECT) {
            except("Called a non-existent function: " + classObject.getName() + "#" + node.getCalled(), node.getLine());
            return null;
        }

        FunctionDefinition definition = (FunctionDefinition) rawDefinition;
        final Context functionContext = new Context(classObject.getContext());

        final List<Object> params = getFunctionParameters(node, context, definition.getParams().size());
        if (params == null) return null;

        defineFunctionParameters(functionContext, definition, params);

        Register.register(new Register.RegisterElement(Register.RegisterElementType.NATIVECALL, node.getCalled() + getParams(definition.getParams()), node.getLine(), context.getCurrentClassName()));

        final Object result = interpretBlock(definition.getBlock(), functionContext);
        context.setCurrentClassName(null);
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
                Register.throwException("Passing void as a parameter function");
                return null;
            }
            params.add(returned);
        }

        if (expectedSize != -1 && (params.size() > expectedSize || params.size() < expectedSize)) {
            except("Parameter size mismatch. Expected: " + expectedSize + ", Found: " + params.size(), node.getLine());
            return null;
        }
        return params;
    }

    private void defineFunctionParameters(final Context functionContext, final FunctionDefinition definition, final List<Object> params) {
        List<String> definitionParams = definition.getParams();
        for (int i = 0; i < definitionParams.size(); i++) {
            final String name = definitionParams.get(i);
            final Object value = params.get(i);
            functionContext.defineVariable(name, new VariableDefinition(name, value, false, false));
        }
    }

    private Object interpretLocalVariableReferenceNode(final LocalVariableReferenceNode node, final Context context) {
        final Object ret = context.parentContext().getVariable(node.getName());
        if (ret == VOID_OBJECT) {
            except("Referencing a non-created variable: " + node.getName(), node.getLine());
        }
        return ret;
    }

    private Object interpretLocalFunctionCallNode(final LocalFunctionCallNode node, final Context raw) {
        final Context context = raw.parentContext();
        final String functionName = node.getName();

        context.setCurrentFunctionName(functionName);

        final ObjectType type = context.getFunction(functionName, node.getParams().size());

        if (type == VOID_OBJECT) {
            except("Calling a function that doesn't exist: " + functionName, node.getLine());
            return null;
        }

        final FunctionDefinition definition = (FunctionDefinition) type;

        final List<Object> params = new ArrayList<>();

        for (final ASTNode param : node.getParams()) {
            final Object returned = interpretNode(param, context);
            if (returned == VOID_OBJECT) {
                except("Passing void as a parameter function: " + param.getClass().getSimpleName() + ", fn: " + functionName, node.getLine());
            }
            params.add(returned);
        }

        if (params.size() > definition.getParams().size()) {
            except("Passing more parameters than needed (" + params.size() + ", " + definition.getParams().size() + ") in fn: " + functionName, node.getLine());
        } else if (params.size() < definition.getParams().size()) {
            except("Passing less parameters than needed (" + params.size() + ", " + definition.getParams().size() + ") in fn: " + functionName, node.getLine());
        }

        final Context functionContext = new Context(context);

        List<String> definitionParams = definition.getParams();
        for (int i = 0; i < definitionParams.size(); i++) {
            final String name = definitionParams.get(i);
            final Object value = params.get(i);

            functionContext.defineVariable(name, new VariableDefinition(name, value, false, false));
        }

        Register.register(new Register.RegisterElement(Register.RegisterElementType.LOCALCALL, node.getName() + getParams(params), node.getLine(), context.getCurrentClassName()));


        final Object result = interpretBlock(definition.getBlock(), functionContext);
        context.setCurrentFunctionName(null);
        return result;
    }

    private Object interpretObjectVariableReference(final ObjectVariableReferenceNode node, final Context context) {
        final String callerObjectName = node.getCaller();
        final String calledObjectName = node.getCalled();
        final Object callerObjectRaw = context.getVariable(callerObjectName);

        if (callerObjectRaw instanceof VoidObject) { // static init
            final ObjectType rawDefinition = context.getClass(callerObjectName);
            if (rawDefinition instanceof VoidObject) {
                except("Accessing a static variable of a non-existent class: " + callerObjectName, node.getLine());
                return null;
            }
            final ClassDefinition definition = (ClassDefinition) rawDefinition;

            final VariableDefinition variable = definition.staticVariables.get(calledObjectName);

            if (variable == null) {
                except("Accessing a static variable that does not exist: " + callerObjectName + "#" + calledObjectName, node.getLine());
                return null;
            }

            return variable.getValue();
        }

        if (!(callerObjectRaw instanceof ClassObject)) {
            except("Getting variable of " + callerObjectRaw.getClass().getSimpleName() + ", expected Class Object", node.getLine());
            return null;
        }
        final ClassObject callerObject = (ClassObject) callerObjectRaw;
        return callerObject.getContext().getVariable(calledObjectName);
    }

    private Object interpretObjectVariableReassignment(final ObjectVariableReassignmentNode node, final Context context) {
        final Object callerObjectRaw = context.getVariable(node.getCaller());

        if (!(callerObjectRaw instanceof ClassObject)) {
            except("Getting variable of " + callerObjectRaw.getClass().getSimpleName() + ", expected Class Object", node.getLine());
            return null;
        }

        final Object value = interpretNode(node.getValue(), context);

        if (value == VOID_OBJECT) except("Cannot assign void to a variable.", node.getLine());

        ((ClassObject) callerObjectRaw).getContext().setVariable(node.getCalled(), value);
        return VOID_OBJECT;
    }


    private Object interpretInclude(final IncludeNode node, final Context context) {
        if (node.isNative()) context.getNatives().add(node.getName());

        for (final ASTNode astnode : node.getIncluded()) {
            final Object result = interpretNode(astnode, context);
            preInterpret(astnode, node.getIncluded(), context);
            if (result instanceof ReturnValue) {
                return ((ReturnValue) result).getValue();
            }
        }
        return VOID_OBJECT;
    }

    private Object interpretPackagedNativeFunctionCall(final PackagedNativeFunctionCallNode node, final Context context) {
        final List<Object> params = new ArrayList<>();
        for (final ASTNode param : node.getParams()) params.add(interpretNode(param, context));

        Register.register(new Register.RegisterElement(Register.RegisterElementType.NATIVECALL,  node.getName() + getParams(params), node.getLine(), context.getCurrentClassName()));


        final ObjectType rawCurrent =  context.getClass(context.getCurrentClassName());

        if (!(rawCurrent instanceof ClassDefinition)) {
            return new ReturnValue(nmh.callPackaged(node.getPackage(), node.getName(), context.getCurrentClassName(), params));
        }

        final ClassDefinition current = (ClassDefinition) rawCurrent;

        if (current.isNative()) {
            return new ReturnValue(nmh.callClassNative(current.getName(), node.getName(), params, context));
        }
        else return new ReturnValue(nmh.callPackaged(node.getPackage(), node.getName(), context.getCurrentClassName(), params));
    }

    private Object interpretArray(final ArrayNode node, final Context context) {
        final Object[] objects = new Object[node.getNodes().size()];

        List<ASTNode> nodes = node.getNodes();
        for (int i = 0, nodesSize = nodes.size(); i < nodesSize; i++) {
            final ASTNode astNode = nodes.get(i);
            final Object result = interpretNode(astNode, context);

            if (result == VOID_OBJECT) except("Adding void as a object in an array.", astNode.getLine());

            objects[i] = result;
        }

        return objects;
    }

    private void except(final String message, final int line) {
        Register.throwException(message, line);
    }

    private String getParams(final List<?> objects) {
        final StringBuilder s = new StringBuilder("(");
        for (int i = 0, size = objects.size(); i < size; i++) {
            final Object o = objects.get(i);
            s.append(o == null ? null : o.getClass().getSimpleName().toLowerCase());
            if (i < size - 1) s.append(", ");
        }
        return s + ")";
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
        if (rawConditionValue instanceof Integer || rawConditionValue instanceof Byte) {
            final int num = (int) rawConditionValue;
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

    private Object interpretFor(final ForNode node, final Context context) {

        final Context forContext = new Context(context);

        if (node.getDeclaration() != null && interpretNode(node.getDeclaration(), forContext) != VOID_OBJECT)
            except("for declaration must be void return", node.getLine());

        final ASTNode condition = node.getCondition();

        while (node.getDeclaration() == null || checkCondition(condition, forContext)) {
            final Object val = interpretBlock(node.getBlock(), forContext);
            if (val == CONTINUE) {
                continue;
            } else if (val == BREAK) {
                break;
            }
            if (val != VOID_OBJECT) {
                return new ReturnValue(val);
            }
            if (node.getIncrementation() != null && interpretNode(node.getIncrementation(), forContext) != VOID_OBJECT)
                except("for incrementation must be void return", node.getLine());
        }

        return VOID_OBJECT;
    }

    private Object interpretWhile(final WhileNode node, final Context context) {
        final Context whileContext = new Context(context);

        final ASTNode condition = node.getCondition();


        while (checkCondition(condition, whileContext)) {
            final Object val = interpretBlock(node.getBlock(), whileContext);
            if (val == CONTINUE) {
                continue;
            } else if (val == BREAK) {
                break;
            }
            if (val != VOID_OBJECT) {
                return new ReturnValue(val);
            }
        }

        return VOID_OBJECT;
    }

    private Object interpretForeach(final ForeachNode node, final Context context) {

        final Context forEachContext = new Context(context);

        final Object list = interpretNode(node.getList(), context);

        if (list == VOID_OBJECT) {
            except("Void type not allowed in foreach", node.getLine());
            return null;
        }
        if (list == null) {
            except("Null list in foreach", node.getLine());
            return null;
        }
        if (!(list instanceof Object[])) {
            except("Expected list or array in foreach, but got " + list.getClass().getSimpleName(), node.getLine());
            return null;
        }

        Object[] arr = (Object[]) list;

        forEachContext.defineVariable(node.getVariable(), new VariableDefinition(node.getVariable(), null, false, false));

        for (final Object o : arr) {
            forEachContext.setVariable(node.getVariable(), o);
            final Object val = interpretBlock(node.getBlock(), forEachContext);
            if (val == CONTINUE) {
                continue;
            } else if (val == BREAK) {
                break;
            }
            if (val != VOID_OBJECT) {
                return new ReturnValue(val);
            }
        }

        return VOID_OBJECT;
    }

    private Object interpretReflectedNativeFunctionDeclaration(final ReflectedNativeFunctionDeclaration node, final Context context) {

        final BlockNode block = new BlockNode();

        final List<ASTNode> nodes = new ArrayList<>();

        for (final ParameterNode parameterNode : node.getParams())
            nodes.add(new VariableReferenceNode(parameterNode.getName()));


        final ASTNode astnode = new PackagedNativeFunctionCallNode(
                node.getName(),
                node.getFileName().substring(0, node.getFileName().length() - 3),
                nodes
        );

        block.addChild(astnode);

        final List<String> params = new ArrayList<>();

        for (final ParameterNode param : node.getParams()) params.add(param.getName());

        final FunctionDefinition function = new FunctionDefinition(node.getName(), node.isStatic(), params, block);

        if (node.isStatic()) {
            final ObjectType obj = context.getClass(node.getFileName());
            if (obj != VOID_OBJECT) ((ClassDefinition) obj).staticFunctions.put(node.getName(), function);
            else return function;
        } else {
            context.defineFunction(node.getName(), function);
        }

        return VOID_OBJECT;
    }

    private Object interpretNativeClassDeclaration(final NativeClassDeclarationNode node, final Context context) {
        final String name = node.getName();
        context.setCurrentClassName(name);

        final ClassDefinition inheritedClass = (ClassDefinition) context.getClass(node.getInheritedClass()); // no need to check, native class do not have errors (we hope)

        final ClassDefinition definition = new ClassDefinition(name, node.isConstant(), inheritedClass, node.getConstructor() == null ? null : new FunctionDefinition(node.getConstructor()), node.getBody(), true);
        context.defineClass(name, definition);

        if (!context.getNatives().contains(node.getFileName())) Privileges.checkClassName(name, node.getLine());

        definition.getBody().forEach(statement -> {
            if (statement instanceof VariableDeclarationNode) {
                VariableDeclarationNode declarationNode = (VariableDeclarationNode) statement;
                if (declarationNode.isStatic()) {
                    definition.staticVariables.put(declarationNode.getName(), new VariableDefinition(declarationNode.getName(), declarationNode.getValue() == null ? null : interpretNode(declarationNode.getValue(), context), declarationNode.isConstant(), true));
                }
            } else if (statement instanceof FunctionDeclarationNode) {
                FunctionDeclarationNode declarationNode = (FunctionDeclarationNode) statement;
                final List<String> params = new ArrayList<>();

                declarationNode.getParameterNodes().forEach(param -> params.add(param.getName()));

                if (declarationNode.isStatic()) {
                    definition.staticFunctions.put(declarationNode.getFunctionName(), new FunctionDefinition(declarationNode.getFunctionName(), true, params, declarationNode.getBlock()));
                }
            } else if (statement instanceof ReflectedNativeFunctionDeclaration) {
                final Object o = interpretReflectedNativeFunctionDeclaration((ReflectedNativeFunctionDeclaration) statement, context);
                if (o instanceof FunctionDefinition) {
                    FunctionDefinition def = (FunctionDefinition) o;
                    definition.staticFunctions.put(def.getName(), def);
                }
            }
        });

        context.setCurrentClassName(null);
        return VOID_OBJECT;
    }

    private Object interpretSelect(final SelectNode node, final Context context) {

        final Object value = interpretNode(node.getCondition(), context);
        if (value == VOID_OBJECT) except("Void condition in switch expression", node.getLine());

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

        return interpretBlock(node.getDefaultBlock(), context);
    }

    private Number interpretNativeCast(final NativeCastNode node, final Context context) {
        final Object expression = interpretNode(node.getCasted(), context);

        if (expression == null) {
            return node.getType() == NativeCastNode.CastType.FLOAT ? 0d : 0;
        }

        switch (node.getType()) {
            case FLOAT:
                return castToFloat(expression, node);
            case INT:
                return castToInt(expression, node);
            default:
                Register.throwException("Unknown cast: " + node.getType().name().toLowerCase(), node.getLine());
                return null;
        }
    }

    private Double castToFloat(Object expression, NativeCastNode node) {
        if (expression instanceof String) {
            return parseDoubleOrThrow((String) expression, node);
        }
        if (expression instanceof Integer) {
            return ((Integer) expression).doubleValue();
        }
        if (expression instanceof Double) {
            return (Double) expression;
        }

        Register.throwException("Could not cast to float", node.getLine());
        return null;
    }

    private Integer castToInt(final Object expression, final NativeCastNode node) {
        if (expression instanceof String) {
            return parseIntegerOrThrow((String) expression, node);
        }
        if (expression instanceof Double) {
            return ((Double) expression).intValue();
        }
        if (expression instanceof Integer) {
            return (Integer) expression;
        }

        Register.throwException("Could not cast to int", node.getLine());
        return null;
    }

    private Double parseDoubleOrThrow(final String expression,final  NativeCastNode node) {
        try {
            return Double.parseDouble(expression);
        } catch (NumberFormatException e) {
            Register.throwException("Could not cast to float", node.getLine());
            return null;
        }
    }

    private Integer parseIntegerOrThrow(final String expression, final NativeCastNode node) {
        try {
            return Integer.parseInt(expression);
        } catch (NumberFormatException e) {
            if (expression.contains(".")) {
                try {
                    return Integer.parseInt(expression.split("\\.")[0]);
                } catch (NumberFormatException ignored) {
                }
            }
            Register.throwException("Could not cast to int", node.getLine());
            return null;
        }
    }

}
