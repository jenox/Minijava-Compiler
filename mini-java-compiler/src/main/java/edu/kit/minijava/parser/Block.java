package edu.kit.minijava.parser;

import java.util.*;

public final class Block extends Statement {
    public Block(List<BlockStatement> statements) {
        this.statements = Collections.unmodifiableList(statements);
    }

    public final List<BlockStatement> statements;
}
