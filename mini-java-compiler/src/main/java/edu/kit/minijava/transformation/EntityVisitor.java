package edu.kit.minijava.transformation;

import java.io.IOException;
import java.util.*;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.nodes.Expression.*;
import edu.kit.minijava.ast.nodes.Program;
import edu.kit.minijava.ast.nodes.Statement.*;
import edu.kit.minijava.ast.references.TypeOfExpression;
import firm.*;
import firm.bindings.binding_ircons;
import firm.nodes.*;
import firm.nodes.Block;

public class EntityVisitor extends ASTVisitor<EntityContext> {

    private CompoundType globalType;
    private String currentClassName;

    private Map<String, Entity> runtimeEntities = new HashMap<>();

    private HashMap<Declaration, HashMap<Declaration, Integer>> method2VariableNums = new HashMap<>();
    private HashMap<Declaration, Integer> variableNums = new HashMap<>();
    private HashMap<Declaration, Entity> entities = new HashMap<>();
    private HashMap<Declaration, Type> types = new HashMap<>();
    private HashMap<Declaration, Integer> classSizes = new HashMap<>();

    private boolean isVariableCounting = false;
    private ClassDeclaration currentClassDeclaration;

    // CONSTANTS
    private Node nullNode = null;
    private Node trueNode = null;
    private Node falseNode = null;

    public void transform(Program program, String outputFilename) throws IOException {
        String[] targetOptions = { "pic=1" };

        // A null target triple causes Firm to choose the host machine triple
        Firm.init(null, targetOptions);

        this.globalType = firm.Program.getGlobalType();

        // Create entities for the runtime library calls
        this.createRuntimeEntities();

        program.accept(this, new EntityContext());

        // Check and dump created graphs - can be viewed with ycomp
        for (Graph g : firm.Program.getGraphs()) {
            g.check();
            // Should produce a file calc(II)I.vcg
            Dump.dumpGraph(g, "");
        }

        Lower.lower();

        Backend.lowerForTarget();

        // TODO What is the compilation unit name for?
        Backend.createAssembler(outputFilename, "<builtin>");
    }

    public void transform(Program program) throws IOException {
        this.transform(program, "a.s");
    }

    private void createRuntimeEntities() {

        PrimitiveType intType = new PrimitiveType(Mode.getIs());

        /* System.in.read */
        String readName = "system_in_read";
        MethodType readType = new MethodType(new Type[] {}, new Type[] { intType });

        Entity readEntity = new Entity(this.globalType, readName, readType);

        // Sets the mangled name of the entity
        readEntity.setLdIdent(readName);

        this.runtimeEntities.put(readName, readEntity);

        /* System.out.write */
        String writeName = "system_out_write";
        MethodType writeType = new MethodType(new Type[] { intType }, new Type[] {});

        Entity writeEntity = new Entity(this.globalType, writeName, writeType);

        // Sets the mangled name of the entity
        writeEntity.setLdIdent(writeName);

        this.runtimeEntities.put(writeName, writeEntity);

        /* System.out.flush */
        String flushName = "system_out_flush";
        MethodType flushType = new MethodType(new Type[] {}, new Type[] {});

        Entity flushEntity = new Entity(this.globalType, flushName, flushType);

        // Sets the mangled name of the entity
        flushEntity.setLdIdent(flushName);

        this.runtimeEntities.put(flushName, flushEntity);

        /* System.out.println */
        String printlnName = "system_out_println";
        MethodType printlnType = new MethodType(new Type[] { intType }, new Type[] {});

        Entity printlnEntity = new Entity(this.globalType, printlnName, printlnType);

        // Sets the mangled name of the entity
        printlnEntity.setLdIdent(printlnName);

        this.runtimeEntities.put(printlnName, printlnEntity);
    }

    @Override
    protected void visit(Program program, EntityContext context) {
        this.isVariableCounting = true;
        for (ClassDeclaration decl : program.getClassDeclarations()) {
            decl.accept(this, context);
        }
        this.isVariableCounting = false;
        for (ClassDeclaration decl : program.getClassDeclarations()) {
            decl.accept(this, context);
        }
    }

    @Override
    protected void visit(ClassDeclaration classDeclaration, EntityContext context) {
        StructType structType = new StructType(classDeclaration.getName());
        context.setClassType(structType);

        this.currentClassName = classDeclaration.getName();
        this.currentClassDeclaration = classDeclaration;
        this.types.put(classDeclaration, structType);
        this.classSizes.put(classDeclaration, 0);

        for (FieldDeclaration fieldDeclaration : classDeclaration.getFieldDeclarations()) {
            fieldDeclaration.accept(this, context);
            Type type = this.types.get(fieldDeclaration);
            this.classSizes.put(classDeclaration, this.classSizes.get(classDeclaration) + new Integer(type.getSize()));
        }

        for (MethodDeclaration methodDecl : classDeclaration.getMethodDeclarations()) {
            methodDecl.accept(this, context);
        }

        for (MainMethodDeclaration mainMethodDeclaration : classDeclaration.getMainMethodDeclarations()) {
            mainMethodDeclaration.accept(this, context);
        }

        // Layout class
        // TODO Is this the right place for this?
        structType.layoutFields();
        structType.finishLayout();

    }

    @Override
    protected void visit(FieldDeclaration fieldDeclaration, EntityContext context) {
        fieldDeclaration.getType().accept(this, context);
        this.types.put(fieldDeclaration, context.getType());

        // create entity for method
        Entity fieldEntity = new Entity(context.getClassType(), this.getUniqueMemberName(fieldDeclaration.getName()),
                context.getType());
        this.entities.put(fieldDeclaration, fieldEntity);
    }

