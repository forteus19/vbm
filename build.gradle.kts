import de.undercouch.gradle.tasks.download.Download
import net.fabricmc.mappingio.format.MappingFormat

plugins {
    `java-library`
    `maven-publish`
    id("de.undercouch.download") version "5.6.0"
}

val bfVersion = "0.9.0.19b"

group = "red.vuis.vbm"
version = "${bfVersion}-SNAPSHOT"

repositories {
    maven("https://maven.quiltmc.org/repository/release")
    mavenCentral()
}

val bfDownloadUrl = "https://cdn.modrinth.com/data/hTexWmdS/versions/AyMDQBD3/BlockFront-1.21.1-0.9.0.19b-RELEASE.jar"
val intermediaryDownloadUrl = "https://raw.githubusercontent.com/forteus19/bf-intermediary/main/intermediary/${bfVersion}.tiny"

val vbmBuildFile = layout.buildDirectory.file("vbm").get().asFile
val baseJarFile = vbmBuildFile.resolve("originalJar").resolve("${bfVersion}-original.jar")
val intermediaryMappingsFile = vbmBuildFile.resolve("intermediaryMapping").resolve("${bfVersion}-intermediary.tiny")
val intermediaryJarFile = vbmBuildFile.resolve("intermediaryJar").resolve("${bfVersion}-intermediary.jar")
val intermediaryJarFullFile = vbmBuildFile.resolve("intermediaryJar").resolve("${bfVersion}-intermediary-full.jar")
val specializedMappingsFile = vbmBuildFile.resolve("specializedMapping").resolve("${bfVersion}-specialized.tiny")
val proposedMappingsFile = vbmBuildFile.resolve("proposedMapping").resolve("${bfVersion}-proposed.tiny")
val mergedMappingsFile = vbmBuildFile.resolve("mergedMapping").resolve("${bfVersion}-merged.tiny")
val namedJarFile = vbmBuildFile.resolve("namedJar").resolve("${bfVersion}-named.jar")
val namedJarFullFile = vbmBuildFile.resolve("namedJar").resolve("${bfVersion}-named-full.jar")
val decompVineflowerFile = vbmBuildFile.resolve("decompVineflower").resolve(bfVersion)

val mappingsFile = layout.projectDirectory.file("mappings").asFile

val enigmaRuntime: Configuration = configurations.create("enigmaRuntime") {
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
    }
}
val decompileRuntime: Configuration = configurations.create("decompileRuntime")

dependencies {
    enigmaRuntime("org.vineflower:vineflower:1.11.1")
    enigmaRuntime("org.quiltmc:enigma-swing:2.7.2")
    decompileRuntime("org.vineflower:vineflower:1.11.1")
}

val formatMappingsTask = tasks.register<FormatMappingsTask>("formatMappings") {
    group = "vbm"
    input.set(mappingsFile)
    output.set(mappingsFile)
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

val mapIntermediaryJarFullTask = tasks.register<TinyRemapperTask>("mapIntermediaryJarFull") {
    dependsOn(downloadBaseJarTask, downloadIntermediaryTask)
    group = "vbm"
    input.set(downloadBaseJarTask.get().dest)
    mappings.set(downloadIntermediaryTask.get().dest)
    output.set(intermediaryJarFullFile)
    from.set("official")
    to.set("intermediary")
    nonClassFiles.set(true)
}

val mapSpecializedMethodsTask = tasks.register<MapSpecializedMethodsTask>("mapSpecializedMethods") {
    dependsOn(mapIntermediaryJarTask)
    group = "vbm"
    jar.set(mapIntermediaryJarTask.get().output)
    input.set(mappingsFile)
    output.set(specializedMappingsFile)
}

val insertProposedMappingsTask = tasks.register<InsertProposedMappingsTask>("insertProposedMappings") {
    dependsOn(mapSpecializedMethodsTask)
    group = "vbm"
    jar.set(mapIntermediaryJarTask.get().output)
    input.set(mapSpecializedMethodsTask.get().output)
    output.set(proposedMappingsFile)
    format.set(MappingFormat.TINY_2_FILE)
}

val mergeMappingsTask = tasks.register<MergeMappingsTask>("mergeMappings") {
    dependsOn(insertProposedMappingsTask)
    group = "vbm"
    inputFiles.from(downloadIntermediaryTask.get().dest, insertProposedMappingsTask.get().output)
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
    dependsOn(mapIntermediaryJarTask, project(":specialist").tasks["build"])
    finalizedBy(formatMappingsTask)
    group = "vbm"
    classpath = files(enigmaRuntime, project(":specialist").tasks["jar"].outputs)
    mainClass = "org.quiltmc.enigma.gui.Main"
    args("-jar", intermediaryJarFile.absolutePath, "-mappings", mappingsFile.absolutePath, "-profile", file("enigma.json").absolutePath)
}

val checkMappingsTask = tasks.register<CheckMappingsTask>("checkMappings") {
    dependsOn(mapSpecializedMethodsTask)
    group = "vbm"
    jar.set(mapIntermediaryJarTask.get().output)
    mappings.set(mapSpecializedMethodsTask.get().output)
}

val decompileVineflowerTask = tasks.register<JavaExec>("decompileVineflower") {
    dependsOn(mapNamedJarTask)
    group = "vbm"
    classpath = files(decompileRuntime)
    mainClass = "org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler"
    args("--folder", mapNamedJarTask.get().output.get().asFile.absolutePath, decompVineflowerFile.absolutePath)
}

val mappingsJarTask = tasks.register<Jar>("mappingsJar") {
    dependsOn(mergeMappingsTask)
    group = "vbm"
    archiveBaseName = "mappings"
    from(mergeMappingsTask.get().output) {
        rename { "mappings/merged.tiny" }
    }
    manifest {
        attributes["BlockFront-Version"] = bfVersion
        attributes["BlockFront-Origin"] = bfDownloadUrl
    }
}

tasks.build {
    dependsOn(mappingsJarTask)
}

publishing {
    publications {
        create<MavenPublication>("mappings") {
            groupId = project.group.toString()
            artifactId = "mappings"
            version = project.version.toString()

            artifact(mappingsJarTask)
        }
    }
}

allprojects {
    apply(plugin = "maven-publish")

    publishing {
        repositories {
            val envUrl = System.getenv("MAVEN_URL")
            val envUsername = System.getenv("MAVEN_USERNAME")
            val envPassword = System.getenv("MAVEN_PASSWORD")
            if (envUrl != null && envUsername != null && envPassword != null) {
                maven {
                    url = uri(envUrl)
                    credentials {
                        username = envUsername
                        password = envPassword
                    }
                    isAllowInsecureProtocol = true
                }
            }
        }
    }
}
