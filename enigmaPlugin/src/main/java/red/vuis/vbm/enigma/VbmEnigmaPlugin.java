package red.vuis.vbm.enigma;

import cuchaz.enigma.analysis.index.JarIndex;
import cuchaz.enigma.api.EnigmaPlugin;
import cuchaz.enigma.api.EnigmaPluginContext;
import cuchaz.enigma.api.service.JarIndexerService;
import cuchaz.enigma.api.service.NameProposalService;
import cuchaz.enigma.classprovider.ClassProvider;
import cuchaz.enigma.translation.mapping.EntryRemapper;
import cuchaz.enigma.translation.representation.entry.Entry;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public final class VbmEnigmaPlugin implements EnigmaPlugin {
    private static final List<VisitorEntry> ENTRIES = List.of(
            new VisitorEntry(
                    new EnumVisitor(),
                    node -> (node.access & Opcodes.ACC_ENUM) != 0
            ),
            new VisitorEntry(
                    new LdcStringForInvokeVisitor(Opcodes.INVOKEVIRTUAL, "register", "registerItem"),
                    /* BFAttachments */ "com/boehmod/blockfront/unnamed/BF_1110",
                    /* BFBlockAttributes */ "com/boehmod/blockfront/unnamed/BF_1086",
                    /* BFBlockSoundAttributes */ "com/boehmod/blockfront/unnamed/BF_1088",
                    /* BFBlockTraversableAttributes */ "com/boehmod/blockfront/unnamed/BF_1090",
                    /* BFBlocks */ "com/boehmod/blockfront/unnamed/BF_1091",
                    /* BFBotVoices */ "com/boehmod/blockfront/unnamed/BF_1092",
                    /* BFCreativeTabs */ "com/boehmod/blockfront/unnamed/BF_1103",
                    /* BFDataComponents */ "com/boehmod/blockfront/unnamed/BF_1104",
                    /* BFEntities */ "com/boehmod/blockfront/unnamed/BF_1106",
                    /* BFItems */ "com/boehmod/blockfront/unnamed/BF_1107",
                    /* BFParticles */ "com/boehmod/blockfront/unnamed/BF_1109"
            ),
            new VisitorEntry(
                    new LdcStringForInvokeVisitor(Opcodes.INVOKESTATIC, "method_5444"),
                   /* BFSounds */ "com/boehmod/blockfront/unnamed/BF_1112"
            )
    );

    @Override
    public void init(EnigmaPluginContext ctx) {
        ctx.registerService("vbm:jar_indexer", JarIndexerService.TYPE, serviceCtx -> VbmEnigmaPlugin::acceptJar);
        ctx.registerService("vbm:name_proposal", NameProposalService.TYPE, serviceCtx -> VbmEnigmaPlugin::proposeName);
    }

    private static void acceptJar(Set<String> scope, ClassProvider classProvider, JarIndex jarIndex) {
        for (String className : scope) {
            ClassNode node = classProvider.get(className);
            if (node == null) {
                continue;
            }
            for (VisitorEntry visitorEntry : ENTRIES) {
                if (visitorEntry.test(node)) {
                    node.accept(visitorEntry.visitor());
                }
            }
        }
    }

    private static Optional<String> proposeName(Entry<?> obfEntry, EntryRemapper remapper) {
        return ProposalVisitor.propose(obfEntry);
    }

    private record VisitorEntry(ProposalVisitor visitor, Predicate<ClassNode> nodeTest) implements Predicate<ClassNode> {
        public VisitorEntry(ProposalVisitor visitor, String... classNames) {
            this(visitor, node -> Arrays.asList(classNames).contains(node.name));
        }

        @Override
        public boolean test(ClassNode classNode) {
            return nodeTest == null || nodeTest.test(classNode);
        }
    }
}
