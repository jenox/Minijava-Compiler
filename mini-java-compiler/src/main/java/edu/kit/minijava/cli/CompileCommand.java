package edu.kit.minijava.cli;

import java.io.*;
import java.util.Map;

import edu.kit.minijava.ast.nodes.Program;
import edu.kit.minijava.lexer.Lexer;
import edu.kit.minijava.parser.*;
import edu.kit.minijava.semantic.*;
import edu.kit.minijava.transformation.EntityVisitor;

public class CompileCommand extends Command {

    public static final String RUNTIME_LIB_ENV_KEY = "MJ_RUNTIME_LIB_PATH";
    public static final String MOLKI_PATH_KEY = "MOLKI_PATH";

    @Override
    public int execute(String path) {

        try {
            FileInputStream stream = new FileInputStream(path);
            InputStreamReader reader = new InputStreamReader(stream, "US-ASCII");

            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);

            Program program = parser.parseProgram();

            new ReferenceAndExpressionTypeResolver(program);

            String asmOutputFilename = "a.molki.s";
            String executableFilename = "a.out";

            EntityVisitor visitor = new EntityVisitor();
            visitor.transform(program, asmOutputFilename);

            // Retrieve runtime path from environment variable
            Map<String, String> env = System.getenv();
            String runtimeLibPath = env.get(RUNTIME_LIB_ENV_KEY);
            String molkiPath = env.get(MOLKI_PATH_KEY);

            if (runtimeLibPath == null) {
                System.err.println("error: Environment variable " + RUNTIME_LIB_ENV_KEY + " not set!");
                return 1;
            }

            if (molkiPath == null) {
                System.err.println("error: Environment variable " + MOLKI_PATH_KEY + " not set!");
                return 1;
            }

            Process molkiProcess = Runtime.getRuntime().exec(molkiPath + " assemble " + asmOutputFilename);

            int molki_result;

            try {
                molki_result = molkiProcess.waitFor();
            }
            catch (Throwable t) {
                molki_result = -1;
            }

            if (molki_result != 0) {
                System.err.println("error: molki script failed");
                return 1;
            }

            // Assemble and link runtime and code

            Process p = Runtime.getRuntime()
                    .exec("gcc" + " " + asmOutputFilename + " " + runtimeLibPath + " -o " + executableFilename);

            int result;

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
