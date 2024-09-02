package me.kuwg.clarity;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.compiler.ASTLoader;
import me.kuwg.clarity.compiler.ASTSaver;
import me.kuwg.clarity.installer.ClarityInstaller;
import me.kuwg.clarity.installer.OS;
import me.kuwg.clarity.installer.WindowsClarityInstaller;
import me.kuwg.clarity.interpreter.Interpreter;
import me.kuwg.clarity.parser.ASTParser;
import me.kuwg.clarity.token.Token;
import me.kuwg.clarity.token.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.util.List;

public class Clarity {
    public static void main(final String[] args) {
        new Thread("Clarity Main Thread") {
            @Override
            public void run() {
                try {
                    exec();
                } catch (final IOException e) {
                    System.err.println("IO Error: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            void exec() throws IOException {

                if (args.length == 0) {
                    printUsage();
                    return;
                }

                if (args.length == 1) {
                    switch (args[0]) {
                        case "install":
                            System.out.println("Installing...");
                            installClarity();
                            return;
                        case "os":
                            System.out.println("Current OS: " + OS.CURRENT_OPERATING_SYSTEM_NAME);
                            System.out.println("Detected OS type: " + OS.CURRENT_OS);
                            System.out.println();
                            return;
                        case "size":
                            final File jarFile = new File(ManagementFactory.getRuntimeMXBean().getClassPath().split(File.pathSeparator)[0]);
                            if (jarFile.exists()) {
                                long jarFileSize = jarFile.length();
                                System.out.println("Running JAR file size: " + jarFileSize + " bytes");
                            } else {
                                System.out.println("Running JAR file not found.");
                            }
                            return;
                        case "help":
                        case "-help":
                            printUsage();
                    }
                    printInputFileRequired();
                    return;
                }



                final File file = new File(args[1]);

                if (!file.exists()) {
                    printFileNotFound(file);
                    return;
                }

                switch (args[0]) {
                    case "interpret":
                        System.out.println("Interpreting and running the file: " + file.getName());
                        runOrInterpretFile(file);
                        break;
                    case "compile":
                        System.out.println("Compiling the file: " + file.getName());
                        compileFile(args, file);
                        break;
                    case "test":
                        System.out.println("Interpreting and running the file: " + file.getName());
                        runOrInterpretFile(file);
                        System.out.println("Compiling the file: " + file.getName());
                        compileFile(args, file);
                        break;
                    case "run":
                        System.out.println("Running the compiled file: " + file.getName());
                        runCompiledFile(file);
                        break;
                    default:
                        printUsage();
                        break;
                }
            }
        }.start();
    }

    private static void installClarity() {
        try {
            final File destDir = new File(System.getProperty("user.home") + "\\Clarity");
            if (!destDir.exists()) destDir.mkdirs();
            System.out.println("Clarity will be installed successfully at: " + destDir.getAbsolutePath());
            ClarityInstaller.install(destDir.getAbsolutePath());
        } catch (final Exception e) {
            System.err.println("Installation failed: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  interpret <source.clr>      - Interpret and run the source file");
        System.out.println("  run <compiled.cclr>         - Interpret the compiled source file");
        System.out.println("  compile <source.clr> [output.cclr] - Compile the source file to AST format");
        System.out.println("  test <source.clr> [output.cclr] - Interpret, run, and then compile the source file");
    }

    private static void printInputFileRequired() {
        System.err.println("Error: Input file required.");
    }

    private static void printFileNotFound(File file) {
        System.err.println("Error: File not found: " + file.getAbsolutePath());
    }

    private static void runOrInterpretFile(File file) throws IOException {
        AST ast = loadOrParseAST(file);
        Interpreter interpreter = new Interpreter(ast);
        int exitCode = interpreter.interpret();
        System.exit(exitCode);
    }

    private static void runCompiledFile(File file) {
        AST ast = loadASTFromFile(file);

        Interpreter interpreter = new Interpreter(ast);
        int exitCode = interpreter.interpret();
        System.exit(exitCode);
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
            System.err.println("Error: Failed to load the AST from file: " + file.getAbsolutePath());
            throw new RuntimeException(e);
        }
    }

    private static AST parseASTFromSource(File file) throws IOException {
        List<Token> tokens = Tokenizer.tokenize(new String(Files.readAllBytes(file.toPath())));
        ASTParser parser = new ASTParser(file.getAbsolutePath(), file.getName(), tokens);
        return parser.parse();
    }

    private static void compileFile(String[] args, File file) throws IOException {
        String output = determineOutputFileName(args, file);
        if (output == null) return;

        AST ast = parseASTFromSource(file);
        System.out.println("Saving the compiled AST to: " + output);
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
            System.err.println("Error: Failed to save the AST to file: " + output);
            throw new RuntimeException(e);
        }
    }
}