package me.kuwg.clarity.parser;

import me.kuwg.clarity.token.Token;

import java.util.HashMap;
import java.util.Map;

public enum Keyword {
    CLASS,
    VAR,
    CONSTRUCTOR,
    LOCAL,
    FN,
    NATIVE,
    IF,
    ELSE,
    RETURN,
    NEW,
    VOID,
    INCLUDE,
    STATIC,
    CONST,
    COMPILED,
    NULL,
    FOR,
    WHILE,
    SELECT,
    WHEN,
    DEFAULT,
    BREAK,
    CONTINUE,
    FLOAT,
    INT,
    INHERITS,
    ASSERT,
    IS,
    ARR,
    STR,
    ENUM,
    BOOL,
    ASYNC,
    RAISE,
    TRY,
    EXCEPT,
    LAMBDA,
    DELETE,

    ;

    private static final Map<String, Keyword> KEYWORD_MAP = new HashMap<>();

    static {
        for (final Keyword keyword : Keyword.values()) {
            KEYWORD_MAP.put(keyword.toString(), keyword);
        }
    }

    public static Keyword keyword(final Token token) {
        final Keyword keyword = KEYWORD_MAP.get(token.getValue().toLowerCase());
        if (keyword == null) {
            throw new UnsupportedOperationException("Unsupported keyword: " + token.getValue() + " at line " + token.getLine());
        }
        return keyword;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}