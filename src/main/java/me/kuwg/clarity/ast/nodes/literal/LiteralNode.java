package me.kuwg.clarity.ast.nodes.literal;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.ast.stream.ASTInputStream;
import me.kuwg.clarity.compiler.ast.stream.ASTOutputStream;

import java.io.IOException;

public class LiteralNode extends ASTNode {
    private String value;

    public LiteralNode(final String value) {
        this.value = parseEscapes(value == null ? null : value.length() > 2 ? value.substring(1, value.length() - 1) : "");
    }

    public LiteralNode() {
        super();
    }

    public final String getValue() {
        return value;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Literal:\n");
        sb.append(indent).append("  Value: ").append(value).append("\n");
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(value);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.value = in.readString();
    }

    @Override
    public String toString() {
        return "LiteralNode{" +
                "value='" + value + '\'' +
                '}';
    }

    // Method to parse escape sequences in the string
    private String parseEscapes(String input) {
        if (input == null) return null;

        StringBuilder result = new StringBuilder();
        int length = input.length();
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            if (c == '\\' && i + 1 < length) {
                char nextChar = input.charAt(i + 1);
                switch (nextChar) {
                    case 'n': result.append('\n'); i++; break;
                    case 'r': result.append('\r'); i++; break;
                    case 't': result.append('\t'); i++; break;
                    case 'b': result.append('\b'); i++; break;
                    case 'f': result.append('\f'); i++; break;
                    case '\\': result.append('\\'); i++; break;
                    case '"': result.append('"'); i++; break;
                    case '\'': result.append('\''); i++; break;
                    case 's': result.append(' '); i++; break;
                    case 'u':
                        if (i + 5 < length) {
                            try {
                                result.append((char) Integer.parseInt(input.substring(i + 2, i + 6), 16));
                                i += 5;
                            } catch (NumberFormatException e) {
                                result.append("\\u");
                            }
                        }
                        break;
                    default: result.append(c); break;
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
