package edu.kit.minijava.backend;

import java.util.*;
import java.util.stream.Collectors;

public class PhiNode {

    public static class Mapping {

        private BasicBlock sourceBlock;
        private int sourceRegister;
        private int targetRegister;

        private String registerSuffix;
        private String moveSuffix;

        public Mapping(BasicBlock sourceBlock, int sourceRegister, int targetRegister,
                       String registerSuffix, String moveSuffix) {
            this.sourceBlock = sourceBlock;
            this.sourceRegister = sourceRegister;
            this.targetRegister = targetRegister;

            this.registerSuffix = registerSuffix;
            this.moveSuffix = moveSuffix;
        }

        public BasicBlock getBlock() {
            return this.sourceBlock;
        }

        public int getSourceRegister() {
            return this.sourceRegister;
        }

        public int getTargetRegister() {
            return this.targetRegister;
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

        public void setSourceRegister(int sourceRegister) {
            this.sourceRegister = sourceRegister;
        }

        public void setTargetRegister(int targetRegister) {
            this.targetRegister = targetRegister;
        }


        @Override
        public String toString() {
            return "Block " + this.getBlock().getBlockLabel()
                + ", " + this.getSourceRegister() + this.getRegisterSuffix()
                + " -> " + this.getTargetRegister();
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

        this.mappings = mappings;
    }

    public BasicBlock getBasicBlock() {
        return this.basicBlock;
    }

    public int getTargetRegister() {
        return this.targetRegister;
    }

    public List<Mapping> getMappings() {
        return this.mappings;
    }

    public int getMappingCount() {
        return this.mappings.size();
    }

    public Set<Integer> getSourceRegisters() {
        return this.mappings
            .stream()
            .map(Mapping::getSourceRegister)
            .collect(Collectors.toSet());
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

        sb.append(" -> ")
            .append("BB")
            .append(this.getBasicBlock().getBlockLabel())
            .append(", ")
            .append(this.getTargetRegister());

        return sb.toString();
    }
}
