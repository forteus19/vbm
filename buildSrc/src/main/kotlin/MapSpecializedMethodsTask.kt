import cuchaz.enigma.command.MapSpecializedMethodsCommand
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class MapSpecializedMethodsTask : DefaultTask() {
    @get:InputFile
    abstract val jar: RegularFileProperty
    @get:InputDirectory
    abstract val input: DirectoryProperty
    @get:OutputFile
    abstract val output: RegularFileProperty
    @get:Input
    abstract val inputFormat: Property<String>
    @get:Input
    abstract val outputFormat: Property<String>

    @TaskAction
    fun run() {
        MapSpecializedMethodsCommand.run(
            jar.get().asPath,
            inputFormat.get(),
            input.get().asPath,
            outputFormat.get(),
            output.get().asPath
        )
    }
}
