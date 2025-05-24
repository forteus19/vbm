package red.vuis.vbm.mappingio;

import java.io.IOException;
import java.util.Objects;
import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingVisitor;
import red.vuis.vbm.proposal.ProposalCollector;

public final class MappingIoProposalCollector implements ProposalCollector {
    private final MappingVisitor visitor;
    private final int destNs;
    private String currentClass = null;

    public MappingIoProposalCollector(MappingVisitor visitor, int destNs) {
        this.visitor = visitor;
        this.destNs = destNs;
    }

    @Override
    public void collectField(String className, String fieldName, String fieldDesc, String target) {
        updateCurrentClass(className);
        try {
            visitor.visitField(fieldName, fieldDesc);
            visitor.visitDstName(MappedElementKind.FIELD, destNs, target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void finished() {
        try {
            visitor.visitEnd();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateCurrentClass(String newClassName) {
        if (!Objects.equals(currentClass, newClassName)) {
            currentClass = newClassName;
            try {
                visitor.visitClass(newClassName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
