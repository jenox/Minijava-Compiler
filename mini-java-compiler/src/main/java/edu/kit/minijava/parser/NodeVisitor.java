package edu.kit.minijava.parser;

public interface NodeVisitor {

    public void visit(AddExpression addExpression);
    
    public void visit(ArrayAccess arrayAccess);
    
    public void visit(AssignmentExpression assignmentExpression);
    
    public void visit(Block block);
    
    public void visit(BooleanLiteral booleanLiteral);
    
    public void visit(BooleanType booleanType);
    
    public void visit(ClassDeclaration classDeclaration);
    
    public void visit(DivideExpression divideExpression);
    
    public void visit(EmptyStatement emptyStatement);
    
    public void visit(EqualToExpression equalToExpression);
    
    public void visit(ExpressionStatement expressionStatement);
    
    public void visit(Field field);
    
    public void visit(FieldAccess fieldAccess);
    
    public void visit(GreaterThanExpression greaterThanExpression);
    
    public void visit(GreaterThanOrEqualToExpression greaterThanOrEqualToExpression);
    
    public void visit(IdentifierAndArgumentsExpression identifierAndArgumentsExpression);
    
    public void visit(IdentifierExpression identifierExpression);
    
    public void visit(IfElseStatement ifElseStatement);
    
    public void visit(IfStatement ifStatement);
    
    public void visit(IntegerLiteral integerLiteral);
    
    public void visit(IntegerType integerType);
    
    public void visit(LessThanExpression lessThanExpression);
    
    public void visit(LessThanOrEqualToExpression lessThanOrEqualToExpression);
    
    public void visit(LocalVariableDeclarationStatement localVariableDeclarationStatement);
    
    public void visit(LocalVariableInitializationStatement localVariableInitializationStatement);
    
    public void visit(LogicalAndExpression logicalAndExpression);
    
    public void visit(LogicalNotExpression logicalNotExpression);
    
    public void visit(LogicalOrExpression logicalOrExpression);
    
    public void visit(MainMethod mainMethod);
    
    public void visit(Method method);
    
    public void visit(MethodInvocation methodInvocation);
    
    public void visit(ModuloExpression moduloExpression);
    
    public void visit(MultiplyExpression multiplyExpression);
    
    public void visit(NegateExpression negateExpression);
    
    public void visit(NewArrayExpression newArrayExpression);
    
    public void visit(NewObjectExpression newObjectExpression);
    
    public void visit(NotEqualToExpression notEqualToExpression);
    
    public void visit(NullLiteral nullLiteral);
    
    public void visit(Parameter parameter);
    
    public void visit(PostfixExpression postfixExpression);
    
    public void visit(Program program);
    
    public void visit(ReturnNoValueStatement returnNoValueStatement);
    
    public void visit(ReturnValueStatement returnValueStatement);
    
    public void visit(SubtractExpression subtractExpression);
    
    public void visit(ThisExpression thisExpression);
    
    public void visit(Type type);
    
    public void visit(UserDefinedType userDefinedType);
    
    public void visit(VoidType voidType);
    
    public void visit(WhileStatement whileStatement);
}