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

                    final String rawValue = matcher.group();

                    final String tokenValue = type.equals(TokenType.NUMBER) ? String.valueOf(processNumber(rawValue.replace("_", ""))) : rawValue;

                    if (type == TokenType.NEWLINE) {
                        line++;
                    } else if (type == TokenType.COMMENT) {
                        int lines = 1;
                        int pos = 0;
                        while ((pos = tokenValue.indexOf("\n", pos) + 1) != 0) {
                            lines++;
                        }
                        line += lines;
                    } else if (type != TokenType.WHITESPACE) {
                        tokens.add(new Token(type, tokenValue, line));
                    }

                    remainingSrc = remainingSrc.substring(rawValue.length());
                    matched = true;
                    break;
                }
            }

            if (!matched)
                throw new IllegalArgumentException("Unexpected character in input at line " + line + ": " + remainingSrc);
        }
        return tokens;
    }

    private static Number processNumber(final String number) {
        final String cleanedNumber = number.replaceAll("_", "");

        // Check for hexadecimal numbers
        if (cleanedNumber.startsWith("0x") || cleanedNumber.startsWith("0X")) {
            return Integer.decode(cleanedNumber);
        }
        // Check for binary numbers
        if (cleanedNumber.startsWith("0b") || cleanedNumber.startsWith("0B")) {
            return Integer.parseInt(cleanedNumber.substring(2), 2);
        }
        // Check for octal numbers
        if (cleanedNumber.startsWith("0") && cleanedNumber.length() > 1 && cleanedNumber.matches("0[0-7]+")) {
            return Integer.parseInt(cleanedNumber, 8);
        }
        // Check for floating-point numbers (includes scientific notation)
        if (cleanedNumber.matches("[+-]?\\d*\\.\\d+([eE][+-]?\\d+)?")) {
            return Double.parseDouble(cleanedNumber);
        }
        // Check for float literals (ending with 'f' or 'F')
        if (cleanedNumber.matches("[+-]?\\d*\\.\\d+[fF]")) {
            return Float.parseFloat(cleanedNumber.replaceAll("[fF]", ""));
        }
        // Otherwise, treat as integer
        return Integer.parseInt(cleanedNumber);
    }
}
