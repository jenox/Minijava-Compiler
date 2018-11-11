package edu.kit.minijava.cli;

import edu.kit.minijava.ast.nodes.Program;
import edu.kit.minijava.lexer.Lexer;
import edu.kit.minijava.parser.Parser;
import edu.kit.minijava.parser.ParserException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class ParserCommand extends Command {

    ParserCommand(boolean printAST) {
        this.printAST = printAST;
    }

    private final boolean printAST;

    @Override
    public int execute(String path) {
        try {
            FileInputStream stream = new FileInputStream(path);
            InputStreamReader reader = new InputStreamReader(stream, "US-ASCII");

            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();

            if (program == null) {
                throw new AssertionError();
            }

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
