package edu.kit.minijava.cli;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.kit.minijava.parser.AddExpression;
import edu.kit.minijava.parser.ArrayAccess;
import edu.kit.minijava.parser.AssignmentExpression;
import edu.kit.minijava.parser.Block;
import edu.kit.minijava.parser.BooleanLiteral;
import edu.kit.minijava.parser.BooleanType;
import edu.kit.minijava.parser.ClassDeclaration;
import edu.kit.minijava.parser.ClassMember;
import edu.kit.minijava.parser.DivideExpression;
import edu.kit.minijava.parser.EmptyStatement;
import edu.kit.minijava.parser.EqualToExpression;
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
import util.INodeVisitor;

public class ParserPrinter implements INodeVisitor {
    
    private int depth;
    private Program program;
    
    public final static String INDENT = "\t";
    
    public ParserPrinter(Program program) {
        this.program = program;
        depth = 0;
    }
    
    public void print() {
        visit(program);
    }

    @Override
    public void visit(AddExpression addExpression) {
        printWhitespace();
        System.out.println("AddExpression");
        depth++;
        addExpression.left.accept(this);
        addExpression.right.accept(this);
        depth--;
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        printWhitespace();
        System.out.println("ArrayAccess");
        depth++;
        arrayAccess.index.accept(this);
        depth--;
    }

    @Override
    public void visit(AssignmentExpression assignmentExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(Block block) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(BooleanType booleanType) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ClassDeclaration classDeclaration) {
        printWhitespace();
        System.out.println("ClassDeclaration (" + classDeclaration.className + ")");
        depth++;
        List<ClassMember> members = new ArrayList<>(classDeclaration.members.size());
        for (ClassMember member : classDeclaration.members) {
            members.add(member);
        }
        //sort members
        members.sort(new Comparator<ClassMember>() {

            @Override
            public int compare(ClassMember o1, ClassMember o2) {
                if (o1.isMethod()) {
                    if (o2.isMethod()) {
                        return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
                    } else {
                        //o1 is method, o2 is not
                        return 1;
                    }
                } else {
                    if (o2.isMethod()) {
                        //o2 is method, o1 is not
                        return -1;
                    } else {
                        //both are not a method
                        return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
                    }
                }
            }
        });
        
        for (ClassMember member : members) {
            member.accept(this);
        }
        depth--;

    }

    @Override
    public void visit(DivideExpression divideExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(EmptyStatement emptyStatement) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(EqualToExpression equalToExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ExpressionStatement expressionStatement) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(Field field) {
        printWhitespace();
        System.out.println("Field (" + field.name + ")");
        depth++;
        field.type.accept(this);
        depth--;

    }

    @Override
    public void visit(FieldAccess fieldAccess) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(GreaterThanOrEqualToExpression greaterThanOrEqualToExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(IdentifierAndArgumentsExpression identifierAndArgumentsExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(IdentifierExpression identifierExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(IfStatement ifStatement) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(IntegerLiteral integerLiteral) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(IntegerType integerType) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LessThanExpression lessThanExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LessThanOrEqualToExpression lessThanOrEqualToExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LocalVariableDeclarationStatement localVariableDeclarationStatement) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LocalVariableInitializationStatement localVariableInitializationStatement) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LogicalAndExpression logicalAndExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LogicalNotExpression logicalNotExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LogicalOrExpression logicalOrExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(MainMethod mainMethod) {
        printWhitespace();
        System.out.println("Main method (" + mainMethod.name + ", " + mainMethod.argumentsParameterName + ")");
        depth++;
        mainMethod.body.accept(this);
        depth--;
    }

    @Override
    public void visit(Method method) {
        printWhitespace();
        System.out.println("Method (" + method.name + ")");
        depth++;
        //print return type first
        method.returnType.accept(this);
        //print parameters
        for (Parameter param : method.parameters) {
            param.accept(this);
        }
        //print block
        method.body.accept(this);
        depth--;
    }

    @Override
    public void visit(MethodInvocation methodInvocation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ModuloExpression moduloExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(MultiplyExpression multiplyExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(NegateExpression negateExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(NewArrayExpression newArrayExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(NewObjectExpression newObjectExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(NotEqualToExpression notEqualToExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(NullLiteral nullLiteral) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(Parameter parameter) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(PostfixExpression postfixExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(PostfixOperation postfixOperation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(Program program) {
        printWhitespace();
        System.out.println("Program");
        depth++;
        List<ClassDeclaration> classes = new ArrayList<>(program.classDeclarations.size());
        for (ClassDeclaration declaration : program.classDeclarations) {
            classes.add(declaration);
        }
        
        //sort classes by name
        classes.sort(new Comparator<ClassDeclaration>() {

            @Override
            public int compare(ClassDeclaration o1, ClassDeclaration o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.className, o2.className);
            }
        });
        
        for (ClassDeclaration declaration : classes) {
            declaration.accept(this);
        }
        depth--;
        
        
    }

    @Override
    public void visit(PropagatedException propagatedException) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ReturnNoValueStatement returnNoValueStatement) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ReturnValueStatement returnValueStatement) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(SubtractExpression subtractExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ThisExpression thisExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(Type type) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(UserDefinedType userDefinedType) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(VoidType voidType) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(WhileStatement whileStatement) {
        // TODO Auto-generated method stub

    }
    
    /**
     * prints depth number of times tab without linebreak at end
     */
    private void printWhitespace() {
        for (int i = 0; i < depth; i++) {
            System.out.print(INDENT);
        }
    }

}
