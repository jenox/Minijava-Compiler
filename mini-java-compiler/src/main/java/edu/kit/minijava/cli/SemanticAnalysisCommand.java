package edu.kit.minijava.cli;

import edu.kit.minijava.ast.nodes.Program;
import edu.kit.minijava.lexer.Lexer;
import edu.kit.minijava.parser.Parser;
import edu.kit.minijava.parser.ParserException;
import edu.kit.minijava.semantic.MemberCollector;
import edu.kit.minijava.semantic.SemanticAnalysisException;
import edu.kit.minijava.semantic.TypeCheckingVisitor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class SemanticAnalysisCommand extends Command {

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

            MemberCollector memberCollector = new MemberCollector();
            memberCollector.collectMembers(program);

            TypeCheckingVisitor semanticChecker = new TypeCheckingVisitor();
            semanticChecker.checkTypes(program);

            return 0;

        }
        catch (SemanticAnalysisException exception) {
            System.err.println("error: " + exception.getLocalizedMessage());

            return 1;
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