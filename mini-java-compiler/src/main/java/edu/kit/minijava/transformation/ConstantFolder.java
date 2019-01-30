package edu.kit.minijava.transformation;

import firm.*;
import firm.bindings.binding_irgraph;
import firm.nodes.*;

import java.util.*;

public class ConstantFolder extends ConstantFolderBase {
    ConstantFolder(Graph graph) {
        super(graph);

        BackEdges.enable(graph);
        this.iterate();
        BackEdges.disable(graph);

        this.debugLog();


        for (Node node : this.getTopologicalOrdering()) {
            TargetValue value = this.getValueForNode(node);

            // For efficiency reasons, do not replace nodes that were constants already.
            if (node instanceof Const) {
                assert ((Const)node).getTarval().equals(value);
                continue;
            }

            if (value.isConstant()) {
                Node memoryBeforeOperation = this.getMemoryUsedByOperation(node).orElse(null);
                Const replacement = (Const)graph.newConst(value);

                safeReplaceNodeWithConstant(node, replacement, memoryBeforeOperation);
                this.hasModifiedGraph = true;
            }
        }

        // Fold control flow for Cond nodes with constant predecessor

        Construction construction = new Construction(graph);
        graph.addConstraints(binding_irgraph.ir_graph_constraints_t.IR_GRAPH_CONSTRAINT_CONSTRUCTION);

        for (Node node : this.getTopologicalOrdering()) {

            if (node instanceof Cond) {
                assert node.getPredCount() == 1;

                construction.setCurrentBlock((Block) node.getBlock());

                if (node.getPred(0) instanceof Const) {
                    Const condition = (Const) node.getPred(0);

                    List<Node> successors = getSuccessorsOf(node);
                    assert successors.size() == 2;

                    Proj firstProjection = (Proj) successors.get(0);
                    Proj secondProjection = (Proj) successors.get(1);

                    Proj trueProj = null;
                    Proj falseProj = null;

                    if (firstProjection.getNum() == Cond.pnTrue) trueProj = firstProjection;
                    else if (firstProjection.getNum() == Cond.pnFalse) falseProj = firstProjection;
                    else {
                        assert false : "Unknown proj for Cond node!";
                    }

                    if (secondProjection.getNum() == Cond.pnTrue) trueProj = secondProjection;
                    else if (secondProjection.getNum() == Cond.pnFalse) falseProj = secondProjection;
                    else {
                        assert false : "Unknown proj for Cond node!";
                    }

                    assert trueProj != null && falseProj != null;

                    Block trueBlock = (Block) getSuccessorsOf(trueProj).get(0);
                    Block falseBlock = (Block) getSuccessorsOf(falseProj).get(0);

                    Node badNode = graph.newBad(Mode.getX());

                    if (condition.getTarval().equals(TargetValue.getBTrue())) {
                        Node jump = construction.newJmp();
                        Graph.exchange(node, jump);

                        for (int i = 0; i < trueBlock.getPredCount(); i++) {
                            if (trueBlock.getPred(i).equals(trueProj)) {
                                trueBlock.setPred(i, jump);
                            }
                        }

                        for (int i = 0; i < falseBlock.getPredCount(); i++) {
                            if (falseBlock.getPred(i).equals(falseProj)) {
                                falseBlock.setPred(i, badNode);
                            }
                        }
                    }
                    else if (condition.getTarval().equals(TargetValue.getBFalse())) {
                        Node jump = construction.newJmp();
                        Graph.exchange(node, jump);

                        for (int i = 0; i < trueBlock.getPredCount(); i++) {
                            if (trueBlock.getPred(i).equals(trueProj)) {
                                trueBlock.setPred(i, badNode);
                            }
                        }

                        for (int i = 0; i < falseBlock.getPredCount(); i++) {
                            if (falseBlock.getPred(i).equals(falseProj)) {
                                falseBlock.setPred(i, jump);
                            }
                        }
                    }
                    // Else: control flow is not constant
                }
            }
        }

        construction.finish();

        firm.bindings.binding_irgopt.remove_bads(graph.ptr);

        // This creates more bad nodes, so we need to remove them again
        firm.bindings.binding_irgopt.remove_unreachable_code(graph.ptr);

        firm.bindings.binding_irgopt.remove_bads(graph.ptr);
    }

    private TargetValue resultOfLastVisitedNode = null;
    private boolean hasModifiedGraph = false;

    public boolean hasModifiedGraph() {
        return this.hasModifiedGraph;
    }

