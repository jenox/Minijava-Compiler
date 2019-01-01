        package edu.kit.minijava.backend;

import java.util.*;

import firm.BackEdges;
import firm.Mode;
import firm.Relation;
import firm.TargetValue;
import firm.nodes.*;
import firm.nodes.NodeVisitor.Default;

public class TransformVisitor extends Default {
    // CONSTANTS
    private static final String NEW_LINE = "\n";
    private static final String INDENT = "    "; // 4 spaces
    private static final String REG_PREFIX = "%@";
    private static final String CONST_PREFIX = "$";
    private int currentBlockNr;

    // ATTRIBUTES
    private HashMap<Integer, List<String>> molkiCode = new HashMap<>();

    // primarily for projections to save their register index
    private HashMap<Node, Integer> nodeToRegIndex;
    private HashMap<Node, List<Integer>> nodeToPhiReg;


    // GETTERS & SETTERS
    public HashMap<Integer, List<String>> getMolkiCode() {
        return this.molkiCode;
    }

    /**
     * inserts given string to ouput.
     *
     * @param molkiCode string inserted with correct indentation and linebreak at end.
     */
    public void appendMolkiCode(String molkiCode) {
        if (this.molkiCode.get(this.currentBlockNr) == null) {
            this.molkiCode.put(this.currentBlockNr, new ArrayList<>());
        }

        this.molkiCode.get(this.currentBlockNr).add(INDENT + molkiCode);
    }

    public void appendMolkiCode(String molkiCode, int blockNr) {
        this.molkiCode.get(blockNr).add(INDENT + molkiCode);
    }

    public TransformVisitor(HashMap<Node, Integer> proj2regIndex, HashMap<Node, List<Integer>> nodeToPhiReg) {
        this.nodeToRegIndex = proj2regIndex;
        this.nodeToPhiReg = nodeToPhiReg;
    }

    // METHODS
    //private boolean stackSlotAssigned(Node node) {
    //    return false;
    //}

    //private int getStackSlotOffset(Node node) {
    //    return 0;
    //}

    //private void getValue(Node node, int destReg) {
    //    if (stackSlotAssigned(node)) {
    //        int offset = getStackSlotOffset(node);
    //        System.out.printf("\tmovl %d(%%rbp), %@d # reload for %s\n", offset, destReg, node);
    //        return;
    //    }
    //    createValue(node);
    //}

    public void createValue(int blockNr, Node node) {
        this.currentBlockNr = blockNr;

        switch (node.getOpCode()) {
            case iro_Add:
                Add add = (Add) node;
                visit(add);
                break;
            case iro_Sub:
                Sub sub = (Sub) node;
                visit(sub);
                break;
            case iro_Mul:
                Mul mul = (Mul) node;
                visit(mul);
                break;
            case iro_Div:
                Div div = (Div) node;
                visit(div);
                break;
            case iro_Mod:
                Mod mod = (Mod) node;
                visit(mod);
                break;
            case iro_Address:
                Address address = (Address) node;
                visit(address);
                break;
            case iro_Call:
                Call call = (Call) node;
                visit(call);
                break;
            case iro_Cmp:
                Cmp cmp = (Cmp) node;
                visit(cmp);
                break;
            case iro_Const:
                Const aConst = (Const) node;
                visit(aConst);
                break;
            case iro_End:
                End aEnd = (End) node;
                visit(aEnd);
                break;
            case iro_Jmp:
                Jmp aJmp = (Jmp) node;
                visit(aJmp);
                break;
            case iro_Load:
                Load aLoad = (Load) node;
                visit(aLoad);
                break;
            case iro_Minus:
                Minus aMinus = (Minus) node;
                visit(aMinus);
                break;
            case iro_Not:
                Not aNot = (Not) node;
                visit(aNot);
                break;
            case iro_Phi:
                Phi aPhi = (Phi) node;
                visit(aPhi);
                break;
            case iro_Return:
                Return aReturn = (Return) node;
                visit(aReturn);
                break;
            case iro_Sel:
                Sel aSel = (Sel) node;
                visit(aSel);
                break;
            case iro_Start:
                Start aStart = (Start) node;
                visit(aStart);
                break;
            case iro_Store:
                Store aStore = (Store) node;
                visit(aStore);
                break;
            case iro_Proj:
                Proj aProj = (Proj) node;
                visit(aProj);
                break;
            case iro_Cond:
                Cond aCond = (Cond) node;
                visit(aCond);
                break;
            case iro_Member:
                Member aMember = (Member) node;
                visit(aMember);
                break;
            default:
                throw new UnsupportedOperationException("unknown node " + node.getClass());
        }
    }

