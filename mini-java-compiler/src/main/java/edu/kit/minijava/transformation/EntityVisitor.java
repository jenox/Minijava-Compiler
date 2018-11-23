                    package edu.kit.minijava.transformation;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.nodes.Expression.*;
import edu.kit.minijava.ast.nodes.Program;
import edu.kit.minijava.ast.nodes.Statement.*;
import firm.*;
import firm.nodes.Block;
import firm.nodes.Node;
import firm.nodes.NodeVisitor;
import firm.nodes.Pin;

import java.util.HashMap;

public class EntityVisitor extends ASTVisitor<EntityContext> {

    private CompoundType globalType;

    private String currentClassName;

    private HashMap<String, Integer> variableNums = new HashMap<>();
    private HashMap<String, Entity> methodEntities = new HashMap<>();

    private boolean isVariableCounting = false;

    public void transform(Program program) {
        String[] targetOptions = { "pic=1" };
        Firm.init("x86_64-linux-gnu", targetOptions);

        program.accept(this);
    }

    @Override
    protected void visit(Program program, EntityContext context) {

        // create global type
        this.globalType = firm.Program.getGlobalType();

        for (ClassDeclaration decl : program.getClassDeclarations()) {
            decl.accept(this);
        }
    }

    @Override
    protected void visit(ClassDeclaration classDeclaration, EntityContext context) {

        this.currentClassName = classDeclaration.getName();

        StructType structType = new StructType(classDeclaration.getName());
        Entity entity = new Entity(this.globalType, classDeclaration.getName(), structType);

        for (MainMethodDeclaration mainMethodDeclaration : classDeclaration.getMainMethodDeclarations()) {
            mainMethodDeclaration.accept(this, context);
        }

        for (MethodDeclaration methodDecl : classDeclaration.getMethodDeclarations()) {
            methodDecl.accept(this, context);
        }

    }

    @Override
    protected void visit(FieldDeclaration fieldDeclaration, EntityContext context) {
        // TODO: fill this
    }

    @Override
    protected void visit(MainMethodDeclaration methodDeclaration, EntityContext context) {

        Type[] parameterTypes = new Type[0];

        int count = 0;
        /*for (TypeReference ref : methodDeclaration.getParameterTypes()) {
            EntityContext entContext = new EntityContext();
            ref.accept(this, entContext);

            parameterTypes[count] = entContext.getType();
            count++;
        }*/

        Type voidType = new PrimitiveType(Mode.getBs());
        Type[] resultTypes = { voidType };
        MethodType mainMethodType = new MethodType(parameterTypes, resultTypes);
        Entity mainMethodEntity = new Entity(this.globalType, this.getUniqueMemberName(methodDeclaration.getName()),
                        mainMethodType);

        EntityContext varCountContext = new EntityContext();
        this.isVariableCounting = true;
        methodDeclaration.getBody().accept(this, varCountContext);

        Graph graph = new Graph(mainMethodEntity, varCountContext.getNumberOfLocalVars());
        Construction construction = new Construction(graph);
        construction.finish();
        Dump.dumpGraph(graph, "after-construction");
        System.out.println("Dumped graph for main-method");

        // TODO: change main method to be like the other methods

    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, EntityContext context) {

        Type[] parameterTypes = new Type[methodDeclaration.getParameterTypes().size()];

        int count = 0;
        for (TypeReference paramRef : methodDeclaration.getParameterTypes()) {
            EntityContext entContext = new EntityContext();
            paramRef.accept(this, entContext);

            parameterTypes[count] = entContext.getType();
            count++;
        }

        EntityContext returnContext = new EntityContext();
        methodDeclaration.getReturnType().accept(this, returnContext);
        Type[] resultType = { returnContext.getType() };

        MethodType methodType = new MethodType(parameterTypes, resultType);
        Entity methodEntity = new Entity(this.globalType, this.getUniqueMemberName(methodDeclaration.getName()),
                        methodType);
        methodEntities.put(this.getUniqueMemberName(methodDeclaration.getName()), methodEntity);

        EntityContext varCountContext = new EntityContext();
        methodDeclaration.getBody().accept(this, varCountContext);

        Graph graph = new Graph(methodEntity, parameterTypes.length + varCountContext.getNumberOfLocalVars());
        Construction construction = new Construction(graph);

        Block oldBlock = construction.getCurrentBlock();
        construction.setCurrentBlock(graph.getStartBlock());

        for (int i = 0; i < parameterTypes.length; i++) {
            Type paramType = parameterTypes[i];

            Node node = construction.newProj(graph.getArgs(), paramType.getMode(), i);
            // TODO: add projection to start node
        }

        EntityContext nodeContext = new EntityContext(construction);
        methodDeclaration.getBody().accept(this, nodeContext);

        Node mem = construction.getCurrentMem();
        // TODO: using body result, IS THIS CORRECT?
        //Node[] results = { nodeContext.getResult() };
        Node[] results = { construction.newConst(0, Mode.getBs()) };
        Node returnNode = construction.newReturn(mem, results);
        graph.getEndBlock().addPred(returnNode);
        construction.getCurrentBlock().mature();

        construction.setCurrentBlock(oldBlock);

        construction.finish();

        Dump.dumpGraph(construction.getGraph(), "after-construction");
        System.out.println("Dumped graph for method");
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, EntityContext context) {
        parameterDeclaration.getType().accept(this, context);
        // TODO: make this analog to variable declaration
    }

