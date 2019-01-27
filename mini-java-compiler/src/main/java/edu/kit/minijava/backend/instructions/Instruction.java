package edu.kit.minijava.backend.instructions;

public abstract class Instruction {

    protected static final String INDENT = "    ";

    public abstract String emitInstruction();
}
