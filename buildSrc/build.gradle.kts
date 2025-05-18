plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://maven.fabricmc.net")
    mavenCentral()
}

dependencies {
    implementation("cuchaz:enigma-cli:2.5.2")
    implementation("net.fabricmc:tiny-remapper:0.11.1")
    implementation("net.fabricmc:mapping-io:0.7.1")
}
