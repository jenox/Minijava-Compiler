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

    public Iterable<Graph> molkiTransform(Program program) throws IOException {
        String[] targetOptions = { "pic=1" };

        // A null target triple causes Firm to choose the host machine triple
        Firm.init(null, targetOptions);

        this.globalType = firm.Program.getGlobalType();

        // Create entities for the runtime library calls
        this.createRuntimeEntities();

        program.accept(this, new EntityContext());

        // Check created graphs
        for (Graph g : firm.Program.getGraphs()) {
            g.check();
        }

        return firm.Program.getGraphs();
    }

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
            ConstantFolder folder = new ConstantFolder(g);
            g.check();
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
        PrimitiveType pointerType = new PrimitiveType(Mode.getP());

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

        // TODO Should this take unsigned integers instead?

        /* Memory allocation */
        String allocName = "alloc_mem";
        MethodType allocType = new MethodType(new Type[] { intType, intType }, new Type[] { pointerType });

        Entity allocEntity = new Entity(this.globalType, allocName, allocType);

        // Sets the mangled name of the entity
        allocEntity.setLdIdent(allocName);

        this.runtimeEntities.put(allocName, allocEntity);
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
            this.classSizes.put(classDeclaration, this.classSizes.get(classDeclaration) + type.getSize());
        }

        for (MethodDeclaration methodDecl : classDeclaration.getMethodDeclarations()) {
            methodDecl.accept(this, context);
        }

        for (MainMethodDeclaration mainMethodDeclaration : classDeclaration.getMainMethodDeclarations()) {
            mainMethodDeclaration.accept(this, context);
        }

        // Layout class
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

        EntityContext methodContext = new EntityContext();

        if (this.isVariableCounting) {
            this.variableNums = new HashMap<>();

            MethodType mainMethodType = new MethodType(parameterTypes, resultTypes);

            Entity mainMethodEntity = new Entity(this.globalType, "minijava_main", mainMethodType);
            this.entities.put(methodDeclaration, mainMethodEntity);

            methodDeclaration.getBody().accept(this, methodContext);
        }
        else {
            this.variableNums = this.method2VariableNums.get(methodDeclaration);

            Entity mainMethodEntity = this.entities.get(methodDeclaration);

            Graph graph = new Graph(mainMethodEntity, this.variableNums.size());
            Construction construction = new Construction(graph);

            // traverse body and create code
            methodContext.setConstruction(construction);
            methodContext.setCalledFromMain(true);
            methodDeclaration.getBody().accept(this, methodContext);

            if (!methodContext.endsOnJumpNode()) {
                Node mem = construction.getCurrentMem();
                Node returnNode = construction.newReturn(mem, new Node[] {});
                graph.getEndBlock().addPred(returnNode);
            }

            construction.finish();
        }

        this.method2VariableNums.put(methodDeclaration, this.variableNums);
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, EntityContext context) {
        // GET TYPES OF ALL PARAMETERS (FOR METHOD ENTITY)
        Type[] parameterTypes = new Type[methodDeclaration.getParameters().size() + 1];
        parameterTypes[0] = new PointerType(context.getClassType());

        EntityContext methodContext = new EntityContext();
        methodContext.setNumberOfLocalVars(1);

        for (int count = 1; count < parameterTypes.length; count++) {
            TypeReference paramRef = methodDeclaration.getParameterTypes().get(count - 1);
            paramRef.accept(this, methodContext);

            parameterTypes[count] = methodContext.getType();
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
            methodDeclaration.getParameters().forEach(p -> p.accept(this, methodContext));

            methodDeclaration.getBody().accept(this, methodContext);
        }
        else {
            this.variableNums = this.method2VariableNums.get(methodDeclaration);

            Entity methodEntity = this.entities.get(methodDeclaration);

            Graph graph = new Graph(methodEntity, this.variableNums.size() + parameterTypes.length);

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
            methodContext.setConstruction(construction);
            methodContext.setCalledFromMain(false);
            methodDeclaration.getBody().accept(this, methodContext);

            // Create return node if the method body does not end with a return statement
            if (!methodContext.endsOnJumpNode()) {

                assert methodDeclaration.getReturnType().isVoid() : "Fell through without return on non-void method.";

                Node mem = construction.getCurrentMem();
                Node returnNode = construction.newReturn(mem, new Node[] {});
                graph.getEndBlock().addPred(returnNode);
            }

            construction.getCurrentBlock().mature();
            construction.getGraph().getEndBlock().mature();

            construction.finish();
        }

        this.method2VariableNums.put(methodDeclaration, this.variableNums);
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, EntityContext context) {
        if (this.isVariableCounting) {
            this.variableNums.put(parameterDeclaration, context.getNumberOfLocalVars());
            context.incrementLocalVarCount();

            parameterDeclaration.getType().accept(this, context);
            this.types.put(parameterDeclaration, context.getType());
        }

        context.setResult(null);
    }

    @Override
    protected void visit(ExplicitTypeReference reference, EntityContext context) {
        Type firmType = null;
        Type elementType = null;

        BasicTypeDeclaration decl = reference.getBasicTypeReference().getDeclaration();
        String name = reference.getBasicTypeReference().getName();

        if (decl.isClassDeclaration()) {
            elementType = new PointerType(new StructType(name));
        }
        else if (decl instanceof PrimitiveTypeDeclaration) {
            // Primitive type
            PrimitiveTypeDeclaration primitiveDeclaration = (PrimitiveTypeDeclaration) decl;

            switch (primitiveDeclaration) {
                case INTEGER:
                    elementType = new PrimitiveType(Mode.getIs());
                    break;
                case VOID:
                case STRING:
                case BOOLEAN:
                    elementType = new PrimitiveType(Mode.getBs());
                    break;
                default:
                    assert false : "Unhandled primitive type!";
                    break;
            }
        }
        else {
            assert false : "Unhandled type declaration";
        }

        elementType.finishLayout();
        this.types.put(reference.getBasicTypeReference().getDeclaration(), elementType);

        if (reference.getNumberOfDimensions() > 0) {
            // We have an array type, wrap in pointers according to number of dimensions
            for (int i = 0; i < reference.getNumberOfDimensions(); i++) {
                elementType = new PointerType(new ArrayType(elementType, 0));
            }
        }

        firmType = elementType;
        context.setType(firmType);
    }

    @Override
    protected void visit(ImplicitTypeReference reference, EntityContext context) {
        // Nothing to do here
    }

    @Override
    protected void visit(IfStatement statement, EntityContext context) {
        // Generate code for the condition
        if (!this.isVariableCounting) {
            Construction construction = context.getConstruction();

            Block blockBeforeIf = construction.getCurrentBlock();

            statement.getCondition().accept(this, context);

            ExpressionResult.Condition condition = context.getResult().convertToCondition();

            blockBeforeIf.mature();
            condition.getBlock().mature();

            // If-Block (the true part)
            firm.nodes.Block bTrue = context.getConstruction().newBlock();
            bTrue.addPred(condition.getIfTrue());

            // Create nodes in the new block now
            context.getConstruction().setCurrentBlock(bTrue);

            // generate code for the true-statement
            statement.getStatementIfTrue().accept(this, context);

            // Set current block to last block in then statement
            bTrue = context.getConstruction().getCurrentBlock();

            boolean thenEndsOnJump = context.endsOnJumpNode();

            Node endElse = null;
            boolean elseEndsOnJump = false;

            firm.nodes.Block bFalse = context.getConstruction().newBlock();
            bFalse.addPred(condition.getIfFalse());
            context.getConstruction().setCurrentBlock(bFalse);

            if (statement.getStatementIfFalse().isPresent()) {
                // Else-Block
                context.setEndsOnJumpNode(false);

                statement.getStatementIfFalse().ifPresent(c -> c.accept(this, context));

                elseEndsOnJump = context.endsOnJumpNode();
            }

            bFalse = context.getConstruction().getCurrentBlock();

            // Follow-up block connection with the jumps out of if- and else-block
            if (!thenEndsOnJump && !elseEndsOnJump) {
                firm.nodes.Block bAfter = context.getConstruction().newBlock();

                context.getConstruction().setCurrentBlock(bTrue);
                Node endIf = context.getConstruction().newJmp();

                context.getConstruction().setCurrentBlock(bFalse);
                endElse = context.getConstruction().newJmp();

                bAfter.addPred(endIf);
                bAfter.addPred(endElse);

                context.getConstruction().setCurrentBlock(bAfter);

                bTrue.mature();
                bFalse.mature();

                context.setEndsOnJumpNode(false);
            }
            // Only then branch ends on a branch node
            else if (!thenEndsOnJump) {
                // In this case, set the else block as the current block
                context.getConstruction().setCurrentBlock(bTrue);

                bFalse.mature();

                context.setEndsOnJumpNode(false);
            }
            else if (!elseEndsOnJump) {
                context.getConstruction().setCurrentBlock(bFalse);

                bTrue.mature();

                context.setEndsOnJumpNode(false);
            }
            // Both end on jump node
            else {
                bTrue.mature();
                bFalse.mature();

                context.setEndsOnJumpNode(true);
            }
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

            Block blockBeforeWhile = construction.getCurrentBlock();

            Node jump = construction.newJmp();

            firm.nodes.Block loopHeader = construction.newBlock();
            loopHeader.addPred(jump);
            construction.setCurrentBlock(loopHeader);

            blockBeforeWhile.mature();

            // loop condition
            statement.getCondition().accept(this, context);

            ExpressionResult.Condition condition = context.getResult().convertToCondition();

            // loop body
            firm.nodes.Block loopBody = construction.newBlock();
            loopBody.addPred(condition.getIfTrue());
            construction.setCurrentBlock(loopBody);

            statement.getStatementWhileTrue().accept(this, context);

            boolean loopEndsOnJumpNode = context.endsOnJumpNode();

            if (!loopEndsOnJumpNode) {
                Node jmp2 = construction.newJmp();
                loopHeader.addPred(jmp2);
            }

            // after-loop block
            firm.nodes.Block afterLoop = construction.newBlock();
            afterLoop.addPred(condition.getIfFalse());
            construction.setCurrentBlock(afterLoop);

            loopHeader.mature();
            loopBody.mature();

            context.setEndsOnJumpNode(false);
        }
        else {
            statement.getStatementWhileTrue().accept(this, context);
        }
    }

    @Override
    protected void visit(ExpressionStatement statement, EntityContext context) {
        statement.getExpression().accept(this, context);

        context.setEndsOnJumpNode(false);
    }

    @Override
    protected void visit(ReturnStatement statement, EntityContext context) {
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
                results = new Node[] { context.getResult().convertToValue().getNode() };
            }
            else {
                results = new Node[] {};
            }

            Node returnNode = context.getConstruction().newReturn(mem, results);
            context.getConstruction().getGraph().getEndBlock().addPred(returnNode);

            context.setEndsOnJumpNode(true);
        }
    }

    @Override
    protected void visit(EmptyStatement statement, EntityContext context) {
        // Nothing to do here
    }

    @Override
    protected void visit(LocalVariableDeclarationStatement statement, EntityContext context) {
        if (!this.isVariableCounting) {
            statement.getValue().ifPresent(e -> {
                e.accept(this, context);
                int num = this.variableNums.get(statement);

                context.getConstruction().setVariable(num, context.getResult().convertToValue().getNode());
            });
        }
        else {
            // Count variable, use old variable count as our index
            this.variableNums.put(statement, context.getNumberOfLocalVars());
            context.incrementLocalVarCount();

            // add variable type
            statement.getType().accept(this, context);
            this.types.put(statement, context.getType());
        }

        context.setResult(null);
        context.setEndsOnJumpNode(false);
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

        ExpressionResult result = null;
        Construction construction = context.getConstruction();

        if (this.isVariableCounting) {
            expression.getLeft().accept(this, context);
            expression.getRight().accept(this, context);
        }
        else {
            if (expression.getOperationType() == BinaryOperationType.LOGICAL_AND) {
                context.setResult(this.handleShortCircuitedAnd(expression, context));
                return;
            }

            if (expression.getOperationType() == BinaryOperationType.LOGICAL_OR) {
                context.setResult(this.handleShortCircuitedOr(expression, context));
                return;
            }

            if (expression.getOperationType() == BinaryOperationType.ASSIGNMENT) {

                expression.getLeft().accept(this, context);

                // This should always be a valid l-value
                ExpressionResult lhs = context.getResult();

                expression.getRight().accept(this, context);
                ExpressionResult.Value right = context.getResult().convertToValue();
                context.setResult(right);

                ExpressionResult assignmentResult = lhs.assignTo(right);

                context.setResult(assignmentResult);
                return;
            }

            expression.getLeft().accept(this, context);
            ExpressionResult.Value leftExpression = context.getResult().convertToValue();
            context.setResult(leftExpression);

            Node left = leftExpression.getNode();

            expression.getRight().accept(this, context);
            ExpressionResult.Value rightExpression = context.getResult().convertToValue();
            context.setResult(rightExpression);

            Node right = rightExpression.getNode();

            switch (expression.getOperationType()) {
                case ADDITION:
                    result = new ExpressionResult.Value(construction, context.getConstruction().newAdd(left, right));
                    break;
                case SUBTRACTION:
                    result = new ExpressionResult.Value(construction, context.getConstruction().newSub(left, right));
                    break;
                case MULTIPLICATION:
                    result = new ExpressionResult.Value(construction, context.getConstruction().newMul(left, right));
                    break;
                case LESS_THAN:
                    result = new ExpressionResult.Condition(construction,
                                    context.getConstruction().newCmp(left, right, Relation.Less));
                    break;
                case LESS_THAN_OR_EQUAL_TO:
                    result = new ExpressionResult.Condition(construction,
                                    context.getConstruction().newCmp(left, right, Relation.LessEqual));
                    break;
                case GREATER_THAN:
                    result = new ExpressionResult.Condition(construction,
                                    context.getConstruction().newCmp(left, right, Relation.Greater));
                    break;
                case GREATER_THAN_OR_EQUAL_TO:
                    result = new ExpressionResult.Condition(construction,
                                    context.getConstruction().newCmp(left, right, Relation.GreaterEqual));
                    break;
                case EQUAL_TO:
                    result = new ExpressionResult.Condition(construction,
                                    context.getConstruction().newCmp(left, right, Relation.Equal));
                    break;
                case NOT_EQUAL_TO:
                    result = new ExpressionResult.Condition(construction,
                                    context.getConstruction().newCmp(left, right, Relation.Equal.negated()));
                    break;
                case DIVISION:
                    Node mem = context.getConstruction().getCurrentMem();
                    Node left64 = construction.newConv(left, Mode.getLs());
                    Node right64 = construction.newConv(right, Mode.getLs());
                    Node div = context.getConstruction().newDiv(mem, left64, right64,
                                    binding_ircons.op_pin_state.op_pin_state_floats);
                    Node res = context.getConstruction().newProj(div, Mode.getLs(), Div.pnRes);
                    Node resInt = construction.newConv(res, Mode.getIs());

                    result = new ExpressionResult.Value(construction, resInt);

                    Node projMem = context.getConstruction().newProj(div, Mode.getM(), Div.pnM);
                    context.getConstruction().setCurrentMem(projMem);
                    break;
                case MODULO:
                    Node mem_ = context.getConstruction().getCurrentMem();
                    left64 = construction.newConv(left, Mode.getLs());
                    right64 = construction.newConv(right, Mode.getLs());
                    Node mod = context.getConstruction().newMod(mem_, left64, right64,
                                    binding_ircons.op_pin_state.op_pin_state_floats);
                    res = context.getConstruction().newProj(mod, Mode.getLs(), Mod.pnRes);
                    resInt = construction.newConv(res, Mode.getIs());

                    result = new ExpressionResult.Value(construction, resInt);

                    Node projMem_ = context.getConstruction().newProj(mod, Mode.getM(), Div.pnM);
                    context.getConstruction().setCurrentMem(projMem_);
                    break;
                default:
                    assert false : "Unhandled binary operation!";
            }
        }

        context.setResult(result);
    }

    @Override
    protected void visit(UnaryOperation expression, EntityContext context) {
        expression.getOther().accept(this, context);

        if (!this.isVariableCounting) {
            Node uni = null;
            ExpressionResult.Value operand = context.getResult().convertToValue();

            switch (expression.getOperationType()) {
                case LOGICAL_NEGATION:
                    uni = context.getConstruction().newNot(operand.getNode());
                    break;
                case NUMERIC_NEGATION:
                    uni = context.getConstruction().newMinus(operand.getNode());
                    break;
                default:
                    assert false : "Unhandled unary operation!";
            }
            context.setResult(new ExpressionResult.Value(context.getConstruction(), uni));
        }
    }

    @Override
    protected void visit(NullLiteral expression, EntityContext context) {
        if (!this.isVariableCounting) {
            TargetValue nullValue = new TargetValue(0, Mode.getP());
            Node result = context.getConstruction().newConst(nullValue);

            context.setResult(new ExpressionResult.Value(context.getConstruction(), result));
        }
    }

    @Override
    protected void visit(BooleanLiteral expression, EntityContext context) {
        if (!this.isVariableCounting) {
            TargetValue booleanValue;

            if (expression.getValue()) {
                booleanValue = new TargetValue(-1, Mode.getBs());
            }
            else {
                booleanValue = new TargetValue(0, Mode.getBs());
            }

            Node boolNode = context.getConstruction().newConst(booleanValue);

            ExpressionResult.Value result = new ExpressionResult.Value(context.getConstruction(), boolNode);

            context.setResult(result);
        }
    }

    @Override
    protected void visit(IntegerLiteral expression, EntityContext context) {
        if (!this.isVariableCounting) {
            // The literal has already been checked for overflow during semantic analysis.
            // The unary minus in case of negative literals has been moved into the literal itself.

            // If this throws a NumberFormatException, something is going wrong in semantic analysis
            int value = Integer.parseInt(expression.getValue());

            TargetValue tarval = new TargetValue(value, Mode.getIs());
            Node literal = context.getConstruction().newConst(tarval);

            context.setResult(new ExpressionResult.Value(context.getConstruction(), literal));
        }
    }

    @Override
    protected void visit(MethodInvocation expression, EntityContext context) {
        Node result = null;

        if (this.isVariableCounting) {
            context.setResult(null);
        }
        else {
            Construction construction = context.getConstruction();
            Graph graph = construction.getGraph();

            Node[] in = new Node[expression.getArguments().size() + 1];

            if (!expression.getContext().isPresent()) {
                in[0] = construction.newProj(graph.getArgs(), Mode.getP(), 0);
            }
            else {
                expression.getContext().get().accept(this, context);
                in[0] = context.getResult().convertToValue().getNode();
            }

            for (int i = 0; i < expression.getArguments().size(); i++) {
                expression.getArguments().get(i).accept(this, context);

                in[i + 1] = context.getResult().convertToValue().getNode();
            }

            Entity methodEntity = this.entities.get(expression.getMethodReference().getDeclaration());
            Node callee = context.getConstruction().newAddress(methodEntity);

            Node mem = context.getConstruction().getCurrentMem();
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
            context.setResult(new ExpressionResult.Value(context.getConstruction(), result));
        }
    }

    @Override
    protected void visit(ExplicitFieldAccess expression, EntityContext context) {
        if (!this.isVariableCounting) {
            Entity field = this.entities.get(expression.getFieldReference().getDeclaration());

            // First, evaluate context and set result
            expression.getContext().accept(this, context);
            Node contextValue = context.getResult().convertToValue().getNode();

            Member member = (Member) context.getConstruction().newMember(contextValue, field);

            Mode mode = this.types.get(expression.getFieldReference().getDeclaration()).getMode();

            // All types without explicit modes associated with them are reference types
            if (mode == null) {
                mode = Mode.getP();
            }

            ExpressionResult.FieldLValue result = new ExpressionResult.FieldLValue(context.getConstruction(), member,
                            mode);

            context.setResult(result);
        }
    }

    @Override
    protected void visit(ArrayElementAccess expression, EntityContext context) {
        if (!this.isVariableCounting) {

            // Evaluate array reference first
            expression.getContext().accept(this, context);
            Node arrayPointer = context.getResult().convertToValue().getNode();

            // Evaluate index expression
            expression.getIndex().accept(this, context);
            Node index = context.getResult().convertToValue().getNode();

            // Ensure correct type of the expression and retrieve element types
            Type type = this.firmTypeFromExpressionType(expression.getContext().getType());
            assert (type instanceof PointerType) : "All arrays are pointer types!";
            type = ((PointerType) type).getPointsTo();

            Type elementType = this.firmTypeFromExpressionType(expression.getType());

            Sel sel = (Sel) context.getConstruction().newSel(arrayPointer, index, type);

            ExpressionResult.ArrayLValue result = new ExpressionResult.ArrayLValue(context.getConstruction(), sel,
                            elementType);

            context.setResult(result);
        }
        else {
            expression.getContext().accept(this, context);
        }
    }

    @Override
    protected void visit(VariableAccess expression, EntityContext context) {

        Declaration decl = expression.getVariableReference().getDeclaration();

        if (!this.isVariableCounting) {

            if (decl instanceof LocalVariableDeclarationStatement || decl instanceof ParameterDeclaration) {
                int n = this.variableNums.get(decl);
                Type type = this.types.get(decl);

                // TODO Use mode instead of type here?
                Mode mode = Optional.ofNullable(type.getMode()).orElse(Mode.getP());

                ExpressionResult.LocalVariableLValue result = new ExpressionResult.LocalVariableLValue(context
                                .getConstruction(), n, type);

                context.setResult(result);
            }
            else if (decl instanceof FieldDeclaration) {
                Entity field = this.entities.get(decl);
                Node thisNode = context.getConstruction().getVariable(0, Mode.getP());
                Member member = (Member) context.getConstruction().newMember(thisNode, field);

                Mode mode = Optional.ofNullable(this.types.get(decl).getMode()).orElse(Mode.getP());

                ExpressionResult.FieldLValue result = new ExpressionResult.FieldLValue(context.getConstruction(),
                                member, mode);

                context.setResult(result);
            }
            else {
                assert false : "Unhandled assignment type";
            }
        }
    }

    @Override
    protected void visit(CurrentContextAccess expression, EntityContext context) {
        if (!this.isVariableCounting) {
            Node getNode = context.getConstruction().getVariable(0, Mode.getP());
            context.setResult(new ExpressionResult.Value(context.getConstruction(), getNode));
        }
    }

    @Override
    protected void visit(NewObjectCreation expression, EntityContext context) {
        if (!this.isVariableCounting) {

            Construction construction = context.getConstruction();

            // Calculate size and alignment
            int classSize = this.classSizes.get(expression.getClassReference().getDeclaration());
            Node size = construction.newConst(classSize, Mode.getIs());

            Node oneConst = construction.newConst(1, Mode.getIs());

            // Allocate memory via system call
            Entity functionEntity = this.runtimeEntities.get("alloc_mem");
            assert functionEntity != null : "Runtime library function entity must be present";

            Node functionAddress = construction.newAddress(functionEntity);

            Node mem = construction.getCurrentMem();
            Node call = construction.newCall(mem, functionAddress, new Node[] { oneConst, size }, functionEntity
                            .getType());

            Node newMem = construction.newProj(call, Mode.getM(), Call.pnM);
            construction.setCurrentMem(newMem);

            Node callResultTuple = construction.newProj(call, Mode.getT(), Call.pnTResult);

            Node result = construction.newProj(callResultTuple, Mode.getP(), 0);

            // As alloc_mem guarantees that its memory region is already zero-initialized, we do
            // not need to initialize each field of the type at this point

            context.setResult(new ExpressionResult.Value(context.getConstruction(), result));
        }
    }

    @Override
    protected void visit(NewArrayCreation expression, EntityContext context) {

        if (!this.isVariableCounting) {

            Construction construction = context.getConstruction();

            // Find array size
            expression.getPrimaryDimension().accept(this, context);

            Node elementCount = context.getResult().convertToValue().getNode();

            // Assert correct type hierarchy
            Type arrayPointerType = this.firmTypeFromExpressionType(expression.getType());
            assert (arrayPointerType instanceof PointerType) : "All arrays must be pointer types!";

            Type arrayType = ((PointerType) arrayPointerType).getPointsTo();
            assert (arrayType instanceof ArrayType) : "All arrays must point to array types!";

            Type elementType = ((ArrayType) arrayType).getElementType();

            // Calculate sizes
            Node elementSize = construction.newConst(elementType.getSize(), Mode.getIs());

            // Allocate memory via system call
            Entity functionEntity = this.runtimeEntities.get("alloc_mem");
            assert functionEntity != null : "Runtime library function entity must be present";

            Node functionAddress = construction.newAddress(functionEntity);

            Node mem = construction.getCurrentMem();
            Node call = construction.newCall(mem, functionAddress,
                            new Node[] { elementCount, elementSize }, functionEntity.getType());

            Node newMem = construction.newProj(call, Mode.getM(), Call.pnM);
            construction.setCurrentMem(newMem);

            Node callResultTuple = construction.newProj(call, Mode.getT(), Call.pnTResult);

            Node result = construction.newProj(callResultTuple, Mode.getP(), 0);

            context.setResult(new ExpressionResult.Value(construction, result));
            context.setType(arrayPointerType);
        }
    }

    @Override
    protected void visit(SystemOutPrintlnExpression expression, EntityContext context) {
        if (!this.isVariableCounting) {
            Construction construction = context.getConstruction();

            expression.getArgument().accept(this, context);
            Node argument = context.getResult().convertToValue().getNode();

            Entity functionEntity = this.runtimeEntities.get("system_out_println");
            assert functionEntity != null : "Runtime library function entity must be present";

            Node functionAddress = construction.newAddress(functionEntity);

            Node mem = construction.getCurrentMem();
            Node call = construction.newCall(mem, functionAddress, new Node[] { argument }, functionEntity.getType());

            Node newMem = construction.newProj(call, Mode.getM(), Call.pnM);
            construction.setCurrentMem(newMem);
        }
    }

    @Override
    protected void visit(SystemOutFlushExpression expression, EntityContext context) {
        if (!this.isVariableCounting) {
            Construction construction = context.getConstruction();

            Entity functionEntity = this.runtimeEntities.get("system_out_flush");
            assert functionEntity != null : "Runtime library function entity must be present";

            Node functionAddress = construction.newAddress(functionEntity);

            Node mem = construction.getCurrentMem();
            Node call = construction.newCall(mem, functionAddress, new Node[] {}, functionEntity.getType());

            Node newMem = construction.newProj(call, Mode.getM(), Call.pnM);
            construction.setCurrentMem(newMem);
        }
    }

    @Override
    protected void visit(SystemOutWriteExpression expression, EntityContext context) {
        if (!this.isVariableCounting) {
            Construction construction = context.getConstruction();

            expression.getArgument().accept(this, context);
            Node argument = context.getResult().convertToValue().getNode();

            Entity functionEntity = this.runtimeEntities.get("system_out_write");
            assert functionEntity != null : "Runtime library function entity must be present";

            Node functionAddress = construction.newAddress(functionEntity);

            Node mem = construction.getCurrentMem();
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

            Entity functionEntity = this.runtimeEntities.get("system_in_read");
            assert functionEntity != null : "Runtime library function entity must be present";

            Node functionAddress = construction.newAddress(functionEntity);

            Node mem = construction.getCurrentMem();
            Node call = construction.newCall(mem, functionAddress, new Node[] {}, functionEntity.getType());

            Node newMem = construction.newProj(call, Mode.getM(), Call.pnM);
            construction.setCurrentMem(newMem);

            Node callResults = construction.newProj(call, Mode.getT(), Call.pnTResult);
            result = construction.newProj(callResults, Mode.getIs(), 0);

            context.setResult(new ExpressionResult.Value(context.getConstruction(), result));
        }

    }

    private ExpressionResult.Condition handleShortCircuitedAnd(BinaryOperation expression, EntityContext context) {
        Construction construction = context.getConstruction();

        expression.getLeft().accept(this, context);
        ExpressionResult.Condition left = context.getResult().convertToCondition();

        Block lhsTrueBlock = construction.newBlock();
        lhsTrueBlock.addPred(left.getIfTrue());
        construction.setCurrentBlock(lhsTrueBlock);

        lhsTrueBlock.mature();

        expression.getRight().accept(this, context);
        ExpressionResult.Condition right = context.getResult().convertToCondition();

        Block blockEitherFalse = construction.newBlock();
        construction.setCurrentBlock(blockEitherFalse);
        blockEitherFalse.addPred(left.getIfFalse());
        blockEitherFalse.addPred(right.getIfFalse());

        Node eitherFalseJmp = construction.newJmp();

        blockEitherFalse.mature();

        ExpressionResult.Condition result = new ExpressionResult.Condition(construction, left.getBlock(), right
                        .getIfTrue(), eitherFalseJmp);

        return result;
    }

    private ExpressionResult.Condition handleShortCircuitedOr(BinaryOperation expression, EntityContext context) {
        Construction construction = context.getConstruction();

        expression.getLeft().accept(this, context);
        ExpressionResult.Condition left = context.getResult().convertToCondition();

        Block lhsFalseBlock = construction.newBlock();
        lhsFalseBlock.addPred(left.getIfFalse());
        construction.setCurrentBlock(lhsFalseBlock);

        lhsFalseBlock.mature();

        expression.getRight().accept(this, context);
        ExpressionResult.Condition right = context.getResult().convertToCondition();

        Block blockEitherTrue = construction.newBlock();
        construction.setCurrentBlock(blockEitherTrue);
        blockEitherTrue.addPred(left.getIfTrue());
        blockEitherTrue.addPred(right.getIfTrue());

        Node eitherTrueJump = construction.newJmp();

        blockEitherTrue.mature();

        ExpressionResult.Condition result = new ExpressionResult.Condition(construction, left.getBlock(),
                        eitherTrueJump, right.getIfFalse());

        return result;
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

    private Type firmTypeFromExpressionType(TypeOfExpression type) {

        Type elementType = null;

        assert type.isResolved() : "Unresolved type!";
        BasicTypeDeclaration decl = type.getDeclaration().get();

        String name = decl.getName();

        if (decl.isClassDeclaration()) {
            elementType = new PointerType(new StructType(name));
        }
        else if (decl instanceof PrimitiveTypeDeclaration) {
            // Primitive type
            PrimitiveTypeDeclaration primitiveDeclaration = (PrimitiveTypeDeclaration) decl;

            switch (primitiveDeclaration) {
                case INTEGER:
                    elementType = new PrimitiveType(Mode.getIs());
                    break;
                case VOID:
                case STRING:
                case BOOLEAN:
                    elementType = new PrimitiveType(Mode.getBs());
                    break;
                default:
                    assert false : "Unhandled primitive type!";
                    break;
            }
        }
        else {
            assert false : "Unhandled type declaration";
        }

        elementType.finishLayout();

        if (type.getNumberOfDimensions() > 0) {
            // We have an array type, wrap in pointers according to number of dimensions
            for (int i = 0; i < type.getNumberOfDimensions(); i++) {
                elementType = new PointerType(new ArrayType(elementType, 0));
            }
        }

        return elementType;
    }
}
