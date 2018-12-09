package edu.kit.minijava.transformation;

import firm.*;
import firm.nodes.*;

public abstract class ExpressionResult {

    private final Construction construction;

    protected ExpressionResult(Construction construction) {
        if (construction == null) throw new IllegalArgumentException();
        this.construction = construction;
    }

    public Construction getConstruction() {
        return this.construction;
    }

    public abstract Value convertToValue();
    public abstract Cond convertToCond();

    public static final class Value extends ExpressionResult {

        private final boolean inCurrentBlock;
        private final Node node;
        private final Block block;

        public Value(Construction construction, Block block, Node node, boolean inCurrentBlock) {
            super(construction);

            this.inCurrentBlock = inCurrentBlock;
            this.node = node;
            if (inCurrentBlock) {
                this.block = construction.getCurrentBlock();
            }
            else {
                this.block = block;
            }
        }

        public Value(Construction construction, Node node) {
            this(construction, construction.getCurrentBlock(), node, true);
        }

        public boolean isInCurrentBlock() {
            return this.inCurrentBlock;
        }

        public Node getNode() {
            return this.node;
        }

        public Block getBlock() {
            return this.block;
        }

        @Override
        public Value convertToValue() {
            return this;
        }

        @Override
        public Cond convertToCond() {
            assert this.getNode().getMode().equals(Mode.getBs())
                : "Can only convert boolean values to control flow graph structures!";

            Node byteFalse = this.getConstruction().newConst(0, Mode.getBs());

            Node cmp = this.getConstruction().newCmp(this.getNode(), byteFalse, Relation.Equal.negated());
            Node cond = this.getConstruction().newCond(cmp);

            Node trueProj = this.getConstruction().newProj(cond, Mode.getX(), firm.nodes.Cond.pnTrue);
            Node falseProj = this.getConstruction().newProj(cond, Mode.getX(), firm.nodes.Cond.pnFalse);

            return new Cond(this.getConstruction(), this.getBlock(), trueProj, falseProj);
        }
    }

    public static final class Cond extends ExpressionResult {

        private final Block block;
        private final Node ifTrue;
        private final Node ifFalse;

        public Cond(Construction construction, Block block, Node ifTrue, Node ifFalse) {
            super(construction);

            this.block = block;
            this.ifTrue = ifTrue;
            this.ifFalse = ifFalse;

        }

        public Cond(Construction construction, Node compareNode) {
            super(construction);

            Node cond = construction.newCond(compareNode);

            Node trueProj = construction.newProj(cond, Mode.getX(), firm.nodes.Cond.pnTrue);
            Node falseProj = construction.newProj(cond, Mode.getX(), firm.nodes.Cond.pnFalse);

            this.block = construction.getCurrentBlock();
            this.ifTrue = trueProj;
            this.ifFalse = falseProj;
        }

        public Block getBlock() {
            return this.block;
        }

        public Node getIfTrue() {
            return this.ifTrue;
        }

        public Node getIfFalse() {
            return this.ifFalse;
        }

        @Override
        public Value convertToValue() {
            Node constZero = this.getConstruction().newConst(0, Mode.getBs());
            Node constMinusOne = this.getConstruction().newConst(-1, Mode.getBs());

            Block afterBlock = this.getConstruction().newBlock();
            this.getConstruction().setCurrentBlock(afterBlock);

            afterBlock.addPred(this.getIfTrue());
            afterBlock.addPred(this.getIfFalse());

            Node phiNode = this.getConstruction().newPhi(new Node[] { constMinusOne, constZero }, Mode.getBs());

            return new Value(this.getConstruction(), this.getConstruction().getCurrentBlock(), phiNode, true);
        }

        @Override
        public Cond convertToCond() {
            return this;
        }
    }

}
