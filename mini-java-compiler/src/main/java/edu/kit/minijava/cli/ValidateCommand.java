package edu.kit.minijava.cli;

import edu.kit.minijava.lexer.*;
import edu.kit.minijava.parser.*;
import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.references.*;
import edu.kit.minijava.semantic.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ValidateCommand extends Command {

    ValidateCommand() {
    }

    @Override
    public int execute(String path) {
        try {
            FileInputStream stream = new FileInputStream(path);
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.US_ASCII);

            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();

            new ReferenceAndExpressionTypeResolver(program);

            ASTDumper dumper = new ASTDumper(program);
            dumper.dump("ast.graphml");

//            for (TypeReference reference : new Collector(program).instancesOfClass(TypeReference.class)) {
//                if (reference.getBasicTypeReference().isResolved()) {
//                    Declaration declaration = reference.getBasicTypeReference().getDeclaration();
//                    System.out.println("Reference " + reference + ": " + declaration);
//                }
//                else {
//                    System.out.println("Unresolved reference " + reference);
//                }
//            }
//
//            for (Reference reference : new Collector(program).instancesOfClass(Reference.class)) {
//                if (reference.isResolved()) {
//                    System.out.println("Reference " + reference + ": " + reference.getDeclaration());
//                }
//                else {
//                    System.out.println("Unresolved reference " + reference);
//                }
//            }
//
//            for (Expression expression : new Collector(program).instancesOfClass(Expression.class)) {
//                System.out.println(expression + ": " + expression.getType());
//            }

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
