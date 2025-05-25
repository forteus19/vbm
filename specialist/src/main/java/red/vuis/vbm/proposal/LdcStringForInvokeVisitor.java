package red.vuis.vbm.proposal;

import java.util.Arrays;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;
import red.vuis.vbm.util.VbmUtils;

public class LdcStringForInvokeVisitor extends ClassInitVisitor {
    private final int invokeOpcode;
    private final int argIndex;
    private final String[] registerMethods;

    public LdcStringForInvokeVisitor(ProposalCollector collector, int invokeOpcode, int argIndex, String... registerMethods) {
        super(collector);
        this.invokeOpcode = invokeOpcode;
        this.argIndex = argIndex;
        this.registerMethods = registerMethods;
    }

    @Override
    protected void analyzeClInit(MethodNode clInit, Frame<SourceValue>[] frames) {
        for (int i = 0; i < clInit.instructions.size() - 1; i++) {
            var match = matchTwoInsns(
                    clInit.instructions, i,
                    MethodInsnNode.class, FieldInsnNode.class,
                    invokeOpcode, Opcodes.PUTSTATIC
            );
            if (match == null) {
                continue;
            }

            MethodInsnNode insn1 = match.insn1();
            FieldInsnNode insn2 = match.insn2();

            if (!VbmUtils.all(
                    Arrays.asList(registerMethods).contains(insn1.name),
                    insn2.owner.equals(className)
            )) {
                continue;
            }

            String ldcValue = getStringLdc(frames[i], argIndex);
            if (ldcValue != null) {
                collector.collectField(className, insn2.name, insn2.desc, VbmUtils.javaName(ldcValue));
            }
        }
    }
}
