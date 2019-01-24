package edu.kit.minijava.backend;

import java.util.*;

import firm.*;
import firm.nodes.*;
import firm.nodes.NodeVisitor.Default;

public class MolkiTransformer extends Default {
    // CONSTANTS
    private static final String INDENT = "    "; // 4 spaces
    private static final String REG_PREFIX = "%@";
    private static final String CONST_PREFIX = "$";
    private static final String REG_WIDTH_D = "d";
    private int currentBlockNr;

    // ATTRIBUTES
    private HashMap<Integer, List<String>> molkiCode = new HashMap<>();

    // primarily for projections to save their register index
    private HashMap<Node, Integer> node2RegIndex;
    private HashMap<Graph, Integer> graph2MaxBlockId;

    // GETTERS & SETTERS
    public HashMap<Integer, List<String>> getMolkiCode() {
        return this.molkiCode;
    }

    /**
     * inserts given string to ouput.
     *
     * @param molkiCode string inserted with correct indentation and linebreak at end.
     */
    private void appendMolkiCode(String molkiCode) {
        this.molkiCode.putIfAbsent(this.currentBlockNr, new ArrayList<>());
        this.molkiCode.get(this.currentBlockNr).add(INDENT + molkiCode);
    }

    private void appendMolkiCode(String molkiCode, int blockNr) {
        this.molkiCode.get(blockNr).add(INDENT + molkiCode);
    }

    public MolkiTransformer(HashMap<Node, Integer> proj2regIndex, HashMap<Graph, Integer> graph2MaxBlockId) {
        this.node2RegIndex = proj2regIndex;
        this.graph2MaxBlockId = graph2MaxBlockId;
    }

    public void createValue(int blockNr, Node node) {
        this.currentBlockNr = blockNr;

        switch (node.getOpCode()) {
            case iro_Add:
                Add add = (Add) node;
                this.molkify(add);
                break;
            case iro_Sub:
                Sub sub = (Sub) node;
                this.molkify(sub);
                break;
            case iro_Mul:
                Mul mul = (Mul) node;
                this.molkify(mul);
                break;
            case iro_Div:
                Div div = (Div) node;
                this.molkify(div);
                break;
            case iro_Mod:
                Mod mod = (Mod) node;
                this.molkify(mod);
                break;
            case iro_Address:
                Address address = (Address) node;
                this.molkify(address);
                break;
            case iro_Call:
                Call call = (Call) node;
                this.molkify(call);
                break;
            case iro_Cmp:
                Cmp cmp = (Cmp) node;
                this.molkify(cmp);
                break;
            case iro_Const:
                Const aConst = (Const) node;
                this.molkify(aConst);
                break;
            case iro_End:
                End aEnd = (End) node;
                this.molkify(aEnd);
                break;
            case iro_Jmp:
                Jmp aJmp = (Jmp) node;
                this.molkify(aJmp);
                break;
            case iro_Load:
                Load aLoad = (Load) node;
                this.molkify(aLoad);
                break;
            case iro_Minus:
                Minus aMinus = (Minus) node;
                this.molkify(aMinus);
                break;
            case iro_Not:
                Not aNot = (Not) node;
                this.molkify(aNot);
                break;
            case iro_Phi:
                Phi aPhi = (Phi) node;
                this.molkify(aPhi);
                break;
            case iro_Return:
                Return aReturn = (Return) node;
                this.molkify(aReturn);
                break;
            case iro_Sel:
                Sel aSel = (Sel) node;
                this.molkify(aSel);
                break;
            case iro_Start:
                Start aStart = (Start) node;
                this.molkify(aStart);
                break;
            case iro_Store:
                Store aStore = (Store) node;
                this.molkify(aStore);
                break;
            case iro_Proj:
                Proj aProj = (Proj) node;
                this.molkify(aProj);
                break;
            case iro_Cond:
                Cond aCond = (Cond) node;
                this.molkify(aCond);
                break;
            case iro_Member:
                Member aMember = (Member) node;
                this.molkify(aMember);
                break;
            case iro_Conv:
                Conv aConv = (Conv) node;
                this.molkify(aConv);
                break;
            default:
                throw new UnsupportedOperationException("unknown node " + node.getClass());
        }
    }

