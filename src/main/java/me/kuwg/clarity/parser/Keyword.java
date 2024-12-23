package me.kuwg.clarity.parser;

import me.kuwg.clarity.token.Token;

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

    ;

    public static Keyword keyword(final Token token) {
        switch (token.getValue()) {
            case "class": return CLASS;
            case "var": return VAR;
            case "constructor": return CONSTRUCTOR;
            case "local": return LOCAL;
            case "fn": return FN;
            case "native": return NATIVE;
            case "if": return IF;
            case "else": return ELSE;
            case "return": return RETURN;
            case "new": return NEW;
            case "void": return VOID;
            case "include": return INCLUDE;
            case "static": return STATIC;
            case "const": return CONST;
            case "compiled": return COMPILED;
            case "null": return NULL;
            case "for": return FOR;
            case "while": return WHILE;
            case "select": return SELECT;
            case "when": return WHEN;
            case "default": return DEFAULT;
            case "break": return BREAK;
            case "continue": return CONTINUE;
            case "float": return FLOAT;
            case "int": return INT;
            case "inherits": return INHERITS;
            case "assert": return ASSERT;
            case "is": return IS;
            case "arr": return ARR;
            case "str": return STR;
            case "enum": return ENUM;
            case "bool": return BOOL;
            case "async": return ASYNC;
            case "raise": return RAISE;
            case "try": return TRY;
            case "except": return EXCEPT;
            case "lambda": return LAMBDA;

            default: throw new UnsupportedOperationException("Unsupported keyword: " + token.getValue() + " at line " + token.getLine());
        }
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}