    @Override
    public void visit(Add add) {
        int srcReg1 = this.nodeToRegIndex.get(add.getLeft());
        int srcReg2 = this.nodeToRegIndex.get(add.getRight());
        int targetReg = this.nodeToRegIndex.get(add);
        int blockNr = add.getBlock().getNr();

        this.appendThreeAdressCommand(blockNr, "add", srcReg1, srcReg2, targetReg);
    }

    @Override
    public void visit(Address address) {
        // nothing to do
    }

    @Override
    public void visit(Block block) {

        // TODO: what does this access to nodeToPhiReg do?
        //if (this.currentBlock != null && this.nodeToPhiReg.containsKey(this.currentBlock)) {
        //    int sourceReg = this.nodeToRegIndex.get(block);
        //    for (int targetReg : this.nodeToPhiReg.get(this.currentBlock)) {
        //        this.appendTwoAdressCommand(blockNr, "mov", sourceReg, targetReg);
        //    }
        //}

        //this.currentBlock = block;

        //String name = "L" + block.getNr();
        //this.appendMolkiCodeNoIndent(name + ":");
    }

    @Override
    public void visit(Call call) {

        Address address = (Address) call.getPred(1);
        String functionName = address.getEntity().getName().replace('.', '_');
        int blockNr = call.getBlock().getNr();

        // TODO: check, if this works as intended

        // ignore first two preds, ie memory and function adress
        int start = 2;

        if (!functionName.equals("minijava_main") && !functionName.equals("system_out_println")
                        && !functionName.equals("system_out_write") && !functionName.equals("system_out_flush")
                        && !functionName.equals("system_in_read") && !functionName.equals("alloc_mem")) {
            start++; // ignore this pred, which is object pointer
        }

        String args = "";

        for (int i = start; i < call.getPredCount(); i++) {

            int arg = this.nodeToRegIndex.get(call.getPred(i));
            if (call.getPredCount() - i == 1) {
                args += REG_PREFIX + arg;
            }
            else {
                args += REG_PREFIX + arg + " | ";
            }

        }

        switch (functionName) {
            case "system_out_println":
                this.appendMolkiCode("call __stdlib_println [ " + args + " ]");
                break;
            case "system_out_write":
                this.appendMolkiCode("# block nr " + call.getBlock().getNr());
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
                int srcReg1 = this.nodeToRegIndex.get(call.getPred(3));
                int srcReg2 = this.nodeToRegIndex.get(call.getPred(2));
                args = REG_PREFIX + srcReg1 + " | " + REG_PREFIX + srcReg2;
                this.appendMolkiCode("call __stdlib_calloc [ " + args + " ] -> %@" + targetReg);
                break;
            default:
                targetReg = this.nodeToRegIndex.get(call);
                this.appendMolkiCode("call " + functionName + " [ " + args + " ] " + "-> %@" + targetReg);
        }
    }

    @Override
    public void visit(Cmp cmp) {
        int srcReg1 = this.nodeToRegIndex.get(cmp.getRight());
        int srcReg2 = this.nodeToRegIndex.get(cmp.getLeft());
        int blockNr = cmp.getBlock().getNr();

        // cmp asm
        this.appendMolkiCode("cmp %@" + srcReg1 + ", %@" + srcReg2);

        // jmp asm
        int succBlockNr = -1;
        boolean hasReturn = false;
        boolean hasOnlyMemPred = false;
        List<Integer> blockNrs = new ArrayList<>();
        List<Integer> projNrs = new ArrayList<>();
        List<Integer> registerNrs = new ArrayList<>();

        // get Conditions
        for (BackEdges.Edge edge : BackEdges.getOuts(cmp)) {
            // get Projections
            for (BackEdges.Edge condEdge : BackEdges.getOuts(edge.node)) {
                // get Blocks
                for (BackEdges.Edge projEdge : BackEdges.getOuts(condEdge.node)) {
                    Block block = (Block) projEdge.node;

                    for (BackEdges.Edge blockEdge : BackEdges.getOuts(block)) {
                        if (blockEdge.node instanceof Return) {
                            Return aReturn = (Return) blockEdge.node;
                            hasReturn = true;
                            hasOnlyMemPred = aReturn.getPredCount() <= 1;

                            if (!hasOnlyMemPred) {
                                succBlockNr = block.getGraph().getEndBlock().getNr();
                                registerNrs.add(this.nodeToRegIndex.get(aReturn.getPred(1)));
                            }
                        }
                    }
                    blockNrs.add(block.getNr());
                    projNrs.add(condEdge.node.getNr());
                }
            }
            break;
        }

        // sometimes, firm switches the position of the proj nodes
        boolean projAreReversed = projNrs.get(0) > projNrs.get(1);
        int trueNr = projAreReversed ? 1 : 0;

        if (!hasReturn) {
            succBlockNr = blockNrs.get(trueNr);
        }
        else if (!hasOnlyMemPred){
            this.appendMolkiCode("mov %@" + registerNrs.get(trueNr) + ", %@r0");
        }
        else if (hasOnlyMemPred) {
            succBlockNr = blockNrs.get(trueNr);
        }

        if (cmp.getRelation().equals(Relation.Less)) {
            this.appendMolkiCode("jl L" + succBlockNr);
        }
        else if (cmp.getRelation().equals(Relation.LessEqual)) {
            this.appendMolkiCode("jle L" + succBlockNr);
        }
        else if (cmp.getRelation().equals(Relation.GreaterEqual)) {
            this.appendMolkiCode("jg L" + succBlockNr);
        }
        else if (cmp.getRelation().equals(Relation.GreaterEqual)) {
            this.appendMolkiCode("jge L" + succBlockNr);
        }
        else if (cmp.getRelation().equals(Relation.Equal)) {
            this.appendMolkiCode("je L" + succBlockNr);
        }
        // TODO: what is the diff between `negated` and `inversed`?
        else if (cmp.getRelation().equals(Relation.Equal.negated())) {
            this.appendMolkiCode("jne L" + succBlockNr);
        }
    }

