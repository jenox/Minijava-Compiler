package edu.kit.minijava.backend;

import java.util.*;

import firm.Relation;
import firm.nodes.*;

public class TransformVisitor implements NodeVisitor {
    // CONSTANTS
    private static final String NEW_LINE = "\n";
    private static final String INDENT = "    "; // 4 spaces
    private static final String REG_PREFIX = "%@";
    private static final String CONST_PREFIX = "$";

    // ATTRIBUTES
    private StringBuilder molkiCode = new StringBuilder();

    private HashMap<Address, String> ptr2Name = new HashMap<>();
    // primarily for jmps to look up the block where they jump to
    private HashMap<Node, String> jmp2BlockName;
    // primarily for projections to save their register index
    private HashMap<Node, Integer> nodeToRegIndex;
    private HashMap<Node, List<Integer>> nodeToPhiReg;

    private Node currentBlock = null;

    // GETTERS & SETTERS
    public String getMolkiCode() {
        return this.molkiCode.toString();
    }

    /**
     * inserts given string to ouput.
     *
     * @param molkiCode string inserted with correct indentation and linebreak at end.
     */
    public void appendMolkiCode(String molkiCode) {
        this.molkiCode.append(INDENT + molkiCode + NEW_LINE);
    }

    public void appendMolkiCodeNoIndent(String code) {
        this.molkiCode.append(code + NEW_LINE);
    }

    public TransformVisitor(HashMap<Node, String> jmp2BlockName, HashMap<Node, Integer> proj2regIndex,
            HashMap<Node, List<Integer>> nodeToPhiReg) {
        this.jmp2BlockName = jmp2BlockName;
        this.nodeToRegIndex = proj2regIndex;
        this.nodeToPhiReg = nodeToPhiReg;
    }

    // METHODS
    @Override
    public void visit(Add add) {
        add.getLeft().accept(this);
        add.getRight().accept(this);

        int srcReg1 = this.nodeToRegIndex.get(add.getLeft());
        int srcReg2 = this.nodeToRegIndex.get(add.getRight());
        int targetReg = this.nodeToRegIndex.get(add);

        this.appendThreeAdressCommand("add", srcReg1, srcReg2, targetReg);
    }

    @Override
    public void visit(Address address) {
        this.ptr2Name.put(address, address.getEntity().getName());
    }

    @Override
    public void visit(Align align) {

    }

    @Override
    public void visit(Alloc alloc) {

    }

    @Override
    public void visit(Anchor anchor) {

    }

    @Override
    public void visit(And and) {

    }

    @Override
    public void visit(Bad bad) {

    }

    @Override
    public void visit(Bitcast bitcast) {

    }

    @Override
    public void visit(Block block) {

        if (this.currentBlock != null && this.nodeToPhiReg.containsKey(this.currentBlock)) {
            int sourceReg = this.nodeToRegIndex.get(block);
            for (int targetReg : this.nodeToPhiReg.get(this.currentBlock)) {
                this.appendTwoAdressCommand("mov", sourceReg, targetReg);
            }
        }

        this.currentBlock = block;

        String name = "L" + block.getNr();
        this.appendMolkiCodeNoIndent(name + ":");
    }

    @Override
    public void visit(Builtin builtin) {

    }

    @Override
    public void visit(Call call) {

        String functionName = this.ptr2Name.get(call.getPtr());

        // TODO: check, if this works as intended

        // ignore first two preds, ie memory and function adress
        int start = 2;

        if (!functionName.equals("__minijava_main") && !functionName.equals("system_out_println")
                && !functionName.equals("system_out_write") && !functionName.equals("system_out_flush")
                && !functionName.equals("system_in_read") && !functionName.equals("alloc_mem")) {
            start++; // ignore this pred, which is object pointer
        }

        String args = "";

        for (int i = start; i < call.getPredCount(); i++) {

            // TODO: how to write parameters into registers without for example, getting a second `sub` expression
            // parameters.get(i).accept(this);
            // TODO: write parameter into register with same number as projection

            int arg = this.nodeToRegIndex.get(call.getPred(i));
            if (call.getPredCount() - i == 1) {
                args += arg;
            }
            else {
                args += arg + " | ";
            }

        }

        switch (functionName) {
            case "system_out_println":
                this.appendMolkiCode("call __stdlib_println [ " + args + " ]");
                break;
            case "system_out_write":
                this.appendMolkiCode("call __stdlib_write [ " + args + " ]");
                break;
            case "system_out_flush":
                int targetReg = this.nodeToRegIndex.get(call);
                this.appendMolkiCode("call __stdlib_flush [ ] -> %@" + targetReg);
                break;
            case "system_in_read":
                targetReg = this.nodeToRegIndex.get(call);
                this.appendMolkiCode("call __stdlib_read [ " + args + " ] -> %@" + targetReg);
                break;
            case "alloc_mem":
                targetReg = this.nodeToRegIndex.get(call);
                this.appendMolkiCode("call __stdlib_calloc [ ] -> %@" + targetReg);
                break;
            default:
                targetReg = this.nodeToRegIndex.get(call);
                this.appendMolkiCode("call " + functionName + " [ " + args + " ] " + "-> %@" + targetReg);
        }
    }