    @Override
    protected void visit(MainMethodDeclaration methodDeclaration, EntityContext context) {
        Type[] parameterTypes = {};
        Type[] resultTypes = {};

        if (this.isVariableCounting) {
            this.variableNums = new HashMap<>();

            MethodType mainMethodType = new MethodType(parameterTypes, resultTypes);

            // Entity mainMethodEntity = new Entity(this.globalType,
            // this.getUniqueMemberName(methodDeclaration.getName()),
            // mainMethodType);
            Entity mainMethodEntity = new Entity(this.globalType, "__minijava_main", mainMethodType);

            this.entities.put(methodDeclaration, mainMethodEntity);

            methodDeclaration.getBody().accept(this, context);
        }
        else {
            this.variableNums = this.method2VariableNums.get(methodDeclaration);

            Entity mainMethodEntity = this.entities.get(methodDeclaration);
            Graph graph = new Graph(mainMethodEntity, context.getNumberOfLocalVars());
            Construction construction = new Construction(graph);

            // traverse body and create code
            context.setConstruction(construction);
            context.setCalledFromMain(true);
            methodDeclaration.getBody().accept(this, context);

            Node mem = construction.getCurrentMem();
            Node returnNode = construction.newReturn(mem, new Node[] {});
            graph.getEndBlock().addPred(returnNode);

            // No code should follow a return statement.
            construction.setUnreachable();
            // Done.
            construction.finish();
        }

        this.method2VariableNums.put(methodDeclaration, this.variableNums);
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, EntityContext context) {
        // GET TYPES OF ALL PARAMETERS (FOR METHOD ENTITY)
        Type[] parameterTypes = new Type[methodDeclaration.getParameters().size() + 1];
        parameterTypes[0] = new PointerType(context.getClassType());

        for (int count = 1; count < parameterTypes.length; count++) {
            EntityContext entContext = new EntityContext();
            TypeReference paramRef = methodDeclaration.getParameterTypes().get(count - 1);
            paramRef.accept(this, entContext);

            parameterTypes[count] = entContext.getType();
        }

        if (this.isVariableCounting) {
            this.variableNums = new HashMap<>();

            this.variableNums.put(this.currentClassDeclaration, 0);

            // GET RETURN TYPE OF THE METHOD
            Type[] resultType = {};

            methodDeclaration.getReturnType().accept(this, context);
            if (!methodDeclaration.getReturnType().isVoid()) {
                resultType = new Type[] { context.getType() };
            }

            // CREATE ENTITY FOR METHOD
            MethodType methodType = new MethodType(parameterTypes, resultType);
            Entity methodEntity = new Entity(this.globalType, this.getUniqueMemberName(methodDeclaration.getName()),
                    methodType);
            this.entities.put(methodDeclaration, methodEntity);

            // COUNT LOCAL VARIABLES
            context.setNumberOfLocalVars(1);
            methodDeclaration.getParameters().forEach(p -> p.accept(this, context));

            methodDeclaration.getBody().accept(this, context);
        }
        else {
            this.variableNums = this.method2VariableNums.get(methodDeclaration);

            Entity methodEntity = this.entities.get(methodDeclaration);
            Graph graph = new Graph(methodEntity, context.getNumberOfLocalVars());
            Construction construction = new Construction(graph);

            // set variables and parameters
            Node projThis = construction.newProj(graph.getArgs(), Mode.getP(), 0);
            construction.setVariable(0, projThis);

            for (int i = 1; i < parameterTypes.length; i++) {
                Type paramType = parameterTypes[i];
                Mode mode = paramType.getMode();

                if (mode == null) {
                    mode = Mode.getP();
                }

                Node paramProj = construction.newProj(graph.getArgs(), mode, i);
                construction.setVariable(i, paramProj);
            }

            // traverse body and create code
            context.setConstruction(construction);
            context.setCalledFromMain(false);
            methodDeclaration.getBody().accept(this, context);

            // no return stmts so far? -> create return node
            if (graph.getEndBlock().getPredCount() == 0) {

                assert methodDeclaration.getReturnType().isVoid() : "Fell through without return on non-void method.";

                Node mem = construction.getCurrentMem();
                Node returnNode = construction.newReturn(mem, new Node[] {});
                graph.getEndBlock().addPred(returnNode);
            }

            // No code should follow a return statement.
            construction.setUnreachable();
            // Done.
            construction.finish();
        }

        this.method2VariableNums.put(methodDeclaration, this.variableNums);
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, EntityContext context) {
        this.variableNums.put(parameterDeclaration, context.getNumberOfLocalVars());
        context.incrementLocalVarCount();

        parameterDeclaration.getType().accept(this, context);
        this.types.put(parameterDeclaration, context.getType());

        context.setResult(null);
    }