    @Override
    public void visit(Const aConst) {
        int blockNr = aConst.getBlock().getNr();

        if (aConst.getMode().equals(Mode.getb())) {
            List<Integer> blockNrs = new ArrayList<>();
            List<Integer> projNrs = new ArrayList<>();

            // get Conditions
            for (BackEdges.Edge edge : BackEdges.getOuts(aConst)) {
                // get Projections
                for (BackEdges.Edge condEdge : BackEdges.getOuts(edge.node)) {
                    // get Blocks
                    for (BackEdges.Edge projEdge : BackEdges.getOuts(condEdge.node)) {
                        Block block = (Block) projEdge.node;
                        blockNrs.add(block.getNr());
                        projNrs.add(condEdge.node.getNr());
                    }
                }
                break;
            }

            // sometimes, firm switches the position of the proj nodes
            boolean projAreReversed = projNrs.get(0) > projNrs.get(1);
            int trueNr = projAreReversed ? 1 : 0;
            int falseNr = projAreReversed ? 0 : 1;

            if (aConst.getTarval().equals(TargetValue.getBTrue())) {
                this.appendMolkiCode("jmp L" + blockNrs.get(trueNr));
            }
            else {
                this.appendMolkiCode("jmp L" + blockNrs.get(falseNr));
            }
        }
        else {
            String constant = CONST_PREFIX + String.valueOf(aConst.getTarval().asInt());
            int targetReg = this.nodeToRegIndex.get(aConst);

            this.appendMolkiCode("mov " + constant + ", %@" + targetReg);
        }
    }

    @Override
    public void visit(Div div) {
        int left = this.nodeToRegIndex.get(div.getLeft());
        int right = this.nodeToRegIndex.get(div.getRight());

        int targetReg1 = this.nodeToRegIndex.get(div);
        int targetReg2 = targetReg1 + 1; // by convention used in PrepVisitor
        int blockNr = div.getBlock().getNr();

        this.appendMolkiCode("idiv [ %@" + left + " | %@" + right + " ]" + " -> [ %@" + targetReg1 + ", " + REG_PREFIX
                        + targetReg2 + "]");
    }

    @Override
    public void visit(End end) {

    }

    @Override
    public void visit(Jmp jmp) {
        int blockNr = jmp.getBlock().getNr();
        for (BackEdges.Edge edge : BackEdges.getOuts(jmp)) {
            Block block = (Block) edge.node;
            this.appendMolkiCode("jmp L" + block.getNr());
            break;
        }
    }



    @Override
    public void visit(Load load) {
        int pointerReg = this.nodeToRegIndex.get(load.getPtr());
        int targetReg = this.nodeToRegIndex.get(load);
        int blockNr = load.getBlock().getNr();

        this.appendMolkiCode("mov " + "(" + REG_PREFIX + pointerReg + "), " + REG_PREFIX + targetReg);
    }

    @Override
    public void visit(Minus minus) {
        int reg = this.nodeToRegIndex.get(minus.getOp());
        int blockNr = minus.getBlock().getNr();
        this.appendMolkiCode("neg " + REG_PREFIX + reg);
    }

