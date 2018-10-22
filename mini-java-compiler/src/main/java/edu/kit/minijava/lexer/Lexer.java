package edu.kit.minijava.lexer;

import java.io.*;
import java.util.function.*;

public class Lexer {

    // MARK: - Initialization

    public Lexer(InputStreamReader reader) throws IOException {
        this.reader = reader;
        this.currentCharacter = this.reader.read();
    }

    // MARK: - State

    private final InputStreamReader reader;
    private int currentCharacter;

    private int currentRow = 0;
    private int currentColumn = 0;
    private boolean lastCharacterWasCarriageReturn = false;

    private boolean hasReachedEndOfInput() {
        return this.currentCharacter == -1;
    }

    private char getCurrentCharacter() {
        if (this.hasReachedEndOfInput()) {
            throw new IllegalStateException();
        }

        return (char)this.currentCharacter;
    }

    private String advance() throws IOException {
        if (this.hasReachedEndOfInput()) {
            throw new IllegalStateException();
        }

        String string = String.valueOf(this.getCurrentCharacter());

        if (string.equals("\r")) {
            this.currentRow += 1;
            this.currentColumn = 0;
            this.lastCharacterWasCarriageReturn = true;
        } else if (string.equals("\n")) {
            if (!this.lastCharacterWasCarriageReturn) {
                this.currentRow += 1;
                this.currentColumn = 0;
                this.lastCharacterWasCarriageReturn = false;
            }
        } else {
            this.currentColumn += 1;
            this.lastCharacterWasCarriageReturn = false;
        }

        this.currentCharacter = this.reader.read();

        return string;
    }

    private String advanceWhile(BooleanSupplier predicate) throws IOException {
        return this.advanceWhile(predicate, s -> true);
    }

    private String advanceWhile(BooleanSupplier predicate, Predicate<String> stringPredicate) throws IOException {
        String string = "";

        while (!this.hasReachedEndOfInput() && predicate.getAsBoolean() && stringPredicate.test(string)) {
            string += this.advance();
        }

        return string;
    }

    // MARK: - Helpers

    private boolean isCurrentCharacterNumeric() {
        if (this.hasReachedEndOfInput()) return false;

        char character = this.getCurrentCharacter();

        return "0123456789".indexOf(character) != -1;
    }

    private boolean isCurrentCharacterAlphanumeric() {
        if (this.hasReachedEndOfInput()) return false;

        char character = this.getCurrentCharacter();

        return "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVXYZ0123456789".indexOf(character) != -1;
    }

    private boolean isCurrentCharacterWhitespace() {
        if (this.hasReachedEndOfInput()) return false;

        switch (this.getCurrentCharacter()) {
            case ' ': return true;
            case '\t': return true;
            case '\r': return true;
            case '\n': return true;
            default: return false;
        }
    }

    private boolean isCurrentCharacterSeparator() {
        if (this.hasReachedEndOfInput()) return false;

        char character = this.getCurrentCharacter();

        return "(){}[];,.".indexOf(character) != -1;
    }

    private boolean isCurrentCharacterOperatorSymbol() {
        if (this.hasReachedEndOfInput()) return false;

        char character = this.getCurrentCharacter();

        return "!=*+-/:<>?%&^~|".indexOf(character) != -1;
    }

    // MARK: - Fetching Tokens

    public Token nextToken() throws IOException, LexerException {
        this.ensureNoPreviousExceptionsWereThrown();
        this.advanceWhile(this::isCurrentCharacterWhitespace);

        if (this.hasReachedEndOfInput()) {
            return null;
        }

        TokenLocation location = new TokenLocation(this.currentRow, this.currentColumn);

        if (this.isCurrentCharacterNumeric()) {
            String text = this.advanceWhile(this::isCurrentCharacterNumeric);

            if (text.startsWith("0") && !text.equals("0")) {
                throw this.fail("Integer literals may only start with 0 if they are zero");
            } else if (this.isCurrentCharacterAlphanumeric()) {
                throw this.fail("Integer literals must not be followed by alphanumeric characters");
            }

            return new Token(TokenType.INTEGER_LITERAL, text, location);
        } else if (this.isCurrentCharacterAlphanumeric()) {
            String text = this.advanceWhile(this::isCurrentCharacterAlphanumeric);
            TokenType type = this.keywordFromString(text);

            if (type != null) {
                return new Token(type, text, location);
            } else {
                return new Token(TokenType.IDENTIFIER, text, location);
            }
        } else if (this.isCurrentCharacterSeparator()) {
           String text = this.advance();
           TokenType separator = this.separatorFromString(text);

           if (separator != null) {
               return new Token(separator, text, location);
           } else {
               throw this.fail("Invalid separator '" + text + "'");
           }
        } else if (this.isCurrentCharacterOperatorSymbol()) {
            String text = this.advanceWhile(this::isCurrentCharacterOperatorSymbol, s -> !s.contains("/*"));

            if (text.endsWith("/*")) {
                text = text.substring(0, text.length() - 2);

                String comment = this.advanceWhile(() -> true, s -> !s.contains("*/"));

                if (!comment.endsWith("*/")) {
                    throw this.fail("Encountered unterminated comment");
                }

                if (text.isEmpty()) {
                    return this.nextToken();
                }
            }

            TokenType operator = this.operatorFromString(text);

            if (operator != null) {
                return new Token(operator, text, location);
            } else {
                throw this.fail("Invalid operator '" + text + "'");
            }
        } else {
            String name = Character.getName(this.getCurrentCharacter());

            throw this.fail("Forbidden character '" + name + "' in input");
        }
    }

    // MARK: - Exception Management

    private LexerException previousException = null;

    private LexerException fail(String message) {
        LexerException exception = new LexerException(message);

        this.previousException = exception;

        return exception;
    }

    private void ensureNoPreviousExceptionsWereThrown() {
        if (this.previousException != null) {
            throw new IllegalStateException("Lexer has thrown exception before");
        }
    }

    // MARK: - Token Type Helpers

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
            case "false": return TokenType.FALSE;
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
            case "null": return TokenType.NULL;
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
            case "true": return TokenType.TRUE;
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
