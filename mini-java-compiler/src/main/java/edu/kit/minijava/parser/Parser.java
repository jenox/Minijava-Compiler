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

    public Expression parseExpression(){
        return null;
    }

    public Expression parseAssignmentExpression(){
        return null;
    }

    public Expression parseLogicalOrExpression(){
        return null;
    }

    public Expression parseLogicalAndExpression(){
        return null;
    }

    public Expression parseEqualityExpression(){
        return null;
    }

    public Expression parseRelationalExpression(){
        return null;
    }

    public Expression parseAdditiveExpression(){
        return null;
    }

    public Expression parseMultiplicativeExpression(){
        return null;
    }

    public Expression parseUnaryExpression(){
        return null;
    }

    public PostfixExpression parsePostfixExpression(){
        return null;
    }

    public PostfixOperation parsePostfixOperation(){
        return null;
    }

    public MethodInvocation parseMethodInvocation(){
        return null;
    }

    public FieldAccess parseFieldAccess(){
        return null;
    }

    public ArrayAccess parseArrayAccess(){
        return null;
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
                return new NullLiteral();
            case FALSE:
                return new BooleanLiteral(false);
            case TRUE:
                return new BooleanLiteral(true);
            case INTEGER_LITERAL:
                try {
                    return new IntegerLiteral(Integer.parseInt(this.currentToken.text));
                } catch (NumberFormatException exception) {
                    throw new RuntimeException();
                }
            case IDENTIFIER:
                Token token = this.consume(TokenType.IDENTIFIER);
                if (this.check(TokenType.OPENING_PARENTHESIS)) {
                    this.consume(TokenType.OPENING_PARENTHESIS);
                    List<Expression> arguments = this.parseArguments();
                    this.consume(TokenType.CLOSING_PARENTHESIS);
                    return new IdentifierAndArgumentsExpression(token.text, arguments);
                } else {
                    return new IdentifierExpression(token.text);
                }
            case THIS:
                return new ThisExpression();
            case OPENING_PARENTHESIS:
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
