# FabricAuraSkills — Phase 1 parity audit (repo-anchored)

> **Runtime / product:** The **shipped** server mod runs on **Java Fabric** only; it must support **Java players** and **Bedrock players through Geyser + Floodgate** (see [PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md) — *Client / player access*). This ledger does **not** imply Paper or Bukkit at runtime. It maps **Granite Skills** (Fabric) against the **reference `bukkit` module** — **capability inventory**, not a second platform. **AuraSkills**-named commands/menus are **scope references**, not final **Granite** surface.

**Purpose:** Expand the seeded **`fabricauraskills_parity_audit_sheet.xlsx`** into a **repo-anchored** planning spine using this file as the **canonical Pass 1** list (bootstrap + commands + menus + listener map). The **workbook** is the editable **dashboard** (charts, filters); it must not become a second source of truth — **if a row disagrees, this markdown in-repo wins** until reconciled.

**Reference anchor files (Bukkit module — audit source, not runtime):** `bukkit/.../AuraSkills.java`, `CommandRegistrar.java`, `MenuRegistrar.java` (plus current Fabric mappings in `fabric/...`).

**Status values** (use in `Audit Matrix` → Status): `Missing` | `Stub` | `Partial` | `Equivalent` | `Needs Regression Proof` — prefer **Partial** or **Needs Regression Proof** over **Stub** when Fabric already implements real logic.

**Blocker tags** (use in Blocker / dependency column): `Slate-Fabric` | `ACF/Brigadier` | `Out of scope` | `Geyser/Bedrock` | `Common seam` | `None` — use **`Out of scope`** for **third-party Bukkit/Paper plugin ecosystems** that are not part of the default Granite Skills / GraniteLib product; **`Common seam`** for **in-repo** shared API (e.g. `api-bukkit` reference) without implying an external plugin roadmap.

### Row family interpretation

| Family | Parity focus | Granite / final UX | Java vs Bedrock (Geyser / Floodgate) |
|--------|----------------|-------------------|--------------------------------------|
| `AS-*` | Hard lifecycle and architecture parity; mostly branding-neutral. | Rebrand impact usually **None** except user-visible bootstrap messages. | Check any **server-visible** message or UI default that Bedrock may render differently. |
| `CMD-*` | **Capability inventory** — reference vs Fabric; map ACF classes. | **Not** the final public namespace. | **High risk:** command feedback, suggestions, permissions; validate **both** client paths. |
| `MENU-*` | Flow + Slate behavior parity. | Titles, lore, button copy TBD for Granite. | **High risk:** inventory/GUI; must work on **Bedrock via Geyser** or be **documented** as limited. |
| `EVT-*` | Hard gameplay parity (XP, loot, death, regions, anti-exploit). | Mostly branding-neutral. | Re-check any **action bar, title, chat, or particles** for cross-platform display. |

---

## 1. Workbook alignment (recommended column use)

| Column (suggested name) | Purpose |
|-------------------------|--------|
| `ID` | Stable key, e.g. `AS-014`, `CMD-003` |
| `Category` | `Bootstrap` \| `Commands` \| `Menus` \| `Listeners` \| `Integrations` \| `API` |
| `Bukkit / baseline reference` | Class or `file#method` (Paper capability inventory) |
| `Current Fabric reference` | Class or `file#method`, or `—` |
| `Parity status` | Same vocabulary as **Status** above |
| `Blocker` | From tags above + free text |
| `Notes` | Behavior gap, test idea, doc link |
| *Optional:* `Final Granite UX impact` | e.g. `None` \| `Rebrand required` \| `Rename later` \| `New command design required` |
| *Optional:* `Final public name / command` | e.g. `TBD` until rebrand sprint |
| *Optional:* `Ship requirement` | e.g. `Parity` \| `Final release` \| `Optional API` |
| *Optional:* `Regression proof` | `Yes` / `No` |
| *Optional:* `Java path status` | e.g. `Untested` \| `Partial` \| `Equivalent` \| `Broken` |
| *Optional:* `Bedrock (Geyser/Floodgate) status` | e.g. `Untested` \| `Partial` \| `Equivalent` \| `Broken` \| `Needs alternate UX` |
| *Optional:* `Bedrock / Geyser impact` | `None` \| `Needs testing` \| `Likely incompatible` \| `Needs alternate UX` \| `Blocked by Geyser/Floodgate` |
| *Optional:* `Cross-platform UX note` | Free text: Java vs Bedrock differences, Floodgate `UUID`/permission caveats, etc. |
| *Optional:* `Bedrock-safe acceptance required` | `Yes` / `No` (use **Yes** for `CMD-*`, `MENU-*`, and visible `EVT-*` / UI rows) |

