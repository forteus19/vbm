package red.vuis.vbm.enigma;

import org.quiltmc.enigma.api.source.TokenType;
import org.quiltmc.enigma.api.translation.mapping.EntryMapping;
import org.quiltmc.enigma.api.translation.representation.entry.Entry;
import org.quiltmc.enigma.api.translation.representation.entry.FieldEntry;
import org.quiltmc.enigma.api.translation.representation.entry.LocalVariableEntry;
import org.quiltmc.enigma.api.translation.representation.entry.MethodEntry;
import red.vuis.vbm.enigma.service.VbmNameProposal;
import red.vuis.vbm.proposal.ProposalCollector;

import java.util.HashMap;
import java.util.Map;

public final class EnigmaProposalCollector implements ProposalCollector {
    public final Map<Entry<?>, EntryMapping> targets = new HashMap<>();
    private MethodEntry lastMethod = null;

    @Override
    public void collectField(String className, String fieldName, String fieldDesc, String target) {
        targets.put(FieldEntry.parse(className, fieldName, fieldDesc), entryMapping(target));
    }

    @Override
    public void collectMethod(String className, String methodName, String methodDesc, String target) {
        lastMethod = MethodEntry.parse(className, methodName, methodDesc);
        targets.put(lastMethod, entryMapping(target));
    }

    @Override
    public void collectMethodArg(int index, int slot, String target) {
        if (lastMethod == null) {
            return;
        }
        targets.put(new LocalVariableEntry(lastMethod, slot, "", true, null), entryMapping(target));
    }

    private static EntryMapping entryMapping(String target) {
        return new EntryMapping(target, null, TokenType.JAR_PROPOSED, VbmNameProposal.ID);
    }
}
