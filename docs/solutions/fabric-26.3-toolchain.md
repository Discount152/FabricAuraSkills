# Fabric 26.3 toolchain and mixin targets

## Problem

Minecraft 26.1+ ships unobfuscated. Yarn mappings are gone, and Loom 1.20 still asks for a mappings dependency unless obfuscation is turned off. Several 1.21.11 mixin targets also changed shape, so a jar that compiles can still crash while mixins apply.

## What works

- Pin Fabric to Minecraft `26.3`, Loader `0.20.1`, Fabric API `0.166.2+26.3`, Loom `1.20.0-alpha.31`, Gradle `9.9.1`, and Java 25 for the fabric module. Keep `api` and `common` on Java 21 / release 8.
- Set `fabric.loom.disableObfuscation=true`. Depend on `project(":slate-fabric")` directly. Unobfuscated Loom does not publish `namedElements`.
- The production artifact is the `jar` task. `remapJar` is not part of this Loom setup.
- Migrate Yarn names to Mojang names on the last Yarn Minecraft version, then bump the game version. Do not run `migrateMappings` against 26.3.

## Mixin signatures that 26.3 changed

- `ServerPlayer.teleportTo(double, double, double)` is a different method from `teleportTo(ServerLevel, double, double, double, Set, float, float, boolean)`. Name the full descriptor.
- `ServerPlayer.teleport(TeleportTransition)` returns `ServerPlayer`.
- `LivingEntity.swing(InteractionHand)` is gone. Player attack swings go through `swingAndResetAttackStrength(InteractionHand, SwingAnimation, boolean)`.
- `BrewingStandBlockEntity.doBrew` is `private static void doBrew(ServerLevel, BlockPos, BrewingStandBlockEntity)`. Read slots with the block entity's `getItems()`.
- `AbstractThrownPotion` lives in `net.minecraft.world.entity.projectile.throwableitemprojectile`.

## How to verify

`JAVA_HOME` must be JDK 25. `./gradlew -PfabricOnly=true :fabric:build` must pass `verifyFabricReleaseBundle`. `./gradlew -PfabricOnly=true :fabric:runServer` must log `Granite Skills enabled` and `Done` with no `InvalidInjectionException`. A missing `server.properties` on the first Loom launch is Minecraft creating defaults, not a mixin failure.
