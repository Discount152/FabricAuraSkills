# Granite Skills 26.3 feature restoration

This ledger records what was brought from the feature baseline onto the Minecraft 26.3 gameplay branch, and what is still not restored. Compilation and unit tests are not release acceptance. In-game GUI, Bedrock, and production install were not run.

## Pins

| Role | Ref | Commit | Notes |
| --- | --- | --- | --- |
| 26.3 gameplay base | `cursor/granite-skills-26.3-65b4` | `c504b25f46acc2d998d07f79b56c3d8279e56137` | Version was 2.3.18. Identity migration, Mojmap gameplay, and the 24-slot level-progression layout already lived here. |
| Feature baseline | `feature/granitelib-module-ids` | `7fae18a8d62c98ec4f38bde84e35004bdd4bd3ad` | Version 2.3.176, Minecraft 1.21.11, Yarn names, Java 21. |
| Common ancestor | `main` | `1295405d96bbcac51453dc1be1fc955e02a1c8a3` | Do not treat `main` as the gameplay tree. |
| GraniteLib 26.3 port | `cursor/granitelib-26-3-8208` | `650c7ed98273ed15eed83780ea2fa171482f8972` | Standalone menu and YAML template library. Copied as `granitelib-fabric`. Loom plugin aligned to `fabric-loom` 1.20.0-alpha.31 so the composite build matches gameplay. |

This work is on `cursor/feature-restoration-26-3-65b4`, branched from `c504b25`. Gameplay version is **2.3.178**. Library version is **0.2.0-alpha.1**. Version 2.3.18 is not reused for the changed gameplay bytes. 2.3.177 restored the menus and then hung while loading persistent chunks; 2.3.178 is that build with the chunk-load callback fixed.

## Artifacts

Install both jars. GraniteLib is a separate Fabric mod, not nested inside Granite Skills.

| Jar | SHA-256 | Bytes |
| --- | --- | --- |
| `fabric/build/libs/GraniteSkills-2.3.178.jar` | `8322f70bfcee189814e74ab961816932c292d85f7f5db96c329ea7c99c75cf4a` | 3734037 |
| `fabric/build/libs/GraniteSkills-2.3.177.jar` | `a75639cfb19825313bb789897ad53ded595c65cff654850cef67b65ba938849e` | 3731938 |
| `granitelib-fabric/build/libs/granitelib-fabric-0.2.0-alpha.1.jar` | `9577adb0e95e5dc80e76994d898aafb4b034ef3c2e54f5d168665f05cd7b83cc` | 325227 |

`GraniteSkills-2.3.177.jar` depends on `granitelib >=0.2.0-alpha.1`. It does not contain a GraniteLib jar. SnakeYAML 2.3 is nested inside the GraniteLib jar. Adventure MiniMessage and Gson serializers are nested inside the Granite Skills jar for chat formatting. Bukkit Slate (`dev.aurelium:slate:1.2.2`) and Polyglot (`dev.aurelium:polyglot:1.2.4`) stay third-party: GraniteLib does not load Bukkit menus or message files.

Authors recorded in `fabric/src/main/resources/fabric.mod.json`: Archyx (original AuraSkills), Discount152 (Fabric port), Granite Skills.

## How the port was done

The 26.3 tree was not replaced. Feature menu, command, text, item-claim, and template sources were copied and adapted onto Mojang 26.3 names. Existing 26.3 gameplay classes were left in place. Yarn mixin descriptors from 1.21.11 were not copied.

Package and identifier rewrite: `dev.aurelium.auraskills` to `dev.granite.graniteskills`, command and permission roots to `graniteskills`. Vanilla `/xp` and `/item` are not registered. Granite item and XP operations sit under `/graniteskills`, `/skills`, `/sk`, and `/skill`.

## Status by feature

