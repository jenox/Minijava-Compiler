package edu.kit.minijava.cli;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import edu.kit.minijava.ast.nodes.Program;
import edu.kit.minijava.backend.*;
import edu.kit.minijava.backend.Util;
import edu.kit.minijava.lexer.Lexer;
import edu.kit.minijava.parser.*;
import edu.kit.minijava.semantic.*;
import edu.kit.minijava.transformation.EntityVisitor;
import firm.*;
import firm.nodes.Node;

public class CompileCommand extends Command {

    private static final String RUNTIME_LIB_ENV_KEY = "MJ_RUNTIME_LIB_PATH_STACK_ARGS";
    private static final String MOLKI_PATH_KEY = "MOLKI_PATH";

    @Override
    public int execute(String path) {

        try {
            FileInputStream stream = new FileInputStream(path);
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.US_ASCII);

            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);

            Program program = parser.parseProgram();

            new ReferenceAndExpressionTypeResolver(program);

            String asmOutputFilenameMolki = "a.molki.s";
            String asmOutputFileName = "a.out.s";
            String executableFilename = "a.out";

            EntityVisitor visitor = new EntityVisitor();
            Iterable<Graph> graphs = visitor.molkiTransform(program);

            // MOLKI TRANSFORMATION
            PrepVisitor prepVisitor = new PrepVisitor();

            graphs.forEach(g -> {
                BackEdges.enable(g);
                int numArgs = ((MethodType) g.getEntity().getType()).getNParams();
                prepVisitor.setNumberOfRegularPseudoregisters(numArgs);
                g.walkTopological(prepVisitor);
            });

            HashMap<Integer, List<Node>> blockId2Nodes = prepVisitor.getBlockId2Nodes();
            HashMap<Graph, List<Integer>> graph2BlockId = prepVisitor.getGraph2BlockId();
            HashMap<Graph, Integer> graph2MaxBlockId = prepVisitor.getGraph2MaxBlockId();
            MolkiTransformer molkiTransformer = new MolkiTransformer(prepVisitor.getNode2RegIndex(), graph2MaxBlockId);
            List<String> output = new ArrayList<>();

            graphs.forEach(g -> {
                String methodName = g.getEntity().getLdName();
                MethodType methodType = (MethodType) g.getEntity().getType();
                // non-main methods have additional `this` parameter
                int numArgs = Math.max(0, methodType.getNParams());
                int numResults = methodType.getNRess();

                // replace '.' with '_' for correct molki syntax
                methodName = methodName.replace('.', '_');

                String args = " [ ";
                String results = "";

                for (int i = 0; i < numArgs; i++) {
                    // this argument is the last
                    String argSuffix = Util.mode2MovSuffix(methodType.getParamType(i).getMode());

                    // fix suffix, if it's a pointer
                    if (argSuffix.equals("")) {
                        argSuffix = "q";
                    }

                    if (i + 1 == numArgs) {
                        args += argSuffix;
                    }
                    else {
                        args += argSuffix + " | ";
                    }
                }
                args += " ] ";

                if (numResults > 0) {
                    results += "-> ";
                }

                for (int i = 0; i < numResults; i++) {
                    results += Util.mode2MovSuffix(methodType.getResType(i).getMode());
                }

                if (methodName.equals("__minijava_main") || methodName.equals("___minijava_main")) {
                    output.add(".function " + methodName);
                }
                else {

                    output.add(".function " + methodName + " " + args + results);
                }

                // for each block, create an arraylist
                // and for each instruction in that block, transform it into a valid molki string
                graph2BlockId.get(g).forEach(i -> {
//                    molkiTransformer.getMolkiCode().put(i, new ArrayList<>());
                    molkiTransformer.insertBlock(i, new BasicBlock(i));

                    blockId2Nodes.get(i).forEach(node -> molkiTransformer.createValue(i, node));
                });

//                Map<Integer, List<String>> molkiCode = molkiTransformer.getMolkiCode();


                Map<Integer, BasicBlock> blockMap = molkiTransformer.getBlockMap();


                // Remove critical edges

                Map<Integer, BasicBlock> additionalBlocks = CriticalEdgeRemover.removeCriticalEdges(blockMap);

                // TODO Also check here that no labels have been assigned multiple times

                blockMap.putAll(additionalBlocks);

                blockMap.forEach((blockNumber, block) -> {

                    output.add(block.formatBlockLabel() + ":");

                    List<String> blockInstructions = block.getFullInstructionListAsString();
                    output.addAll(blockInstructions);
                });



                // go through all blocks of that graph

                // TODO This should not be handled at this point, but instead directly when generating code,
                // e.g. by generating compare and jump instructions in a separate list which is later appended
                // to the instructions from the basic block.

//                graph2BlockId.get(g).forEach(block -> {
////
////                    // Move all the jump and compare instructions to the end
////
////                    // TODO Can we move all cmp instructions to the end as well without the risk of
////                    // breaking any semantics?
////
////                    List<String> instructions = molkiCode.get(block);
////                    List<String> jmpInstructions = new ArrayList<>();
////                    for (int i = 0; i < instructions.size(); i++) {
////                        String str = instructions.get(i);
////
////                        if (str.startsWith(Util.INDENT + "phi_")) {
////                            instructions.remove(i);
////
////                            String temp = str.substring(Util.INDENT.length() + 4);
////                            instructions.add(i, Util.INDENT + temp);
////                        }
////                        if (Util.containsJmp(str)) {
////                            instructions.remove(i);
////                            i--;
////
////                            jmpInstructions.add(str);
////                        }
////                        else if (str.contains(Util.INDENT + "cmp")) {
////                            // if (i+1 < instructions.size() && Util.containsJmp(instructions.get(i+1))) {
////                            instructions.remove(i);
////                            i--;
////
////                            jmpInstructions.add(str);
////                            // }
////                        }
////                    }
////
////                    instructions.addAll(jmpInstructions);
//
//                    // output asm for the block
//                    output.add("L" + block + ":");
//                    output.addAll(molkiCode.get(block));
//                });

                output.add(".endfunction\n");
            });

            Path file = Paths.get(asmOutputFilenameMolki);
            Files.write(file, output, StandardCharsets.UTF_8);

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

            int molki_result;

            try {
                molki_result = this.exec(molkiPath + " " + asmOutputFilenameMolki + " " + asmOutputFileName);
            }
            catch (Throwable throwable) {
                molki_result = -1;
            }

            if (molki_result != 0) {
                System.err.println("error: molki script failed");
                return 1;
            }

            // Assemble and link runtime and code
            int result = 0;
            try {
                result = this.exec("gcc" + " " + asmOutputFileName + " " + runtimeLibPath + " -o " +
                                executableFilename);
            }
            catch (Throwable throwable) {
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

    private int exec(String command) throws Throwable {
        System.out.println("Executing command “" + command + "”");

        Process process = Runtime.getRuntime().exec(command);
        int result = process.waitFor();

        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();

            while (line != null) {
                System.out.println(line);
                line = reader.readLine();
            }
        }

        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line = reader.readLine();

            while (line != null) {
                System.out.println(line);
                line = reader.readLine();
            }
        }

        return result;
    }
}
