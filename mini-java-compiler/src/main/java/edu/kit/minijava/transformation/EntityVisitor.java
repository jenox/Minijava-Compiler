    package edu.kit.minijava.transformation;

import java.util.HashMap;

import com.sun.jna.Pointer;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.nodes.Expression.*;
import edu.kit.minijava.ast.nodes.Program;
import edu.kit.minijava.ast.nodes.Statement.*;
import edu.kit.minijava.ast.references.TypeOfExpression;
import firm.*;
import firm.bindings.binding_ircons;
import firm.nodes.*;

public class EntityVisitor extends ASTVisitor<EntityContext> {

    private CompoundType globalType;
    private String currentClassName;

    private HashMap<Declaration, Integer> variableNums = new HashMap<>();
    private HashMap<Declaration, Entity> entities = new HashMap<>();
    private HashMap<Declaration, Type> types = new HashMap<>();
    // TODO: add hashmap of object sizes

    private boolean isVariableCounting = false;


    public void transform(Program program) {
        String[] targetOptions = { "pic=1" };
        Firm.init("x86_64-linux-gnu", targetOptions);

        this.globalType = firm.Program.getGlobalType();
        program.accept(this, new EntityContext());

        // Check and dump created graphs - can be viewed with ycomp
        for (Graph g : firm.Program.getGraphs()) {
            g.check();
            // Should produce a file calc(II)I.vcg
            Dump.dumpGraph(g, "");
        }
    }

    @Override
    protected void visit(Program program, EntityContext context) {
        for (ClassDeclaration decl : program.getClassDeclarations()) {
            decl.accept(this, context);
        }
    }

    @Override
    protected void visit(ClassDeclaration classDeclaration, EntityContext context) {
        StructType structType = new StructType(classDeclaration.getName());
        context.setClassType(structType);

        this.currentClassName = classDeclaration.getName();
        this.variableNums.put(classDeclaration, 0);
        this.types.put(classDeclaration, structType);

        // TODO: calc size of class here
        for (FieldDeclaration fieldDeclaration : classDeclaration.getFieldDeclarations()) {
            fieldDeclaration.accept(this, context);
        }

        for (MainMethodDeclaration mainMethodDeclaration : classDeclaration.getMainMethodDeclarations()) {
            mainMethodDeclaration.accept(this, context);
        }

        for (MethodDeclaration methodDecl : classDeclaration.getMethodDeclarations()) {
            methodDecl.accept(this, context);
        }
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

        Type voidType = new PrimitiveType(Mode.getBs());
        Type[] resultTypes = { voidType };

        MethodType mainMethodType = new MethodType(parameterTypes, resultTypes);
        // TODO: should main have globaltype or classtype as parent type?
        Entity mainMethodEntity = new Entity(this.globalType, this.getUniqueMemberName(methodDeclaration.getName()),
                        mainMethodType);
        this.entities.put(methodDeclaration, mainMethodEntity);

        this.isVariableCounting = true;
        methodDeclaration.getBody().accept(this, context);

        this.isVariableCounting = false;
        Graph graph = new Graph(mainMethodEntity, context.getNumberOfLocalVars());
        Construction construction = new Construction(graph);


        // traverse body and create code
        context.setConstruction(construction);
        methodDeclaration.getBody().accept(this, context);

        Node mem = construction.getCurrentMem();
        Node[] results = { construction.newDummy(Mode.getBs()) };
        Node returnNode = construction.newReturn(mem, results);
        graph.getEndBlock().addPred(returnNode);

        // No code should follow a return statement.
        construction.setUnreachable();
        // Done.
        construction.finish();
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

        // GET RETURN TYPE OF THE METHOD
        methodDeclaration.getReturnType().accept(this, context);
        Type[] resultType = { context.getType() };

        // CREATE ENTITY FOR METHOD
        MethodType methodType = new MethodType(parameterTypes, resultType);
        Entity methodEntity = new Entity(this.globalType, this.getUniqueMemberName(methodDeclaration.getName()),
                        methodType);
        this.entities.put(methodDeclaration, methodEntity);

        // COUNT LOCAL VARIABLES
        context.setNumberOfLocalVars(1);
        methodDeclaration.getParameters().forEach(p -> p.accept(this, context));
        this.isVariableCounting = true;
        methodDeclaration.getBody().accept(this, context);

        this.isVariableCounting = false;
        Graph graph = new Graph(methodEntity, context.getNumberOfLocalVars());
        Construction construction = new Construction(graph);

        // set variables and parameters
        Node projThis = construction.newProj(graph.getArgs(), Mode.getP(), 0);
        construction.setVariable(0, projThis);

        for (int i = 1; i < parameterTypes.length; i++) {
            Type paramType = parameterTypes[i];

            Node paramProj = construction.newProj(graph.getArgs(), paramType.getMode(), i);
            construction.setVariable(i, paramProj);
        }

        // traverse body and create code
        context.setConstruction(construction);
        methodDeclaration.getBody().accept(this, context);

        // no return stmts so far? -> create return node
        if (graph.getEndBlock().getPredCount() == 0) {
            Node mem = construction.getCurrentMem();
            Node[] results = { construction.newDummy(Mode.getBs()) };
            Node returnNode = construction.newReturn(mem, results);
            graph.getEndBlock().addPred(returnNode);
        }

        // No code should follow a return statement.
        construction.setUnreachable();
        // Done.
        construction.finish();
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, EntityContext context) {
        this.variableNums.put(parameterDeclaration, context.getNumberOfLocalVars());

        parameterDeclaration.getType().accept(this, context);
        this.types.put(parameterDeclaration, context.getType());
        context.incrementLocalVarCount();

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
                    elementType = new StructType(name);
            }
            this.types.put(reference.getBasicTypeReference().getDeclaration(), elementType);

