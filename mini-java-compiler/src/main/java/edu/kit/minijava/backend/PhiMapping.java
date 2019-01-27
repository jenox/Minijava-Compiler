package edu.kit.minijava.backend;

public class PhiMapping {

    private BasicBlock sourceBlock;
    private int sourceRegister;

    public PhiMapping(BasicBlock sourceBlock, int sourceRegister) {
        this.sourceBlock = sourceBlock;
        this.sourceRegister = sourceRegister;
    }

    public BasicBlock getBlock() {
        return this.sourceBlock;
    }

    public int getSrcReg() {
        return this.sourceRegister;
    }

    public void setBlock(BasicBlock sourceBlock) {
        this.sourceBlock = sourceBlock;
    }

    public void setSrcReg(int sourceRegister) {
        this.sourceRegister = sourceRegister;
    }
}
