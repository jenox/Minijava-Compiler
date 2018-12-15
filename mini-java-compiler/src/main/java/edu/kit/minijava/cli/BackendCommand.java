package edu.kit.minijava.cli;

import edu.kit.minijava.ast.nodes.Program;
import edu.kit.minijava.backend.MolkiVisitor;
import edu.kit.minijava.lexer.Lexer;
import edu.kit.minijava.parser.Parser;
import edu.kit.minijava.parser.ParserException;
import edu.kit.minijava.semantic.ReferenceAndExpressionTypeResolver;
import edu.kit.minijava.semantic.SemanticException;
import edu.kit.minijava.transformation.EntityVisitor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import firm.*;

public class BackendCommand extends Command {

    public static final String RUNTIME_LIB_ENV_KEY = "MJ_RUNTIME_LIB_PATH";

    @Override
    public int execute(String path) {

        try {
            FileInputStream stream = new FileInputStream(path);
            InputStreamReader reader = new InputStreamReader(stream, "US-ASCII");

            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);

            Program program = parser.parseProgram();

            new ReferenceAndExpressionTypeResolver(program);

            EntityVisitor visitor = new EntityVisitor();
            Iterable<Graph> graphs = visitor.molkiTransform(program);

            // MOLKI TRANSFORMATION
            MolkiVisitor molkiVisitor = new MolkiVisitor();

            // TODO: do we have to treat main method differently?
            // VISIT METHODS
            graphs.forEach(g -> {
                String methodName = g.getEntity().getName();
                MethodType methodType = (MethodType) g.getEntity().getType();
                int noArgs = methodType.getNParams();
                int noResults = methodType.getNRess();

                molkiVisitor.appendMolkiCode("\n.function " + methodName + " " + noArgs + " " + noResults);
                g.walkTopological(molkiVisitor);
                molkiVisitor.appendMolkiCode("\n.endfunction\n");
            });

            String output = molkiVisitor.getMolkiCode();
            System.out.println(output);

            return 0;
        }
        catch (ParserException | SemanticException exception) {
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


