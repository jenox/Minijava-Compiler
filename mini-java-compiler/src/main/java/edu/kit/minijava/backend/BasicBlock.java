package edu.kit.minijava.backend;

import edu.kit.minijava.backend.instructions.*;
import firm.nodes.Cond;

import java.util.*;
import java.util.stream.Collectors;

public class BasicBlock {

    private static int minFreeBlockNumber = 0;
    private static Set<Integer> allocatedBlockNumbers = new HashSet<>();

    private static int getUniqueBlockLabel() {
        while (allocatedBlockNumbers.contains(minFreeBlockNumber)) {
            minFreeBlockNumber++;
        }

        return minFreeBlockNumber;
    }

    public static boolean isBlockNumberAllocated(int number) {
        return allocatedBlockNumbers.contains(number);
    }

    private int blockLabel;
    private List<Instruction> instructions = new ArrayList<>();
    private List<PhiNode> phiNodes = new ArrayList<>();

    /**
     * Instructions that have to be emitted at the end of the basis block, irrespective of at which point they are
     * inserted into the block. This for example includes control flow handling (Compare and Jump
     * instructions).
     */
    private Instruction compareInstruction;
    private ConditionalJump conditionalJump;
    private Jump unconditionalJump;

    public BasicBlock(int blockLabel) {
        this.blockLabel = blockLabel;

        allocatedBlockNumbers.add(blockLabel);
    }

    /**
     * Create a new block with a new, not yet allocated block number.
     */
    public BasicBlock() {
        int newBlockLabel = getUniqueBlockLabel();
        allocatedBlockNumbers.add(newBlockLabel);

        this.blockLabel = newBlockLabel;
    }

    public int getBlockLabel() {
        return this.blockLabel;
    }

    public List<Instruction> getInstructions() {
        return this.instructions;
    }

    public List<Instruction> getBlockEndingInstructions() {
        List<Instruction> result = new ArrayList<>(3);

        if (this.compareInstruction != null) result.add(this.compareInstruction);
        if (this.conditionalJump != null) result.add(this.conditionalJump);
        if (this.unconditionalJump != null) result.add(this.unconditionalJump);
        return result;
    }

    public Optional<Instruction> getCompare() {
        return Optional.of(this.compareInstruction);
    }

    public Optional<ConditionalJump> getConditionalJump() {
        return Optional.of(this.conditionalJump);
    }

    public Optional<Jump> getEndJump() {
        return Optional.of(this.unconditionalJump);
    }

    public void setCompare(Instruction compareInstruction) {
        this.compareInstruction = compareInstruction;
    }

    public void setConditionalJump(ConditionalJump conditionalJump) {
        this.conditionalJump = conditionalJump;
    }

    public void setEndJump(Jump unconditionalJump) {
        this.unconditionalJump = unconditionalJump;
    }

    public List<PhiNode> getPhiNodes() {
        return this.phiNodes;
    }

    public void addPhiNode(PhiNode phiNode) {
        this.phiNodes.add(phiNode);
    }

    public boolean hasBranchingControlFlow() {
        if (this.compareInstruction != null) {
            assert this.conditionalJump != null;

            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Retrieve a list of all the instructions in this block, including the ones that are marked as being appended to
     * the end of the block.
     * @return A list of the normal and instructions marked as ending the block included in this block.
     */
    public List<Instruction> getFullInstructionList() {
        List<Instruction> instructions = new ArrayList<>(this.instructions);
        instructions.addAll(this.getBlockEndingInstructions());

        return instructions;
    }

    public List<String> getFullInstructionListAsString() {
        return this.getFullInstructionList()
            .stream()
            .map(Instruction::emitIntermediateInstruction)
            .collect(Collectors.toList());
    }

    public void appendInstruction(Instruction instruction) {
        this.instructions.add(instruction);
    }

    public String formatBlockLabel() {
        // TODO Correctly set this for all platforms.
        return "L" + this.getBlockLabel();
    }

    @Override
    public String toString() {
        return "Block " + this.getBlockLabel() + ", " + this.getInstructions().size() + " + " + this.getBlockEndingInstructions().size() + " inst., "
            + this.phiNodes.size() + " phi nodes";
    }
}
