package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;

public class MemberCollector implements ASTVisitor<MemberCollector.Options, SemanticAnalysisException> {

    public enum Options {}

    public void collectMembers(Program program) throws SemanticAnalysisException {
        if (program == null) throw new IllegalArgumentException();

        program.getClassSymbolTable().clear();

        this.visit(program, null);
    }

    @Override
    public void visit(Program program, Options context) throws SemanticAnalysisException {

        // Collect all defined classes first, then visit them each separately
        for (ClassDeclaration declaration : program.getClassDeclarations()) {

            if (program.getClassSymbolTable().containsKey(declaration.getName())) {
                throw new RedeclarationException(declaration, null);
            }

            program.getClassSymbolTable().put(declaration.getName(), declaration);
        }

        // Afterwards, visit each class separately:
        // We cannot interleave this because else not all defined types may be collected yet at this point.
        for (ClassDeclaration declaration : program.getClassDeclarations()) {
            this.visit(declaration, null);
        }
    }

    @Override
    public void visit(ClassDeclaration classDeclaration, Options context) throws SemanticAnalysisException {

        // Collect methods and fields separately
        for (MethodDeclaration methodDeclaration : classDeclaration.getMethodDeclarations()) {

            String methodName = methodDeclaration.getName();

            if (classDeclaration.getMethodSymbolTable().containsKey(methodName)) {
                throw new RedeclarationException(methodDeclaration, methodDeclaration.getLocation());
            }

            classDeclaration.getMethodSymbolTable().put(methodName, methodDeclaration);
        }

        // Collect fields
        for (FieldDeclaration fieldDeclaration : classDeclaration.getFieldDeclarations()) {
            String fieldName = fieldDeclaration.getName();

            if (classDeclaration.getFieldSymbolTable().containsKey(fieldName)) {
                throw new RedeclarationException(fieldDeclaration, fieldDeclaration.getLocation());
            }

            classDeclaration.getFieldSymbolTable().put(fieldName, fieldDeclaration);
        }
    }

    // All other methods do not need to do anything
    // TODO Do we want to make implementing all methods in the visitor optional?

    @Override
    public void visit(FieldDeclaration fieldDeclaration, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(MethodDeclaration methodDeclaration, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(ParameterDeclaration parameterDeclaration, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Statement.IfStatement statement, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Statement.WhileStatement statement, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Statement.ExpressionStatement statement, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Statement.ReturnStatement statement, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Statement.EmptyStatement statement, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Statement.Block statement, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Statement.LocalVariableDeclarationStatement statement, Options context)
        throws SemanticAnalysisException {

    }

    @Override
    public void visit(Expression.BinaryOperation expression, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Expression.UnaryOperation expression, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Expression.NullLiteral expression, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Expression.BooleanLiteral expression, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Expression.IntegerLiteral expression, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Expression.MethodInvocation expression, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Expression.ExplicitFieldAccess expression, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Expression.ArrayElementAccess expression, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Expression.VariableAccess expression, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Expression.CurrentContextAccess expression, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Expression.NewObjectCreation expression, Options context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(Expression.NewArrayCreation expression, Options context) throws SemanticAnalysisException {

    }

}
