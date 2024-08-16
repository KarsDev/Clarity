package me.kuwg.clarity;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.parser.ASTParser;
import me.kuwg.clarity.token.Token;
import me.kuwg.clarity.token.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        final File file = new File("test.clr");
        final String content = new String(Files.readAllBytes(file.toPath()));

        final List<Token> tokens = Tokenizer.tokenize(content);
        final ASTParser parser = new ASTParser(tokens);

        final AST ast = parser.parse();
        System.out.println(ast);
    }
}