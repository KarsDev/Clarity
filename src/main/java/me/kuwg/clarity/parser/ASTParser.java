package me.kuwg.clarity.parser;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.expression.BinaryExpressionNode;
import me.kuwg.clarity.ast.nodes.function.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.ParameterNode;
import me.kuwg.clarity.ast.nodes.literal.DecimalNode;
import me.kuwg.clarity.ast.nodes.literal.IntegerNode;
import me.kuwg.clarity.ast.nodes.literal.LiteralNode;
import me.kuwg.clarity.ast.nodes.variable.VariableDeclarationNode;
import me.kuwg.clarity.ast.nodes.variable.VariableReferenceNode;
import me.kuwg.clarity.token.Token;
import me.kuwg.clarity.token.TokenType;

import java.util.*;

import static me.kuwg.clarity.token.TokenType.*;

public final class ASTParser {
    private final List<Token> tokens;
    private int currentTokenIndex = 0;


    public ASTParser(final List<Token> tokens) {
        this.tokens = tokens;
    }

    public AST parse() {
        final BlockNode node = new BlockNode();

        while (currentTokenIndex < tokens.size())
            node.addChild(parseStatement());

        return new AST(node);
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private ASTNode parseStatement() {
        final Token current = current();
        switch (current.getType()) {
            case KEYWORD:
                return parseKeyword();
        }

        throw new UnsupportedOperationException("Unsupported token: " + current.getValue() + " at line " + current.getLine());
    }

    private BlockNode parseBlock() {
        final BlockNode node = new BlockNode();

        consume(DIVIDER, "{");

        while (!matchAndConsume(DIVIDER, "}")) node.addChild(parseStatement());

        return node;
    }

    private ASTNode parseKeyword() {
        final Token current = current();
        final Keyword keyword = Keyword.keyword(current);

        switch (keyword) {
            case VAR: return parseVariableDeclaration();
            case FN: return parseFunctionDeclaration();
        }

        throw new UnsupportedOperationException("Unsupported keyword: " + keyword);
    }

    private ASTNode parseVariableDeclaration() {
        consume(); // consume "var"

        final String name = consume(VARIABLE).getValue();

        if (matchAndConsume(OPERATOR, "=")) {
            // assign
            return new VariableDeclarationNode(name, parseExpression());
        }

        return new VariableDeclarationNode(name, null);
    }

    private FunctionDeclarationNode parseFunctionDeclaration() {
        consume(); // consume fn keyword

        final String name = consume(VARIABLE).getValue();

        final List<ParameterNode> params = new ArrayList<>();

        // yummy consume food
        consume(DIVIDER, "(");

        while (!matchAndConsume(DIVIDER, ")")) params.add(new ParameterNode(consume(VARIABLE).getValue()));

        final BlockNode block = parseBlock();
        return new FunctionDeclarationNode(name, params, block);
    }

    private ASTNode parseExpression() {
        // Start with the left operand (could be a literal or variable)
        ASTNode left = parsePrimary();

        // Continue parsing the expression while operators are present
        while (isOperator(current())) {
            Token operatorToken = consume(); // consume the operator
            ASTNode right = parsePrimary(); // parse the right operand
            left = new BinaryExpressionNode(left, operatorToken.getValue(), right); // create a binary expression node
        }

        return left;
    }

    private ASTNode parsePrimary() {
        Token token = consume();

        switch (token.getType()) {
            case VARIABLE:
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
                return new LiteralNode(consume().getValue());
            case DIVIDER:
                if (token.getValue().equals("(")) {
                    ASTNode expression = parseExpression();
                    consume(DIVIDER, ")");
                    return expression;
                }
                break;
        }

        throw new UnsupportedOperationException("Unsupported expression token: " + token.getValue() + " at line " + token.getLine());
    }

    private boolean isOperator(Token token) {
        return token.getType() == OPERATOR;
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

    private Token lookahead() {
        return currentTokenIndex + 1 >= tokens.size() ? except() : tokens.get(currentTokenIndex + 1);
    }

    private Token except() {
        throw new RuntimeException("Out of tokens");
    }
}