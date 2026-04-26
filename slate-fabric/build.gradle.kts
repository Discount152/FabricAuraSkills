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

    // Template YAML (Spike 0 / Sprint 95) — JiJ so standalone granitelib-fabric resolves at runtime.
    // 2.3 — Loom JiJ semver warning on plain "2.2"
    implementation("org.yaml:snakeyaml:2.3")
    include(implementation("org.yaml:snakeyaml:2.3")!!)

    testImplementation(platform("org.junit:junit-bom:5.13.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<org.gradle.api.tasks.bundling.Jar>().configureEach {
    archiveBaseName.set("granitelib-fabric")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

val granitelibVersion: String =
    requireNotNull(rootProject.findProperty("granitelibVersion") as? String) {
        "root gradle.properties must define granitelibVersion (see granitelib standalone metadata)"
    }

tasks.processResources {
    inputs.property("version", granitelibVersion)
    filesMatching("fabric.mod.json") {
        expand("version" to granitelibVersion)
    }
}