    @Override
    protected void visit(ExplicitTypeReference reference, EntityContext context) {
        Type firmType = null;

        BasicTypeDeclaration decl = reference.getBasicTypeReference().getDeclaration();
        Type elementType = null;
        String name = reference.getBasicTypeReference().getName();

        // check for array type
        if (reference.getNumberOfDimensions() > 0) {

            switch (name) {
                case "boolean":
                case "String":
                case "void":
                    elementType = new PrimitiveType(Mode.getBs());
                    break;
                case "int":
                    elementType = new PrimitiveType(Mode.getIs());
                    break;
                default:
                    elementType = new StructType(name); //TODO: arrays von structs passen noch nicht
            }
            this.types.put(reference.getBasicTypeReference().getDeclaration(), elementType);

            for (int i = 0; i < reference.getNumberOfDimensions(); i++) {
                elementType = new ArrayType(elementType, 0);
            }

            elementType.finishLayout();
            firmType = elementType;
        }
        else if (!decl.isClassDeclaration()) {

            PrimitiveTypeDeclaration decl_ = (PrimitiveTypeDeclaration) decl;

            switch (decl_) {
                case INTEGER:
                    firmType = new PrimitiveType(Mode.getIs());
                    break;
                case VOID:
                case STRING:
                case BOOLEAN:
                    firmType = new PrimitiveType(Mode.getBs());
                    break;
                default:
                    break;
            }
        }
        // user defined type
        else {
            firmType = new PointerType(new StructType(name));
        }

        context.setType(firmType);
    }

    @Override
    protected void visit(ImplicitTypeReference reference, EntityContext context) {
        // nothing to do here
    }

    @Override
    protected void visit(IfStatement statement, EntityContext context) {
        // generate code for the condition
        if (!this.isVariableCounting) {
            Construction construction = context.getConstruction();

            statement.getCondition().accept(this, context);
            Node cond;
            if (context.getResult().getMode().equals(Mode.getBs())) {
                Node byteFalse = null;
                if (this.falseNode == null) {
                    byteFalse = construction.newConst(0, Mode.getBs());
                }
                else {
                    byteFalse = this.falseNode;
                }

                Node cmp = construction.newCmp(context.getResult(), byteFalse, Relation.Equal.negated());
                cond = construction.newCond(cmp);
            }
            else {
                cond = construction.newCond(context.getResult());
            }

            Node projTrue = context.getConstruction().newProj(cond, Mode.getX(), Cond.pnTrue);
            Node projFalse = context.getConstruction().newProj(cond, Mode.getX(), Cond.pnFalse);

            // If-Block (the true part)
            firm.nodes.Block bTrue = context.getConstruction().newBlock();
            bTrue.addPred(projTrue);
            context.getConstruction().setCurrentBlock(bTrue); /* we create nodes in the new block now */

            // generate code for the true-statement
            statement.getStatementIfTrue().accept(this, context);

            // Jump out of if-block
            Node endIf = context.getConstruction().newJmp();

            Node endElse = null;
            if (statement.getStatementIfFalse().isPresent()) {
                // Else-Block
                firm.nodes.Block bFalse = context.getConstruction().newBlock();
                bFalse.addPred(projFalse);
                context.getConstruction().setCurrentBlock(bFalse);

                statement.getStatementIfFalse().ifPresent(c -> c.accept(this, context));

                // Jump out of else-block
                endElse = context.getConstruction().newJmp();
            }

            // Follow-up block connect with the jumps out of if- and else-block
            firm.nodes.Block bAfter = context.getConstruction().newBlock();
            bAfter.addPred(endIf);
            if (statement.getStatementIfFalse().isPresent()) {
                bAfter.addPred(endElse);
            }
            else {
                bAfter.addPred(projFalse);
            }
            context.getConstruction().setCurrentBlock(bAfter);
        }
        else {
            statement.getStatementIfTrue().accept(this, context);
            statement.getStatementIfFalse().ifPresent(c -> c.accept(this, context));
        }
    }

    @Override
    protected void visit(WhileStatement statement, EntityContext context) {
        Construction construction = context.getConstruction();
        if (!this.isVariableCounting) {
            Node jump = construction.newJmp();

            firm.nodes.Block loopHeader = construction.newBlock();
            loopHeader.addPred(jump);
            construction.setCurrentBlock(loopHeader);

            // loop condition
            statement.getCondition().accept(this, context);

            Node cond;
            if (context.getResult().getMode().equals(Mode.getBs())) {
                Node byteFalse = null;
                if (this.falseNode == null) {
                    byteFalse = context.getConstruction().newConst(0, Mode.getBs());
                }
                else {
                    byteFalse = this.falseNode;
                }
                Node cmp = construction.newCmp(context.getResult(), byteFalse, Relation.Equal.negated());
                cond = construction.newCond(cmp);
            }
            else {
                cond = construction.newCond(context.getResult());
            }

            Node projTrue = construction.newProj(cond, Mode.getX(), Cond.pnTrue);
            Node projFalse = construction.newProj(cond, Mode.getX(), Cond.pnFalse);

            // loop body
            firm.nodes.Block loopBody = construction.newBlock();
            loopBody.addPred(projTrue);
            construction.setCurrentBlock(loopBody);

            statement.getStatementWhileTrue().accept(this, context);

            Node jmp2 = construction.newJmp();
            loopHeader.addPred(jmp2);

            // after-loop block
            firm.nodes.Block afterLoop = construction.newBlock();
            afterLoop.addPred(projFalse);
            construction.setCurrentBlock(afterLoop);
        }
        else {
            statement.getStatementWhileTrue().accept(this, context);
        }
    }

    @Override
    protected void visit(ExpressionStatement statement, EntityContext context) {
        context.setTopLevel(true);
        statement.getExpression().accept(this, context);
    }

