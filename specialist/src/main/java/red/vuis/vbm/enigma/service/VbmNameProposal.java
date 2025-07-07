package red.vuis.vbm.enigma.service;

import org.quiltmc.enigma.api.Enigma;
import org.quiltmc.enigma.api.analysis.index.jar.JarIndex;
import org.quiltmc.enigma.api.service.NameProposalService;
import org.quiltmc.enigma.api.translation.mapping.EntryMapping;
import org.quiltmc.enigma.api.translation.mapping.EntryRemapper;
import org.quiltmc.enigma.api.translation.representation.entry.Entry;
import red.vuis.vbm.enigma.EnigmaProposalCollector;

import java.util.Map;

public class VbmNameProposal implements NameProposalService {
    public static final String ID = "vbm:name_proposal";

    private final EnigmaProposalCollector collector;

    public VbmNameProposal(EnigmaProposalCollector collector) {
        this.collector = collector;
    }

    @Override
    public Map<Entry<?>, EntryMapping> getProposedNames(Enigma enigma, JarIndex index) {
        return collector.targets;
    }

    @Override
    public Map<Entry<?>, EntryMapping> getDynamicProposedNames(EntryRemapper remapper, Entry<?> obfEntry, EntryMapping oldMapping, EntryMapping newMapping) {
        return Map.of();
    }

    @Override
    public String getId() {
        return ID;
    }
}
