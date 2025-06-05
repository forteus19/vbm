plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    maven("https://maven.fabricmc.net")
    mavenCentral()
}

dependencies {
    implementation("red.vuis.vbm:specialist:1.0-SNAPSHOT")
    implementation("net.fabricmc:mapping-io:0.7.1")
    implementation("cuchaz:enigma-cli:2.5.2-NRC-SNAPSHOT")
    implementation("net.fabricmc:tiny-remapper:0.11.1")
}
