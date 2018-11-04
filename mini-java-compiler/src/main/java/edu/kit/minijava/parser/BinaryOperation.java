package edu.kit.minijava.parser;

import edu.kit.minijava.lexer.*;
import edu.kit.minijava.ast.*;

enum BinaryOperation {
    MULTIPLICATION(TokenType.MULTIPLY, Associativity.LEFT_ASSOCIATIVE, 50),
    DIVISION(TokenType.DIVIDE, Associativity.LEFT_ASSOCIATIVE, 50),
    MODULO(TokenType.MODULO, Associativity.LEFT_ASSOCIATIVE, 50),
    ADDITION(TokenType.PLUS, Associativity.LEFT_ASSOCIATIVE, 40),
    SUBTRACTION(TokenType.MINUS, Associativity.LEFT_ASSOCIATIVE, 40),
    LESS_THAN(TokenType.LESS_THAN, Associativity.LEFT_ASSOCIATIVE, 20),
    LESS_THAN_OR_EQUAL_TO(TokenType.LESS_THAN_OR_EQUAL_TO, Associativity.LEFT_ASSOCIATIVE, 20),
    GREATER_THAN(TokenType.GREATER_THAN, Associativity.LEFT_ASSOCIATIVE, 20),
    GREATER_THAN_OR_EQUAL_TO(TokenType.GREATER_THAN_OR_EQUAL_TO, Associativity.LEFT_ASSOCIATIVE, 20),
    EQUAL_TO(TokenType.EQUAL_TO, Associativity.LEFT_ASSOCIATIVE, 15),
    NOT_EQUAL_TO(TokenType.NOT_EQUAL_TO, Associativity.LEFT_ASSOCIATIVE, 15),
    LOGICAL_AND(TokenType.LOGICAL_AND, Associativity.LEFT_ASSOCIATIVE, 10),
    LOGICAL_OR(TokenType.LOGICAL_OR, Associativity.LEFT_ASSOCIATIVE, 5),
    ASSIGNMENT(TokenType.ASSIGN, Associativity.RIGHT_ASSOCIATIVE, 0);

    BinaryOperation(TokenType tokenType, Associativity associativity, int precedence) {
        this.tokenType = tokenType;
        this.associativity = associativity;
        this.precedence = precedence;
    }

    final TokenType tokenType;
    final Associativity associativity;
    final int precedence;

    Expression instantiate(Expression lhs, Expression rhs) {
        switch (this) {
            case MULTIPLICATION: return new MultiplyExpression(lhs, rhs);
            case DIVISION: return new DivideExpression(lhs, rhs);
            case MODULO: return new ModuloExpression(lhs, rhs);
            case ADDITION: return new AddExpression(lhs, rhs);
            case SUBTRACTION: return new SubtractExpression(lhs, rhs);
            case LESS_THAN: return new LessThanExpression(lhs, rhs);
            case LESS_THAN_OR_EQUAL_TO: return new LessThanOrEqualToExpression(lhs, rhs);
            case GREATER_THAN: return new GreaterThanExpression(lhs, rhs);
            case GREATER_THAN_OR_EQUAL_TO: return new GreaterThanOrEqualToExpression(lhs, rhs);
            case EQUAL_TO: return new EqualToExpression(lhs, rhs);
            case NOT_EQUAL_TO: return new NotEqualToExpression(lhs, rhs);
            case LOGICAL_AND: return new LogicalAndExpression(lhs, rhs);
            case LOGICAL_OR: return new LogicalOrExpression(lhs, rhs);
            case ASSIGNMENT: return new AssignmentExpression(lhs, rhs);
            default: throw new AssertionError();
        }
    }

    static BinaryOperation forTokenType(TokenType tokenType) {
        for (BinaryOperation operation : BinaryOperation.values()) {
            if (operation.tokenType == tokenType) {
                return operation;
            }
        }

        return null;
    }
}
