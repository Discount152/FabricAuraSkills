# Fabric GraniteSkills — handoff packet

> **26.3 update:** the shipping mod is **Granite Skills** on **Minecraft 26.3** (Loader 0.20.1, Fabric API 0.166.2+26.3, Java 25, Loom 1.20.0-alpha.31, Gradle 9.9.1, Mojang names). The mod id and Java packages stay `graniteskills` / `dev.granite.graniteskills` so Granitelib and existing `config/graniteskills` data keep resolving. Sections below that still say 1.21.11 or Yarn describe the previous known-good tree, not the current toolchain.

This document is the **authoritative handoff** for the experimental **Fabric dedicated-server** port in this repository. It records **what was built**, **how it is wired**, **how to build and run it**, **gaps vs the Paper plugin**, and **prioritized next work**.

**Companion doc:** `fabric/PARITY_BACKLOG.md` (shorter, checklist-style gaps). This handoff goes deeper on process, file locations, and sequencing.

---

## 0. Product intent, deployment, and how to measure success

### 0.1 Deployment assumptions

| Assumption | Detail |
|------------|--------|
| **Runtime** | Dedicated **Fabric** server, **Minecraft 1.21.11**, **Java 21**. |
| **Distribution** | **Server-side mod** only (see `fabric.mod.json` server entrypoint); **vanilla clients** join without installing GraniteSkills. |
| **Stack** | No Bukkit/Paper/Spigot at runtime. **Fabric API** + bundled **[fabric-permissions-api](https://github.com/lucko/fabric-permissions-api)** (JiJ); optional **LuckPerms Fabric** (or any provider) for real permission nodes. |
| **Config** | Same YAML / `config.yml` family as Paper, rooted at `<server>/config/graniteskills/` via `FabricLoader`. |

### 0.2 Two success metrics (do not conflate them)

1. **Shipping bar (Fabric-first):** Stable progression on a Fabric server — correct load/save, enforced permissions when a provider is present, core XP sources trustworthy, no silent data loss, exploit resistance **for the sources you actually ship**. Paper is **not** the release definition.
2. **Reference axis (Paper):** The Bukkit module is the **feature inventory and behavioral baseline** for “what GraniteSkills can do.” Use it to find gaps and tests, not as a mandate to clone every integration.

**One-line summary:** *Paper is the regression baseline and feature checklist; Fabric stability and server-only correctness are the release bar.*

### 0.3 Definition of done — “v1 / public server” candidate

**Required before advertising the mod for serious public servers:**

- Join / load / save paths validated; shutdown and crash behavior understood (see §0.5).
- **Permissions:** Skill nodes (`graniteskills.skill.<skill>`), generic `hasPermission`, and MVP commands use **fabric-permissions-api** (see §8). Operators tighten with LuckPerms; do **not** rely on “everyone true” for admin-only actions beyond documented defaults.
- Core XP sources you claim to support behave correctly (block break, entity kill, combat damage, statistics, jumping, fishing including `loot_pool` when skill loot tables are loaded).
- Fishing mixin verified after each MC/Yarn bump.
- Usable **admin / player command** surface: Fabric ships **`/graniteskills version`**, **`/graniteskills skills`**, **`/graniteskills reload`**, **`/graniteskills save`** (see `fabric/FABRIC_QUICKSTART.md`). Full Paper `/skills` ACF tree remains deferred.

**Explicitly defer** unless a **named deployment** needs them: full Slate-style menus, full Paper soft-dependency stack (WorldGuard, Vault jobs, MythicMobs, …), full item/NBT modifier pipeline, fishing automation extras / open-water parity, full remaining mana-ability set (e.g. Terraform), **full Bukkit API event parity** on Fabric.

### 0.4 Anti-exploit and balance (first-class)

Exploit resistance is **core** for a skills mod, not polish. Track alongside levelers:

- Placed-block / crop-loop / automation farming (ties to block source parity).
- Mob farms, spawner tagging, damage/jump/stat **spam** (ties to entity/damage/stat/jumping handlers).
- Fishing automation edge cases (mixin ordering, junk/treasure category matching).

**Already in tree:** `AntiAfkManager` / `FabricAntiAfkManager` register checks from `common` — extend deliberately; do not assume they cover all exploits.

### 0.5 Persistence and data integrity (pointers)

Behavior lives primarily in **`common`** (`MigrationManager`, storage providers, `User` serialization). For handoff, record these **questions** when changing configs or skills:

- What happens if a skill is **removed** from YAML or renamed? (Test with a copy of production data.)
- Are migrations run on version bump (`MigrationManager`)?
- Save ordering on **stop** vs **async** disconnect (`FabricPlayerLifecycle`, `disable()`).

Do **not** assume parity with Paper’s file layout beyond what `FabricUser.worldNameCompat` documents.

### 0.6 Reassessment triggers (architecture)

Pause **parity expansion** and reassess design if any of the following hold:

- **Many** features depend on Fabric **stubs / no-op managers** (`FabricNoOpLootManager`, empty trait/ability registration, …) and stubs keep multiplying.
- **`common`** accumulates Bukkit-oriented branches or hacks solely for Fabric.
- **Mixins** become the default fix for missing abstractions (cost/complexity trend up).
- Cost of maintaining **Paper alignment** exceeds cost of a **Fabric-native** replacement for a subsystem.

This is a **governance** check, not a technical failure.

### 0.7 Platform seams — `common` is not neutral

Reusing **`common`** saves time but inherits **Bukkit-first** seams. Every Fabric **stub** (`FabricItemRegistryStub`, `FabricNoOpLootManager`, empty `FabricTraitManager.registerTraitImplementations`, …) marks a place where shared code expected **Paper-side** behavior. If stubs proliferate, plan a **clearer platform boundary** (portable core vs per-platform modules) rather than growing infinite shims.

---

## 1. Repository context

| Item | Detail |
|------|--------|
| **Project** | [Discount152/FabricAuraSkills](https://github.com/Discount152/FabricAuraSkills) — Granite Skills, with the Fabric module in this tree. |
| **Version** | `gradle.properties` → `projectVersion=2.3.12` (aligned with upstream release tagging intent). |
| **Goal of the port** | Reuse **`api`** + **`common`** (shared logic, YAML, storage, leveling math) on **Fabric 1.21.11** with a **dedicated-server** mod, without rewriting the core plugin brain. |

**Modules:**

| Module | Role |
|--------|------|
| `api` | Public API types (skills, sources, events, loot interfaces). |
| `common` | Shared implementation: `GraniteSkillsPlugin`, `LevelManager`, loaders, storage, rewards, etc. |
| `bukkit` | Paper/Bukkit plugin: event listeners, levelers, loot, menus, traits, hooks. |
| `api-bukkit` | Bukkit-facing API glue. |
| `fabric` | Fabric Loom mod: `FabricGraniteSkills` implements `GraniteSkillsPlugin`, bridges Minecraft/Fabric to `common`. |

---

## 2. Versions and toolchain (exact)

| Component | Value | Where defined |
|-----------|--------|----------------|
| **Gradle** | 8.14.2 | `gradle/wrapper/gradle-wrapper.properties` |
| **Java** | 21 | `fabric/build.gradle.kts` (`JavaLanguageVersion.of(21)`) |
| **Minecraft** | 1.21.11 | `fabric/build.gradle.kts` → `minecraft("com.mojang:minecraft:1.21.11")` |
| **Yarn mappings** | 1.21.11+build.4 v2 | `mappings("net.fabricmc:yarn:...")` |
| **Fabric Loader** | 0.19.1 | `modImplementation("net.fabricmc:fabric-loader:0.19.1")` |
| **Fabric API** | 0.140.2+1.21.11 | `modImplementation("net.fabricmc.fabric-api:...")` |
| **Loom** | 1.13.6 | `fabric/build.gradle.kts` plugin `fabric-loom` |

**Implication:** Any bump to MC/Yarn without updating mixin **`INVOKE` targets** can break runtime even when **compile** still passes. After version bumps, validate mixins against Yarn names and bytecode.

---

## 3. Gradle: multi-project layout and `fabricOnly`

**File:** `settings.gradle.kts`

- **Default:** includes `api`, `bukkit`, `common`, `api-bukkit`, `fabric` (full multiplatform repo).
- **Fabric-only mode:** pass **`-PfabricOnly=true`** (Gradle project property `fabricOnly`). Then only **`api`**, **`common`**, and **`fabric`** are included.

**Why it exists:** Configuring **Bukkit** pulls Shadow/Paper-classpath style work; a **corrupted Gradle instrumentation cache** (`transforms`) often fails during that configuration. Fabric-only mode lets you **compile and remap the mod** without touching Bukkit.

**Commands:**

```text
# Fabric JAR only (recommended when Bukkit/toolchain or cache is broken)
./gradlew -PfabricOnly=true :fabric:compileJava
./gradlew -PfabricOnly=true :fabric:build

# Full repo (Paper plugin + Fabric mod)
./gradlew :fabric:build
./gradlew build
```

**Repo helper scripts:**

| Script | Behavior |
|--------|----------|
| `run-fabric-compile.cmd` | Sets `GRADLE_USER_HOME=D:\gradle`, `TEMP`/`TMP` to `D:\gradle-temp`, runs `gradlew.bat -PfabricOnly=true :fabric:compileJava`, logs to `build-fabric-compile.log`. **Requires** `D:\gradle` (and temp dir) to exist or be creatable; if you rely on default `%USERPROFILE%\.gradle`, run `gradlew` directly instead. |
| `fix-gradle-transforms-cache.cmd` | Deletes `%USERPROFILE%\.gradle\caches\8.14.2\transforms` to fix `ExternalDependencyInstrumentingArtifactTransform` / `instrumented-*.jar` failures. Version folder must match your Gradle (8.14.2 here). |

**JVM / heap:** `gradle.properties` sets `org.gradle.jvmargs=-Xmx3g ...` so Loom/remapping has enough heap.

**Note:** Builds may report **Gradle 9 deprecation** and a **Problems** report warning: *invocation of `Task.project` at execution time* (e.g. tied to `:fabric:processResources`). Non-fatal today; track for Gradle 10 / configuration cache.

**Permissions library:** `fabric/build.gradle.kts` uses **`include(modImplementation("me.lucko:fabric-permissions-api:0.6.1"))`** so the API is **JiJ** in the GraniteSkills jar; `fabric.mod.json` declares **`fabric-permissions-api-v0`**.

---

## 4. Fabric mod identity and packaging

| Item | Value |
|------|--------|
| **Mod id** | `graniteskills` |
| **Entrypoint** | `fabric.mod.json` → `server`: `dev.granite.graniteskills.fabric.FabricGraniteSkillsMod` |
| **Mixins** | `graniteskills.mixins.json` → package `dev.granite.graniteskills.fabric.mixin` — see §6.2 for XP-related entries; full list: `BlockEntityBrewingCleanupMixin`, `BrewingStandBlockEntityMixin`, `MobSpawnerLogicMixin`, `EnchantmentScreenHandlerMixin`, `FishingBobberEntityMixin`, `ForgingScreenHandlerMixin`, `ScreenHandlerBrewingTakeoutXpMixin`, `ScreenHandlerGrindstoneXpMixin`, `LivingEntityConsumeMixin`, `PotionEntityMixin`, `ServerWorldSpawnerMixin`, `ServerPlayerEntitySharpHookMixin` |
| **Config / data folder** | `FabricLoader.getInstance().getConfigDir().resolve("graniteskills")` → typically `<server>/config/graniteskills/` |
| **Resources** | `fabric` `main` resources include **`common/src/main/resources`** via `fabric/build.gradle.kts` `sourceSets { main { resources { srcDir(...) } } }` — default configs and messages ship from **common** jars/resources as on Paper. |
| **Depends** | `fabric.mod.json`: `fabricloader >= 0.19.0`, `minecraft ~1.21.11`, `java >= 21`, `fabric-api *` |

**Output JARs** (after `:fabric:build`):

- `fabric/build/libs/GraniteSkills-<version>.jar` — install this on the Fabric **dedicated server** (plus Fabric API). The file was named `FabricGraniteSkills-*.jar` on 1.21.11.

---

## 5. Server bootstrap sequence (exact order)

Understanding this order matters when debugging “XP not firing” or “API null”.

### 5.1 `FabricGraniteSkillsMod` (`fabric/.../FabricGraniteSkillsMod.java`)

1. **`onInitializeServer()`** (early, before full server tick loop semantics):
   - `FabricGraniteSkillsCommands.registerEarly()` — registers Brigadier `/graniteskills version|skills`.
   - **`ServerLifecycleEvents.SERVER_STARTING`**: constructs **`FabricGraniteSkills(server)`**, `FabricGraniteSkills.setInstance(plugin)`, `plugin.enable()`.
   - **`ServerLifecycleEvents.SERVER_STOPPING`**: `disable()` on instance.

### 5.2 `FabricGraniteSkills.enable()` (`fabric/.../FabricGraniteSkills.java`)

Synchronous initialization of **common** subsystems: logger, message provider, registries, **`FabricConfigProvider`**, storage, migrations, world manager, backup, XP requirements loader type, leaderboard, UI, rewards, **`FabricLevelManager`**, anti-AFK, then:

- **`FabricPlayerLifecycle.register(this)`** — join/disconnect → load/save users (SQL delay option respected for load).

Then **`scheduler.executeSync(this::finishEnable)`** — **critical**: skills and XP handler registration run on the **server thread** in the next sync task.

### 5.3 `finishEnable()`

1. `loadSkills()` — `SkillLoader` + `StatLoader` (from YAML under config folder).
2. `levelManager.loadXpRequirements()` — **`FabricXpRequirementsLoader`**.
3. **`levelManager.registerFabricXpHandlers()`** — see §6 (does **not** register fishing explicitly).
4. Boss bar options load, rewards, **trait / ability / mana ability “registration”** (see §8 — traits/abilities still no-op; mana providers partially wired).
5. Leaderboards, stat temp modifier task, log line summarizing enabled XP paths.

**Invariant:** `FabricGraniteSkills.getInstance()` is non-null **after** `SERVER_STARTING` enable completes; mixins that run during early world load must still tolerate null if called before that point (fishing mixin checks `plugin == null`).

---

## 6. XP sources: Paper vs Fabric mapping

### 6.1 Paper (`BukkitLevelManager.registerLevelers()`)

**File:** `bukkit/src/main/java/dev/granite/graniteskills/bukkit/level/BukkitLevelManager.java`

Registers these **`SourceLeveler`** listeners with Bukkit:

1. `AnvilLeveler`
2. `BlockLeveler`
3. `BrewingLeveler`
4. `DamageLeveler`
5. `EnchantingLeveler`
6. `EntityLeveler`
7. `FishingLeveler`
8. `GrindstoneLeveler`
9. `ItemConsumeLeveler`
10. `JumpingLeveler`
11. `ManaAbilityUseLeveler`
12. `PotionSplashLeveler`
13. `StatisticLeveler`

### 6.2 Fabric (`FabricLevelManager.registerFabricXpHandlers()`)

**File:** `fabric/.../FabricLevelManager.java`

Explicit registrations:

| Handler | Mechanism |
|---------|-----------|
| `FabricBlockBreakHandler` | Fabric world/block events → block break XP (id-based; state predicates limited — see backlog). |
| `FabricEntityKillHandler` | Entity death → XP when player is killer (subset of `EntityLeveler` behavior). |
| `FabricCombatXpHandler` | Damage XP via `ServerLivingEntityEvents.AFTER_DAMAGE` + `FabricDamageCauseMapper`. |
| `FabricStatisticXpHandler` | Custom stats only (subset of `StatisticLeveler`). |
| `FabricJumpingXpHandler` | Jump detection (subset of `JumpingLeveler`). |

**Fishing:** **Not** registered here. Documented in Javadoc: implemented by **`FishingBobberEntityMixin`** calling **`FabricFishingXpHandler.onFishCaughtStat`** when vanilla increments **`Stats.FISH_CAUGHT`** during bobber `use`.

**Also mixin-driven (no `registerFabricXpHandlers` call):** **`LivingEntityConsumeMixin`** → **`FabricItemConsumeHandler`**; **`PotionEntityMixin`** → **`FabricPotionSplashHandler`**; **`ForgingScreenHandlerMixin`** → **`FabricAnvilXpHandler`**; **`ScreenHandlerGrindstoneXpMixin`** (grindstone-only branch on **`ScreenHandler.onSlotClick`**) → **`FabricGrindstoneXpHandler`**; **`EnchantmentScreenHandlerMixin`** → **`FabricEnchantingXpHandler`**; **`BrewingStandBlockEntityMixin`** → **`FabricBrewingXpHandler`** (`BREW` trigger only; see backlog for limits).

### 6.3 Not ported as Fabric handlers (no equivalent to listed Bukkit levelers)

- `ManaAbilityUseLeveler` — **`FabricManaAbilityUseXpHandler`** implements YAML matching; **providers that call it after activation** include `FabricReplenish`, `FabricSpeedMine`, `FabricLightningBlade`, and **Sharp Hook** (`ServerPlayerEntitySharpHookMixin` + `FabricSharpHook`). **Still missing:** Treecapitator, Terraform, Charged Shot, Absorption, and any Bukkit-only `ManaAbilityActivateEvent` parity.
- **Brewing** — **`BREW`** on stand **`craft`** (nearest open GUI); **`TAKEOUT`** via `FabricBrewingTakeoutTracker` + `ScreenHandlerBrewingTakeoutXpMixin`. **Not ported:** Paper stand **owner** metadata (place/open claim), hopper edge cases.
- Previously missing but **now implemented** (see above): `AnvilLeveler`, `GrindstoneLeveler`, `ItemConsumeLeveler`, `PotionSplashLeveler`, `EnchantingLeveler`, partial `BrewingLeveler`
- Full **`EntityLeveler`** (spawner tags, MythicMobs, all triggers)
- Full **`StatisticLeveler`** (block/item mined/used stat mapping)
- Full **`BlockLeveler`** (YAML `states` / `after_states`, `check_replace`)

---

## 7. Fishing implementation (deep dive)

### 7.1 Why not `PlayerFishEvent`?

Fabric has no Bukkit fish event. The port hooks **vanilla** behavior inside **`FishingBobberEntity#use`**.

### 7.2 `FishingBobberEntityMixin` (`fabric/.../mixin/FishingBobberEntityMixin.java`)

| Injection | Purpose |
|-----------|---------|
| **`@ModifyArgs`** on `ItemEntity` **5-arg** `<init>(World, double×3, ItemStack)` | Captures the **first** spawned item stack into **`FabricFishingCatchHolder`** (arg index **4** = `ItemStack`). |
| **`@ModifyArgs`** on `ItemEntity` **8-arg** `<init>(World, double×3, ItemStack, double×3)` | Same for alternate constructor (same arg index **4**). |
| **`@Redirect`** on `PlayerEntity.increaseStat(Stat, int)` inside `use` | After calling `player.increaseStat(stat, amount)`, if stat is **`FISH_CAUGHT`** and player is **`ServerPlayerEntity`**, calls **`FabricFishingXpHandler.onFishCaughtStat(...)`**. |
| **`@Inject` at `use` RETURN** | **`FabricFishingCatchHolder.clear()`** so thread-local state does not leak between uses. |

**Fragility:** `ordinal = 0` and exact **`INVOKE` descriptor** strings must match Yarn for **1.21.11**. If Mojang adds another `ItemEntity` construction before the “catch” in `use`, ordinals may need adjustment.

### 7.3 `FabricFishingCatchHolder` (`fabric/.../FabricFishingCatchHolder.java`)

- **`ThreadLocal<ItemStack>`** holds a **copy** of the pending catch stack between constructor args and stat increment.
- **`set` / `take` / `clear`** are **`public static`** so the mixin package can call them (different package from `dev.granite.graniteskills.fabric`).

### 7.4 `FabricFishingXpHandler` (`fabric/.../FabricFishingXpHandler.java`)

Mirrors **`FishingLeveler`** logic conceptually:

- Respects source enabled, creative disable option, user/skill permission, blocked/disabled worlds.
- Resolves item: prefers **`FabricFishingCatchHolder.take()`**; fallback **`bobber.getHookedEntity()`** if `ItemEntity`.
- Finds first matching **`FishingXpSource`** via **`FabricLootItemMatcher.passesLootFilter`** (not Bukkit `ItemRegistry`).
- **`levelManager.addXp(user, skill, source, source.getXp())`**.

### 7.5 `FabricLootItemMatcher` vs Bukkit `ItemRegistry.passesFilter`

**File:** `fabric/.../FabricLootItemMatcher.java`

- **Materials / exclusions / category:** `Registries.ITEM` id path vs YAML; **`FabricItemCategories`** for fishing junk/treasure style categories.
- **`loot_pool`:** If filter has `lootPool()`, matcher requires **`LootManager.getLootTable`**. Fabric uses **`FabricNoOpLootManager`** (always `null` table) → **pool-based fishing rows never match** (`return false` after null checks).
- **Custom model data (1.21.11):** Uses **`CustomModelDataComponent`**: `DEFAULT`, **`getColor(0)`** when config specifies an int CMD; treats non-default component as “has CMD” when filter forbids CMD. This maps Bukkit-era int CMD to the component API; edge cases may differ for packs using floats/strings only.

### 7.6 Paper-only fishing extras (not on Fabric)

- **`FishingLootHandler`** (extra drops, open water, etc.) — Bukkit `GraniteSkills.registerEvents()` registers it; **no Fabric equivalent** documented.
- Bukkit **`PlayerFishEvent.State.CAUGHT_FISH`** timing vs mixin-on-stat — should be equivalent for “caught item” XP but not byte-for-byte verified.

---

## 8. Other important Fabric classes (stubs vs real)

| Class | Role | Parity note |
|-------|------|-------------|
| `FabricApiProvider` | Exposes API implementation | **`getLootManager()`** → **`FabricNoOpLootManager`** |
| `FabricNoOpLootManager` | No loot tables | Blocks **loot_pool** fishing and any API relying on registered loot |
| `FabricItemRegistryStub` | `ItemRegistry` | Not Bukkit NBT/item pipeline; fishing uses **`FabricLootItemMatcher`** instead |
| `FabricModifierManager` | Modifiers | No-op vs Paper item/armor modifiers |
| `FabricMenuHelper` | Menus | Empty/stub — no Slate menus |
| `FabricUiProvider` / `FabricBossBarManager` | UI | Minimal vs Paper |
| `FabricUser.hasPermission` / `hasSkillPermission` | Permissions | Uses **fabric-permissions-api** (`Permissions.check`). **`hasSkillPermission`:** `graniteskills.skill.<skill>` with **default `true` if unset** (so servers without LuckPerms still gain XP; deny via provider). **`hasPermission`:** default **`false` if unset**. **`canSelectJob`:** `graniteskills.jobs.block.<skill key or id>` with default false. Install **LuckPerms Fabric** (recommended) for production. |
| `FabricTraitManager.registerTraitImplementations` | Traits | **Empty** — no HP, luck, movement, etc. from traits |
| `FabricAbilityManager.registerAbilityImplementations` | Abilities | **Empty** |
| `FabricManaAbilityManager.registerProviders` | Mana abilities | **Partial:** `FabricTimerCountdown`, `FabricReplenish`, `FabricSpeedMine`, `FabricLightningBlade`; Sharp Hook via mixin (not in this method) |
| `FabricEventHandler` | Common events | Present for API wiring; Bukkit fires many explicit events Fabric does not |
| `HookManager` | Soft-deps | Paper hooks (Vault, WG, PAPI, …) not registered on Fabric |

**Paper `GraniteSkills.registerEvents()`** (non-leveler) includes **`BlockLootHandler`**, **`FishingLootHandler`**, **`MobLootHandler`**, region listeners, armor, requirements, jobs, etc. — **largely absent on Fabric**. See `bukkit/.../GraniteSkills.java` around `registerEvents()`.

---

## 9. Commands

**Fabric:** Brigadier under `fabric/.../FabricGraniteSkillsCommands.java`:

- `/graniteskills version`
- `/graniteskills skills` (player-only, dumps skill levels)

**Paper:** Full ACF command tree + menus (`bukkit/.../menus/MenuRegistrar.java`, etc.).

---

## 10. Build verification checklist (post–cache wipe)

Performed successfully in a clean-cache audit:

1. `.\gradlew.bat -PfabricOnly=true :fabric:build --no-daemon` → **BUILD SUCCESSFUL** (includes `remapJar`).
2. `.\gradlew.bat build --no-daemon` → **BUILD SUCCESSFUL** (`api` tests, `bukkit` tests, `common` tests, `fabric` build).

**Runtime checklist** (manual — not automated here):

- Server starts with mod + Fabric API.
- Config folder populates under `config/graniteskills/`.
- Join, gain XP from a **known** source (e.g. break block, kill mob, fish).
- After any **MC/Yarn** upgrade: re-test fishing and mixin load (watch log for mixin apply errors).

---

## 11. Roadmap: Paper-aligned features (prioritized for Fabric)

Use the phases below as a **backlog ordered by gameplay value on Fabric**, not a promise of 1:1 Paper clone. **Paper class names** are anchors to find behavior in `bukkit/`.

**Phase D (integrations)** items are **demand-driven**: implement WorldGuard/Vault/jobs/Mythic/etc. **only** when a real Fabric deployment names them.

**Phase E (UI/menus)** and **Phase F (API events)** are **explicitly low priority** until core progression, permissions, persistence, and anti-exploit are solid. **Do not** sink major effort into Slate-style menus before that.

### Phase A — XP sources (levelers)

| Priority | Paper reference | Fabric goal |
|----------|-----------------|-------------|
| High | `FishingLeveler` + `FishingLootHandler` | Keep mixin approach; add **loot table loading** + **`FabricLootItemMatcher` pool support**; port open-water / bonus drop behavior if required by configs. |
| High | `BlockLeveler` + `BlockLevelerHelper` | Block **state** matching (`states` / `after_states` in YAML); **placed-block** / `check_replace` if configs depend on it. |
| High | `EntityLeveler` | Spawner detection, full trigger set, MythicMobs integration (if still in scope), parity with `EntityXpGainEvent`. |
| Medium | `StatisticLeveler` | Map Bukkit statistic IDs to Minecraft stats / scoreboard / criteria not yet wired. |
| Medium | `AnvilLeveler`, `EnchantingLeveler`, `GrindstoneLeveler` | ScreenHandler / anvil / grindstone / enchant events on Fabric API or mixins. |
| Medium | `BrewingLeveler` | Brewing stand tick / inventory change hooks. |
| Medium | `ItemConsumeLeveler` | `UseItemCallback` / finish-using-item semantics. |
| Medium | `PotionSplashLeveler` | Potion entity splash events. |
| Medium | `ManaAbilityUseLeveler` | Extend Fabric mana providers (Treecapitator, Terraform, Charged Shot, Absorption, …); several already call `FabricManaAbilityUseXpHandler`. |
| Ongoing | `DamageLeveler` | Already partial via `FabricCombatXpHandler`; align edge cases (shield, cooldowns, damager filters) with Paper. |

### Phase B — Gameplay systems (traits, abilities, mana)

| Paper reference | Fabric goal |
|-----------------|-------------|
| `BukkitTraitManager.registerTraitImplementations()` | Implement Fabric trait handlers (HP, regen, luck, damage, crit, movespeed, etc.) using Fabric/MC attribute and event APIs; register in **`FabricTraitManager.registerTraitImplementations()`**. |
| `BukkitAbilityManager` | Port ability implementations or equivalents. |
| `BukkitManaAbilityManager.registerProviders()` | Port mana abilities (`Replenish`, `Treecapitator`, …) with Fabric events/mixins. |

### Phase C — Items, modifiers, requirements

| Paper reference | Fabric goal |
|-----------------|-------------|
| `ItemListener`, `ArmorListener`, `RequirementListener` | Replace **`FabricItemRegistryStub`** / **`FabricModifierManager`** with real item/NBT or component-based checks; armor equip tracking. |
| `BukkitLootManager` + loot handlers | Replace **`FabricNoOpLootManager`** with loader compatible with `common` loot YAML; implement **`MobLootHandler`**, **`BlockLootHandler`** analogs. |

### Phase D — Regions, economy, permissions, integrations

| Paper reference | Fabric goal |
|-----------------|-------------|
| `RegionListener`, `RegionBlockListener` | WorldGuard equivalent or Fabric region API; document if intentionally unsupported. |
| `JobsListener`, economy hooks | Fabric economy / jobs mods or custom bridge. |
| `FabricUser` permission checks | **Done (baseline):** fabric-permissions-api + JiJ; recommend **LuckPerms Fabric** in production. Extend for job-limit permission enumeration if needed (see Bukkit `getPermissionJobLimit`). |
| Vault / PlaceholderAPI / ProtocolLib | N/A or mod-specific replacements; **defer** unless a deployment requires them. |

### Phase E — UI and commands

| Paper reference | Fabric goal |
|-----------------|-------------|
| `MenuRegistrar`, Slate menus | Port menus to a Fabric GUI library or vanilla-style screens; flesh out **`FabricMenuHelper`**. |
| `BukkitUiProvider`, boss bar, action bar | Complete **`FabricUiProvider`** / **`FabricBossBarManager`** / **`FabricActionBarManager`** to match Paper options. |
| ACF command tree | Port commands to Brigadier (or register a Fabric command library) beyond `/graniteskills version|skills`. |

### Phase F — API and events

| Goal | Notes |
|------|--------|
| Fire `EntityXpGainEvent`, `DamageXpGainEvent`, etc. from Fabric | **Low priority** until other mods consume these on Fabric. Paper fires from `BukkitLevelManager`; Fabric uses **`LevelManager.addXp`** without the same event surface. |
| `GraniteSkillsBukkit` / `api-bukkit` | Not applicable; ensure **`api`** surface is satisfied for mod consumers when/if needed. |

### Phase G — Engineering hygiene

| Task | Notes |
|------|--------|
| Mixin tests / game test | Optional: minimal dedicated server smoke or mixin verification after bumps. |
| Gradle deprecation | Resolve `Task.project` at execution time in `:fabric:processResources` when upgrading Loom/Gradle. |
| CI | Add jobs: `-PfabricOnly=true :fabric:build` + full `build` on stable agents. |

---

## 12. Quick file index (Fabric)

| Area | Path |
|------|------|
| Mod entry | `fabric/src/main/java/.../FabricGraniteSkillsMod.java` |
| Plugin core | `fabric/src/main/java/.../FabricGraniteSkills.java` |
| Level / XP registration | `fabric/src/main/java/.../FabricLevelManager.java` |
| Fishing mixin | `fabric/src/main/java/.../mixin/FishingBobberEntityMixin.java` |
| Mixin config | `fabric/src/main/resources/graniteskills.mixins.json` |
| Mod metadata | `fabric/src/main/resources/fabric.mod.json` |
| Build | `fabric/build.gradle.kts` |
| Parity shortlist | `fabric/PARITY_BACKLOG.md` |

---

## 13. Summary for the next owner

1. **The Fabric mod is a thin bridge:** most behavior still lives in **`common`**; Fabric supplies **events, mixins, commands, storage path, matchers, and permission checks**.
2. **It is not Paper-equivalent:** traits, abilities, **most** mana abilities, loot manager, menus, many levelers, and most integrations are **missing or stubbed** — documented above and in `fabric/PARITY_BACKLOG.md`.
3. **Permissions:** **fabric-permissions-api** is bundled; skill XP uses **allow-by-default** on skill nodes so vanilla Fabric without a provider still works — use **LuckPerms** (or similar) to deny nodes and to manage command nodes on serious servers.
4. **Fishing XP is mixin-based** and sensitive to **Yarn/Minecraft** changes; **loot_pool** fishing requires a **real `LootManager`** on Fabric.
5. **Builds:** use **`-PfabricOnly=true`** when Bukkit or Gradle transforms break; use **full `./gradlew build`** for release confidence.
6. **Roadmap:** use **Phases A–G** as a **value-ordered** backlog; **do not** treat Paper parity as the product mission — see **§0**. Prioritize **levelers + anti-exploit + persistence confidence**, then **traits/rewards**, then **integrations only on demand**, then **UI/events** last.

---

*Document generated for repository handoff. Update this file when major wiring or parity milestones change.*
