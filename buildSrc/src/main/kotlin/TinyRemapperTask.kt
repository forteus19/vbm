import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import net.fabricmc.tinyremapper.TinyUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files

abstract class TinyRemapperTask : DefaultTask() {
    @get:InputFile
    abstract val input: RegularFileProperty
    @get:InputFile
    abstract val mappings: RegularFileProperty
    @get:OutputFile
    abstract val output: RegularFileProperty
    @get:Input
    abstract val from: Property<String>
    @get:Input
    abstract val to: Property<String>
    @get:Input
    abstract val nonClassFiles: Property<Boolean>

    @TaskAction
    fun run() {
        val inputPath = input.get().asPath
        val mappingsPath = mappings.get().asPath
        val outputPath = output.get().asPath
        Files.deleteIfExists(outputPath)

        val remapper = TinyRemapper.newRemapper()
            .withMappings(TinyUtils.createTinyMappingProvider(mappingsPath, from.get(), to.get()))
            .build()

        OutputConsumerPath.Builder(outputPath).build().use { output ->
            if (nonClassFiles.get()) {
                output.addNonClassFiles(inputPath)
            }
            remapper.readInputsAsync(inputPath)
            remapper.apply(output)
            remapper.finish()
        }
    }
}
