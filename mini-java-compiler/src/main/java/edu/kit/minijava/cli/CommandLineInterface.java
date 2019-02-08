package edu.kit.minijava.cli;

import org.apache.commons.cli.*;

import java.util.*;

public class CommandLineInterface {

    private static final String NAME = "mini-java-compiler";

    // Main compiler modes

    private static final String ECHO_CMD = "echo";
    private static final String ECHO_DESC = "Print input file to stdout.";

    private static final String LEXTEST_CMD = "lextest";
    private static final String LEXTEST_DESC = "Run lexer and output tokens.";

    private static final String PARSERTEST_CMD = "parsetest";
    private static final String PARSETEST_DESC = "Run parser and indicate syntactic validity via exit code.";

    private static final String PRINT_AST_CMD = "print-ast";
    private static final String PRINT_AST_DESC = "Generate AST and output pretty-printed program.";

    private static final String CHECK_CMD = "check";
    private static final String CHECK_DESC = "Check semantic validity of the input program.";

    private static final String COMPILE_FIRM_CMD = "compile-firm";
    private static final String COMPILE_FIRM_DESC = "Compile the input program using the Firm backend.";

    private static final String COMPILE_CMD = "compile";
    private static final String COMPILE_DESC = "Compile the input program.";

    private static final String HELP_CMD = "help";
    private static final String HELP_DESC = "Print this message.";

    // Switches

    private static final String NO_OPTIMIZATION_SWITCH = "no-optimization";
    private static final String NO_OPTIMIZATION_SWITCH_DESC = "Do not optimize the generated code.";

    private static final String DUMP_SWITCH = "dump";
    private static final String DUMP_SWITCH_DESC = "Dump intermediate files such as function graphs.";

    private static final String VERBOSE_SWITCH = "verbose";
    private static final String VERBOSE_SWITCH_DESC = "Be verbose during compilation.";


    private static CommandLine cmdLine;

    public static void main(String[] arguments) {

        // Parse input arguments
        Options options = initalizeOptions();
        cmdLine = generateCommandLine(options, arguments);

        // Help does not require an input file

        if (cmdLine.hasOption(HELP_CMD)) {
            printHelpMessage(options);
            System.exit(0);
            return;
        }

        // All other commands require a single input file
        List<String> inputFiles = cmdLine.getArgList();

        if (inputFiles == null || inputFiles.size() != 1) {
            printError("Please provide a single input file to compile");

            System.out.println();
            printHelpMessage(options);

            System.exit(1);
            return;
        }

        // After validating, we can find the path to the input file
        final String fileArgument = inputFiles.get(0);
        CompilerFlags flags = extractFlags();

        final Command command;

        if (cmdLine.hasOption(ECHO_CMD)) {
            command = new EchoCommand(flags);
        }
        else if (cmdLine.hasOption(LEXTEST_CMD)) {
            command = new LextestCommand(flags);
        }
        else if (cmdLine.hasOption(PARSERTEST_CMD)) {
            command = new ParserCommand(flags,false);
        }
        else if (cmdLine.hasOption(PRINT_AST_CMD)) {
            command = new ParserCommand(flags,true);
        }
        else if (cmdLine.hasOption(CHECK_CMD)) {
            command = new ValidateCommand(flags);
        }
        else if (cmdLine.hasOption(COMPILE_FIRM_CMD)) {
            command = new TransformerCommand(flags);
        }
        else if (cmdLine.hasOption(COMPILE_CMD)) {
            command = new CompileCommand(flags);
        }
        else {
            // If no stage is given, compile the input file with our own backend
            command = new CompileCommand(flags);
        }

        int status = 0;
        try {
            status = command.execute(fileArgument);
        }
        catch (Throwable exception) {
            String message = exception.getLocalizedMessage();

            exception.printStackTrace();
            if (message == null || message.isEmpty()) {
                System.err.println("error: Something went terribly wrong!");
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

    private static Options initalizeOptions() {

        Options options = new Options();
        OptionGroup compileStageGroup = new OptionGroup();

        Option echoOption = Option.builder()
            .longOpt(ECHO_CMD)
            .desc(ECHO_DESC)
            .build();
        compileStageGroup.addOption(echoOption);

        Option lexOption = Option.builder()
            .longOpt(LEXTEST_CMD)
            .desc(LEXTEST_DESC)
            .build();
        compileStageGroup.addOption(lexOption);

        Option parserOption = Option.builder()
            .longOpt(PARSERTEST_CMD)
            .desc(PARSETEST_DESC)
            .build();
        compileStageGroup.addOption(parserOption);

        Option printAstOption = Option.builder()
            .longOpt(PRINT_AST_CMD)
            .desc(PRINT_AST_DESC)
            .build();
        compileStageGroup.addOption(printAstOption);

        Option checkOption = Option.builder()
            .longOpt(CHECK_CMD)
            .desc(CHECK_DESC)
            .build();
        compileStageGroup.addOption(checkOption);

        Option compileFirmOption = Option.builder()
            .longOpt(COMPILE_FIRM_CMD)
            .desc(COMPILE_FIRM_DESC)
            .build();
        compileStageGroup.addOption(compileFirmOption);

        Option compileOption = Option.builder()
            .longOpt(COMPILE_CMD)
            .desc(COMPILE_DESC)
            .build();
        compileStageGroup.addOption(compileOption);

        Option helpOption = Option.builder()
            .longOpt(HELP_CMD)
            .desc(HELP_DESC)
            .build();
        compileStageGroup.addOption(helpOption);

        options.addOptionGroup(compileStageGroup);

        // Add switches

        Option noOptimizationOption = Option.builder()
            .longOpt(NO_OPTIMIZATION_SWITCH)
            .desc(NO_OPTIMIZATION_SWITCH_DESC)
            .build();
        options.addOption(noOptimizationOption);

        Option dumpGraphsOption = Option.builder()
            .longOpt(DUMP_SWITCH)
            .desc(DUMP_SWITCH_DESC)
            .build();
        options.addOption(dumpGraphsOption);

        Option verboseOption = Option.builder()
            .longOpt(VERBOSE_SWITCH)
            .desc(VERBOSE_SWITCH_DESC)
            .build();
        options.addOption(verboseOption);

        return options;
    }

    private static CommandLine generateCommandLine(Options options, String[] args) {
        CommandLineParser cmdLineParser = new DefaultParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = cmdLineParser.parse(options, args);
        }
        catch (ParseException e) {
            printError(e.getMessage());

            System.out.println();
            printHelpMessage(options);

            System.exit(1);
        }

        return cmdLine;
    }

    private static CompilerFlags extractFlags() {
        return new CompilerFlags(
            cmdLine.hasOption(DUMP_SWITCH),
            !cmdLine.hasOption(NO_OPTIMIZATION_SWITCH),
            cmdLine.hasOption(VERBOSE_SWITCH)
        );
    }

    private static void printError(String errorMessage) {
        System.err.println("error: " + errorMessage + "!");
    }

    private static void printHelpMessage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(NAME, options);
    }
}
