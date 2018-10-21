import java.io.*;
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
                final Lexer lexer = new Lexer(arguments.get(1));
                System.out.println(lexer.text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (arguments.get(0).equals("--lex")) {
            try {
                final Lexer lexer = new Lexer(arguments.get(1));

                while (true) {
                    final Token token = lexer.nextToken();

                    if (token != null) {
                        System.out.println(token);
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (arguments.get(0).equals("--parse")) {
            try {
                final Lexer lexer = new Lexer(arguments.get(1));
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