    @Override
    protected void visit(ReturnStatement statement, EntityContext context) {
        context.setTopLevel(true);

        Optional<Expression> returnValue = statement.getValue();
        if (returnValue.isPresent()) {
            returnValue.get().accept(this, context);
        }
        else {
            context.setResult(null);
        }

        if (!this.isVariableCounting) {
            Node[] results;
            Node mem = context.getConstruction().getCurrentMem();

            if (context.getResult() != null) {
                results = new Node[] { context.getResult() };

                Node returnNode = context.getConstruction().newReturn(mem, results);
                context.getConstruction().getGraph().getEndBlock().addPred(returnNode);
            }
        }
    }

    @Override
    protected void visit(EmptyStatement statement, EntityContext context) {
        // do nothing
    }

    @Override
    protected void visit(LocalVariableDeclarationStatement statement, EntityContext context) {
        // use old variable count as our index
        // TODO: do store here
        if (!this.isVariableCounting) {
            statement.getValue().ifPresent(e -> {
                e.accept(this, context);
                int num = this.variableNums.get(statement);

                // TODO What was this for?
//                if (e instanceof SystemInReadExpression) {
//                    Type type = this.types.get(statement.getType().getBasicTypeReference().getDeclaration());
//                    context.getConstruction().setVariable(num, context.getConstruction().newConst(0, type.getMode()));
//                }
//                else {
                context.getConstruction().setVariable(num, context.getResult());
//                }
            });
        }
        else {
            // count variable
            this.variableNums.put(statement, context.getNumberOfLocalVars());
            context.incrementLocalVarCount();

            // add variable type
            statement.getType().accept(this, context);
            this.types.put(statement, context.getType());
        }

        context.setResult(null);
    }

    @Override
    protected void visit(edu.kit.minijava.ast.nodes.Statement.Block block, EntityContext context) {
        for (Statement stmt : block.getStatements()) {
            stmt.accept(this, context);

            if (stmt instanceof ReturnStatement) {
                break;
            }
        }
    }

    @Override
    protected void visit(BinaryOperation expression, EntityContext context) {

        Node bin = null;

        if (expression.getOperationType() == BinaryOperationType.LOGICAL_AND) {
            if (this.isVariableCounting) {
                expression.getRight().accept(this, context);
                context.setLeftSideOfAssignment(false);
                expression.getLeft().accept(this, context);

            }
            else {
                context.setResult(this.handleShortCircuitedAnd(expression, context));
            }

            return;
        }

        if (expression.getOperationType() == BinaryOperationType.LOGICAL_OR) {
            if (this.isVariableCounting) {
                expression.getRight().accept(this, context);
                context.setLeftSideOfAssignment(false);
                expression.getLeft().accept(this, context);

            }
            else {
                context.setResult(this.handleShortCircuitedOr(expression, context));
            }

            return;
        }

        expression.getRight().accept(this, context);
        Node right = context.getResult();

        context.setLeftSideOfAssignment(expression.getOperationType().equals(BinaryOperationType.ASSIGNMENT));
        expression.getLeft().accept(this, context);
        Node left = context.getResult();

        if (!this.isVariableCounting) {
            switch (expression.getOperationType()) {
                case ADDITION:
                    bin = context.getConstruction().newAdd(left, right);
                    break;
                case SUBTRACTION:
                    bin = context.getConstruction().newSub(left, right);
                    break;
                case MULTIPLICATION:
                    bin = context.getConstruction().newMul(left, right);
                    break;
                case LESS_THAN:
                    bin = context.getConstruction().newCmp(left, right, Relation.Less);
                    break;
                case LESS_THAN_OR_EQUAL_TO:
                    bin = context.getConstruction().newCmp(left, right, Relation.LessEqual);
                    break;
                case GREATER_THAN:
                    bin = context.getConstruction().newCmp(left, right, Relation.Greater);
                    break;
                case GREATER_THAN_OR_EQUAL_TO:
                    bin = context.getConstruction().newCmp(left, right, Relation.GreaterEqual);
                    break;
                case EQUAL_TO:
                    bin = context.getConstruction().newCmp(left, right, Relation.Equal);
                    break;
                case NOT_EQUAL_TO:
                    bin = context.getConstruction().newCmp(left, right, Relation.Equal.negated());
                    break;
                case DIVISION:
                    Node mem = context.getConstruction().getCurrentMem();
                    Node div = context.getConstruction().newDiv(mem, left, right,
                            binding_ircons.op_pin_state.op_pin_state_floats);
                    bin = context.getConstruction().newProj(div, Mode.getIs(), Div.pnRes);
                    Node projMem = context.getConstruction().newProj(div, Mode.getM(), Div.pnM);
                    context.getConstruction().setCurrentMem(projMem);
                    break;
                case MODULO:
                    Node mem_ = context.getConstruction().getCurrentMem();
                    Node mod = context.getConstruction().newMod(mem_, left, right,
                            binding_ircons.op_pin_state.op_pin_state_floats);
                    bin = context.getConstruction().newProj(mod, Mode.getIs(), Mod.pnRes);
                    Node projMem_ = context.getConstruction().newProj(mod, Mode.getM(), Div.pnM);
                    context.getConstruction().setCurrentMem(projMem_);
                    break;
                case ASSIGNMENT:
                    bin = right;
                    break;
                default:
                    assert false : "Unhandled binary operation!";
            }
        }

        context.setResult(bin);
    }

    @Override
    protected void visit(UnaryOperation expression, EntityContext context) {
        expression.getOther().accept(this, context);
        Node otherNode = context.getResult();

        Node uni = null;

        if (!this.isVariableCounting) {
            Construction construction = context.getConstruction();
            switch (expression.getOperationType()) {
                case LOGICAL_NEGATION:
                    uni = construction.newNot(otherNode);
                    break;
                case NUMERIC_NEGATION:
                    uni = context.getConstruction().newMinus(otherNode);
                    break;
                default:
                    break;
            }
        }

        context.setResult(uni);
    }