            for (int i = 0; i < reference.getNumberOfDimensions(); i++) {
                elementType = new ArrayType(elementType, 0);
            }

            firmType = elementType;
        } else if (!decl.isClassDeclaration()) {

            PrimitiveTypeDeclaration decl_ = (PrimitiveTypeDeclaration) decl;

            switch (decl_) {
                case INTEGER:
                    firmType = new PrimitiveType(Mode.getIs());
                    break;
                case VOID:
                case STRING:
                    firmType = new PrimitiveType(Mode.getBs());
                    break;
                case BOOLEAN:
                    firmType = new PrimitiveType(Mode.getb());
                    break;
                default:
                    break;
            }
        }
        // user defined type
        else {
            firmType = new StructType(name);
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
            statement.getCondition().accept(this, context);
            Node cond = context.getConstruction().newCond(context.getResult());

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
        } else {
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
            Node cond = construction.newCond(context.getResult());
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
        } else {
            statement.getStatementWhileTrue().accept(this, context);
        }
    }

    @Override
    protected void visit(ExpressionStatement statement, EntityContext context) {
        statement.getExpression().accept(this, context);
    }

    @Override
    protected void visit(ReturnStatement statement, EntityContext context) {
        statement.getValue().ifPresent(e -> e.accept(this, context));

        if (!this.isVariableCounting) {
            Node[] results = {};
            Node mem = context.getConstruction().getCurrentMem();

            // TODO: check this
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
                context.getConstruction().setVariable(num, context.getResult());
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
        }
    }

    @Override
    protected void visit(BinaryOperation expression, EntityContext context) {
        context.setLeftSideOfAssignment(expression.getOperationType() == BinaryOperationType.ASSIGNMENT);
        expression.getLeft().accept(this, context);
        Node left = context.getResult();
        Declaration decl = context.getDecl();
        context.setLeftSideOfAssignment(false);

        expression.getRight().accept(this, context);
        Node right = context.getResult();

        Node bin = null;


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
                case LOGICAL_OR:
                    bin = context.getConstruction().newOr(left, right);
                    break;
                case LOGICAL_AND:
                    bin = context.getConstruction().newAnd(left, right);
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
                    if (decl instanceof LocalVariableDeclarationStatement) {
                        int num = this.variableNums.get(decl);
                        int leftDim = ((LocalVariableDeclarationStatement) decl).getType().getNumberOfDimensions();
                        int rightDim = expression.getType().getNumberOfDimensions();

                        if (leftDim == 1 && rightDim == 0) {
                            Node arrayPointer = context.getConstruction().getVariable(num, Mode.getP());
                            mem = context.getConstruction().getCurrentMem();

                            Node store = context.getConstruction().newStore(mem, arrayPointer, right);
                            Node newMem = context.getConstruction().newProj(store, Mode.getM(), Store.pnM);
                            context.getConstruction().setCurrentMem(newMem);
                        } else {
                            context.getConstruction().setVariable(num, right);
                        }

                        bin = right;
                    }
                    else if (decl instanceof FieldDeclaration) {
                        Entity field = this.entities.get(decl);
                        Node thisNode = context.getConstruction().getVariable(0, Mode.getP());
                        Node member = context.getConstruction().newMember(thisNode, field);
                        mem = context.getConstruction().getCurrentMem();

                        Node store = context.getConstruction().newStore(mem, member, right);
                        Node newMem = context.getConstruction().newProj(store, Mode.getM(), Store.pnM);
                        context.getConstruction().setCurrentMem(newMem);

                        bin = right;
                    }
                default:
                    break;
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
            switch (expression.getOperationType()) {
                case LOGICAL_NEGATION:
                    uni = context.getConstruction().newNot(otherNode);
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
        Node result = context.getConstruction().newUnknown(Mode.getP());
        context.setResult(result);
    }

    @Override
    protected void visit(BooleanLiteral expression, EntityContext context) {
        Node bool = null;

        if (!this.isVariableCounting) {
            if (expression.getValue()) {
                bool = context.getConstruction().newConst(TargetValue.getBTrue());
            }
            else {
                bool = context.getConstruction().newConst(TargetValue.getBFalse());
            }
        }

        context.setResult(bool);
    }

    @Override
    protected void visit(IntegerLiteral expression, EntityContext context) {
        if (!this.isVariableCounting) {
            int val = Integer.valueOf(expression.getValue());
            TargetValue tarval = new TargetValue(Integer.valueOf(expression.getValue()), Mode.getIs());
            Node lit = null;
            lit = context.getConstruction().newConst(tarval);
            context.setResult(lit);
        }
    }

    @Override
    protected void visit(MethodInvocation expression, EntityContext context) {
        Construction construction = context.getConstruction();
        Graph graph = construction.getGraph();
        Node result = null;

        if (!this.isVariableCounting) {
            Node argsSize = context.getConstruction().newConst(expression.getArguments().size(), Mode.getIs());
            Node mem = context.getConstruction().getCurrentMem();
            Node[] in = new Node[expression.getArguments().size() + 1];
            in[0] = construction.newProj(graph.getArgs(), Mode.getP(), 0);

            for (int i = 1; i < expression.getArguments().size(); i++) {
                expression.getArguments().get(i).accept(this, context);
                in[i] = context.getResult();
            }

            Entity methodEntity = this.entities.get(expression.getMethodReference().getDeclaration());
            Node callee = context.getConstruction().newAddress(methodEntity);
            Node callNode = context.getConstruction().newCall(mem, callee, in, methodEntity.getType());

            Node newStore = context.getConstruction().newProj(callNode, Mode.getM(), Call.pnM);
            context.getConstruction().setCurrentMem(newStore);

            Node tuple = context.getConstruction().newProj(callNode, Mode.getT(), Call.pnTResult);
            result = context.getConstruction().newProj(tuple, Mode.getIs(), 0);
        }

        context.setResult(result);
    }

    @Override
    protected void visit(ExplicitFieldAccess expression, EntityContext context) {
        if (!this.isVariableCounting) {
            expression.getContext().accept(this, context);

            Entity field = this.entities.get(expression.getFieldReference().getDeclaration());
            Node address = context.getConstruction().newAddress(field);
            Node mem = context.getConstruction().getCurrentMem();
            Mode mode = this.types.get(expression.getFieldReference().getDeclaration()).getMode();

            Node load = context.getConstruction().newLoad(mem, address, mode);
            Node newMem = context.getConstruction().newProj(load, Mode.getM(), Load.pnM);
            Node result = context.getConstruction().newProj(load, mode, Load.pnRes);

            context.getConstruction().setCurrentMem(newMem);
            context.setResult(result);
        }
    }

    @Override
    protected void visit(ArrayElementAccess expression, EntityContext context) {
        context.setResult(null);
        // TODO: is there another way to get the array pointer?
        if (!this.isVariableCounting && !context.isLeftSideOfAssignment()) {
            // for sel stmt
            expression.getContext().accept(this, context);
            int num = this.variableNums.get(context.getDecl());
            Node arrayPointer = context.getConstruction().getVariable(num, Mode.getP());
            Type type = this.types.get(context.getDecl());
            Type elementType = this.types.get(expression.getContext().getType().getDeclaration().get());

            // for sel stmt
            expression.getIndex().accept(this, context);
            Node index = context.getResult();

            // access array
            Node sel = context.getConstruction().newSel(arrayPointer, index, type);
            Node mem = context.getConstruction().getCurrentMem();
            Node load = context.getConstruction().newLoad(mem, sel, elementType.getMode());
            Node resM = context.getConstruction().newProj(load, Mode.getM(), Load.pnM);
            Node result = context.getConstruction().newProj(load, elementType.getMode(), Load.pnRes);

            context.getConstruction().setCurrentMem(resM);
            context.setResult(result);
        } else {
            expression.getContext().accept(this, context);
        }
    }

    @Override
    protected void visit(VariableAccess expression, EntityContext context) {
        context.setResult(null);
        Declaration decl = expression.getVariableReference().getDeclaration();
        context.setDecl(decl);
        if (!this.isVariableCounting && !context.isLeftSideOfAssignment()) {

            if (decl instanceof LocalVariableDeclarationStatement || decl instanceof ParameterDeclaration) {
                Node mem  = context.getConstruction().getCurrentMem();
                int n     = this.variableNums.get(decl);
                Type type = this.types.get(decl);
                Mode mode = type.getMode();

                if (mode == null) {
                    mode = Mode.getP();
                }

                Node getNode = context.getConstruction().getVariable(n, mode);

                context.setResult(getNode);
            } else if (decl instanceof FieldDeclaration) {
                // TODO: do we even need to visit the context?
                //expression.getContext().accept(this, context);

                Entity field = this.entities.get(decl);
                Node thisNode = context.getConstruction().getVariable(0, Mode.getP());
                Node member = context.getConstruction().newMember(thisNode, field);
                Node mem = context.getConstruction().getCurrentMem();

                Mode mode = this.types.get(decl).getMode();
                if (mode == null) {
                    mode = Mode.getP();
                }

                Node load = context.getConstruction().newLoad(mem, member, mode);
                Node newMem = context.getConstruction().newProj(load, Mode.getM(), Load.pnM);
                Node result = context.getConstruction().newProj(load, mode, Load.pnRes);

                context.getConstruction().setCurrentMem(newMem);
                context.setResult(result);
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
            Node mem = context.getConstruction().getCurrentMem();
            // TODO: calculate correct size here
            Node size = context.getConstruction().newConst(1, Mode.getIu());
            int alignment = this.types.get(expression.getClassReference().getDeclaration()).getAlignment();

            Node alloc = context.getConstruction().newAlloc(mem, size, alignment);
            Node newMem = context.getConstruction().newProj(alloc, Mode.getM(), Alloc.pnM);
            Node res = context.getConstruction().newProj(alloc, Mode.getP(), Alloc.pnRes);

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

    }

    @Override
    protected void visit(SystemOutFlushExpression expression, EntityContext context) {

    }

    @Override
    protected void visit(SystemOutWriteExpression expression, EntityContext context) {

    }

    @Override
    protected void visit(SystemInReadExpression expression, EntityContext context) {

    }

    public String getUniqueMemberName(String methodName) {
        return this.currentClassName + "." + methodName;
    }
}
