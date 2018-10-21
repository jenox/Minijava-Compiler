import java.io.*;
import java.nio.file.*;
import java.util.*;

import edu.kit.minijava.lexer.*;
import edu.kit.minijava.parser.*;

public class Skeleton {

    public static void main(String[] args) {
        final ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(args));

        // TODO: https://commons.apache.org/proper/commons-cli/introduction.html ?

        if (arguments.isEmpty()) {
            System.out.println("Please run with \"--echo <file-name>\".");
        } else if (arguments.get(0).equals("--echo")) {
            try {
                final Path path = Paths.get(arguments.get(1));
                final String text = new String(Files.readAllBytes(path));
                final Lexer lexer = new Lexer(text);

                System.out.println(lexer.text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (arguments.get(0).equals("--lex")) {
            try {
                final Path path = Paths.get(arguments.get(1));
                final String text = new String(Files.readAllBytes(path));
                final Lexer lexer = new Lexer(text);

                while (true) {
                    final Token token = lexer.nextToken();

                    if (token != null) {
                        System.out.println(token + " " + token.location);
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (arguments.get(0).equals("--parse")) {
            try {
                final Path path = Paths.get(arguments.get(1));
                final String text = new String(Files.readAllBytes(path));
                final Lexer lexer = new Lexer(text);
                final Parser parser = new Parser(lexer);

                Program program = parser.parseProgram();

                System.out.println(program);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }  else {
            System.out.println("Please run with \"--echo <file-name>\".");
        }
    }
}
