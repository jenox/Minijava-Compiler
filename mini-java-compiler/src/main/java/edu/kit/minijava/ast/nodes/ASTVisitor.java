package edu.kit.minijava.ast.nodes;

public abstract class ASTVisitor<T> {
    protected void visit(Program program, T context) {}
    protected void visit(ClassDeclaration classDeclaration, T context) {}
    protected void visit(FieldDeclaration fieldDeclaration, T context) {}
    protected void visit(MethodDeclaration methodDeclaration, T context) {}
    protected void visit(ParameterDeclaration parameterDeclaration, T context) {}

    protected void visit(Statement.IfStatement statement, T context) {}
    protected void visit(Statement.WhileStatement statement, T context) {}
    protected void visit(Statement.ExpressionStatement statement, T context) {}
    protected void visit(Statement.ReturnStatement statement, T context) {}
    protected void visit(Statement.EmptyStatement statement, T context) {}
    protected void visit(Statement.LocalVariableDeclarationStatement statement, T context) {}
    protected void visit(Statement.Block block, T context) {}

    protected void visit(Expression.BinaryOperation expression, T context) {}
    protected void visit(Expression.UnaryOperation expression, T context) {}
    protected void visit(Expression.NullLiteral expression, T context) {}
    protected void visit(Expression.BooleanLiteral expression, T context) {}
    protected void visit(Expression.IntegerLiteral expression, T context) {}
    protected void visit(Expression.MethodInvocation expression, T context) {}
    protected void visit(Expression.ExplicitFieldAccess expression, T context) {}
    protected void visit(Expression.ArrayElementAccess expression, T context) {}
    protected void visit(Expression.VariableAccess expression, T context) {}
    protected void visit(Expression.CurrentContextAccess expression, T context) {}
    protected void visit(Expression.NewObjectCreation expression, T context) {}
    protected void visit(Expression.NewArrayCreation expression, T context) {}
}
