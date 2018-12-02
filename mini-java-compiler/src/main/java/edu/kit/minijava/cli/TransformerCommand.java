package edu.kit.minijava.cli;

import java.io.*;

import edu.kit.minijava.ast.nodes.Program;
import edu.kit.minijava.lexer.Lexer;
import edu.kit.minijava.parser.*;
import edu.kit.minijava.semantic.*;
import edu.kit.minijava.transformation.EntityVisitor;

public class TransformerCommand extends Command {

    @Override
    public int execute(String path) {

        try {
            FileInputStream stream = new FileInputStream(path);
            InputStreamReader reader = new InputStreamReader(stream, "US-ASCII");

            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);

            Program program = parser.parseProgram();

            ReferenceAndExpressionTypeResolver resolver = new ReferenceAndExpressionTypeResolver(program);

            String asmOutputFilename = "a.s";
            String executableFilename = "a.out";

            EntityVisitor visitor = new EntityVisitor();
            visitor.transform(program, asmOutputFilename);

            // TODO Set correct path to library

            // Compile standard library
            Process p = Runtime.getRuntime().exec("gcc -S lib/stdlib.c -o lib.s");
            int result;
            try {
                result = p.waitFor();
            }
            catch (Throwable t) {
                result = -1;
            }

            if (result != 0) {
                System.err.println("error: Failed to compile standard library!");
                return 1;
            }

            // Assemble and link runtime and code

            p = Runtime.getRuntime().exec("gcc lib.s " + asmOutputFilename + " -o " + executableFilename);

            try {
                result = p.waitFor();
            }
            catch (Throwable t) {
                result = -1;
            }

            if (result != 0) {
                System.err.println("error: Linking step failed!");
                return 1;
            }

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


