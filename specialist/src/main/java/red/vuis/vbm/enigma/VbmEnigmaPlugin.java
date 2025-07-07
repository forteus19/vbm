package red.vuis.vbm.enigma;

import org.quiltmc.enigma.api.EnigmaPlugin;
import org.quiltmc.enigma.api.EnigmaPluginContext;
import org.quiltmc.enigma.api.service.JarIndexerService;
import org.quiltmc.enigma.api.service.NameProposalService;
import red.vuis.vbm.enigma.service.VbmJarIndexer;
import red.vuis.vbm.enigma.service.VbmNameProposal;

public final class VbmEnigmaPlugin implements EnigmaPlugin {
    private static final EnigmaProposalCollector COLLECTOR = new EnigmaProposalCollector();

    @Override
    public void init(EnigmaPluginContext ctx) {
        ctx.registerService(JarIndexerService.TYPE, serviceCtx -> new VbmJarIndexer(COLLECTOR));
        ctx.registerService(NameProposalService.TYPE, serviceCtx -> new VbmNameProposal(COLLECTOR));
    }
}