    private void molkify(Add add) {
        int srcReg1 = this.node2RegIndex.get(add.getLeft());
        int srcReg2 = this.node2RegIndex.get(add.getRight());
        int targetReg = this.node2RegIndex.get(add);

        this.appendThreeAdressCommand("addl", srcReg1, REG_WIDTH_D, srcReg2, REG_WIDTH_D, targetReg, REG_WIDTH_D);
    }

    private void molkify(Address address) {
        // nothing to do
    }

    private void molkify(Block block) {
        // nothing to do here
    }

    private void molkify(Call call) {

        Address address = (Address) call.getPred(1);
        String functionName = address.getEntity().getLdName();

        // Ignore first two preds, i.e. memory and function address
        int start = 2;

        String args = "";
        String regSuffix = "";

        // build args string
        for (int i = start; i < call.getPredCount(); i++) {
            regSuffix = Util.mode2RegSuffix(call.getPred(i).getMode());

            int arg = this.node2RegIndex.get(call.getPred(i));
            if (call.getPredCount() - i == 1) {
                args += REG_PREFIX + arg + regSuffix;
            }
            else {
                args += REG_PREFIX + arg + regSuffix + " | ";
            }
        }

        boolean isVoid = true;
        for (BackEdges.Edge edge : BackEdges.getOuts(call)) {
            if (edge.node.getMode().equals(Mode.getT())) {
                for (BackEdges.Edge projEdge : BackEdges.getOuts(edge.node)) {
                    regSuffix = Util.mode2RegSuffix(projEdge.node.getMode());
                }
                isVoid = false;
            }
        }


        int targetReg = this.node2RegIndex.get(call);

        if (isVoid) {
            this.appendMolkiCode("call " + functionName + " [ " + args + " ] ");
        }
        else {
            this.appendMolkiCode("call " + functionName + " [ " + args + " ] -> %@" + targetReg + regSuffix);
        }
    }

    private void molkify(Cmp cmp) {
        int srcReg1 = this.node2RegIndex.get(cmp.getLeft());
        int srcReg2 = this.node2RegIndex.get(cmp.getRight());

        String regSuffix = Util.mode2RegSuffix(cmp.getLeft().getMode());
        String cmpSuffix = Util.mode2MovSuffix(cmp.getLeft().getMode());

        this.appendMolkiCode("cmp" + cmpSuffix + " [ %@" + srcReg1 + regSuffix + " | %@" + srcReg2 + regSuffix + " ]");
    }

    private void molkify(Const aConst) {
        if (!aConst.getMode().equals(Mode.getb())) {
            String constant = CONST_PREFIX + String.valueOf(aConst.getTarval().asInt());
            int targetReg = this.node2RegIndex.get(aConst);
            String regSuffix = Util.mode2RegSuffix(aConst.getMode());
            String movSuffix = Util.mode2MovSuffix(aConst.getMode());

            this.appendMolkiCode("mov" + movSuffix + " " + constant + regSuffix + " -> %@" + targetReg + regSuffix);
        }
    }

    private void molkify(Div div) {
        int left = this.node2RegIndex.get(div.getLeft());
        int right = this.node2RegIndex.get(div.getRight());

        int targetReg1 = this.node2RegIndex.get(div);
        int targetReg2 = targetReg1 + 1; // by convention used in PrepVisitor

        this.appendFourAdressCommand("divl", left, REG_WIDTH_D, right, REG_WIDTH_D, targetReg1, REG_WIDTH_D, targetReg2,
                REG_WIDTH_D);
    }

    private void molkify(End end) {
        // nothing to do
    }

    private void molkify(Jmp jmp) {
        int numberOfSuccessors = 0;

        for (BackEdges.Edge edge : BackEdges.getOuts(jmp)) {
            // a jmp should never be connected to more than one successor block
            numberOfSuccessors++;
            assert numberOfSuccessors < 2;

            Block block = (Block) edge.node;
            this.appendMolkiCode("jmp L" + block.getNr());
        }
    }

