package edu.kit.minijava.backend;

import java.util.*;

public class BasicBlock {

    private int blockLabel;
    private List<String> instructions = new ArrayList<>();

    /**
     * Instructions that have to be emitted at the end of the basis block, irrespective of at which point they are
     * inserted into the block. This for example includes Phi node handling and control flow (Compare and Jump
     * instructions).
     */
    private List<String> blockEndingInstructions = new ArrayList<>();

    public BasicBlock(int blockLabel) {
        this.blockLabel = blockLabel;
    }

    public int getBlockLabel() {
        return this.blockLabel;
    }

    public List<String> getInstructions() {
        return this.instructions;
    }

    public List<String> getBlockEndingInstructions() {
        return this.blockEndingInstructions;
    }

    /**
     * Retrieve a list of all the instructions in this block, including the ones that are marked as being appended to
     * the end of the block.
     * @return A list of the normal and instructions marked as ending the block included in this block.
     */
    public List<String> getFullInstructionList() {
        List<String> instructions = new ArrayList<>(this.instructions);
        instructions.addAll(this.blockEndingInstructions);

        return instructions;
    }

    public void appendInstruction(String instruction) {
        this.instructions.add(instruction);
    }

    public void appendBlockEndingInstruction(String instruction) {
        this.blockEndingInstructions.add(instruction);
    }
}