**Import:** Copy the **CSV block** in §5 into a new sheet (e.g. `Phase1_import`) and `Data > From Text/CSV`, or paste rows into `Audit Matrix`. Adjust column order to match your workbook.

---

## 2. `AuraSkills.java` — bootstrap & lifecycle (ordered)

| ID | Bukkit reference | Fabric reference | Status | Blocker | Notes |
|----|------------------|------------------|--------|---------|-------|
| AS-001 | `ApiAuraSkills` + `ApiRegistrationUtil` | `FabricAuraSkills` + `ApiAuraSkills` + `ApiRegistrationUtil` | Equivalent | None | Core API registered. |
| AS-002 | `ApiAuraSkillsBukkit` + `ApiBukkitRegistrationUtil` + `ApiItemManager` | — | Missing | Common seam | `api-bukkit` / Bukkit menu API not on Fabric. |
| AS-003 | `BukkitLogger` | `FabricLogger` | Equivalent | None | |
| AS-004 | `BukkitMessageProvider` | `FabricMessageProvider` | Partial | None | MiniMessage path in place; keep regression tests vs Paper. |
| AS-005 | Managers/registries (skills, abilities, stats, traits, …) | Same pattern in `FabricAuraSkills` | Partial | None | Shared `common` brain; edge cases may differ. |
| AS-006 | `Slate` + `MenuOptions` + `InventoryManager` + `MenuFileManager` | — / `FabricMenuHelper` stub | Missing | Slate-Fabric | No Slate, no inventory manager, no menu YAML load like Bukkit. |
| AS-007 | `ConfirmManager` | — | Missing | Slate-Fabric + ACF/Brigadier | Confirm flows tied to commands/menus. |
| AS-008 | `CommandRegistrar.registerCommands` → `PaperCommandManager` | `FabricAuraSkillsCommands` | Partial | ACF/Brigadier | Small Brigadier subset vs full ACF tree (see §3). |
| AS-009 | `messageProvider.setACFMessages` | — | Missing | ACF/Brigadier | No ACF message integration. |
| AS-010 | `BukkitLevelManager` + `registerLevelers` | `FabricLevelManager` + `registerFabricXpHandlers` | Partial | None | Levelers wired via Fabric handlers/mixins; compare to Bukkit list. **Sprint 10** — `StatisticLeveler`. **Sprint 11** — `BrewingLeveler` hopper. **Sprint 12** — `EVT-021`. **Sprint 13** — `EVT-022` jumping. **Sprint 14** — `EVT-023` enchanting. **Sprint 15** — `EVT-024` grindstone. **Sprint 16** — `EVT-025` anvil. **Sprint 17** — `EVT-026` item consume. **Sprint 18** — `EVT-027` potion splash. |
| AS-011 | `BukkitAntiAfkManager` | `FabricAntiAfkManager` | Partial | None | `common` checks; verify parity with Bukkit listeners. |
| AS-012 | `Metrics` + `MetricsUtil` | — | Missing | Out of scope | Reference optional metrics; not default Granite product scope. |
| AS-013 | `UpdateChecker` | — | Missing | None | Optional product parity. |
| AS-014 | `onLoad` → Bukkit region-flag hook (reference) | — | Missing | Out of scope | `FabricRegionNode` is a stub; third-party region plugins are not a default target. |
| AS-015 | First-tick: `SkillsLoadEvent` | — | Missing | Common seam | No Bukkit event fired; add Fabric/common event if API parity required. |
| AS-016 | First-tick: `requirementManager` after skills | `FabricRequirementManager` in `finishEnable` | Partial | Out of scope | `region` requirement type: no external region provider in default scope; node passes. |
| AS-017 | First-tick: `lootManager.loadLootTables` | `FabricLootManager.loadLootTables` | Partial | None | Compare to Bukkit loot + handler coverage. |
| AS-018 | `traitManager.registerTraitImplementations` | `FabricTraitManager.registerTraitImplementations` | Partial | Needs Regression Proof | **Sprint 19** — explicit Bukkit vs Fabric diff in **`fabric/SPRINT_19_TRAIT_PARITY.md`**. **`EXPERIENCE_BONUS`** implemented (`FabricExperienceBonusTrait` + `PlayerEntityExperienceBonusMixin`). **Still missing vs Bukkit reference:** `HealthRegenTraits`, `DoubleDropTrait`, `AnvilDiscountTrait`. |
| AS-019 | `abilityManager.registerAbilityImplementations` | `FabricAbilityManager.registerAbilityImplementations` | Partial | Needs Regression Proof | Many skills covered; diff vs `BukkitAbilityManager`. |
| AS-020 | `manaAbilityManager.registerProviders` | `FabricManaAbilityManager.registerProviders` | Partial | None | All major abilities + XP hook (see `FabricManaAbilityUseXpHandler` javadoc, Sprint 12). **Gaps:** Bukkit `ManaAbilityActivateEvent` for plugins — **N/A** on Fabric. |
| AS-021 | `registerEvents()` (see §4) | Scattered Fabric API + mixins | Partial | None | Not 1:1 class mapping; see listener table. |
| AS-022 | `registerAndLoadMenus` | — | Missing | Slate-Fabric | |
| AS-023 | `registerPriorityEvents` → `PlayerJoinQuit` | `FabricPlayerLifecycle` | Partial | None | Join/disconnect; compare full Bukkit behavior. |
| AS-024 | `leaderboardManager` tasks | Same in `finishEnable` | Partial | None | |
| AS-025 | `statManager.scheduleTemporaryModifierTask` | same | Partial | None | |
| AS-026 | `modifierManager` | `FabricModifierManager` + schedules | Partial | None | |
| AS-027 | NBT API init / `BukkitItemRegistry` storage | `FabricItemRegistry` + codecs | Partial | None | Different persistence model. |

