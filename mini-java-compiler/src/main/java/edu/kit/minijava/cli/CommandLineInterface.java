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

    public static final String PRINT_AST_CMD = "print-ast";
    public static final String PRINT_AST_DESC = "print abstract syntax tree";

    public static final String COMPILE_FIRM_CMD = "transform";
    public static final String COMPILE_FIRM_ALT_CMD = "compile-firm";
    public static final String COMPILE_FIRM_DESC = "compile using firm";

    public static final String COMPILE_CMD = "compile";
    public static final String COMPILE_DESC = "compile input file";

    public static void main(String[] arguments) {

        final Command command;
        String fileArgument = "";

        Options options = initalizeOptions();
        CommandLine cmdLine = generateCommandLine(options, arguments);



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

            fileArgument = arguments[arguments.length - 1];

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
        Option echoOption = Option.builder().hasArg().longOpt(ECHO_CMD)
                .desc(ECHO_DESC).build();
        Option lexOption = Option.builder().hasArg().longOpt(LEXTEST_CMD).build();
        Option parserOption = Option.builder().hasArg().longOpt(PARSERTEST_CMD).build();
        Option printAstOption = Option.builder().hasArg().longOpt(PRINT_AST_CMD).build();
        Option compileFirmOption = Option.builder().hasArg().longOpt(COMPILE_FIRM_CMD).build();
        Option compileOption = Option.builder().hasArg().longOpt(COMPILE_CMD).build();

        Options options = new Options();
        options.addOption(echoOption);
        options.addOption(lexOption);
        options.addOption(parserOption);
        options.addOption(printAstOption);
        options.addOption(compileFirmOption);
        options.addOption(compileOption);


        return options;
    }

    private static CommandLine generateCommandLine(Options options, String[] args) {
        CommandLineParser cmdLineParser = new DefaultParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = cmdLineParser.parse(options, args);
        }
        catch (ParseException e) {
            e.printStackTrace();
            printErrorAndExit(options);
        }

        return cmdLine;
    }
}