    @Override
    protected void visit(NullLiteral expression, EntityContext context) {
        // Node result = context.getConstruction().newUnknown(Mode.getP());
        if (this.isVariableCounting) {
            //nothing to do
            return;
        }

        if (this.nullNode == null) {
            TargetValue arst = new TargetValue(0, Mode.getP());
            Node result = context.getConstruction().newConst(arst);
            context.setResult(result);
            this.nullNode = result;
        }
        else {
            context.setResult(this.nullNode);
        }
    }

    @Override
    protected void visit(BooleanLiteral expression, EntityContext context) {
        Node bool = null;

        if (!this.isVariableCounting) {
            if (expression.getValue()) {
                if (this.trueNode == null) {
                    bool = context.getConstruction().newConst(-1, Mode.getBs());
                }
                else {
                    bool = this.trueNode;
                }
            }
            else {
                if (this.falseNode == null) {
                    bool = context.getConstruction().newConst(0, Mode.getBs());
                }
                else {
                    bool = this.falseNode;
                }
            }
        }

        context.setResult(bool);
    }

    @Override
    protected void visit(IntegerLiteral expression, EntityContext context) {
        if (!this.isVariableCounting) {
            Double val = new Double(expression.getValue());
            double maxInt = Integer.MAX_VALUE;
            double minInt = Integer.MIN_VALUE;
            double tmp;
            int intVal;

            if (val > maxInt) {
                tmp = (val - maxInt - 1) + minInt;
            }
            else if (val < minInt) {
                tmp = maxInt - (minInt - val - 1);
            }
            else {
                tmp = val;
            }
            intVal = (int) tmp;

            TargetValue tarval = new TargetValue(intVal, Mode.getIs());
            Node lit = null;
            lit = context.getConstruction().newConst(tarval);
            context.setResult(lit);
        }
    }

    @Override
    protected void visit(MethodInvocation expression, EntityContext context) {
        Node result = null;

        if (!this.isVariableCounting) {
            Construction construction = context.getConstruction();
            Graph graph = construction.getGraph();

            Node mem = context.getConstruction().getCurrentMem();
            Node[] in = new Node[expression.getArguments().size() + 1];

            if (!expression.getContext().isPresent()) {
                in[0] = construction.newProj(graph.getArgs(), Mode.getP(), 0);
            }
            else {
                if (expression.getContext().get() instanceof VariableAccess) {
                    expression.getContext().get().accept(this, context);
                    in[0] = context.getResult();
                    // Declaration decl = ((VariableAccess) expression.getContext().get())
                    // .getVariableReference().getDeclaration();
                    // int num = this.variableNums.get(decl);
                    // in[0] = construction.getVariable(num, Mode.getP());
                }
                else if (expression.getContext().get() instanceof MethodInvocation) {
                    expression.getContext().get().accept(this, context);
                    in[0] = context.getResult();
                }
                else if (expression.getContext().get() instanceof ExplicitFieldAccess) {
                    expression.getContext().get().accept(this, context);
                    in[0] = context.getResult();
                }
                else if (expression.getContext().get() instanceof NewObjectCreation) {
                    expression.getContext().get().accept(this, context);
                    in[0] = context.getResult();
                }
                else {
                    in[0] = construction.newProj(graph.getArgs(), Mode.getP(), 0);
                }
            }

            for (int i = 0; i < expression.getArguments().size(); i++) {
                expression.getArguments().get(i).accept(this, context);
                if (context.getResult().getMode().equals(Mode.getb())) {
                    Node cresult = context.getResult();
                    if (cresult.equals(TargetValue.getBTrue())) {
                        in[i + 1] = construction.newConst(-1, Mode.getBs());
                    }
                    else {
                        in[i + 1] = construction.newConst(0, Mode.getBs());
                    }
                }
                else {
                    in[i + 1] = context.getResult();
                }
            }

            Entity methodEntity = this.entities.get(expression.getMethodReference().getDeclaration());
            Node callee = context.getConstruction().newAddress(methodEntity);
            Node callNode = context.getConstruction().newCall(mem, callee, in, methodEntity.getType());

            Node newMem = context.getConstruction().newProj(callNode, Mode.getM(), Call.pnM);
            context.getConstruction().setCurrentMem(newMem);

            // Retrieve mode for the return value
            if (!expression.getType().isVoid()) {
                Mode returnMode = this.getModeForType(expression.getType());
                assert returnMode != null;

                Node tuple = context.getConstruction().newProj(callNode, Mode.getT(), Call.pnTResult);
                result = context.getConstruction().newProj(tuple, returnMode, 0);
            }
        }

        context.setResult(result);
    }