---

## 3. `CommandRegistrar` — ACF command surface

**Replacements/conditions/contexts/completions:** part of ACF — **no Fabric port** until **ACF + Brigadier** program lands (`MENU_AND_COMMANDS_PORT.md`).

| ID | Bukkit (registered command class) | Fabric | Status | Blocker |
|----|-----------------------------------|--------|--------|---------|
| CMD-001 | `SkillsRootCommand` | `FabricAuraSkillsCommands` (`/auraskills*`, top-level `/skills` → skills dump only) | Partial | ACF/Brigadier + Slate-Fabric (menus) |
| CMD-002 | `StatsCommand` | — | Missing | ACF/Brigadier |
| CMD-003 | `SkillCommand` | — | Missing | ACF/Brigadier |
| CMD-004 | `ManaCommand` (common) | — | Missing | ACF/Brigadier |
| CMD-005 | `ModifierCommand` | — | Missing | ACF/Brigadier |
| CMD-006 | `ItemCommand` | — | Missing | ACF/Brigadier + item pipeline |
| CMD-007 | `ArmorCommand` | — | Missing | ACF/Brigadier |
| CMD-008 | `ProfileCommand` | — | Missing | ACF/Brigadier |
| CMD-009 | `BackupCommand` | — | Missing | ACF/Brigadier |
| CMD-010 | `XpCommand` | — | Missing | ACF/Brigadier |
| CMD-011 | `PresetCommand` | — | Missing | ACF/Brigadier |
| CMD-012 | `StorageCommand` | — | Missing | ACF/Brigadier |
| CMD-013 | `OpenMenuCommand` | — | Missing | Slate-Fabric + ACF/Brigadier |
| CMD-014 | `ManaAbilityCommand` | — | Missing | ACF/Brigadier |
| CMD-015 | `TraitCommand` | — | Missing | ACF/Brigadier |
| CMD-016 | `JobsCommand` | — | Missing | Out of scope + ACF/Brigadier | Reference jobs feature; not default Granite scope. |
| CMD-017 | `AntiAfkCommand` | — | Missing | ACF/Brigadier |
| CMD-018 | `registerSkillCommands` (per-skill `SkillCommands.*`) | — | Missing | ACF/Brigadier + config `ENABLE_SKILL_COMMANDS` |
| CMD-019 | ACF: replacements (`skills_alias`, desc keys) | — | Missing | ACF/Brigadier | |
| CMD-020 | ACF: contexts (User, Skill, ManaAbility, Stat, Trait, UUID, JsonArg, Duration) | — | Missing | ACF/Brigadier | |
| CMD-021 | ACF: async completions (skills, stats, menu_names, …) | — | Missing | ACF/Brigadier | |

