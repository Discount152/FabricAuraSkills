# Fabric AuraSkills — Reference-baseline gap list (Fabric runtime)

**Product target:** **Granite Skills** on **GraniteLib**, **Java Fabric** server only — with **equivalent capability** to the **reference `bukkit` module** in this repo (not Bukkit at runtime). **Supported players:** **Java** clients and **Bedrock** clients via **Geyser + Floodgate** on that Fabric server — see **[PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md)** (*Client / player access*). This file is a **living checklist**; shrink bullets as Fabric catches the reference.

For **bootstrap order**, **Gradle `fabricOnly`**, **fishing mixin** notes, and **phased technical roadmap** (A–G), see **[FABRIC_PORT_HANDOFF.md](../FABRIC_PORT_HANDOFF.md)** in the repository root.

**Baseline:** a **dedicated-server Fabric** mod can run skills, XP, storage, permissions, reload/save, and many sources; that is **not** the end state — it is the floor from which parity work continues.

The sections below list behavior present in the **reference** (Bukkit-side) build that is **not yet** implemented on **Fabric**, or that differs. **Integrations** that were Bukkit plugins are in scope as **Fabric mod bridges** or shims.

## Geyser / Floodgate / Bedrock (cross-platform acceptance)

These are **product** requirements, not optional polish:

- **Geyser + Floodgate** on the **same** Fabric server: Bedrock players must be able to use **core progression** (XP, skills, stats) without Java-only assumptions where avoidable.
- **High-risk areas** for Java vs Bedrock divergence: **inventory menus**, **command UX**, **action bar / title / boss bar / chat** formatting, **click** behavior, **permission** resolution for linked Bedrock accounts (Floodgate), **item** interactions that assume Java edition client details.
- **Definition of done:** a feature is not “finished” for the project if it only passes **Java-client** testing; either it is **verified** on Bedrock via Geyser/Floodgate or the **limitation is documented** and accepted (see [PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md) §5). Track per-row **Bedrock status** in [PARITY_AUDIT_LEDGER_PHASE1.md](PARITY_AUDIT_LEDGER_PHASE1.md) optional columns.

## Integrations (reference: Bukkit soft-dependencies; Fabric = bridges)

- **WorldGuard / regions** — no region flags or placed-block tracking; `check_replace` and similar behaviors from Paper are not replicated.
- **Vault / economy hooks** — `EconomyHook` is never registered; jobs income and economy-tied rewards need a Fabric-specific hook later.
- **PlaceholderAPI** — not applicable on Fabric as-is.
- **LuckPerms / permissions** — **`fabric-permissions-api` is bundled (JiJ).** `FabricUser` uses `Permissions.check` (skill nodes `auraskills.skill.<name>` default **allow** if unset; arbitrary permission strings default **deny** if unset). Brigadier commands use `auraskills.command.*` nodes (see **`MOD_COMMANDS.txt`**). For production, install **LuckPerms Fabric** (or another provider) to manage nodes.
- **ProtocolLib** — no protocol-level action bar overrides.
- **MythicMobs, Slimefun, Towny, Nexo, hologram plugins** — not integrated; **in parity scope** with explicit Fabric mod bridges (see **PAPER_PARITY_PROGRAM.md** §2).

## Anti-exploit / balance

- **Exploit resistance** for supported XP sources should be tracked alongside feature parity (farming loops, spawner/tagging, stat/jump/damage spam, fishing automation). `AntiAfkManager` is registered from `common` but does not cover all cases.

## Menus and UI

- **Slate / inventory menus** — **In progress (Path A):** full **Slate** port to Fabric + Paper-parity **command tree**; see **`fabric/MENU_AND_COMMANDS_PORT.md`**. Until shipped, `FabricMenuHelper` remains a stub.
- **Boss bar XP** — stubbed; action bar / title behavior is minimal compared to Paper.

## Gameplay / sources