Status words: **present** (code is on this branch and covered by a unit test or by dedicated-server startup), **partial** (some of the feature-baseline behavior is here), **missing** (the feature-baseline file is not on this branch), **intentionally replaced** (26.3 already has a different implementation that must stay), **unverified** (not exercised in a client or on Bedrock).

### Dependency and lifecycle

| Feature | Old path | Current path | Status | Adaptation | Validation | Limitation |
| --- | --- | --- | --- | --- | --- | --- |
| Standalone GraniteLib | `slate-fabric` on the feature branch, package `dev.granitelib` | `granitelib-fabric/` | present | Existing 26.3 port reused. Gradle include renamed from `slate-fabric` to `granitelib-fabric`. Compile dependency, not Jar-in-Jar. | `granitelib-fabric` tests: 35, 0 failures. Server log must show the `granitelib` mod. | The old `slate-fabric/` directory is still on disk and is not a Gradle project. |
| Mod dependency metadata | feature `fabric.mod.json` depended on `granitelib >=0.1.0-alpha.2` and mod id `auraskills` | `fabric/src/main/resources/fabric.mod.json` | present | Depends on `granitelib >=0.2.0-alpha.1`. Mod id stays `graniteskills`. | Jar metadata inspected after `remapJar`. | A server without the GraniteLib jar will refuse to load Granite Skills. |
| Startup, reload, shutdown | `FabricAuraSkills` menu hooks in `finishEnable` and reload | `FabricGraniteSkills` | present | Builds `FabricMenuRegistry`, loads built-in menu YAML, reloads the template registry on enable and on `/graniteskills reload`. Identity migration from the 2.3.18 commit stays in `finishEnable`. | Unit tests do not boot a world. Dedicated-server log is the startup check. | Reload of a live world was not clicked through in a client. |
| Text formatting | `fabric/text` on the feature branch | `fabric/src/main/java/dev/granite/graniteskills/fabric/text/` | present | Adventure `Component` and Minecraft `Component` are fully qualified in `FabricTextAdapter`. Codec is `ComponentSerialization.CODEC` with `RegistryOps.create`. | Fabric text tests included in the 159 fabric tests. | Message YAML for the new action-bar toggle strings was not added. |

### Menus

All eight menus live under `fabric/src/main/java/dev/granite/graniteskills/fabric/menu/`. Bundled templates are `fabric/src/main/resources/assets/graniteskills/granite/templates/*.yml`. Disk overrides are `config/graniteskills/granite/templates/`. Built-in layouts are `fabric/src/main/resources/menus/*.yml`.

| Menu | Current type | Status | Validation | Limitation |
| --- | --- | --- | --- | --- |
| Skills | `FabricSkillsMenuGui` | present, unverified in a client | Layout and placeholder tests in the fabric suite. `/skills` calls `FabricGraniteSkillsMenus.open`. | No Java client click-through. |
| Stats | `FabricStatsMenuGui` | present, unverified in a client | Same suite. `/stats` opens it. | Same. |
| Level progression | `FabricLevelProgressionMenuGui`, `FabricLevelProgressionLayout` | present, unverified in a client | `FabricLevelProgressionSprint118Test` asserts page size 24 and slots 45, 48, 49, 52, 53, with path slots 9 through 44. | The accepted 24-slot layout was not redrawn. |
| Stat info | `FabricStatInfoMenuGui` | present, unverified in a client | Component helper tests. | Custom model data uses the 26.3 list constructor. |
| Sources | `FabricSourcesMenuGui` | present, unverified in a client | `FabricSourcesRenderPlanTest` checks two sources do not share one slot. | Diagnose lists stay empty until rejected-source tracking is ported. |
| Abilities | `FabricAbilitiesMenuGui` | present, unverified in a client | Ability lore pipeline test. | No client. |
| Leaderboard | `FabricLeaderboardMenuGui` | present, unverified in a client | Template parse coverage in the suite. | No client. |
| Unclaimed items | `FabricUnclaimedItemsMenuGui` | present, unverified in a client | Merge helper compiled against 26.3 `CompoundTag`. | Claim flow not clicked. |