**Fabric admin parity (not in Bukkit ACF list):** `reload`, `save` in `FabricAuraSkillsCommands` — **extra**; keep row if tracking “operator surface” separately.

---

## 4. `MenuRegistrar` — Slate menus

| ID | Bukkit (menu / context) | Fabric | Status | Blocker |
|----|-------------------------|--------|--------|---------|
| MENU-001 | Slate `ContextManager`: Skill, Stat, Source, SortType, Ability, ManaAbility, Trait | — | Missing | Slate-Fabric |
| MENU-002 | `PlaceholderHelper` global replacer + locale | — | Missing | Slate-Fabric |
| MENU-003 | Menu `skills` | — | Missing | Slate-Fabric |
| MENU-004 | Menu `stats` | — | Missing | Slate-Fabric |
| MENU-005 | Menu `level_progression` | — | Missing | Slate-Fabric |
| MENU-006 | Menu `leaderboard` | — | Missing | Slate-Fabric |
| MENU-007 | Menu `sources` | — | Missing | Slate-Fabric |
| MENU-008 | Menu `abilities` | — | Missing | Slate-Fabric |
| MENU-009 | Menu `stat_info` | — | Missing | Slate-Fabric |
| MENU-010 | `ApiMenuManager` non-default menu names | — | Missing | Slate-Fabric + api-bukkit |

---

## 5. `registerEvents` — Bukkit listeners vs Fabric (high level)

