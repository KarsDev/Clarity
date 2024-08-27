package me.kuwg.clarity.interpreter;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.block.ReturnNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassInstantiationNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.call.*;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.include.IncludeNode;
import me.kuwg.clarity.ast.nodes.literal.*;
import me.kuwg.clarity.ast.nodes.reference.ContextReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.assign.ObjectVariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.get.LocalVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.ObjectVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.VariableReferenceNode;
import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.interpreter.definition.ClassDefinition;
import me.kuwg.clarity.interpreter.definition.FunctionDefinition;
import me.kuwg.clarity.interpreter.definition.VariableDefinition;
import me.kuwg.clarity.interpreter.exceptions.MultipleMainMethodsException;
import me.kuwg.clarity.nmh.NativeMethodHandler;
import me.kuwg.clarity.interpreter.types.ClassObject;
import me.kuwg.clarity.interpreter.types.ObjectType;
import me.kuwg.clarity.interpreter.definition.ReturnValue;
import me.kuwg.clarity.interpreter.types.VoidObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static me.kuwg.clarity.interpreter.types.VoidObject.VOID;

// all for building part
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
            } else if (node instanceof FunctionDeclarationNode) {
                interpretFunctionDeclaration((FunctionDeclarationNode) node, general);
                ast.getRoot().getChildrens().remove(node);
            } else if (node instanceof ClassDeclarationNode) {
                interpretClassDeclaration((ClassDeclarationNode) node, general);
                ast.getRoot().getChildrens().remove(node);
            } else if (node instanceof IncludeNode) {
                interpretInclude((IncludeNode) node, general);
                ast.getRoot().getChildrens().remove(node);
            }

        }

        if (main != null) {
            final Object result = interpretNode(main.getBlock(), general);

            if (result == VOID) {
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
        if (node instanceof VoidNode) return VOID;
        if (node instanceof IncludeNode) return interpretInclude((IncludeNode) node, context);
        if (node instanceof PackagedNativeFunctionCallNode) return interpretPackagedNativeFunctionCall((PackagedNativeFunctionCallNode) node, context);
        if (node instanceof ArrayNode) return interpretArrayNode((ArrayNode) node, context);


        throw new UnsupportedOperationException("Unsupported node: " + (node == null ? "null" : node.getClass().getSimpleName()) + ", val=" + node);
    }

    private Object interpretBlock(final BlockNode block, final Context context) {
        for (final ASTNode node : block) {
            Object result = interpretNode(node, context);
            if (result instanceof ReturnValue) {
                return ((ReturnValue) result).getValue();
            }
        }
        return VOID;
    }

    private Object interpretVariableDeclaration(final VariableDeclarationNode node, final Context context) {
        VariableDefinition variableDefinition = new VariableDefinition(node.getName(), node.getValue() == null ? null : interpretNode(node.getValue(), context), node.isConstant(), node.isStatic());

        context.defineVariable(node.getName(), variableDefinition);
        return VOID;
    }

    private Object interpretFunctionDeclaration(final FunctionDeclarationNode node, final Context context) {
        context.defineFunction(node.getFunctionName(), new FunctionDefinition(node));
        return VOID;
    }

    private Object interpretClassDeclaration(final ClassDeclarationNode node, final Context context) {
        final ClassDefinition definition = new ClassDefinition(node.getName(), node.getConstructor() == null ? null : new FunctionDefinition(node.getConstructor()), node.getBody());
        context.defineClass(node.getName(), definition);

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
            }
        });

        return VOID;
    }


    private Object interpretBinaryExpressionNode(final BinaryExpressionNode node, final Context context) {
        Object leftValue = interpretNode(node.getLeft(), context);
        Object rightValue = interpretNode(node.getRight(), context);

        String operator = node.getOperator();

        if (leftValue == null) leftValue = "null";
        if (rightValue == null) rightValue = "null";

        if (leftValue instanceof String || rightValue instanceof String) {
            if (operator.equals("+")) {
                return leftValue + rightValue.toString();
            } else {
                throw new IllegalArgumentException("Operator " + operator + " is not supported for string operands.");
            }
        }

        if (leftValue instanceof Number && rightValue instanceof Number) {
            Number leftNumber = (Number) leftValue;
            Number rightNumber = (Number) rightValue;

            boolean leftIsDouble = leftNumber instanceof Double;
            boolean rightIsDouble = rightNumber instanceof Double;

            if (leftIsDouble || rightIsDouble) {
                double left = leftNumber.doubleValue();
                double right = rightNumber.doubleValue();

                return evaluateDoubleOperation(left, right, operator);
            } else {
                int left = leftNumber.intValue();
                int right = rightNumber.intValue();

                return evaluateIntegerOperation(left, right, operator);
            }
        } else {
            throw new IllegalArgumentException("Invalid operands for binary expression: " + leftValue.getClass().getSimpleName() + " and " + rightValue.getClass().getSimpleName());
        }
    }

    private Object evaluateDoubleOperation(double left, double right, String operator) {
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
                throw new UnsupportedOperationException("Unsupported operator: " + operator);
        }
    }

    private Object evaluateIntegerOperation(int left, int right, String operator) {
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
                throw new UnsupportedOperationException("Unsupported operator: " + operator);
        }
    }

    private Object interpretDefaultNativeFunctionCall(final DefaultNativeFunctionCallNode node, final Context context) {
        final List<Object> params = node.getParams().stream().map(param -> interpretNode(param, context)).collect(Collectors.toList());
        return nmh.callDefault(node.getName(), params);
    }

    private Object interpretVariableReference(final VariableReferenceNode node, final Context context) {
        final Object ret = context.getVariable(node.getName());
        if (ret == VOID) {
            throw new IllegalStateException("Referencing a non-created variable: " + node.getName());
        }
        return ret;
    }

    private Object interpretFunctionCall(final FunctionCallNode node, Context context) {

        final String functionName = node.getFunctionName();
        final ObjectType type = context.getFunction(functionName);

        if (type == VOID) {
            throw new IllegalArgumentException("Calling a function that doesn't exist: " + functionName);
        }

        final FunctionDefinition definition = (FunctionDefinition) type;

        final List<Object> params = new ArrayList<>();

        for (final ASTNode param : node.getParams()) {
            final Object returned = interpretNode(param, context);
            if (returned == VOID) {
                throw new IllegalArgumentException("Passing void as a parameter function: " + param.getClass().getSimpleName() + ", fn: " + functionName);
            }
            params.add(returned);
        }

        if (params.size() > definition.getParams().size()) {
            throw new IllegalStateException("Passing more parameters than needed (" + params.size() + ", " + definition.getParams().size() + ") in fn: " + functionName);
        } else if (params.size() < definition.getParams().size()) {
            throw new IllegalStateException("Passing less parameters than needed (" + params.size() + ", " + definition.getParams().size() + ") in fn: " + functionName);
        }

        final Context functionContext = new Context(context);

        List<String> definitionParams = definition.getParams();
        for (int i = 0; i < definitionParams.size(); i++) {
            final String name = definitionParams.get(i);
            final Object value = params.get(i);
            functionContext.defineVariable(name, new VariableDefinition(name, value, false, false));
        }
        return interpretBlock(definition.getBlock(), functionContext);
    }

    private Object interpretReturnNode(final ReturnNode node, final Context context) {
        return new ReturnValue(interpretNode(node.getValue(), context));
    }

    private Object interpretClassInstantiation(final ClassInstantiationNode node, final Context context) {
        final String name = node.getName();

        final Context classContext = new Context(context);

        final List<Object> params = new ArrayList<>();

        for (final ASTNode sub : node.getParams()) {
            params.add(interpretNode(sub, classContext));
        }

        final ObjectType raw = context.getClass(name);

        if (raw == VOID) {
            throw new UnsupportedOperationException("Class not found: " + name);
        }

        final ClassDefinition definition = (ClassDefinition) raw;
        interpretBlock(definition.getBody(), classContext);
        return interpretConstructor(definition.getConstructor(), params, classContext, name);
    }

    private ClassObject interpretConstructor(final FunctionDefinition constructor, final List<Object> params, final Context context, final String cn) {
        if (constructor == null) {
            return new ClassObject(cn, new Context(context));
        }

        if (constructor.getParams().size() != params.size()) {
            System.out.println("Params length do not match for initialization of class " + cn);
        }

        List<String> constructorParams = constructor.getParams();

        final Context constructorContext = new Context(context);

        for (int i = 0; i < constructorParams.size(); i++) {
            constructorContext.defineVariable(constructorParams.get(i), new VariableDefinition(constructorParams.get(i), params.get(i), false, false));
        }

        final Object result = interpretBlock(constructor.getBlock(), constructorContext);
        if (result != VOID) throw new IllegalStateException("You can't return in a constructor!");
        return new ClassObject(cn, constructorContext);
    }

    private Object interpretContextReference(final Context context) {
        return context.parentContext();
    }

    private Object interpretVariableReassignment(final VariableReassignmentNode node, final Context context) {
        context.setVariable(node.getName(), interpretNode(node.getValue(), context));
        return VOID;
    }

    private Object interpretObjectFunctionCall(final ObjectFunctionCallNode node, final Context context) {
        final Object caller = context.getVariable(node.getCaller());

        if (caller instanceof VoidObject) {
            final ObjectType rawDefinition = context.getClass(node.getCaller());
            if (rawDefinition instanceof VoidObject) {
                throw new IllegalStateException("Accessing a static function of a non-existent class: " + node.getCaller());
            }
            final ClassDefinition classDefinition = (ClassDefinition) rawDefinition;

            final FunctionDefinition definition = classDefinition.staticFunctions.get(node.getCalled());

            if (definition == null) {
                throw new IllegalStateException("Accessing a static function that does not exist: " + node.getCaller() + "#" + node.getCalled() + "(...)");
            }


            final List<Object> params = new ArrayList<>();

            for (final ASTNode param : node.getParams()) {
                final Object returned = interpretNode(param, context);
                if (returned == VOID) {
                    throw new IllegalArgumentException("Passing void as a parameter function: " + param.getClass().getSimpleName() + ", fn: " + definition.getName());
                }
                params.add(returned);
            }

            if (params.size() > definition.getParams().size()) {
                throw new IllegalStateException("Passing more parameters than needed (" + params.size() + ", " + definition.getParams().size() + ") in fn: " + definition.getName());
            } else if (params.size() < definition.getParams().size()) {
                throw new IllegalStateException("Passing less parameters than needed (" + params.size() + ", " + definition.getParams().size() + ") in fn: " + definition.getName());
            }

            final Context functionContext = new Context(context);

            List<String> definitionParams = definition.getParams();
            for (int i = 0; i < definitionParams.size(); i++) {
                final String name = definitionParams.get(i);
                final Object value = params.get(i);

                functionContext.defineVariable(name, new VariableDefinition(name, value, false, false));
            }

            return interpretBlock(definition.getBlock(), functionContext);
        }

        if (caller instanceof Object[]) {
            final Object[] array = (Object[]) caller;

            final List<Object> params = new ArrayList<>();

            for (final ASTNode param : node.getParams()) {
                final Object returned = interpretNode(param, context);
                if (returned == VOID) {
                    throw new IllegalArgumentException("Passing void as a parameter in a array function");
                }
                params.add(returned);
            }

            final String fn = node.getCalled();

            switch (fn) {
                case "at":
                    if (params.size() == 1 && params.get(0) instanceof Integer) {
                        return array[(int) params.get(0)];
                    }
                    break;

                case "size":
                    if (params.isEmpty()) {
                        return array.length;
                    }
                    break;

                default:
                    throw new IllegalStateException("Illegal function in array context: " + fn + " with params " + params);
            }
        }

        if (!(caller instanceof ClassObject))
            throw new UnsupportedOperationException("You can't call a function out of a " + caller.getClass().getSimpleName());

        ClassObject classObject = (ClassObject) caller;

        final ObjectType rawDefinition = classObject.getContext().getFunction(node.getCalled());

        if (rawDefinition == VOID)
            throw new IllegalStateException("Called a non existent function: " + classObject.getName() + "#" + node.getCalled());

        FunctionDefinition definition = (FunctionDefinition) rawDefinition;

        final Context functionContext = new Context(classObject.getContext());
        if (node.getParams().size() != definition.getParams().size())
            throw new IllegalStateException("Called a function but params size is different: " + classObject.getName() + "#" + node.getCalled());

        for (int i = 0; i < definition.getParams().size(); i++) {
            functionContext.defineVariable(definition.getParams().get(i), new VariableDefinition(definition.getParams().get(i), interpretNode(node.getParams().get(i), context), false, false));
        }
        return interpretBlock(definition.getBlock(), functionContext);
    }

    private Object interpretLocalVariableReferenceNode(final LocalVariableReferenceNode node, final Context context) {
        final Object ret = context.parentContext().getVariable(node.getName());
        if (ret == VOID) {
            throw new IllegalStateException("Referencing a non-created variable: " + node.getName());
        }
        return ret;
    }

    private Object interpretLocalFunctionCallNode(final LocalFunctionCallNode node, final Context raw) {
        final Context context = raw.parentContext();
        final String functionName = node.getFunctionName();
        final ObjectType type = context.getFunction(functionName);

        if (type == VOID) {
            throw new IllegalArgumentException("Calling a function that doesn't exist: " + functionName);
        }

        final FunctionDefinition definition = (FunctionDefinition) type;

        final List<Object> params = new ArrayList<>();

        for (final ASTNode param : node.getParams()) {
            final Object returned = interpretNode(param, context);
            if (returned == VOID) {
                throw new IllegalArgumentException("Passing void as a parameter function: " + param.getClass().getSimpleName() + ", fn: " + functionName);
            }
            params.add(returned);
        }

        if (params.size() > definition.getParams().size()) {
            throw new IllegalStateException("Passing more parameters than needed (" + params.size() + ", " + definition.getParams().size() + ") in fn: " + functionName);
        } else if (params.size() < definition.getParams().size()) {
            throw new IllegalStateException("Passing less parameters than needed (" + params.size() + ", " + definition.getParams().size() + ") in fn: " + functionName);
        }

        final Context functionContext = new Context(context);

        List<String> definitionParams = definition.getParams();
        for (int i = 0; i < definitionParams.size(); i++) {
            final String name = definitionParams.get(i);
            final Object value = params.get(i);

            functionContext.defineVariable(name, new VariableDefinition(name, value, false, false));
        }

        return interpretBlock(definition.getBlock(), functionContext);
    }

    private Object interpretObjectVariableReference(final ObjectVariableReferenceNode node, final Context context) {
        final String callerObjectName = node.getCaller();
        final String calledObjectName = node.getCalled();
        final Object callerObjectRaw = context.getVariable(callerObjectName);

        if (callerObjectRaw instanceof VoidObject) { // static init
            final ObjectType rawDefinition = context.getClass(callerObjectName);
            if (rawDefinition instanceof VoidObject) {
                throw new IllegalStateException("Accessing a static variable of a non-existent class: " + callerObjectName);
            }
            final ClassDefinition definition = (ClassDefinition) rawDefinition;

            final VariableDefinition variable = definition.staticVariables.get(calledObjectName);

            if (variable == null) {
                throw new IllegalStateException("Accessing a static variable that does not exist: " + callerObjectName + "#" + calledObjectName);
            }

            return variable.getValue();
        }

        if (!(callerObjectRaw instanceof ClassObject))
            throw new IllegalStateException("Getting variable of " + callerObjectRaw.getClass().getSimpleName() + ", expected Class Object");
        final ClassObject callerObject = (ClassObject) callerObjectRaw;
        return callerObject.getContext().getVariable(calledObjectName);
    }

    private Object interpretObjectVariableReassignment(final ObjectVariableReassignmentNode node, final Context context) {
        final String callerObjectName = node.getCaller();
        final Object callerObjectRaw = context.getVariable(callerObjectName);
        if (!(callerObjectRaw instanceof ClassObject))
            throw new IllegalStateException("Getting variable of " + callerObjectRaw.getClass().getSimpleName() + ", expected Class Object");
        final ClassObject callerObject = (ClassObject) callerObjectRaw;
        callerObject.getContext().setVariable(node.getCalled(), interpretNode(node.getValue(), context));
        return VOID;
    }

    private Object interpretInclude(final IncludeNode node, final Context context) {
        return interpretNode(node.getIncluded(), context);
    }

    private Object interpretPackagedNativeFunctionCall(final PackagedNativeFunctionCallNode node, final Context context) {
        final List<Object> params = node.getParams().stream().map(param -> interpretNode(param, context)).collect(Collectors.toList());
        return nmh.callPackaged(node.getPackage(), node.getName(), params);
    }

    private Object interpretArrayNode(final ArrayNode node, final Context context) {
        final Object[] objects = new Object[node.getNodes().size()];

        List<ASTNode> nodes = node.getNodes();
        for (int i = 0, nodesSize = nodes.size(); i < nodesSize; i++) {
            final ASTNode astNode = nodes.get(i);
            final Object result = interpretNode(astNode, context);
            if (result == VOID) {
                throw new IllegalStateException("Adding void as a object in an array.");
            }

            objects[i] = result;
        }

        return objects;
    }
}
