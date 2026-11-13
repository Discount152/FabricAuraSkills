# Fabric AuraSkills — parity backlog (phase B)

For the **full handoff** (product goals, deployment assumptions, bootstrap order, Gradle `fabricOnly`, fishing mixin details, file index, and phased roadmap), see **`FABRIC_PORT_HANDOFF.md`** in the repository root.

**Core Fabric port (skills, XP, storage, permissions, reload/save, default YAML sources, mana abilities wired in `FabricManaAbilityManager`) is treated as *complete* for dedicated-server use.** Remaining bullets are **Paper parity gaps**, **third-party integrations**, or **optional polish** — not blockers to run the mod.

This document tracks Paper/plugin behavior that is intentionally **not** implemented on Fabric, or differs enough that server configs may need adjustment. **Third-party parity** (WorldGuard, Mythic, jobs, …) is **optional** unless a named deployment needs it.

## Integrations (Paper soft-dependencies)

- **WorldGuard / regions** — no region flags or placed-block tracking; `check_replace` and similar behaviors from Paper are not replicated.
- **Vault / economy hooks** — `EconomyHook` is never registered; jobs income and economy-tied rewards need a Fabric-specific hook later.
- **PlaceholderAPI** — not applicable on Fabric as-is.
- **LuckPerms / permissions** — **`fabric-permissions-api` is bundled (JiJ).** `FabricUser` uses `Permissions.check` (skill nodes `auraskills.skill.<name>` default **allow** if unset; arbitrary permission strings default **deny** if unset). **`/auraskills version|skills`** use `auraskills.command.version` / `auraskills.command.skills` (default **allow** if unset). For production, install **LuckPerms Fabric** (or another provider) to manage nodes.
- **ProtocolLib** — no protocol-level action bar overrides.
- **MythicMobs, Slimefun, Towny, Nexo, hologram plugins** — not integrated (defer unless needed).

## Anti-exploit / balance

- **Exploit resistance** for supported XP sources should be tracked alongside feature parity (farming loops, spawner/tagging, stat/jump/damage spam, fishing automation). Fabric polls the same check types as Paper (`block_a` through `fishing_a`) from `FabricAntiAfkManager` before XP is granted. `check_replace` / placed-block tracking is still absent.

## Menus and UI

- **Slate / inventory menus** — **In progress (Path A):** full **Slate** port to Fabric + Paper-parity **command tree**; see **`fabric/MENU_AND_COMMANDS_PORT.md`**. Until shipped, `FabricMenuHelper` remains a stub.
- **Boss bar XP** — `FabricBossBarManager` shows the configured single bar (color, progress, stay time). Per-skill mode, income formatting beyond the message placeholders, and Adventure boss-bar audiences are not ported. Action bar / title behavior is still thinner than Paper.

## Gameplay / sources

