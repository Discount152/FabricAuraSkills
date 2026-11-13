plugins {
    java
    id("fabric-loom") version "1.20.0-alpha.31"
}

/** Fabric port of Slate — skeleton. Upstream Bukkit Slate lives in {@code vendor/Slate} (git submodule). */
group = "dev.aurelium"
version = rootProject.version

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
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
    minecraft("com.mojang:minecraft:26.3")
    implementation("net.fabricmc:fabric-loader:0.20.1")
    implementation("net.fabricmc.fabric-api:fabric-api:0.166.2+26.3")
}

tasks.withType<org.gradle.api.tasks.bundling.Jar>().configureEach {
    archiveBaseName.set("slate-fabric")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}
