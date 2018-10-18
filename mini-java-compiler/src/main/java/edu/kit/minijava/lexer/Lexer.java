package edu.kit.minijava.lexer;

import java.io.*;
import java.nio.file.*;

public class Lexer {

    // MARK: - Initialization

    public Lexer(String filename) throws IOException {
        final Path path = Paths.get(filename);

        this.text = new String(Files.readAllBytes(path));
    }

    public final String text;

    // MARK: - Fetching Tokens

    public Token nextToken() {
        return null;
    }
}