    private void iterate() {
        while (!this.isWorklistEmpty()) {
            this.resultOfLastVisitedNode = null;

            Node node = this.removeElementFromWorklist();
            node.accept(this);

            assert this.resultOfLastVisitedNode != null;

            // If a change was made, make sure to revisit affected edges.
            if (this.setValueForNode(node, this.resultOfLastVisitedNode)) {
                for (BackEdges.Edge edge : BackEdges.getOuts(node)) {
                    this.addElementToWorklist(edge.node);
                }
            }
        }
    }

    private Optional<Node> getMemoryUsedByOperation(Node node) {
        Node memory = null;

        if (node instanceof Div) {
            memory = ((Div)node).getMem();
        }
        else if (node instanceof Mod) {
            memory = ((Mod)node).getMem();
        }

        return Optional.ofNullable(memory);
    }

    @Override
    public void visit(Const node) {
        assert this.getValueForNode(node) == UNDEFINED;

        this.resultOfLastVisitedNode = node.getTarval();
    }

    @Override
    public void visit(Add node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::add);

        this.print("+", left, right);
    }

    @Override
    public void visit(Sub node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::sub);

        this.print("-", left, right);
    }

    @Override
    public void visit(Mul node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::mul);

        this.print("*", left, right);
    }

    @Override
    public void visit(Div node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());
        TargetValue zero = new TargetValue(0, Mode.getIs());

        // Division by zero is undefined, we choose zero as result.
        if (!right.equals(zero)) {
            this.resultOfLastVisitedNode = fold(left, right, TargetValue::div);
        }
        else {
            this.resultOfLastVisitedNode = zero;
        }

        this.print("/", left, right);
    }

    @Override
    public void visit(Mod node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());
        TargetValue zero = new TargetValue(0, Mode.getIs());

        // Division by zero is undefined, we choose zero as result.
        if (!right.equals(zero)) {
            this.resultOfLastVisitedNode = fold(left, right, TargetValue::mod);
        }
        else {
            this.resultOfLastVisitedNode = zero;
        }

        this.print("%", left, right);
    }

    @Override
    public void visit(Minus node) {
        TargetValue other = this.getValueForNode(node.getOp());

        this.resultOfLastVisitedNode = fold(other, TargetValue::neg);

        this.print("-", other);
    }

    @Override
    public void visit(And node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::and);

        this.print("&&", left, right);
    }

    @Override
    public void visit(Or node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::or);

        this.print("||", left, right);
    }

    @Override
    public void visit(Not node) {
        TargetValue other = this.getValueForNode(node.getOp());

        this.resultOfLastVisitedNode = fold(other, TargetValue::not);

        this.print("!", other);
    }

    @Override
    public void visit(Cmp node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, (lhs, rhs) -> {
            if (node.getRelation().contains(left.compare(right))) {
                return TargetValue.getBTrue();
            }
            else {
                return TargetValue.getBFalse();
            }
        });

        this.print(node.getRelation().toString(), left, right);
    }

    @Override
    public void visit(Proj projection) {
        if (projection.getMode().equals(Mode.getM())) {
            this.defaultVisit(projection);
        }
        else {
            this.resultOfLastVisitedNode = this.getValueForNode(projection.getPred());
        }
    }

    @Override
    public void visit(Phi node) {
        TargetValue result = UNDEFINED;

        for (Node predecessor : node.getPreds()) {
            TargetValue value = this.getValueForNode(predecessor);

            result = join(result, value);
        }

        this.resultOfLastVisitedNode = result;
    }

    @Override
    public void visit(Conv node) {
        assert node.getPredCount() == 1;

        TargetValue oldValue = this.getValueForNode(node.getPred(0));
        TargetValue newValue;

        if (oldValue.isConstant()) {
            newValue = oldValue.convertTo(node.getMode());
        }
        else {
            newValue = oldValue;
        }

        this.resultOfLastVisitedNode = newValue;
    }

    @Override
    public void defaultVisit(Node node) {
        this.resultOfLastVisitedNode = NOT_A_CONSTANT;
    }

    private void print(String operator, TargetValue lhs, TargetValue rhs) {
        //System.out.println(describe(lhs) + " " + operator + " " + describe(rhs) + " = " +
        //        describe(this.resultOfLastVisitedNode));
    }

    private void print(String operator, TargetValue value) {
        //System.out.println(operator + describe(value) + " = " + describe(this.resultOfLastVisitedNode));
    }
}
