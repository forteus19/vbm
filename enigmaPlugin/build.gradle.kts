plugins {
    java
}

repositories {
    maven("https://maven.jaxonpow.com/snapshots")
    mavenCentral()
}

dependencies {
    compileOnly("cuchaz:enigma:2.5.2-NRC-SNAPSHOT")
    compileOnly("org.ow2.asm:asm:9.7.1")
    compileOnly("org.ow2.asm:asm-analysis:9.7.1")
    compileOnly("org.ow2.asm:asm-tree:9.7.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
