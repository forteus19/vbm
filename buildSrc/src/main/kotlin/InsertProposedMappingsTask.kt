import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.MappingWriter
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import red.vuis.vbm.mappingio.MappingIoProposalCollector
import red.vuis.vbm.proposal.ProposalRegistry
import red.vuis.vbm.util.VbmUtils

abstract class InsertProposedMappingsTask : DefaultTask() {
    @get:InputFile
    abstract val jar: RegularFileProperty
    @get:InputFile
    abstract val input: RegularFileProperty
    @get:OutputFile
    abstract val output: RegularFileProperty
    @get:Input
    abstract val format: Property<MappingFormat>

    @TaskAction
    fun run() {
        val classes = VbmUtils.readJarClassNodes(jar.get().asPath)
        val tree = MemoryMappingTree()
        MappingReader.read(input.get().asPath, tree);

        tree.reset()
        tree.visitNamespaces("intermediary", listOf("named"))

        val collector = MappingIoProposalCollector(tree, tree.getNamespaceId("named"));
        ProposalRegistry.collect(collector, classes)

        tree.accept(MappingWriter.create(output.get().asPath, format.get()))
    }
}
