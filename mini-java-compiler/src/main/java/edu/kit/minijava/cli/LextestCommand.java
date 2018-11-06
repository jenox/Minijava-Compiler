package edu.kit.minijava.cli;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.kit.minijava.lexer.Lexer;
import edu.kit.minijava.lexer.LexerException;
import edu.kit.minijava.lexer.Token;

class LextestCommand extends Command {
    public int execute(String path) {
        try {
            FileInputStream stream = new FileInputStream(path);
            InputStreamReader reader = new InputStreamReader(stream, "US-ASCII");

            Lexer lexer = new Lexer(reader);

            Token token = lexer.nextToken();

            while (token != null) {
                System.out.println(this.descriptionOfToken(token));

                token = lexer.nextToken();
            }

            System.out.println("EOF");

            return 0;
        }
        catch (LexerException exception) {
            System.err.println("error: " + exception.getLocalizedMessage());

            return 1;
        }
        catch (FileNotFoundException exception) {
            System.err.println("error: File '" + path + "' was not found!");

            return 1;
        }
        catch (IOException exception) {
            System.err.println("error: File '" + path + "' could not be read!");

            return 1;
        }
    }

    private String descriptionOfToken(Token token) {
        switch (token.getType()) {
            case ABSTRACT: return "abstract";
            case ASSERT: return "assert";
            case BOOLEAN: return "boolean";
            case BREAK: return "break";
            case BYTE: return "byte";
            case CASE: return "case";
            case CATCH: return "catch";
            case CHAR: return "char";
            case CLASS: return "class";
            case CONST: return "const";
            case CONTINUE: return "continue";
            case DEFAULT: return "default";
            case DOUBLE: return "double";
            case DO: return "do";
            case ELSE: return "else";
            case ENUM: return "enum";
            case EXTENDS: return "extends";
            case FALSE: return "false";
            case FINALLY: return "finally";
            case FINAL: return "final";
            case FLOAT: return "float";
            case FOR: return "for";
            case GOTO: return "goto";
            case IF: return "if";
            case IMPLEMENTS: return "implements";
            case IMPORT: return "import";
            case INSTANCEOF: return "instanceof";
            case INTERFACE: return "interface";
            case INT: return "int";
            case LONG: return "long";
            case NATIVE: return "native";
            case NEW: return "new";
            case NULL: return "null";
            case PACKAGE: return "package";
            case PRIVATE: return "private";
            case PROTECTED: return "protected";
            case PUBLIC: return "public";
            case RETURN: return "return";
            case SHORT: return "short";
            case STATIC: return "static";
            case STRICTFP: return "strictfp";
            case SUPER: return "super";
            case SWITCH: return "switch";
            case SYNCHRONIZED: return "synchronized";
            case THIS: return "this";
            case THROWS: return "throws";
            case THROW: return "throw";
            case TRANSIENT: return "transient";
            case TRUE: return "true";
            case TRY: return "try";
            case VOID: return "void";
            case VOLATILE: return "volatile";
            case WHILE: return "while";
            case ASSIGN: return "=";
            case EQUAL_TO: return "==";
            case NOT_EQUAL_TO: return "!=";
            case LESS_THAN: return "<";
            case LESS_THAN_OR_EQUAL_TO: return "<=";
            case GREATER_THAN: return ">";
            case GREATER_THAN_OR_EQUAL_TO: return ">=";
            case PLUS: return "+";
            case PLUS_AND_ASSIGN: return "+=";
            case MINUS: return "-";
            case MINUS_AND_ASSIGN: return "-=";
            case MULTIPLY: return "*";
            case MULTIPLY_AND_ASSIGN: return "*=";
            case DIVIDE: return "/";
            case DIVIDE_AND_ASSIGN: return "/=";
            case MODULO: return "%";
            case MODULO_AND_ASSIGN: return "%=";
            case INCREASE: return "++";
            case DECREASE: return "--";
            case LOGICAL_NEGATION: return "!";
            case LOGICAL_OR: return "||";
            case LOGICAL_AND: return "&&";
            case QUESTION_MARK: return "?";
            case COLON: return ":";
            case BITWISE_NOT: return "~";
            case BITWISE_AND: return "&";
            case BITWISE_AND_AND_ASSIGN: return "&=";
            case BITWISE_OR: return "|";
            case BITWISE_OR_AND_ASSIGN: return "|=";
            case BITWISE_XOR: return "^";
            case BITWISE_XOR_AND_ASSIGN: return "^=";
            case SHIFT_LEFT: return "<<";
            case SHIFT_LEFT_AND_ASSIGN: return "<<=";
            case SHIFT_RIGHT: return ">>";
            case SHIFT_RIGHT_AND_ASSIGN: return ">>=";
            case UNSIGNED_SHIFT_RIGHT: return ">>>";
            case UNSIGNED_SHIFT_RIGHT_AND_ASSIGN: return ">>>=";
            case OPENING_PARENTHESIS: return "(";
            case CLOSING_PARENTHESIS: return ")";
            case OPENING_BRACE: return "{";
            case CLOSING_BRACE: return "}";
            case OPENING_BRACKET: return "[";
            case CLOSING_BRACKET: return "]";
            case SEMICOLON: return ";";
            case COMMA: return ",";
            case PERIOD: return ".";
            case IDENTIFIER: return "identifier " + token.getText();
            case INTEGER_LITERAL: return "integer literal " + token.getText();
            default: throw new RuntimeException();
        }
    }
}
