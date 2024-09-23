package me.kuwg.clarity.token;

import java.util.Objects;

/**
 * Represents an immutable token in the source code, containing its type, value, and line number.
 * A token is the smallest unit of meaning, such as a keyword, identifier, or symbol,
 * extracted from the source during lexical analysis.
 */
public final class Token {
    private final TokenType type;
    private final String value;
    private final int line;

    /**
     * Constructs a new {@code Token} with the specified type, value, and line number.
     *
     * @param type  the {@link TokenType} of the token
     * @param value the value of the token (adjusted for newline tokens to be represented as "\\n")
     * @param line  the line number where the token occurs in the source
     */
    public Token(final TokenType type, final String value, final int line) {
        this.type = type;
        this.value = type == TokenType.NEWLINE ? "\\n" : value;
        this.line = line;
    }

    /**
     * Returns the type of the token.
     *
     * @return the {@link TokenType} of the token
     */
    public TokenType getType() {
        return type;
    }

    /**
     * Returns the value of the token.
     *
     * @return the token's value as a {@code String}
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the line number where the token is located in the source.
     *
     * @return the line number of the token
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns a string representation of the token, including its type, value, and line number.
     *
     * @return a string representing the token in the format: "Token{type=TYPE, value='VALUE', line=LINE}"
     */
    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", value='" + value + '\'' +
                ", line=" + line +
                '}';
    }

    /**
     * Compares this token to the specified object for equality.
     * Two tokens are considered equal if they have the same type, value, and line number.
     *
     * @param o the object to compare to
     * @return {@code true} if the tokens are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Token token = (Token) o;
        return line == token.line && type == token.type && value.equals(token.getValue());
    }

    /**
     * Returns the hash code for this token.
     * The hash code is calculated based on the token's type, value, and line number.
     *
     * @return the hash code of this token
     */
    @Override
    public int hashCode() {
        int result = Objects.hashCode(type);
        result = 31 * result + Objects.hashCode(value);
        result = 31 * result + line;
        return result;
    }

    /**
     * Checks if the token matches the given type and value.
     *
     * @param type  the {@link TokenType} to check
     * @param value the value to check
     * @return {@code true} if the token matches the specified type and value, {@code false} otherwise
     */
    public boolean is(final TokenType type, final String value) {
        return this.type.equals(type) && this.value.equals(value);
    }
}