    @Override
    protected void visit(ExplicitFieldAccess expression, EntityContext context) {
        boolean isLeftSide = context.isLeftSideOfAssignment();
        context.setLeftSideOfAssignment(false);
        Node right = context.getResult();

        if (!this.isVariableCounting) {
            Entity field = this.entities.get(expression.getFieldReference().getDeclaration());
            Node thisNode = null;
            ClassDeclaration decl = (ClassDeclaration) expression.getContext().getType().getDeclaration().get();

            if (expression.getContext() instanceof MethodInvocation) {
                expression.getContext().accept(this, context);
                thisNode = context.getResult();
            }
            else if (expression.getContext() instanceof ExplicitFieldAccess) {
                expression.getContext().accept(this, context);
                thisNode = context.getResult();
            }
            else if (expression.getContext() instanceof NewObjectCreation) {
                expression.getContext().accept(this, context);
                thisNode = context.getResult();
            }
            else if (expression.getContext() instanceof VariableAccess) {
                expression.getContext().accept(this, context);
                thisNode = context.getResult();
                // Declaration contexDecl = ((VariableAccess) expression.getContext())
                // .getVariableReference().getDeclaration();
                // int num = this.variableNums.get(contexDecl);
                // thisNode = context.getConstruction().getVariable(num ,Mode.getP());
            }
            else if (!context.isCalledFromMain()) {
                thisNode = context.getConstruction().getVariable(0, Mode.getP());
            }

            Node member = context.getConstruction().newMember(thisNode, field);
            Node mem = context.getConstruction().getCurrentMem();
            Mode mode = this.types.get(expression.getFieldReference().getDeclaration()).getMode();

            if (mode == null) {
                mode = Mode.getP();
            }

            Node newMem = null;
            Node result = null;

            if (isLeftSide) {
                Node store = context.getConstruction().newStore(mem, member, right);
                newMem = context.getConstruction().newProj(store, Mode.getM(), Store.pnM);
            }
            else {
                Node load = context.getConstruction().newLoad(mem, member, mode);
                newMem = context.getConstruction().newProj(load, Mode.getM(), Load.pnM);
                result = context.getConstruction().newProj(load, mode, Load.pnRes);
                context.setResult(result);
            }

            context.getConstruction().setCurrentMem(newMem);
        }
    }

    @Override
    protected void visit(ArrayElementAccess expression, EntityContext context) {
        boolean isLeftSide = context.isLeftSideOfAssignment();
        boolean isTopLevel = context.isTopLevel();
        context.setLeftSideOfAssignment(false);
        context.setTopLevel(false);
        Node right = context.getResult();
        context.setResult(null);

        if (!this.isVariableCounting) {
            // for sel stmt
            expression.getContext().accept(this, context);

            Node arrayPointer = context.getResult();

            Type type = this.types.get(context.getDecl());
            Type elementType = this.types.get(expression.getContext().getType().getDeclaration().get());

            // for sel stmt
            expression.getIndex().accept(this, context);
            Node index = context.getResult();

            // access array
            Node mem = context.getConstruction().getCurrentMem();
            Node newMem = null;

            if (isTopLevel) {
                if (isLeftSide) {
                    Node sel = context.getConstruction().newSel(arrayPointer, index, type);
                    Node store = context.getConstruction().newStore(mem, sel, right);
                    newMem = context.getConstruction().newProj(store, Mode.getM(), Store.pnM);
                }
                else {
                    Node sel = context.getConstruction().newSel(arrayPointer, index, type);
                    Node load = context.getConstruction().newLoad(mem, sel, elementType.getMode());
                    newMem = context.getConstruction().newProj(load, Mode.getM(), Load.pnM);
                    Node result = context.getConstruction().newProj(load, elementType.getMode(), Load.pnRes);
                    context.setResult(result);
                }

                context.getConstruction().setCurrentMem(newMem);
            }
            else {
                Node sel = context.getConstruction().newSel(arrayPointer, index, type);
                context.setResult(sel);
            }

        }
        else {
            expression.getContext().accept(this, context);
        }
    }

    @Override
    protected void visit(VariableAccess expression, EntityContext context) {
        boolean isLeftSide = context.isLeftSideOfAssignment();
        context.setLeftSideOfAssignment(false);
        Node right = context.getResult();
        context.setResult(null);

        Declaration decl = expression.getVariableReference().getDeclaration();
        if (expression.getVariableReference().getDeclaration().getType().getNumberOfDimensions() > 0) {
            context.setDecl(decl);
        }
        if (!this.isVariableCounting) {

            if (decl instanceof LocalVariableDeclarationStatement || decl instanceof ParameterDeclaration) {
                Node mem = context.getConstruction().getCurrentMem();
                int n = this.variableNums.get(decl);
                Type type = this.types.get(decl);
                Mode mode = type.getMode();

                if (mode == null) {
                    mode = Mode.getP();
                }

                if (isLeftSide) {
                    context.getConstruction().setVariable(n, right);
                }
                else {
                    Node getNode = context.getConstruction().getVariable(n, mode);
                    context.setResult(getNode);
                }

            }
            else if (decl instanceof FieldDeclaration) {
                // expression.getContext().accept(this, context);

                Entity field = this.entities.get(decl);
                Node thisNode = context.getConstruction().getVariable(0, Mode.getP());
                Node member = context.getConstruction().newMember(thisNode, field);

                Mode mode = this.types.get(decl).getMode();
                if (mode == null) {
                    mode = Mode.getP();
                }

                Node mem = context.getConstruction().getCurrentMem();
                Node newMem = null;

                if (isLeftSide) {
                    Node store = context.getConstruction().newStore(mem, member, right);
                    newMem = context.getConstruction().newProj(store, Mode.getM(), Store.pnM);
                }
                else {
                    Node load = context.getConstruction().newLoad(mem, member, mode);
                    newMem = context.getConstruction().newProj(load, Mode.getM(), Load.pnM);
                    Node result = context.getConstruction().newProj(load, mode, Load.pnRes);
                    context.setResult(result);
                }

                context.getConstruction().setCurrentMem(newMem);
            }
        }
    }