- **Block XP `states` on break** — `FabricBlockBreakHandler` + `FabricBlockStateMatcher` match YAML state maps, **`getBlocksBroken`** tall-plant multipliers, **`age` / `pickles`**, and **gathering-luck** after XP (`FabricBlockLevelerHelper`, `FabricInteractGatheringLuck`). **`INTERACT` / `COLLECT`** — `FabricBlockInteractHandler` (`UseBlockCallback`): same multipliers/luck; COLLECT item rules mirror Paper (shears / bottle / bucket on hive, nest, leveled cauldrons). Still not ported: Bukkit block metadata skips (`fertilized`, `filledManually`) and cauldron `filledManually` clearing.
- **Placed-block exclusion (`check_replace`)** — not implemented without a region/placement tracker.
- **Entity death XP** — only **player-as-killer** + `DEATH` + `PLAYER` damager sources; **spawner_multiplier** is applied via `FabricSpawnerMobTracker` (no MythicMobs).
- **Damage / defense / agility fall** — `DamageXpSource` is handled via `ServerLivingEntityEvents.AFTER_DAMAGE` (Minecraft damage types mapped to Bukkit-style `DamageCause`). Cooldowns, damager filters, shield blocking option, defense min/max, **mustSurvive**, and **no debuff** for splash **and lingering clouds** are mirrored; edge cases vs Paper may remain.
- **Entity `DAMAGE` trigger** — Fabric awards XP for player melee/projectile hits; **spawner_multiplier** applies. **Bleed DoT** is attributed to the bleed applier via `FabricBleedListener` ThreadLocal for generic damage XP (no NBT persistence across restarts).
- **Statistic sources** — `FabricStatisticXpHandler` supports **`Stats.CUSTOM`** ids (e.g. `walk_one_cm`) and typed keys **`mined/<block>`**, **`used/<item>`**, **`broken/<item>`**, **`picked_up/<item>`**, **`custom/<id>`** (registry ids or `minecraft:` paths). Mob-kills / damage-dealt style stats are not mapped yet.
- **Jumping** — tick-based jump detection mirrors Paper’s velocity checks; edge cases may differ from `PlayerMoveEvent`.
- **Fishing** — mixin on `FishingHook#retrieve` awards XP when `Stats.FISH_CAUGHT` increments; matches `sources/fishing.yml` by **material**, **fishing_junk** / **fishing_treasure** category, item meta, and **`loot_pool`**. `FabricFishingLootHandler` rolls item, command, and entity loot (entity entries spawn at the bobber). Open-water checks follow the loot option when configured.
- **Other levelers** — **on Fabric:** item consume, potion splash, anvil, **grindstone** (`ScreenHandlerGrindstoneXpMixin` on `ScreenHandler.onSlotClick`, grindstone-only); **enchanting table** (`EnchantmentScreenHandlerMixin` → `FabricEnchantingXpHandler`); **brewing** **`BREW`** / **`TAKEOUT`** (see above + `FabricBrewingOwnerTracker`). **Mana abilities** registered in `FabricManaAbilityManager`: **Terraform**, **Charged Shot**, `FabricTimerCountdown`, **Absorption**, **Replenish**, **Speed Mine**, **Treecapitator**, **Lightning Blade**, **Sharp Hook** (see mixin / handler Javadoc). **`ignore_interact`:** `FabricManaAbilityItemTags` (`PublicBukkitValues` byte/int). No Bukkit `ManaAbilityActivateEvent` API on Fabric.

## Worlds and dimensions

- **World names** — `FabricUser.worldNameCompat` maps the overworld registry key to the string `world` for `blocked_worlds` / `disabled_worlds` in `config.yml`. Other dimensions use their registry path (e.g. `the_nether`). This may still differ from Paper world folder names on some hosts.

## Items and NBT

- **Custom items, modifiers, requirements on items/armor** — `FabricItemRegistryStub` and `FabricModifierManager` are no-ops; Paper NBT/item pipeline is not ported.

## Commands

- **ACF / full Paper `/skills` command tree** — not ported. Fabric exposes **`/auraskills version`**, **`/auraskills skills`**, **`/auraskills reload`** (config + YAML data + online trait refresh), and **`/auraskills save`** (flush loaded profiles). Grant **`auraskills.command.reload`** / **`auraskills.command.save`** in LuckPerms (default **deny**).

## API surface

- **`AuraSkillsBukkit` / `api-bukkit`** — not registered on Fabric; Bukkit events are not fired. Granitelib and other Fabric mods listen through `dev.aurelium.auraskills.fabric.FabricSkillsListeners` (user load, skill level-up, XP gain). Mod id, packages, `config/auraskills`, permission nodes, and NBT roots stay `auraskills` / `AuraSkills`.

## Build note

- **Fabric-only** (skips Bukkit / Shadow / Paper toolchain — faster and avoids configuring broken plugin classpath):  
  **`./gradlew -PfabricOnly=true :fabric:compileJava`** or **`./gradlew -PfabricOnly=true :fabric:build`**.  
  `run-fabric-compile.cmd` passes `-PfabricOnly=true` and sets `GRADLE_USER_HOME` to `D:\gradle` when you use that layout.
- **Full multiplatform** (default `settings.gradle.kts`): **`./gradlew :fabric:build`** — includes `bukkit` and `api-bukkit`.
- The `fabric` module uses Fabric Loom **1.20.0-alpha.31** for Minecraft **26.3** (unobfuscated Mojang names, Java 25). Set `fabric.loom.disableObfuscation=true`.
- If Gradle fails with **`ExternalDependencyInstrumentingArtifactTransform`** / **`Failed to create Jar file ... instrumented-`**, run **`fix-gradle-transforms-cache.cmd`** from the repo root (or delete `%USERPROFILE%\.gradle\caches\8.14.2\transforms` yourself), then rebuild.
