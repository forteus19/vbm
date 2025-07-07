plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    maven("https://maven.fabricmc.net")
    maven("https://maven.quiltmc.org/repository/release")
    mavenCentral()
}

dependencies {
    implementation("red.vuis.vbm:specialist:1.0-SNAPSHOT")
    implementation("net.fabricmc:mapping-io:0.7.1")
    implementation("org.quiltmc:enigma-cli:2.6.2")
    implementation("net.fabricmc:tiny-remapper:0.11.1")
}
