package edu.kit.minijava.backend;

import java.util.*;

import firm.Relation;
import firm.nodes.*;

public class TransformVisitor implements NodeVisitor {
    // CONSTANTS
    private String newlineCmd = "\n    ";

    // ATTRIBUTES
    private String molkiCode = "";

    private HashMap<Address, String> ptr2Name = new HashMap<>();
    // primarily for jmps to look up the block where they jump to
    private HashMap<Node, String> jmp2BlockName;
    // primarily for projections to save their register index
    private HashMap<Node, Integer> proj2regIndex;
    private HashMap<Node, Integer> blockToPhiReg;

    private Node currentBlock = null;

    // GETTERS & SETTERS
    public String getMolkiCode() {
        return this.molkiCode;
    }

    public void appendMolkiCode(String molkiCode) {
        this.molkiCode += molkiCode;
    }

    public TransformVisitor(HashMap<Node, String> jmp2BlockName, HashMap<Node, Integer> proj2regIndex,
            HashMap<Node, Integer> blockToPhiReg) {
        this.jmp2BlockName = jmp2BlockName;
        this.proj2regIndex = proj2regIndex;
        this.blockToPhiReg = blockToPhiReg;
    }

    // METHODS
    @Override
    public void visit(Add add) {
        add.getLeft().accept(this);
        add.getRight().accept(this);

        int left = this.proj2regIndex.get(add.getLeft());
        int right = this.proj2regIndex.get(add.getRight());
        int currentIndex = this.proj2regIndex.get(add);

        this.appendMolkiCode(this.newlineCmd + "add [ %@" + left + " | %@" + right + " ] -> %@" + currentIndex);
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

        if (this.currentBlock != null && this.blockToPhiReg.containsKey(this.currentBlock)) {
            // TODO: add mov command
            this.appendMolkiCode("\n phi mov");
        }

        String name = "Block_" + block.getNr();
        this.appendMolkiCode("\n" + name + ":");
    }

    @Override
    public void visit(Builtin builtin) {

    }

    @Override
    public void visit(Call call) {
        call.getPtr().accept(this);

        String functionName = this.ptr2Name.get(call.getPtr());

        // TODO: check, if this works as intended
        Iterator<Node> iter = call.getPreds().iterator();
        ArrayList<Node> parameters = new ArrayList<>();

        while (iter.hasNext()) {
            parameters.add(iter.next());
        }

        parameters.remove(0); // memory / side effect
        parameters.remove(0); // function address

        if (!functionName.equals("__minijava_main") && !functionName.equals("system_out_println")
                && !functionName.equals("system_out_write") && !functionName.equals("system_out_flush")
                && !functionName.equals("system_in_read") && !functionName.equals("alloc_mem")) {
            parameters.remove(0); // object pointer
        }

        String args = "";
        for (int i = 0; i < parameters.size(); i++) {
            // TODO: how to write parameters into registers without for example, getting a second `sub` expression
            // parameters.get(i).accept(this);
            // TODO: write parameter into register with same number as projection

            int arg = this.proj2regIndex.get(parameters.get(i));
            if (parameters.size() - i == 1) {
                args += arg;
            }
            else {
                args += arg + " | ";
            }
        }

        switch (functionName) {
            case "system_out_println":
                this.appendMolkiCode(this.newlineCmd + "call __stdlib_println [ " + args + " ]");
                break;
            case "system_out_write":
                this.appendMolkiCode(this.newlineCmd + "call __stdlib_write [ " + args + " ]");
                break;
            case "system_out_flush":
                int currentIndex = this.proj2regIndex.get(call);
                this.appendMolkiCode(this.newlineCmd + "call __stdlib_flush [ ] -> %@" + currentIndex);
                break;
            case "system_in_read":
                currentIndex = this.proj2regIndex.get(call);
                this.appendMolkiCode(this.newlineCmd + "call __stdlib_read [ " + args + " ] -> %@" + currentIndex);
                break;
            case "alloc_mem":
                currentIndex = this.proj2regIndex.get(call);
                this.appendMolkiCode(this.newlineCmd + "call __stdlib_calloc [ ] -> %@" + currentIndex);
                break;
            default:
                currentIndex = this.proj2regIndex.get(call);
                this.appendMolkiCode(
                        this.newlineCmd + "call " + functionName + " [ " + args + " ] " + "-> %@" + currentIndex);
        }
    }

    @Override
    public void visit(Cmp cmp) {
        // TODO: shouldn't there also be a jmp being generated here?
        cmp.getLeft().accept(this);
        int left = this.proj2regIndex.get(cmp.getLeft());
        cmp.getRight().accept(this);
        int right = this.proj2regIndex.get(cmp.getRight());

        this.appendMolkiCode(this.newlineCmd + "cmp %@" + left + ", %@" + right);
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
        String temp = "$" + String.valueOf(aConst.getTarval().asInt());
        int currentIndex = this.proj2regIndex.get(aConst);

        this.appendMolkiCode(this.newlineCmd + "mov " + temp + ", %@" + currentIndex);
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
        div.getLeft().accept(this);
        div.getRight().accept(this);
        int left = this.proj2regIndex.get(div.getLeft());
        int right = this.proj2regIndex.get(div.getRight());

        // TODO: how to handle div results properly?
        // 2 Zielregister
        this.appendMolkiCode(this.newlineCmd + "idiv [ %@" + left + " | %@" + right + " ]" + " -> [ %@"
                + this.proj2regIndex.get(div) + "]");
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
        this.appendMolkiCode(this.newlineCmd + "jmp " + name);
    }

