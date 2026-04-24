# Fabric AuraSkills — Reference-baseline gap list (Fabric runtime)

**Product target:** **Granite Skills** on **GraniteLib**, **Java Fabric** server only — with **equivalent capability** to the **in-repo** **reference `bukkit` module** (not Bukkit at runtime). **Supported players:** **Java** and **Bedrock (Geyser + Floodgate)**. This file is a **living checklist**; shrink bullets as Fabric catches the reference.

**Hard scope rule:** the default product is **only** a Fabric- and Geyser/Floodgate-friendly port of **AuraSkills + Slate** behavior, shipped as **Granite Skills + GraniteLib**. Unrelated **Paper/Bukkit plugin ecosystems** are **out of default scope** — see *Out of scope (explicit)* below — and must **not** be read as a roadmap of future integrations.

For **bootstrap order**, **Gradle `fabricOnly`**, **fishing mixin** notes, and **phased technical roadmap** (A–G), see **[FABRIC_PORT_HANDOFF.md](../FABRIC_PORT_HANDOFF.md)** in the repository root.

**Baseline:** a **dedicated-server Fabric** mod can run skills, XP, storage, permissions, reload/save, and many sources; that is **not** the end state — it is the floor from which parity work continues.

The sections below list behavior present in the **reference** module that is **not yet** implemented on **Fabric**, or that differs.

## Out of scope (explicit)

The following are **not** part of the default **Granite** product roadmap. The **reference** JAR may still mention or optionally hook them; that does **not** obligate this Fabric port. **Owner** may re-open any item later as a **separate** decision.

- **Third-party Bukkit/Paper plugin stacks** (region plugins, generic economy/payment bridges, chat/protocol shims, optional metrics, other server-plugin ecosystems not required for core skills/Slate).
- **Jobs / economy income** as implemented via reference **economy** hooks (reference `JobsListener` / `EconomyHook` are reference-only; not planned here by default).
- **Reference-only** optional metrics/update checks — same rule: not default Granite scope.

**In scope and staying:** **Fabric API**, **fabric-permissions-api** (bundled) + an optional **Fabric permission provider** in production, **Geyser + Floodgate** validation for cross-play, and all **in-repo** gameplay aligned with **`bukkit` as reference** (YAML, levelers, menus when ported, etc.).

## Geyser / Floodgate / Bedrock (cross-platform acceptance)

These are **product** requirements, not optional polish:

- **Geyser + Floodgate** on the **same** Fabric server: Bedrock players must be able to use **core progression** (XP, skills, stats) without Java-only assumptions where avoidable.
- **High-risk areas** for Java vs Bedrock divergence: **inventory menus**, **command UX**, **action bar / title / boss bar / chat** formatting, **click** behavior, **permission** resolution for linked Bedrock accounts (Floodgate), **item** interactions that assume Java edition client details.
- **Definition of done:** a feature is not “finished” for the project if it only passes **Java-client** testing; either it is **verified** on Bedrock via Geyser/Floodgate or the **limitation is documented** and accepted (see [PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md) §5). Track per-row **Bedrock status** in [PARITY_AUDIT_LEDGER_PHASE1.md](PARITY_AUDIT_LEDGER_PHASE1.md) optional columns.

## Anti-exploit / balance

- **Exploit resistance** for supported XP sources should be tracked alongside feature parity (farming loops, spawner/tagging, stat/jump/damage spam, fishing automation). `AntiAfkManager` is registered from `common` but does not cover all cases.

## Traits (reference `BukkitTraitManager`)

- **Sprint 19 (batch 1)** — Parity audit in **`fabric/SPRINT_19_TRAIT_PARITY.md`**. **`Traits.EXPERIENCE_BONUS`** on Fabric (`FabricExperienceBonus` + `FabricExperienceBonusTrait` + `PlayerEntityExperienceBonusMixin`).
- **Sprint 20 (batch 2)** — **`HealthRegenTraits`**: **`FabricHealthRegenTraits`** + **`HungerManagerHealthRegenMixin`** + custom-delay tasks; Bukkit-only **`CustomRegenEvent`** not fired — **`fabric/SPRINT_20_HEALTH_REGEN_PARITY.md`**, manual **`SPRINT_20_MANUAL_VALIDATION.md`**.
- **Sprint 21 (batch 3)** — **`AnvilDiscountTrait`**: **`FabricAnvilDiscountTrait`**, **`FabricAnvilDiscount`**, **`AnvilScreenHandlerAnvilDiscountMixin`** — **`SPRINT_21_ANVIL_DISCOUNT_PARITY.md`**, manual **`SPRINT_21_MANUAL_VALIDATION.md`**.
- **Sprint 22 (batch 4)** — **`DoubleDropTrait`** (core in-scope subset): **`FabricDoubleDropTrait`**, **`FabricDoubleDrop`**, `FabricLevelManager` registration order — **`SPRINT_22_DOUBLE_DROP_PARITY.md`**, manual **`SPRINT_22_MANUAL_VALIDATION.md`**. **Not** in default product: Bukkit **WorldGuard** + **`LootDropEvent`** hook surface.

