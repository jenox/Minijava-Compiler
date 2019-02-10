package edu.kit.minijava.backend.instructions;

/**
 * A generic instruction represented by its GAS assembly syntax.
 */
public class GenericInstruction extends Instruction {

    private String instructionText;

    public GenericInstruction(String instructionText) {
        this.instructionText = instructionText;
    }

    public String getInstructionText() {
        return this.instructionText;
    }

    @Override
    public String emitIntermediateInstruction() {
        return INDENT + this.getInstructionText();
    }
}
