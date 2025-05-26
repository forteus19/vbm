import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.MappingWriter
import net.fabricmc.mappingio.adapter.MappingDstNsReorder
import net.fabricmc.mappingio.adapter.MappingNsCompleter
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.tree.MappingTreeView
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

// https://github.com/FabricMC/yarn/blob/25w20a/filament/src/main/java/net/fabricmc/filament/task/mappingio/MergeMappingsTask.java
abstract class MergeMappingsTask : DefaultTask() {
    @get:InputFiles
    abstract val inputFiles: ConfigurableFileCollection
    @get:OutputFile
    abstract val output: RegularFileProperty
    @get:Input
    abstract val format: Property<MappingFormat>

    @TaskAction
    fun run() {
        var tree = MemoryMappingTree()
        for (file in inputFiles.files) {
            MappingReader.read(file.toPath(), MappingSourceNsSwitch(tree, "intermediary"))
        }

        val namedId = tree.getNamespaceId("named")
        for (entry in tree.classes) {
            if (entry.getName(namedId) != null) {
                continue
            }
            entry.setDstName(matchEnclosingClass(entry.srcName, tree), namedId)
        }

        MappingWriter.create(output.get().asPath, format.get()).use { writer ->
            tree.accept(
                MappingSourceNsSwitch(
                    MappingDstNsReorder(
                        MappingNsCompleter(
                            writer,
                            mapOf("named" to "intermediary"),
                            true
                        ),
                        listOf("intermediary", "named")
                    ),
                    "official"
                )
            )
        }
    }
}

fun matchEnclosingClass(sharedName: String, tree: MappingTreeView): String {
    val namedId = tree.getNamespaceId("named")
    val path = sharedName.split("$")

    for (i in (path.size - 2) downTo 0) {
        val match = tree.getClass(path.slice(0..i).joinToString("$"))
        if (match != null && match.getName(namedId) != null) {
            return match.getName(namedId) + "$" + path.slice(i + 1 until path.size).joinToString("$")
        }
    }

    return sharedName
}
