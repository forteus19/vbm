package red.vuis.vbm.proposal.visitor;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import red.vuis.vbm.proposal.ProposalCollector;
import red.vuis.vbm.util.DoubleId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecordVisitor extends ProposalVisitor {
    public static final String RECORD = "java/lang/Record";

    private MethodNode toStringMethod = null;
    private final List<MethodNode> possibleGetters = new ArrayList<>();
    private final List<DoubleId> possibleInits = new ArrayList<>();

    public RecordVisitor(@NotNull ProposalCollector collector) {
        super(collector);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        toStringMethod = null;
        possibleGetters.clear();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name.equals("toString") && descriptor.equals("()Ljava/lang/String;")) {
            toStringMethod = new MethodNode(api, name, descriptor, signature, exceptions);
            return toStringMethod;
        }

        if ((access & (Opcodes.ACC_FINAL | Opcodes.ACC_STATIC)) == 0 && (access & Opcodes.ACC_PUBLIC) != 0 && descriptor.startsWith("()")) {
            MethodNode possibleGetter = new MethodNode(api, name, descriptor, signature, exceptions);
            possibleGetters.add(possibleGetter);
            return possibleGetter;
        }

//        if (name.equals("<init>")) {
//            possibleInits.add(new DoubleId(name, descriptor));
//            return null;
//        }

        return null;
    }

    @Override
    public void visitEnd() {
        if (toStringMethod == null) {
            return;
        }

        Map<DoubleId, String> components = new HashMap<>();
        Map<MethodNode, String> getters = new HashMap<>();

        try {
            InvokeDynamicInsnNode bootstrapNode = null;
            for (AbstractInsnNode insn : toStringMethod.instructions) {
                if (insn instanceof InvokeDynamicInsnNode indy) {
                    bootstrapNode = indy;
                    break;
                }
            }
            if (bootstrapNode == null) {
                return;
            }

            String[] names = ((String) bootstrapNode.bsmArgs[1]).split(";");

            for (int i = 0; i < bootstrapNode.bsmArgs.length - 2; i++) {
                Object arg = bootstrapNode.bsmArgs[i + 2];
                if (arg instanceof Handle handle && handle.getTag() == Opcodes.H_GETFIELD && handle.getOwner().equals(className)) {
                    components.put(new DoubleId(handle.getName(), handle.getDesc()), names[i]);
                }
            }
        } catch (Exception e) {
            return;
        }

        Set<DoubleId> matchedTargets = new HashSet<>();
        for (MethodNode pgNode : possibleGetters) {
            List<AbstractInsnNode> pgRealInsns = new ArrayList<>();
            for (AbstractInsnNode insn : pgNode.instructions) {
                if (insn.getType() != AbstractInsnNode.LINE &&
                        insn.getType() != AbstractInsnNode.FRAME &&
                        insn.getType() != AbstractInsnNode.LABEL
                ) {
                    pgRealInsns.add(insn);
                }
            }

            if (pgRealInsns.size() != 3) continue;
            if (!(pgRealInsns.get(0) instanceof VarInsnNode insn1 &&
                    insn1.getOpcode() == Opcodes.ALOAD &&
                    insn1.var == 0)) continue;
            DoubleId targetField;
            if (!(pgRealInsns.get(1) instanceof FieldInsnNode insn2 &&
                    insn2.getOpcode() == Opcodes.GETFIELD &&
                    components.containsKey(targetField = new DoubleId(insn2.name, insn2.desc)) &&
                    Type.getReturnType(pgNode.desc).getDescriptor().equals(insn2.desc) &&
                    !matchedTargets.contains(targetField))) continue;
            int insn3c = pgRealInsns.get(2).getOpcode();
            if (!(insn3c >= Opcodes.IRETURN && insn3c <= Opcodes.RETURN)) continue;

            getters.put(pgNode, components.get(targetField));
            matchedTargets.add(targetField);
        }

//        DoubleId mainInit = null;
//        for (DoubleId possibleInit : possibleInits) {
//            List<String> argDescs = Arrays.stream(Type.getMethodType(possibleInit.desc()).getArgumentTypes())
//                    .map(Type::getDescriptor).toList();
//            List<String> compDescs = components.keySet().stream()
//                    .map(DoubleId::desc).toList();
//            if (argDescs.equals(compDescs)) {
//                mainInit = possibleInit;
//            }
//        }

        for (Map.Entry<DoubleId, String> componentEntry : components.entrySet()) {
            DoubleId key = componentEntry.getKey();
            collector.collectField(className, key.name(), key.desc(), componentEntry.getValue());
        }
        for (Map.Entry<MethodNode, String> getterEntry : getters.entrySet()) {
            MethodNode key = getterEntry.getKey();
            collector.collectMethod(className, key.name, key.desc, getterEntry.getValue());
        }
//        if (mainInit != null) {
//            collector.collectMethod(className, mainInit.name(), mainInit.desc(), mainInit.name());
//            for (Map.Entry<DoubleId, String> componentEntry : components.entrySet()) {
//                collector.
//            }
//        }
    }
}
