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

    private Token expect(TokenType type) {
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
        throw new UnsupportedOperationException();
    }
}
