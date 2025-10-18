plugins {
    `java-library`
    `maven-publish`
}

group = "red.vuis.vbm"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://maven.fabricmc.net")
    maven("https://maven.quiltmc.org/repository/release")
    mavenCentral()
}

dependencies {
    compileOnlyApi("org.jetbrains:annotations:26.0.2")
    api("net.fabricmc:mapping-io:0.7.1")
    api("org.quiltmc:enigma:2.6.2")
    api("org.ow2.asm:asm:9.7.1")
    api("org.ow2.asm:asm-analysis:9.7.1")
    api("org.ow2.asm:asm-tree:9.7.1")
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

publishing {
    publications {
        create<MavenPublication>("specialist") {
            groupId = project.group.toString()
            artifactId = "specialist"
            version = project.version.toString()

            from(components["java"])
        }
    }
}
