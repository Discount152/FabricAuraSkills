pluginManagement {

    repositories {

        maven("https://maven.fabricmc.net/")

        gradlePluginPortal()

    }

}



rootProject.name = "FabricAuraSkills"



/**

 * When true, only `api`, `common`, `slate-fabric`, and `fabric` are included. Use for Fabric-only dev builds:

 * `gradlew -PfabricOnly=true :fabric:compileJava`

 * Skips Bukkit subprojects (Shadow, run-task, etc.) so a broken default Gradle cache does not block Fabric.

 */

val fabricOnly: Boolean =

    startParameter.projectProperties["fabricOnly"]?.toString()?.equals("true", ignoreCase = true) == true



if (fabricOnly) {

    include("api")

    include("common")

    include("slate-fabric")

    include("fabric")

} else {

    include("api")

    include("bukkit")

    include("common")

    include("api-bukkit")

    include("slate-fabric")

    include("fabric")

}



plugins {

    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")

}

