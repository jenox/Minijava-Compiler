package edu.kit.minijava.parser;

import java.util.*;

public final class Block extends Statement {
    public Block(List<BlockStatement> statements) {
        if (statements == null) { throw new IllegalArgumentException(); }

        this.statements = Collections.unmodifiableList(statements);
    }

    public final List<BlockStatement> statements;

    @Override
    public String toString() {
        return "Block(" + this.statements + ")";
    }
}