## Abilities (reference `BukkitAbilityManager`) — `registerAbilityImplementations()`

- **Sprint 23 (audit + batch 1)** — **`SPRINT_23_ABILITY_PARITY.md`**, manual **`SPRINT_23_MANUAL_VALIDATION.md`**: all **14** skill ability classes registered on Fabric (same set/order as Bukkit). **Batch 1:** **`Geneticist`** + **`GROWTH_AURA`** for **`CropBlock` / `NetherWartBlock`**. **Sprint 24 (batch 2) —** **`SPRINT_24_FARMING_GROWTH_AURA.md`**, manual **`SPRINT_24_MANUAL_VALIDATION.md`**: **`GROWTH_AURA`** cactus, sugar cane, cocoa, stem, sweet berry, kelp, cave vines + unified `age` handling. **Sprint 25 (batch 3) —** **`SPRINT_25_FARMING_GROWTH_AURA.md`**, manual **`SPRINT_25_MANUAL_VALIDATION.md`**: bonemeal, bamboo, weeping/twisting vines; see doc for **not** in scope (chorus, trees, some bamboo). **Sprint 26 (proof) —** **`SPRINT_26_ABILITY_PROOF_MATRIX.md`**, manual **`SPRINT_26_MANUAL_ABILITY_REGRESSION.md`**: Bukkit vs Fabric per-family table; ledger **`AS-019`**; no new ability code in sprint 26 (farming tail remains as in Sprint 25 doc).

## Menus and UI

- **Slate / inventory menus** — **In progress (Path A):** full **Slate** port to Fabric + Paper-parity **command tree**; see **`fabric/MENU_AND_COMMANDS_PORT.md`**. **Sprint 27–29** — **Skills** + **Stats** via **`FabricMenuRegistry`**, **`FabricMenuLoaders`**, **`GraniteContainerMenus`**, `/skills` + `/stats` (temporary); `FabricMenuHelper` has `level_progression` + `stats` format keys; **Sprint 29** registry scaffold — not YAML `MenuFileManager` / Slate load.
- **Boss bar XP** — stubbed; action bar / title behavior is minimal compared to Paper.

## Gameplay / sources

