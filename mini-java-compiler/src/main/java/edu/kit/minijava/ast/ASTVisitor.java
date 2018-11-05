package edu.kit.minijava.ast;

public interface ASTVisitor {
    void visit(Program program);
    void visit(ClassDeclaration classDeclaration);
    void visit(Field field);
    void visit(MainMethod mainMethod);
    void visit(Method method);
    void visit(Parameter parameter);

    void visit(Type type);
    void visit(IntegerType type);
    void visit(VoidType type);
    void visit(BooleanType type);
    void visit(UserDefinedType type);

    void visit(Block block);
    void visit(EmptyStatement statement);
    void visit(IfStatement statement);
    void visit(IfElseStatement statement);
    void visit(ExpressionStatement statement);
    void visit(WhileStatement statement);
    void visit(ReturnNoValueStatement statement);
    void visit(ReturnValueStatement statement);
    void visit(LocalVariableDeclarationStatement statement);
    void visit(LocalVariableInitializationStatement statement);

    void visit(AssignmentExpression expression);
    void visit(LogicalOrExpression expression);
    void visit(LogicalAndExpression expression);
    void visit(LogicalNotExpression expression);
    void visit(EqualToExpression expression);
    void visit(NotEqualToExpression expression);
    void visit(LessThanExpression expression);
    void visit(LessThanOrEqualToExpression expression);
    void visit(GreaterThanExpression expression);
    void visit(GreaterThanOrEqualToExpression expression);
    void visit(AddExpression expression);
    void visit(SubtractExpression expression);
    void visit(MultiplyExpression expression);
    void visit(DivideExpression expression);
    void visit(ModuloExpression expression);
    void visit(NegateExpression expression);
    void visit(PostfixExpression expression);
    void visit(MethodInvocation operation);
    void visit(FieldAccess operation);
    void visit(ArrayAccess operation);
    void visit(NullLiteral literal);
    void visit(BooleanLiteral literal);
    void visit(IntegerLiteral literal);
    void visit(IdentifierExpression expression);
    void visit(IdentifierAndArgumentsExpression expression);
    void visit(ThisExpression expression);
    void visit(NewObjectExpression expression);
    void visit(NewArrayExpression expression);
}
