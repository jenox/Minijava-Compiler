package edu.kit.minijava.parser;

import java.util.Collections;
import java.util.List;

public final class Block extends Statement {
    public Block(List<BlockStatement> statements) {
        if (statements == null) throw new IllegalArgumentException();

        this.statements = Collections.unmodifiableList(statements);
    }

    public final List<BlockStatement> statements;

    @Override
    public String toString() {
        return "Block(" + this.statements + ")";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
