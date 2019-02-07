package edu.kit.minijava.backend.instructions;

/**
 * Abstract superclass for intermediate assembly instructions.
 */
public abstract class Instruction {

    static final String INDENT = "    ";

    public abstract String emitIntermediateInstruction();
}
