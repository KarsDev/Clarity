package me.kuwg.clarity.token;

import java.util.regex.Pattern;

public enum TokenType {
    COMMENT("//[^\r\n]*|/#.*?#/"), // Matches single-line comments (//...) and block comments (/#...#/).
    NEWLINE("\r\n|\n|\r"), // Matches newlines
    WHITESPACE("\\s+"), // Matches any whitespace characters
    KEYWORD("\\b(class|var|constructor|local|fn|native|if|else|return|new|void|include|static|const|compiled|null|for|while|select|when|default|break|continue|float|int|inherits|assert|is|arr|str)\\b"), // All keywords
    BOOLEAN("\\b(true|false)\\b"), // Matches booleans
    STRING("\"(\\\\.|[^\"\\\\])*\"|'(\\\\.|[^'\\\\])*'"), // Matches anything between " and " or ' and ', including escaped characters
    OPERATOR("\\^\\^|\\||<<|>>|\\+\\+|--|[+\\-*/%=<>!]=?|==|\\.\\.\\.|\\.|\\|\\||&&|\\?|:|\\^|&"), // Added << and >> operators
    NUMBER("\\b(?:(?:0[xX][0-9a-fA-F]+(?:_[0-9a-fA-F]+)*)|(?:0[bB][01]+(?:_[01]+)*)|(?:0[0-7]+(?:_[0-7]+)*)|(?:\\d+(?:_\\d+)*))(?:\\.\\d+(?:_\\d+)*([eE][+-]?\\d+(?:_\\d+)*)?|[eE][+-]?\\d+(?:_\\d+)*|[fF])?\\b"),
    DIVIDER("[\\(\\)\\[\\]\\{\\},]"), // Matches dividers like ( ) [ ] { }
    VARIABLE("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b"); // Matches valid variable names

    public static final TokenType[] VALUES = values();

    private final Pattern pattern;

    TokenType(final String regex) {
        this.pattern = Pattern.compile(regex, Pattern.DOTALL); // Add Pattern.DOTALL to handle newlines in block comments
    }

    public final Pattern getPattern() {
        return pattern;
    }
}