    private void molkify(Load load) {
        int targetReg = this.node2RegIndex.get(load);
        String regSuffix = Util.mode2RegSuffix(load.getLoadMode());
        String movSuffix = Util.mode2MovSuffix(load.getLoadMode());

        if (load.getPred(1) instanceof Sel) {
            Sel sel = (Sel) load.getPred(1);
            int baseReg = this.node2RegIndex.get(sel.getPtr());
            int indexReg = this.node2RegIndex.get(sel.getIndex());
            int alignment = sel.getType().getAlignment();

            this.appendMoveWithOffset(movSuffix, baseReg, indexReg, REG_WIDTH_D, alignment, regSuffix, targetReg,
                    regSuffix);
        }
        else if (load.getPred(1) instanceof Member) {
            Member member = (Member) load.getPred(1);
            int baseReg = this.node2RegIndex.get(member.getPtr());
            int offset = member.getEntity().getOffset();

            this.appendMoveWithOffset(movSuffix, offset, baseReg, targetReg, regSuffix);

        }
        else {
            int pointerReg = this.node2RegIndex.get(load.getPtr());

            this.moveWithOffset(movSuffix, pointerReg, targetReg, regSuffix);
        }
    }

    private void molkify(Minus minus) {
        int inputReg = this.node2RegIndex.get(minus.getOp());
        int reg = this.node2RegIndex.get(minus);
        this.appendMolkiCode("negl " + REG_PREFIX + inputReg + "d -> " + REG_PREFIX + reg + "d");
    }

    private void molkify(Mod mod) {
        int srcReg1 = this.node2RegIndex.get(mod.getLeft());
        int srcReg2 = this.node2RegIndex.get(mod.getRight());

        int targetReg1 = this.node2RegIndex.get(mod);
        int targetReg2 = targetReg1 + 1; // by convention used in PrepVisitor

        this.appendMolkiCode("divl [ " + REG_PREFIX + srcReg1 + "d | " + REG_PREFIX + srcReg2 + "d ]" + " -> [ "
                + REG_PREFIX + targetReg2 + "d | " + REG_PREFIX + targetReg1 + "d ]");
    }

    private void molkify(Mul mul) {
        int srcReg1 = this.node2RegIndex.get(mul.getLeft());
        int srcReg2 = this.node2RegIndex.get(mul.getRight());

        int targetReg = this.node2RegIndex.get(mul);

        this.appendMolkiCode("mull [ " + REG_PREFIX + srcReg1 + "d | " + REG_PREFIX + srcReg2 + "d ]" + " -> "
                + REG_PREFIX + targetReg + "d ");
    }

    private void molkify(Not not) {
        int inputReg = this.node2RegIndex.get(not.getOp());
        int reg = this.node2RegIndex.get(not);
        this.appendMolkiCode("notb " + REG_PREFIX + inputReg + "l -> " + REG_PREFIX + reg + "l");
    }

    private void molkify(Return aReturn) {

        if (aReturn.getPredCount() == 1 && !aReturn.getPred(0).getMode().equals(Mode.getM())) {
            String regSuffix = Util.mode2RegSuffix(aReturn.getPred(0).getMode());
            String movSuffix = Util.mode2MovSuffix(aReturn.getPred(0).getMode());
            this.appendMolkiCode("mov" + movSuffix + " %@" + this.node2RegIndex.get(aReturn.getPred(0)) + regSuffix
                    + " -> %@$" + regSuffix);
        }
        else if (aReturn.getPredCount() > 1) {
            String regSuffix = Util.mode2RegSuffix(aReturn.getPred(1).getMode());
            String movSuffix = Util.mode2MovSuffix(aReturn.getPred(1).getMode());
            this.appendMolkiCode("mov" + movSuffix + " %@" + this.node2RegIndex.get(aReturn.getPred(1)) + regSuffix
                    + " -> %@$" + regSuffix);
        }

        // Select single successor
        Block successorBlock = (Block) BackEdges.getOuts(aReturn).iterator().next().node;

        this.appendMolkiCode("jmp " + "L" + successorBlock.getNr());
    }

    private void molkify(Sel sel) {
    }

    private void molkify(Start start) {
        // nothing to do
    }

