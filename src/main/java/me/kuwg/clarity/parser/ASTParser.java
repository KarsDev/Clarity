package me.kuwg.clarity.parser;

import me.kuwg.clarity.Clarity;
import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.*;
import me.kuwg.clarity.ast.nodes.clazz.ClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassInstantiationNode;
import me.kuwg.clarity.ast.nodes.clazz.NativeClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.annotation.AnnotationDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.annotation.AnnotationUseNode;
import me.kuwg.clarity.ast.nodes.clazz.cast.CastType;
import me.kuwg.clarity.ast.nodes.clazz.cast.NativeCastNode;
import me.kuwg.clarity.ast.nodes.clazz.envm.EnumDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.ReflectedNativeFunctionDeclaration;
import me.kuwg.clarity.ast.nodes.member.MemberFunctionCallNode;
import me.kuwg.clarity.ast.nodes.statements.*;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.call.*;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.ast.nodes.include.IncludeNode;
import me.kuwg.clarity.ast.nodes.literal.*;
import me.kuwg.clarity.ast.nodes.variable.assign.LocalVariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.get.LocalVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.ObjectVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.VariableReferenceNode;
import me.kuwg.clarity.compiler.ASTLoader;
import me.kuwg.clarity.register.Register;
import me.kuwg.clarity.token.Token;
import me.kuwg.clarity.token.TokenType;
import me.kuwg.clarity.token.Tokenizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static me.kuwg.clarity.token.TokenType.*;

public final class ASTParser {

    private final String ORIGINAL;
    private final String fileName;
    private final List<Token> tokens;
    private int currentTokenIndex = 0;

    public ASTParser(final String original, final String fileName, final List<Token> tokens) {
        ORIGINAL = original;
        this.fileName = fileName;
        this.tokens = tokens;
    }

    public AST parse() {
        final BlockNode node = new BlockNode();

        while (matchAndConsume(KEYWORD, "include")) {
            final ASTNode include = parseInclude();
            if (include != null) node.addChild(include);
        }

        while (currentTokenIndex < tokens.size()) {
            final ASTNode result = parseExpression();
            if (result != null) node.addChild(result);
        }
        return new AST(node);
    }

    private BlockNode parseBlock() {
        final BlockNode node = new BlockNode();
        final int line = current().getLine();
        if (!matchAndConsume(DIVIDER, "{")) {
            final ASTNode result = parseExpression();
            if (result != null) node.addChild(result);
            else throw new IllegalStateException("Block has no expression at line " + node.getLine());
            return node.setLine(result.getLine());
        }

        while (!matchAndConsume(DIVIDER, "}")) {
            final ASTNode result = parseExpression();
            if (result != null) node.addChild(result);
        }

        return node.setLine(line);
    }

    private ASTNode parseKeyword() {
        final Token current = current();
        final Keyword keyword = Keyword.keyword(current);

        switch (keyword) {
            case VAR:
            case CONST:
            case STATIC:
                return parseVariableDeclaration();
            case FN:
            case CONSTRUCTOR:
                return parseFunctionDeclaration();
            case NATIVE:
                return parseNativeDeclaration();
            case RETURN:
                return parseReturnDeclaration();
            case CLASS:
                return parseClassDeclaration();
            case LOCAL:
                return parseLocalDeclaration();
            case NEW:
                return parseNewDeclaration();
            case VOID:
                return parseVoidDeclaration();
            case IF:
                return parseIfDeclaration();
            case NULL:
                return parseNullDeclaration();
            case FOR:
                return parseForDeclaration();
            case WHILE:
                return parseWhileDeclaration();
            case SELECT:
                return parseSelectDeclaration();
            case BREAK:
                return parseBreakDeclaration();
            case CONTINUE:
                return parseContinueDeclaration();
            case FLOAT:
                return parseFloatDeclaration();
            case INT:
                return parseIntDeclaration();
            case ASSERT:
                return parseAssertDeclaration();
            case IS:
                return parseIsDeclaration();
            case STR:
                return parseStrDeclaration();
            case ARR:
                return parseArrDeclaration();
            case ENUM:
                return parseEnumDeclaration();
            case BOOL:
                return parseBoolDeclaration();
            default:
                Register.throwException("Unsupported keyword: " + keyword + ", at line " + current.getLine());
                return null;
        }
    }

