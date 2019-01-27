package edu.kit.minijava.backend.instructions;

public class GenericInstruction extends Instruction {

    private String instructionText;

    public GenericInstruction(String instructionText) {
        this.instructionText = instructionText;
    }

    public String getInstructionText() {
        return this.instructionText;
    }

    @Override
    public String emitInstruction() {
        return this.getInstructionText();
    }
}
