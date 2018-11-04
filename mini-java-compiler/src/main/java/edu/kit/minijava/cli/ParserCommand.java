package edu.kit.minijava.cli;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.kit.minijava.lexer.*;
import edu.kit.minijava.parser.*;
import edu.kit.minijava.ast.*;

public class ParserCommand extends Command {

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
            
           // ParserPrinter printer = new ParserPrinter(program);
           // printer.print();

            return 0;
        }
        catch (ParserException exception) {
            System.err.println("error: " + exception.getLocalizedMessage());
            //exception.printStackTrace();

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