    @Override
    public void visit(Mod mod) {
        int srcReg1 = this.nodeToRegIndex.get(mod.getLeft());
        int srcReg2 = this.nodeToRegIndex.get(mod.getRight());

        int targetReg1 = this.nodeToRegIndex.get(mod);
        int targetReg2 = targetReg1 + 1; // by convention used in PrepVisitor
        int blockNr = mod.getBlock().getNr();

        this.appendMolkiCode("imod [ " + REG_PREFIX + srcReg1 + " | " + REG_PREFIX + srcReg2 + " ]" + " -> [ %@"
                        + REG_PREFIX + targetReg1 + " | %@" + targetReg2 + "]");
    }

    @Override
    public void visit(Mul mul) {
        int srcReg1 = this.nodeToRegIndex.get(mul.getLeft());
        int srcReg2 = this.nodeToRegIndex.get(mul.getRight());

        int targetReg = this.nodeToRegIndex.get(mul);
        int blockNr = mul.getBlock().getNr();

        this.appendThreeAdressCommand(blockNr, "mul", srcReg1, srcReg2, targetReg);
    }

    @Override
    public void visit(Not not) {
        int reg = this.nodeToRegIndex.get(not.getOp());
        int blockNr = not.getBlock().getNr();
        this.appendMolkiCode("not " + REG_PREFIX + reg);
    }

    @Override
    public void visit(Phi phi) {
        int blockNr = phi.getBlock().getNr();

        if (!phi.getMode().equals(Mode.getM())) {
            // TODO: don't know what to do here
            // if preds are in the same block, just take the left one
            int predZeroBlockNr = phi.getPred(0).getBlock().getNr();
            int predOneBlockNr = phi.getPred(1).getBlock().getNr();


            if (predZeroBlockNr == predOneBlockNr) {
                this.appendMolkiCode("mov %@" + this.nodeToRegIndex.get(phi.getPred(0)) + ", %@" + this.nodeToRegIndex.get(phi));
            }
            else {
                // if preds are in different blocks, put store instructions there
                phi.getPreds().forEach(pred -> {
                    this.appendMolkiCode("mov %@" + this.nodeToRegIndex.get(pred) + ", %@" + this.nodeToRegIndex.get(phi), pred.getBlock().getNr());
                });
            }

        }
    }

    @Override
    public void visit(Return aReturn) {
        if (aReturn.getPredCount() == 2) {
            this.appendMolkiCode("mov %@" + this.nodeToRegIndex.get(aReturn.getPred(1)) + ", %@r0");
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
        int blockNr = sel.getBlock().getNr();

        this.appendMolkiCode("mov " + indexString + "(" + pointerString + "), %@" + targetString);
    }

    @Override
    public void visit(Start start) {
        //nothing to do
    }

    @Override
    public void visit(Store store) {
        int pointerReg = this.nodeToRegIndex.get(store.getPtr());
        int storeReg = this.nodeToRegIndex.get(store);
        int blockNr = store.getBlock().getNr();

        this.appendMolkiCode("mov " + REG_PREFIX + storeReg + ", (" + REG_PREFIX + pointerReg + ")");
    }

    @Override
    public void visit(Sub sub) {
        int srcReg1 = this.nodeToRegIndex.get(sub.getRight());
        int srcReg2 = this.nodeToRegIndex.get(sub.getLeft());

        int targetReg = this.nodeToRegIndex.get(sub);
        int blockNr = sub.getBlock().getNr();

        this.appendThreeAdressCommand(blockNr, "sub", srcReg1, srcReg2, targetReg);
    }

    @Override
    public void visit(Proj node) {
        //nothing to do
    }

    @Override
    public void visit(Cond node) {
        //nothing to do
    }

    @Override
    public void visit(Member node) {
        //nothing to do
    }



    @Override
    public void defaultVisit(Node n) {
        throw new UnsupportedOperationException("unknown node " + n.getClass());
    }
    /*
     * UTILITY FUNCTIONS
     */

    private void appendTwoAdressCommand(int blockNr, String cmd, int srcReg, int targetReg) {
        this.appendMolkiCode(cmd + " " + REG_PREFIX + srcReg + ", " + REG_PREFIX + targetReg);
        this.appendMolkiCode(NEW_LINE);

    }

    public void appendThreeAdressCommand(int blockNr, String cmd, int srcReg1, int srcReg2, int targetReg) {
        this.appendMolkiCode(cmd + " [ %@" + srcReg1 + " | %@" + srcReg2 + " ] -> %@" + targetReg);
    }
}