    @Override
    protected void visit(CurrentContextAccess expression, EntityContext context) {
        if (!this.isVariableCounting) {
            Node mem = context.getConstruction().getCurrentMem();
            Node getNode = context.getConstruction().getVariable(0, Mode.getP());
            context.setResult(getNode);
        }
    }

    @Override
    protected void visit(NewObjectCreation expression, EntityContext context) {
        if (!this.isVariableCounting) {
            // calculate size and alignment
            Node mem = context.getConstruction().getCurrentMem();
            int classSize = this.classSizes.get(expression.getClassReference().getDeclaration());
            Node size = context.getConstruction().newConst(classSize, Mode.getIu());
            int alignment = this.types.get(expression.getClassReference().getDeclaration()).getAlignment();

            // allocate memory
            Node alloc = context.getConstruction().newAlloc(mem, size, alignment);
            Node newMem = context.getConstruction().newProj(alloc, Mode.getM(), Alloc.pnM);
            Node res = context.getConstruction().newProj(alloc, Mode.getP(), Alloc.pnRes);

            // init fields
            context.setLeftSideOfAssignment(true);
            for (FieldDeclaration decl : expression.getClassReference().getDeclaration().getFieldDeclarations()) {
                Entity field = this.entities.get(decl);
                Node member = context.getConstruction().newMember(res, field);
                Mode mode = this.types.get(decl).getMode();

                Node right = null;

                //if mode is null, we have a non-atomic type, initialize with null pointer
                if (mode == null || mode.equals(Mode.getP())) {
                    //we have an array, initialize with null pointer
                    if (this.nullNode == null) {
                        TargetValue arst = new TargetValue(0, Mode.getP());
                        right = context.getConstruction().newConst(arst);
                        this.nullNode = right;
                    }
                    else {
                        right = this.nullNode;
                    }
                }
                else if (mode.equals(Mode.getBs())) {
                    if (this.trueNode == null) {
                        right = context.getConstruction().newConst(0, Mode.getBs());
                    }
                    else {
                        right = this.trueNode;
                    }
                }
                else if (mode.equals(Mode.getIs())) {
                    right = context.getConstruction().newConst(0, Mode.getBs());
                    //TODO: sollte hier vllt Mode.getIs stehen?
                }

                Node store = context.getConstruction().newStore(mem, member, right);
                newMem = context.getConstruction().newProj(store, Mode.getM(), Store.pnM);

            }
            context.setLeftSideOfAssignment(false);

            context.getConstruction().setCurrentMem(newMem);
            context.setResult(res);
        }
    }

    @Override
    protected void visit(NewArrayCreation expression, EntityContext context) {
        if (!this.isVariableCounting) {
            expression.getPrimaryDimension().accept(this, context);
            Node size = context.getConstruction().newConv(context.getResult(), Mode.getIu());

            int alignment = context.getType().getAlignment();

            Node mem = context.getConstruction().getCurrentMem();
            Node alloc = context.getConstruction().newAlloc(mem, size, alignment);
            Node newMem = context.getConstruction().newProj(alloc, Mode.getM(), Alloc.pnM);
            Node res = context.getConstruction().newProj(alloc, Mode.getP(), Alloc.pnRes);

            context.getConstruction().setCurrentMem(newMem);
            context.setResult(res);
        }
    }

    @Override
    protected void visit(SystemOutPrintlnExpression expression, EntityContext context) {
        if (!this.isVariableCounting) {
            Construction construction = context.getConstruction();
            Node mem = construction.getCurrentMem();

            expression.getArgument().accept(this, context);
            Node argument = context.getResult();

            Entity functionEntity = this.runtimeEntities.get("system_out_println");
            assert functionEntity != null : "Runtime library function entity must be present";

            Node functionAddress = construction.newAddress(functionEntity);

            Node call = construction.newCall(mem, functionAddress, new Node[] { argument }, functionEntity.getType());

            Node newMem = construction.newProj(call, Mode.getM(), Call.pnM);
            construction.setCurrentMem(newMem);
        }
    }

    @Override
    protected void visit(SystemOutFlushExpression expression, EntityContext context) {
        if (!this.isVariableCounting) {
            Construction construction = context.getConstruction();
            Node mem = construction.getCurrentMem();

            Entity functionEntity = this.runtimeEntities.get("system_out_flush");
            assert functionEntity != null : "Runtime library function entity must be present";

            Node functionAddress = construction.newAddress(functionEntity);

            Node call = construction.newCall(mem, functionAddress, new Node[] {}, functionEntity.getType());

            Node newMem = construction.newProj(call, Mode.getM(), Call.pnM);
            construction.setCurrentMem(newMem);
        }
    }

    @Override
    protected void visit(SystemOutWriteExpression expression, EntityContext context) {
        if (!this.isVariableCounting) {
            Construction construction = context.getConstruction();
            Node mem = construction.getCurrentMem();

            expression.getArgument().accept(this, context);
            Node argument = context.getResult();

            Entity functionEntity = this.runtimeEntities.get("system_out_write");
            assert functionEntity != null : "Runtime library function entity must be present";

            Node functionAddress = construction.newAddress(functionEntity);

            Node call = construction.newCall(mem, functionAddress, new Node[] { argument }, functionEntity.getType());

            Node newMem = construction.newProj(call, Mode.getM(), Call.pnM);
            construction.setCurrentMem(newMem);
        }
    }

