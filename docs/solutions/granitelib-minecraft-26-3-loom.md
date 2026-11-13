# GraniteLib on Minecraft 26.3 needs the no-remap Loom plugin

**Status:** current — verified with `:granitelib-fabric:build` and 35 unit tests on Loom 1.19.4.

## Problem

Pointing `:granitelib-fabric` at Minecraft 26.3 with the usual `id("fabric-loom")` plugin fails
before compilation, whether or not `mappings` is declared:

- no mappings line → `Configuration 'mappings' has no dependencies`
- `mappings(loom.officialMojangMappings())` → `Failed to find official mojang mappings for 26.3`

Mojang stopped shipping `client_mappings` when the game became unobfuscated in 26.1. The short
plugin id still applies `LoomGradlePlugin`, which always remaps.

## Fix

Use the no-remap plugin id, and do not declare mappings:

```kotlin
plugins {
    id("net.fabricmc.fabric-loom") version "1.19.4"
}
```

That id applies `LoomNoRemapGradlePlugin`. Keep `id("fabric-loom")` only on modules that still
target an obfuscated version (the gameplay mod is still on 1.21.11 Yarn until its own port).

26.3 also renamed the container click enum from `ClickType` to
`net.minecraft.world.inventory.ContainerInput`. `ChestMenu.clicked` takes that type. The rest of
the 6×9 menu surface (`MenuType.GENERIC_9x6`, `SimpleMenuProvider`, `ServerPlayer.openMenu`) kept
the Mojang names from 1.21.

Toolchain that actually configures: Gradle 9.8.2, Java 25, Fabric Loader 0.20.1, Fabric API
`0.166.2+26.3`.

## How to verify

```
JAVA_HOME=<jdk-25> ./gradlew -PfabricOnly=true :granitelib-fabric:build
```

The jar's `fabric.mod.json` must depend on `minecraft ~26.3` and `java >=25`. Granite Skills
sources that still call `GraniteContainerMenus` with Yarn types (`ServerPlayerEntity`, `Text`,
`SimpleInventory`, `SlotActionType`) will not compile until that module is on 26.3 as well.
