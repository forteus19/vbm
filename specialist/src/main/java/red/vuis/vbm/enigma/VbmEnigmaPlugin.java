package red.vuis.vbm.enigma;

import cuchaz.enigma.analysis.index.JarIndex;
import cuchaz.enigma.api.EnigmaPlugin;
import cuchaz.enigma.api.EnigmaPluginContext;
import cuchaz.enigma.api.service.JarIndexerService;
import cuchaz.enigma.api.service.NameProposalService;
import cuchaz.enigma.classprovider.ClassProvider;
import cuchaz.enigma.translation.mapping.EntryRemapper;
import cuchaz.enigma.translation.representation.entry.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import red.vuis.vbm.proposal.ProposalRegistry;

public final class VbmEnigmaPlugin implements EnigmaPlugin {
    private static final EnigmaProposalCollector COLLECTOR = new EnigmaProposalCollector();

    @Override
    public void init(EnigmaPluginContext ctx) {
        ctx.registerService("vbm:jar_indexer", JarIndexerService.TYPE, serviceCtx -> VbmEnigmaPlugin::acceptJar);
        ctx.registerService("vbm:name_proposal", NameProposalService.TYPE, serviceCtx -> VbmEnigmaPlugin::proposeName);
    }

    private static void acceptJar(Set<String> scope, ClassProvider classProvider, JarIndex jarIndex) {
        ProposalRegistry.collect(COLLECTOR, scope.stream()
                .map(classProvider::get)
                .filter(Objects::nonNull)
                .iterator());
    }

    private static Optional<String> proposeName(Entry<?> entry, EntryRemapper entryRemapper) {
        return Optional.ofNullable(COLLECTOR.targets.get(entry));
    }
}
