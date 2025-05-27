package red.vuis.vbm.proposal;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import red.vuis.vbm.proposal.visitor.BFBlocksVisitor;
import red.vuis.vbm.proposal.visitor.EnumVisitor;
import red.vuis.vbm.proposal.visitor.LdcStringForInvokeVisitor;
import red.vuis.vbm.proposal.visitor.ProposalVisitor;

public final class ProposalRegistry {
    private static final List<VisitorEntry> ENTRIES = List.of(
            new VisitorEntry(
                    EnumVisitor::new,
                    node -> (node.access & Opcodes.ACC_ENUM) != 0
            ),
            new VisitorEntry(
                    collector -> new LdcStringForInvokeVisitor(collector, Opcodes.INVOKEVIRTUAL, 0, "register", "registerItem"),
                    /* BFAttachments */ "com/boehmod/blockfront/unnamed/BF_1110",
                    /* BFBlockAttributes */ "com/boehmod/blockfront/unnamed/BF_1086",
                    /* BFBlockSoundAttributes */ "com/boehmod/blockfront/unnamed/BF_1088",
                    /* BFBlockTraversableAttributes */ "com/boehmod/blockfront/unnamed/BF_1090",
                    /* BFBotVoices */ "com/boehmod/blockfront/unnamed/BF_1092",
                    /* BFCreativeTabs */ "com/boehmod/blockfront/unnamed/BF_1103",
                    /* BFDataComponents */ "com/boehmod/blockfront/unnamed/BF_1104",
                    /* BFEntityTypes */ "com/boehmod/blockfront/unnamed/BF_1106",
                    /* BFItems */ "com/boehmod/blockfront/unnamed/BF_1107",
                    /* BFParticles */ "com/boehmod/blockfront/unnamed/BF_1109"
            ),
            new VisitorEntry(
                    collector -> new LdcStringForInvokeVisitor(collector, Opcodes.INVOKESTATIC, 0, "method_5444"),
                    /* BFSounds */ "com/boehmod/blockfront/unnamed/BF_1112"
            ),
            new VisitorEntry(
                    collector -> new LdcStringForInvokeVisitor(collector, Opcodes.INVOKESPECIAL, 0, "<init>"),
                    /* BFBlockSetTypes */ "com/boehmod/blockfront/unnamed/BF_1089"
            ),
            new VisitorEntry(
                    collector -> new LdcStringForInvokeVisitor(collector, Opcodes.INVOKESPECIAL, 1, "<init>"),
                    /* BFClientSettings */ "com/boehmod/blockfront/unnamed/BF_432"
            )
    );

    private ProposalRegistry() {}

    public static void collect(ProposalCollector collector, Iterator<ClassNode> classNodes) {
        BFBlocksVisitor bfBlocksVisitor = new BFBlocksVisitor(collector);
        classNodes.forEachRemaining(node -> {
            for (VisitorEntry entry : ENTRIES) {
                if (entry.test(node)) {
                    ProposalVisitor visitor = entry.constructor().apply(collector);
                    node.accept(visitor);
                }
            }
            bfBlocksVisitor.accept(node);
        });
        collector.finished();
    }

    private record VisitorEntry(Function<ProposalCollector, ProposalVisitor> constructor, Predicate<ClassNode> nodeTest) implements Predicate<ClassNode> {
        public VisitorEntry(Function<ProposalCollector, ProposalVisitor> constructor, List<String> classNames) {
            this(constructor, node -> classNames.contains(node.name));
        }

        public VisitorEntry(Function<ProposalCollector, ProposalVisitor> constructor, String... classNames) {
            this(constructor, List.of(classNames));
        }

        @Override
        public boolean test(ClassNode classNode) {
            return nodeTest == null || nodeTest.test(classNode);
        }
    }
}
