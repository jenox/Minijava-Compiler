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
import edu.kit.minijava.transformation.GraphGenerator;
import firm.*;
import firm.nodes.Node;

public class CompileCommand extends Command {

    private static final String RUNTIME_LIB_ENV_KEY = "MJ_RUNTIME_LIB_PATH_STACK_ARGS";
    private static final String REGISTER_ALLOCATION_KEY = "REGISTER_ALLOCATOR_PATH";

    CompileCommand(CompilerFlags flags) {
        super(flags);
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

            String asmIntermediateFilename = "a.molki.s";
            String asmOutputFileName = "a.out.s";

            String executableFilename = "a.out";

            EntityVisitor visitor = new EntityVisitor();
            visitor.startVisit(program);

            GraphGenerator generator = new GraphGenerator(visitor.getRuntimeEntities(),
                                                          visitor.getEntities(),
                                                          visitor.getTypes(),
                                                          visitor.getMethod2VariableNums(),
                                                          visitor.getMethod2ParamTypes(),
                                                          this.getFlags().optimize(),
                                                          this.getFlags().dumpIntermediates(),
                                                          this.getFlags().beVerbose());
            Iterable<Graph> graphs = generator.transform(program);


            // Transformation to intermediate representation used by register allocator

            PrepVisitor prepVisitor = new PrepVisitor();

            graphs.forEach(g -> {
                BackEdges.enable(g);
                int numArgs = ((MethodType) g.getEntity().getType()).getNParams();
                prepVisitor.setNumberOfRegularPseudoregisters(numArgs);
                g.walkTopological(prepVisitor);
            });

            HashMap<Integer, List<Node>> blockId2Nodes = prepVisitor.getBlockId2Nodes();
            HashMap<Graph, List<Integer>> graph2BlockId = prepVisitor.getGraph2BlockId();
            CodeGenerator codeGenerator = new CodeGenerator(prepVisitor.getNode2RegIndex());
            List<String> output = new ArrayList<>();

            for (Graph g : graphs) {
                String methodName = g.getEntity().getLdName();
                MethodType methodType = (MethodType) g.getEntity().getType();

                // Non-main methods have additional `this` parameter
                int numArgs = Math.max(0, methodType.getNParams());
                int numResults = methodType.getNRess();

                // Replace '.' with '_' for correct assembly syntax
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

                // Transform each node in each block into a intermediate representation instruction
                for (int i : graph2BlockId.get(g)) {
                    for (Node node : blockId2Nodes.get(i)) {
                        codeGenerator.createValue(i, node);
                    }
                }

                Map<Integer, BasicBlock> blockMap = codeGenerator.getBlockMap();
                List<BasicBlock> blockList = new ArrayList<>();

                for (int blockNumber : graph2BlockId.get(g)) {
                    BasicBlock currentBlock = blockMap.get(blockNumber);

                    if (currentBlock != null) {
                        blockList.add(blockMap.get(blockNumber));
                    }
                }

                // Remove critical edges
                List<BasicBlock> criticalEdgeFreeBlocks = CriticalEdgeRemover.removeCriticalEdges(blockList);

                // Resolve Phi nodes
                for (BasicBlock basicBlock : criticalEdgeFreeBlocks) {
                    PhiResolver.resolvePhiNodes(basicBlock);
                }

                for (BasicBlock block : criticalEdgeFreeBlocks) {
                    output.add(block.formatBlockLabel() + ":");
                    output.addAll(block.getFullInstructionListAsString());
                }

                output.add(".endfunction\n");
            }

            Path file = Paths.get(asmIntermediateFilename);
            Files.write(file, output, StandardCharsets.UTF_8);

            // Retrieve runtime path from environment variable
            Map<String, String> env = System.getenv();
            String runtimeLibPath = env.get(RUNTIME_LIB_ENV_KEY);
            String registerAllocatorPath = env.get(REGISTER_ALLOCATION_KEY);

            if (runtimeLibPath == null) {
                System.err.println("error: Environment variable " + RUNTIME_LIB_ENV_KEY + " not set!");
                return 1;
            }

            if (registerAllocatorPath == null) {
                System.err.println("error: Environment variable " + REGISTER_ALLOCATION_KEY + " not set!");
                return 1;
            }

            int registerAllocatorResult;
            try {
                registerAllocatorResult = this.exec(registerAllocatorPath + " "
                    + asmIntermediateFilename + " "
                    + asmOutputFileName);
            }
            catch (Throwable throwable) {
                registerAllocatorResult = -1;
            }

            if (registerAllocatorResult != 0) {
                System.err.println("error: Register allocation failed!");
                return 1;
            }

            // Assemble and link runtime and code

            int result;
            try {
                result = this.exec("gcc" + " "
                    + asmOutputFileName + " "
                    + runtimeLibPath
                    + " -o " + executableFilename);
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
        System.out.println("Executing command \"" + command + "\"");

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
