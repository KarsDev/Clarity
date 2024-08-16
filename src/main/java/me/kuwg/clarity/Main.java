package me.kuwg.clarity;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.compiler.ASTLoader;
import me.kuwg.clarity.compiler.ASTSaver;
import me.kuwg.clarity.interpreter.Interpreter;
import me.kuwg.clarity.parser.ASTParser;
import me.kuwg.clarity.token.Token;
import me.kuwg.clarity.token.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Main {
    public static void main(final String[] args) throws IOException {
        if (args.length == 0) {
            printUsage();
            return;
        }

        if (args.length == 1) {
            printInputFileRequired();
            return;
        }

        File file = new File(args[1]);

        if (!file.exists()) {
            printFileNotFound(file);
            return;
        }

        switch (args[0]) {
            case "run":
            case "interpret":
                runOrInterpretFile(file);
                break;
            case "compile":
                compileFile(args, file);
                break;
            default:
                printUsage();
                break;
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  run <source.clr>      - Interpret and run the source file");
        System.out.println("  compile <source.clr> [output.cclr] - Compile the source file to AST format");
    }

    private static void printInputFileRequired() {
        System.out.println("Error: Input file required.");
    }

    private static void printFileNotFound(File file) {
        System.err.println("File not found: " + file);
    }

    private static void runOrInterpretFile(File file) throws IOException {
        AST ast = loadOrParseAST(file);
        Interpreter interpreter = new Interpreter(ast);
        System.exit(interpreter.interpret());
    }

    private static AST loadOrParseAST(File file) throws IOException {
        String fileName = file.getName();
        int i = fileName.lastIndexOf('.');

        if (i > 0 && fileName.substring(i + 1).equals("cclr")) {
            return loadASTFromFile(file);
        } else {
            return parseASTFromSource(file);
        }
    }

    private static AST loadASTFromFile(File file) {
        ASTLoader loader = new ASTLoader(file);
        try {
            return loader.load();
        } catch (IOException e) {
            System.err.println("Failed to load the AST:");
            throw new RuntimeException(e);
        }
    }

    private static AST parseASTFromSource(File file) throws IOException {
        List<Token> tokens = Tokenizer.tokenize(new String(Files.readAllBytes(file.toPath())));
        ASTParser parser = new ASTParser(tokens);
        return parser.parse();
    }

    private static void compileFile(String[] args, File file) throws IOException {
        String output = determineOutputFileName(args, file);
        if (output == null) return;

        AST ast = parseASTFromSource(file);
        saveASTToFile(ast, output);
    }

    private static String determineOutputFileName(String[] args, File file) {
        if (args.length < 3) {
            return createOutputFileName(file);
        } else if (args[1].equals(args[2])) {
            System.err.println("Error: Output file cannot be the same as input file.");
            return null;
        } else {
            return args[2];
        }
    }

    private static String createOutputFileName(File file) {
        String fileName = file.getName();
        int i = fileName.lastIndexOf('.');
        if (i > 0 && fileName.substring(i + 1).equals("clr")) {
            return fileName.substring(0, fileName.length() - 4) + ".cclr";
        } else {
            return fileName + ".cclr";
        }
    }

    private static void saveASTToFile(AST ast, String output) {
        ASTSaver saver = new ASTSaver(ast);
        try {
            saver.save(new File(output));
        } catch (IOException e) {
            System.err.println("Failed to save the AST: ");
            throw new RuntimeException(e);
        }
    }
}
