package me.kuwg.clarity.token;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class Tokenizer {

    public static List<Token> tokenize(final String src) {
        final List<Token> tokens = new ArrayList<>();
        String remainingSrc = src;
        int line = 1;

        while (!remainingSrc.isEmpty()) {
            boolean matched = false;
            for (final TokenType type : TokenType.VALUES) {
                final Matcher matcher = type.getPattern().matcher(remainingSrc);
                if (matcher.lookingAt()) {

                    final String tokenValue = type.equals(TokenType.NUMBER) ? processNumber(matcher.group()).toString() : matcher.group();

                    if (type == TokenType.NEWLINE) line++;
                    else if (type != TokenType.COMMENT && type != TokenType.WHITESPACE) tokens.add(new Token(type, tokenValue, line));


                    remainingSrc = remainingSrc.substring(tokenValue.length());
                    matched = true;
                    break;
                }
            }

            if (!matched) throw new IllegalArgumentException("Unexpected character in input at line " + line + ": " + remainingSrc);
        }
        return tokens;
    }

    private static Number processNumber(final String number) {
        String sanitizedNumber = number.replace("_", "");

        if (sanitizedNumber.contains("e") || sanitizedNumber.contains("E")) {
            final String[] parts = sanitizedNumber.split("[eE]");
            final double result = Double.parseDouble(sanitizedNumber);
            if (!parts[0].contains(".") && result == (int) result) return (int) result;
            return result;
        }
        if (sanitizedNumber.contains(".")) return Double.parseDouble(sanitizedNumber);
        return Integer.parseInt(sanitizedNumber);
    }
}
