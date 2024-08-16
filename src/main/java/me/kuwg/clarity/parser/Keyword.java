package me.kuwg.clarity.parser;

import me.kuwg.clarity.token.Token;

public enum Keyword { // <></>ODO
    ;

    public static Keyword keyword(final Token token) {
        switch (token.getValue().toLowerCase()) {
            default: throw new UnsupportedOperationException("Unsupported keyword: " + token.getValue() + " at line " + token.getLine());
        }
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}