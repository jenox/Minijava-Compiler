package edu.kit.minijava.parser;

import edu.kit.minijava.lexer.*;
import edu.kit.minijava.ast.nodes.*;

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

    private final TokenType tokenType;
    private final Associativity associativity;
    private final int precedence;

    public TokenType getTokenType() {
        return this.tokenType;
    }

    public Associativity getAssociativity() {
        return this.associativity;
    }

    public int getPrecedence() {
        return this.precedence;
    }

    private BinaryOperationType getBinaryOperationType() {
        switch (this) {
            case MULTIPLICATION: return BinaryOperationType.MULTIPLICATION;
            case DIVISION: return BinaryOperationType.DIVISION;
            case MODULO: return BinaryOperationType.MODULO;
            case ADDITION: return BinaryOperationType.ADDITION;
            case SUBTRACTION: return BinaryOperationType.SUBTRACTION;
            case LESS_THAN: return BinaryOperationType.LESS_THAN;
            case LESS_THAN_OR_EQUAL_TO: return BinaryOperationType.LESS_THAN_OR_EQUAL_TO;
            case GREATER_THAN: return BinaryOperationType.GREATER_THAN;
            case GREATER_THAN_OR_EQUAL_TO: return BinaryOperationType.GREATER_THAN_OR_EQUAL_TO;
            case EQUAL_TO: return BinaryOperationType.EQUAL_TO;
            case NOT_EQUAL_TO: return BinaryOperationType.NOT_EQUAL_TO;
            case LOGICAL_AND: return BinaryOperationType.LOGICAL_AND;
            case LOGICAL_OR: return BinaryOperationType.LOGICAL_OR;
            case ASSIGNMENT: return BinaryOperationType.ASSIGNMENT;
            default: throw new AssertionError();
        }
    }

    Expression.BinaryOperation instantiate(Expression lhs, Expression rhs, TokenLocation location) {
        return new Expression.BinaryOperation(this.getBinaryOperationType(), lhs, rhs, location);
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
