package me.kuwg.clarity.token;

import java.util.regex.Pattern;

public enum TokenType {
    COMMENT("^//.*"),
    NEWLINE("\r\n|\n|\r"),
    WHITESPACE("\\s+"),
    KEYWORD("\\b(TODO)\\b"), // TODO
    STRING("^(['\"])(.*?)(?<!\\\\)\\1$"),
    OPERATOR("\\+|\\-|\\*|\\/|%|==|!=|\\+=|\\-=|\\*=|\\/=|%="),
    DIVIDER("[\\{\\}\\[\\]\\(\\),]"),
    NUMBER("\\b\\d[\\d_]*\\d\\b|\\b\\d\\b"),
    VARIABLE("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");
    ;

    public static final TokenType[] VALUES = values();

    private final Pattern pattern;

    TokenType(final String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public final Pattern getPattern() {
        return pattern;
    }
}
