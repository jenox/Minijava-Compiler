package edu.kit.minijava.transformation;

import firm.*;
import firm.nodes.*;

import java.util.*;

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

    public Value assignTo(Value value) {
        throw new UnsupportedOperationException("Can only assign to LValues!");
    }

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

    public static final class ArrayLValue extends ExpressionResult {

        private final Sel selNode;
        private final Type valueType;
        private final Mode valueMode;

        public ArrayLValue(Construction construction, Sel selNode, Type valueType) {
            super(construction);

            if (selNode == null) throw new IllegalArgumentException();
            this.selNode = selNode;

            if (valueType == null) throw new IllegalArgumentException();
            this.valueType = valueType;

            this.valueMode = Optional.ofNullable(this.getValueType().getMode()).orElse(Mode.getP());
        }

        public Sel getSelNode() {
            return this.selNode;
        }

        public Type getValueType() {
            return this.valueType;
        }

        public Mode getValueMode() {
            return this.valueMode;
        }

        @Override
        public Value convertToValue() {
            Node mem = this.getConstruction().getCurrentMem();
            Node load = this.getConstruction().newLoad(mem, this.getSelNode(), this.getValueType().getMode());

            Node newMem = this.getConstruction().newProj(load, Mode.getM(), Load.pnM);
            this.getConstruction().setCurrentMem(newMem);

            Node result = this.getConstruction().newProj(load, this.getValueType().getMode(), Load.pnRes);

            return new Value(this.getConstruction(), result);
        }

        @Override
        public Cond convertToCond() {

            assert this.getValueMode().equals(Mode.getBs())
                : "Can only convert boolean values to control flow graph structures!";

            return this.convertToValue().convertToCond();
        }

        @Override
        public Value assignTo(Value value) {

            Mode otherMode = Optional.ofNullable(value.getNode().getMode()).orElse(Mode.getP());
            if (!this.getValueMode().equals(otherMode)) {
                throw new IllegalArgumentException("Mismatching modes for assignment!");
            }

            Node mem = this.getConstruction().getCurrentMem();
            Node store = this.getConstruction().newStore(mem, this.getSelNode(), value.getNode());

            // Update memory nodes
            mem = this.getConstruction().newProj(store, Mode.getM(), Store.pnM);
            this.getConstruction().setCurrentMem(mem);

            return new Value(this.getConstruction(), value.getNode());
        }
    }

    public static final class LocalVariableLValue extends ExpressionResult {

        private final int variableIndex;
        private final Type valueType;
        private final Mode valueMode;

        public LocalVariableLValue(Construction construction, int variableIndex, Type valueType) {
            super(construction);

            this.variableIndex = variableIndex;

            if (valueType == null) throw new IllegalArgumentException();
            this.valueType = valueType;

            this.valueMode = Optional.ofNullable(valueType.getMode()).orElse(Mode.getP());
        }

        public int getVariableIndex() {
            return this.variableIndex;
        }

        public Type getValueType() {
            return this.valueType;
        }

        public Mode getValueMode() {
            return this.valueMode;
        }

        @Override
        public Value convertToValue() {
            Node value = this.getConstruction().getVariable(this.getVariableIndex(), this.getValueType().getMode());
            return new Value(this.getConstruction(), value);
        }

        @Override
        public Cond convertToCond() {

            assert this.getValueMode().equals(Mode.getBs())
                : "Can only convert boolean values to control flow graph structures!";

            return this.convertToValue().convertToCond();
        }

        @Override
        public Value assignTo(Value value) {

            Mode otherMode = Optional.ofNullable(value.getNode().getMode()).orElse(Mode.getP());
            if (!this.getValueMode().equals(otherMode)) {
                throw new IllegalArgumentException("Mismatching modes for assignment!");
            }

            this.getConstruction().setVariable(this.variableIndex, value.getNode());

            return new Value(this.getConstruction(), value.getNode());
        }
    }

    public static final class FieldLValue extends ExpressionResult {

        private final Member memberNode;
        private final Mode valueMode;

        public FieldLValue(Construction construction, Member memberNode, Mode valueMode) {
            super(construction);

            if (memberNode == null) throw new IllegalArgumentException();
            this.memberNode = memberNode;

            if (valueMode == null) throw new IllegalArgumentException();
            this.valueMode = valueMode;
        }

        public Member getMemberNode() {
            return this.memberNode;
        }

        public Mode getValueMode() {
            return this.valueMode;
        }

        @Override
        public Value convertToValue() {
            Node mem = this.getConstruction().getCurrentMem();
            Node load = this.getConstruction().newLoad(mem, this.getMemberNode(), this.getValueMode());

            mem = this.getConstruction().newProj(load, Mode.getM(), Load.pnM);
            this.getConstruction().setCurrentMem(mem);

            // Retrieve and return result
            Node result = this.getConstruction().newProj(load, this.getValueMode(), Load.pnRes);
            return new Value(this.getConstruction(), result);
        }

        @Override
        public Cond convertToCond() {
            assert this.getValueMode().equals(Mode.getBs())
                : "Can only convert boolean values to control flow graph structures!";

            return this.convertToValue().convertToCond();
        }

        @Override
        public Value assignTo(Value value) {

            Mode otherMode = Optional.ofNullable(value.getNode().getMode()).orElse(Mode.getP());
            if (!this.getValueMode().equals(otherMode)) {
                throw new IllegalArgumentException("Mismatching modes for assignment!");
            }

            Node mem = this.getConstruction().getCurrentMem();
            Node store = this.getConstruction().newStore(mem, this.getMemberNode(), value.getNode());

            mem = this.getConstruction().newProj(store, Mode.getM(), Store.pnM);
            this.getConstruction().setCurrentMem(mem);

            return new Value(this.getConstruction(), value.getNode());
        }
    }
}
