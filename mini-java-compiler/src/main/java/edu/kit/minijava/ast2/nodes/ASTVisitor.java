package edu.kit.minijava.ast2.nodes;

public interface ASTVisitor<T> {
    void visit(Program program, T context);
    void visit(ClassDeclaration classDeclaration, T context);
    void visit(FieldDeclaration fieldDeclaration, T context);
    void visit(MethodDeclaration methodDeclaration, T context);
    void visit(ParameterDeclaration parameterDeclaration, T context);

    void visit(Statement.IfStatement statement, T context);
    void visit(Statement.WhileStatement statement, T context);
    void visit(Statement.ExpressionStatement statement, T context);
    void visit(Statement.ReturnStatement statement, T context);
    void visit(Statement.EmptyStatement statement, T context);
    void visit(Statement.Block statement, T context);
    void visit(Statement.LocalVariableDeclarationStatement statement, T context);

    void visit(Expression.BinaryOperation expression, T context);
    void visit(Expression.UnaryOperation expression, T context);
    void visit(Expression.NullLiteral expression, T context);
    void visit(Expression.BooleanLiteral expression, T context);
    void visit(Expression.IntegerLiteral expression, T context);
    void visit(Expression.MethodInvocation expression, T context);
    void visit(Expression.ExplicitFieldAccess expression, T context);
    void visit(Expression.ArrayElementAccess expression, T context);
    void visit(Expression.VariableAccess expression, T context);
    void visit(Expression.CurrentContextAccess expression, T context);
    void visit(Expression.NewObjectCreation expression, T context);
    void visit(Expression.NewArrayCreation expression, T context);
}
