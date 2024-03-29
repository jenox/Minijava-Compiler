package edu.kit.minijava.cli;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.references.Reference;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

abstract class ASTDumperBase extends ASTVisitor<Void> {

    public ASTDumperBase(Program program) {
        this.program = program;
    }

    private PrintWriter writer = null;
    private final Program program;


    // MARK: - Dumping

    public void dump(String path) throws IOException {
        this.writer = new PrintWriter(path);

        this.print("<graphml");
        this.print(" xmlns='http://graphml.graphdrawing.org/xmlns'");
        this.print(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'");
        this.print(" xmlns:y='http://www.yworks.com/xml/graphml'");
        this.print(" xsi:schemaLocation='http://graphml.graphdrawing.org/xmlns");
        this.print(" http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd'>");

        this.print("<key for='node' id='d0' yfiles.type='nodegraphics' />");
        this.print("<key for='edge' id='d1' yfiles.type='edgegraphics' />");
        this.print("<graph>");

        this.program.accept(this);

        this.print("</graph>");
        this.print("</graphml>");

        this.writer.flush();
    }

    private void print(String text) {
        this.writer.println(text);
    }

    private String escape(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }


    // MARK: - Identifying Objects

    private final Map<Object, UUID> identifiers = new HashMap<>();

    private UUID getIdentifierForNode(Object node) {
        return this.identifiers.computeIfAbsent(node, __ -> UUID.randomUUID());
    }


    // MARK: - Writing Nodes

    void outputBasicTypeDeclarationNode(BasicTypeDeclaration node) {
        this.outputNode(node, "#ff9999");
    }

    void outputSubroutineDeclarationNode(SubroutineDeclaration node) {
        this.outputNode(node, "#9999ff");
    }

    void outputVariableDeclarationNode(VariableDeclaration node) {
        this.outputNode(node, "#99ff99");
    }

    void outputStatementNode(Statement node) {
        this.outputNode(node, "#ffbb66");
    }

    void outputExpressionNode(Expression node) {
        this.outputNode(node, "#ffdd88");
    }

    void outputSystemCallNode(Expression node) {
        this.outputNode(node, "#44ccff");
    }

    void outputReferenceNode(ASTDumpable node) {
        this.outputNode(node, "#dddddd");
    }

    private void outputNode(ASTDumpable node, String color) {
        String description = node.toStringForDumpingAST();

        this.print("<node id='" + this.getIdentifierForNode(node) + "'>");
        this.print("<data key='d0'>");
        this.print("<y:ShapeNode>");
        this.print("<y:Geometry width='160' height='40' x='-80' y='-20'></y:Geometry>");
        this.print("<y:Fill color='" + color + "'></y:Fill>");
        this.print("<y:NodeLabel>" + this.escape(description) + "</y:NodeLabel>");
        this.print("</y:ShapeNode>");
        this.print("</data>");
        this.print("</node>");
    }


    // MARK: - Writing Edges

    void outputReferencingEdge(Object source, Object target) {
        this.outputEdge(source, target, "dashed", null);
    }

    void outputOwningEdgeAndVisit(ASTNode source, @Nullable ASTNode target, @Nullable String label) {
        if (target == null) return;

        target.accept(this);

        this.outputEdge(source, target, "line", label);
    }

    void outputOwningEdgeAndResolve(ASTNode source, Reference target, @Nullable String label) {
        this.outputReferenceNode(target);
        this.outputEdge(source, target, "line", label);
        this.outputReferencingEdge(target, target.getDeclaration());
    }

    private void outputEdge(Object source, Object target, String kind, @Nullable String label) {
        UUID id = UUID.randomUUID();
        UUID sid = this.getIdentifierForNode(source);
        UUID tid = this.getIdentifierForNode(target);

        this.print("<edge id='" + id + "' source='" + sid + "' target='" + tid + "'>");
        this.print("<data key='d1'>");
        this.print("<y:PolyLineEdge>");
        this.print("<y:LineStyle color='#000000' width='1.0' type='" + kind + "'></y:LineStyle>");
        this.print("<y:Arrows source='none' target='standard'></y:Arrows>");

        if (label != null) {
            this.print("<y:EdgeLabel>" + this.escape(label) + "</y:EdgeLabel>");
        }

        this.print("</y:PolyLineEdge>");
        this.print("</data>");
        this.print("</edge>");
    }
}
