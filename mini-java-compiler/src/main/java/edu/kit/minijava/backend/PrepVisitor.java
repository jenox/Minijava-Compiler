package edu.kit.minijava.backend;

import java.util.*;

import firm.BackEdges;
import firm.Graph;
import firm.Mode;
import firm.nodes.*;
import firm.nodes.NodeVisitor.Default;

public class PrepVisitor extends Default {
    // ATTRIBUTES
    private int registerIndex = 0;
    private HashMap<Node, Integer> node2regIndex = new HashMap<>();
    private HashMap<Node, List<Integer>> nodeToPhiReg = new HashMap<>();
    private HashMap<Integer, List<Node>> blockId2Nodes = new HashMap<>();
    private HashMap<Graph, List<Integer>> graph2BlockId = new HashMap<>();

    // GETTERS
    public HashMap<Node, Integer> getProj2regIndex() {
        return this.node2regIndex;
    }
    public HashMap<Node, List<Integer>> getBlockToPhiReg() {
        return this.nodeToPhiReg;
    }
    public HashMap<Integer, List<Node>> getBlockId2Nodes() {
        return this.blockId2Nodes;
    }
    public HashMap<Graph, List<Integer>> getGraph2BlockId() {
        return graph2BlockId;
    }

    @Override
    public void visit(Add add) {
        int currentIndex = this.registerIndex++;
        this.node2regIndex.put(add, currentIndex);

        this.addInstrToBlock(null, add);
    }

    @Override
    public void visit(Address address) {
        String name = address.getEntity().getName().replace('.', '_');
    }

    @Override
    public void visit(Block block) {
        if (this.graph2BlockId.get(block.getGraph()) == null) {
            this.graph2BlockId.put(block.getGraph(), new ArrayList<>());
        }
        this.graph2BlockId.get(block.getGraph()).add(block.getNr());

        if (this.blockId2Nodes.get(block.getNr()) == null) {
            this.blockId2Nodes.put(block.getNr(), new ArrayList<>());
        }
    }

    @Override
    public void visit(Call call) {
        // allocate register for call, regardless of whether is is used or not
        this.node2regIndex.put(call, this.registerIndex);
        this.registerIndex++;

        this.addInstrToBlock(null, call);
    }

    @Override
    public void visit(Cmp cmp) {
        // nothing to do
        this.addInstrToBlock(null, cmp);
    }

    @Override
    public void visit(Const aConst) {
        this.node2regIndex.put(aConst, this.registerIndex++);

        // just calling `.getBlock.getNr()` leads to false numbers, really weird
        // a constant should normally have a successor which is using it
        int blockNr = aConst.getBlock().getNr();
        for (BackEdges.Edge e : BackEdges.getOuts(aConst)) {
            if (e.node.getBlock().getNr() != aConst.getBlock().getNr()) {
                blockNr = e.node.getBlock().getNr();
            }
        }

        this.addInstrToBlock(blockNr, aConst);
    }

    @Override
    public void visit(Div div) {
        // we need two target registers for div. These are registerIndex and registerIndex + 1.
        // Assignment of registerIndex+1 is not represented explicitly.
        this.node2regIndex.put(div, this.registerIndex);

        this.registerIndex += 2;

        this.addInstrToBlock(null, div);
    }

    @Override
    public void visit(Mod mod) {
        // we need two target registers for mod. These are registerIndex and registerIndex + 1.
        // Assignment of registerIndex+1 is not represented explicitly.
        this.node2regIndex.put(mod, this.registerIndex);

        this.registerIndex += 2;

        this.addInstrToBlock(null, mod);
    }

    @Override
    public void visit(Phi phi) {
        //for (Node node : phi.getPreds()) {
        //    this.appendList(node, this.registerIndex);
        //}
        this.node2regIndex.put(phi, this.registerIndex++);

        this.addInstrToBlock(null, phi);
    }

    @Override
    public void visit(Proj proj) {
        // this projection is parameter if predecessor is args node
        boolean isParam = proj.getGraph().getArgs().equals(proj.getPred());
        int register = 0;
        if (isParam) {
            register = proj.getNum() - 1; // TODO: sollte eigentlich gehen
        }
        else {
            Node pred = proj.getPred();
            // no memory predecessor
            if (pred.getPredCount() == 1 && !proj.getMode().equals(Mode.getX())
                            && !proj.getMode().equals(Mode.getT())
                            && !proj.getMode().equals(Mode.getM())) {
                register = this.node2regIndex.get(pred.getPred(0));
            }
            // existing memory predecessor
            else if (pred.getPredCount() > 1 && !proj.getMode().equals(Mode.getX())
                        && !proj.getMode().equals(Mode.getT())
                        && !proj.getMode().equals(Mode.getM())) {
                    register = this.node2regIndex.get(pred.getPred(1));
            }
            else {
                return;
            }
        }

        this.node2regIndex.put(proj, register);

        this.addInstrToBlock(null, proj);
    }

    @Override
    public void visit(Return aReturn) {
        this.node2regIndex.put(aReturn, this.registerIndex++);

        this.addInstrToBlock(null, aReturn);
    }

    @Override
    public void visit(Sel sel) {
        this.node2regIndex.put(sel, this.registerIndex++);

        this.addInstrToBlock(null, sel);
    }

    @Override
    public void visit(Sub sub) {
        this.node2regIndex.put(sub, this.registerIndex++);

        this.addInstrToBlock(null, sub);
    }

    /**
     * add mapping from node to register of phi instruction
     *
     * @param node node to set target register for
     * @param reg  register
     */
    private void appendList(Node node, int reg) {
        List<Integer> regs = this.nodeToPhiReg.get(node);
        if (regs == null) {
            regs = new ArrayList<>();
            this.nodeToPhiReg.put(node, regs);
        }

        regs.add(reg);
    }

    @Override
    public void visit(Start node) {
        // nothing to do
    }

    @Override
    public void visit(Cond node) {
        // nothing to do
    }

    @Override
    public void visit(End node) {
        // nothing to do
    }

    @Override
    public void visit(Jmp node) {
        this.addInstrToBlock(null, node);
    }

    @Override
    public void visit(Conv node) {
        // nothing to do
    }

    @Override
    public void visit(Member node) {
        this.node2regIndex.put(node, this.registerIndex++);

        this.addInstrToBlock(null, node);
    }

    @Override
    public void visit(Load node) {
        this.node2regIndex.put(node, this.registerIndex++);

        this.addInstrToBlock(null, node);
    }

    @Override
    public void visit(Not not) {
        int reg = this.node2regIndex.get(not.getOp());
        this.node2regIndex.put(not, reg);

        this.addInstrToBlock(null, not);
    }

    @Override
    public void defaultVisit(Node n) {
        throw new UnsupportedOperationException("unknown node: " + n + " " + n.getClass());
    }

    public void setRegisterIndex(int registerIndex) {
        this.registerIndex = registerIndex;
    }

    private void addInstrToBlock(Integer blockNr, Node node) {
        blockNr = blockNr == null ? node.getBlock().getNr() : blockNr;
        if (this.blockId2Nodes.get(blockNr) == null) {
            this.blockId2Nodes.put(blockNr, new ArrayList<>());
        }
        this.blockId2Nodes.get(blockNr).add(node);
    }

}