    private void molkify(Store store) {
        int storeReg = this.node2RegIndex.get(store.getValue());
        String regSuffix = Util.mode2RegSuffix(store.getValue().getMode());
        String movSuffix = Util.mode2MovSuffix(store.getValue().getMode());

        if (store.getPred(1) instanceof Sel) {
            Sel sel = (Sel) store.getPred(1);
            int baseReg = this.node2RegIndex.get(sel.getPtr());
            int indexReg = this.node2RegIndex.get(sel.getIndex());

            this.appendMolkiCode("mov" + movSuffix + " " + REG_PREFIX + storeReg + regSuffix + " -> (" + REG_PREFIX
                    + baseReg + ", " + REG_PREFIX + indexReg + "d, " + sel.getType().getAlignment() + ")" + regSuffix);
        }
        else if (store.getPred(1) instanceof Member) {
            Member member = (Member) store.getPred(1);
            int baseReg = this.node2RegIndex.get(member.getPtr());
            int offset = member.getEntity().getOffset();

            this.appendMolkiCode("mov" + movSuffix + " " + REG_PREFIX + storeReg + regSuffix + " -> " + offset + "("
                    + REG_PREFIX + baseReg + ")" + regSuffix);
        }
        else {
            int pointerReg = this.node2RegIndex.get(store.getPtr());
            this.appendMolkiCode("mov" + movSuffix + " " + REG_PREFIX + storeReg + regSuffix + " -> (" + REG_PREFIX
                    + pointerReg + ")" + regSuffix);
        }
    }

    private void molkify(Sub sub) {
        int srcReg1 = this.node2RegIndex.get(sub.getLeft());
        int srcReg2 = this.node2RegIndex.get(sub.getRight());

        int targetReg = this.node2RegIndex.get(sub);

        String regSuffix = Util.mode2RegSuffix(sub.getMode());

        this.appendMolkiCode("subl" + " [ " + REG_PREFIX + srcReg1 + regSuffix + " | " + REG_PREFIX + srcReg2
                + regSuffix + " ] -> " + REG_PREFIX + targetReg + regSuffix);
    }

    private void molkify(Proj node) {
        // nothing to do
    }

