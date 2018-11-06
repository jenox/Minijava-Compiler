package edu.kit.minijava.ast2.nodes;

public interface ASTVisitor {
    void visit(Program program);
    void visit(ClassDeclaration classDeclaration);
    void visit(FieldDeclaration fieldDeclaration);
    void visit(MethodDeclaration methodDeclaration);
    void visit(ParameterDeclaration parameterDeclaration);

    void visit(Statement.IfStatement statement);
    void visit(Statement.WhileStatement statement);
    void visit(Statement.ExpressionStatement statement);
    void visit(Statement.ReturnStatement statement);
    void visit(Statement.EmptyStatement statement);
    void visit(Statement.Block statement);
    void visit(Statement.LocalVariableDeclarationStatement statement);

    void visit(Expression.BinaryOperation expression);
    void visit(Expression.UnaryOperation expression);
    void visit(Expression.NullLiteral expression);
    void visit(Expression.BooleanLiteral expression);
    void visit(Expression.IntegerLiteral expression);
    void visit(Expression.MethodInvocation expression);
    void visit(Expression.ExplicitFieldAccess expression);
    void visit(Expression.ArrayElementAccess expression);
    void visit(Expression.VariableAccess expression);
    void visit(Expression.CurrentContextAccess expression);
    void visit(Expression.NewObjectCreation expression);
    void visit(Expression.NewArrayCreation expression);
}
