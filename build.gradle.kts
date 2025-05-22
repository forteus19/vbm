import de.undercouch.gradle.tasks.download.Download
import net.fabricmc.mappingio.format.MappingFormat

plugins {
    id("de.undercouch.download") version "5.6.0"
    idea
}

idea.project {
    setLanguageLevel("21")
}

repositories {
    maven("https://maven.jaxonpow.com/snapshots")
    mavenCentral()
}

val bfVersion = "0.7.0.10b"

val bfDownloadUrl = "https://cdn.modrinth.com/data/hTexWmdS/versions/2w8sWRMB/BlockFront-1.21.1-0.7.0.10b-RELEASE.jar"
val intermediaryDownloadUrl = "https://raw.githubusercontent.com/forteus19/bf-intermediary/main/intermediary/${bfVersion}.tiny"

val vbmBuildFile = layout.buildDirectory.file("vbm").get().asFile
val baseJarFile = vbmBuildFile.resolve("originalJar").resolve("${bfVersion}-original.jar")
val intermediaryMappingsFile = vbmBuildFile.resolve("intermediaryMapping").resolve("${bfVersion}-intermediary.tiny")
val intermediaryJarFile = vbmBuildFile.resolve("intermediaryJar").resolve("${bfVersion}-intermediary.jar")
val specializedMappingsFile = vbmBuildFile.resolve("specializedMapping").resolve("${bfVersion}-specialized.tiny")
val mergedMappingsFile = vbmBuildFile.resolve("mergedMapping").resolve("${bfVersion}-merged.tiny")
val namedJarFile = vbmBuildFile.resolve("namedJar").resolve("${bfVersion}-named.jar")
val namedJarFullFile = vbmBuildFile.resolve("namedJar").resolve("${bfVersion}-named-full.jar")
val decompVineflowerFile = vbmBuildFile.resolve("decompVineflower").resolve(bfVersion)

val mappingsFile = layout.projectDirectory.file("mappings").asFile

val enigmaRuntime: Configuration by configurations.creating {
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
    }
}
val decompileRuntime: Configuration by configurations.creating

dependencies {
    enigmaRuntime("cuchaz:enigma-swing:2.5.2-NRC-SNAPSHOT")
    decompileRuntime("org.vineflower:vineflower:1.11.1")
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

val mapIntermediaryJarTask = tasks.register<TinyRemapperTask>("mapIntermediaryJar") {
    dependsOn(downloadBaseJarTask, downloadIntermediaryTask)
    group = "vbm"
    input.set(downloadBaseJarTask.get().dest)
    mappings.set(downloadIntermediaryTask.get().dest)
    output.set(intermediaryJarFile)
    from.set("official")
    to.set("intermediary")
    nonClassFiles.set(false)
}

val mapSpecializedMethodsTask = tasks.register<MapSpecializedMethodsTask>("mapSpecializedMethods") {
    dependsOn(mapIntermediaryJarTask)
    group = "vbm"
    jar.set(mapIntermediaryJarTask.get().output)
    input.set(mappingsFile)
    output.set(specializedMappingsFile)
    inputFormat.set("enigma")
    outputFormat.set("tinyv2:intermediary:named")
}

val mergeMappingsTask = tasks.register<MergeMappingsTask>("mergeMappings") {
    dependsOn(mapSpecializedMethodsTask)
    group = "vbm"
    inputFiles.from(downloadIntermediaryTask.get().dest, mapSpecializedMethodsTask.get().output)
    output.set(mergedMappingsFile)
    format.set(MappingFormat.TINY_2_FILE)
}

val mapNamedJarTask = tasks.register<TinyRemapperTask>("mapNamedJar") {
    dependsOn(mergeMappingsTask)
    group = "vbm"
    input.set(mapIntermediaryJarTask.get().output)
    mappings.set(mergeMappingsTask.get().output)
    output.set(namedJarFile)
    from.set("intermediary")
    to.set("named")
    nonClassFiles.set(false)
}

val mapNamedJarFullTask = tasks.register<TinyRemapperTask>("mapNamedJarFull") {
    dependsOn(mergeMappingsTask)
    group = "vbm"
    input.set(downloadBaseJarTask.get().dest)
    mappings.set(mergeMappingsTask.get().output)
    output.set(namedJarFullFile)
    from.set("official")
    to.set("named")
    nonClassFiles.set(true)
}

val enigmaTask = tasks.register<JavaExec>("enigma") {
    dependsOn(mapIntermediaryJarTask, project(":enigmaPlugin").tasks["build"])
    group = "vbm"
    classpath = files(enigmaRuntime, project(":enigmaPlugin").tasks["jar"].outputs)
    mainClass = "cuchaz.enigma.gui.Main"
    args("-jar", intermediaryJarFile.absolutePath, "-mappings", mappingsFile.absolutePath, "-profile", file("enigma.json").absolutePath)
}

val decompileVineflowerTask = tasks.register<JavaExec>("decompileVineflower") {
    dependsOn(mapNamedJarTask)
    group = "vbm"
    classpath = files(decompileRuntime)
    mainClass = "org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler"
    args("--folder", mapNamedJarTask.get().output.get().asFile.absolutePath, decompVineflowerFile.absolutePath)
}
