package me.kuwg.clarity.parser;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.block.ReturnNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassInstantiationNode;
import me.kuwg.clarity.ast.nodes.clazz.NativeClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.ReflectedNativeFunctionDeclaration;
import me.kuwg.clarity.ast.nodes.statements.ForNode;
import me.kuwg.clarity.ast.nodes.statements.ForeachNode;
import me.kuwg.clarity.ast.nodes.statements.IfNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.call.*;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.ast.nodes.include.IncludeNode;
import me.kuwg.clarity.ast.nodes.literal.*;
import me.kuwg.clarity.ast.nodes.reference.ContextReferenceNode;
import me.kuwg.clarity.ast.nodes.statements.WhileNode;
import me.kuwg.clarity.ast.nodes.variable.assign.ObjectVariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.get.LocalVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.ObjectVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.VariableReferenceNode;
import me.kuwg.clarity.compiler.ASTLoader;
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

        if (!matchAndConsume(DIVIDER, "{")) {
            final ASTNode result = parseExpression();
            if (result != null) node.addChild(result);
            else throw new IllegalStateException("Block has no expression at line " + node.getLine());
            return node;
        }

        while (!matchAndConsume(DIVIDER, "}")) {
            final ASTNode result = parseExpression();
            if (result != null) node.addChild(result);
        }

        return node;
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
            default:
                throw new UnsupportedOperationException("Unsupported keyword: " + keyword);
        }
    }

    private ASTNode parseVariableDeclaration() {
        boolean isConst = false;
        boolean isStatic = false;

        while (true) {
            if (matchAndConsume(KEYWORD, "const")) {
                isConst = true;
            } else if (matchAndConsume(KEYWORD, "static")) {
                isStatic = true;
            } else {
                break;
            }
        }

        final int line = current().getLine();

        if (!matchAndConsume(KEYWORD, "var")) {
            if (isStatic) undo();
            if (isConst) throw new UnsupportedOperationException("Constant methods still not supported.");
            return parseFunctionDeclaration().setLine(line);
        }

        final String name = consume(VARIABLE).getValue();

        ASTNode value;
        value = matchAndConsume(OPERATOR, "=") ? parseExpression() : new VoidNode().setLine(line);

        return new VariableDeclarationNode(name, value, isConst, isStatic).setLine(lookahead(-1).getLine());
    }

    private ASTNode parseFunctionDeclaration() {
        boolean isStatic = matchAndConsume(KEYWORD, "static");
        if (matchAndConsume(KEYWORD, "native")) {
            consume(KEYWORD, "fn");

            final String name = consume(VARIABLE).getValue();

            final List<ParameterNode> params = new ArrayList<>();

            consume(DIVIDER, "(");
            if (match(VARIABLE)) {
                do {
                    params.add(new ParameterNode(consume(VARIABLE).getValue()));
                    if (match(DIVIDER, ",")) {
                        consume();
                    } else {
                        break;
                    }
                } while (true);
            }

            consume(DIVIDER, ")");

            return new ReflectedNativeFunctionDeclaration(name, fileName, params, isStatic);
        }

        matchAndConsume(KEYWORD, "fn");

        final String name = matchAndConsume(KEYWORD, "constructor") ? "constructor" : consume(VARIABLE).getValue();

        final List<ParameterNode> params = new ArrayList<>();

        final int line = current().getLine();

        consume(DIVIDER, "(");
        if (match(VARIABLE)) {
            do {
                params.add(new ParameterNode(consume(VARIABLE).getValue()));
                if (match(DIVIDER, ",")) {
                    consume();
                } else {
                    break;
                }
            } while (true);
        }

        consume(DIVIDER, ")");

        final BlockNode block = parseBlock();

        if (name.equals("main") && params.isEmpty()) {
            return new MainFunctionDeclarationNode(block).setLine(line);
        }

        return new FunctionDeclarationNode(name, isStatic, params, block).setLine(line);
    }

    private ASTNode parseNativeDeclaration() {
        consume(); // consume native

        if (matchAndConsume(KEYWORD, "class")) {
            return parseNativeClassDeclaration();
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

        while (!matchAndConsume(DIVIDER, ")")) {
            params.add(parseExpression());
        }

        return new DefaultNativeFunctionCallNode(name, params).setLine(line);
    }

    private ASTNode parseExpression() {
        return parsePrecedence(1);
    }

    private int getPrecedence(Token token) {
        if (token.getType() == OPERATOR) {
            String op = token.getValue();
            switch (op) {
                case "||":
                    return 1;
                case "&&":
                    return 2;
                case "==":
                case "!=":
                    return 3;
                case "<":
                case "<=":
                case ">":
                case ">=":
                    return 4;
                case "+":
                case "-":
                    return 5;
                case "*":
                case "/":
                case "%":
                    return 6;
                case "^":
                    return 7;
                default:
                    return -1;
            }
        }
        return -1;
    }

    @SuppressWarnings("InfiniteRecursion")
    private ASTNode parsePrecedence(int precedence) {
        ASTNode left = parsePrimary();
        while (true) {
            int currentPrecedence = getPrecedence(current());

            if (currentPrecedence < precedence) {
                break;
            }

            final int line = current().getLine();

            Token operatorToken = consume();
            ASTNode right = parsePrecedence(currentPrecedence + 1);
            left = new BinaryExpressionNode(left, operatorToken.getValue(), right).setLine(line);
        }
        return left;
    }

    private ASTNode parsePrimary() {
        Token token = consume();

        final int line = current().getLine();

        switch (token.getType()) {
            case VARIABLE:
                if (matchAndConsume(OPERATOR, "=")) {
                    if (match(OPERATOR, ".")) {
                        consume(); // consume the '.'
                        final String called = consume(VARIABLE).getValue();
                        return new ObjectVariableReassignmentNode(token.getValue(), called, parseExpression()).setLine(line);
                    } else {
                        return new VariableReassignmentNode(token.getValue(), parseExpression()).setLine(line);
                    }
                } else if (matchAndConsume(OPERATOR, ".")) {
                    final String name = consume(VARIABLE).getValue();

                    if (matchAndConsume(DIVIDER, "(")) {
                        final List<ASTNode> params = new ArrayList<>();

                        while (true) {
                            if (match(DIVIDER, ")")) break;
                            params.add(parseExpression());
                            if (!matchAndConsume(DIVIDER, ",")) break;
                        }

                        consume(DIVIDER, ")");

                        return new ObjectFunctionCallNode(token.getValue(), name, params).setLine(line);
                    } else {
                        return new ObjectVariableReferenceNode(token.getValue(), name).setLine(line);
                    }
                } else if (matchAndConsume(DIVIDER, "(")) {
                    final List<ASTNode> params = new ArrayList<>();

                    while (true) {
                        if (match(DIVIDER, ")")) break;
                        params.add(parseExpression());
                        if (!matchAndConsume(DIVIDER, ",")) break;
                    }

                    consume(DIVIDER, ")");

                    return new FunctionCallNode(token.getValue(), params).setLine(line);
                }

                return new VariableReferenceNode(token.getValue()).setLine(line);

            case NUMBER:
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

            case STRING:
                return new LiteralNode(token.getValue()).setLine(line);

            case DIVIDER:
                if (token.getValue().equals("(")) {
                    ASTNode expression = parseExpression();
                    consume(DIVIDER, ")");
                    return expression.setLine(line);
                } else if (token.getValue().equals("[")) {

                    List<ASTNode> nodes = new ArrayList<>();

                    while (true) {
                        if (match(DIVIDER, "]")) break;
                        nodes.add(parseExpression());
                        if (!matchAndConsume(DIVIDER, ",")) break;
                    }

                    consume(DIVIDER, "]");

                    return new ArrayNode(nodes).setLine(line);
                }

                break;

            case KEYWORD:
                undo();
                if (token.getValue().equals("local")) {
                    return parsePrimaryLocalDeclaration().setLine(line);
                } else {
                    return parseKeyword().setLine(line);
                }
            case BOOLEAN:
                return new BooleanNode(Boolean.parseBoolean(token.getValue()));
        }

        throw new UnsupportedOperationException("Unsupported expression token: " + token.getValue() + " (type=" + token.getType() + ") at line " + token.getLine());
    }

    private ASTNode parseReturnDeclaration() {
        consume(); // consume "return"
        return new ReturnNode(parseExpression());
    }

    private ASTNode parseClassDeclaration() {
        consume(); // consume "class"
        final String name = consume(VARIABLE).getValue();

        final int line = current().getLine();

        final BlockNode body = parseBlock();

        FunctionDeclarationNode constructor = null;
        for (ASTNode node : body.getChildrens()) {
            if (node instanceof FunctionDeclarationNode) {
                FunctionDeclarationNode cast = (FunctionDeclarationNode) node;
                if (cast.getFunctionName().equals("constructor")) {
                    constructor = cast;
                    body.getChildrens().remove(node);
                    break;
                }
            }
        }

        return new ClassDeclarationNode(name, fileName, constructor, body).setLine(line);
    }

    private ASTNode parseLocalDeclaration() {
        consume(); // consume "local"
        final int line = current().getLine();
        if (matchAndConsume(OPERATOR, "."))
            return new ContextReferenceNode(ContextReferenceNode.ReferenceType.LOCAL).setLine(line);
        undo();
        return parseVariableDeclaration();
    }

    private ASTNode parseNewDeclaration() {
        consume(); // consume "new"

        final int line = current().getLine();

        final String clazz = consume(VARIABLE).getValue();
        consume(DIVIDER, "(");

        final List<ASTNode> params = new ArrayList<>();

        while (true) {
            if (match(DIVIDER, ")")) break;
            params.add(parseExpression());
            if (!matchAndConsume(DIVIDER, ",")) break;
        }

        consume(DIVIDER, ")");

        return new ClassInstantiationNode(clazz, params).setLine(line);
    }

    private ASTNode parsePrimaryLocalDeclaration() {
        consume(); // consume "local"
        consume(OPERATOR, ".");
        final int line = current().getLine();
        final String name = consume(VARIABLE).getValue();
        if (matchAndConsume(DIVIDER, "(")) {
            final List<ASTNode> params = new ArrayList<>();
            do if (match(VARIABLE)) params.add(parseExpression());
            while (matchAndConsume(DIVIDER, ","));
            consume(DIVIDER, ")");
            return new LocalFunctionCallNode(name, params).setLine(line);
        } else {
            return new LocalVariableReferenceNode(name).setLine(line);
        }
    }

    private VoidNode parseVoidDeclaration() {
        final int line = current().getLine();
        consume();
        return new VoidNode().setLine(line);
    }

    private IncludeNode parseInclude() {
        boolean isNative = matchAndConsume(KEYWORD, "native");
        boolean isCompiled = matchAndConsume(KEYWORD, "compiled");

        if (isNative && isCompiled) throw new UnsupportedOperationException("Native compiled files do not exist, at line " + current().getLine());

        final String path = parseIncludePath(isCompiled);

        if (isCompiled) {

            File file = new File(path);
            ASTLoader loader = new ASTLoader(file);
            try {
                return new IncludeNode(path, loader.load().getRoot(), false);
            } catch (IOException e) {
                System.err.println("Failed to load the AST:");
                if (e instanceof NoSuchFileException) {
                    System.err.println("No such file: " + file);
                }
                System.exit(1);
            }
        }

        if (isNative) {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
            if (inputStream == null) {
                try {
                    throw new IOException("Native library not found: " + path);
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
            return new IncludeNode(path, ast.getRoot(), true).setLine(current().getLine());
        }

        final String content;
        try {
            content = new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final List<Token> tokens = Tokenizer.tokenize(content);
        final ASTParser parser = new ASTParser(ORIGINAL, path, tokens);
        final AST ast = parser.parse();
        return new IncludeNode(path, ast.getRoot(), false).setLine(current().getLine());
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

        consume(); // consume "if"

        final ASTNode condition = parseExpression();
        final BlockNode ifBlock = parseBlock();

        final IfNode node = new IfNode(condition, ifBlock);

        while (matchAndConsume(KEYWORD, "else")) {
            if (matchAndConsume(KEYWORD, "if")) {
                final ASTNode elseIfCondition = parseExpression();
                final BlockNode elseIfBlock = parseBlock();
                node.addElseIfStatement(new IfNode(elseIfCondition, elseIfBlock));
            } else {
                final BlockNode elseBlock = parseBlock();
                node.setElseBlock(elseBlock);
                break;
            }
        }

        return node;
    }

    private ASTNode parseNullDeclaration() {
        consume(); // consume "null"
        return new NullNode();
    }

    private ASTNode parseForDeclaration() {
        consume(); // consume "for"

        if (lookahead().is(OPERATOR, ":")) {
            // foreach
            final String var = consume(VARIABLE).getValue();

            consume(); // consume :

            final ASTNode list = parseExpression();

            return new ForeachNode(var, list, parseBlock());
        }

        final ASTNode declaration;
        if (!match(DIVIDER, ",")) declaration = parseVariableDeclaration();
        else declaration = null;

        consume(DIVIDER, ",");

        final ASTNode condition;
        if (!match(DIVIDER, ",")) condition = parseExpression();
        else condition = null;

        consume(DIVIDER, ",");

        final ASTNode incrementation;
        if (!match(DIVIDER, ",")) incrementation = parseExpression();
        else incrementation = null;

        final BlockNode block = parseBlock();
        return new ForNode(declaration, condition, incrementation, block);
    }

    private ASTNode parseWhileDeclaration(){
        consume(); // consume "while"

        final ASTNode condition = parseExpression();

        final BlockNode block = parseBlock();
        return new WhileNode(condition, block);
    }

    private ASTNode parseNativeClassDeclaration() {
        final String name = consume(VARIABLE).getValue();
        final int line = current().getLine();

        final BlockNode body = parseBlock();

        FunctionDeclarationNode constructor = null;
        for (ASTNode node : body.getChildrens()) {
            if (node instanceof FunctionDeclarationNode) {
                FunctionDeclarationNode cast = (FunctionDeclarationNode) node;
                if (cast.getFunctionName().equals("constructor")) {
                    constructor = cast;
                    body.getChildrens().remove(node);
                    break;
                }
            }
        }
        return new NativeClassDeclarationNode(name, fileName, constructor, body).setLine(line);
    }













    private void undo() {
        currentTokenIndex--;
    }

    private Token consume() {
        if (currentTokenIndex >= tokens.size()) {
            throw new IllegalStateException("Unexpected end of input.");
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
            throw new IllegalStateException("Expected token type " + expectedType + " but found " + token.getType() + " at line " + token.getLine());
        }
        return token;
    }

    private void consume(final TokenType expectedType, final String expectedValue) {
        final Token consumed = consume(expectedType);
        if (!consumed.getValue().equals(expectedValue)) {
            throw new IllegalStateException("Expected value " + expectedValue + " but found " + consumed.getValue() + " at line " + consumed.getLine());
        }
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