    private void molkify(Phi phi) {
        String regSuffix = Util.mode2RegSuffix(phi.getMode());
        String movSuffix = Util.mode2MovSuffix(phi.getMode());
        int regIndexOfPhi = this.node2RegIndex.get(phi);

        if (!phi.getMode().equals(Mode.getM())) {
            // check if condition pred leads in both cases to this block
            int blockNr = phi.getBlock().getNr();
            boolean phiBlockIsOnlySuccessor = true;

            if (phi.getBlock().getPred(0) instanceof Proj) {
                Proj projNode = (Proj) phi.getBlock().getPred(0);
                Cond cond = (Cond) projNode.getPred();

                for (BackEdges.Edge edge : BackEdges.getOuts(cond)) {
                    Block block = (Block) BackEdges.getOuts(edge.node).iterator().next().node;

                    if (block.getNr() != blockNr) {
                        phiBlockIsOnlySuccessor = false;
                    }
                }
            }

            if (phiBlockIsOnlySuccessor && phi.getBlock().getPred(0) instanceof Proj) {
                Proj firstPredProj = (Proj) phi.getBlock().getPred(0);
                Proj secondPredProj = (Proj) phi.getBlock().getPred(1);

                Cond otherCond = (Cond) firstPredProj.getPred();

                List<Node> phiPreds = new ArrayList<>();
                phi.getPreds().forEach(phiPreds::add);

                assert phiPreds.size() == 2 : "Incorrect number of phi predecessors: " + phiPreds.size();

                Node firstPred = phiPreds.get(0);
                Node secondPred = phiPreds.get(1);

                Node truePred = null;
                Node falsePred = null;

                if (firstPredProj.getNum() == Cond.pnTrue) {
                    truePred = firstPred;
                }
                else if (firstPredProj.getNum() == Cond.pnFalse) {
                    falsePred = firstPred;
                }
                else {
                    assert false : "Unknown predecessor for phi!" + phi.toString();
                }

                if (secondPredProj.getNum() == Cond.pnTrue) {
                    truePred = secondPred;
                }
                else if (secondPredProj.getNum() == Cond.pnFalse) {
                    falsePred = secondPred;
                }
                else {
                    assert false : "Unknown predecessor for phi!" + phi.toString();
                }

                assert truePred != null && falsePred != null;

                int truePredRegIndex = this.node2RegIndex.get(truePred);
                int falsePredRegIndex = this.node2RegIndex.get(falsePred);

                Graph graph = phi.getBlock().getGraph();
                int newMaxBlockId = this.graph2MaxBlockId.get(graph) + 1;
                int labelNr = newMaxBlockId;
                this.graph2MaxBlockId.put(graph, newMaxBlockId);

                if (otherCond.getSelector() instanceof Cmp) {
                    this.appendMolkiCode("mov" + movSuffix + " %@" + truePredRegIndex + regSuffix + " -> %@"
                            + regIndexOfPhi + regSuffix);

                    Cmp cmp = (Cmp) otherCond.getSelector();
                    String condJmp = Util.relation2Jmp(cmp.getRelation());
                    this.appendMolkiCode("phi_" + condJmp + " L" + labelNr);

                    this.appendMolkiCode("mov" + movSuffix + " %@" + falsePredRegIndex + regSuffix + " -> %@"
                            + regIndexOfPhi + regSuffix);

                    this.appendMolkiCode("L" + labelNr + ":");
                }
                else if (otherCond.getSelector() instanceof Const) {
                    Const tempConst = (Const) otherCond.getSelector();
                    boolean isTrue = tempConst.getTarval().equals(TargetValue.getBTrue());

                    if (isTrue) {
                        this.appendMolkiCode("mov" + movSuffix + " %@" + truePredRegIndex + regSuffix + " -> %@"
                                + regIndexOfPhi + regSuffix);
                    }
                    else {
                        this.appendMolkiCode("mov" + movSuffix + " %@" + falsePredRegIndex + regSuffix + " -> %@"
                                + regIndexOfPhi + regSuffix);
                    }
                }
            }
            else {
                for (int i = 0; i < phi.getPredCount(); i++) {
                    int regIndexOfIthPred = this.node2RegIndex.get(phi.getPred(i));
                    int blockNumOfIthPred = phi.getBlock().getPred(i).getBlock().getNr();

                    this.appendMolkiCode("mov" + movSuffix + " %@" + regIndexOfIthPred + regSuffix + " -> %@"
                            + regIndexOfPhi + regSuffix, blockNumOfIthPred);
                }
            }
        }
    }

    private void molkify(Cond cond) {
        Node selector = cond.getSelector();

        String condJump = null;
        String uncondJump = null;

        for (BackEdges.Edge edge : BackEdges.getOuts(cond)) {

            // Cond nodes should always be succeeded by proj nodes (with X mode set)
            Proj proj = (Proj) edge.node;

            // Get block which is the successor of the proj node
            // iterator().next() always yields the first entry in the iteration
            Block block = (Block) BackEdges.getOuts(proj).iterator().next().node;

            if (selector instanceof Cmp) {

                Cmp cmp = (Cmp) selector;
                Relation relation = cmp.getRelation();

                // Only generate a conditional jump for the true part, otherwise generate an unconditional jump
                if (proj.getNum() == Cond.pnTrue) {

                    // TODO Allow conditions to be inverted.
                    condJump = Util.relation2Jmp(relation);

                    condJump += " " + "L" + block.getNr();
                }
                else {
                    uncondJump = "jmp " + "L" + block.getNr();
                }
            }
            else {
                assert selector instanceof Const && selector.getMode().equals(Mode.getb());
                Const aConst = (Const) selector;

                if (proj.getNum() == Cond.pnTrue && aConst.getTarval().equals(TargetValue.getBTrue())) {
                    uncondJump = "jmp L" + block.getNr();
                }
                else if (proj.getNum() == Cond.pnFalse && aConst.getTarval().equals(TargetValue.getBFalse())) {
                    uncondJump = "jmp L" + block.getNr();
                }
            }
        }

        if (condJump != null) {
            this.appendMolkiCode(condJump);
        }
        if (uncondJump != null) {
            this.appendMolkiCode(uncondJump);
        }
    }

    private void molkify(Member node) {
        // nothing to do
    }

    private void molkify(Conv node) {
        // Nothing to do here as conversion nodes are only constructed for div and mod
        // operations and conversion of the operands instead should be handled there.
    }

