package edu.kit.minijava.cli;

import edu.kit.minijava.lexer.*;
import edu.kit.minijava.parser.*;
import edu.kit.minijava.ast.nodes.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ParserCommand extends Command {

    ParserCommand(CompilerFlags flags, boolean printAST) {
        super(flags);
        this.printAST = printAST;
    }

    private final boolean printAST;

    @Override
    public int execute(String path) {
        try {
            FileInputStream stream = new FileInputStream(path);
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.US_ASCII);

            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();

            if (this.printAST) {
                PrettyPrinter printer = new PrettyPrinter();
                String formatted = printer.format(program);

                System.out.print(formatted);
            }

            return 0;
        }
        catch (ParserException exception) {
            System.err.println("error: " + exception.getLocalizedMessage());

            return 1;
        }
        catch (FileNotFoundException exception) {
            System.err.println("error: File '" + path + "' was not found!");

            return 1;
        }
        catch (IOException exception) {
            System.err.println("error: File '" + path + "' could not be read!");

            return 1;
        }
    }
}
