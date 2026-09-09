# Sprint 144 — Agility statistic resolution + OreHarvester multi-break

## Agility — root cause

Bare custom stat tokens such as `walk_one_cm` were passed through paths that could still yield a null `Identifier` inside `Stats.CUSTOM.getOrCreateStat` (vanilla NPE on `Identifier.toString()`). Agility **source ids** (`walk_per_meter`, …) were not consulted when resolving YAML `statistic:` rows, so mismatched or empty YAML could not be repaired from the source key alone.

## Agility — fix

- **`FabricStatisticSourceResolver`**
  - **`agilityVanillaPathForSourceKey`**: `walk_per_meter` → `walk_one_cm`, `sprint_per_meter` → `sprint_one_cm`, `swim_per_meter` → `swim_one_cm`.
  - **`resolveForAgility` / `resolveDetailedForAgility`**: prefer that mapping, then YAML.
  - **`minecraftCustomStatIdentifier`**: builds `minecraft` ids with **`Identifier.of("minecraft", path)`** when `tryParse` does not apply; **`getOrCreateStat`** wrapped in try/catch.
- **`FabricStatisticXpHandler`**: agility rows use **`resolveDetailedForAgility`**; non-agility statistic rows still use **`resolveDetailed`**.
- **`FabricAgilityResolutionProof`**: resolved filter uses **`resolveForAgility`**.
- **XPDIAG**: **`agility_stat_delta`** uses **`source_id`** = source **simple name**; **`converted_units`** = stat delta ÷ 100 for agility (cm → whole meters).

## OreHarvester / vein mining — root cause

Extra breaks sometimes occurred **outside** the same-source snapshot ring or **after** timing skew; a too-narrow baseline volume and short window could yield **`removed_count=0`** while blocks were actually gone.

## OreHarvester — fix

- **Volume baseline radius** = **`min(RADIUS_MAX, configured_radius + 2)`** so the comparison volume matches a slightly larger Chebyshev ball than the snapshot-only pass.
- Embedded **`mod_compat.multi_break_xp.window_ticks`** default increased from **50** to **60** (operators can tune).

## Tests

- **`FabricS144AgilityStatisticAliasTest`**: alias map and **`effectiveAgilityStatisticToken`**.

## Manual validation

- Startup / **`[XPDIAG] agility_sources_resolved`**: `statistic_sources` lists `walk_per_meter,sprint_per_meter,swim_per_meter` (order may vary); `jump_sources` includes `jump_per_100` when jumping is enabled.
- Walk/sprint/swim in-game: **`agility_stat_delta`** shows positive **`delta`**, **`award_called=true`** after threshold, **`stat_id=minecraft:…_one_cm`**.
- **`xpdiag`**: **`multi_break_lifecycle`** shows non-zero **`removed_count`** / **`xp_awarded_blocks`** for vein-style breaks when multi-break XP is enabled.
- Confirm farming, fishing, `/sources`, ability lore, leaderboard unchanged (no edits in those areas).

## Gradle

`.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle`