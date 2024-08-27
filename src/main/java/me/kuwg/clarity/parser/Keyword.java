package me.kuwg.clarity.parser;

import me.kuwg.clarity.token.Token;

public enum Keyword {

    CLASS, VAR, CONSTRUCTOR, LOCAL, FN, NATIVE, IF, ELSE, RETURN, NEW, VOID, INCLUDE
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
            default:
                throw new UnsupportedOperationException("Unsupported keyword: " + token.getValue() + " at line " + token.getLine());
        }
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}