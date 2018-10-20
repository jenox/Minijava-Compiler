import java.io.*;
import java.util.*;

import edu.kit.minijava.lexer.*;

public class Skeleton {

    public static void main(String[] args) {
        final ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(args));

        // TODO: https://commons.apache.org/proper/commons-cli/introduction.html ?

        if (arguments.isEmpty()) {
            System.out.println("Please run with \"--echo <file-name>\".");
            System.out.println(arguments);
        } else if (arguments.get(0).equals("--echo")) {
            try {
                final Lexer lexer = new Lexer(arguments.get(1));
                System.out.println(lexer.text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (arguments.size() > 0 && arguments.get(0).equals("--lex")) {
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
        } else {
            System.out.println("Please run with \"--echo <file-name>\".");
        }
    }
}
