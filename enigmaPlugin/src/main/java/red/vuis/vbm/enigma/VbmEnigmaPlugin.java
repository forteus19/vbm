package red.vuis.vbm.enigma;

import cuchaz.enigma.api.EnigmaPlugin;
import cuchaz.enigma.api.EnigmaPluginContext;
import cuchaz.enigma.api.service.JarIndexerService;
import cuchaz.enigma.api.service.NameProposalService;
import java.util.Optional;

public final class VbmEnigmaPlugin implements EnigmaPlugin {
    @Override
    public void init(EnigmaPluginContext ctx) {
        final FieldNameVisitor visitor = new FieldNameVisitor();

        ctx.registerService("vbm:jar_indexer", JarIndexerService.TYPE, serviceCtx -> JarIndexerService.fromVisitor(visitor));
        ctx.registerService("vbm:name_proposal", NameProposalService.TYPE, serviceCtx -> getProposer(visitor));
    }

    private NameProposalService getProposer(FieldNameVisitor visitor) {
        return (entry, remapper) -> Optional.ofNullable(visitor.getName(entry));
    }
}
