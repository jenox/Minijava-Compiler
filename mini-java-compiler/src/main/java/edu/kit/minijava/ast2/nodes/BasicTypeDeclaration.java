package edu.kit.minijava.ast2.nodes;

import java.util.*;

public interface BasicTypeDeclaration {
    List<? extends SubroutineDeclaration> getStaticMethodDeclarations();
    List<? extends SubroutineDeclaration> getInstanceMethodDeclarations();
    List<? extends VariableDeclaration> getFieldDeclarations();
}
