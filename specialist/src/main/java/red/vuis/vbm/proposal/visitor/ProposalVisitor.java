package red.vuis.vbm.proposal.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;
import red.vuis.vbm.proposal.ProposalCollector;
import red.vuis.vbm.util.FieldId;

public abstract class ProposalVisitor extends ClassVisitor {
    protected final ProposalCollector collector;
    protected String className = null;

    public ProposalVisitor(ProposalCollector collector) {
        super(Opcodes.ASM9);
        this.collector = collector;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        className = name;
    }

    public static String getStringLdc(Frame<SourceValue> frame, int argIndex) {
        int a = 0;
        for (int i = 0; i < frame.getStackSize(); i++) {
            SourceValue source = frame.getStack(i);

            for (AbstractInsnNode sourceInsn : source.insns) {
                if (sourceInsn instanceof LdcInsnNode ldcInsn && ldcInsn.cst instanceof String value) {
                    if (a++ == argIndex) {
                        return value;
                    }
                }
            }
        }
        return null;
    }

    public static <A extends AbstractInsnNode, B extends AbstractInsnNode> MatchTwoResult<A, B> matchTwoInsns(InsnList insns, int offset, Class<A> insnClass1, Class<B> insnClass2, int opcode1, int opcode2) {
        AbstractInsnNode absInsn1 = insns.get(offset);
        AbstractInsnNode absInsn2 = insns.get(offset + 1);
        if (absInsn1.getOpcode() == opcode1 && absInsn2.getOpcode() == opcode2) {
            return new MatchTwoResult<>(insnClass1.cast(absInsn1), insnClass2.cast(absInsn2));
        } else {
            return null;
        }
    }

    public record MatchTwoResult<A extends AbstractInsnNode, B extends AbstractInsnNode>(A insn1, B insn2) {
        public FieldId fieldId2() {
            if (insn2 instanceof FieldInsnNode fieldInsn2) {
                return new FieldId(fieldInsn2.name, fieldInsn2.desc);
            } else {
                throw new RuntimeException("Not a FieldInsnNode");
            }
        }
    }
}
