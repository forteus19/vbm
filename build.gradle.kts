import de.undercouch.gradle.tasks.download.Download
import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import net.fabricmc.tinyremapper.TinyUtils
import java.nio.file.Files

buildscript {
    repositories {
        maven("https://maven.fabricmc.net")
        mavenCentral()
    }
    dependencies {
        classpath("com.google.code.gson:gson:2.13.1")
        classpath("net.fabricmc:tiny-remapper:0.11.1")
    }
}

plugins {
    id("de.undercouch.download") version "5.6.0"
}

repositories {
    maven("https://maven.jaxonpow.com/snapshots")
}

val bfVersion = "0.7.0.9b"

val bfDownloadUrl = "https://cdn.modrinth.com/data/hTexWmdS/versions/WmlyHsQJ/BlockFront-1.21.1-0.7.0.9b-RELEASE.jar"
val intermediaryDownloadUrl = "https://raw.githubusercontent.com/forteus19/bf-intermediary/main/intermediary/${bfVersion}.tiny"

val vbmBuildFile = layout.buildDirectory.file("vbm").get().asFile
val baseJarFile = vbmBuildFile.resolve("original").resolve("${bfVersion}.jar")
val intermediaryMappingsFile = vbmBuildFile.resolve("intermediaryMapping").resolve("${bfVersion}.tiny")
val intermediaryJarFile = vbmBuildFile.resolve("intermediaryJar").resolve("${bfVersion}.jar")

val mappingsFile = layout.projectDirectory.file("mappings").asFile

val enigmaRuntime: Configuration by configurations.creating {
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
    }
}

dependencies {
    enigmaRuntime("cuchaz:enigma-swing:2.5.2-NRC-SNAPSHOT")
}

val downloadBaseJarTask = tasks.register<Download>("downloadBaseJar") {
    group = "vbm"
    src(bfDownloadUrl)
    dest(baseJarFile)
    overwrite(false)
}

val downloadIntermediaryTask = tasks.register<Download>("downloadIntermediary") {
    group = "vbm"
    src(intermediaryDownloadUrl)
    dest(intermediaryMappingsFile)
    overwrite(false)
}

val mapIntermediaryJarTask = tasks.register<MapJarTask>("mapIntermediaryJar") {
    dependsOn(downloadBaseJarTask)
    dependsOn(downloadIntermediaryTask)
    group = "vbm"
    input.set(baseJarFile)
    mappings.set(intermediaryMappingsFile)
    output.set(intermediaryJarFile)
    from.set("official")
    to.set("intermediary")
}

val enigmaTask = tasks.register<JavaExec>("enigma") {
    dependsOn(mapIntermediaryJarTask)
    group = "vbm"
    classpath = files(enigmaRuntime)
    mainClass = "cuchaz.enigma.gui.Main"
    args("-jar", intermediaryJarFile.absolutePath, "-mappings", mappingsFile.absolutePath)
}

abstract class MapJarTask : DefaultTask() {
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

    @TaskAction
    fun run() {
        val inputPath = input.asFile.get().toPath()
        val mappingsPath = mappings.asFile.get().toPath()
        val outputPath = output.asFile.get().toPath()

        Files.deleteIfExists(outputPath)

        val remapper = TinyRemapper.newRemapper()
            .withMappings(TinyUtils.createTinyMappingProvider(mappingsPath, from.get(), to.get()))
            .build()

        OutputConsumerPath.Builder(outputPath).build().use { output ->
            output.addNonClassFiles(inputPath)
            remapper.readInputsAsync(inputPath)
            remapper.apply(output)
            remapper.finish()
        }
    }
}
