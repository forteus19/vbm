package red.vuis.vbm.proposal.visitor;

import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;
import red.vuis.vbm.proposal.ProposalCollector;
import red.vuis.vbm.util.VbmUtils;

public class BFBlocksVisitor extends ClassInitVisitor {
    public static final String BF_BLOCK_ENTITY_TYPES = "com/boehmod/blockfront/unnamed/BF_1087";
    public static final String BF_BLOCKS = "com/boehmod/blockfront/unnamed/BF_1091";

    private final Map<String, String> blockNames = new HashMap<>();
    private MethodNode clInitBet = null;
    private int matched = 0;

    public BFBlocksVisitor(ProposalCollector collector) {
        super(collector);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        matched++;
    }

    @Override
    protected void analyzeClInit(MethodNode clInit, Frame<SourceValue>[] frames) {
        if (className.equals(BF_BLOCK_ENTITY_TYPES)) {
            clInitBet = clInit;
        }
        if (className.equals(BF_BLOCKS)) {
            for (int i = 0; i < clInit.instructions.size() - 1; i++) {
                var match = LdcStringForInvokeVisitor.matchInvokePutPattern(className, clInit.instructions, i, Opcodes.INVOKEVIRTUAL);
                if (match == null) {
                    continue;
                }

                MethodInsnNode insn1 = match.insn1();
                FieldInsnNode insn2 = match.insn2();

                if (!insn1.name.equals("register")) {
                    continue;
                }

                String ldcValue = getStringLdc(frames[i], 0);
                if (ldcValue != null) {
                    String javaName = VbmUtils.javaName(ldcValue);
                    collector.collectField(className, insn2.name, insn2.desc, javaName);
                    blockNames.put(insn2.name, javaName);
                }
            }
        }

        if (matched < 1 || clInitBet == null) {
            return;
        }

        MatchTwoResult<FieldInsnNode, MethodInsnNode> lastBlockGet = null;

        for (int i = 0; i < clInitBet.instructions.size() - 1; i++) {
            var match1 = matchTwoInsns(
                    clInitBet.instructions, i,
                    FieldInsnNode.class, MethodInsnNode.class,
                    Opcodes.GETSTATIC, Opcodes.INVOKEVIRTUAL
            );
            if (match1 != null) {
                FieldInsnNode fieldInsn = match1.insn1();
                if (fieldInsn.owner.equals(BF_BLOCKS)) {
                    lastBlockGet = match1;
                }
                continue;
            }

            var match2 = LdcStringForInvokeVisitor.matchInvokePutPattern(className, clInitBet.instructions, i, Opcodes.INVOKEVIRTUAL);
            if (match2 == null || lastBlockGet == null) {
                continue;
            }

            FieldInsnNode lastBlock = lastBlockGet.insn1();
            lastBlockGet = null;

            MethodInsnNode insn1 = match2.insn1();
            FieldInsnNode insn2 = match2.insn2();

            if (!insn1.name.equals("register")) {
                continue;
            }

            if (blockNames.containsKey(lastBlock.name)) {
                collector.collectField(BF_BLOCK_ENTITY_TYPES, insn2.name, insn2.desc, blockNames.get(lastBlock.name));
            }
        }
    }
}
