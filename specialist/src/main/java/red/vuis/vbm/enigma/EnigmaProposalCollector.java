package red.vuis.vbm.enigma;

import cuchaz.enigma.translation.representation.TypeDescriptor;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;
import java.util.HashMap;
import java.util.Map;
import red.vuis.vbm.proposal.ProposalCollector;

public final class EnigmaProposalCollector implements ProposalCollector {
    public final Map<Entry<?>, String> targets = new HashMap<>();

    @Override
    public void collectField(String className, String fieldName, String fieldDesc, String target) {
        targets.put(
                new FieldEntry(
                        new ClassEntry(className),
                        fieldName,
                        new TypeDescriptor(fieldDesc)
                ),
                target
        );
    }
}
