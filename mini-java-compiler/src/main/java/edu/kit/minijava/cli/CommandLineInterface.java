package edu.kit.minijava.cli;

import org.apache.commons.cli.*;

public class CommandLineInterface {

    public static final String NAME = "mini-java-compiler";

    // COMMANDS
    public static final String ECHO_CMD = "echo";
    public static final String ECHO_DESC = "print input";

    public static final String LEXTEST_CMD = "lextest";
    public static final String LEXTEST_DESC = "run lexer";

    public static final String PARSERTEST_CMD = "parsetest";
    public static final String PARSETEST_DESC = "run parser";

    public static final String PRINT_AST_CMD = "printAst"; // TODO: '-' is an invalid character for apache cli,
                                                           // therefore we cannot use 'print-ast'
    public static final String PRINT_AST_DESC = "print abstract syntax tree";

    public static final String COMPILE_FIRM_CMD = "transform";
    public static final String COMPILE_FIRM_ALT_CMD = "compileFirm"; // TODO: same problem as in 'print-ast',
                                                                 // 'compile-firm' is not permitted
    public static final String COMPILE_FIRM_DESC = "compile using firm";

    public static final String COMPILE_CMD = "compile";
    public static final String COMPILE_DESC = "compile input file";

    public static void main(String[] arguments) {

        final Command command;
        String fileArgument = "";

        Options options = initalizeOptions();

        if (arguments.length == 1) {
            // Directly compile the file
            fileArgument = arguments[0];
            command = new CompileCommand();
        }

        else if (arguments.length != 2) {
            command = new CompileCommand();
            CommandLineInterface.printErrorAndExit(options);
        }
        else {

            fileArgument = arguments[1];

            switch (arguments[0]) {
                case "--echo":
                    command = new EchoCommand();
                    break;
                case "--lextest":
                    command = new LextestCommand();
                    break;
                case "--parsetest":
                    command = new ParserCommand(false);
                    break;
                case "--print-ast":
                    command = new ParserCommand(true);
                    break;
                case "--check":
                    command = new ValidateCommand();
                    break;
                case "--transform":
                case "--compile-firm":
                    command = new TransformerCommand();
                    break;
                case "--compile":
                    command = new CompileCommand();
                    break;
                default:
                    CommandLineInterface.printErrorAndExit(options);
                    return;
            }
        }

        int status = 0;
        try {
            status = command.execute(fileArgument);
        }
        catch (Throwable exception) {
            String message = exception.getLocalizedMessage();

            exception.printStackTrace();
            if (message == null || message.isEmpty()) {
                System.err.println("error: something went terribly wrong!");
                System.err.println();
                exception.printStackTrace(System.err);
            }
            else {
                System.err.println("error: " + message);
            }

            status = 255;
        }
        finally {
            System.exit(status);
        }
    }

    private static void printErrorAndExit(Options options) {
        System.err.println("error, invalid command and/or number of arguments!");
        // System.err.println("Usage: --echo <path>");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(NAME, options);

        System.exit(1);
    }

    private static Options initalizeOptions() {
        Options options = new Options();
        options.addOption(ECHO_CMD, true, ECHO_DESC);
        options.addOption(LEXTEST_CMD, true, LEXTEST_DESC);
        options.addOption(PARSERTEST_CMD, true, PARSETEST_DESC);
        options.addOption(PRINT_AST_CMD, true, PRINT_AST_DESC);
        options.addOption(COMPILE_FIRM_CMD,COMPILE_FIRM_ALT_CMD, true, COMPILE_FIRM_DESC);
        options.addOption(COMPILE_CMD, true, COMPILE_DESC);

        return options;
    }
}
