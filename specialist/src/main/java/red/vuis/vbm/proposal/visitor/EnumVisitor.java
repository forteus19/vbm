package red.vuis.vbm.proposal.visitor;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;
import red.vuis.vbm.proposal.ProposalCollector;
import red.vuis.vbm.util.DoubleId;
import red.vuis.vbm.util.VbmUtils;

import java.util.HashSet;
import java.util.Set;

public final class EnumVisitor extends ClassInitVisitor {
    private final Set<DoubleId> enumFields = new HashSet<>();

    public EnumVisitor(ProposalCollector collector) {
        super(collector);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        enumFields.clear();
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if ((access & Opcodes.ACC_ENUM) != 0) {
            enumFields.add(new DoubleId(name, descriptor));
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    protected void analyzeClInit(MethodNode clInit, Frame<SourceValue>[] frames) {
        for (int i = 0; i < clInit.instructions.size() - 1; i++) {
            var match = matchTwoInsns(
                    clInit.instructions, i,
                    MethodInsnNode.class, FieldInsnNode.class,
                    Opcodes.INVOKESPECIAL, Opcodes.PUTSTATIC
            );
            if (match == null) {
                continue;
            }

            MethodInsnNode insn1 = match.insn1();
            FieldInsnNode insn2 = match.insn2();

            if (!VbmUtils.all(
                    insn1.name.equals("<init>"),
                    insn2.owner.equals(className),
                    enumFields.contains(match.fieldId2())
            )) {
                continue;
            }

            String ldcValue = getStringLdc(frames[i], 0);
            if (ldcValue != null) {
                collector.collectField(className, insn2.name, insn2.desc, VbmUtils.javaName(ldcValue));
            }
        }
    }
}
