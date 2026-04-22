plugins {
    java
    id("fabric-loom") version "1.13.6"
}

/** Fabric port of Slate — skeleton. Upstream Bukkit Slate lives in {@code vendor/Slate} (git submodule). */
group = "dev.aurelium"
version = rootProject.version

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

loom {
    // Library module: no dev run configs required; :fabric consumes namedElements.
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings("net.fabricmc:yarn:1.21.11+build.4:v2")
    modImplementation("net.fabricmc:fabric-loader:0.18.4")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.141.3+1.21.11")
}

tasks.withType<org.gradle.api.tasks.bundling.Jar>().configureEach {
    archiveBaseName.set("granitelib-fabric")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}