    @Override
    public void visit(Cmp cmp) {
        int left = this.nodeToRegIndex.get(cmp.getLeft());
        int right = this.nodeToRegIndex.get(cmp.getRight());

        this.appendMolkiCode("cmp %@" + left + ", %@" + right);
    }

    @Override
    public void visit(Cond cond) {
    }

    @Override
    public void visit(Confirm confirm) {
    }

    @Override
    public void visit(Const aConst) {
        // TODO: should this use `asInt`?
        // was passiert bei booleans?
        String constant = CONST_PREFIX + String.valueOf(aConst.getTarval().asInt());
        int targetReg = this.nodeToRegIndex.get(aConst);

        this.appendMolkiCode("mov " + constant + ", %@" + targetReg);
    }

    @Override
    public void visit(Conv conv) {

    }

    @Override
    public void visit(CopyB copyB) {

    }

    @Override
    public void visit(Deleted deleted) {

    }

    @Override
    public void visit(Div div) {
        int left = this.nodeToRegIndex.get(div.getLeft());
        int right = this.nodeToRegIndex.get(div.getRight());

        int targetReg1 = this.nodeToRegIndex.get(div);
        int targetReg2 = targetReg1 + 1; // by convention used in PrepVisitor

        this.appendMolkiCode("idiv [ %@" + left + " | %@" + right + " ]" + " -> [ %@" + targetReg1 + ", " + REG_PREFIX
                + targetReg2 + "]");
    }

    @Override
    public void visit(Dummy dummy) {

    }

    @Override
    public void visit(End end) {

    }

    @Override
    public void visit(Eor eor) {

    }

    @Override
    public void visit(Free free) {

    }

    @Override
    public void visit(IJmp iJmp) {

    }

    @Override
    public void visit(Id id) {

    }

    @Override
    public void visit(Jmp jmp) {
        String name = this.jmp2BlockName.get(jmp);
        this.appendMolkiCode("jmp " + name);
    }

    @Override
    public void visit(Load load) {
        int pointerReg = this.nodeToRegIndex.get(load.getPtr());
        int targetReg = this.nodeToRegIndex.get(load);

        this.appendMolkiCode("mov " + "(" + REG_PREFIX + pointerReg + "), " + REG_PREFIX + targetReg);
    }

    @Override
    public void visit(Member member) {

    }

    @Override
    public void visit(Minus minus) {
        int reg = this.nodeToRegIndex.get(minus.getOp());
        this.appendMolkiCode("neg " + REG_PREFIX + reg);
    }

    @Override
    public void visit(Mod mod) {
        int srcReg1 = this.nodeToRegIndex.get(mod.getLeft());
        int srcReg2 = this.nodeToRegIndex.get(mod.getRight());

        int targetReg1 = this.nodeToRegIndex.get(mod);
        int targetReg2 = targetReg1 + 1; // by convention used in PrepVisitor

        this.appendMolkiCode("imod [ " + REG_PREFIX + srcReg1 + " | " + REG_PREFIX + srcReg2 + " ]" + " -> [ %@"
                + REG_PREFIX + targetReg1 + " | %@" + targetReg2 + "]");
    }

    @Override
    public void visit(Mul mul) {
        int srcReg1 = this.nodeToRegIndex.get(mul.getLeft());
        int srcReg2 = this.nodeToRegIndex.get(mul.getRight());

        int targetReg = this.nodeToRegIndex.get(mul);

        this.appendThreeAdressCommand("mul", srcReg1, srcReg2, targetReg);
    }

    @Override
    public void visit(Mulh mulh) {

    }

    @Override
    public void visit(Mux mux) {

    }

    @Override
    public void visit(NoMem noMem) {

    }

    @Override
    public void visit(Not not) {
        int reg = this.nodeToRegIndex.get(not.getOp());
        this.appendMolkiCode("not " + REG_PREFIX + reg);
    }

    @Override
    public void visit(Offset offset) {

    }

    @Override
    public void visit(Or or) {

    }

    @Override
    public void visit(Phi phi) {
        // nothing to do
    }

    @Override
    public void visit(Pin pin) {

    }