Template commands `/graniteskills templates status`, `export`, and `overwrite confirm` are in `FabricGraniteTemplateCommands`. Export refuses to replace an existing file unless `overwrite confirm` is used. That guard was not run against a live disk.

### Commands

Registration is `FabricBrigadierCommands.register`. The old chat-only `/skills` dump is gone. `/skills` opens the skills GUI for a player whose profile is loaded.

Roots, also mirrored under `/graniteskills` and under `/skills`, `/sk`, and `/skill` unless noted:

- `/skills`, `/sk`, `/skill`, `/stats`, `/openmenu`
- `/graniteskills help`, `version`, `reload`, `save`
- `xp view|add|set|remove` (not vanilla `/xp`)
- `mana view|add|set|remove`
- `skilllevel view|add|set|remove|setall|reset`
- `manaability resetcooldown`
- per-skill shortcuts (`/mining` and the other skill names) in `FabricSkillShortcutCommands`, each opening that skill's level-progression menu
- `top`, `skilltop`, `rank`, `skillrank`, `updateleaderboards`
- `multiplier`, `toggle`, `abtoggle`, `lang`
- `modifier`, `trait`
- `item`, `armor` (not vanilla `/item`)
- `claimitems`, `sources` (including `diagnose`), `resethealth`, `transfer`, `profile`, `backup`, `preset`, `storage`, `templates`, `antiafk`, `xpdiag`

Permissions stay `graniteskills.command.*`. Non-operator denial was unit-tested in `FabricCommandPermissionsTest` and was not confirmed with a non-op player on a server.

`/abtoggle` changes an in-memory idle mode (`SMART`, `ALWAYS`, `OFF`) on `User`. File and SQL storage do not read or write that mode, so a restart returns the player to `SMART`.

### Gameplay and HUD

26.3 gameplay that already shipped in 2.3.18 stays: agility, endurance, healing, fishing grappler, farming, enchanting, forging, alchemy, anti-AFK, the XP boss bar, and `FabricSkillsListeners`. Those classes were not replaced.

These 55 feature-baseline files are **missing**. They target Yarn 1.21.11 mixins or helpers that call those mixins. Copying them would not apply on 26.3, and several would register a second block-break or XP path beside the 26.3 handlers.

Helpers: `FabricAgilityResolutionProof`, `FabricAgilitySkill`, `FabricAnvilDiscount`, `FabricAnvilTakeContext`, `FabricBlockBreakPreCapture`, `FabricBlockXpSourceLookup`, `FabricCropInteractionHarvestXp`, `FabricCropPreUseCapture`, `FabricDoubleDrop`, `FabricEntityDamageAttribution`, `FabricEntityXpSourceLookup`, `FabricExperienceBonus`, `FabricExternalBlockBreakXp`, `FabricFarmingCropBreakAliases`, `FabricFarmingCropResolver`, `FabricFarmingVanillaBlocks`, `FabricFishingBobberUseContext`, `FabricHealthRegen`, `FabricPlayerDeathHandler`, `FabricStatisticSourceResolver`, `FabricXpLookupNormalize`, `FabricXpUserResolution`.

Loot: `FabricBlockLootHandler`, `FabricExcavationLootProvider`, `FabricLootCauses`, `FabricMobLootHandler`, `FabricUnsupportedEntityLootParser`, `FabricMobContext`, `FabricMobContextProvider`.

