package me.kuwg.clarity.parser;

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
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.ast.nodes.include.IncludeNode;
import me.kuwg.clarity.ast.nodes.literal.*;
import me.kuwg.clarity.ast.nodes.reference.ContextReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.assign.ObjectVariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.assign.VariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.get.LocalVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.ObjectVariableReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.get.VariableReferenceNode;
import me.kuwg.clarity.token.Token;
import me.kuwg.clarity.token.TokenType;
import me.kuwg.clarity.token.Tokenizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static me.kuwg.clarity.token.TokenType.*;

public final class ASTParser {

    private final String ORIGINAL;
    private final List<Token> tokens;
    private int currentTokenIndex = 0;

    public ASTParser(final String original, final List<Token> tokens) {
        ORIGINAL = original;
        this.tokens = tokens;
    }

    public AST parse() {
        final BlockNode node = new BlockNode();

        while (matchAndConsume(KEYWORD, "include")) {
            final ASTNode include = parseInclude();
            if (include != null) node.addChild(include);
        }

        while (currentTokenIndex < tokens.size()) {
            final ASTNode result = parseStatement();
            if (result != null) node.addChild(result);
        }
        return new AST(node);
    }

    private ASTNode parseStatement() {
        return Objects.requireNonNull(current().getType()) == KEYWORD ? parseKeyword() : parseExpression();
    }

    private BlockNode parseBlock() {
        final BlockNode node = new BlockNode();

        consume(DIVIDER, "{");

        while (!matchAndConsume(DIVIDER, "}")) {
            final ASTNode result = parseStatement();
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

        ASTNode value = matchAndConsume(OPERATOR, "=") ? parseStatement() : new VoidNode().setLine(line);

        return handleVariableDeclaration(name, value, isConst, isStatic);
    }

    private ASTNode parseFunctionDeclaration() {

        boolean isStatic = matchAndConsume(KEYWORD, "static");

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

    private int getPrecedence(Token token) {
        if (token.getType() == OPERATOR) {
            String op = token.getValue();
            if ("+".equals(op) || "-".equals(op)) {
                return 1;
            } else if ("*".equals(op) || "/".equals(op) || "%".equals(op) || "^".equals(op)) {
                return 2;
            }
        }
        return -1;
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
                        nodes.add(parseStatement());
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

        }

        throw new UnsupportedOperationException("Unsupported expression token: " + token.getValue() + " at line " + token.getLine());
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

        return new ClassDeclarationNode(name, constructor, body).setLine(line);
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
        final boolean isNative = matchAndConsume(KEYWORD, "native");
        final String path = parseIncludePath();

        final File file = isNative ? null : new File(path);
        final String absolutePath = isNative ? path : file.getAbsolutePath();

        if (!isNative && ORIGINAL.equals(absolutePath)) {
            return null;
        }

        final String content;
        if (isNative) {
            content = new BufferedReader(
                    new InputStreamReader(
                            Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(path),
                                    "Native library not found: " + path), StandardCharsets.UTF_8
                    )
            ).lines().collect(Collectors.joining("\n"));
        } else {
            try {
                content = new String(Files.readAllBytes(file.toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        final List<Token> tokens = Tokenizer.tokenize(content);
        final ASTParser parser = new ASTParser(ORIGINAL, tokens);
        final AST ast = parser.parse();
        return new IncludeNode(ast.getRoot(), isNative).setLine(current().getLine());
    }

    private String parseIncludePath() {
        if (match(STRING)) {
            return consume().getValue();
        }
        StringBuilder path = new StringBuilder();

        path.append(consume(VARIABLE).getValue());

        while (matchAndConsume(OPERATOR, ".")) {
            path.append("\\").append(consume(VARIABLE).getValue());
        }

        return path + ".clr";
    }

    private ASTNode handleVariableDeclaration(final String name, final ASTNode value, final boolean k, final boolean s) {
        return new VariableDeclarationNode(name, value, k, s).setLine(lookahead(-1).getLine());
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
