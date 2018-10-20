package edu.kit.minijava.lexer;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;

public class Lexer {

    // MARK: - Initialization

    public Lexer(String filename) throws IOException {
        final Path path = Paths.get(filename);

        this.text = new String(Files.readAllBytes(path));
    }

    public final String text;

    // MARK: - State

    private int currentIndex = 0;

    private boolean hasReachedEndOfFile() {
        return this.currentIndex < this.text.length();
    }

    private void increaseCurrentIndex() {
        this.currentIndex += 1;
    }

    private Character getCurrentCharacter() {
        if (this.hasReachedEndOfFile()) {
            return null;
        } else {
            return this.text.charAt(this.currentIndex);
        }
    }

    private String advance() {
        String string = String.valueOf(this.getCurrentCharacter());

        this.increaseCurrentIndex();

        return string;
    }

    private String advanceWhile(Predicate<Character> whilePredicate) {
        return this.advanceWhile(whilePredicate, s -> false);
    }

    private String advanceWhile(Predicate<Character> whilePredicate, Predicate<Character> untilPredicate) {
        String string = "";

        Character character = this.getCurrentCharacter();

        while (!this.hasReachedEndOfFile() && whilePredicate.test(character) && !untilPredicate.test(character)) {
            string += this.getCurrentCharacter();
            this.increaseCurrentIndex();
        }

        return string;
    }

    // MARK: - Helpers

    private boolean isNumeric(char character) {
        return "0123456789".indexOf(character) != -1;
    }

    private boolean isAlphanumeric(char character) {
        return "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVXYZ0123456789".indexOf(character) != -1;
    }

    private boolean isWhitespace(char character) {
        char[] characters = {' ', '\t', '\r', '\n'};

        return Arrays.asList(characters).contains(character);
    }

    private boolean isSeparator(char character) {
        return "(){}[];,.".indexOf(character) != -1;
    }

    private boolean isOperatorSymbol(char character) {
        return "!=*+-/:<>?%&^~|".indexOf(character) != -1;
    }

    // MARK: - Fetching Tokens

    public Token nextToken() {
        advanceWhile(isWhitespace());

        char literal = this.getCurrentCharacter();
        String token_text = '';
        TokenType token_type;

        if(literal == null){
            return null;
        }

        if(isNumeric(literal)){
            token_text += this.advance();
            token_text += advanceWhile(isNumeric());
            token_type = TokenType.INTEGER_LITERAL;

            assert (token_text.startsWith("0") && (token_text.length() > 1)) : ("Not a valid INTEGER_LITERAL: " + token_text);
            assert (isAlphanumeric(this.getCurrentCharacter())) : "INTEGER_LITERAL followed by [a-zA-Z]";

        } else if(isAlphanumeric(literal)) {
            assert false;
        } else if(isSeparator(literal)) {
            assert false;
        } else if(isOperatorSymbol(literal)) {
            assert false;
        } else {
            assert false : "Not a valid char"
        }


        return new Token(token_type, token_text);
    }

    private TokenType keywordFromString(String string) {
        switch (string) {
            case "abstract": return TokenType.ABSTRACT;
            case "assert": return TokenType.ASSERT;
            case "boolean": return TokenType.BOOLEAN;
            case "break": return TokenType.BREAK;
            case "byte": return TokenType.BYTE;
            case "case": return TokenType.CASE;
            case "catch": return TokenType.CATCH;
            case "char": return TokenType.CHAR;
            case "class": return TokenType.CLASS;
            case "const": return TokenType.CONST;
            case "continue": return TokenType.CONTINUE;
            case "default": return TokenType.DEFAULT;
            case "double": return TokenType.DOUBLE;
            case "do": return TokenType.DO;
            case "else": return TokenType.ELSE;
            case "enum": return TokenType.ENUM;
            case "extends": return TokenType.EXTENDS;
            case "finally": return TokenType.FINALLY;
            case "final": return TokenType.FINAL;
            case "float": return TokenType.FLOAT;
            case "for": return TokenType.FOR;
            case "goto": return TokenType.GOTO;
            case "if": return TokenType.IF;
            case "implements": return TokenType.IMPLEMENTS;
            case "import": return TokenType.IMPORT;
            case "instanceof": return TokenType.INSTANCEOF;
            case "interface": return TokenType.INTERFACE;
            case "int": return TokenType.INT;
            case "long": return TokenType.LONG;
            case "native": return TokenType.NATIVE;
            case "new": return TokenType.NEW;
            case "package": return TokenType.PACKAGE;
            case "private": return TokenType.PRIVATE;
            case "protected": return TokenType.PROTECTED;
            case "public": return TokenType.PUBLIC;
            case "return": return TokenType.RETURN;
            case "short": return TokenType.SHORT;
            case "static": return TokenType.STATIC;
            case "strictfp": return TokenType.STRICTFP;
            case "super": return TokenType.SUPER;
            case "switch": return TokenType.SWITCH;
            case "synchronized": return TokenType.SYNCHRONIZED;
            case "this": return TokenType.THIS;
            case "throws": return TokenType.THROWS;
            case "throw": return TokenType.THROW;
            case "transient": return TokenType.TRANSIENT;
            case "try": return TokenType.TRY;
            case "void": return TokenType.VOID;
            case "volatile": return TokenType.VOLATILE;
            case "while": return TokenType.WHILE;
            default: return null;
        }
    }

