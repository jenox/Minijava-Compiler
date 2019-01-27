package edu.kit.minijava.backend.instructions;

import edu.kit.minijava.backend.*;

public class Jump extends Instruction {

    private BasicBlock targetBlock;

    public Jump(BasicBlock targetBlock) {
        this.targetBlock = targetBlock;
    }

    public BasicBlock getTargetBlock() {
        return this.targetBlock;
    }

    public void setTargetBlock(BasicBlock newTargetBlock) {
        this.targetBlock = newTargetBlock;
    }

    @Override
    public String emitInstruction() {
        return INDENT + "jmp " + this.getTargetBlock().formatBlockLabel();
    }
}
