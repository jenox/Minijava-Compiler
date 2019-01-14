package edu.kit.minijava.cli;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import edu.kit.minijava.ast.nodes.Program;
import edu.kit.minijava.backend.*;
import edu.kit.minijava.lexer.Lexer;
import edu.kit.minijava.parser.*;
import edu.kit.minijava.semantic.*;
import edu.kit.minijava.transformation.EntityVisitor;
import firm.*;
import firm.nodes.Node;

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
            PrepVisitor prepVisitor = new PrepVisitor();

            graphs.forEach(g -> {
                BackEdges.enable(g);
                int numArgs = ((MethodType) g.getEntity().getType()).getNParams();
                prepVisitor.setRegisterIndex(numArgs);
                g.walkTopological(prepVisitor);
            });

            HashMap<Integer, List<Node>> blockId2Nodes = prepVisitor.getBlockId2Nodes();
            HashMap<Graph, List<Integer>> graph2BlockId = prepVisitor.getGraph2BlockId();
            MolkiTransformer molkiTransformer = new MolkiTransformer(prepVisitor.getNode2RegIndex());
            List<String> output = new ArrayList<>();

            graphs.forEach(g -> {
                String methodName = g.getEntity().getName();
                MethodType methodType = (MethodType) g.getEntity().getType();
                // non-main methods have additional `this` parameter
                int numArgs = Math.max(0, methodType.getNParams());
                int noResults = methodType.getNRess();

                //replace '.' with '_' for correct molki syntax
                methodName = methodName.replace('.', '_');

                //replace `__minijava_main` with `minijava_main`
                if (methodName.equals("__minijava_main")) {
                    methodName = methodName.substring(2);
                }

                output.add(".function " + methodName + " " + numArgs + " " + noResults);

                graph2BlockId.get(g).forEach(i -> {
                    molkiTransformer.getMolkiCode().put(i, new ArrayList<>());

                    blockId2Nodes.get(i).forEach(node -> molkiTransformer.createValue(i, node));
                });

                HashMap<Integer, List<String>> molkiCode = molkiTransformer.getMolkiCode();

                graph2BlockId.get(g).forEach(i -> {
                    // move jmps to the end of the block
                    String jmpString = null;
                    for (String str : molkiCode.get(i)) {
                        if (str.contains("jmp")) {
                            jmpString = str;
                        }

                    }

                    if (jmpString != null) {
                        molkiCode.get(i).remove(jmpString);
                        molkiCode.get(i).add(jmpString);
                    }

                    // output asm
                    output.add("L" + i + ":");
                    molkiCode.get(i).forEach(str -> output.add(str));
                });

                output.add(".endfunction\n");
            });


            Path file = Paths.get("a.molki.s");
            Files.write(file, output, Charset.forName("UTF-8"));

            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("python3.6 ../molki/molki.py compile a.molki.s -o a.out");

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