    private ASTNode parseVariableDeclaration() {

        boolean isConst = false;
        boolean isStatic = false;
        boolean isLocal = false;

        while (true) {
            if (matchAndConsume(KEYWORD, "const") && !isConst) {
                isConst = true;
            } else if (matchAndConsume(KEYWORD, "static") && !isStatic) {
                isStatic = true;
            } else if (matchAndConsume(KEYWORD, "local") && !isLocal) {
                isLocal = true;
            } else {
                break;
            }
        }

        if (current().is(KEYWORD, "class")) {
            if (isStatic) throw new RuntimeException("Static classes are not allowed, at line " + current().getLine());
            if (isLocal) throw new RuntimeException("Local classes are not allowed, at line " + current().getLine());
            if (isConst) undo();
            return parseClassDeclaration();
        }

        if (lookahead().is(KEYWORD, "enum")) {
            if (isStatic) throw new RuntimeException("Static enums are not allowed, at line " + current().getLine());
            if (isLocal) throw new RuntimeException("Local enums are not allowed, at line " + current().getLine());
            if (isConst) undo();
            return parseEnumDeclaration();
        }

        if (match(KEYWORD, "static") && lookahead().is(KEYWORD, "native")) {
            if (isStatic) undo();
            if (isLocal) undo();
            if (isConst) undo();
            return parseFunctionDeclaration();
        }

        if (current().is(KEYWORD, "native")) {
            if (isStatic) undo();
            if (isLocal) undo();
            if (isConst) undo();
            return parseNativeDeclaration();
        }

        final int line = current().getLine();

        final String typeDefault = consume().getValue();

        if (typeDefault.equals("fn")) {
            undo(); // undo typeDefault

            if (isStatic) undo();
            if (isLocal) undo();
            if (isConst) undo();
            return parseFunctionDeclaration().setLine(line);
        }

        final String name = consume(VARIABLE).getValue();

        if (typeDefault.equals("void")) { // can't create void variables
            Register.throwException("Void variables are not supported: " + name, line);
        }

        final ASTNode value = matchAndConsume(OPERATOR, "=") ? parseExpression() : new VoidNode().setLine(line);

        return new VariableDeclarationNode(name, typeDefault.equals("var") ? null : typeDefault, value, isConst, isStatic, isLocal).setLine(lookahead(-1).getLine());
    }

    private ASTNode parseFunctionDeclaration() {
        boolean isConst = false;
        boolean isStatic = false;
        boolean isLocal = false;

        while (true) {
            if (matchAndConsume(KEYWORD, "const") && !isConst) {
                isConst = true;
            } else if (matchAndConsume(KEYWORD, "static") && !isStatic) {
                isStatic = true;
            } else if (matchAndConsume(KEYWORD, "local") && !isLocal) {
                isLocal = true;
            } else {
                break;
            }
        }

        if (matchAndConsume(KEYWORD, "native")) {
            consume(KEYWORD, "fn");

            final String name = consume(VARIABLE).getValue();
            final List<ParameterNode> params = parseParameters();

            final String typeDefault = parseScopedValue();

            final int line = current().getLine();
            return new ReflectedNativeFunctionDeclaration(name, typeDefault, fileName, params, isStatic, isConst, isLocal).setLine(line);
        }

        matchAndConsume(KEYWORD, "fn");

        final String name = matchAndConsume(KEYWORD, "constructor") ? "constructor" : consume(VARIABLE).getValue();
        final int line = current().getLine();
        final List<ParameterNode> params = parseParameters();

        final String typeDefault = parseScopedValue();


        final BlockNode block = parseBlock();

        if (name.equals("main") && params.isEmpty()) {
            return new MainFunctionDeclarationNode(block).setLine(line);
        }

        return new FunctionDeclarationNode(name, typeDefault, isStatic, isConst, isLocal, params, block).setLine(line);
    }

