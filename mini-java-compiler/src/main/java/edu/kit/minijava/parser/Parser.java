package edu.kit.minijava.parser;

import edu.kit.minijava.lexer.*;
import java.util.*;

public final class Parser {

    // MARK: - Initialization

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = lexer.nextToken();
    }

    private final Lexer lexer;

    // MARK: - Parsing

    private Token currentToken; // nullable

    private void advance() {
        if (this.currentToken == null) {
            throw new IllegalStateException();
        }

        this.currentToken = this.lexer.nextToken();
    }

    private boolean check(TokenType type) {
        if (this.currentToken == null) {
            return false;
        } else {
            return currentToken.type == type;
        }
    }

    private Token consume(TokenType type) {
        Token token = this.currentToken;

        if (token.type != type) {
            throw new RuntimeException();
        }

        this.advance();

        return token;
    }

    // MARK: - Parsing MiniJava Files

    public Program parseProgram() {
        throw new UnsupportedOperationException();
    }

    // MARK: - Parsing Statements

    public Statement parseStatement() {
        throw new UnsupportedOperationException();
    }

    // MARK: - Parsing Types

    public Type parseType() {
        BasicType basicType = this.parseBasicType();

        int dim = 0;

        while (this.check(TokenType.OPENING_BRACKET)) {
            this.consume(TokenType.OPENING_BRACKET);
            this.consume(TokenType.CLOSING_BRACKET);
            dim += 1;
        }

        return new Type(basicType, dim);
    }

    public BasicType parseBasicType() {
        switch (this.currentToken.type) {
            case INT:
                this.consume(TokenType.INT);
                return new IntegerType();
            case BOOLEAN:
                this.consume(TokenType.BOOLEAN);
                return new BooleanType();
            case VOID:
                this.consume(TokenType.VOID);
                return new VoidType();
            case IDENTIFIER:
                Token token = this.consume(TokenType.IDENTIFIER);
                return new UserDefinedType(token.text);
            default:
                throw new RuntimeException("Bad BasicType");
        }
    }

    public Parameter parseParameter() {
        Type type = this.parseType();
        Token token = this.consume(TokenType.IDENTIFIER);
        return new Parameter(type, token.text);
    }

    public List<Parameter> parseParameters() {
        List<Parameter> parameter_list = new ArrayList<Parameter>();
        parameter_list.add(this.parseParameter());
        while (this.check(TokenType.COMMA)) {
            this.consume(TokenType.COMMA);
            parameter_list.add(this.parseParameter());
        }
        return parameter_list;
    }

    // MARK: - Parsing Expressions

    public Expression parseExpression() {
        return this.parseAssignmentExpression();
    }

    private Expression parseAssignmentExpression() {
        Expression expression = this.parseLogicalOrExpression();

        if (this.check(TokenType.ASSIGN)) {
            return new AssignmentExpression(expression, this.parseAssignmentExpression());
        } else {
            return expression;
        }
    }

    private Expression parseLogicalOrExpression() {
        Expression expression = this.parseLogicalAndExpression();

        while (this.currentToken != null) {
            switch (this.currentToken.type) {
                case LOGICAL_OR:
                    this.consume(TokenType.LOGICAL_OR);
                    expression = new LogicalOrExpression(expression, this.parseLogicalAndExpression());
                    break;
                default:
                    return expression;
            }
        }

        return expression;
    }

    private Expression parseLogicalAndExpression() {
        Expression expression = this.parseEqualityExpression();

        while (this.currentToken != null) {
            switch (this.currentToken.type) {
                case LOGICAL_AND:
                    this.consume(TokenType.LOGICAL_AND);
                    expression = new LogicalAndExpression(expression, this.parseEqualityExpression());
                    break;
                default:
                    return expression;
            }
        }

        return expression;
    }

    private Expression parseEqualityExpression() {
        Expression expression = this.parseRelationalExpression();

        while (this.currentToken != null) {
            switch (this.currentToken.type) {
                case EQUAL_TO:
                    this.consume(TokenType.EQUAL_TO);
                    expression = new EqualToExpression(expression, this.parseRelationalExpression());
                    break;
                case NOT_EQUAL_TO:
                    this.consume(TokenType.NOT_EQUAL_TO);
                    expression = new NotEqualToExpression(expression, this.parseRelationalExpression());
                    break;
                default:
                    return expression;
            }
        }

        return expression;
    }

    private Expression parseRelationalExpression() {
        Expression expression = this.parseAdditiveExpression();

        while (this.currentToken != null) {
            switch (this.currentToken.type) {
                case LESS_THAN:
                    this.consume(TokenType.LESS_THAN);
                    expression = new LessThanExpression(expression, this.parseAdditiveExpression());
                    break;
                case LESS_THAN_OR_EQUAL_TO:
                    this.consume(TokenType.LESS_THAN_OR_EQUAL_TO);
                    expression = new LessThanOrEqualToExpression(expression, this.parseAdditiveExpression());
                    break;
                case GREATER_THAN:
                    this.consume(TokenType.GREATER_THAN);
                    expression = new GreaterThanExpression(expression, this.parseAdditiveExpression());
                    break;
                case GREATER_THAN_OR_EQUAL_TO:
                    this.consume(TokenType.GREATER_THAN_OR_EQUAL_TO);
                    expression = new GreaterThanOrEqualToExpression(expression, this.parseAdditiveExpression());
                    break;
                default:
                    return expression;
            }
        }

        return expression;
    }

    private Expression parseAdditiveExpression() {
        Expression expression = this.parseMultiplicativeExpression();

        while (this.currentToken != null) {
            switch (this.currentToken.type) {
                case PLUS:
                    this.consume(TokenType.PLUS);
                    expression = new AddExpression(expression, this.parseMultiplicativeExpression());
                    break;
                case MINUS:
                    this.consume(TokenType.MINUS);
                    expression = new SubtractExpression(expression, this.parseMultiplicativeExpression());
                    break;
                default:
                    return expression;
            }
        }

        return expression;
    }

    private Expression parseMultiplicativeExpression() {
        Expression expression = this.parseUnaryExpression();

        while (this.currentToken != null) {
            switch (this.currentToken.type) {
                case MULTIPLY:
                    this.consume(TokenType.MULTIPLY);
                    expression = new MultiplyExpression(expression, this.parseUnaryExpression());
                    break;
                case DIVIDE:
                    this.consume(TokenType.DIVIDE);
                    expression = new DivideExpression(expression, this.parseUnaryExpression());
                    break;
                case MODULO:
                    this.consume(TokenType.MODULO);
                    expression = new ModuloExpression(expression, this.parseUnaryExpression());
                    break;
                default:
                    return expression;
            }
        }

        return expression;
    }

    private Expression parseUnaryExpression() {
        if (this.currentToken == null) {
            throw new RuntimeException();
        }

        switch (this.currentToken.type) {
            case NULL:
            case TRUE:
            case FALSE:
            case INTEGER_LITERAL:
            case IDENTIFIER:
            case THIS:
            case OPENING_PARENTHESIS:
            case NEW:
                return this.parsePostfixExpression();
            case LOGICAL_NEGATION:
                return new LogicalNotExpression(this.parseUnaryExpression());
            case MINUS:
                return new NegateExpression(this.parseUnaryExpression());
            default:
                throw new RuntimeException();
        }
    }

    private Expression parsePostfixExpression() {
        Expression expression = this.parsePrimaryExpression();

        if (this.currentToken == null) {
            return expression;
        }

        switch (this.currentToken.type) {
            case PERIOD:
            case OPENING_BRACKET:
                return new PostfixExpression(expression, this.parsePostfixOperation());
            default:
                return expression;
        }
    }

    private PostfixOperation parsePostfixOperation() {
        if (this.check(TokenType.PERIOD)) {
            this.consume(TokenType.PERIOD);
            String identifier = this.consume(TokenType.IDENTIFIER).text;

            if (this.check(TokenType.OPENING_PARENTHESIS)) {
                this.consume(TokenType.OPENING_PARENTHESIS);
                List<Expression> arguments = this.parseArguments();
                this.consume(TokenType.CLOSING_PARENTHESIS);

                return new MethodInvocation(identifier, arguments);
            } else {
                return new FieldAccess(identifier);
            }
        } else if (this.check(TokenType.OPENING_BRACKET)) {
            this.consume(TokenType.OPENING_BRACKET);
            Expression expression = this.parseExpression();
            this.consume(TokenType.CLOSING_BRACKET);

            return new ArrayAccess(expression);
        } else {
            throw new RuntimeException();
        }
    }

    private List<Expression> parseArguments() {
        TokenType[] first = { TokenType.LOGICAL_NEGATION, TokenType.MINUS,  TokenType.NULL, TokenType.TRUE,
                TokenType.FALSE, TokenType.INTEGER_LITERAL, TokenType.IDENTIFIER, TokenType.THIS,
                TokenType.OPENING_PARENTHESIS, TokenType.NEW };
        List<Expression> exp_list = new ArrayList<>();

        if (this.currentToken == null) {
            throw new RuntimeException();
        }

        if (Arrays.asList(first).contains(this.currentToken.type)) {
            exp_list.add(this.parseExpression());
            while (this.check(TokenType.COMMA)) {
                this.consume(TokenType.COMMA);
                exp_list.add(this.parseExpression());
            }
        }

        return exp_list;
    }

    private Expression parsePrimaryExpression() {
        switch (this.currentToken.type) {
            case NULL:
                this.consume(TokenType.NULL);
                return new NullLiteral();
            case FALSE:
                this.consume(TokenType.FALSE);
                return new BooleanLiteral(false);
            case TRUE:
                this.consume(TokenType.TRUE);
                return new BooleanLiteral(true);
            case INTEGER_LITERAL: {
                String value = this.consume(TokenType.INTEGER_LITERAL).text;
                try {
                    return new IntegerLiteral(Integer.parseInt(value));
                } catch (NumberFormatException exception) {
                    throw new RuntimeException();
                }
            }
            case IDENTIFIER: {
                String identifier = this.consume(TokenType.IDENTIFIER).text;
                if (this.check(TokenType.OPENING_PARENTHESIS)) {
                    this.consume(TokenType.OPENING_PARENTHESIS);
                    List<Expression> arguments = this.parseArguments();
                    this.consume(TokenType.CLOSING_PARENTHESIS);
                    return new IdentifierAndArgumentsExpression(identifier, arguments);
                } else {
                    return new IdentifierExpression(identifier);
                }
            }
            case THIS:
                this.consume(TokenType.THIS);
                return new ThisExpression();
            case OPENING_PARENTHESIS:
                this.consume(TokenType.OPENING_PARENTHESIS);
                Expression expression = this.parseExpression();
                this.consume(TokenType.CLOSING_PARENTHESIS);
                return expression;
            case NEW:
                this.consume(TokenType.NEW);

                if (this.currentToken == null) {
                    throw new RuntimeException();
                }

                BasicType basicType = null;

                switch (this.currentToken.type) {
                    case IDENTIFIER:
                        Token token2 = this.consume(TokenType.IDENTIFIER);
                        if (this.check(TokenType.OPENING_PARENTHESIS)) {
                            this.consume(TokenType.OPENING_PARENTHESIS);
                            this.consume(TokenType.CLOSING_PARENTHESIS);
                            return new NewObjectExpression(token2.text);
                        } else if (this.check(TokenType.OPENING_BRACKET)) {
                            this.consume(TokenType.OPENING_BRACKET);
                            basicType = new UserDefinedType(token2.text);
                        }
                    case INT:
                    case BOOLEAN:
                    case VOID:
                        if (basicType == null) {
                            basicType = this.parseBasicType();
                            this.consume(TokenType.OPENING_BRACKET);
                        }
                        Expression expression2 = this.parseExpression();
                        this.consume(TokenType.CLOSING_BRACKET);
                        int dim = 1;
                        while (this.check(TokenType.OPENING_BRACKET)) {
                            this.consume(TokenType.OPENING_BRACKET);
                            this.consume(TokenType.CLOSING_BRACKET);
                            dim += 1;
                        }
                        return new NewArrayExpression(basicType, expression2, dim);
                    default:
                        throw new RuntimeException("PrimaryExpression not valid");
                }
            default:
                throw new RuntimeException("parsePrimary");
        }
    }
}
