package util;

import edu.kit.minijava.parser.AddExpression;
import edu.kit.minijava.parser.ArrayAccess;
import edu.kit.minijava.parser.AssignmentExpression;
import edu.kit.minijava.parser.BasicType;
import edu.kit.minijava.parser.Block;
import edu.kit.minijava.parser.BlockStatement;
import edu.kit.minijava.parser.BooleanLiteral;
import edu.kit.minijava.parser.BooleanType;
import edu.kit.minijava.parser.ClassDeclaration;
import edu.kit.minijava.parser.ClassMember;
import edu.kit.minijava.parser.DivideExpression;
import edu.kit.minijava.parser.EmptyStatement;
import edu.kit.minijava.parser.EqualToExpression;
import edu.kit.minijava.parser.Expression;
import edu.kit.minijava.parser.ExpressionStatement;
import edu.kit.minijava.parser.Field;
import edu.kit.minijava.parser.FieldAccess;
import edu.kit.minijava.parser.GreaterThanExpression;
import edu.kit.minijava.parser.GreaterThanOrEqualToExpression;
import edu.kit.minijava.parser.IdentifierAndArgumentsExpression;
import edu.kit.minijava.parser.IdentifierExpression;
import edu.kit.minijava.parser.IfElseStatement;
import edu.kit.minijava.parser.IfStatement;
import edu.kit.minijava.parser.IntegerLiteral;
import edu.kit.minijava.parser.IntegerType;
import edu.kit.minijava.parser.LessThanExpression;
import edu.kit.minijava.parser.LessThanOrEqualToExpression;
import edu.kit.minijava.parser.LocalVariableDeclarationStatement;
import edu.kit.minijava.parser.LocalVariableInitializationStatement;
import edu.kit.minijava.parser.LogicalAndExpression;
import edu.kit.minijava.parser.LogicalNotExpression;
import edu.kit.minijava.parser.LogicalOrExpression;
import edu.kit.minijava.parser.MainMethod;
import edu.kit.minijava.parser.Method;
import edu.kit.minijava.parser.MethodInvocation;
import edu.kit.minijava.parser.ModuloExpression;
import edu.kit.minijava.parser.MultiplyExpression;
import edu.kit.minijava.parser.NegateExpression;
import edu.kit.minijava.parser.NewArrayExpression;
import edu.kit.minijava.parser.NewObjectExpression;
import edu.kit.minijava.parser.NotEqualToExpression;
import edu.kit.minijava.parser.NullLiteral;
import edu.kit.minijava.parser.Parameter;
import edu.kit.minijava.parser.PostfixExpression;
import edu.kit.minijava.parser.PostfixOperation;
import edu.kit.minijava.parser.Program;
import edu.kit.minijava.parser.PropagatedException;
import edu.kit.minijava.parser.ReturnNoValueStatement;
import edu.kit.minijava.parser.ReturnValueStatement;
import edu.kit.minijava.parser.SubtractExpression;
import edu.kit.minijava.parser.ThisExpression;
import edu.kit.minijava.parser.Type;
import edu.kit.minijava.parser.UserDefinedType;
import edu.kit.minijava.parser.VoidType;
import edu.kit.minijava.parser.WhileStatement;

public interface INodeVisitor {

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
    
    public void visit(PropagatedException propagatedException);
    
    public void visit(ReturnNoValueStatement returnNoValueStatement);
    
    public void visit(ReturnValueStatement returnValueStatement);
    
    public void visit(SubtractExpression subtractExpression);
    
    public void visit(ThisExpression thisExpression);
    
    public void visit(Type type);
    
    public void visit(UserDefinedType userDefinedType);
    
    public void visit(VoidType voidType);
    
    public void visit(WhileStatement whileStatement);
}
