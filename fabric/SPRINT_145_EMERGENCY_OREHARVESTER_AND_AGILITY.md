# Sprint 145 — Emergency: OreHarvester safety + Agility CUSTOM stats

## What interfered with OreHarvester

The Fabric **multi-break / vein XP** compatibility layer registered **`PlayerBlockBreakEvents.BEFORE`** handlers that ran on **every** block break (snapshot same-source scan + draft storage), plus **`AFTER`** queue jobs and **`END_WORLD_TICK`** compensation. Even when returning `true` / not cancelling breaks, that extra synchronous work and Sprint **144** baseline widening (**radius + 2**) changed timing and server load around vein mining tools such as **OreHarvester**.

## Safety rollback / disable behavior

1. **Embedded default:** `mod_compat.multi_break_xp.enabled: **false**` in bundled **`config.yml`**.
2. **No hooks when disabled:** **`FabricModCompatMultiBreakXp.register`** returns immediately if disabled — **no** BEFORE/AFTER/world-tick registrations for multi-break XP.
3. **Baseline rollback:** Volume baseline again uses the **same Chebyshev radius** as the snapshot job (**no `+2`** expansion).
4. **Normal mining XP:** Unchanged — still handled by **`FabricBlockBreakHandler`** / primary break awards only.

Operators who accept the trade-offs may set **`enabled: true`** and restart the server.

## Agility stat resolver — root cause

Bare **`minecraft:walk_one_cm`**-style resolution via **`Identifier.of`** + **`Stats.CUSTOM.getOrCreateStat`** alone could still yield **`invalid_stat_identifier`** at startup because CUSTOM stats align with vanilla **`Stats`** identifier constants; dynamic ids could miss registry alignment during early initialization.

## Agility stat resolver — fix

**`vanillaDistanceMovementStat`** resolves **`walk_one_cm`**, **`sprint_one_cm`**, **`swim_one_cm`** using **`Stats.WALK_ONE_CM`**, **`Stats.SPRINT_ONE_CM`**, **`Stats.SWIM_ONE_CM`** before the generic CUSTOM path. AuraSkills source ids (**`walk_per_meter`**, …) still map through **`resolveForAgility`** → **`walk_one_cm`** strings unchanged.

## Jump

**`jump_per_100`** remains a **jumping** source (not `StatisticXpSource`); **`FabricJumpingXpHandler`** unchanged.

## Tests

- **`FabricS145EmergencyOreHarvesterAndAgilityTest`**: embedded config default, bundled **`agility.yml`** presence, alias keys.

## Gradle

`.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle`