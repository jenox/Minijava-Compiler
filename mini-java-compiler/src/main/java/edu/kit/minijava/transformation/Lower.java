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

        // Replace name of main method (which is guaranteed to be unique) with special name
        if (name.endsWith(".main")) {
            name = "__minijava_main";
        }

        /* C linker doesn't allow all possible ascii chars for identifiers,
         * filter some out */
        name = name.replaceAll("[()\\[\\];]", "_");
        entity.setLdIdent(name);
    }

    private void fixEntityNames() {
        for (Entity entity : Program.getGlobalType().getMembers()) {
            this.fixEntityLdName(entity);
        }
    }

    private void layoutClass(ClassType cls) {
        if (cls.equals(Program.getGlobalType()))
            return;

        for (int m = 0; m < cls.getNMembers(); /* nothing */) {
            Entity member = cls.getMember(m);
            Type type = member.getType();
            if (! (type instanceof MethodType)) {
                ++m;
                continue;
            }

            // TODO This might not be needed here at all.
            // For now, move the entity to be sure.
            // Maybe we should put methods into the classes first, then move them in this lowering step.
            /* methods get implemented outside the class, move the entity */
            member.setOwner(Program.getGlobalType());
        }

        cls.layoutFields();
    }

    private void layoutTypes() {
        for (Type type : Program.getTypes()) {
            if (type instanceof ClassType) {
                this.layoutClass((ClassType) type);
            }
            type.finishLayout();
        }
    }

    /**
     * Lower some highlevel constructs to make firm-graph suitable to be used
     * by x86 backend.
     */
    public static void lower() {
        Lower instance = new Lower();
        instance.layoutTypes();
        instance.fixEntityNames();
        Util.lowerSels();
    }
}
