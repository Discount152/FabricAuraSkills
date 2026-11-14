buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.ow2.asm:asm:9.11.1")
        classpath("org.ow2.asm:asm-commons:9.11.1")
    }
}

plugins {
    java
    id("fabric-loom") version "1.20.0-alpha.31"
}

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files as NioFiles
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import org.gradle.api.tasks.bundling.Jar

/** Ship JARs as {@code GraniteSkills-<projectVersion>[ -sources].jar} (version from root {@code projectVersion}). */
tasks.withType<org.gradle.api.tasks.bundling.Jar>().configureEach {
    archiveBaseName.set("GraniteSkills")
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
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
}

loom {
    mods {
        register("graniteskills") {
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
    // 26.1+ is unobfuscated: Mojang names are in the game, so there is no mappings dependency.
    minecraft("com.mojang:minecraft:26.3")
    implementation("net.fabricmc:fabric-loader:0.20.1")
    implementation("net.fabricmc.fabric-api:fabric-api:0.166.2+26.3")

    // fabric-permissions-api 0.7.0 depends on Minecraft >=26.1. JiJ so servers do not install it separately.
    include(implementation("me.lucko:fabric-permissions-api:0.7.0")!!)

    implementation(project(":api"))
    implementation(project(":common"))
    // Slate Fabric port (skeleton). Unobfuscated Loom publishes the default variant, not namedElements.
    // Standalone GraniteLib 26.3 menu library. Separate mod jar; not nested in Granite Skills.
    implementation(project(":granitelib-fabric"))

    implementation("com.google.guava:guava:33.2.1-jre")

    // Jar-in-Jar: ship api + common inside the remapped mod (production server has only this one jar).
    include(implementation(project(":api"))!!)
    include(implementation(project(":common"))!!)

    // :common uses these at runtime but they are not inside common.jar — mirror common/build.gradle.kts.
    // include() does not nest transitive dependencies. Adventure 4.25.0 needs adventure-key,
    // examination-api, and examination-string, and the Gson serializer needs the JSON serializer,
    // the commons serializer, and option. Configurate needs geantyref and SnakeYAML.
    // These stay inside this mod's jar. Geyser 2.12.1 keeps Adventure 5.2.0 (no Examinable) in its
    // own mod classloader. Granite does not depend on Geyser and does not load that copy.
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
        "net.kyori:adventure-key:4.25.0",
        "net.kyori:examination-api:1.3.0",
        "net.kyori:examination-string:1.3.0",
        "net.kyori:adventure-text-serializer-legacy:4.25.0",
        "net.kyori:adventure-text-minimessage:4.25.0",
        "net.kyori:adventure-text-serializer-gson:4.25.0",
        "net.kyori:adventure-text-serializer-json:4.25.0",
        "net.kyori:adventure-text-serializer-commons:4.25.0",
        "net.kyori:option:1.1.0",
        "co.aikar:acf-core:0.5.1-SNAPSHOT",
        "com.ezylang:EvalEx:3.3.0",
        "org.spongepowered:configurate-core:4.1.2",
        "org.spongepowered:configurate-yaml:4.1.2",
        "io.leangen.geantyref:geantyref:1.3.11",
        "org.yaml:snakeyaml:2.3",
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
 * Post-jar audit: JiJ must include api/common, polyglot, and Configurate **4.1.2** only (no 4.2 — modpack clash).
 * Minecraft 26.3 is unobfuscated, so the production artifact is the `jar` task.
 * Runs automatically after `jar` (e.g. as part of `build`). Manual: `gradlew -PfabricOnly=true :fabric:verifyFabricReleaseBundle`.
 */
tasks.named<Jar>("jar") {
    finalizedBy("relocateFabricKyori")
}

val kyoriFrom = "net/kyori/"
val kyoriTo = "dev/granite/graniteskills/shaded/kyori/"
val kyoriDotFrom = "net.kyori."
val kyoriDotTo = "dev.granite.graniteskills.shaded.kyori."

fun shadeKyoriClass(bytes: ByteArray): ByteArray {
    if (bytes.size < 8 ||
        bytes[0] != 0xCA.toByte() ||
        bytes[1] != 0xFE.toByte() ||
        bytes[2] != 0xBA.toByte() ||
        bytes[3] != 0xBE.toByte()
    ) {
        return bytes
    }
    val reader = ClassReader(bytes)
    val writer = ClassWriter(0)
    val remapper = object : Remapper() {
        override fun map(internalName: String): String {
            return if (internalName.startsWith(kyoriFrom)) {
                kyoriTo + internalName.removePrefix(kyoriFrom)
            } else {
                internalName
            }
        }
    }
    reader.accept(ClassRemapper(writer, remapper), 0)
    return writer.toByteArray()
}

fun shadeKyoriJar(input: File, output: File) {
    ZipFile(input).use { zip ->
        ZipOutputStream(output.outputStream()).use { out ->
            val manifest = ZipEntry("META-INF/MANIFEST.MF")
            manifest.time = 0
            out.putNextEntry(manifest)
            out.write("Manifest-Version: 1.0\nMulti-Release: true\n".toByteArray())
            out.closeEntry()
            zip.entries().asSequence().forEach { entry ->
                if (entry.name == "META-INF/MANIFEST.MF" ||
                    entry.name.endsWith(".SF") ||
                    entry.name.endsWith(".RSA") ||
                    entry.name.endsWith(".DSA")
                ) {
                    return@forEach
                }
                var name = entry.name.replace("net/kyori/", kyoriTo)
                if (name.startsWith("META-INF/services/net.kyori.")) {
                    name = "META-INF/services/" + kyoriDotTo + name.removePrefix("META-INF/services/net.kyori.")
                }
                val next = ZipEntry(name)
                next.time = entry.time
                out.putNextEntry(next)
                if (!entry.isDirectory) {
                    val bytes = zip.getInputStream(entry).use { it.readBytes() }
                    val rewritten = when {
                        name.endsWith(".class") -> shadeKyoriClass(bytes)
                        name.startsWith("META-INF/services/") ->
                            bytes.toString(Charsets.UTF_8).replace(kyoriDotFrom, kyoriDotTo).toByteArray()
                        else -> bytes
                    }
                    out.write(rewritten)
                }
                out.closeEntry()
            }
        }
    }
}

fun jarMentionsKyori(file: File): Boolean {
    ZipFile(file).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            if (entry.isDirectory) {
                return@forEach
            }
            if (!entry.name.endsWith(".class") && !entry.name.startsWith("META-INF/services/")) {
                return@forEach
            }
            val text = zip.getInputStream(entry).use { it.readBytes() }.toString(Charsets.ISO_8859_1)
            if (text.contains("net/kyori") || text.contains("net.kyori")) {
                return true
            }
        }
    }
    return false
}

/**
 * Knot loads every mod's nested jars into one class space and keeps one copy of each mod id.
 * Geyser 2.12.1 publishes net_kyori_adventure-api 5.2.0, which has no Examinable, and Knot would
 * drop Granite's 4.25.0 jar of the same id. Shade Granite's net.kyori packages and fold them into
 * graniteskills_kyori so menu code keeps Adventure 4.25.0 beside Geyser's Adventure 5.2.0.
 */
tasks.register("relocateFabricKyori") {
    group = "build"
    description = "Shade net.kyori inside the release jar so it does not share Geyser's Adventure 5."
    dependsOn("jar")
    doLast {
        val jarFile = tasks.named<Jar>("jar").get().archiveFile.get().asFile
        fun relocate(input: File, output: File) {
            shadeKyoriJar(input, output)
        }
        val work = NioFiles.createTempDirectory("granite-kyori-shade")
        val rewrittenNested = linkedMapOf<String, File>()
        ZipFile(jarFile).use { zip ->
            zip.entries().asSequence()
                .filter { !it.isDirectory && it.name.startsWith("META-INF/jars/") && it.name.endsWith(".jar") }
                .forEach { entry ->
                    val extracted = work.resolve(entry.name.substringAfterLast("/")).toFile()
                    zip.getInputStream(entry).use { input ->
                        extracted.outputStream().use { out -> input.copyTo(out) }
                    }
                    if (jarMentionsKyori(extracted)) {
                        val relocated = File(extracted.parentFile, extracted.name + ".shaded")
                        relocate(extracted, relocated)
                        rewrittenNested[entry.name] = relocated
                    }
                }
        }
        val kyoriLibNames = rewrittenNested.keys.filter { name ->
            val base = name.substringAfterLast("/")
            base.startsWith("adventure-") || base.startsWith("examination-") || base.startsWith("option-")
        }.toSet()
        val mergedName = "META-INF/jars/graniteskills-kyori-4.25.0.jar"
        val merged = work.resolve("graniteskills-kyori-4.25.0.jar").toFile()
        val seen = mutableSetOf<String>()
        val services = linkedMapOf<String, ByteArray>()
        ZipOutputStream(merged.outputStream()).use { out ->
            val manifest = ZipEntry("META-INF/MANIFEST.MF")
            manifest.time = 0
            out.putNextEntry(manifest)
            out.write("Manifest-Version: 1.0\nMulti-Release: true\n".toByteArray())
            out.closeEntry()
            val modJson = ZipEntry("fabric.mod.json")
            modJson.time = 0
            out.putNextEntry(modJson)
            out.write(
                """{"schemaVersion":1,"id":"graniteskills_kyori","version":"4.25.0","name":"Granite Skills Kyori","custom":{"fabric-loom:generated":true}}"""
                    .toByteArray(),
            )
            out.closeEntry()
            kyoriLibNames.forEach { name ->
                ZipFile(rewrittenNested.getValue(name)).use { zip ->
                    zip.entries().asSequence().forEach { entry ->
                        if (entry.isDirectory ||
                            entry.name == "META-INF/MANIFEST.MF" ||
                            entry.name == "fabric.mod.json"
                        ) {
                            return@forEach
                        }
                        val bytes = zip.getInputStream(entry).use { it.readBytes() }
                        if (entry.name.startsWith("META-INF/services/")) {
                            val previous = services[entry.name]
                            services[entry.name] = if (previous == null) {
                                bytes
                            } else {
                                previous + byteArrayOf('\n'.code.toByte()) + bytes
                            }
                            return@forEach
                        }
                        if (!seen.add(entry.name)) {
                            return@forEach
                        }
                        val next = ZipEntry(entry.name)
                        next.time = entry.time
                        out.putNextEntry(next)
                        out.write(bytes)
                        out.closeEntry()
                    }
                }
            }
            services.forEach { (name, bytes) ->
                val next = ZipEntry(name)
                next.time = 0
                out.putNextEntry(next)
                out.write(bytes)
                out.closeEntry()
            }
        }
        val repacked = work.resolve(jarFile.name + ".repacked").toFile()
        ZipFile(jarFile).use { zip ->
            ZipOutputStream(repacked.outputStream()).use { out ->
                zip.entries().asSequence().forEach { entry ->
                    if (entry.name in kyoriLibNames) {
                        return@forEach
                    }
                    val next = ZipEntry(entry.name)
                    next.time = entry.time
                    out.putNextEntry(next)
                    if (!entry.isDirectory) {
                        val replacement = rewrittenNested[entry.name]
                        val bytes = if (replacement != null) {
                            replacement.readBytes()
                        } else {
                            zip.getInputStream(entry).use { it.readBytes() }
                        }
                        if (entry.name == "fabric.mod.json") {
                            var text = bytes.toString(Charsets.UTF_8)
                            kyoriLibNames.forEach { name ->
                                val base = name.substringAfterLast("/")
                                text = text.replace("{\"file\":\"META-INF/jars/$base\"},", "")
                                text = text.replace(",{\"file\":\"META-INF/jars/$base\"}", "")
                            }
                            text = text.replace(
                                "\"jars\":[",
                                "\"jars\":[{\"file\":\"$mergedName\"},",
                            )
                            out.write(text.toByteArray())
                        } else {
                            out.write(bytes)
                        }
                    }
                    out.closeEntry()
                }
                val mergedEntry = ZipEntry(mergedName)
                mergedEntry.time = 0
                out.putNextEntry(mergedEntry)
                merged.inputStream().use { it.copyTo(out) }
                out.closeEntry()
            }
        }
        val shaded = work.resolve(jarFile.name + ".shaded").toFile()
        relocate(repacked, shaded)
        shaded.copyTo(jarFile, overwrite = true)
        logger.lifecycle("relocateFabricKyori: shaded net.kyori in {}", jarFile.name)
    }
}

tasks.register("verifyFabricReleaseBundle") {
    group = "verification"
    description =
        "Audit META-INF/jars in the mod jar for required embedded libs and missing runtime classes."
    dependsOn("relocateFabricKyori", ":granitelib-fabric:jar")
    doLast {
        val jarFile = tasks.named<Jar>("jar").get().archiveFile.get().asFile
        val graniteLibJar = project(":granitelib-fabric").tasks.named<Jar>("jar").get().archiveFile.get().asFile
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
        val requiredNames = listOf(
            "configurate-core-4.1.2.jar",
            "configurate-yaml-4.1.2.jar",
            "graniteskills-kyori-4.25.0.jar",
            "geantyref-1.3.11.jar",
            "snakeyaml-2.3.jar",
        )
        val droppedKyoriIds = listOf(
            "adventure-api-4.25.0.jar",
            "adventure-key-4.25.0.jar",
            "examination-api-1.3.0.jar",
            "examination-string-1.3.0.jar",
            "adventure-text-minimessage-4.25.0.jar",
            "adventure-text-serializer-gson-4.25.0.jar",
            "adventure-text-serializer-json-4.25.0.jar",
            "adventure-text-serializer-commons-4.25.0.jar",
            "adventure-text-serializer-legacy-4.25.0.jar",
            "option-1.1.0.jar",
        )
        droppedKyoriIds.forEach { name ->
            require(nestedJars.none { it == name }) {
                "JiJ must fold $name into graniteskills-kyori-4.25.0.jar so Knot does not drop it as a duplicate of Geyser"
            }
        }
        requiredNames.forEach { name ->
            requireJar({ it == name }, "JiJ must include $name")
        }
        requireJar({ it.contains("polyglot") }, "JiJ must include polyglot")
        val forbidden = nestedJars.filter {
            Regex("configurate-(core|yaml)-4\\.2.*\\.jar").matches(it) ||
                Regex("adventure-.*-5\\..*\\.jar").matches(it)
        }
        require(forbidden.isEmpty()) {
            "JiJ must keep Adventure 4.25.0 and Configurate 4.1.2, not Geyser's Adventure 5 or Configurate 4.2: $forbidden"
        }
        val missing = missingRuntimeClasses(jarFile, graniteLibJar)
        val present = bundledClassNames(jarFile)
        require("dev/granite/graniteskills/shaded/kyori/examination/Examinable" in present) {
            "Release jar must shade Examinable into dev.granite.graniteskills.shaded.kyori"
        }
        require("net/kyori/examination/Examinable" !in present && "net/kyori/adventure/text/Component" !in present) {
            "Release jar must not publish unshaded net.kyori classes; Geyser owns that package"
        }
        require(missing.isEmpty()) {
            "Packaged jar references runtime classes it does not contain:\n" +
                missing.joinToString("\n") { (ref, from) -> "  $ref <- $from" }
        }
        logger.lifecycle(
            "verifyFabricReleaseBundle: OK ({} nested jars in {}, runtime class audit passed)",
            nestedJars.size,
            jarFile.name,
        )
    }
}

/**
 * Classes the dedicated server already provides, plus annotation-only types and optional
 * integrations that are loaded only when that plugin is installed. Adventure and Configurate
 * are not in this list: those must be inside the release jar.
 */
val platformClassPrefixes = listOf(
    "java/", "javax/", "jdk/", "sun/", "com/sun/",
    "net/minecraft/", "com/mojang/",
    "org/slf4j/", "org/apache/logging/", "org/apache/commons/",
    "com/google/gson/", "com/google/common/", "com/google/auto/", "com/google/errorprone/",
    "io/netty/", "it/unimi/dsi/fastutil/", "org/joml/",
    "net/fabricmc/", "org/objectweb/asm/", "org/spongepowered/asm/",
    "org/jetbrains/", "org/checkerframework/", "org/jspecify/",
    "net/luckperms/",
)

val optionalIntegrationClasses = listOf(
    "com/zaxxer/hikari/metrics/",
    "com/zaxxer/hikari/hibernate/",
    "com/zaxxer/hikari/util/JavassistProxyFactory",
    "dev/granite/graniteskills/common/hooks/LuckPermsHook",
)

fun missingRuntimeClasses(jarFile: File, companionJar: File): List<Pair<String, String>> {
    val available = HashSet<String>()
    val missing = mutableListOf<Pair<String, String>>()
    val archives = mutableListOf<Pair<String, ByteArray>>()
    fun readBundle(file: File, scanOwners: Boolean) {
        ZipFile(file).use { zip ->
            collectClasses(zip, "", available, if (scanOwners) archives else mutableListOf())
            zip.entries().asSequence()
                .filter { !it.isDirectory && it.name.startsWith("META-INF/jars/") && it.name.endsWith(".jar") }
                .forEach { entry ->
                    val tmp = File.createTempFile("jij-", ".jar")
                    tmp.deleteOnExit()
                    zip.getInputStream(entry).use { input ->
                        tmp.outputStream().use { out -> input.copyTo(out) }
                    }
                    ZipFile(tmp).use { nested ->
                        collectClasses(
                            nested,
                            entry.name.substringAfterLast("/") + "!",
                            available,
                            if (scanOwners) archives else mutableListOf(),
                        )
                    }
                }
        }
    }
    readBundle(jarFile, scanOwners = true)
    // GraniteLib is a required separate mod. Its classes satisfy references, and they are not
    // nested into this jar.
    readBundle(companionJar, scanOwners = false)
    for ((owner, bytes) in archives) {
        val ownerClass = owner.substringAfter('!').removeSuffix(".class")
        if (optionalIntegrationClasses.any { ownerClass.startsWith(it) }) {
            continue
        }
        if (!ownerClass.startsWith("dev/granite/") &&
            !ownerClass.startsWith("net/kyori/") &&
            !ownerClass.startsWith("org/spongepowered/configurate/") &&
            !ownerClass.startsWith("dev/aurelium/")
        ) {
            continue
        }
        for (ref in constantClassNames(bytes)) {
            if (ref == ownerClass || ref in available) {
                continue
            }
            if (platformClassPrefixes.any { ref.startsWith(it) }) {
                continue
            }
            missing.add(ref to ownerClass)
        }
    }
    return missing.distinct()
}

fun bundledClassNames(jarFile: File): Set<String> {
    val names = HashSet<String>()
    fun read(file: File) {
        ZipFile(file).use { zip ->
            zip.entries().asSequence()
                .filter { !it.isDirectory && it.name.endsWith(".class") && !it.name.startsWith("META-INF/") }
                .forEach { names.add(it.name.removeSuffix(".class")) }
            zip.entries().asSequence()
                .filter { !it.isDirectory && it.name.startsWith("META-INF/jars/") && it.name.endsWith(".jar") }
                .forEach { entry ->
                    val tmp = File.createTempFile("jij-", ".jar")
                    tmp.deleteOnExit()
                    zip.getInputStream(entry).use { input ->
                        tmp.outputStream().use { out -> input.copyTo(out) }
                    }
                    read(tmp)
                }
        }
    }
    read(jarFile)
    return names
}

fun collectClasses(
    zip: ZipFile,
    prefix: String,
    available: MutableSet<String>,
    archives: MutableList<Pair<String, ByteArray>>,
) {
    zip.entries().asSequence().filter { !it.isDirectory && it.name.endsWith(".class") && !it.name.startsWith("META-INF/") }
        .forEach { entry ->
            val bytes = zip.getInputStream(entry).use { it.readBytes() }
            available.add(entry.name.removeSuffix(".class"))
            archives.add("$prefix${entry.name}" to bytes)
        }
}

fun constantClassNames(bytes: ByteArray): Set<String> {
    val buf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
    if (buf.remaining() < 10 || buf.int != 0xCAFEBABE.toInt()) {
        return emptySet()
    }
    buf.short
    buf.short
    val count = buf.short.toInt() and 0xffff
    val utf = arrayOfNulls<String>(count)
    val classNameIndex = IntArray(count)
    var index = 1
    while (index < count) {
        val tag = buf.get().toInt() and 0xff
        when (tag) {
            1 -> {
                val len = buf.short.toInt() and 0xffff
                val raw = ByteArray(len)
                buf.get(raw)
                utf[index] = String(raw, Charsets.UTF_8)
            }
            7 -> classNameIndex[index] = buf.short.toInt() and 0xffff
            8, 16, 19, 20 -> buf.position(buf.position() + 2)
            15 -> buf.position(buf.position() + 3)
            3, 4, 9, 10, 11, 12, 17, 18 -> buf.position(buf.position() + 4)
            5, 6 -> {
                buf.position(buf.position() + 8)
                index++
            }
            else -> error("Unknown class-file tag $tag while auditing the release jar")
        }
        index++
    }
    val names = HashSet<String>()
    for (nameIndex in classNameIndex) {
        if (nameIndex == 0) {
            continue
        }
        val name = utf[nameIndex] ?: continue
        if (!name.startsWith("[")) {
            names.add(name)
        }
    }
    return names
}

/** Preserves every successful remapped JAR under {@code fabric-mod-jar-history/} (survives {@code :fabric:clean}). */
tasks.register("archiveFabricModJar") {
    group = "build"
    description =
        "Copy the GraniteSkills jar to fabric-mod-jar-history/ with a timestamp (keeps all iterations)."
    dependsOn("relocateFabricKyori")
    doLast {
        val src = tasks.named<Jar>("jar").get().archiveFile.get().asFile
        val destDir = rootProject.layout.projectDirectory.dir("fabric-mod-jar-history").asFile
        destDir.mkdirs()
        val ts = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")
            .withZone(ZoneId.systemDefault())
            .format(Instant.now())
        val dest = destDir.resolve("GraniteSkills-${project.version}-$ts.jar")
        src.copyTo(dest, overwrite = false)

    }
}

/**
 * Gradle [clean] wipes [fabric/build] including [fabric/build/libs]. Copy any existing
 * GraniteSkills jars out first so every iteration stays under [fabric-mod-jar-history/]
 * for documentation / rollback (same intent as [archiveFabricModJar] after remap).
 */
tasks.named("clean").configure {
    doFirst("archiveFabricJarsBeforeClean") {
        val libsDir = layout.buildDirectory.get().asFile.resolve("libs")
        if (!libsDir.isDirectory) {
            return@doFirst
        }
        val jars = libsDir.listFiles()
            ?.filter { it.isFile && it.name.startsWith("GraniteSkills-") && it.name.endsWith(".jar") }
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
            "clean: archived {} GraniteSkills jar(s) from build/libs to fabric-mod-jar-history (*-preclean-{}-*.jar) before deleting build/",
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
    options.release.set(25)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