    @Override
    public void visit(Proj proj) {
    }

    @Override
    public void visit(Raise raise) {

    }

    @Override
    public void visit(Return aReturn) {
        Node currentBlock = aReturn.getBlock();
        Block startBlock = currentBlock.getGraph().getStartBlock();

        // check, if we're the main function
        if (!currentBlock.equals(startBlock)) {
            this.appendMolkiCode("mov %@" + this.nodeToRegIndex.get(aReturn) + ", %@r0"); // TODO: wieso r0?

            // check, if we should produce a jmp
            if (!(this.jmp2BlockName.get(aReturn) == null) && !currentBlock.equals(aReturn.getPred(0).getBlock())) {

                // check if we are able to produce a jmp
                // TODO: if Bedingung überprüfen / vereinfachen
                if (aReturn.getBlock().getPred(0).getPred(0).getPred(0) instanceof Cmp) {
                    Cmp tempCmp = (Cmp) aReturn.getBlock().getPred(0).getPred(0).getPred(0);

                    if (tempCmp.getRelation().equals(Relation.Less)) {
                        this.appendMolkiCode("jl " + this.jmp2BlockName.get(aReturn));
                    }
                    else if (tempCmp.getRelation().equals(Relation.LessEqual)) {
                        this.appendMolkiCode("jle " + this.jmp2BlockName.get(aReturn));
                    }
                    else if (tempCmp.getRelation().equals(Relation.GreaterEqual)) {
                        this.appendMolkiCode("jg " + this.jmp2BlockName.get(aReturn));
                    }
                    else if (tempCmp.getRelation().equals(Relation.GreaterEqual)) {
                        this.appendMolkiCode("jge " + this.jmp2BlockName.get(aReturn));
                    }
                    else if (tempCmp.getRelation().equals(Relation.Equal)) {
                        this.appendMolkiCode("je " + this.jmp2BlockName.get(aReturn));
                    }
                    // TODO: what is the diff between `negated` and `inversed`?
                    else if (tempCmp.getRelation().equals(Relation.Equal.negated())) {
                        this.appendMolkiCode("jne " + this.jmp2BlockName.get(aReturn));
                    }
                }
                else {
                    this.appendMolkiCode("jmp " + this.jmp2BlockName.get(aReturn));
                }
            }
        }
    }

    @Override
    public void visit(Sel sel) {
        int pointerReg = this.nodeToRegIndex.get(sel.getPtr());
        int indexReg = this.nodeToRegIndex.get(sel.getIndex());
        int targetReg = this.nodeToRegIndex.get(sel);

        String indexString = REG_PREFIX + indexReg;
        String pointerString = REG_PREFIX + pointerReg;
        String targetString = REG_PREFIX + targetReg;

        this.appendMolkiCode("mov " + indexString + "(" + pointerString + "), %@" + targetString);
    }

    @Override
    public void visit(Shl shl) {

    }

    @Override
    public void visit(Shr shr) {

    }

    @Override
    public void visit(Shrs shrs) {

    }

    @Override
    public void visit(Size size) {

    }

    @Override
    public void visit(Start start) {

    }

    @Override
    public void visit(Store store) {
        int pointerReg = this.nodeToRegIndex.get(store.getPtr());
        int storeReg = this.nodeToRegIndex.get(store);

        this.appendMolkiCode("mov " + REG_PREFIX + storeReg + ", (" + REG_PREFIX + pointerReg + ")");
    }

    @Override
    public void visit(Sub sub) {
        int srcReg1 = this.nodeToRegIndex.get(sub.getLeft());
        int srcReg2 = this.nodeToRegIndex.get(sub.getRight());

        int targetReg = this.nodeToRegIndex.get(sub);

        this.appendThreeAdressCommand("sub", srcReg1, srcReg2, targetReg);
    }

    @Override
    public void visit(Switch aSwitch) {

    }

    @Override
    public void visit(Sync sync) {

    }

    @Override
    public void visit(Tuple tuple) {

    }

    @Override
    public void visit(Unknown unknown) {

    }

    @Override
    public void visitUnknown(Node node) {

    }

    /*
     * UTILITY FUNCTIONS
     */

    private void appendTwoAdressCommand(String cmd, int srcReg, int targetReg) {
        this.molkiCode.append(cmd).append(" " + REG_PREFIX + srcReg).append(", " + REG_PREFIX + targetReg);
        this.molkiCode.append(NEW_LINE);

    }

    public void appendThreeAdressCommand(String cmd, int srcReg1, int srcReg2, int targetReg) {
        this.appendMolkiCode(cmd + " [ %@" + srcReg1 + " | %@" + srcReg2 + " ] -> %@" + targetReg);
    }

}
