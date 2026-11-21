plugins {
    java
    id("fabric-loom") version "1.20.0-alpha.31"
}

group = "dev.granitelib.consumer"
version = "0.0.0"
description = "Throws away after the test. Compiles against GraniteLib only."

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    minecraft("com.mojang:minecraft:26.3")
    implementation("net.fabricmc:fabric-loader:0.20.1")
    val graniteLibVersion = rootProject.property("granitelibVersion") as String
    val releasedJar = rootProject.layout.projectDirectory.file(
        "granitelib-fabric/build/libs/granitelib-fabric-$graniteLibVersion.jar"
    )
    implementation(files(releasedJar))
    implementation(fileTree(layout.buildDirectory.dir("released-granitelib/META-INF/jars")) {
        include("*.jar")
    })

    testImplementation(platform("org.junit:junit-bom:5.13.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

val extractReleasedGraniteLibRuntime = tasks.register<Copy>("extractReleasedGraniteLibRuntime") {
    val graniteLibVersion = rootProject.property("granitelibVersion") as String
    from(zipTree(rootProject.layout.projectDirectory.file(
        "granitelib-fabric/build/libs/granitelib-fabric-$graniteLibVersion.jar"
    )))
    include("META-INF/jars/*.jar")
    into(layout.buildDirectory.dir("released-granitelib"))
}

tasks.named("compileJava") {
    dependsOn(extractReleasedGraniteLibRuntime)
}

tasks.named("compileTestJava") {
    dependsOn(extractReleasedGraniteLibRuntime)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    dependsOn(extractReleasedGraniteLibRuntime)
}
