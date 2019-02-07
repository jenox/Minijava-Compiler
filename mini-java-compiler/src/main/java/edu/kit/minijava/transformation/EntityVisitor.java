package edu.kit.minijava.transformation;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.nodes.Expression.*;
import edu.kit.minijava.ast.nodes.Program;
import edu.kit.minijava.ast.nodes.Statement.*;
import firm.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EntityVisitor extends ASTVisitor<EntityContext> {
    private CompoundType globalType;
    private String currentClassName;


    private Map<String, Entity> runtimeEntities = new HashMap<>();
    private HashMap<Declaration, Entity> entities = new HashMap<>();
    private HashMap<Declaration, Type> types = new HashMap<>();

    private HashMap<Declaration, HashMap<Declaration, Integer>> method2VariableNums = new HashMap<>();
    private HashMap<Declaration, Integer> variableNums = new HashMap<>();
    private HashMap<MethodDeclaration, Type[]> method2ParamTypes = new HashMap<>();

    private HashMap<ClassDeclaration, Integer> classSizes = new HashMap<>();
    private ClassDeclaration currentClassDeclaration;



    public Map<String, Entity> getRuntimeEntities() {
        return this.runtimeEntities;
    }

    public HashMap<Declaration, Entity> getEntities() {
        return this.entities;
    }

    public HashMap<Declaration, Type> getTypes() {
        return this.types;
    }

    public HashMap<Declaration, HashMap<Declaration, Integer>> getMethod2VariableNums() {
        return this.method2VariableNums;
    }

    public HashMap<MethodDeclaration, Type[]> getMethod2ParamTypes() {
        return this.method2ParamTypes;
    }

    public HashMap<ClassDeclaration, Integer> getClassSizes() {
        return this.classSizes;
    }


    public void startVisit(Program program) throws IOException {
        String[] targetOptions = { "pic=1" };

        // A null target triple causes Firm to choose the host machine triple
        Firm.init(null, targetOptions);

        this.globalType = firm.Program.getGlobalType();

        // Create entities for the runtime library calls
        this.createRuntimeEntities();

        program.accept(this, new EntityContext());
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

        // As MiniJava only supports signed integers, we also use these for the allocation
        // standard library function.

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
        for (ClassDeclaration decl : program.getClassDeclarations()) {
            decl.accept(this, context);
        }
    }

    @Override
    protected void visit(ClassDeclaration classDeclaration, EntityContext context) {
        StructType structType = new StructType(classDeclaration.getName());
        context.setClassType(structType);

        this.types.put(classDeclaration, structType);

        this.currentClassName = classDeclaration.getName();
        this.currentClassDeclaration = classDeclaration;

        for (FieldDeclaration fieldDeclaration : classDeclaration.getFieldDeclarations()) {
            fieldDeclaration.accept(this, context);
        }

        assert this.classSizes.get(classDeclaration) == null;

        // Layout class
        structType.layoutFields();
        structType.finishLayout();

        this.classSizes.put(classDeclaration, structType.getSize());

        for (MethodDeclaration methodDecl : classDeclaration.getMethodDeclarations()) {
            methodDecl.accept(this, context);
        }

        for (MainMethodDeclaration mainMethodDeclaration : classDeclaration.getMainMethodDeclarations()) {
            mainMethodDeclaration.accept(this, context);
        }
    }

    @Override
    protected void visit(FieldDeclaration fieldDeclaration, EntityContext context) {
        fieldDeclaration.getType().accept(this, context);
        this.types.put(fieldDeclaration, context.getType());

        // create entity for method
        String name = this.getUniqueMemberName(fieldDeclaration.getName());
        Entity fieldEntity = new Entity(context.getClassType(), name, context.getType());
        this.entities.put(fieldDeclaration, fieldEntity);
    }

    @Override
    protected void visit(MainMethodDeclaration methodDeclaration, EntityContext context) {
        Type[] parameterTypes = {};
        Type[] resultTypes = {};

        this.variableNums = new HashMap<>();

        MethodType mainMethodType = new MethodType(parameterTypes, resultTypes);

        Entity mainMethodEntity = new Entity(this.globalType, "__minijava_main", mainMethodType);
        this.entities.put(methodDeclaration, mainMethodEntity);

        methodDeclaration.getBody().accept(this, context);


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

        this.variableNums = new HashMap<>();

        this.variableNums.put(this.currentClassDeclaration, 0);

        // GET RETURN TYPE OF THE METHOD
        Type[] resultType = {};

        methodDeclaration.getReturnType().accept(this, context);
        if (!methodDeclaration.getReturnType().isVoid()) {
            resultType = new Type[] { context.getType() };
        }

        this.method2ParamTypes.put(methodDeclaration, parameterTypes);

        // CREATE ENTITY FOR METHOD
        MethodType methodType = new MethodType(parameterTypes, resultType);
        Entity methodEntity = new Entity(this.globalType, this.getUniqueMemberName(methodDeclaration.getName()),
                        methodType);
        this.entities.put(methodDeclaration, methodEntity);

        // COUNT LOCAL VARIABLES
        methodDeclaration.getParameters().forEach(p -> p.accept(this, methodContext));

        methodDeclaration.getBody().accept(this, methodContext);


        this.method2VariableNums.put(methodDeclaration, this.variableNums);
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, EntityContext context) {
        this.variableNums.put(parameterDeclaration, context.getNumberOfLocalVars());
        context.incrementLocalVarCount();

        parameterDeclaration.getType().accept(this, context);
        this.types.put(parameterDeclaration, context.getType());

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
        statement.getStatementIfTrue().accept(this, context);
        statement.getStatementIfFalse().ifPresent(c -> c.accept(this, context));
    }

    @Override
    protected void visit(WhileStatement statement, EntityContext context) {
        statement.getStatementWhileTrue().accept(this, context);
    }

    @Override
    protected void visit(ExpressionStatement statement, EntityContext context) {
        statement.getExpression().accept(this, context);

    }

    @Override
    protected void visit(ReturnStatement statement, EntityContext context) {
        Optional<Expression> returnValue = statement.getValue();
        if (returnValue.isPresent()) {
            returnValue.get().accept(this, context);
        }
    }

    @Override
    protected void visit(EmptyStatement statement, EntityContext context) {
        // Nothing to do here
    }

    @Override
    protected void visit(LocalVariableDeclarationStatement statement, EntityContext context) {
        // Count variable, use old variable count as our index
        this.variableNums.put(statement, context.getNumberOfLocalVars());
        context.incrementLocalVarCount();

        // add variable type
        statement.getType().accept(this, context);
        this.types.put(statement, context.getType());

    }

    @Override
    protected void visit(Statement.Block block, EntityContext context) {
        for (Statement stmt : block.getStatements()) {
            stmt.accept(this, context);

            if (stmt instanceof ReturnStatement) {
                break;
            }
        }
    }

    @Override
    protected void visit(BinaryOperation expression, EntityContext context) {
        expression.getLeft().accept(this, context);
        expression.getRight().accept(this, context);
    }

    @Override
    protected void visit(UnaryOperation expression, EntityContext context) {
        expression.getOther().accept(this, context);
    }

    @Override
    protected void visit(NullLiteral expression, EntityContext context) {
        // nothing to do here
    }

    @Override
    protected void visit(BooleanLiteral expression, EntityContext context) {
        // nothing to do here
    }

    @Override
    protected void visit(IntegerLiteral expression, EntityContext context) {
        // nothing to do here
    }

    @Override
    protected void visit(MethodInvocation expression, EntityContext context) {
    }

    @Override
    protected void visit(ExplicitFieldAccess expression, EntityContext context) {
        // nothing to do here
    }

    @Override
    protected void visit(ArrayElementAccess expression, EntityContext context) {
        expression.getContext().accept(this, context);
    }

    @Override
    protected void visit(VariableAccess expression, EntityContext context) {
        // nothing to do here
    }

    @Override
    protected void visit(CurrentContextAccess expression, EntityContext context) {
        // nothing to do here
    }

    @Override
    protected void visit(NewObjectCreation expression, EntityContext context) {
        // nothing to do here
    }

    @Override
    protected void visit(NewArrayCreation expression, EntityContext context) {
        // nothing to do here
    }

    @Override
    protected void visit(SystemOutPrintlnExpression expression, EntityContext context) {
        // nothing to do here
    }

    @Override
    protected void visit(SystemOutFlushExpression expression, EntityContext context) {
        // nothing to do here
    }

    @Override
    protected void visit(SystemOutWriteExpression expression, EntityContext context) {
        // nothing to do here
    }

    @Override
    protected void visit(SystemInReadExpression expression, EntityContext context) {
        // nothing to do here
    }

    private String getUniqueMemberName(String methodName) {
        return this.currentClassName + "$" + methodName;
    }
}