- **Block skill loot (Bukkit `BlockLootHandler`)** — implemented on Fabric: **`FabricBlockLootHandler`** (pools, Excavation/Mining/Foraging/Farming providers, item + command loot, `override_vanilla_loot`). Manual: `SPRINT_02_MANUAL_VALIDATION.md`. **Gaps vs reference:** no Bukkit `LootDropEvent` cancel path; reference-only “custom loot” / plugin guards are **out of default scope**; `override_vanilla_loot` clears vanilla `ItemEntity` instances in a small box in `AFTER` (not a inject of `Block.dropStacks`).
- **Mob skill loot (Bukkit `MobLootHandler`)** — implemented on Fabric: **`FabricMobLootHandler`** + **`FabricMobContextProvider`** / **`FabricMobContext`** + **`FabricEntityXpSourceLookup`**. Manual: `SPRINT_03_MANUAL_VALIDATION.md`. **Gaps vs reference:** no Bukkit `LootDropEvent` cancel; last-hit `DamageSource` must be direct player or player-owned projectile (same class of cases as Bukkit’s `EntityDamageByEntityEvent` gate). Plugin-only loot gating is **out of default scope**.
- **On-death reset (`on_death.reset_skills` / `on_death.reset_xp` / `on_death.reset_xp_ratio`)** — implemented on Fabric: **`FabricPlayerDeathHandler`** (parity with Bukkit `PlayerDeath.java`). Validate Java + Geyser/Floodgate: `SPRINT_01_MANUAL_VALIDATION.md`.
- **Block XP `states` on break** — `FabricBlockBreakHandler` + `FabricBlockStateMatcher` match YAML state maps, **`getBlocksBroken`** tall-plant multipliers, **`age` / `pickles`**, and **gathering-luck** after XP (`FabricBlockLevelerHelper`, `FabricInteractGatheringLuck`). **`INTERACT` / `COLLECT`** — `FabricBlockInteractHandler` (`UseBlockCallback`): same multipliers/luck; COLLECT item rules mirror Paper (shears / bottle / bucket on hive, nest, leveled cauldrons). Still not ported: Bukkit block metadata skips (`fertilized`, `filledManually`) and cauldron `filledManually` clearing.
- **Placed-block exclusion (`check_replace`)** — **implemented:** **`FabricRegionManager`** + NBT `regiondata/` (Bukkit model), `BlockItemPlacedBlockMixin`, chunk load + save timer, break removal; **`FabricRegionBlockIntegrity`** + mixins (`PistonHandlerRegionMixin`, `FallingBlockEntityRegionMixin`, `TreeFeatureGrowContextMixin`, `TreeFeatureSetBlockMixin`) + tall-plant / support follow-up on break (`SPRINT_05_MANUAL_VALIDATION.md`). **Remaining gaps vs reference `RegionBlockListener`:** config-driven “blocked check replace” via **external region plugins** is **out of default scope** (use `check_block_replace.blocked_worlds` and in-game rules); mushroom / other non-`TreeFeature` grow paths may differ; rare piston/retract edge cases if reported.
- **Entity death XP (Sprint 07)** — **`FabricEntityKillHandler`** uses **shared** `FabricEntityDamageAttribution` (melee, `ProjectileEntity`, `ThrownItemEntity` / potions, `getAttacker` fallback, bleed damager) + **`FabricEntityXpSourceLookup#forEntityDeathKill`** (`DEATH` + damage cause + `PLAYER` / `PROJECTILE` / `THROWN_POTION` gating) + **`spawner_multiplier`** via `FabricSpawnerMobTracker` + mixins. **Gaps vs reference** `EntityLeveler#onMobTransform` / RoseStacker PDC, MythicMobs `preventMythicXp` — out of default scope. Slime / drowned spawner **tag inheritance** is still **not** replicated (Bukkit PDC on transform).
- **Damage / defense / agility fall (Sprint 08)** — Bukkit `DamageLeveler#onDamage` order: cooldown → self-inflict → shield (defense, `isBlocking` only) → `mustSurvive` / min-max. `DamageXpSource` **damager** / **excluded** matching uses `ProjectileEntity` and **`ThrownItemEntity`** owners when `include_projectiles` (splash potion is not a Yarn `ProjectileEntity`). **Shared gates:** `FabricXpHandlerUtil.shouldBlockXp` for the damage skill. Manual: `SPRINT_08_MANUAL_VALIDATION.md`. **Gaps vs reference** `DamageListener` (cancel / `DAMAGE_CORRECT_LAST_DAMAGE` schedule): not default Fabric; edge cases if reported.
- **Entity `DAMAGE` trigger (Sprint 08)** — Live hits follow **`EntityLeveler#resolveDamager`**: melee only for `ENTITY_ATTACK` / `ENTITY_SWEEP_ATTACK`; `ProjectileEntity` + `PotionEntity` / other **`ThrownItemEntity`**; optional **`THROWN_POTION`→Alchemy** when `source.entity.give_alchemy_on_potion_combat` (same as Bukkit). Skill resolution: **`FabricEntityXpSourceLookup#forEntityDamageOnly`**. **No** unqualified `getAttacker()` fallback (matches reference resolver). **spawner_multiplier** + **bleed** DoT (ThreadLocal) unchanged.
- **Statistic sources (Sprint 10)** — `FabricStatisticXpHandler` + **`FabricStatisticSourceResolver`**: **`Stats.CUSTOM`** (bare or **`custom/…`**, with known paths for `damage_dealt`, `mob_kills`, `player_kills`, `deaths`, shield/damage sub-stats), typed **`mined/`**, **`used/`**, **`broken/`**, **`picked_up/`**, **`dropped/`**, **`killed/<entity_type>`**, **`killed_by/<entity_type>`**. Overlap with **entity kill / damage** levelers if both configured: **config** / operator responsibility (not auto-deduped). Manual: `SPRINT_10_MANUAL_VALIDATION.md`.
- **Jumping (Sprint 13)** — `FabricJumpingXpHandler` (end-tick) + `shouldBlockXp` + spectator / `isClimbing` skips; Bukkit `PlayerMoveEvent` + optional **cancel** / region API: **see `SPRINT_13_MANUAL_VALIDATION.md`**, still **Partial**.
- **Fishing (Sprint 09)** — **`FishingBobberEntityMixin`** + **`FabricFishingXpHandler`**: `Stats.FISH_CAUGHT` then **`tryRollBonusLoot`** (Bukkit `HIGH`) before base XP (Bukkit `MONITOR`); re-match `FishingXpSource` on the post-bonus `ItemStack`. **`FabricFishingLootHandler`:** `require_open_water` + `FishingBobberEntity#isInOpenWater` (like Bukkit `1.16.5+`); **rare/epic** with Treasure Hunter / Epic Catch when the catch has **no** matching source (Bukkit: `Fishing` skill + null `originalSource` pool `else`); pool `source` for non-ability rows only if **matched** (Bukkit). **Gaps vs reference:** Bukkit `LootDropEvent` cancel, `EntityLoot`, **`getExpToDrop == 0`** skip for bonus (see `SPRINT_09_MANUAL_VALIDATION.md`).
- **Mana-ability use XP (Sprint 12)** — `FabricManaAbilityUseXpHandler` after each activation with consumed mana; all providers in `FabricManaAbilityManager#registerProviders` (plus `FabricSharpHook` / mixin) call it. **Gaps vs Bukkit:** no `ManaAbilityActivateEvent` for plugins to cancel or rewrite `getManaUsed()`.
- **Other levelers** — **on Fabric:** **item consume (Sprint 17)** — `LivingEntityConsumeMixin` → `FabricItemConsumeHandler#onConsumeItemCommit` (`LivingEntity#consumeItem` = consumption commit, not use-tick; spectator + `isSourceEnabled(ITEM_CONSUME)`), **potion splash (Sprint 18)** — `PotionEntityMixin` → `FabricPotionSplashHandler` (`lacksPotionEffectForParity` / Bukkit empty effects, spectator, `isSourceEnabled(POTION_SPLASH)`), **anvil (Sprint 16)** — `ForgingScreenHandlerMixin` + `FabricAnvilTakeContext` (pre-consumption left/right + level cost at `onTake` HEAD for output), spectator; **grindstone (Sprint 15)** — `ScreenHandlerGrindstoneXpMixin` + `FabricGrindstoneXpHandler#allowsGrindstoneClickForXp` (Bukkit `failsClickChecks`-style: cursor, 36-slot fullness vs shift/swap/drop, spectator); **enchanting table (Sprint 14)** — `EnchantmentScreenHandlerMixin` → `FabricEnchantingXpHandler`: **exp_requirement** uses `enchantmentPower[button]` (Bukkit `getExpLevelCost`), spectator skip; Bukkit `EnchantItemEvent` **cancel** / `LevelerContext` region: **N/A** / `shouldBlockXp` only. **Brewing (Sprint 11)** — **`BREW` / `TAKEOUT`** with **`FabricBrewingOwnerTracker`**, `ScreenHandlerBrewingTakeoutXpMixin`, `LockableContainerBrewingStandSetStackMixin` (see above). **Mana** providers + `FabricTimerCountdown` / **`ignore_interact`:** `FabricManaAbilityItemTags` — Javadoc on `FabricManaAbilityManager` / `FabricManaAbilityUseXpHandler`.

