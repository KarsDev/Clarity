package me.kuwg.clarity;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.compiler.ASTData;
import me.kuwg.clarity.compiler.ASTLoader;
import me.kuwg.clarity.compiler.ASTSaver;
import me.kuwg.clarity.compiler.cir.CIRCompiler;
import me.kuwg.clarity.installer.modules.ClarityModuleInstaller;
import me.kuwg.clarity.installer.sys.ClarityInstaller;
import me.kuwg.clarity.installer.sys.OS;
import me.kuwg.clarity.interpreter.Interpreter;
import me.kuwg.clarity.parser.ASTParser;
import me.kuwg.clarity.token.Token;
import me.kuwg.clarity.token.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.util.List;

public class Clarity {

    private static int EXIT_CODE = 0;

    public static final String USER_HOME = System.getProperty("user.home");

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
                    switch (args[0].toLowerCase()) {
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
                            return;
                        case "ast":
                            printASTInfo();
                            return;
                    }
                    printInputFileRequired();
                    return;
                }



                final File file = new File(args[1]);

                switch (args[0]) {
                    case "interpret":
                    case "test":
                        if (requireFile(file)) return;
                        System.out.println("Interpreting and running the file: " + file.getName());
                        runOrInterpretFile(file);
                        break;
                    case "compile":
                        if (requireFile(file)) return;
                        System.out.println("Compiling the file: " + file.getName());
                        compileFile(args, file);
                        break;
                    case "run":
                        if (requireFile(file)) return;
                        System.out.println("Running the compiled file: " + file.getName());
                        runCompiledFile(file);
                        break;
                    case "cpp":
                        if (requireFile(file)) return;
                        System.out.println("Compiling the file to cpp: " + file.getName());
                        compileToCPP(file);
                        break;
                    case "install":
                        if (args.length == 2) System.out.println("Installing module: " + args[1]);
                        else System.out.println("Installing modules...");
                        installModule(args);
                        break;
                    case "filesize":
                        System.out.println(file.getName() + "'s size: " + file.length() + " bytes");
                        break;
                    default:
                        printUsage();
                        break;
                }
            }
            boolean requireFile(final File file) {
                if (!file.exists()) {
                    printFileNotFound(file);
                    return true;
                }
                return false;
            }
        }.start();

        await(() -> System.exit(EXIT_CODE));
    }

    private static void installClarity() {
        try {
            final File destDir = new File(Clarity.USER_HOME + "\\Clarity");
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
        System.out.println("  test <source.clr> - Compile the source file to clarity IR");
        System.out.println("  test <source.clr> [output.cclr] - Interpret, run, and then compile the source file");
    }

    private static void printInputFileRequired() {
        System.err.println("Error: Input file required.");
    }

    private static void printFileNotFound(File file) {
        System.err.println("Error: File not found: " + file.getAbsolutePath());
    }

    private static void installModule(final String[] args) {
        final String[] modules = new String[args.length-1];
        System.arraycopy(args, 1, modules, 0, args.length - 1);
        ClarityModuleInstaller.installModules(modules);
    }

    private static void compileToCPP(File file) throws IOException {
        AST ast = loadOrParseAST(file);
        CIRCompiler compiler = new CIRCompiler(ast, new File(file.getName().substring(file.getName().lastIndexOf(".") + 1) + ".cpp"));
        compiler.compile();
    }

    private static void runOrInterpretFile(File file) throws IOException {
        AST ast = loadOrParseAST(file);
        Interpreter interpreter = new Interpreter(ast);
        EXIT_CODE = interpreter.interpret();
    }

    private static void runCompiledFile(File file) {
        AST ast = loadASTFromFile(file);

        Interpreter interpreter = new Interpreter(ast);
        EXIT_CODE = interpreter.interpret();
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

    private static void printASTInfo() {
        System.out.println("AST Info:");
        System.out.println("  Nodes: " + ASTData.NODE_TO_ID.size());
        System.out.println("  Compression: GZIP");
        System.out.println("  Format:");
        System.out.println("   Default: CLR");
        System.out.println("   Compressed: CCLR");
    }

    @SuppressWarnings("LoopConditionNotUpdatedInsideLoop")
    private static void await(final Runnable runnable) {
        final ThreadGroup group = Thread.currentThread().getThreadGroup();
        while (group != null) {
            final Thread[] threads = new Thread[group.activeCount()];
            group.enumerate(threads);

            boolean hasActiveNonDaemonThread = false;
            for (final Thread thread : threads) {
                if (thread != null && !thread.isDaemon() && thread != Thread.currentThread()) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    hasActiveNonDaemonThread = true;
                }
            }

            if (!hasActiveNonDaemonThread) {
                break;
            }
        }

        runnable.run();
    }
}