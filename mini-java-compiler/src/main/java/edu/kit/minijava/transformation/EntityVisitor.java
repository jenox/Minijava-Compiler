package edu.kit.minijava.transformation;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.nodes.Expression.*;
import edu.kit.minijava.ast.nodes.Program;
import edu.kit.minijava.ast.nodes.Statement.*;
import firm.*;

public class EntityVisitor extends ASTVisitor<EntityContext> {

    private CompoundType globalType;

    private String currentClassName;

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

    }

    @Override
    protected void visit(FieldDeclaration fieldDeclaration, EntityContext context) {

        // TODO

    }

    @Override
    protected void visit(MainMethodDeclaration methodDeclaration, EntityContext context) {

        Type[] parameterTypes = new Type[methodDeclaration.getParameterTypes().size()];

        int count = 0;
        for (TypeReference ref : methodDeclaration.getParameterTypes()) {
            EntityContext entContext = new EntityContext();
            ref.accept(this, entContext);

            parameterTypes[count] = entContext.getType();
            count++;
        }

        Type voidType = new PrimitiveType(Mode.getBs());
        Type[] resultTypes = { voidType };
        MethodType mainMethodType = new MethodType(parameterTypes, resultTypes);
        Entity mainMethodEntity = new Entity(this.globalType, this.getUniqueMemberName(methodDeclaration.getName()),
                mainMethodType);
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

    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, EntityContext context) {
        parameterDeclaration.getType().accept(this, context);
    }

    @Override
    protected void visit(ExplicitTypeReference reference, EntityContext context) {

        Type firmType = null;

        BasicTypeDeclaration decl = reference.getBasicTypeReference().getDeclaration();

        switch ((PrimitiveTypeDeclaration) decl) {
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

        // TODO: genauso wie ExplicitTypeReference behandeln?

    }

    @Override
    protected void visit(IfStatement statement, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(WhileStatement statement, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(ExpressionStatement statement, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(ReturnStatement statement, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(EmptyStatement statement, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(LocalVariableDeclarationStatement statement, EntityContext context) {
        statement.getType().accept(this, context);
    }

    @Override
    protected void visit(Block block, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(BinaryOperation expression, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(UnaryOperation expression, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(NullLiteral expression, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(BooleanLiteral expression, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(IntegerLiteral expression, EntityContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(MethodInvocation expression, EntityContext context) {
        // TODO Auto-generated method stub

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
