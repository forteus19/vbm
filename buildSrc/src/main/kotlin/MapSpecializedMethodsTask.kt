import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.quiltmc.enigma.command.MapSpecializedMethodsCommand

abstract class MapSpecializedMethodsTask : DefaultTask() {
    @get:InputFile
    abstract val jar: RegularFileProperty
    @get:InputDirectory
    abstract val input: DirectoryProperty
    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun run() {
        MapSpecializedMethodsCommand.run(
            jar.get().asPath,
            input.get().asPath,
            output.get().asPath,
            "intermediary",
            "named"
        )
    }
}
