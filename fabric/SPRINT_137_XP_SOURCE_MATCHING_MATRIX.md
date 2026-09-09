# Sprint 137: XP source matching matrix and diagnostics

Version: **2.3.165** (from **2.3.164**).

## Goal

Stop one-off wheat/cod patches. Ship a repeatable **truth table**: runtime context, normalized lookup keys,
configured candidate sources, gate result, **`addXp` mutation proof**, plus category-level tooling so failures are obvious in one log line.

## Root cause themes (prior sprints distilled)

| Category | Symptom pattern | Sprint 137 mitigation |
|---------|----------------|------------------------|
| User map misses | Silent no-op on `addXp` | **`FabricXpUserResolution.resolve`** everywhere critical paths migrated |
| Opaque gate pass | **`reason=checks_passed`** not actionable | Rename to **`shared_gate_pass`**; resolution lines stay separate |
| No source match | No candidate list | **`block_break_resolution`**, **`damage_resolution`** with **`candidate_source_ids`** |
| Multi-break skew | Compensation unclear | **`dedupe_skipped_count`**, **`placed_block_skipped_count`** on **`multi_break_window`** |

## Implemented components

| Component | Purpose |
|-----------|---------|
| `FabricXpLookupNormalize` | Normalize block/item/damage keys; enumerate BREAK/DAMAGE **`SkillSource`** ids for diagnostics |
| `FabricXpUserResolution` | Reliable **`User`** for awards on Fabric (`FabricUserManager`) |
| `FabricXpDiagnostics.logSourceResolution` | Single-line no-match matrix row |
| `FabricXpDiagnostics.logAddXpMutation` | Before/after XP + level proves mutation |
| `FabricLevelManager#addXp` | Hook mutation log when **`xpdiag` on** |
| `FabricBlockBreakHandler` | **`block_break_resolution`** when BREAK lookup fails |
| `FabricCombatXpHandler` | **`damage_resolution`** when DAMAGE lookup fails; user resolution |
| `FabricStatisticXpHandler` | User resolution outer loop |
| `FabricJumpingXpHandler`, `FabricEntityKillHandler` | User resolution |
| **`FabricModCompatMultiBreakXp` job** | Count dedupe vs placed skips; extended **`logMultiBreakWindow`** |

## Skills / source types (audit surface)

Excluded **Forging** per spec. Covered listener types registered from **`FabricLevelManager.registerFabricXpHandlers`** and related registrations:

**farming, foraging, mining, excavation** — `BLOCK` BREAK / interact (`FabricBlockBreakHandler`, `FabricBlockInteractHandler`, crop harvest XP, **`FabricModCompatMultiBreakXp` for mining/foraging multi-break`).
**fishing** — Fishing bobber XP + **`FabricFishingLootHandler`** (**`SourceTypes.FISHING`**).
**fighting, defense, archery_ENTITY_DAMAGE/DAMAGE_trigger** — **`FabricCombatXpHandler`**, **`FabricEntityKillHandler`**, **`FabricMobLootHandler`**.
**agility / movement** — **`STATISTIC`** Poller (**`FabricStatisticXpHandler`**), **`JUMPING`** (**`FabricJumpingXpHandler`**), **`DAMAGE`** fall/damage (**`FabricCombatXpHandler`**).

**alchemy / enchanting / anvil / grindstone** — dedicated handlers (`FabricBrewingXpHandler`, `FabricEnchantingXpHandler`, **`FabricAnvilXpHandler`, `FabricGrindstoneXpHandler`).

Detailed per-source YAML rows remain authoritative; runtime hooks are enumerated in-repo in **`FABRIC_PORT_HANDOFF.md`** and **`FabricLevelManager`**.

## Helper tests

**`FabricS137XpMatrixNormalizeTest`** exercises key/join normalization (block/damage enums). Category integration (Rsponge fishing, agility stat strings, Enchanting mixin timing) stays **manual** or future gametest unless noted.

## Manual validation matrix (`/auraskills xpdiag on`)

For each scenario expect either **`event=block_break_resolution`/`damage_resolution`/`...`** with **`source_match=false`** and populated **`candidate_source_ids`**, or successful path with **`addxp_mutation`** showing **`after_skill_xp`** > **`before_skill_xp`** when XP event is not cancelled and amount > 0.

| Scenario | Signals |
|---------|---------|
| Farming break/interact vs configured crop | **`block_break_resolution`** vs **`block_break`** award + mutation |
| Mining standing + crouch multi-break | **`multi_break_window`** with skip counts + mutation lines per award |
| Fishing | Sprint 136 **`fishing_entry`** lines + mutation on award |
| Agility (jump / stats / fall damage) | **`damage_resolution`** or statistic tick awards + mutation |
| Combat kill / defense hit | entity / damage handlers + mutation |
| Brew / enchant / anvil | existing handler-specific logs + mutation |

## Known gaps

- Not every handler yet emits **`logSourceResolution`** (only block BREAK no-match and player damage no-match in this pass).
- **`save_called` / `feedback_called`** in **`addxp_mutation`** are placeholders (`-`) until optional deeper **`LevelManager`** hooks.
- Full matrix automation (one log schema for brew/enchant) is incremental.

## Scope

**/sources** rendering, ability lore, leaderboard, GraniteLib architecture, menu layout: **unchanged** for this sprint.