    @Override
    protected void visit(ExplicitTypeReference reference, EntityContext context) {

        Type firmType = null;

        reference.isVoid();
        BasicTypeDeclaration decl = reference.getBasicTypeReference().getDeclaration();
        PrimitiveTypeDeclaration decl_ = (PrimitiveTypeDeclaration) decl;

        switch (decl_) {
        case INTEGER:
            firmType = new PrimitiveType(Mode.getIs());
            context.setType(firmType);
            return;
        case VOID:
        case STRING:
        case BOOLEAN:
            firmType = new PrimitiveType(Mode.getBs());
            context.setType(firmType);
            return;
        default:
            break;
        }

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

            firmType = new ArrayType(elementType, reference.getNumberOfDimensions());
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
        statement.getCondition().accept(this, context);
        Node selector = context.getResult();

        // TODO: does table need to be set?
        Node ifNode = null;

        if (!this.isVariableCounting)
            ifNode = context.getConstruction().newSwitch(selector, 2, null);

        context.setResult(ifNode);
    }

    @Override
    protected void visit(WhileStatement statement, EntityContext context) {
        Node selector = context.getResult();

        // TODO: does table need to be set?
        Node whileNode = null;

        if (!this.isVariableCounting)
            whileNode = context.getConstruction().newSwitch(selector, 2, null);

        context.setResult(whileNode);
    }

    @Override
    protected void visit(ExpressionStatement statement, EntityContext context) {
        statement.getExpression().accept(this, context);
    }

    @Override
    protected void visit(ReturnStatement statement, EntityContext context) {
        statement.getValue().ifPresent(e -> e.accept(this, context));
    }

    @Override
    protected void visit(EmptyStatement statement, EntityContext context) {
        // nothing to do here
    }

    @Override
    protected void visit(LocalVariableDeclarationStatement statement, EntityContext context) {
        //statement.getType().accept(this, context);
        statement.getValue().ifPresent(e -> e.accept(this, context));

        // use old variable count as our index
        if (!this.isVariableCounting) {
            context.getConstruction().setVariable(context.getNumberOfLocalVars(), context.getResult());
            variableNums.put(statement.getName(), context.getNumberOfLocalVars());
        }

        context.incrementLocalVarCount();
    }

    @Override
    protected void visit(edu.kit.minijava.ast.nodes.Statement.Block block, EntityContext context) {
        // TODO: does this order make sense or not?
        Node blockNode = null;

        if (!this.isVariableCounting)
            blockNode = context.getConstruction().newBlock();

        for (Statement stmt : block.getStatements()) {
            stmt.accept(this, context);
        }

        context.setResult(blockNode);
    }