| ID | Bukkit class | Fabric mapping | Status | Notes |
|----|--------------|----------------|--------|-------|
| EVT-001 | `PlayerJoinQuit` | `FabricPlayerLifecycle` | Partial | |
| EVT-002 | `DamageListener` + `DamageLeveler` | `FabricCombatXpHandler` + `FabricDamagePipeline` | Partial | **Sprint 08** — Bukkit `DamageLeveler` order + shield (`isBlocking` only), `DamageXpSource` damagers for `ThrownItemEntity` / potion shooters, `EntityLeveler#resolveDamager` live path via `FabricEntityDamageAttribution` + `FabricEntityXpSourceLookup#forEntityDamageOnly` (incl. `THROWN_POTION`→Alchemy). `DamageListener` cancel/correct last damage: **N/A** / partial on Fabric. |
| EVT-003 | `CriticalHandler` | `FabricCriticalHandler` (via pipeline) | Partial | |
| EVT-004 | `BlockInteractions` | `FabricBlockBreakHandler`, `FabricBlockInteractHandler`, etc. | Partial | |
| EVT-005 | `BlockLootHandler` | `FabricBlockLootHandler` + `FabricBlockXpSourceLookup` + `FabricRegionManager` | Partial | Core pool + `override_vanilla_loot` + `check_replace` via placed-block NBT. Plugin-only loot gating: **out of scope**. |
| EVT-006 | `FishingLootHandler` + `FishingLeveler` | `FabricFishingLootHandler` + `FabricFishingXpHandler` (mixin + `Stats.FISH_CAUGHT`) | Partial | **Sprint 09** — HIGH-then-MONITOR order, null-`originalSource` rare/epic, `isInOpenWater` pool option; Bukkit `LootDropEvent` / `EntityLoot` / `getExpToDrop` gate: **N/A** or follow-up. |
| EVT-007 | `MobLootHandler` | `FabricMobLootHandler` | Partial | No Bukkit `LootDropEvent` cancel; skill from `FabricEntityXpSourceLookup` (Fabric damage cause mapping). Plugin-only gates: **out of scope**. |
| EVT-008 | `RequirementListener` | `FabricRequirementHandlers` | Partial | |
| EVT-009 | `ItemListener` | Item/modifier stack | Partial | |
| EVT-010 | `ArmorListener` | Partial via modifier schedules | Partial | |
| EVT-011 | `ArmorModifierListener` | `FabricItemModifierSchedules` / state | Partial | |
| EVT-012 | `RegionListener` | — | Missing | Out of scope | Reference chunk/region file load; third-party region stack not a default target. |
| EVT-013 | `RegionBlockListener` | `BlockItemPlacedBlockMixin` + `FabricPlacedBlockBreakHandler` + `FabricRegionLifecycle` + `FabricRegionBlockIntegrity` + piston/fall/tree mixins (Sprint 05) | Partial | Piston + fall + `TreeFeature` same-Y + tall/support; external “blocked region” plugins **out of default scope**; mushroom grow & rare edges **Needs Regression Proof** |
| EVT-014 | `PlayerDeath` (`ON_DEATH_RESET_SKILLS` / `ON_DEATH_RESET_XP` / `ON_DEATH_RESET_XP_RATIO`) | `FabricPlayerDeathHandler` | Equivalent | Bukkit `PlayerDeath.java` — `ServerLivingEntityEvents.AFTER_DEATH` for `ServerPlayerEntity`; Geyser/Floodgate = same server player path. |
| EVT-017 | `EntityLeveler` (entity `DEATH` / `DAMAGE` XP + spawner) | `FabricEntityKillHandler` + `FabricCombatXpHandler` + `FabricSpawnerMobTracker` + mixins; `FabricEntityDamageAttribution` + `FabricEntityXpSourceLookup` | Partial | **Sprint 07** — kill/mob-loot killer resolution + `DEATH`-only source lookup + bleed PDC-style map. **Gaps:** Bukkit `onMobTransform` spawner copy (slime/drowned), `EntityXpGainEvent` API, MythicMobs — **out of default scope** or follow-up. |
| EVT-018 | `StatisticLeveler` | `FabricStatisticXpHandler` + `FabricStatisticSourceResolver` (timer + stat resolution) | Partial | **Sprint 10** — `Stats.CUSTOM` known ids + `killed/` + `killed_by/` + `dropped/` + block/item types; Bukkit’s enum-only `Statistic` surface is a subset. Double-count vs entity levelers: **config**; cross-play: **Needs Regression Proof** in optional columns. |
| EVT-019 | `BrewingLeveler` | `FabricBrewingXpHandler` + `FabricBrewingTakeoutTracker` + `FabricBrewingOwnerTracker` + mixins (incl. `LockableContainerBrewingStandSetStackMixin`) | Partial | **Sprint 11** — BREW + owner + TAKEOUT; TAKEOUT **clear** on hopper empty of result slots; TAKEOUT XP only on **player** GUI. Rare automation-mod paths: **Needs Regression Proof** if reported. |
| EVT-021 | `ManaAbilityUseLeveler` | `FabricManaAbilityUseXpHandler` (called from all mana providers after consumption) | Partial | **Sprint 12** — full provider set + `isSourceEnabled(MANA_ABILITY_USE)`; Bukkit `ManaAbilityActivateEvent` **cancel** / third-party rewrites: **N/A** |
| EVT-022 | `JumpingLeveler` | `FabricJumpingXpHandler` (`ServerTickEvents#END_SERVER_TICK`) | Partial | **Sprint 13** — `shouldBlockXp`, spectator, `isClimbing`; Bukkit `PlayerMoveEvent` + `check_cancelled` / `LevelerContext` world blocks: **N/A** / partial |
| EVT-023 | `EnchantingLeveler` | `FabricEnchantingXpHandler` + `EnchantmentScreenHandlerMixin` | Partial | **Sprint 14** — `enchantmentPower` for `exp_requirement` unit, spectator; Bukkit `EnchantItemEvent` **cancel** / `LevelerContext` region: **N/A** / `shouldBlockXp` |
| EVT-024 | `GrindstoneLeveler` | `FabricGrindstoneXpHandler` + `ScreenHandlerGrindstoneXpMixin` | Partial | **Sprint 15** — `failsClickChecks`-style cursor + 36-slot fullness + `SlotActionType` allowlist, spectator; Bukkit `check_cancelled` / `LevelerContext` region: **N/A** / `shouldBlockXp` |
| EVT-025 | `AnvilLeveler` | `FabricAnvilXpHandler` + `ForgingScreenHandlerMixin` + `ForgingScreenHandlerAnvilOnTakeMixin` | Partial | **Sprint 16** — `FabricAnvilTakeContext` + `onTake` HEAD/RETURN (anvil, output slot), spectator; Bukkit `failsClickChecks` on `InventoryClickEvent` / `check_cancelled` / region: **N/A** / `onTakeOutput` + `shouldBlockXp` |
| EVT-026 | `ItemConsumeLeveler` | `FabricItemConsumeHandler` + `LivingEntityConsumeMixin` | Partial | **Sprint 17** — `isSourceEnabled(ITEM_CONSUME)`, spectator, `onConsumeItemCommit` javadoc; Bukkit `PlayerItemConsumeEvent` **cancel** / `LevelerContext` region: **N/A** / `shouldBlockXp` |
| EVT-027 | `PotionSplashLeveler` | `FabricPotionSplashHandler` + `PotionEntityMixin` | Partial | **Sprint 18** — `lacksPotionEffectForParity` (Bukkit empty `getEffects`), `isSourceEnabled`, spectator, splash+lingering javadoc; Bukkit event **cancel** / region: **N/A** / `shouldBlockXp` |
| EVT-015 | `JobsListener` | — | Missing | Out of scope | Reference jobs/economy listener; not default Granite scope. |
| EVT-016 | `BukkitLeaderboardExclusion` (listener) | `FabricLeaderboardExclusion` | Partial | |

