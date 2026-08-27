package red.vuis.vbm.enigma.service;

import org.quiltmc.enigma.api.analysis.index.jar.JarIndex;
import org.quiltmc.enigma.api.class_provider.ProjectClassProvider;
import org.quiltmc.enigma.api.service.JarIndexerService;
import red.vuis.vbm.enigma.EnigmaProposalCollector;
import red.vuis.vbm.proposal.ProposalRegistry;

import java.util.Objects;
import java.util.Set;

public class VbmJarIndexer implements JarIndexerService {
    public static final String ID = "vbm:jar_indexer";

    private final EnigmaProposalCollector collector;

    public VbmJarIndexer(EnigmaProposalCollector collector) {
        this.collector = collector;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void acceptJar(Set<String> scope, ProjectClassProvider classProvider, JarIndex jarIndex) {
        ProposalRegistry.collect(collector, scope.stream()
                .map(classProvider::get)
                .filter(Objects::nonNull)
                .toList());
    }
}