    /**
     * append command cmd [ srcReg1 | srcReg2 ] -> targetReg Example add [ %@21d | %@22d ] -> %@23d
     *
     * @param cmd             command
     * @param srcReg1         number of first source register
     * @param suffixReg1      width of first register
     * @param srcReg2         number of second source register
     * @param suffixReg2      width of second register
     * @param targetReg       number of target register
     * @param suffixTargetReg width of target register
     */
    private void appendThreeAdressCommand(String cmd, int srcReg1, String suffixReg1, int srcReg2, String suffixReg2,
            int targetReg, String suffixTargetReg) {
        this.appendMolkiCode(cmd + " [ %@" + srcReg1 + suffixReg1 + " | %@" + srcReg2 + suffixReg2 + " ] -> %@"
                + targetReg + suffixTargetReg);
    }

    /**
     * append command with two source registers and two target registers
     * <p>
     * Example<br>
     * <br>
     * <code>divl [ %@21d | %@22d ] -> [ %@23d | %@24d ]</code>
     * </p>
     *
     * @param cmd              command
     * @param srcReg1
     * @param suffixReg1       width of srcReg1
     * @param srcReg2
     * @param suffixReg2       width of srcReg2
     * @param targetReg1
     * @param suffixTargetReg1 width of targetReg1
     * @param targetReg2
     * @param suffixTargetReg2 width of targetReg2
     */
    private void appendFourAdressCommand(String cmd, int srcReg1, String suffixReg1, int srcReg2, String suffixReg2,
            int targetReg1, String suffixTargetReg1, int targetReg2, String suffixTargetReg2) {

        this.appendMolkiCode(cmd + " [ " + REG_PREFIX + srcReg1 + suffixReg1 + " | " + REG_PREFIX + srcReg2 + suffixReg2
                + " ] -> [ %@" + targetReg1 + suffixTargetReg1 + " | " + REG_PREFIX + targetReg2 + suffixTargetReg2
                + " ]");
    }

    /**
     * <p>
     * Example<br>
     * <br>
     * movd (%@17, %@18d, 10) -> %@19
     * </p>
     *
     * @param moveSuffix
     * @param baseReg
     * @param indexReg
     * @param suffixIndexReg
     * @param alignment
     * @param suffixSrcReg
     * @param targetReg
     * @param suffixTargetReg
     */
    private void appendMoveWithOffset(String moveSuffix, int baseReg, int indexReg, String suffixIndexReg,
            int alignment, String suffixSrcReg, int targetReg, String suffixTargetReg) {

        StringBuilder sb = new StringBuilder("mov");
        sb.append(moveSuffix).append(" ( ");
        sb.append(REG_PREFIX).append(baseReg);
        sb.append(", ").append(REG_PREFIX).append(indexReg).append(suffixIndexReg);
        sb.append(", ").append(alignment).append(")").append(suffixSrcReg);
        sb.append(" -> ").append(REG_PREFIX).append(targetReg).append(suffixTargetReg);

        this.appendMolkiCode(sb.toString());
    }

    /**
     * <p>
     * Example<br>
     * <br>
     * movd 7(%@19)d -> %@20d
     * </p>
     *
     * @param movSuffix
     * @param offset
     * @param baseReg
     * @param targetReg
     * @param suffixTargetReg width of register
     */
    private void appendMoveWithOffset(String movSuffix, int offset, int baseReg, int targetReg,
            String suffixTargetReg) {
        this.appendMolkiCode("mov" + movSuffix + " " + offset + "(" + REG_PREFIX + baseReg + ")" + suffixTargetReg
                + " -> " + REG_PREFIX + targetReg + suffixTargetReg);
    }

    /**
     * <p>
     * Example<br><br>
     * movd (%@17) -> %@18d
     * @param movSuffix
     * @param pointerReg
     * @param targetReg
     * @param suffixTargetReg width of target register
     */
    private void moveWithOffset(String movSuffix, int pointerReg, int targetReg, String suffixTargetReg) {
        this.appendMolkiCode("mov" + movSuffix + " " + "(" + REG_PREFIX + pointerReg + ") -> " + REG_PREFIX
                + targetReg + suffixTargetReg);
    }
}
