package edu.kit.minijava.ast.nodes;

public interface ASTVisitor<T, ExceptionType extends Throwable> {
    void visit(Program program, T context) throws ExceptionType;

    void visit(ClassDeclaration classDeclaration, T context) throws ExceptionType;

    void visit(FieldDeclaration fieldDeclaration, T context) throws ExceptionType;

    void visit(MethodDeclaration methodDeclaration, T context) throws ExceptionType;

    void visit(ParameterDeclaration parameterDeclaration, T context) throws ExceptionType;

    void visit(Statement.IfStatement statement, T context) throws ExceptionType;

    void visit(Statement.WhileStatement statement, T context) throws ExceptionType;

    void visit(Statement.ExpressionStatement statement, T context) throws ExceptionType;

    void visit(Statement.ReturnStatement statement, T context) throws ExceptionType;

    void visit(Statement.EmptyStatement statement, T context) throws ExceptionType;

    void visit(Statement.Block statement, T context) throws ExceptionType;

    void visit(Statement.LocalVariableDeclarationStatement statement, T context) throws ExceptionType;

    void visit(Expression.BinaryOperation expression, T context) throws ExceptionType;

    void visit(Expression.UnaryOperation expression, T context) throws ExceptionType;

    void visit(Expression.NullLiteral expression, T context) throws ExceptionType;

    void visit(Expression.BooleanLiteral expression, T context) throws ExceptionType;

    void visit(Expression.IntegerLiteral expression, T context) throws ExceptionType;

    void visit(Expression.MethodInvocation expression, T context) throws ExceptionType;

    void visit(Expression.ExplicitFieldAccess expression, T context) throws ExceptionType;

    void visit(Expression.ArrayElementAccess expression, T context) throws ExceptionType;

    void visit(Expression.VariableAccess expression, T context) throws ExceptionType;

    void visit(Expression.CurrentContextAccess expression, T context) throws ExceptionType;

    void visit(Expression.NewObjectCreation expression, T context) throws ExceptionType;

    void visit(Expression.NewArrayCreation expression, T context) throws ExceptionType;
}
