package me.kuwg.clarity.parser;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.block.ReturnNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassDeclarationNode;
import me.kuwg.clarity.ast.nodes.clazz.ClassInstantiationNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.call.FunctionCallNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.MainFunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.ast.nodes.literal.DecimalNode;
import me.kuwg.clarity.ast.nodes.literal.IntegerNode;
import me.kuwg.clarity.ast.nodes.literal.LiteralNode;
import me.kuwg.clarity.ast.nodes.function.call.NativeFunctionCallNode;
import me.kuwg.clarity.ast.nodes.reference.ContextReferenceNode;
import me.kuwg.clarity.ast.nodes.variable.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.VariableReassignmentNode;
import me.kuwg.clarity.ast.nodes.variable.VariableReferenceNode;
import me.kuwg.clarity.token.Token;
import me.kuwg.clarity.token.TokenType;

import java.util.ArrayList;
import java.util.List;

import static me.kuwg.clarity.token.TokenType.*;

public final class ASTParser {
    private final List<Token> tokens;
    private int currentTokenIndex = 0;

    public ASTParser(final List<Token> tokens) {
        this.tokens = tokens;
    }

    public AST parse() {
        final BlockNode node = new BlockNode();

        while (currentTokenIndex < tokens.size()) {
            final ASTNode result = parseStatement();
            if (result != null) node.addChild(result);
        }
        return new AST(node);
    }

    private ASTNode parseStatement() {
        final Token current = current();
        switch (current.getType()) {
            case KEYWORD:
                return parseKeyword();
            default:
                return parseExpression();
        }
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
            default:
                throw new UnsupportedOperationException("Unsupported keyword: " + keyword);
        }
    }

    private ASTNode parseVariableDeclaration() {
        consume(); // consume "var"

        final String name = consume(VARIABLE).getValue();

        if (matchAndConsume(OPERATOR, "=")) {
            return new VariableDeclarationNode(name, parseExpression());
        }

        return new VariableDeclarationNode(name, null);
    }

    private ASTNode parseFunctionDeclaration() {

        final String name = consume().getValue().equals("constructor") ? "constructor" : consume(VARIABLE).getValue();


        final List<ParameterNode> params = new ArrayList<>();

        // consume function parameters
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
            return new MainFunctionDeclarationNode(block);
        }

        return new FunctionDeclarationNode(name, params, block);
    }

    private NativeFunctionCallNode parseNativeDeclaration() {
        consume(); // consume native

        consume(OPERATOR, ".");

        final String name = consume(VARIABLE).getValue();
        final List<ASTNode> params = new ArrayList<>();

        consume(DIVIDER, "(");
        while (!matchAndConsume(DIVIDER, ")")) {
            params.add(parseExpression());
        }
        return new NativeFunctionCallNode(name, params);
    }

    private ASTNode parseExpression() {
        if (lookahead().getValue().equals("(")) {
            if (!match(VARIABLE)) {
                return parsePrecedence(1);
            }
            final Token token = consume();
            consume();
            final List<ASTNode> params = new ArrayList<>();

            while (true) {
                params.add(parseExpression());
                if (match(DIVIDER, ",")) {
                    consume(DIVIDER, ",");
                } else {
                    consume(DIVIDER, ")");
                    break;
                }
            }

            return new FunctionCallNode(token.getValue(), params);
        }

        return parsePrecedence(1);
    }

    private ASTNode parsePrecedence(int precedence) {
        ASTNode left = parsePrimary();
        while (true) {
            int currentPrecedence = getPrecedence(current());
            if (currentPrecedence < precedence) {
                break;
            }

            Token operatorToken = consume();
            ASTNode right = parsePrecedence(currentPrecedence + 1);
            left = new BinaryExpressionNode(left, operatorToken.getValue(), right);
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
        Token token = current();
        if (!token.getType().equals(KEYWORD)) consume();
        switch (token.getType()) {
            case VARIABLE:
                if (current().getValue().equals("=")) {
                    consume();
                    return new VariableReassignmentNode(token.getValue(), parseExpression());
                }
                return new VariableReferenceNode(token.getValue());
            case NUMBER:
                final String value = token.getValue();

                try {
                    return new IntegerNode(Integer.parseInt(value));
                } catch (final NumberFormatException e) {
                    try {
                        return new DecimalNode(Double.parseDouble(value));
                    } catch (final NumberFormatException e1) {
                        throw new RuntimeException(e1);
                    }
                }
            case STRING:
                return new LiteralNode(token.getValue());
            case DIVIDER:
                if (token.getValue().equals("(")) {
                    ASTNode expression = parseExpression();
                    consume(DIVIDER, ")");
                    return expression;
                }

                break;
            case KEYWORD:
                return parseKeyword();
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


        final BlockNode body = parseBlock();

        FunctionDeclarationNode constructor = null;
        for (ASTNode node : body.getChildren()) {
            if (node instanceof FunctionDeclarationNode) {
                FunctionDeclarationNode cast = (FunctionDeclarationNode) node;
                if (cast.getFunctionName().equals("constructor")) {
                    constructor = cast;
                    body.getChildren().remove(node);
                    break;
                }
            }
        }

        return new ClassDeclarationNode(name, constructor, body);
    }

    private ASTNode parseLocalDeclaration() {
        consume(); // consume "local"
        consume(OPERATOR, ".");
        return new ContextReferenceNode(ContextReferenceNode.ReferenceType.LOCAL);
    }

    private ASTNode parseNewDeclaration() {
        consume(); // consume "new"
        final String clazz = consume(VARIABLE).getValue();
        consume(DIVIDER, "(");
        final List<ASTNode> params = new ArrayList<>();

        while (true) {
            params.add(parseExpression());
            if (match(DIVIDER, ",")) {
                consume(DIVIDER, ",");
            } else {
                consume(DIVIDER, ")");
                break;
            }
        }

        return new ClassInstantiationNode(clazz, params);
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

    private Token consume(final TokenType expectedType, final String expectedValue) {
        final Token consumed = consume(expectedType);
        if (!consumed.getValue().equals(expectedValue)) {
            throw new IllegalStateException("Expected value " + expectedValue + " but found " + consumed.getValue() + " at line " + consumed.getLine());
        }
        return consumed;
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
        throw new RuntimeException("Out of tokens");
    }
}