    private ASTNode parseNativeDeclaration() {

        boolean isConst = false;
        boolean isStatic = false;
        boolean isLocal = false;

        while (true) {
            if (matchAndConsume(KEYWORD, "const") && !isConst) {
                isConst = true;
            } else if (matchAndConsume(KEYWORD, "static") && !isStatic) {
                isStatic = true;
            } else if (matchAndConsume(KEYWORD, "local") && !isLocal) {
                isLocal = true;
            } else {
                break;
            }
        }

        consume(); // consume "native"

        if (match(KEYWORD, "class") || match(KEYWORD, "const")) {
            undo();
            return parseNativeClassDeclaration();
        }

        if (match(KEYWORD, "fn")) {
            if (isStatic) undo();
            if (isLocal) undo();
            if (isConst) undo();

            undo();
            return parseFunctionDeclaration();
        }

        consume(OPERATOR, ".");

        final int line = current().getLine();

        if (lookahead().getType().equals(OPERATOR)) {
            StringBuilder pkg = new StringBuilder();
            final String name;

            while (true) {
                final String val = consume(VARIABLE).getValue();

                if (matchAndConsume(DIVIDER, "(")) {
                    name = val;
                    break;
                }
                pkg.append(val).append(".");
                consume(OPERATOR, ".");
            }

            final List<ASTNode> params = new ArrayList<>();

            while (true) {
                if (match(DIVIDER, ")")) break;
                params.add(parseExpression());
                if (!matchAndConsume(DIVIDER, ",")) break;
            }

            consume(DIVIDER, ")");

            final String ip = pkg.substring(0, pkg.length() - 1);
            return ("def".equals(ip) ? new DefaultNativeFunctionCallNode(name, params) : new PackagedNativeFunctionCallNode(name, ip, params)).setLine(line);
        }

        final String name = consume(VARIABLE).getValue();

        final List<ASTNode> params = new ArrayList<>();

        consume(DIVIDER, "(");

        if (!match(DIVIDER, ")")) {
            do {
                params.add(parseExpression());
            } while (matchAndConsume(DIVIDER, ","));
        }
        consume(DIVIDER, ")");

        return new DefaultNativeFunctionCallNode(name, params).setLine(line);
    }

    private ASTNode parseExpression() {
        return parsePrecedence(1);
    }

    private int getPrecedence(Token token) {
        switch (token.getValue()) {
            case "||":
                return 1;
            case "&&":
                return 2;
            case "|":
                return 3;
            case "^^":
                return 4;
            case "&":
                return 5;
            case "==":
            case "!=":
                return 6;
            case "<":
            case "<=":
            case ">":
            case ">=":
            case "is":
                return 7;
            case "<<":
            case ">>":
                return 8;
            case "+":
            case "-":
                return 9;
            case "*":
            case "/":
            case "%":
                return 10;
            case "^": // ^ is pow
                return 11;
            default:
                return -1;
        }
    }

    private ASTNode parsePrecedence(int precedence) {
        ASTNode left = parsePrimary();
        while (true) {
            int currentPrecedence = getPrecedence(current());
            if (currentPrecedence < precedence) {
                break;
            }

            final int line = current().getLine();
            Token operatorToken = consume();

            if (operatorToken.is(KEYWORD, "is")) {
                final CastType valueOf = match(KEYWORD) ? CastType.fromValue(consume(KEYWORD).getValue()) : CastType.CLASS.setValue(consume(VARIABLE).getValue());
                if (valueOf == null) {
                    Register.throwException("Unknown native type: " + lookahead(-1).getValue());
                    return null;
                }
                return new IsNode(left, valueOf).setLine(line);
            }

            ASTNode right = parsePrecedence(currentPrecedence + 1);

            left = new BinaryExpressionNode(left, operatorToken.getValue(), right).setLine(line);
        }
        return left;
    }