---

## 6. Proposed first implementation batch (dependency + risk)

**Goal:** maximum unlock toward “Paper-like” server with **one coordinated spine**.

| Order | Work unit | Rationale | Risk |
|-------|-----------|-----------|------|
| **0** | **Player death reset** — `ON_DEATH_RESET_SKILLS` / `ON_DEATH_RESET_XP` (mirror `PlayerDeath.java`) | Config parity; `EVT-014`; **no** Slate/ACF dependency | Low — isolated listener |
| **1** | **Slate-Fabric vertical slice** — one menu (`skills`) open/close, one YAML, `Text`/scheduler bridge | Unblocks `OpenMenuCommand`, `MENU-*`, `CMD-013` | High — sets architecture |
| **2** | **ACF + Brigadier** — `CommandRegistrar` skeleton: `SkillsRootCommand` + `OpenMenuCommand` + shared contexts (`Skill`, `User`) | Unlocks `CMD-001`–`CMD-021` incrementally. **Caveat:** match **Paper command *capability***, not a premature freeze of final **Granite Skills** public names/aliases (see [PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md)). | Medium — dependency on (1) for menu commands |
| **3** | **Loot: `BlockLootHandler` / `MobLootHandler` parity** | Gameplay feel; `EVT-005`, `EVT-007` | Medium — no Slate dependency |
| **4** | **Requirement** `region` + `FabricRegionNode` clarity | Document stub vs reference; `AS-014`, `AS-016`, `EVT-012` | Low — no external mod API in default scope |
| **5** | **SkillsLoadEvent (or common event)** on Fabric if API consumers need it | `AS-015` | Low–medium |

**Not in default Granite roadmap:** optional reference metrics (`AS-012`), `UpdateChecker` (`AS-013`), reference jobs (`EVT-015` / `CMD-016`); full `api-bukkit` (`AS-002`) remains **common seam** for reference types only.

---

## 7. CSV (paste / import)

