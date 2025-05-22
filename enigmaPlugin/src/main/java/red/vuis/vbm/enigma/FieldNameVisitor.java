package red.vuis.vbm.enigma;

import cuchaz.enigma.translation.representation.TypeDescriptor;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

public class FieldNameVisitor extends ClassVisitor {
    protected ClassEntry classEntry = null;
    protected String className = null;
    protected final Set<FieldIdentifier> enumFields = new HashSet<>();
    protected final List<MethodNode> clInits = new ArrayList<>();
    protected final Map<Entry<?>, String> target = new HashMap<>();

    public FieldNameVisitor() {
        super(Opcodes.ASM9);
    }

    public String getName(Entry<?> entry) {
        return target.get(entry);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        classEntry = new ClassEntry(name);
        className = name;
        enumFields.clear();
        clInits.clear();
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if ((access & Opcodes.ACC_ENUM) != 0) {
            if (!enumFields.add(new FieldIdentifier(name, descriptor))) {
                throw new IllegalStateException("Enum class has duplicate fields (\"%s\", \"%s\")".formatted(name, descriptor));
            }
        }

        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name.equals("<clinit>")) {
            MethodNode node = new MethodNode(api, access, name, descriptor, signature, exceptions);
            clInits.add(node);
            return node;
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

        Analyzer<SourceValue> analyzer = new Analyzer<>(new SourceInterpreter());

        for (MethodNode clInit : clInits) {
            Frame<SourceValue>[] frames;
            try {
                frames = analyzer.analyze(className, clInit);
            } catch (AnalyzerException e) {
                throw new RuntimeException(e);
            }

            for (int i = 1; i < clInit.instructions.size(); i++) {
                getFieldProposal(
                        clInit.instructions.get(i - 1),
                        clInit.instructions.get(i),
                        frames[i - 1]
                ).ifPresent(
                        proposal -> target.put(proposal.entry(), proposal.name())
                );
            }
        }
    }

    protected Optional<Proposal> getFieldProposal(AbstractInsnNode absInstr1, AbstractInsnNode absInstr2, Frame<SourceValue> frame) {
        if (absInstr2.getOpcode() != Opcodes.PUTSTATIC) {
            return Optional.empty();
        }
        FieldInsnNode instr2 = (FieldInsnNode) absInstr2;
        if (!BooleanUtils.all(
                enumFields.contains(new FieldIdentifier(instr2.name, instr2.desc)),
                instr2.owner.equals(className)
        )) {
            return Optional.empty();
        }

        int opcode1 = absInstr1.getOpcode();
        if (!BooleanUtils.any(
                BooleanUtils.all(
                        opcode1 == Opcodes.INVOKESPECIAL,
                        ((MethodInsnNode) absInstr1).name.equals("<init>")
                ),
                opcode1 == Opcodes.INVOKESTATIC
        )) {
            return Optional.empty();
        }
        MethodInsnNode instr1 = (MethodInsnNode) absInstr1;
        if (
                !instr1.owner.equals(className)
        ) {
            return Optional.empty();
        }

        String rawName = getStringLdc(frame);
        if (rawName == null) {
            return Optional.empty();
        }

        StringBuilder javaName = new StringBuilder(rawName.length());
        for (int i = 0; i < rawName.length(); i++) {
            char c = rawName.charAt(i);
            if (BooleanUtils.any(
                    Character.isAlphabetic(c),
                    Character.isDigit(c)
            )) {
                javaName.append(Character.toUpperCase(c));
            } else {
                javaName.append('_');
            }
        }

        return Optional.of(
                new Proposal(new FieldEntry(classEntry, instr2.name, new TypeDescriptor(instr2.desc)), javaName.toString())
        );
    }

    protected static String getStringLdc(Frame<SourceValue> frame) {
        for (int i = 0; i < frame.getStackSize(); i++) {
            for (AbstractInsnNode absInstr : frame.getStack(i).insns) {
                if (absInstr instanceof LdcInsnNode instr && instr.cst instanceof String str) {
                    return str;
                }
            }
        }
        return null;
    }

    protected record FieldIdentifier(String name, String descriptor) {
    }

    protected record Proposal(Entry<?> entry, String name) {
    }
}
