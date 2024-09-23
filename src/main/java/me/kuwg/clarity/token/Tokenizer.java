package me.kuwg.clarity.token;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Tokenizer class responsible for parsing a source string into a list of tokens.
 * The class uses a predefined set of token types to identify valid tokens.
 */
public class Tokenizer {

    /**
     * Tokenizes the given source string into a list of {@link Token} objects.
     * This method processes the input string by matching it against token patterns
     * and handles specific token types such as numbers, newlines, and comments.
     *
     * @param src the source string to be tokenized
     * @return a list of {@link Token} objects extracted from the source string
     * @throws IllegalArgumentException if an unexpected character is encountered in the input
     */
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

                    // Process numbers to handle underscores in numeric literals
                    final String tokenValue = type.equals(TokenType.NUMBER) ? String.valueOf(processNumber(rawValue.replace("_", ""))) : rawValue;

                    if (type == TokenType.NEWLINE) {
                        line++; // Increment line number for newline tokens
                    } else if (type == TokenType.COMMENT) {
                        // Adjust line count for multiline comments
                        int lines = 1;
                        int pos = 0;
                        while ((pos = tokenValue.indexOf("\n", pos) + 1) != 0) {
                            lines++;
                        }
                        line += lines;
                    } else if (type != TokenType.WHITESPACE) {
                        // Add non-whitespace tokens to the list
                        tokens.add(new Token(type, tokenValue, line));
                    }

                    remainingSrc = remainingSrc.substring(rawValue.length());
                    matched = true;
                    break;
                }
            }

            // Throw exception if no token pattern matches the current input
            if (!matched) {
                throw new IllegalArgumentException("Unexpected character in input at line " + line + ": " + remainingSrc);
            }
        }
        return tokens;
    }

    /**
     * Processes a numeric literal string and returns its corresponding {@link Number} object.
     * This method handles different numeric formats such as hexadecimal, binary, octal,
     * floating-point, and standard integer values.
     *
     * @param number the numeric string to be processed
     * @return a {@link Number} representing the value of the processed numeric string
     * @throws NumberFormatException if the number format is invalid
     */
    public static Number processNumber(final String number) {
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
        if (cleanedNumber.toLowerCase().endsWith("f")) {
            return Double.parseDouble(cleanedNumber.substring(0, cleanedNumber.length() - 1));
        }
        // Otherwise, treat as integer
        return Integer.parseInt(cleanedNumber);
    }
}