```csv
ID,Category,Bukkit reference,Fabric reference,Status,Blocker,Notes
AS-001,Bootstrap,ApiRegistration,FabricAuraSkills+ApiRegistrationUtil,Equivalent,None,Core API
AS-002,Bootstrap,ApiBukkit+ApiItemManager,—,Missing,Common seam,api-bukkit
AS-006,Bootstrap,Slate+MenuFileManager+InventoryManager,FabricMenuHelper,Missing,Slate-Fabric,Stub menu helper
AS-008,Bootstrap,CommandRegistrar→PaperCommandManager,FabricAuraSkillsCommands,Partial,ACF/Brigadier,Small command set
AS-012,Bootstrap,Metrics+MetricsUtil,—,Missing,Out of scope,Optional reference metrics
AS-014,Bootstrap,onLoad region flags hook ref,—,Missing,Out of scope,Stub FabricRegionNode
AS-015,Bootstrap,SkillsLoadEvent,—,Missing,Common seam,API event
AS-022,Bootstrap,registerAndLoadMenus,—,Missing,Slate-Fabric,
CMD-001,Commands,SkillsRootCommand,FabricAuraSkillsCommands,Partial,ACF/Brigadier+Slate-Fabric,Aliases differ
CMD-013,Commands,OpenMenuCommand,—,Missing,Slate-Fabric+ACF/Brigadier,
MENU-003,Menus,Menu skills,—,Missing,Slate-Fabric,
EVT-005,Listeners,BlockLootHandler,FabricBlockLootHandler+FabricRegionManager,Partial,None,Plugin loot gates out of scope; check_replace NBT; Sprint 04
EVT-002,Listeners,DamageListener+DamageLeveler,FabricCombatXpHandler+pipeline,Partial,None,Sprint 08: resolver+lookup+damagers
EVT-006,Listeners,FishingLootHandler+FishingLeveler,FabricFishing handlers,Partial,None,Sprint 09 order+open water
EVT-007,Listeners,MobLootHandler,FabricMobLootHandler,Partial,No Bukkit LootDropEvent cancel; Sprint 03,
EVT-014,Listeners,PlayerDeath,FabricPlayerDeathHandler,Equivalent,None,on_death.* config; see Sprint 01 manual
EVT-017,Listeners,EntityLeveler,FabricEntityKillHandler+Combat+Spawner+Sprint07,Partial,None,Entity death XP + attribution; see Sprint 07 manual; gaps: transform, API event
EVT-018,Listeners,StatisticLeveler,FabricStatisticXpHandler+FabricStatisticSourceResolver,Partial,None,Sprint 10: custom+killed/dropped; manual SPRINT_10
EVT-019,Listeners,BrewingLeveler,FabricBrewing handlers+setStack mixin,Partial,None,Sprint 11 TAKEOUT hopper clear; manual SPRINT_11
EVT-021,Listeners,ManaAbilityUseLeveler,FabricManaAbilityUseXpHandler+providers,Partial,None,Sprint 12 source gate; Bukkit event N/A; manual SPRINT_12
EVT-022,Listeners,JumpingLeveler,FabricJumpingXpHandler,Partial,None,Sprint 13 shouldBlockXp+climb; manual SPRINT_13
EVT-023,Listeners,EnchantingLeveler,FabricEnchantingXpHandler+mixin,Partial,None,Sprint 14 exp cost from table; manual SPRINT_14
EVT-024,Listeners,GrindstoneLeveler,FabricGrindstoneXpHandler+mixin,Partial,None,Sprint 15 click gates; manual SPRINT_15
EVT-025,Listeners,AnvilLeveler,FabricAnvilXpHandler+anvil onTake mixin,Partial,None,Sprint 16 take snapshot; manual SPRINT_16
EVT-026,Listeners,ItemConsumeLeveler,FabricItemConsumeHandler+LivingEntityConsumeMixin,Partial,None,Sprint 17 commit+javadoc; manual SPRINT_17
EVT-027,Listeners,PotionSplashLeveler,FabricPotionSplashHandler+PotionEntityMixin,Partial,None,Sprint 18 empty effects gate; manual SPRINT_18
EVT-012,Listeners,RegionListener,—,Missing,Out of scope,Ref chunk load; ext region plugins N/A
EVT-013,Listeners,RegionBlockListener,Placed block mixin+break+chunk load+Sprint05 integrity,Partial,Piston/fall/TreeFeature; ext region plugins N/A,
```

*(Extend this CSV with all rows from §2–5 as you grow the sheet.)*

---

## 8. Caveat (from your handoff)

This pass **does not** claim line-by-line audit of every Bukkit class — only **registration surfaces** for the three entry files. **Next pass:** every `*Leveler`, every `HookManager` hook, `plugin.yml` permissions vs Fabric permission checks.

**External workbook path:** `d:\Downloads\fabricauraskills_parity_audit_sheet.xlsx` was not in the repo; keep the `.xlsx` as the master and **import §7** or **link** this file from `Read Me` as the Phase 1 source.
