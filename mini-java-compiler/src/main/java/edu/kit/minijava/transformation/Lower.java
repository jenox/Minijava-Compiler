package edu.kit.minijava.transformation;

import firm.*;

/**
 * A transformation pass that lowers some highlevel features of OO languages,
 * so we can generate machine assembler:
 *
 *   - Move methods from classtypes into global type
 *   - escape/replace special chars in LdNames so normal assemblers are fine
 *     with them
 */
public class Lower {
    private Lower() {
    }

    private void fixEntityLdName(Entity entity) {
        String name = entity.getLdName();

        // Main has already received its special name during construction of the Firm graph

        name = name.replaceAll("[()\\[\\];]", "_");
        Ident identifier = Ident.mangleGlobal(name);
        entity.setLdIdent(identifier);
    }

    private void fixEntityNames() {
        for (Entity entity : Program.getGlobalType().getMembers()) {
            this.fixEntityLdName(entity);
        }
    }

    private void layoutClass(ClassType cls) {
        if (cls.equals(Program.getGlobalType())) {
            return;
        }

        for (int m = 0; m < cls.getNMembers(); /* nothing */) {
            Entity member = cls.getMember(m);
            Type type = member.getType();
            if (! (type instanceof MethodType)) {
                ++m;
                continue;
            }

            // Move class members outside the class
            member.setOwner(Program.getGlobalType());
        }

        cls.layoutFields();
    }

    /**
     * Lower some highlevel constructs to make firm-graph suitable to be used
     * by x86 backend.
     */
    public static void lower() {
        Lower instance = new Lower();
        instance.fixEntityNames();
        Util.lowerSels();
    }

    public static void fixNames() {
        Lower instance = new Lower();
        instance.fixEntityNames();
    }
}