    @Override
    public void visit(Load load) {
        // load.getPtr().accept(this);
        // String ptrResult = this.tempResult;

        // int currentIndex = this.registerIndex++;
        // this.appendMolkiCode(this.newlineCmd + "mov " + "(" + ptrResult + "), %@" + currentIndex);
        // this.tempResult = "%@" + currentIndex;
    }

    @Override
    public void visit(Member member) {

    }

    @Override
    public void visit(Minus minus) {
        // minus.getOp().accept(this);
        // this.appendMolkiCode( this.newlineCmd + "neg " + tempResult);
    }

    @Override
    public void visit(Mod mod) {
        // mod.getLeft().accept(this);
        // String left = this.tempResult;
        // mod.getRight().accept(this);
        // String right = this.tempResult;
        // int left = this.proj2regIndex.get(cmp.getLeft());
        // int right = this.proj2regIndex.get(cmp.getRight());

        //// TODO: how to handle mod results properly?
        // this.appendMolkiCode(this.newlineCmd + "imod [ " + left + " | " + right + " ]"
        // + " -> [ %@" + this.registerIndex++ + " | %@" + this.registerIndex++ + "]");
    }

    @Override
    public void visit(Mul mul) {
        // mul.getLeft().accept(this);
        // String left = this.tempResult;
        // mul.getRight().accept(this);
        // String right = this.tempResult;
        // int left = this.proj2regIndex.get(cmp.getLeft());
        // int right = this.proj2regIndex.get(cmp.getRight());

        // int currentIndex = this.registerIndex++;

        // this.appendMolkiCode(this.newlineCmd + "mul [ " + left + " | " + right + " ] -> %@" + currentIndex);
        // this.tempResult = "%@" + currentIndex;
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
        // not.getOp().accept(this);
        // this.appendMolkiCode(this.newlineCmd + "not " + tempResult);
    }

    @Override
    public void visit(Offset offset) {

    }

    @Override
    public void visit(Or or) {

    }

    @Override
    public void visit(Phi phi) {
        // TODO: what to do here?
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
            this.appendMolkiCode(this.newlineCmd + "mov %@" + this.proj2regIndex.get(aReturn) + ", %@r0");

            // check, if we should produce a jmp
            if (!(this.jmp2BlockName.get(aReturn) == null) && !currentBlock.equals(aReturn.getPred(0).getBlock())) {

                // check if we are able to produce a jmp
                if (aReturn.getBlock().getPred(0).getPred(0).getPred(0) instanceof Cmp) {
                    Cmp tempCmp = (Cmp) aReturn.getBlock().getPred(0).getPred(0).getPred(0);

                    if (tempCmp.getRelation().equals(Relation.Less)) {
                        this.appendMolkiCode(this.newlineCmd + "jl " + this.jmp2BlockName.get(aReturn));
                    }
                    else if (tempCmp.getRelation().equals(Relation.LessEqual)) {
                        this.appendMolkiCode(this.newlineCmd + "jle " + this.jmp2BlockName.get(aReturn));
                    }
                    else if (tempCmp.getRelation().equals(Relation.GreaterEqual)) {
                        this.appendMolkiCode(this.newlineCmd + "jg " + this.jmp2BlockName.get(aReturn));
                    }
                    else if (tempCmp.getRelation().equals(Relation.GreaterEqual)) {
                        this.appendMolkiCode(this.newlineCmd + "jge " + this.jmp2BlockName.get(aReturn));
                    }
                    else if (tempCmp.getRelation().equals(Relation.Equal)) {
                        this.appendMolkiCode(this.newlineCmd + "je " + this.jmp2BlockName.get(aReturn));
                    }
                    // TODO: what is the diff between `negated` and `inversed`?
                    else if (tempCmp.getRelation().equals(Relation.Equal.negated())) {
                        this.appendMolkiCode(this.newlineCmd + "jne " + this.jmp2BlockName.get(aReturn));
                    }
                }
                else {
                    this.appendMolkiCode(this.newlineCmd + "jmp " + this.jmp2BlockName.get(aReturn));
                }
            }
        }
    }

    @Override
    public void visit(Sel sel) {
        // sel.getPtr().accept(this);
        // String ptrResult = this.tempResult;
        // sel.getIndex().accept(this);
        // String indexResult = this.tempResult;

        // int currentIndex = this.registerIndex++;

        // this.appendMolkiCode(this.newlineCmd + "mov " + indexResult + "(" + ptrResult + "), %@" + currentIndex);
        // this.tempResult = "%@" + currentIndex;
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
        // String value = this.tempResult;
        // store.getPtr().accept(this);
        // String ptrResult = this.tempResult;

        // int currentIndex = this.registerIndex++;

        // this.appendMolkiCode(this.newlineCmd + "mov " + value + ", (" + ptrResult + ")");
    }

    @Override
    public void visit(Sub sub) {
        sub.getLeft().accept(this);
        sub.getRight().accept(this);
        int left = this.proj2regIndex.get(sub.getLeft());
        int right = this.proj2regIndex.get(sub.getRight());

        int currentIndex = this.proj2regIndex.get(sub);

        // RIGHT AND LEFT ARE SWITCHED BECAUSE OF ASSEMBLER SYNTAX
        this.appendMolkiCode(this.newlineCmd + "sub [ %@" + left + " | %@" + right + " ] -> %@" + currentIndex);
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
}
