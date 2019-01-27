package edu.kit.minijava.backend;

import java.util.*;

public class PhiNode {

    private BasicBlock basicBlock;

    private int targetRegister;

    // A phi node represents a list of mappings between a predecessor block and a register that should serve as a
    // source for the value selected by the Phi node.
    private List<PhiMapping> mappings;

    public PhiNode(BasicBlock basicBlock, int targetRegister, List<PhiMapping> mappings) {
        this.basicBlock = basicBlock;
        this.targetRegister = targetRegister;

        if (mappings == null) {
            throw new IllegalArgumentException();
        }

        this.mappings = new ArrayList<>(mappings);
    }

    public PhiNode(BasicBlock basicBlock, int targetRegister, List<BasicBlock> sourceBlocks,
                   List<Integer> sourceRegisters) {

        if (sourceBlocks == null || sourceRegisters == null) {
            throw new IllegalArgumentException();
        }

        if (sourceBlocks.size() != sourceRegisters.size()) {
            throw new IllegalArgumentException("Mismatching size for predecessor blocks and value source in Phi!");
        }

        this.mappings = new ArrayList<>(sourceBlocks.size());

        for (int i = 0; i < sourceBlocks.size(); i++) {
            this.mappings.add(new PhiMapping(sourceBlocks.get(i), sourceRegisters.get(i)));
        }
    }

    public BasicBlock getBasicBlock() {
        return this.basicBlock;
    }

    public int getTargetRegister() {
        return this.targetRegister;
    }

    public List<PhiMapping> getMappings() {
        return this.mappings;
    }

    public void modifySourceBlock(int index, BasicBlock newSourceBlock) {
        if (index < 0 || index >= this.mappings.size()) {
            throw new IllegalArgumentException("Index out of bounds in Phi node!");
        }

        // Set the new source block at the appropriate index in the list.
        this.mappings.get(index).setBlock(newSourceBlock);
    }
}