    @Override
    protected void visit(BinaryOperation expression, EntityContext context) {
        expression.getLeft().accept(this, context);
        Node left = context.getResult();
        expression.getRight().accept(this, context);
        Node right = context.getResult();

        Node bin = null;

        if (!this.isVariableCounting)
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
                    // TODO: no idea where to get this pin state from
                    bin = context.getConstruction().newDiv(context.getConstruction().getCurrentMem(), left, right, null);
                    break;
                case MODULO:
                    // TODO: no idea where to get this pin state from
                    bin = context.getConstruction().newMod(context.getConstruction().getCurrentMem(), left, right, null);
                    break;
                    // TODO: I dont know how to get assignment
                default:
                    break;
            }

        context.setResult(bin);
    }

    @Override
    protected void visit(UnaryOperation expression, EntityContext context) {
        // TODO Auto-generated method stub
        expression.getOther().accept(this, context);
        Node otherNode = context.getResult();

        Node uni = null;

        if (!this.isVariableCounting)
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

        context.setResult(uni);
    }

    @Override
    protected void visit(NullLiteral expression, EntityContext context) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void visit(BooleanLiteral expression, EntityContext context) {
        Node bool = null;

        if (!this.isVariableCounting)
            if (expression.getValue()) {
                bool = context.getConstruction().newConst(TargetValue.getBTrue());
            } else {
                bool = context.getConstruction().newConst(TargetValue.getBTrue());
            }

        context.setResult(bool);
    }

    @Override
    protected void visit(IntegerLiteral expression, EntityContext context) {
        TargetValue tarval = new TargetValue(Integer.valueOf(expression.getValue()), Mode.getIs());
        Node lit = null;

        if(!this.isVariableCounting)
            lit = context.getConstruction().newConst(tarval);

        context.setResult(lit);
    }

    @Override
    protected void visit(MethodInvocation expression, EntityContext context) {
        Node result = null;
        if (!this.isVariableCounting) {
            Node argsSize = context.getConstruction().newConst(expression.getArguments().size(), Mode.getIs());
            Node mem = context.getConstruction().getCurrentMem();
            Node in = context.getConstruction().newAlloc(mem, argsSize, 0);

            // TODO: should there be additional allocations here?
            // TODO: and should they be added to `in`?
            for (int i = 0; i < expression.getArguments().size(); i++) {
                expression.getArguments().get(i).accept(this, context);
            }

            Entity methodEntity = methodEntities.get(expression.getMethodReference().getName());
            Node callee = context.getConstruction().newAddress(methodEntity);
            Node callNode = context.getConstruction().newCall(mem, callee, new Node[] { in }, methodEntity.getType());

            int pn_Call_M = 0;
            Node newStore = context.getConstruction().newProj(callNode, Mode.getM(), pn_Call_M);
            context.getConstruction().setCurrentMem(newStore);

            int pn_Call_T_result = 0;
            Node tuple = context.getConstruction().newProj(callNode, Mode.getT(), pn_Call_T_result);
            result = context.getConstruction().newProj(tuple, Mode.getIs(), 0);
        }

        context.setResult(result);
    }

    @Override
    protected void visit(ExplicitFieldAccess expression, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(ArrayElementAccess expression, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(VariableAccess expression, EntityContext context) {
        Node var = null;

        if (!this.isVariableCounting) {
            Node mem = context.getConstruction().getCurrentMem();
            int n = variableNums.get(expression.getVariableReference().getName());
            // TODO: how to know, which mode to use?
            // TODO: maybe derive the mode from the type of the expression?
            var = context.getConstruction().getVariable(n, Mode.getIs());
        }

        context.setResult(var);
    }

    @Override
    protected void visit(CurrentContextAccess expression, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(NewObjectCreation expression, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(NewArrayCreation expression, EntityContext context) {
        // TODO Auto-generated method stub

    }

    public String getUniqueMemberName(String methodName) {
        return this.currentClassName + "." + methodName;
    }

}
