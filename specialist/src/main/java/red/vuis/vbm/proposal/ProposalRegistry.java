package red.vuis.vbm.proposal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import red.vuis.vbm.proposal.visitor.AllClassesVisitor;
import red.vuis.vbm.proposal.visitor.BFBlocksVisitor;
import red.vuis.vbm.proposal.visitor.EnumVisitor;
import red.vuis.vbm.proposal.visitor.LdcStringForInvokeVisitor;
import red.vuis.vbm.proposal.visitor.PacketVisitor;
import red.vuis.vbm.proposal.visitor.ProposalVisitor;
import red.vuis.vbm.proposal.visitor.RecordVisitor;
import red.vuis.vbm.util.VbmUtils;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ProposalRegistry {
    private static final List<VisitorEntry> ENTRIES = List.of(
            new VisitorEntry(
                    AllClassesVisitor::new,
                    node -> true
            ),
            new VisitorEntry(
                    RecordVisitor::new,
                    node -> RecordVisitor.RECORD.equals(node.superName)
            ),
            new VisitorEntry(
                    EnumVisitor::new,
                    node -> (node.access & Opcodes.ACC_ENUM) != 0
            ),
            new VisitorEntry(
                    PacketVisitor::new,
                    node -> node.interfaces.contains(PacketVisitor.CUSTOM_PACKET_PAYLOAD)
            ),
            new VisitorEntry(
                    BFBlocksVisitor::new,
                    BFBlocksVisitor.BF_BLOCK_ENTITY_TYPES,
                    BFBlocksVisitor.BF_BLOCKS
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
            ),
            new VisitorEntry(
                    collector -> new LdcStringForInvokeVisitor(collector, Opcodes.INVOKESTATIC, 0, "method_1581"),
                    /* GunDamageConfigs */ "com/boehmod/blockfront/unnamed/BF_1454",
                    /* GunSpreadConfigs */ "com/boehmod/blockfront/unnamed/BF_1433"
            )
    );

    private ProposalRegistry() {}

    public static void collect(ProposalCollector collector, Iterable<ClassNode> classNodes) {
        ProposalVisitor[] visitors = new ProposalVisitor[ENTRIES.size()];
        for (int i = 0; i < visitors.length; i++) {
            visitors[i] = ENTRIES.get(i).constructor().apply(collector);
        }

        for (ClassNode node : classNodes) {
            for (int i = 0; i < visitors.length; i++) {
                if (ENTRIES.get(i).test(node)) {
                    node.accept(visitors[i]);
                }
            }
        }

        collector.finished();
    }

    private record VisitorEntry(@NotNull VisitorConstructor constructor, @Nullable Predicate<ClassNode> nodeTest) implements Predicate<ClassNode> {
        public VisitorEntry(VisitorConstructor constructor) {
            this(constructor, (Predicate<ClassNode>) null);
        }

        public VisitorEntry(VisitorConstructor constructor, List<String> classNames) {
            this(constructor, node -> classNames.contains(node.name));
        }

        public VisitorEntry(VisitorConstructor constructor, String classNameFirst, String... classNames) {
            this(constructor, VbmUtils.listOf(classNameFirst, classNames));
        }

        @Override
        public boolean test(ClassNode classNode) {
            return nodeTest == null || nodeTest.test(classNode);
        }
    }

    private interface VisitorConstructor extends Function<ProposalCollector, ProposalVisitor> {
    }
}
