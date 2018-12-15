package edu.kit.minijava.cli;

public class CommandLineInterface {
    public static void main(String[] arguments) {

        if (arguments.length != 2) {
            CommandLineInterface.printErrorAndExit();
        }

        final Command command;

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
            case "--backend":
                command = new BackendCommand();
                break;
            default:
                CommandLineInterface.printErrorAndExit();
                return;
        }

        int status = 0;
        try {
            status = command.execute(arguments[1]);
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

    private static void printErrorAndExit() {
        System.err.println("error, invalid command and/or number of arguments!");
        System.err.println("Usage: --echo <path>");

        System.exit(1);
    }
}
