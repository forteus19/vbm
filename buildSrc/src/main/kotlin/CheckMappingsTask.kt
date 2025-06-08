import net.fabricmc.mappingio.MappedElementKind
import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.MappingUtil
import net.fabricmc.mappingio.MappingVisitor
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch
import net.fabricmc.mappingio.tree.MappingTreeView
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import red.vuis.vbm.mappingio.MappingIoProposalCollector
import red.vuis.vbm.proposal.ProposalRegistry
import red.vuis.vbm.util.VbmUtils

abstract class CheckMappingsTask : DefaultTask() {
    @get:InputFile
    abstract val jar: RegularFileProperty
    @get:InputFile
    abstract val mappings: RegularFileProperty

    @TaskAction
    fun run() {
        val classes = VbmUtils.readJarClassNodes(jar.get().asPath)

        val inputTree = MemoryMappingTree()
        MappingReader.read(mappings.get().asPath, inputTree);

        val proposedTree = MemoryMappingTree()
        proposedTree.visitNamespaces("intermediary", listOf("named"))
        val namedId = proposedTree.getNamespaceId("named")
        val collector = MappingIoProposalCollector(proposedTree, namedId)
        ProposalRegistry.collect(collector, classes)

        proposedTree.accept(MappingSourceNsSwitch(DuplicateChecker(inputTree, inputTree.getNamespaceId(MappingUtil.NS_TARGET_FALLBACK)), "named"))
    }
}

class DuplicateChecker(val other: MappingTreeView, val ns: Int) : MappingVisitor {
    private lateinit var currentClass: String

    override fun visitNamespaces(
        srcNamespace: String,
        dstNamespaces: List<String>
    ) {
    }

    override fun visitClass(srcName: String): Boolean {
        currentClass = srcName
        return true
    }

    override fun visitField(srcName: String, srcDesc: String?): Boolean {
        if (other.getField(currentClass, srcName, srcDesc, ns) != null) {
            errorln("Overwritten field: $currentClass : $srcName $srcDesc")
        }
        return false
    }

    override fun visitMethod(srcName: String, srcDesc: String?): Boolean {
        if (other.getMethod(currentClass, srcName, srcDesc, ns) != null) {
            errorln("Overwritten method: $currentClass : $srcName $srcDesc")
        }
        return false
    }

    override fun visitMethodArg(argPosition: Int, lvIndex: Int, srcName: String?): Boolean {
        return false
    }

    override fun visitMethodVar(
        lvtRowIndex: Int,
        lvIndex: Int,
        startOpIdx: Int,
        endOpIdx: Int,
        srcName: String?
    ): Boolean {
        return false
    }

    override fun visitDstName(
        targetKind: MappedElementKind?,
        namespace: Int,
        name: String
    ) {
    }

    override fun visitComment(targetKind: MappedElementKind, comment: String) {
    }
}
