package me.kuwg.clarity.token;

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
}
