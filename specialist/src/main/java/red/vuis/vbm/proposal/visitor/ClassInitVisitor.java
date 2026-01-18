package red.vuis.vbm.proposal.visitor;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;
import red.vuis.vbm.proposal.ProposalCollector;
import red.vuis.vbm.util.VbmUtils;

public abstract class ClassInitVisitor extends ProposalVisitor {
    private MethodNode clInit = null;

    public ClassInitVisitor(ProposalCollector collector) {
        super(collector);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        clInit = null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name.equals("<clinit>")) {
            clInit = new MethodNode(access, name, descriptor, signature, exceptions);
            return clInit;
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (clInit != null) {
            analyzeClInit(clInit, VbmUtils.getMethodFrames(className, clInit));
            clInit = null;
        }
    }

    protected abstract void analyzeClInit(MethodNode clInit, Frame<SourceValue>[] frames);
}
