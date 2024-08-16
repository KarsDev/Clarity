package me.kuwg.clarity.parser;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.token.Token;
import me.kuwg.clarity.token.TokenType;

import java.util.*;

import static me.kuwg.clarity.token.TokenType.*;

public final class Parser {
    private final List<Token> tokens;
    private int currentTokenIndex = 0;


    public Parser(final List<Token> tokens) {
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
        }

        throw new UnsupportedOperationException("Unsupported keyword: " + keyword);
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