    private ASTNode parsePrimary() {
        Token token = consume();
        final int line = token.getLine();

        switch (token.getType()) {
            case VARIABLE:
                ASTNode node = new VariableReferenceNode(token.getValue()).setLine(line);

                while (true) {
                    if (matchAndConsume(OPERATOR, ".")) {
                        final String name = consume(VARIABLE).getValue();
                        if (matchAndConsume(DIVIDER, "(")) {
                            List<ASTNode> params = new ArrayList<>();
                            while (!match(DIVIDER, ")")) {
                                params.add(parseExpression());
                                if (!matchAndConsume(DIVIDER, ",")) break;
                            }
                            consume(DIVIDER, ")");
                            node = new MemberFunctionCallNode(node, name, params).setLine(line);
                        } else {
                            node = new ObjectVariableReferenceNode(node, name).setLine(current().getLine());
                        }
                    } else if (matchAndConsume(DIVIDER, "(")) {
                        List<ASTNode> params = new ArrayList<>();
                        while (!match(DIVIDER, ")")) {
                            params.add(parseExpression());
                            if (!matchAndConsume(DIVIDER, ",")) break;
                        }
                        consume(DIVIDER, ")");
                        node = new FunctionCallNode(node, params).setLine(line);
                    } else {
                        break;
                    }
                }

                if (matchAndConsume(OPERATOR, "=")) {
                    ASTNode expression = parseExpression();
                    if (node instanceof ObjectVariableReferenceNode) return new VariableReassignmentNode(((ObjectVariableReferenceNode) node).getCalled(), expression).setLine(line);
                    else return new VariableReassignmentNode(((VariableReferenceNode) node).getName(), expression).setLine(line);
                }

                switch (current().getValue()) {
                    case "+=":
                    case "-=":
                    case "*=":
                    case "/=":
                    case "^=":
                    case "%=": {
                        if (node instanceof VariableReferenceNode) {
                            final char v = consume().getValue().charAt(0); // consume op
                            final VariableReferenceNode left = (VariableReferenceNode) node;
                            node = new VariableReassignmentNode(left.getName(), new BinaryExpressionNode(left, String.valueOf(v), parseExpression()));
                        }
                        break;
                    }
                    case "++":
                    case "--": {
                        if (node instanceof VariableReferenceNode) {
                            final char v = consume().getValue().charAt(0); // consume op
                            final VariableReferenceNode left = (VariableReferenceNode) node;
                            node = new VariableReassignmentNode(left.getName(), new BinaryExpressionNode(left, String.valueOf(v), new IntegerNode(1)));
                        }
                        break;
                    }
                }
                return node;

            case NUMBER:
                return parseNumber(token);

            case STRING:
                return new LiteralNode(token.getValue()).setLine(line);

            case DIVIDER:
                if (token.getValue().equals("(")) {
                    ASTNode astNode = parseExpression();
                    consume(DIVIDER, ")");
                    while (match(OPERATOR)){
                        final String op = consume().getValue();
                        ASTNode expression = parseExpression();
                        astNode = new BinaryExpressionNode(astNode, op, expression);
                    }
                    return astNode;
                } else if (token.getValue().equals("[")) {
                    return parseArray(line);
                }
            case KEYWORD:
                undo();
                return parseKeyword();

            case BOOLEAN:
                return new BooleanNode(Boolean.parseBoolean(token.getValue())).setLine(line);

            case OPERATOR:
                if (token.getValue().equals("@")) {
                    final ASTNode result;

                    if (match(KEYWORD, "class")) {
                        result = parseAnnotationClassDeclaration();
                    } else {
                        result = parseAnnotationUse();
                    }

                    return result.setLine(line);
                }

                return parseUnaryOperator(token, line).setLine(line);
            default:
                throw new UnsupportedOperationException("Unsupported expression token: " + token.getValue() + " (type=" + token.getType() + ") at line " + token.getLine());
        }
    }

