# JUnit cannot bootstrap Minecraft registries in this project

**Status:** dead end — do not re-attempt without a Loader/Loom upgrade.

## Problem

Agility statistic resolution (`walk_one_cm`, `sprint_one_cm`, `swim_one_cm` →
`Stats.CUSTOM.getOrCreateStat`) was fixed three times across Sprints 143, 144 and 145, and each fix
could only be judged by reading `resolved=true reason=ok` out of a live dedicated-server log. The
obvious improvement is a unit test that touches the real stat registry, so a regression fails the
build instead of reaching an operator.

## Root cause

Two layers block it, in order:

1. **`Bootstrap.initialize()` throws `IllegalAccessError`.** Yarn's named mappings put
   `net.minecraft.registry.SimpleRegistry` and `net.minecraft.registry.entry.RegistryEntry$Reference`
   in *different* packages, so `SimpleRegistry.add` calling `Reference.setRegistryKey` is a
   cross-package access to a non-public method. It works in production only because intermediary
   mappings flatten both into `net.minecraft`. In dev it needs Fabric API's
   `fabric-transitive-access-wideners-v1`, which is only applied when the game runs under Knot.

2. **`fabric-loader-junit` 0.18.4 cannot load those wideners here.** Adding
   `testImplementation("net.fabricmc:fabric-loader-junit:0.18.4")` does boot Knot and discover all
   mods, but mod discovery reads class tweakers *before* runtime remapping is applied, so every
   Fabric API module fails with:

   ```
   ClassTweakerFormatException: line 1: Namespace (intermediary) does not match current runtime namespace (named)
   ```

   Wiring Loom's `generateRemapClasspath` output into the test task via
   `-Dfabric.remapClasspathFile` clears the "Runtime mod remapping disabled" warning and gets past
   the first failing module, but the class-tweaker namespace check still runs too early — it just
   fails on the next module instead. The failure surfaces confusingly as
   `ServiceConfigurationError: ... FabricLoaderLauncherSessionListener could not be instantiated`,
   with zero tests discovered and an empty test-results directory.

Environment: Loom 1.13.6, Fabric Loader 0.18.4, Fabric API 0.141.3+1.21.11, MC 1.21.11, Yarn
1.21.11+build.4.

## What to do instead

Split the problem: test everything *downstream* of registry access as pure logic, and let the mod
prove resolution itself at startup.

- `FabricStatisticXpHandler.computeProgress` is pure arithmetic — assert the actual XP amounts and
  the remainder carry-over there (`FabricAgilityStatisticPollingTest`).
- Pin the YAML thresholds and multipliers that those amounts derive from, so the manual spot-check
  numbers cannot silently go stale.
- `FabricAgilityResolutionProof` already logs an explicit `FAIL: agility StatisticXpSource rows
  exist (...) but zero resolved` line at startup, so runtime verification is one grep, not a hunt.
- For ordering contracts that need a server (for example the end-of-tick dedupe between
  `FabricBlockBreakHandler` and `FabricExternalBlockBreakXp`), pin the invariants structurally —
  see `FabricBlockBreakXpDedupeInvariantsTest`.

## How to verify the dead end still exists

Add the `fabric-loader-junit` dependency and a test calling `SharedConstants.createGameVersion()`
then `Bootstrap.initialize()`. If it still fails, the test-results XML will contain
`ClassTweakerFormatException`. Re-check after a Fabric Loader upgrade — this is a loader-side
ordering bug, so a newer loader may fix it outright.
