plugins {
    java
    // 26.1+ is unobfuscated. `net.fabricmc.fabric-loom` is the no-remap plugin; the short
    // `fabric-loom` id still expects Yarn/Mojmap and fails on 26.3.
    id("net.fabricmc.fabric-loom") version "1.19.4"
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
    options.release.set(25)
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