Mixins: `AnvilScreenHandlerAnvilDiscountMixin`, `BlockDropStacksMultiBreakMixin`, `BlockItemPlacedBlockMixin`, `BoneMealItemGrowthAuraMixin`, `FallingBlockEntityRegionMixin`, `FarmingAgeableRandomTickGrowthAuraMixin`, `ForgingScreenHandlerAnvilOnTakeMixin`, `HungerManagerHealthRegenMixin`, `LivingEntityAuraSkillsDamageMixin`, `LockableContainerBrewingStandSetStackMixin`, `PistonHandlerRegionMixin`, `PlayerEntityExperienceBonusMixin`, `ServerPlayerInteractionManagerCropPreUseMixin`, `ServerPlayerInteractionManagerTryBreakPreCaptureMixin`, `TreeFeatureGrowContextMixin`, `TreeFeatureSetBlockMixin`.

Region: `FabricPlacedBlockBreakHandler`, `FabricRegionBlockIntegrity`, `FabricRegionLifecycle`, `FabricRegionManager`, `FabricTreeGrowContext`. `FabricSpeedMine` still says player-placed block tracking is not on Fabric yet.

Traits and requirements: `FabricRequirementNodes`, `FabricAnvilDiscountTrait`, `FabricDoubleDropTrait`, `FabricExperienceBonusTrait`, `FabricHealthRegenTraits`.

`SkillManager` records source-file counts at load. The feature baseline's rejected-source map and embedded-fallback flag are not filled in, so `/sources diagnose` runs and prints empty rejection lists.

Action-bar idle mode is in memory only. The feature `ActionBarManager` persistence changes were not ported.

### Data and migration

| Feature | Current path | Status | Limitation |
| --- | --- | --- | --- |
| Identity migration of worlds, player files, SQL table names, and items | `IdentityMigrator`, `LegacySqlTables`, `FabricLegacyItemMigrator`, `NamespacedId` | present on the 2.3.18 base and kept | Not re-run against a disposable AuraSkills world in this pass. |
| Profile transfer | `FabricUserDataTransfer` | partial | Transfers `UserState` fields that the command copies. It is not a full profile copy, and it does not overwrite destination progress that the command treats as already present. Destructive tests were not run. |
| Offline player-data name rewrite | `FabricOfflinePlayerDataAuraStripping` | present as code | Rewritten to 26.3 `CompoundTag` and compressed NBT IO. Not run on a real playerdata directory. |
| Unclaimed item give | `FabricItemRegistry.addRegisteredItemToInventory` | present as code | Leftovers message the user. Not clicked in game. |

## Automated results

Command: `./gradlew -PfabricOnly=true :fabric:build :granitelib-fabric:build`

| Module | Tests | Failures |
| --- | --- | --- |
| fabric | 159 | 0 |
| granitelib-fabric | 35 | 0 |
| common | 5 | 0 |
| api | 2 | 0 |

`checkstyleMain` and `checkstyleTest` passed for fabric. `checkstyleMain` passed for common, api, and granitelib-fabric.

Dedicated-server startup uses Loom `./gradlew -PfabricOnly=true :fabric:runServer` against `fabric/run` with `eula=true`. That process loads the same compiled modules that produced the jars above. It is a disposable local world. Nothing was installed on a production host.

Startup on 14 Nov 2026 (UTC) reached `Done (0.300s)` with no error lines. The loader listed `granitelib 0.2.0-alpha.1` and `graniteskills 2.3.177`. The mod logged 11 skills, 327 sources, 9 stats, 17 traits, 21 message files, and 160 config options. It registered the four built-in menu ids `skills`, `stats`, `level_progression`, and `stat_info`. Sources, abilities, leaderboard, and unclaimed items open through their own helpers, matching the feature-baseline loader, and the template registry reported disk 0, bundled 8, failed 0. Identity migration logged that the data was already migrated and left the old `auraskills` folder in place. Brigadier registration logged `/skills`, `/sk`, `/skill`, and the Granite command families. From the server console, `graniteskills version` replied with the data folder `fabric/run/config/graniteskills`. No player joined, so GUI clicks, permissions for a non-operator, and Bedrock were not exercised.

## Startup hang in 2.3.177

