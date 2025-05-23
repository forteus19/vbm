package red.vuis.vbm.enigma;

import cuchaz.enigma.translation.representation.TypeDescriptor;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;
import red.vuis.vbm.util.FieldId;

public abstract class ProposalVisitor extends ClassVisitor {
    private static final Map<Entry<?>, String> TARGETS = new HashMap<>();
    protected String className = null;

    public ProposalVisitor(int api) {
        super(api);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        className = name;
    }

    public static Optional<String> propose(Entry<?> entry) {
        return Optional.ofNullable(TARGETS.get(entry));
    }

    protected void addTarget(Entry<?> entry, String name) {
        if (TARGETS.containsKey(entry)) {
            System.err.printf("Duplicate entry \"%s\" with name \"%s\"\n", entry.getFullName(), name);
            return;
        }
        TARGETS.put(entry, name);
    }

    protected void addFieldTarget(String className, String fieldName, String fieldDesc, String name) {
        addTarget(
                new FieldEntry(
                        new ClassEntry(className),
                        fieldName,
                        new TypeDescriptor(fieldDesc)
                ),
                name
        );
    }

    protected static String getStringLdc(Frame<SourceValue> frame) {
        for (int i = 0; i < frame.getStackSize(); i++) {
            SourceValue source = frame.getStack(i);

            for (AbstractInsnNode sourceInsn : source.insns) {
                if (sourceInsn instanceof LdcInsnNode ldcInsn && ldcInsn.cst instanceof String value) {
                    return value;
                }
            }
        }
        return null;
    }

    protected static <A extends AbstractInsnNode, B extends AbstractInsnNode> MatchTwoResult<A, B> matchTwoInsns(InsnList insns, int offset, Class<A> insnClass1, Class<B> insnClass2, int opcode1, int opcode2) {
        AbstractInsnNode absInsn1 = insns.get(offset);
        AbstractInsnNode absInsn2 = insns.get(offset + 1);
        if (absInsn1.getOpcode() == opcode1 && absInsn2.getOpcode() == opcode2) {
            return new MatchTwoResult<>(insnClass1.cast(absInsn1), insnClass2.cast(absInsn2));
        } else {
            return null;
        }
    }

    protected record MatchTwoResult<A extends AbstractInsnNode, B extends AbstractInsnNode>(A insn1, B insn2) {
        public FieldId fieldId2() {
            if (insn2 instanceof FieldInsnNode fieldInsn2) {
                return new FieldId(fieldInsn2.name, fieldInsn2.desc);
            } else {
                throw new RuntimeException("Not a FieldInsnNode");
            }
        }
    }
}
