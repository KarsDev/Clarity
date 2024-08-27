package me.kuwg.clarity.token;

import java.util.Objects;

public final class Token {
    private final TokenType type;
    private final String value;
    private final int line;

    public Token(final TokenType type, final String value, final int line) {
        this.type = type;
        this.value = type == TokenType.NEWLINE ? "\\n" : value;
        this.line = line;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", value='" + value + '\'' +
                ", line=" + line +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Token token = (Token) o;
        return line == token.line && type == token.type && value.equals(token.getValue());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(type);
        result = 31 * result + Objects.hashCode(value);
        result = 31 * result + line;
        return result;
    }

    public boolean is(final TokenType type, final String value) {
        return this.type.equals(type) && this.value.equals(value);
    }
}