`FabricPlayerLifecycle.migrateChunk` ran from Fabric's `ServerChunkEvents.CHUNK_LOAD`, which Fabric injects at the end of `ChunkStatusTasks.full`. The server thread was inside `ServerChunkCache.getChunk`, waiting for that same FULL task on `ServerChunkCache$MainThreadExecutor`. The callback then called `BlockEntity.setChanged()`, and `Level.blockEntityChanged` calls `getChunkAt` for the chunk that is still loading. On a chest that still had a loot table, `Container.getItem` unpacked the table, `LootTable.fill` called `setItem`, and Fabric's transfer mixin redirected that into `setChanged` as well. The watchdog dump `crash-2026-11-14_21.09.36-server.txt` (22:09 CET) shows the server thread parked in `managedBlock` with `FabricPlayerLifecycle.migrateChunk` (the 2.3.177 lines 80 and 81) on the stack, nested inside `ChunkStatusTasks.lambda$full$0`. A fresh world with no forced chunks reached `Done` because nothing re-entered `getChunk`. One `minecraft:forced` ticket at chunk 0,0 stalled the server until the 60-second watchdog.

2.3.178 still rewrites legacy item names. During `CHUNK_LOAD` it edits the stored stacks in place and calls `LevelChunk.markUnsaved()`, which only records the chunk for saving. Chests that still have a loot table are left closed so loot is not generated during load. Dropped items, item frames, and armor stands are scanned on `ServerTickEvents.END_SERVER_TICK` with `getChunkNow`, after the load task has finished. Player inventory migration on join is unchanged.

Disposable Loom server, same world, after the fix: first boot `Done (0.217s)` with zero persistent chunks; `forceload add 0 0` returned immediately; restart `Done (1.527s)` while loading 25 persistent chunks; a further restart with `online-mode=false` `Done (0.852s)` loading the same 25 chunks. No watchdog. `/skills` and XP were not run: the client could not create an OpenGL context, the console command is player-only, and there is no GameTest. The production mod pack was not installed on this disposable world. The production log already showed that pack reaches `Done` when Granite Skills is the only mod removed.

## Manual regression checklist

Do this on a disposable Fabric 26.3 world with both jars. Stop before any production copy.

1. Confirm the log lists mods `graniteskills` 2.3.178 and `granitelib` 0.2.0-alpha.1, then `Done`. A world with forced chunks must finish "Loading N persistent chunks" instead of sitting at 15%.
2. As an operator, run `/graniteskills version`, `/skills`, `/sk`, `/skill`, and `/stats`. Each skills alias must open the skills chest, not a chat dump.
3. Click a skill and confirm the level-progression page uses the 24-slot path (slots 9–44) and the control slots 45, 48, 49, 52, and 53. Page forward and back, then return to skills.
4. Open stats, one stat info page, abilities, sources, the leaderboard, and unclaimed items. Confirm back returns to the previous menu.
5. As a non-operator, confirm `/graniteskills reload` is denied and `/skills` is allowed when `graniteskills.command.skills` is granted.
6. Run `/graniteskills xp add`, `/graniteskills item`, and confirm vanilla `/xp` and `/item` still behave as vanilla.
7. Run `/abtoggle`, note the mode, restart the server, and confirm the mode is back to SMART. That gap is known.
8. Run `/graniteskills templates status`. Export to a fresh folder. Repeat export and confirm it refuses until `overwrite confirm`.
9. Join with a Java client and with a Bedrock client through Geyser and Floodgate. Repeat steps 2–4. This pass did not do that.
10. On a copy of an old AuraSkills world, confirm `config/auraskills` is copied to `config/graniteskills` and the old folder remains. Do not run storage merge or reset commands on the only copy of a world.

## What this restoration is not

The menu and command surface from 2.3.176 is on the 26.3 branch and the project builds. The 55 gameplay, loot, mixin, and region files above are not. Client and Bedrock checks are still pending. Do not treat this build as a finished gameplay restoration.