    @Override
    protected void visit(SystemInReadExpression expression, EntityContext context) {
        Node result = null;

        if (!this.isVariableCounting) {
            Construction construction = context.getConstruction();
            Node mem = construction.getCurrentMem();

            Entity functionEntity = this.runtimeEntities.get("system_in_read");
            assert functionEntity != null : "Runtime library function entity must be present";

            Node functionAddress = construction.newAddress(functionEntity);

            Node call = construction.newCall(mem, functionAddress, new Node[] {}, functionEntity.getType());

            Node newMem = construction.newProj(call, Mode.getM(), Call.pnM);
            construction.setCurrentMem(newMem);

            Node callResults = construction.newProj(call, Mode.getT(), Call.pnTResult);
            result = construction.newProj(callResults, Mode.getIs(), 0);
        }

        context.setResult(result);
    }

    private Node handleShortCircuitedAnd(BinaryOperation expression, EntityContext context) {
        Construction construction = context.getConstruction();

        expression.getLeft().accept(this, context);
        Node left = context.getResult();

        // Projection to cond with lhs
        Node lhsCond = this.createCond(left, construction);

        Node leftTrue = construction.newProj(lhsCond, Mode.getX(), Cond.pnTrue);
        Node leftFalse = construction.newProj(lhsCond, Mode.getX(), Cond.pnFalse);

        // construction.getCurrentBlock().mature();

        Block lhsTrueBlock = construction.newBlock();
        lhsTrueBlock.addPred(leftTrue);
        construction.setCurrentBlock(lhsTrueBlock);

        expression.getRight().accept(this, context);
        Node right = context.getResult();

        // Projection to cond with lhs
        Node rhsCond = this.createCond(right, construction);

        Node rightTrue = context.getConstruction().newProj(rhsCond, Mode.getX(), Cond.pnTrue);
        Node rightFalse = context.getConstruction().newProj(rhsCond, Mode.getX(), Cond.pnFalse);

        // construction.getCurrentBlock().mature();

        Block blockEitherFalse = construction.newBlock();
        construction.setCurrentBlock(blockEitherFalse);
        blockEitherFalse.addPred(leftFalse);
        blockEitherFalse.addPred(rightFalse);

        Node eitherFalseJmp = construction.newJmp();

        Node constZero = construction.newConst(0, Mode.getBs());
        Node constOne = construction.newConst(-1, Mode.getBs());

        Block afterBlock = construction.newBlock();
        construction.setCurrentBlock(afterBlock);

        afterBlock.addPred(eitherFalseJmp);
        afterBlock.addPred(rightTrue);

        Node phiNode = construction.newPhi(new Node[] { constZero, constOne }, Mode.getBs());

        context.setResult(phiNode);
        return phiNode;
    }

    private Node handleShortCircuitedOr(BinaryOperation expression, EntityContext context) {
        Construction construction = context.getConstruction();

        expression.getLeft().accept(this, context);
        Node left = context.getResult();

        // Projection to cond with lhs
        Node lhsCond = this.createCond(left, construction);

        Node leftTrue = construction.newProj(lhsCond, Mode.getX(), Cond.pnTrue);
        Node leftFalse = construction.newProj(lhsCond, Mode.getX(), Cond.pnFalse);

        // construction.getCurrentBlock().mature();

        Block lhsFalseBlock = construction.newBlock();
        lhsFalseBlock.addPred(leftFalse);
        construction.setCurrentBlock(lhsFalseBlock);

        expression.getRight().accept(this, context);
        Node right = context.getResult();

        // Projection to cond with lhs
        Node rhsCond = this.createCond(right, construction);

        Node rightTrue = context.getConstruction().newProj(rhsCond, Mode.getX(), Cond.pnTrue);
        Node rightFalse = context.getConstruction().newProj(rhsCond, Mode.getX(), Cond.pnFalse);

        // construction.getCurrentBlock().mature();

        Block blockEitherTrue = construction.newBlock();
        construction.setCurrentBlock(blockEitherTrue);
        blockEitherTrue.addPred(leftTrue);
        blockEitherTrue.addPred(rightTrue);

        Node eitherTrueJump = construction.newJmp();

        Node constZero = construction.newConst(0, Mode.getBs());
        Node constOne = construction.newConst(-1, Mode.getBs());

        Block afterBlock = construction.newBlock();
        construction.setCurrentBlock(afterBlock);

        afterBlock.addPred(rightFalse);
        afterBlock.addPred(eitherTrueJump);

        Node phiNode = construction.newPhi(new Node[] { constZero, constOne }, Mode.getBs());

        context.setResult(phiNode);
        return phiNode;
    }

    private String getUniqueMemberName(String methodName) {
        return this.currentClassName + "." + methodName;
    }

    private Mode getModeForType(TypeOfExpression type) {
        if (type.isInteger()) {
            return Mode.getIs();
        }
        else if (type.isBoolean()) {
            return Mode.getBs();
        }
        else if (type.isArrayType() || type.isObjectOrNull()) {
            return Mode.getP();
        }
        else if (type.isVoid()) {
            return null;
        }
        else {
            assert false : "Cannot get correct mode for type!";
            return null;
        }
    }

    private Node createCond(Node node, Construction construction) {
        Node cond;
        if (node.getMode().equals(Mode.getBs())) {
            Node byteFalse = construction.newConst(0, Mode.getBs());
            Node cmp = construction.newCmp(node, byteFalse, Relation.Equal.negated());
            cond = construction.newCond(cmp);
        }
        else {
            cond = construction.newCond(node);
        }
        return cond;
    }
}
