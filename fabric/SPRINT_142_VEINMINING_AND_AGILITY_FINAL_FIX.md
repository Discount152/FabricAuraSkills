# Sprint 142 - Vein mining (OreHarvester) + Agility non-fall sources (Fabric)

**Ship:** FabricAuraSkills **2.3.170**

## Vein mining

**Root causes:** (1) Multi-break jobs ended at tick+**20**, too short for delayed OreHarvester removals. (2) `BlockState.equals` treated redstone `lit` toggles versus snapshot as mined state drift.

**Fixes:** Configurable **`mod_compat.multi_break_xp.window_ticks`** (default **100**, `MOD_COMPAT_MULTI_BREAK_WINDOW_TICKS`). **`isOreLitFlickerVersusSnapshot`** ignores lit-only divergence on same block id. **`job_queued`** logs snapshot center ore, not AIR after primary break.

## Agility statistic sources

**Root cause:** `SOURCE_STATISTIC_GAIN_PERIOD_TICKS` was applied through wall-clock **`timerSync(ticks * 50ms)`** instead of aligning to **server tick** cadence named by the option.

**Fix:** `FabricStatisticXpHandler` polls on **`ServerTickEvents.END_SERVER_TICK`** when **`server.getTicks() % period == 0`**.

**Diagnostics / startup:** `[AuraSkills] Agility statistic polling registered`, tracked walk/sprint/swim ids, `xpdiag` **`agility_stat_delta`** adds **`stat_id`**, **`converted_units`**.

## Agility jumps

**Root causes:** Bit-exact **`Double.compare`** on jump velocity missed real impulses; multiple `JumpingXpSource` defs made the award source ambiguous.

**Fix:** Velocity epsilon (**`1.5e-4`**), **`resolveAgilityJumpSource`** prefers **`jump_per_100`** on agility. **`agility_jump_delta`** lines under `xpdiag`. Startup: **`Agility jump handler registered`**.

## JVM tests

**`FabricS142VeinMiningAndAgilityTest`** covers statistic threshold accumulation and multi-break reason classification (no Minecraft `Bootstrap`; redstone cases: manual checklist).

## Build (PASS)

```
.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle
```

Gradle **BUILD SUCCESSFUL**; **verifyFabricReleaseBundle: OK** (FabricAuraSkills-2.3.170.jar).

## Scope

Farming / Fishing logic untouched; `/sources`, ability lore, leaderboard, GraniteLib, menu layout untouched; LevelManager award body unchanged aside from existing diagnostics.