    private ASTNode parseNumber(Token token) {
        final int line = token.getLine();
        final String value = token.getValue();
        try {
            return new IntegerNode(Integer.parseInt(value)).setLine(line);
        } catch (NumberFormatException e) {
            try {
                return new DecimalNode(Double.parseDouble(value)).setLine(line);
            } catch (NumberFormatException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    private ASTNode parseArray(int line) {
        List<ASTNode> nodes = new ArrayList<>();
        while (!match(DIVIDER, "]")) {
            nodes.add(parseExpression());
            if (!matchAndConsume(DIVIDER, ",")) break;
        }
        consume(DIVIDER, "]");
        return new ArrayNode(nodes).setLine(line);
    }

    private ASTNode parseUnaryOperator(final Token token, int line) {
        final ASTNode right = parsePrimary();
        switch (token.getValue()) {
            case "-":
                return new BinaryExpressionNode(new IntegerNode(0).setLine(line), "-", right).setLine(line);
            case "!":
                return new BinaryExpressionNode(new BooleanNode(false), "==", right).setLine(line);
            case ".":
                if (right instanceof IntegerNode) {
                    final int integerValue = ((IntegerNode) right).getValue();
                    final int numberOfDigits = (int) Math.log10(integerValue) + 1;
                    final double divisor = Math.pow(10, numberOfDigits);
                    return new DecimalNode(integerValue / divisor);
                }
            case "++":
            case "--": {
                if (right instanceof VariableReferenceNode) {
                    final char v = token.getValue().charAt(0); // consume op
                    final VariableReferenceNode left = (VariableReferenceNode) right;
                    System.out.println(new VariableReassignmentNode(left.getName(), new BinaryExpressionNode(left, String.valueOf(v), new IntegerNode(1))));
                    return new VariableReassignmentNode(left.getName(), new BinaryExpressionNode(left, String.valueOf(v), new IntegerNode(1)));
                }
            }
            default:
                throw new UnsupportedOperationException("Unsupported unary operator: " + token.getValue() + " at line " + line);
        }
    }

    private ASTNode parseReturnDeclaration() {
        final int line = consume().getLine(); // consume "return"

        final ASTNode expr = parseExpression();

        if (matchAndConsume(KEYWORD, "when")) {

            return new ConditionedReturnNode(expr, parseExpression()).setLine(line);
        }

        return new ReturnNode(expr).setLine(line);
    }

    private ASTNode parseClassDeclaration() {

        final boolean isConstant = matchAndConsume(KEYWORD, "const");

        consume(); // consume "class"
        final String name = consume(VARIABLE).getValue();

        final String inheritedClass;

        if (matchAndConsume(KEYWORD, "inherits")) {
            inheritedClass = consume(VARIABLE).getValue();
        } else {
            inheritedClass = null;
        }

        final int line = current().getLine();

        final BlockNode body = parseBlock();

        List<FunctionDeclarationNode> constructors = new ArrayList<>();
        for (ASTNode node : body.getChildren()) {
            if (node instanceof FunctionDeclarationNode) {
                FunctionDeclarationNode cast = (FunctionDeclarationNode) node;
                if (cast.getFunctionName().equals("constructor")) {
                    constructors.add(cast);
                    body.getChildren().remove(node);
                }
            }
        }

        return new ClassDeclarationNode(name, isConstant, inheritedClass, fileName, constructors, body).setLine(line);
    }

    private ASTNode parseLocalDeclaration() {
        consume(); // consume "local"
        final int line = current().getLine();

        if (matchAndConsume(OPERATOR, ".")) {
            final String name = consume(VARIABLE).getValue();
            if (match(DIVIDER, "(")) {
                consume(); // consume open paren
                List<ASTNode> params = new ArrayList<>();
                while (!match(DIVIDER, ")")) {
                    params.add(parseExpression());
                    if (!matchAndConsume(DIVIDER, ",")) break;
                }
                consume(DIVIDER, ")");
                return new LocalFunctionCallNode(name, params).setLine(line);
            } else if (matchAndConsume(OPERATOR, "=")) {
                return new LocalVariableReassignmentNode(name, parseExpression()).setLine(line);
            }
            return new LocalVariableReferenceNode(name).setLine(line);
        }
        undo();

        return parseVariableDeclaration();
    }

    private ASTNode parseNewDeclaration() {
        consume(); // consume "new"

        final int line = current().getLine();
        final String clazz = consume(VARIABLE).getValue(); // get the class name
        consume(DIVIDER, "(");

        final List<ASTNode> params = new ArrayList<>();

        while (true) {
            if (match(DIVIDER, ")")) break;
            params.add(parseExpression());
            if (!matchAndConsume(DIVIDER, ",")) break;
        }

        consume(DIVIDER, ")");

        ASTNode node = new ClassInstantiationNode(clazz, params).setLine(line);

        while (true) {
            if (matchAndConsume(OPERATOR, ".")) {
                if (match(VARIABLE)) {
                    final String nextPart = consume(VARIABLE).getValue();
                    if (match(DIVIDER, "(")) {
                        node = parseMethodCall(nextPart, node);
                    } else {
                        node = new ObjectVariableReferenceNode(node, nextPart).setLine(current().getLine());
                    }
                } else {
                    Register.throwException("Expected variable or method after '.'", current().getLine());
                }
            } else {
                break;
            }
        }

        return node;
    }

    // Parse method call, assuming `nextPart` is the method name and `receiver` is the previous node
    private ASTNode parseMethodCall(String methodName, ASTNode receiver) {
        consume(DIVIDER, "(");

        final List<ASTNode> params = new ArrayList<>();
        while (true) {
            if (match(DIVIDER, ")")) break;
            params.add(parseExpression());
            if (!matchAndConsume(DIVIDER, ",")) break;
        }

        consume(DIVIDER, ")");
        return new MemberFunctionCallNode(receiver, methodName, params).setLine(current().getLine());
    }

    private VoidNode parseVoidDeclaration() {
        final int line = consume().getLine();
        return new VoidNode().setLine(line);
    }

    private IncludeNode parseInclude() {
        boolean isNative = matchAndConsume(KEYWORD, "native");
        boolean isCompiled = matchAndConsume(KEYWORD, "compiled");

        if (isNative && isCompiled) throw new UnsupportedOperationException("Native compiled files do not exist, at line " + current().getLine());

        final int line = current().getLine();

        final String path = parseIncludePath(isCompiled);

        if (isCompiled) {
            final File file;

            if (matchAndConsume(VARIABLE, "from")) {
                file = new File(Clarity.USER_HOME + "/Clarity/libraries/" + consume(VARIABLE).getValue(), path);
            } else {
                file = new File(path);
            }

            ASTLoader loader = new ASTLoader(file);
            try {
                return new IncludeNode(path, loader.load().getRoot(), false).setLine(line);
            } catch (IOException e) {
                System.err.println("Failed to load the AST:");
                if (e instanceof NoSuchFileException) {
                    System.err.println("No such file: " + file);
                }
                System.exit(1);
            }
        }

        if (isNative) {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("natives/" + path);
            if (inputStream == null) {
                try {
                    throw new IOException("Native library not found: '" + path + "'");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String content;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                content = reader.lines().collect(Collectors.joining("\n"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            final List<Token> tokens = Tokenizer.tokenize(content);
            final ASTParser parser = new ASTParser(ORIGINAL, path, tokens);
            final AST ast = parser.parse();
            return new IncludeNode(path, ast.getRoot(), true).setLine(line);
        }

        final String content;
        try {
            content = new String(Files.readAllBytes(new File(new File(ORIGINAL).getParentFile(), path).toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final List<Token> tokens = Tokenizer.tokenize(content);
        final ASTParser parser = new ASTParser(ORIGINAL, path, tokens);
        final AST ast = parser.parse();

        return new IncludeNode(path, ast.getRoot(), false).setLine(line);
    }

    private String parseIncludePath(final boolean compiled) {
        if (match(STRING)) {
            return consume().getValue();
        }
        StringBuilder path = new StringBuilder();

        path.append(consume(VARIABLE).getValue());

        while (matchAndConsume(OPERATOR, ".")) {
            path.append("\\").append(consume(VARIABLE).getValue());
        }

        return path + (compiled ? ".cclr" : ".clr");
    }

    private ASTNode parseIfDeclaration() {

        final int line = consume().getLine(); // consume "if"

        final ASTNode condition = parseExpression();
        final BlockNode ifBlock = parseBlock();

        final IfNode node = new IfNode(condition, ifBlock);

        while (matchAndConsume(KEYWORD, "else")) {
            if (match(KEYWORD, "if")) {
                final int elifLine = consume().getLine();
                final ASTNode elseIfCondition = parseExpression();
                final BlockNode elseIfBlock = parseBlock();
                node.addElseIfStatement(new IfNode(elseIfCondition, elseIfBlock).setLine(elifLine));
            } else {
                final BlockNode elseBlock = parseBlock();
                node.setElseBlock(elseBlock);
                break;
            }
        }

        return node.setLine(line);
    }

    private ASTNode parseNullDeclaration() {
        return new NullNode().setLine(consume().getLine());
    }

    private ASTNode parseForDeclaration() {
        final int line = consume().getLine(); // consume "for"

        if (lookahead().is(OPERATOR, ":")) {
            final String var = consume(VARIABLE).getValue();
            consume();
            final ASTNode list = parseExpression();
            return new ForeachNode(var, list, parseBlock()).setLine(line);
        }

        final ASTNode declaration;
        if (!match(DIVIDER, ",")) {
            declaration = current().is(DIVIDER, "{") ? null : parseVariableDeclaration();
        } else {
            declaration = null;
        }

        consume(DIVIDER, ",");

        final ASTNode condition;
        if (!match(DIVIDER, ",")) {
            condition = current().is(DIVIDER, "{") ? null : parseExpression();
        } else {
            condition = null;
        }
        consume(DIVIDER, ",");

        final ASTNode incrementation;
        if (!match(DIVIDER, ",")) {
            incrementation = current().is(DIVIDER, "{") ? null : parseExpression();
        } else {
            incrementation = null;
        }

        final BlockNode block = parseBlock();
        return new ForNode(declaration, condition, incrementation, block).setLine(line);
    }


    private ASTNode parseWhileDeclaration() {
        final int line = consume().getLine(); // consume "while"
        final ASTNode condition = parseExpression();
        final BlockNode block = parseBlock();
        return new WhileNode(condition, block).setLine(line);
    }

    private ASTNode parseNativeClassDeclaration() {
        final boolean isConstant = matchAndConsume(KEYWORD);
        matchAndConsume(KEYWORD, "native");

        if (match(KEYWORD, "fn")) {
            undo();
            return parseFunctionDeclaration();
        }

        consume(KEYWORD, "class");
        final String name = consume(VARIABLE).getValue();

        final String inheritedClass;
        if (matchAndConsume(KEYWORD, "inherits")) {
            inheritedClass = consume(VARIABLE).getValue();
        } else {
            inheritedClass = null;
        }

        final int line = current().getLine();
        final BlockNode body = parseBlock();

        final List<FunctionDeclarationNode> constructors = new ArrayList<>();
        for (ASTNode node : body.getChildren()) {
            if (node instanceof FunctionDeclarationNode) {
                FunctionDeclarationNode cast = (FunctionDeclarationNode) node;
                if (cast.getFunctionName().equals("constructor")) {
                    constructors.add(cast);
                    body.getChildren().remove(node);
                }
            }
        }
        return new NativeClassDeclarationNode(name, isConstant, inheritedClass, fileName, constructors, body).setLine(line);
    }

    private ASTNode parseSelectDeclaration() {
        final int line = consume().getLine(); // consume "select"
        final ASTNode condition = parseExpression();

        final List<SelectNode.WhenNode> cases = new ArrayList<>();
        BlockNode defaultBlock = null;

        consume(DIVIDER, "{");
        while (!match(DIVIDER, "}")) {
            if (matchAndConsume(KEYWORD, "when")) {
                final int whenLine = current().getLine(); // line of "when"
                final ASTNode whenCondition = parseExpression();
                if (!match(DIVIDER, "{")) {
                    Register.throwException("When block must be declared with brackets");
                    return null;
                }
                final BlockNode whenBlock = parseBlock();
                cases.add(new SelectNode.WhenNode(whenCondition, whenBlock).setLine(whenLine));
                continue;
            } else if (match(KEYWORD, "default")) {
                if (defaultBlock != null) throw new IllegalStateException("Multiple default blocks: " + current());
                final int defaultLine = consume().getLine(); // consume "default"
                if (!match(DIVIDER, "{")) {
                    Register.throwException("Default block must be declared with brackets");
                    return null;
                }
                defaultBlock = parseBlock().setLine(defaultLine);
                continue;
            }
            throw new UnsupportedOperationException("Unsupported token in switch: " + current());
        }

        consume(DIVIDER, "}");
        return new SelectNode(condition, cases, defaultBlock).setLine(line);
    }

    private ASTNode parseBreakDeclaration() {
        return new ReturnNode(new BreakNode().setLine(consume().getLine()));
    }

    private ASTNode parseContinueDeclaration() {
        return new ReturnNode(new ContinueNode().setLine(consume().getLine()));
    }

    private ASTNode parseFloatDeclaration() {
        final int line = consume().getLine(); // consume "float"
        if (!matchAndConsume(DIVIDER, "(")) {
            undo();
            return parseVariableDeclaration();
        }
        return new NativeCastNode(CastType.FLOAT, parseExpression()).setLine(consume(DIVIDER, ")").getLine()).setLine(line);
    }

    private ASTNode parseIntDeclaration() {
        final int line = consume().getLine(); // consume "int"
        if (!matchAndConsume(DIVIDER, "(")) {
            undo();
            return parseVariableDeclaration();
        }
        return new NativeCastNode(CastType.INT, parseExpression()).setLine(consume(DIVIDER, ")").getLine()).setLine(line);
    }

    private ASTNode parseAssertDeclaration() {
        final int line = consume().getLine(); // consume "assert"
        final ASTNode condition = parseExpression();
        final ASTNode message = matchAndConsume(KEYWORD, "else") ? parseExpression() : new LiteralNode("\"Assertion error at line " + line + "\"");
        return new AssertNode(condition, message).setLine(line);
    }

    private ASTNode parseIsDeclaration() {
        throw new RuntimeException();
    }

    private List<ParameterNode> parseParameters() {
        List<ParameterNode> params = new ArrayList<>();

        consume(DIVIDER, "(");

        if (match(VARIABLE)) {
            do {
                Token token = consume(VARIABLE); // Consume the parameter variable
                params.add(new ParameterNode(token.getValue()).setLine(token.getLine()));

                if (match(DIVIDER, ",")) {
                    consume(); // Consume the comma separator
                } else {
                    break;
                }
            } while (true);
        }

        consume(DIVIDER, ")"); // Consume the closing parenthesis
        return params;
    }

    private ASTNode parseStrDeclaration() {
        final int line = consume().getLine(); // consume "str"
        if (!matchAndConsume(DIVIDER, "(")) {
            undo();
            return parseVariableDeclaration();
        }
        return new NativeCastNode(CastType.STR, parseExpression()).setLine(consume(DIVIDER, ")").getLine()).setLine(line);
    }

    private ASTNode parseArrDeclaration() {
        final int line = consume().getLine(); // consume "arr"
        if (!matchAndConsume(DIVIDER, "(")) {
            undo();
            return parseVariableDeclaration();
        }
        return new NativeCastNode(CastType.ARR, parseExpression()).setLine(consume(DIVIDER, ")").getLine()).setLine(line);
    }

    private ASTNode parseEnumDeclaration()  {
        final boolean isConstant = matchAndConsume(KEYWORD, "const");
        final int line = consume().getLine(); // consume "enum"
        final String enumName = consume(VARIABLE).getValue();
        consume(DIVIDER, "{");

        final List<EnumDeclarationNode.EnumValueNode> enumValues = new ArrayList<>();

        if (match(VARIABLE)) {
            do {
                final String name = consume().getValue();
                final ASTNode value;
                if (matchAndConsume(DIVIDER, "(")) {
                    value = parseExpression();
                    consume(DIVIDER, ")");
                } else {
                    value = new NullNode();
                }
                enumValues.add(new EnumDeclarationNode.EnumValueNode(name, value));
            } while (matchAndConsume(DIVIDER, ","));
        }

        consume(DIVIDER, "}");
        return new EnumDeclarationNode(enumName, isConstant, fileName, enumValues).setLine(line);
    }

    private ASTNode parseAnnotationClassDeclaration() {
        consume(); // consume keyword 'class'

        final String name = consume(VARIABLE).getValue();

        // custom block parsing
        consume(DIVIDER, "{");

        final List<AnnotationDeclarationNode.AnnotationElement> elements = new ArrayList<>();
        while (!matchAndConsume(DIVIDER, "}")) {
            final String requiredValueName = consume(VARIABLE).getValue();
            final ASTNode node = matchAndConsume(KEYWORD, "default") ? parseExpression() : null;
            elements.add(new AnnotationDeclarationNode.AnnotationElement(requiredValueName, node));
        }

        return new  AnnotationDeclarationNode(name, elements);
    }

    private ASTNode parseAnnotationUse() {
        final String used = consume(VARIABLE).getValue();
        final List<AnnotationUseNode.AnnotationValueAssign> values = new ArrayList<>();
        if (matchAndConsume(DIVIDER, "(")) {
            while (true) {
                if (!match(VARIABLE)) break;
                final String name = consume().getValue();
                consume(OPERATOR, "=");
                final ASTNode value = parseExpression();
                values.add(new AnnotationUseNode.AnnotationValueAssign(name, value));
                if (!matchAndConsume(DIVIDER, ",")) break;
            }
            consume(DIVIDER, ")");
        }

        return new AnnotationUseNode(used, values, parseExpression());
    }

    private String parseScopedValue() {
        return matchAndConsume(OPERATOR, "->") ? consume().getValue() : null;
    }

    private ASTNode parseBoolDeclaration() {
        final int line = consume().getLine(); // consume "bool"
        if (!matchAndConsume(DIVIDER, "(")) {
            undo();
            return parseVariableDeclaration();
        }
        return new NativeCastNode(CastType.BOOL, parseExpression()).setLine(consume(DIVIDER, ")").getLine()).setLine(line);
    }



















    private boolean matchAndConsume(final TokenType type) {
        final boolean match = match(type);
        if (match) consume();
        return match;
    }

    private void undo() {
        currentTokenIndex--;
    }

    private Token consume() {
        if (currentTokenIndex >= tokens.size()) {
            throw new IllegalStateException("Unexpected end of input, in file: " + fileName);
        }
        return tokens.get(currentTokenIndex++);
    }

    private boolean matchAndConsume(final TokenType type, final String value) {
        final boolean match = match(type, value);
        if (match) consume();
        return match;
    }

    private Token consume(final TokenType expectedType) {
        final Token token = consume();
        if (token.getType() != expectedType) {
            throw new IllegalStateException("Expected token type " + expectedType + " but found " + token.getType() + " at line " + token.getLine() + ", in file: " + fileName);
        }
        return token;
    }

    private Token consume(final TokenType expectedType, final String expectedValue) {
        final Token token = consume();
        if (token.getType() != expectedType || !token.getValue().equals(expectedValue)) {
            throw new IllegalStateException("Expected value " + expectedValue + " but found " + token.getValue() + " at line " + token.getLine() + ", in file: " + fileName);
        }
        return token;
    }

    private boolean match(final TokenType expectedType) {
        return currentTokenIndex < tokens.size() && current().getType() == expectedType;
    }

    private boolean match(final TokenType expectedType, final String expectedValue) {
        return match(expectedType) && current().getValue().equals(expectedValue);
    }

    private Token current() {
        return currentTokenIndex < tokens.size() ? tokens.get(currentTokenIndex) : except();
    }

    private Token lookahead(final int ahead) {
        return currentTokenIndex + ahead >= tokens.size() ? except() : tokens.get(currentTokenIndex + ahead);
    }

    private Token lookahead() {
        return lookahead(1);
    }

    private Token except() {
        return new Token(null, "null", -1);
    }
}