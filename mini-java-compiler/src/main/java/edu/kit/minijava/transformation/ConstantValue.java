package edu.kit.minijava.transformation;

import firm.*;

import java.util.*;

class ConstantValue {

    // MARK: - Creation

    private ConstantValue(Type type, TargetValue value) {
        this.type = type;
        this.value = value;
    }

    static ConstantValue undefined() {
        return new ConstantValue(Type.UNDEFINED, null);
    }

    static ConstantValue constant(TargetValue value) {
        assert value != null;

        return new ConstantValue(Type.CONSTANT, value);
    }

    static ConstantValue notAConstant() {
        return new ConstantValue(Type.NOT_A_CONSTANT, null);
    }

    private enum Type {
        UNDEFINED,
        CONSTANT,
        NOT_A_CONSTANT
    }

    private final Type type;
    private final TargetValue value;


    public boolean isUndefined() {
        return this.type == Type.UNDEFINED;
    }

    boolean isNotAConstant() {
        return this.type == Type.NOT_A_CONSTANT;
    }

    boolean isConstant() {
        return this.type == Type.CONSTANT;
    }

    Optional<TargetValue> getValue() {
        return Optional.ofNullable(this.value);
    }

    // TODO: Unit tests
    ConstantValue join(ConstantValue other) {
        if (this.isNotAConstant() || other.isNotAConstant()) {
            return ConstantValue.notAConstant();
        }
        else if (this.isConstant() && other.isConstant()) {
            if (this.value.equals(other.value)) {
                return ConstantValue.constant(this.value);
            }
            else {
                return ConstantValue.notAConstant();
            }
        }
        else if (this.isConstant()) {
            return ConstantValue.constant(this.value);
        }
        else if (other.isConstant()) {
            return ConstantValue.constant(other.value);
        }
        else {
            return ConstantValue.undefined();
        }
    }


    // MARK: - Folding

    interface UnaryOperation {
        TargetValue perform(TargetValue value);
    }

    interface BinaryOperation {
        TargetValue perform(TargetValue lhs, TargetValue rhs);
    }

    static ConstantValue fold(ConstantValue element, UnaryOperation operation) {
        switch (element.type) {
            case UNDEFINED:
                return ConstantValue.undefined();
            case CONSTANT:
                return ConstantValue.constant(operation.perform(element.value));
            case NOT_A_CONSTANT:
                return ConstantValue.notAConstant();
            default:
                throw new AssertionError();
        }
    }

    static ConstantValue fold(ConstantValue left, ConstantValue right, BinaryOperation operation) {
        if (left.isConstant() && right.isConstant()) {
            return ConstantValue.constant(operation.perform(left.value, right.value));
        }
        else if (left.isNotAConstant() || right.isNotAConstant()) {
            return ConstantValue.notAConstant();
        }
        else {
            return ConstantValue.undefined();
        }
    }


    // MARK: - Overrides

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;

        if (!(object instanceof ConstantValue)) return false;

        ConstantValue element = (ConstantValue)object;

        switch (this.type) {
            case UNDEFINED:
                return element.type == Type.UNDEFINED;
            case CONSTANT:
                return element.type == Type.CONSTANT && this.value.equals(element.value);
            case NOT_A_CONSTANT:
                return element.type == Type.NOT_A_CONSTANT;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public int hashCode() {
        if (this.value != null) {
            return this.value.hashCode();
        }
        else {
            return 0;
        }
    }

    @Override
    public String toString() {
        switch (this.type) {
            case UNDEFINED:
                return "undefined";
            case CONSTANT:
                return this.value.toString();
            case NOT_A_CONSTANT:
                return "not a constant";
            default:
                throw new AssertionError();
        }
    }
}