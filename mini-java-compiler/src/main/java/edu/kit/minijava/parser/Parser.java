package edu.kit.minijava.parser;

import edu.kit.minijava.lexer.*;

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

    private boolean check(TokenType type){
        if(currentToken == null){
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

    // MARK: - Parsing Expressions

    public Expression parseExpression() {
        throw new UnsupportedOperationException();
    }

    // MARK: - Parsing Types

    public Type parseType() {
        BasicType basicType = parseBasicType();

        int dim = 0;

        while(this.check(TokenType.OPENING_BRACKET)){
            this.consume(TokenType.OPENING_BRACKET);
            this.consume(TokenType.CLOSING_BRACKET);
            dim += 1;
        }

        return new Type(basicType, dim);
    }

    public BasicType parseBasicType(){
        switch(this.currentToken.type){
            case INT:
                this.consume(TokenType.INT);
                return new IntegerType();
            case BOOLEAN:
                this.consume(TokenType.INT);
                return new BooleanType();
            case VOID:
                this.consume(TokenType.INT);
                return new VoidType();
            case IDENTIFIER:
                Token token = this.consume(TokenType.INT);
                return new UserDefinedType(token.text);
            default:
                throw new RuntimeException("Bad BasicType");
        }
    }
}
