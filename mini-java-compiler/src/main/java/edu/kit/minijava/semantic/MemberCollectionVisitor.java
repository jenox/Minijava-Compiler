package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberCollectionVisitor implements ASTVisitor<MemberCollectionVisitor.Options> {

    public enum Options {}

//    private SymbolTable<VariableDeclaration> variableSymbolTable = new SymbolTable<>();
//    private Program astRoot = null;

    private final List<SemanticAnalysisException> encounteredProblems = new ArrayList<>();

    public void collectMembers(Program program) throws SemanticAnalysisException {
        if (program == null) throw new IllegalArgumentException();

//        this.astRoot = program;

//        this.variableSymbolTable = new SymbolTable<>();

        program.getClassSymbolTable().clear();

        this.visit(program, null);

        // Check whether any Exceptions were stored
        if (!encounteredProblems.isEmpty()) {
            // Throw the first exception that occurred while visiting the AST
            throw encounteredProblems.get(0);
        }
    }

    @Override
    public void visit(Program program, Options context) {

        // Collect all defined classes first, then visit them each separately
        for (ClassDeclaration declaration : program.getClassDeclarations()) {

            if (program.getClassSymbolTable().containsKey(declaration.getName())) {
                encounteredProblems.add(new RedeclarationException(declaration, null));
                return;
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
    public void visit(ClassDeclaration classDeclaration, Options context) {

        // Collect methods and fields separately
        for (MethodDeclaration methodDeclaration : classDeclaration.getMethodDeclarations()) {

            String methodName = methodDeclaration.getName();

            if (classDeclaration.getMethodSymbolTable().containsKey(methodName)) {
                this.encounteredProblems.add(new RedeclarationException(methodDeclaration, methodDeclaration.getLocation()));
                continue;
            }

            classDeclaration.getMethodSymbolTable().put(methodName, methodDeclaration);
        }

        // Collect fields
        for (FieldDeclaration fieldDeclaration : classDeclaration.getFieldDeclarations()) {
            String fieldName = fieldDeclaration.getName();

            if (classDeclaration.getMethodSymbolTable().containsKey(fieldName)) {
                this.encounteredProblems.add(new RedeclarationException(fieldDeclaration, fieldDeclaration.getLocation()));
                continue;
            }

            classDeclaration.getFieldSymbolTable().put(fieldName, fieldDeclaration);
        }
    }

    // All other methods do not need to do anything
    // TODO Find a more elegant way to collect all members (do we even need a visitor here?)

    @Override
    public void visit(FieldDeclaration fieldDeclaration, Options context) {

    }

    @Override
    public void visit(MethodDeclaration methodDeclaration, Options context) {

    }

    @Override
    public void visit(ParameterDeclaration parameterDeclaration, Options context) {

    }

    @Override
    public void visit(Statement.IfStatement statement, Options context) {

    }

    @Override
    public void visit(Statement.WhileStatement statement, Options context) {

    }

    @Override
    public void visit(Statement.ExpressionStatement statement, Options context) {

    }

    @Override
    public void visit(Statement.ReturnStatement statement, Options context) {

    }

    @Override
    public void visit(Statement.EmptyStatement statement, Options context) {

    }

    @Override
    public void visit(Statement.Block statement, Options context) {

    }

    @Override
    public void visit(Statement.LocalVariableDeclarationStatement statement, Options context) {

    }

    @Override
    public void visit(Expression.BinaryOperation expression, Options context) {

    }

    @Override
    public void visit(Expression.UnaryOperation expression, Options context) {

    }

    @Override
    public void visit(Expression.NullLiteral expression, Options context) {

    }

    @Override
    public void visit(Expression.BooleanLiteral expression, Options context) {

    }

    @Override
    public void visit(Expression.IntegerLiteral expression, Options context) {

    }

    @Override
    public void visit(Expression.MethodInvocation expression, Options context) {

    }

    @Override
    public void visit(Expression.ExplicitFieldAccess expression, Options context) {

    }

    @Override
    public void visit(Expression.ArrayElementAccess expression, Options context) {

    }

    @Override
    public void visit(Expression.VariableAccess expression, Options context) {

    }

    @Override
    public void visit(Expression.CurrentContextAccess expression, Options context) {

    }

    @Override
    public void visit(Expression.NewObjectCreation expression, Options context) {

    }

    @Override
    public void visit(Expression.NewArrayCreation expression, Options context) {

    }

}
