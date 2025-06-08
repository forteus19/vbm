package red.vuis.vbm.proposal.visitor;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import red.vuis.vbm.proposal.ProposalCollector;

public class AllClassesVisitor extends ProposalVisitor {
    public AllClassesVisitor(@NotNull ProposalCollector collector) {
        super(collector);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (!name.startsWith("field_")) {
            collector.collectField(className, name, descriptor, name);
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (!name.startsWith("method_") && !name.equals("<init>") && !name.equals("<clinit>")) {
            collector.collectMethod(className, name, descriptor, name);
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}
