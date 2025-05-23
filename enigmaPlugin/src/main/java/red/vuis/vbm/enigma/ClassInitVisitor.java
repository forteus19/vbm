package red.vuis.vbm.enigma;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

public abstract class ClassInitVisitor extends ProposalVisitor {
    private MethodNode clInit = null;

    protected ClassInitVisitor(int api) {
        super(api);
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
            Analyzer<SourceValue> analyzer = new Analyzer<>(new SourceInterpreter());
            Frame<SourceValue>[] frames;
            try {
                frames = analyzer.analyze(className, clInit);
            } catch (AnalyzerException e) {
                throw new RuntimeException(e);
            }
            analyzeClInit(clInit, frames);
        }
    }

    protected abstract void analyzeClInit(MethodNode clInit, Frame<SourceValue>[] frames);
}
