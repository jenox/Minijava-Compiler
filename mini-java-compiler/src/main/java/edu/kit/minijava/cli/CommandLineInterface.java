package edu.kit.minijava.cli;

public class CommandLineInterface {
    public static void main(String[] arguments) {
        try {
            if (arguments.length == 2 && arguments[0].equals("--echo")) {
                EchoCommand command = new EchoCommand();
                int status = command.execute(arguments[1]);

                System.exit(status);
            } else if (arguments.length == 2 && arguments[0].equals("--lextest")) {
                LextestCommand command = new LextestCommand();
                int status = command.execute(arguments[1]);

                System.exit(status);
            } else {
                System.err.println("Error, invalid command and/or number of arguments!");
                System.err.println("Usage: --echo <path>");

                System.exit(1);
            }
        } catch (Exception exception) {
            String message = exception.getLocalizedMessage();

            if (message == null || message.isEmpty()) {
                System.err.println("Error: something went terribly wrong!");
            } else {
                System.err.println("Error: " + message);
            }

            System.exit(255);
        }
    }
}