    private TokenType operatorFromString(String string) {
        switch (string) {
            case "!=": return TokenType.NOT_EQUAL_TO;
            case "!": return TokenType.LOGICAL_NEGATION;
            case "*=": return TokenType.MULTIPLY_AND_ASSIGN;
            case "*": return TokenType.MULTIPLY;
            case "++": return TokenType.INCREASE;
            case "+=": return TokenType.PLUS_AND_ASSIGN;
            case "+": return TokenType.PLUS;
            case "-=": return TokenType.MINUS_AND_ASSIGN;
            case "--": return TokenType.DECREASE;
            case "-": return TokenType.MINUS;
            case "/=": return TokenType.DIVIDE_AND_ASSIGN;
            case "/": return TokenType.DIVIDE;
            case ":": return TokenType.COLON;
            case "<<=": return TokenType.SHIFT_LEFT_AND_ASSIGN;
            case "<<": return TokenType.SHIFT_LEFT;
            case "<=": return TokenType.LESS_THAN_OR_EQUAL_TO;
            case "<": return TokenType.LESS_THAN;
            case "==": return TokenType.EQUAL_TO;
            case "=": return TokenType.ASSIGN;
            case ">=": return TokenType.GREATER_THAN_OR_EQUAL_TO;
            case ">>=": return TokenType.SHIFT_RIGHT_AND_ASSIGN;
            case ">>>=": return TokenType.UNSIGNED_SHIFT_RIGHT_AND_ASSIGN;
            case ">>>": return TokenType.UNSIGNED_SHIFT_RIGHT;
            case ">>": return TokenType.SHIFT_RIGHT;
            case ">": return TokenType.GREATER_THAN;
            case "?": return TokenType.QUESTION_MARK;
            case "%=": return TokenType.MODULO_AND_ASSIGN;
            case "%": return TokenType.MODULO;
            case "&=": return TokenType.BITWISE_AND_AND_ASSIGN;
            case "&&": return TokenType.LOGICAL_AND;
            case "&": return TokenType.BITWISE_AND;
            case "^=": return TokenType.BITWISE_XOR_AND_ASSIGN;
            case "^": return TokenType.BITWISE_XOR;
            case "~": return TokenType.BITWISE_NOT;
            case "|=": return TokenType.BITWISE_OR_AND_ASSIGN;
            case "||": return TokenType.LOGICAL_OR;
            case "|": return TokenType.BITWISE_OR;
            default: return null;
        }
    }

    private TokenType separatorFromString(String string) {
        switch (string) {
            case "(": return TokenType.OPENING_PARENTHESIS;
            case ")": return TokenType.CLOSING_PARENTHESIS;
            case "{": return TokenType.OPENING_BRACE;
            case "}": return TokenType.CLOSING_BRACE;
            case "[": return TokenType.OPENING_BRACKET;
            case "]": return TokenType.CLOSING_BRACKET;
            case ";": return TokenType.SEMICOLON;
            case ",": return TokenType.COMMA;
            case ".": return TokenType.PERIOD;
            default: return null;
        }
    }
}
