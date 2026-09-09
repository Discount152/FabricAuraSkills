# Sprint 138: Remaining XP category fixes after S137 matrix

Version: **2.3.166** (from **2.3.165**).

## Failed categories from manual test

- Farming age/state crops: wheat, carrots, potatoes.
- Fishing vanilla item/loot matching.
- Vein mining multi-break award path.
- Agility non-fall sources.

## Root cause and fixes by category

### Farming crops

Root cause: alias comparison accepted only raw path tokens; namespaced config tokens (`minecraft:wheat`, `minecraft:potato`) and seed/item aliases could miss depending on server config edits.

Fix: `FabricFarmingCropBreakAliases` now normalizes namespace prefixes before alias-group matching.

### Fishing

Root cause: return-phase snapshot could hit `no_post_bonus_stack` after bonus/vanilla stack movement and abort award path despite valid pre-bonus catch.

Fix: `FabricFishingXpHandler` now falls back to pre-bonus catch when post-bonus stack is transiently empty, then continues source lookup + award. Added `event=fishing_resolution` no-match diagnostics with candidate ids/count.

Also fixed item-filter matching to accept namespaced material tokens via `FabricLootItemMatcher.materialTokenMatchesPath`.

### Vein mining

S137 skip counters retained (`dedupe_skipped_count`, `placed_block_skipped_count`). This sprint preserves that path and improves user-resolution consistency in remaining XP handlers to avoid false no-awards from null user map misses.

### Agility non-fall

Root cause: statistic source strings with namespace could fail parse as custom stats.

Fix: `FabricStatisticSourceResolver` now accepts namespaced custom stat ids; unresolved stats emit `unsupported_or_unresolved_statistic` diagnostics from `FabricStatisticXpHandler`.

## Diagnostics additions

- `event=fishing_resolution` (`FabricXpDiagnostics.logSourceResolution`) when fishing source lookup fails.
- `event=statistic_resolution` unresolved-stat rows with candidate source list.
- Existing S137 `addxp_mutation`, `block_break_resolution`, `damage_resolution`, and multi-break skip counters remain.

## Tests

Added `FabricS138RemainingXpCategoryFixesTest`:
- crop alias/namespace matches for wheat/carrot/potato,
- sugar cane regression guard,
- namespaced fishing material token matching.

Runtime gaps: full in-world maturity and crouch-veinmine behavior still validated manually with `/auraskills xpdiag on`.

## Manual validation matrix

Run `/auraskills xpdiag on` and test:
- mature wheat/carrots/potatoes,
- sugar cane,
- vanilla fish and unknown/modded loot,
- crouch veinmine ore cluster,
- fall damage + configured non-fall agility sources,
- standing ore mining, excavation sand, mob kill, mob damage.

Expected valid awards: `source_match=true`, `award_called=true`, `state_mutated=true`, `after_skill_xp > before_skill_xp`.
Expected failures: explicit reason + candidate source ids shown.

## Scope confirmation

`/sources`, ability lore, leaderboard, GraniteLib, menu layout unchanged in this sprint.