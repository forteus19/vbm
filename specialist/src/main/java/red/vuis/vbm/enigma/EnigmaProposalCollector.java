package red.vuis.vbm.enigma;

import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;
import red.vuis.vbm.proposal.ProposalCollector;

import java.util.HashMap;
import java.util.Map;

public final class EnigmaProposalCollector implements ProposalCollector {
    public final Map<Entry<?>, String> targets = new HashMap<>();
    private MethodEntry lastMethod = null;

    @Override
    public void collectField(String className, String fieldName, String fieldDesc, String target) {
        targets.put(FieldEntry.parse(className, fieldName, fieldDesc), target);
    }

    @Override
    public void collectMethod(String className, String methodName, String methodDesc, String target) {
        lastMethod = MethodEntry.parse(className, methodName, methodDesc);
        targets.put(lastMethod, target);
    }

    @Override
    public void collectMethodArg(int index, int slot, String target) {
        if (lastMethod == null) {
            return;
        }
        targets.put(new LocalVariableEntry(lastMethod, slot, "", true, null), target);
    }
}