## Worlds and dimensions

- **World names** — `FabricUser.worldNameCompat` maps the overworld registry key to the string `world` for `blocked_worlds` / `disabled_worlds` in `config.yml`. Other dimensions use their registry path (e.g. `the_nether`). This may still differ from Paper world folder names on some hosts.

## Items and NBT

- **Custom items, modifiers, requirements on items/armor** — Fabric has **`FabricItemRegistry`**, **`FabricModifierManager`**, and item state/schedules; **full** Paper NBT/legacy-behavior parity (every edge case in Bukkit `ItemListener` / armor) may still differ — track against Bukkit and shrink this bullet as coverage closes.

## Commands

- **ACF / full reference `skills` root command tree** (aliases `sk` / `skill` per Bukkit) — **not** complete on Fabric. Current Brigadier surface: see **`MOD_COMMANDS.txt`**. **Planned:** **[MENU_AND_COMMANDS_PORT.md](MENU_AND_COMMANDS_PORT.md)** + **[PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md)**. Staff nodes **`auraskills.command.reload`** / **`auraskills.command.save`** (default **deny** with a **Fabric permission provider** in production, if you use one).

## API surface

- **`AuraSkillsBukkit` / `api-bukkit`** — not registered on Fabric; anything depending on Bukkit-specific API will not run.

## Build note

- **Fabric-only** (skips Bukkit / Shadow / Paper toolchain — faster and avoids configuring broken plugin classpath):  
  **`./gradlew -PfabricOnly=true :fabric:compileJava`** or **`./gradlew -PfabricOnly=true :fabric:build`**.  
  `run-fabric-compile.cmd` passes `-PfabricOnly=true` and sets `GRADLE_USER_HOME` to `D:\gradle` when you use that layout.
- **Full multiplatform** (default `settings.gradle.kts`): **`./gradlew :fabric:build`** — includes `bukkit` and `api-bukkit`.
- The `fabric` module uses Fabric Loom **1.13.x** for Minecraft **1.21.11** with **Yarn 1.21.11+build.4**.
- If Gradle fails with **`ExternalDependencyInstrumentingArtifactTransform`** / **`Failed to create Jar file ... instrumented-`**, run **`fix-gradle-transforms-cache.cmd`** from the repo root (or delete `%USERPROFILE%\.gradle\caches\8.14.2\transforms` yourself), then rebuild.
