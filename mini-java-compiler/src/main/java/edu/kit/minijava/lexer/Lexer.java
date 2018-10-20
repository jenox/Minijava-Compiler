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
        return null;
    }
}
