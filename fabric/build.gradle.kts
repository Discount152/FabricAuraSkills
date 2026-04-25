plugins {
    java
    id("fabric-loom") version "1.13.6"
}

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipFile

/** Ship JARs as {@code FabricAuraSkills-<projectVersion>[ -sources].jar} (version from root {@code projectVersion}). */
tasks.withType<org.gradle.api.tasks.bundling.Jar>().configureEach {
    archiveBaseName.set("FabricAuraSkills")
}
tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveBaseName.set("FabricAuraSkills")
    finalizedBy("verifyFabricReleaseBundle")
}
tasks.named<net.fabricmc.loom.task.RemapSourcesJarTask>("remapSourcesJar") {
    archiveBaseName.set("FabricAuraSkills")
}

sourceSets {
    named("main") {
        resources {
            srcDir("${rootProject.projectDir}/common/src/main/resources")
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

loom {
    mods {
        register("auraskills") {
            sourceSet("main")
        }
    }
    runs {
        named("server") {
            server()
            ideConfigGenerated(true)
        }
        named("client") {
            client()
            ideConfigGenerated(true)
        }
    }
}

repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://jitpack.io")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings("net.fabricmc:yarn:1.21.11+build.4:v2")
    // Match common Apex Hosting / shared-host fabric_1.21.11 images (Loader 0.18.4); fabric.mod.json requires >=0.18.4.
    modImplementation("net.fabricmc:fabric-loader:0.18.4")
    // Align with common Fabric 1.21.11 host images (was 0.140.2; 0.141.x matches many shared-host 1.21.11 packs).
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.141.3+1.21.11")

    // Aligns with MC 1.21.11 per https://github.com/lucko/fabric-permissions-api/blob/master/USAGE.md
    include(modImplementation("me.lucko:fabric-permissions-api:0.6.1")!!)

    implementation(project(":api"))
    implementation(project(":common"))
    // Slate Fabric port (skeleton); Loom expects namedElements between Loom subprojects.
    implementation(project(":granitelib-fabric", configuration = "namedElements"))

    implementation("com.google.guava:guava:33.2.1-jre")

    // Jar-in-Jar: ship api + common + granitelib-fabric inside the remapped mod (production server has only this one jar).
    include(implementation(project(":api"))!!)
    include(implementation(project(":common"))!!)
    include(implementation(project(":granitelib-fabric", configuration = "namedElements"))!!)

    // :common uses these at runtime but they are not inside common.jar — mirror common/build.gradle.kts.
    implementation("dev.aurelium:polyglot:1.2.4") {
        exclude(group = "org.spongepowered", module = "configurate-yaml")
        exclude(group = "org.spongepowered", module = "configurate-core")
    }
    include(
        implementation("dev.aurelium:polyglot:1.2.4") {
            exclude(group = "org.spongepowered", module = "configurate-yaml")
            exclude(group = "org.spongepowered", module = "configurate-core")
        }!!,
    )
    val embeddedFromCommon = listOf(
        "net.kyori:adventure-api:4.25.0",
        "co.aikar:acf-core:0.5.1-SNAPSHOT",
        "net.kyori:adventure-text-serializer-legacy:4.25.0",
        "net.kyori:adventure-text-serializer-gson:4.25.0",
        "net.kyori:adventure-text-minimessage:4.25.0",
        "com.ezylang:EvalEx:3.3.0",
        "org.spongepowered:configurate-core:4.1.2",
        "org.spongepowered:configurate-yaml:4.1.2",
        "org.atteo:evo-inflector:1.3",
        "com.github.Querz:NBT:6.1",
    )
    embeddedFromCommon.forEach { lib ->
        implementation(lib)
        include(implementation(lib)!!)
    }
    implementation("com.zaxxer:HikariCP:5.1.0") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    include(
        implementation("com.zaxxer:HikariCP:5.1.0") {
            exclude(group = "org.slf4j", module = "slf4j-api")
        },
    )

    testImplementation(platform("org.junit:junit-bom:5.13.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

/**
 * Post-[remapJar] audit: JiJ must include api/common, polyglot, and Configurate **4.1.2** only (no 4.2 — modpack clash).
 * Runs automatically after `remapJar` (e.g. as part of `build`). Manual: `gradlew -PfabricOnly=true :fabric:verifyFabricReleaseBundle`.
 */
tasks.register("verifyFabricReleaseBundle") {
    group = "verification"
    description =
        "Audit META-INF/jars in the remapped mod for required embedded libs and forbidden configurate 4.2."
    dependsOn("remapJar")
    doLast {
        val jarFile = tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar").get().archiveFile.get().asFile
        require(jarFile.isFile) { "Remapped jar missing: $jarFile" }
        val nestedJars = ZipFile(jarFile).use { z ->
            z.entries().toList()
                .filter { !it.isDirectory && it.name.startsWith("META-INF/jars/") && it.name.endsWith(".jar") }
                .map { it.name.substringAfterLast("/") }
        }
        fun requireJar(predicate: (String) -> Boolean, msg: String) {
            require(nestedJars.any(predicate)) { "$msg — nested jars: $nestedJars" }
        }
        requireJar({ it.startsWith("api-") && it.endsWith(".jar") }, "JiJ must include api-*.jar")
        requireJar({ it.startsWith("common-") && it.endsWith(".jar") }, "JiJ must include common-*.jar")
        requireJar(
            { it.startsWith("granitelib-fabric-") && it.endsWith(".jar") },
            "JiJ must include granitelib-fabric-*.jar",
        )
        requireJar({ it == "configurate-core-4.1.2.jar" }, "JiJ must include configurate-core-4.1.2.jar")
        requireJar({ it == "configurate-yaml-4.1.2.jar" }, "JiJ must include configurate-yaml-4.1.2.jar")
        requireJar({ it.contains("polyglot") }, "JiJ must include polyglot")
        val forbidden = nestedJars.filter {
            Regex("configurate-(core|yaml)-4\\.2.*\\.jar").matches(it)
        }
        require(forbidden.isEmpty()) {
            "JiJ must not bundle configurate 4.2 (runtime clash with 4.1.x on servers): $forbidden"
        }
        logger.lifecycle(
            "verifyFabricReleaseBundle: OK ({} nested jars in {})",
            nestedJars.size,
            jarFile.name,
        )
    }
}

/** Preserves every successful remapped JAR under {@code fabric-mod-jar-history/} (survives {@code :fabric:clean}). */
tasks.register("archiveFabricModJar") {
    group = "build"
    description =
        "Copy FabricAuraSkills remapped JAR to fabric-mod-jar-history/ with a timestamp (keeps all iterations)."
    dependsOn("remapJar")
    doLast {
        val remap = tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar").get()
        val src = remap.archiveFile.get().asFile
        val destDir = rootProject.layout.projectDirectory.dir("fabric-mod-jar-history").asFile
        destDir.mkdirs()
        val ts = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")
            .withZone(ZoneId.systemDefault())
            .format(Instant.now())
        val dest = destDir.resolve("FabricAuraSkills-${project.version}-$ts.jar")
        src.copyTo(dest, overwrite = false)

    }
}

/**
 * Gradle [clean] wipes [fabric/build] including [fabric/build/libs]. Copy any existing
 * FabricAuraSkills jars out first so every iteration stays under [fabric-mod-jar-history/]
 * for documentation / rollback (same intent as [archiveFabricModJar] after remap).
 */
tasks.named("clean").configure {
    doFirst("archiveFabricJarsBeforeClean") {
        val libsDir = layout.buildDirectory.get().asFile.resolve("libs")
        if (!libsDir.isDirectory) {
            return@doFirst
        }
        val jars = libsDir.listFiles()
            ?.filter { it.isFile && it.name.startsWith("FabricAuraSkills-") && it.name.endsWith(".jar") }
            .orEmpty()
        if (jars.isEmpty()) {
            return@doFirst
        }
        val destDir = rootProject.layout.projectDirectory.dir("fabric-mod-jar-history").asFile
        destDir.mkdirs()
        val ts = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")
            .withZone(ZoneId.systemDefault())
            .format(Instant.now())
        jars.sortedBy { it.name }.forEachIndexed { idx, jar ->
            val stem = jar.name.removeSuffix(".jar")
            val dest = destDir.resolve("$stem-preclean-$ts-$idx.jar")
            try {
                jar.copyTo(dest, overwrite = false)
            } catch (e: Exception) {
                logger.warn("Could not archive {} before clean: {}", jar.name, e.message)
            }
        }
        logger.lifecycle(
            "clean: archived {} FabricAuraSkills jar(s) from build/libs to fabric-mod-jar-history (*-preclean-{}-*.jar) before deleting build/",
            jars.size,
            ts,
        )
    }
}

tasks.named("build") {
    finalizedBy("archiveFabricModJar")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
    options.release.set(21)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
