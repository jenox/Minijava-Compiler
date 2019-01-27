package edu.kit.minijava.backend;

import java.util.*;

public class PhiNode {

    public static class Mapping {

        private BasicBlock sourceBlock;
        private int sourceRegister;

        private String registerSuffix;
        private String moveSuffix;

        public Mapping(BasicBlock sourceBlock, int sourceRegister, String registerSuffix, String moveSuffix) {
            this.sourceBlock = sourceBlock;
            this.sourceRegister = sourceRegister;

            this.registerSuffix = registerSuffix;
            this.moveSuffix = moveSuffix;
        }

        public BasicBlock getBlock() {
            return this.sourceBlock;
        }

        public int getReg() {
            return this.sourceRegister;
        }

        public String getRegisterSuffix() {
            return this.registerSuffix;
        }

        public String getMoveSuffix() {
            return this.moveSuffix;
        }

        public void setBlock(BasicBlock sourceBlock) {
            this.sourceBlock = sourceBlock;
        }

        public void setReg(int sourceRegister) {
            this.sourceRegister = sourceRegister;
        }

        @Override
        public String toString() {
            return "(BB" + this.getBlock() + ", " + this.getReg() + this.getRegisterSuffix() + ")";
        }
    }

    private BasicBlock basicBlock;

    private int targetRegister;

    // A phi node represents a list of mappings between a predecessor block and a register that should serve as a
    // source for the value selected by the Phi node.
    private List<Mapping> mappings;

    public PhiNode(BasicBlock basicBlock, int targetRegister, List<Mapping> mappings) {
        this.basicBlock = basicBlock;
        this.targetRegister = targetRegister;

        if (mappings == null) {
            throw new IllegalArgumentException();
        }

        this.mappings = new ArrayList<>(mappings);
    }

//    public PhiNode(BasicBlock basicBlock, int targetRegister, List<BasicBlock> sourceBlocks,
//                   List<Integer> sourceRegisters) {
//
//        if (sourceBlocks == null || sourceRegisters == null) {
//            throw new IllegalArgumentException();
//        }
//
//        if (sourceBlocks.size() != sourceRegisters.size()) {
//            throw new IllegalArgumentException("Mismatching size for predecessor blocks and value source in Phi!");
//        }
//
//        this.mappings = new ArrayList<>(sourceBlocks.size());
//
//        for (int i = 0; i < sourceBlocks.size(); i++) {
//            this.mappings.add(new Mapping(sourceBlocks.get(i), sourceRegisters.get(i)));
//        }
//    }

    public BasicBlock getBasicBlock() {
        return this.basicBlock;
    }

    public int getTargetRegister() {
        return this.targetRegister;
    }

    public List<Mapping> getMappings() {
        return this.mappings;
    }

    public void modifySourceBlock(int index, BasicBlock newSourceBlock) {
        if (index < 0 || index >= this.mappings.size()) {
            throw new IllegalArgumentException("Index out of bounds in Phi node!");
        }

        // Set the new source block at the appropriate index in the list.
        this.mappings.get(index).setBlock(newSourceBlock);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String sep = "";

        for (Mapping mapping : this.getMappings()) {
            sb.append(sep).append(mapping.toString());
            sep = ", ";
        }

        sb.append(" -> ").append("BB").append(this.getBasicBlock()).append(", ").append(this.getTargetRegister());
        return sb.toString();
    }
}