- **Block skill loot (Bukkit `BlockLootHandler`)** — implemented on Fabric: **`FabricBlockLootHandler`** (pools, Excavation/Mining/Foraging/Farming providers, item + command loot, `override_vanilla_loot`). Manual: `SPRINT_02_MANUAL_VALIDATION.md`. **Gaps vs Bukkit:** no WorldGuard `CUSTOM_LOOT` flag, no Slimefun block skip; `override_vanilla_loot` clears vanilla `ItemEntity` instances in a small box in `AFTER` (not a inject of `Block.dropStacks`).
- **On-death reset (`on_death.reset_skills` / `on_death.reset_xp` / `on_death.reset_xp_ratio`)** — implemented on Fabric: **`FabricPlayerDeathHandler`** (parity with Bukkit `PlayerDeath.java`). Validate Java + Geyser/Floodgate: `SPRINT_01_MANUAL_VALIDATION.md`.
- **Block XP `states` on break** — `FabricBlockBreakHandler` + `FabricBlockStateMatcher` match YAML state maps, **`getBlocksBroken`** tall-plant multipliers, **`age` / `pickles`**, and **gathering-luck** after XP (`FabricBlockLevelerHelper`, `FabricInteractGatheringLuck`). **`INTERACT` / `COLLECT`** — `FabricBlockInteractHandler` (`UseBlockCallback`): same multipliers/luck; COLLECT item rules mirror Paper (shears / bottle / bucket on hive, nest, leveled cauldrons). Still not ported: Bukkit block metadata skips (`fertilized`, `filledManually`) and cauldron `filledManually` clearing.
- **Placed-block exclusion (`check_replace`)** — not implemented without a region/placement tracker.
- **Entity death XP** — only **player-as-killer** + `DEATH` + `PLAYER` damager sources; **spawner_multiplier** is applied via `FabricSpawnerMobTracker` (no MythicMobs).
- **Damage / defense / agility fall** — `DamageXpSource` is handled via `ServerLivingEntityEvents.AFTER_DAMAGE` (Minecraft damage types mapped to Bukkit-style `DamageCause`). Cooldowns, damager filters, shield blocking option, defense min/max, **mustSurvive**, and **no debuff** for splash **and lingering clouds** are mirrored; edge cases vs Paper may remain.
- **Entity `DAMAGE` trigger** — Fabric awards XP for player melee/projectile hits; **spawner_multiplier** applies. **Bleed DoT** is attributed to the bleed applier via `FabricBleedListener` ThreadLocal for generic damage XP (no NBT persistence across restarts).
- **Statistic sources** — `FabricStatisticXpHandler` supports **`Stats.CUSTOM`** ids (e.g. `walk_one_cm`) and typed keys **`mined/<block>`**, **`used/<item>`**, **`broken/<item>`**, **`picked_up/<item>`**, **`custom/<id>`** (registry ids or `minecraft:` paths). Mob-kills / damage-dealt style stats are not mapped yet.
- **Jumping** — tick-based jump detection mirrors Paper’s velocity checks; edge cases may differ from `PlayerMoveEvent`.
- **Fishing** — mixin on `FishingBobberEntity#use` awards XP when `Stats.FISH_CAUGHT` increments; matches `sources/fishing.yml` by **material**, **fishing_junk** / **fishing_treasure** category, item meta, and **`loot_pool`** when the skill loot table is available (`FabricLootItemMatcher` matches pool templates with item-type or full stack equality). No `FishingLootHandler` extra drops or open-water checks.
- **Other levelers** — **on Fabric:** item consume, potion splash, anvil, **grindstone** (`ScreenHandlerGrindstoneXpMixin` on `ScreenHandler.onSlotClick`, grindstone-only); **enchanting table** (`EnchantmentScreenHandlerMixin` → `FabricEnchantingXpHandler`); **brewing** **`BREW`** / **`TAKEOUT`** (see above + `FabricBrewingOwnerTracker`). **Mana abilities** registered in `FabricManaAbilityManager`: **Terraform**, **Charged Shot**, `FabricTimerCountdown`, **Absorption**, **Replenish**, **Speed Mine**, **Treecapitator**, **Lightning Blade**, **Sharp Hook** (see mixin / handler Javadoc). **`ignore_interact`:** `FabricManaAbilityItemTags` (`PublicBukkitValues` byte/int). No Bukkit `ManaAbilityActivateEvent` API on Fabric.

## Worlds and dimensions

- **World names** — `FabricUser.worldNameCompat` maps the overworld registry key to the string `world` for `blocked_worlds` / `disabled_worlds` in `config.yml`. Other dimensions use their registry path (e.g. `the_nether`). This may still differ from Paper world folder names on some hosts.

## Items and NBT

- **Custom items, modifiers, requirements on items/armor** — Fabric has **`FabricItemRegistry`**, **`FabricModifierManager`**, and item state/schedules; **full** Paper NBT/legacy-behavior parity (every edge case in Bukkit `ItemListener` / armor) may still differ — track against Bukkit and shrink this bullet as coverage closes.

## Commands

- **ACF / full Paper `skills` root command tree** (aliases `sk` / `skill` per Bukkit) — **not** complete on Fabric. Current Brigadier surface: see **`MOD_COMMANDS.txt`**. **Planned:** **[MENU_AND_COMMANDS_PORT.md](MENU_AND_COMMANDS_PORT.md)** + **[PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md)**. Staff nodes **`auraskills.command.reload`** / **`auraskills.command.save`** (default **deny** when using LuckPerms).

## API surface

- **`AuraSkillsBukkit` / `api-bukkit`** — not registered on Fabric; anything depending on Bukkit-specific API will not run.

## Build note

- **Fabric-only** (skips Bukkit / Shadow / Paper toolchain — faster and avoids configuring broken plugin classpath):  
  **`./gradlew -PfabricOnly=true :fabric:compileJava`** or **`./gradlew -PfabricOnly=true :fabric:build`**.  
  `run-fabric-compile.cmd` passes `-PfabricOnly=true` and sets `GRADLE_USER_HOME` to `D:\gradle` when you use that layout.
- **Full multiplatform** (default `settings.gradle.kts`): **`./gradlew :fabric:build`** — includes `bukkit` and `api-bukkit`.
- The `fabric` module uses Fabric Loom **1.13.x** for Minecraft **1.21.11** with **Yarn 1.21.11+build.4**.
- If Gradle fails with **`ExternalDependencyInstrumentingArtifactTransform`** / **`Failed to create Jar file ... instrumented-`**, run **`fix-gradle-transforms-cache.cmd`** from the repo root (or delete `%USERPROFILE%\.gradle\caches\8.14.2\transforms` yourself), then rebuild.
