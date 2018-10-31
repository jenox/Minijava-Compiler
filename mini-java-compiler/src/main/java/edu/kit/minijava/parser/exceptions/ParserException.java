package edu.kit.minijava.parser.exceptions;

import edu.kit.minijava.lexer.Token;
import edu.kit.minijava.lexer.TokenType;

public class ParserException extends Exception {

    private TokenType expectedType;
    private Token actualToken;
    
    public ParserException(TokenType expectedType, Token actualToken) {
        this.expectedType = expectedType;
        this.actualToken = actualToken;
    }
    
    public Token getActualToken() {
        return actualToken;
    }
    
    public TokenType getExpectedType() {
        return expectedType;
    }
    
    @Override
    public String getLocalizedMessage() {
        return "expected " + expectedType + " but got " + actualToken;
    }
}
