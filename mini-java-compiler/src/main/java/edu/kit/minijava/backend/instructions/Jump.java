package edu.kit.minijava.backend.instructions;

import edu.kit.minijava.backend.*;

/**
 * An unconditional jump instruction.
 */
public class Jump extends Instruction {

    private final BasicBlock targetBlock;

    public Jump(BasicBlock targetBlock) {
        this.targetBlock = targetBlock;
    }

    public BasicBlock getTargetBlock() {
        return this.targetBlock;
    }

    @Override
    public String emitIntermediateInstruction() {
        return INDENT + "jmp " + this.getTargetBlock().formatBlockLabel();
    }
}
