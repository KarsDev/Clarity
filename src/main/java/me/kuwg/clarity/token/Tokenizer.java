package me.kuwg.clarity.token;

import me.kuwg.clarity.register.Register;

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
        if (number.matches("0[xX][0-9a-fA-F_]+")) { // Hexadecimal
            return Integer.decode(number.replace("_", ""));
        } else if (number.matches("0[bB][01_]+")) { // Binary
            return Integer.parseInt(number.replace("_", "").substring(2), 2);
        } else if (number.matches("0[0-7_]+")) { // Octal
            return Integer.parseInt(number.replace("_", ""), 8);
        } else if (number.matches("\\d+([eE][+-]?\\d+)?")) { // Integer or scientific notation
            if (number.contains("e") || number.contains("E")) {
                return Double.parseDouble(number.replace("_", ""));
            } else {
                return Integer.parseInt(number.replace("_", ""));
            }
        } if (number.matches("\\d+[fFdD]")) { // Floating point
            return Float.parseFloat(number.replace("_", "").replaceAll("[fFdD]", ""));
        }  else {
            Register.throwException("Invalid number format: " + number);
            return null;
        }
    }
}
