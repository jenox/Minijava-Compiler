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

    public static final String CHECK_CMD = "check";
    public static final String CHECK_DESC = "run semantic analysis";

    public static final String COMPILE_FIRM_CMD = "transformUsingLibfirmBackend";
    public static final String COMPILE_FIRM_ALT_CMD = "compile-firm";
    public static final String COMPILE_FIRM_DESC = "compile using firm";

    public static final String COMPILE_CMD = "compile";
    public static final String COMPILE_DESC = "compile input file";

    // OPTIONs
    public static final String NO_OPTIMIZATION = "no-optimization";
    public static final String DUMP_GRAPHS = "dump-graphs";

    private static CommandLine cmdLine;

    public static void main(String[] arguments) {

        final Command command;
        String fileArgument = "";

        Options options = initalizeOptions();
        cmdLine = generateCommandLine(options, arguments);

        if (arguments.length == 1) {
            // Directly compile the file
            fileArgument = arguments[0];
            command = new CompileCommand();
        }
        else {

            if (arguments.length > 0) {
                fileArgument = arguments[arguments.length - 1];
            }

            if (cmdLine.hasOption(ECHO_CMD)) {
                fileArgument = cmdLine.getOptionValue(ECHO_CMD);
                command = new EchoCommand();
            }
            else if (cmdLine.hasOption(LEXTEST_CMD)) {
                fileArgument = cmdLine.getOptionValue(LEXTEST_CMD);
                command = new LextestCommand();
            }
            else if (cmdLine.hasOption(PARSERTEST_CMD)) {
                fileArgument = cmdLine.getOptionValue(PARSERTEST_CMD);
                command = new ParserCommand(false);
            }
            else if (cmdLine.hasOption(PRINT_AST_CMD)) {
                fileArgument = cmdLine.getOptionValue(PRINT_AST_CMD);
                command = new ParserCommand(true);
            }
            else if (cmdLine.hasOption(CHECK_CMD)) {
                fileArgument = cmdLine.getOptionValue(CHECK_CMD);
                command = new ValidateCommand();
            }
            else if (cmdLine.hasOption(COMPILE_FIRM_CMD)) {
                fileArgument = cmdLine.getOptionValue(COMPILE_CMD);
                command = new TransformerCommand();
            }
            else if (cmdLine.hasOption(COMPILE_FIRM_ALT_CMD)) {
                fileArgument = cmdLine.getOptionValue(COMPILE_FIRM_ALT_CMD);
                command = new TransformerCommand();
            }
            else if (cmdLine.hasOption(COMPILE_CMD)) {
                fileArgument = cmdLine.getOptionValue(COMPILE_CMD);
                command = new CompileCommand();
            }
            else {
                command = null; // has to be initalized
                printErrorAndExit(options);
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
        Option echoOption = Option.builder().hasArg().longOpt(ECHO_CMD).desc(ECHO_DESC).build();
        Option lexOption = Option.builder().hasArg().longOpt(LEXTEST_CMD).desc(LEXTEST_DESC).build();
        Option parserOption = Option.builder().hasArg().longOpt(PARSERTEST_CMD).desc(PARSETEST_DESC).build();
        Option printAstOption = Option.builder().hasArg().longOpt(PRINT_AST_CMD).desc(PRINT_AST_DESC).build();
        Option compileFirmOption = Option.builder().hasArg().longOpt(COMPILE_FIRM_CMD).desc(COMPILE_FIRM_DESC).build();
        Option compileFirmAltOption = Option.builder().hasArg().longOpt(COMPILE_FIRM_ALT_CMD).desc(COMPILE_FIRM_DESC)
                        .build();
        Option compileOption = Option.builder().hasArg().longOpt(COMPILE_CMD).desc(COMPILE_DESC).build();
        Option checkOption = Option.builder().hasArg().longOpt(CHECK_CMD).desc(CHECK_DESC).build();

        Option noOptimizationOption = Option.builder().longOpt(NO_OPTIMIZATION).build();
        Option dumpGraphsOption = Option.builder().longOpt(DUMP_GRAPHS).build();

        Options options = new Options();
        options.addOption(echoOption);
        options.addOption(lexOption);
        options.addOption(parserOption);
        options.addOption(printAstOption);
        options.addOption(compileFirmOption);
        options.addOption(compileOption);
        options.addOption(checkOption);
        options.addOption(compileFirmAltOption);

        options.addOption(noOptimizationOption);
        options.addOption(dumpGraphsOption);

        return options;
    }

    private static CommandLine generateCommandLine(Options options, String[] args) {
        CommandLineParser cmdLineParser = new DefaultParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = cmdLineParser.parse(options, args);
        }
        catch (ParseException e) {
            printErrorAndExit(options);
        }

        return cmdLine;
    }

    public static boolean areOptimizationsActivated() {
        return !cmdLine.hasOption(NO_OPTIMIZATION); // if option is set return false
    }

    public static boolean shouldGraphsBeDumped() {
        return cmdLine.hasOption(DUMP_GRAPHS);
    }

}
