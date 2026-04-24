# Fabric AuraSkills — handoff packet

This document is the **authoritative handoff** for the **Java Fabric** dedicated-server mod in this repository. It records **what was built**, **how it is wired**, **how to build and run it**, **gaps vs the in-repo `bukkit` reference implementation** (capability inventory — **not** a second runtime), and **prioritized next work**. **Shipped product identity:** **Granite Skills** / **GraniteLib** — see **`fabric/PAPER_PARITY_PROGRAM.md`**.

**Companion docs:** `fabric/PARITY_BACKLOG.md` (gap list + Geyser/Floodgate), **`fabric/PAPER_PARITY_PROGRAM.md`** (capability + reference-baseline; **Fabric** runtime; **Java + Bedrock** via Geyser/Floodgate; **Granite** identity), **`fabric/RELEASE_GATES.md`** (runnable vs limited prod vs **release-grade** — **A / B / C**). This handoff goes deeper on process, file paths, and Gradle.

---

## 0. Product intent, deployment, and how to measure success

### 0.1 Deployment assumptions

| Assumption | Detail |
|------------|--------|
| **Runtime** | Dedicated **Java Fabric** server, **Minecraft 1.21.11**, **Java 21**. |
| **Distribution** | **Two** standalone server-side **Fabric** JARs — **GraniteLib** (`:granitelib-fabric`) and **Granite Skills** (`:fabric`) — installed together; see **`fabric/PAPER_PARITY_PROGRAM.md`** *Two-JAR completion* and **`fabric/RELEASE_GATES.md`**. |
| **Clients** | **Java** and **Bedrock** (via **Geyser** + **Floodgate** on the same server) join **without** client mods — server drives progression and UI. |
| **Stack** | No Bukkit/Paper/Spigot at runtime. **Fabric API** + bundled **[fabric-permissions-api](https://github.com/lucko/fabric-permissions-api)** (JiJ); optional **Fabric permission provider** in production for real allow/deny nodes. **Geyser** + **Floodgate** are **expected** in the supported deployment for cross-play; validate features on **both** player paths. |
| **Config** | Same YAML / `config.yml` family as the reference layout, rooted at `<server>/config/auraskills/` via `FabricLoader`. |

### 0.2 Product target and how to measure progress

**Primary engineering target (this repository):** a **Fabric-native** **Granite Skills** mod (with **GraniteLib**) with **equivalent** core **AuraSkills + Slate** **capabilities** to the **reference `bukkit` module** in this monorepo — see **`fabric/PAPER_PARITY_PROGRAM.md`**. The **`bukkit` module** is the **reference implementation and feature inventory**; production runs **Fabric only** (no Bukkit/Paper/Spigot at runtime — see §0.1). **Supported clients:** **Java** and **Bedrock via Geyser + Floodgate** (see §0.1). Third-party Bukkit/Paper **plugin ecosystems** are **out of default scope** — see **`fabric/PARITY_BACKLOG.md`**.

**Engineering axis (do not conflate with “we’re done”):** *Fabric stability* — load/save, permissions, no silent data loss, exploit resistance, mixin health after MC bumps — is tracked **alongside** reference-coverage work. Milestone releases can ship **intermediate** builds while gaps remain; the program goal is **full capability coverage on Fabric**, not “ship Bukkit.”

**API note:** `api-bukkit` describes the **reference**; a **Fabric-native** API for mod authors may be added. See **`PAPER_PARITY_PROGRAM.md` §1–2**.

### 0.3 Milestones (intermediate vs program complete)

**Intermediate milestone — “serious public Fabric server” (partial parity):** Join/load/save validated; **fabric-permissions-api** + a **permission provider** in production if you need deny/allow; core XP sources that are already ported behave; fishing mixin re-verified after MC/Yarn bumps; minimal **Brigadier** admin/player commands (see `fabric/MOD_COMMANDS.txt` / `fabric/FABRIC_QUICKSTART.md`) until the full tree lands. **Where feasible, spot-check** Bedrock (Geyser/Floodgate) for progression and chat/title surfaces.

**Program complete** — see **`fabric/PAPER_PARITY_PROGRAM.md` §5** and **two-JAR completion**: **GraniteLib** + **Granite Skills** are each **shippable**; together they match the **in-repo Bukkit reference** in **`fabric/RELEASE_GATES.md` Gate C**. Substantively: Slate-style menus and **full command capability** vs the **reference** tree (per **`fabric/MENU_AND_COMMANDS_PORT.md`**); traits/abilities/mana and core loot/block/mob/region behavior on **Fabric-native** handlers; **third-party Bukkit plugin ecosystems** remain **out of default scope** (see **`fabric/PARITY_BACKLOG.md`**); **final Granite** public command UX may still be a follow-up rebrand pass. **Acceptance** includes **Java and Bedrock-via-Geyser** for user-visible flows, or **explicit** documented limitations for Bedrock.

**Test / acceptance expectation:** do not treat “passes on Java only” as sufficient for **menus, commands, or formatted messages**; include **Geyser + Floodgate** in manual (or automated where possible) test plans.

Milestone checklists in older revisions of this file that suggested deferring **menus** unless a deployment needs them are **superseded** by **`PAPER_PARITY_PROGRAM.md`** for product intent; sequencing still follows **§11** below.

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

Do **not** assume the **reference (Bukkit)** world folder layout matches Fabric `FabricUser.worldNameCompat` — verify per host.

### 0.6 Reassessment triggers (architecture)

Pause **reference-coverage expansion** and reassess design if any of the following hold:

- **Many** features depend on Fabric **stubs / thin shims** (see §8; verify against `fabric/…` — **stale** stub lists are a sign to refresh this handoff) and technical debt keeps multiplying.
- **`common`** accumulates Bukkit-oriented branches or hacks solely for Fabric.
- **Mixins** become the default fix for missing abstractions (cost/complexity trend up).
- Cost of maintaining **lockstep with the Bukkit `reference` module** exceeds cost of a **Fabric-native** replacement for a subsystem.

This is a **governance** check, not a technical failure.

### 0.7 Platform seams — `common` is not neutral

Reusing **`common`** saves time but inherits **Bukkit-first** seams. Stubs in **`fabric/…`** (see §8) mark where the **reference** build had fuller behavior. If stubs proliferate, plan a **clearer platform boundary** (portable core vs per-platform modules) rather than growing infinite shims. **Not** a reason to reintroduce Bukkit at runtime.

---

## 1. Repository context

| Item | Detail |
|------|--------|
| **Upstream** | [Archy-X/AuraSkills](https://github.com/Archy-X/AuraSkills) — this tree is a fork/clone with an added **`fabric`** module. |
| **Version** | `gradle.properties` → `projectVersion` (e.g. **2.3.30** — aligned with upstream release tagging intent). |
| **Goal of the port** | Reuse **`api`** + **`common`** (shared logic, YAML, storage, leveling math) on **Fabric 1.21.11** with a **dedicated-server** mod, without rewriting the core plugin brain. |

**Modules:**

| Module | Role |
|--------|------|
| `api` | Public API types (skills, sources, events, loot interfaces). |
| `common` | Shared implementation: `AuraSkillsPlugin`, `LevelManager`, loaders, storage, rewards, etc. |
| `bukkit` | **Bukkit (reference) plugin** in this monorepo — listeners, levelers, loot, menus, traits, hooks. **Not** the Fabric server runtime. |
| `api-bukkit` | Bukkit-facing API glue. |
| `fabric` | Fabric Loom mod: `FabricAuraSkills` implements `AuraSkillsPlugin`, bridges Minecraft/Fabric to `common`. |

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

# Full monorepo build (Bukkit `reference` module + Fabric mod)
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

**Permissions library:** `fabric/build.gradle.kts` uses **`include(modImplementation("me.lucko:fabric-permissions-api:0.6.1"))`** so the API is **JiJ** in the AuraSkills jar; `fabric.mod.json` declares **`fabric-permissions-api-v0`**.

---

## 4. Fabric mod identity and packaging

| Item | Value |
|------|--------|
| **Mod id** | `auraskills` |
| **Entrypoint** | `fabric.mod.json` → `server`: `dev.aurelium.auraskills.fabric.FabricAuraSkillsMod` |
| **Mixins** | `auraskills.mixins.json` → package `dev.aurelium.auraskills.fabric.mixin` — see §6.2 for XP-related entries; full list: `BlockEntityBrewingCleanupMixin`, `BrewingStandBlockEntityMixin`, `MobSpawnerLogicMixin`, `EnchantmentScreenHandlerMixin`, `FishingBobberEntityMixin`, `ForgingScreenHandlerMixin`, `ScreenHandlerBrewingTakeoutXpMixin`, `ScreenHandlerGrindstoneXpMixin`, `LivingEntityConsumeMixin`, `PotionEntityMixin`, `ServerWorldSpawnerMixin`, `ServerPlayerEntitySharpHookMixin` |
| **Config / data folder** | `FabricLoader.getInstance().getConfigDir().resolve("auraskills")` → typically `<server>/config/auraskills/` |
| **Resources** | `fabric` `main` resources include **`common/src/main/resources`** via `fabric/build.gradle.kts` `sourceSets { main { resources { srcDir(...) } } }` — default configs and messages ship from **common** the same way as the **reference** Bukkit build. |
| **Depends** | `fabric.mod.json`: `fabricloader >= 0.19.0`, `minecraft ~1.21.11`, `java >= 21`, `fabric-api *` |

**Output JARs** (after `:fabric:build`):

- `fabric/build/libs/FabricAuraSkills-<version>.jar` — install this on the Fabric **dedicated server** (plus Fabric API).

---

## 5. Server bootstrap sequence (exact order)

Understanding this order matters when debugging “XP not firing” or “API null”.

### 5.1 `FabricAuraSkillsMod` (`fabric/.../FabricAuraSkillsMod.java`)

1. **`onInitializeServer()`** (early, before full server tick loop semantics):
   - `FabricAuraSkillsCommands.registerEarly()` — registers Brigadier `/auraskills version|skills`.
   - **`ServerLifecycleEvents.SERVER_STARTING`**: constructs **`FabricAuraSkills(server)`**, `FabricAuraSkills.setInstance(plugin)`, `plugin.enable()`.
   - **`ServerLifecycleEvents.SERVER_STOPPING`**: `disable()` on instance.

### 5.2 `FabricAuraSkills.enable()` (`fabric/.../FabricAuraSkills.java`)

Synchronous initialization of **common** subsystems: logger, message provider, registries, **`FabricConfigProvider`**, storage, migrations, world manager, backup, XP requirements loader type, leaderboard, UI, rewards, **`FabricLevelManager`**, anti-AFK, then:

- **`FabricPlayerLifecycle.register(this)`** — join/disconnect → load/save users (SQL delay option respected for load).

Then **`scheduler.executeSync(this::finishEnable)`** — **critical**: skills and XP handler registration run on the **server thread** in the next sync task.

### 5.3 `finishEnable()`

1. `loadSkills()` — `SkillLoader` + `StatLoader` (from YAML under config folder).
2. `levelManager.loadXpRequirements()` — **`FabricXpRequirementsLoader`**.
3. **`levelManager.registerFabricXpHandlers()`** — see §6 (does **not** register fishing explicitly).
4. Boss bar options load, rewards, **trait / ability / mana ability “registration”** (see §8 — traits via **`FabricTraitManager`**; **`EXPERIENCE_BONUS`** wired in Sprint 19 — see **`fabric/SPRINT_19_TRAIT_PARITY.md`**; remaining Bukkit-only trait classes still listed there; mana providers partially wired).
5. Leaderboards, stat temp modifier task, log line summarizing enabled XP paths.

**Invariant:** `FabricAuraSkills.getInstance()` is non-null **after** `SERVER_STARTING` enable completes; mixins that run during early world load must still tolerate null if called before that point (fishing mixin checks `plugin == null`).

---

## 6. XP sources: reference (`bukkit`) vs Fabric mapping

### 6.1 Reference: `BukkitLevelManager.registerLevelers()`

**File:** `bukkit/src/main/java/dev/aurelium/auraskills/bukkit/level/BukkitLevelManager.java`

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
| `FabricEntityKillHandler` | Entity death → `EntityXpSource` with `DEATH` trigger: `FabricEntityDamageAttribution` + `FabricEntityXpSourceLookup#forEntityDeathKill` + `FabricSpawnerMobTracker` (Sprint 07 hardening; subset of `EntityLeveler`). |
| `FabricBlockLootHandler` | Block break → roll skill `loot/*.yml` pools (Bukkit `BlockLootHandler`); `override_vanilla_loot` via `AFTER` item-entity clear near block, then custom drops. |
| `FabricPlayerDeathHandler` | Player death → optional skill / XP reset from `on_death.*` in `config.yml` (Bukkit `PlayerDeath` parity). |
| `FabricCombatXpHandler` | Damage XP via `ServerLivingEntityEvents.AFTER_DAMAGE` + `FabricDamageCauseMapper`. |
| `FabricStatisticXpHandler` | Bukkit `StatisticLeveler` timer + delta; **`FabricStatisticSourceResolver`** — `Stats.CUSTOM` (incl. `mob_kills` / `damage_dealt` / deaths / shield-damage), typed **`killed/`**, **`killed_by/`**, **`dropped/`**, plus mined/used/broken/picked_up (Sprint 10). |
| `FabricJumpingXpHandler` | End-tick jump impulse matching (Bukkit `JumpingLeveler`); **Sprint 13** — `shouldBlockXp`, spectator + climb skip. **Partial** vs `PlayerMoveEvent` + Bukkit `LevelerContext` location blocks. |

**Fishing:** **Not** registered here. Documented in Javadoc: implemented by **`FishingBobberEntityMixin`** calling **`FabricFishingXpHandler.onFishCaughtStat`** when vanilla increments **`Stats.FISH_CAUGHT`** during bobber `use`.

**Also mixin-driven (no `registerFabricXpHandlers` call):** **`LivingEntityConsumeMixin`** → **`FabricItemConsumeHandler`** (Sprint 17: **`onConsumeItemCommit`**, spectator, source-type gate, `consumeItem` = commit); **`PotionEntityMixin`** → **`FabricPotionSplashHandler`** (Sprint 18: empty-effect parity, spectator, `POTION_SPLASH` enabled, splash+lingering on same entity); **`ForgingScreenHandlerMixin`** → **`FabricAnvilXpHandler`** (Sprint 16: **`FabricAnvilTakeContext`** at `onTake` HEAD for output + level cost, spectator); **`ScreenHandlerGrindstoneXpMixin`** (grindstone-only branch on **`ScreenHandler.onSlotClick`**) → **`FabricGrindstoneXpHandler`** (Sprint 15: Bukkit **`failsClickChecks`**-style click gates, spectator); **`EnchantmentScreenHandlerMixin`** → **`FabricEnchantingXpHandler`** (Sprint 14: **`enchantmentPower`**-based level cost for `exp_requirement` unit, spectator skip); **`BrewingStandBlockEntityMixin`** → **`FabricBrewingXpHandler`** (`BREW` trigger only; see backlog for limits).

### 6.3 Not ported as Fabric handlers (no equivalent to listed Bukkit levelers)

- `ManaAbilityUseLeveler` — **`FabricManaAbilityUseXpHandler`**: YAML match on `ManaAbility` (same rules as Bukkit); **all** registered providers call it after mana use — `FabricReplenish`, `FabricSpeedMine`, `FabricLightningBlade`, `FabricTreecapitator`, `FabricTerraform`, `FabricChargedShot`, `FabricAbsorption`, **Sharp Hook** (`ServerPlayerEntitySharpHookMixin` + `FabricSharpHook`). **Sprint 12** — `isSourceEnabled(MANA_ABILITY_USE)` gate. **Not replicated:** Bukkit `ManaAbilityActivateEvent` **cancel** / modified `manaUsed` from plugins (no event bus on Fabric). Manual: `SPRINT_12_MANUAL_VALIDATION.md`.
- **Brewing (Sprint 11)** — **`BREW`** in `BrewingStandBlockEntity#craft` when YAML’s first matching row is `BREW`; **`FabricBrewingOwnerTracker`**: placer on `BlockItem#place` + **claim-if-absent** on `BrewingStandBlock#onUse` (Bukkit: place + `InventoryOpen` unclaimed). **Online owner only**; offline / hopper-only brew: no BREW XP (like Bukkit). **`TAKEOUT`:** `FabricBrewingTakeoutTracker` + `ScreenHandlerBrewingTakeoutXpMixin`; **`LockableContainerBrewingStandSetStackMixin`** clears `TAKEOUT` slot state when result slots 0–2 are **set empty** (e.g. hopper), so the next **manual** take is not mis-attributed. Hoppers do **not** award TAKEOUT XP (Bukkit: player `InventoryClickEvent` only). Manual: `SPRINT_11_MANUAL_VALIDATION.md`.
- Previously missing but **now implemented** (see above): `AnvilLeveler`, `GrindstoneLeveler`, `ItemConsumeLeveler`, `PotionSplashLeveler`, `EnchantingLeveler`, partial `BrewingLeveler`
- **`EntityLeveler`** — **partial** on Fabric: kill + damage paths + spawner tagging; **Sprint 07** closed attribution/cause gaps for death/mob loot; transform/split spawner inheritance + third-party mob plugins **out of default scope**
- Full **`StatisticLeveler`** 1:1 with only Bukkit `Statistic` enum names — **N/A** on Fabric: YAML can use **slash-typed** stats Bukkit’s enum could not; core aggregate + per-entity + dropped coverage is **in** (Sprint 10); any remaining Bukkit scoreboard / criteria edge cases: **Partial** (see `SPRINT_10_MANUAL_VALIDATION.md`).
- Full **`BlockLeveler`** — **state** matching is implemented (`FabricBlockStateMatcher`, `FabricBlockBreakHandler`); **`check_replace`** uses **`FabricRegionManager`** + placed-block NBT (Sprint 04) and **`FabricRegionBlockIntegrity`** + mixins for pistons, falling sand/gravel, `TreeFeature` grow-Y cleanup, and tall-plant / support follow-up (Sprint 05 — **`fabric/SPRINT_05_MANUAL_VALIDATION.md`**). Remaining gaps: config that relied on **external region plugins** is **out of default scope** (use `blocked_worlds` / ops); mushroom / non-`TreeFeature` grows; unreported edge cases.

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
- **`set` / `take` / `clear`** are **`public static`** so the mixin package can call them (different package from `dev.aurelium.auraskills.fabric`).

### 7.4 `FabricFishingXpHandler` (`fabric/.../FabricFishingXpHandler.java`)

Mirrors Bukkit event order: **`FishingLootHandler` @HIGH**-equivalent ( **`tryRollBonusLoot`** first), then **`FishingLeveler` @MONITOR** (re-resolve item, **`findFishingSource`**, **`FabricXpHandlerUtil.shouldBlockXp`**, `addXp`).

- Resolves item: prefers **`FabricFishingCatchHolder.take()`**; fallback **`bobber.getHookedEntity()`** if `ItemEntity`.
- **`SourceTypes.FISHING` enabled** check applies only to **base XP** (rare pool may still run with skill `Fishing` per Bukkit if loot table loads — **narrow** if both disabled, table may be empty).

### 7.5 `FabricLootItemMatcher` vs Bukkit `ItemRegistry.passesFilter`

**File:** `fabric/.../FabricLootItemMatcher.java`

- **Materials / exclusions / category:** `Registries.ITEM` id path vs YAML; **`FabricItemCategories`** for fishing junk/treasure style categories.
- **`loot_pool`:** If filter has `lootPool()`, matching uses **`plugin.getApiProvider().getLootManager().getLootTable(skill.getId())`** (see `FabricLootItemMatcher`, **`FabricApiLootBridge`** + **`FabricLootManager`**). If the skill’s loot table is **unloaded** or the pool is missing, the row does **not** match (`return false` after null checks) — re-check YAML load order vs reference.
- **Custom model data (1.21.11):** Uses **`CustomModelDataComponent`**: `DEFAULT`, **`getColor(0)`** when config specifies an int CMD; treats non-default component as “has CMD” when filter forbids CMD. This maps Bukkit-era int CMD to the component API; edge cases may differ for packs using floats/strings only.

### 7.6 Reference (Bukkit)-only fishing extras (not fully on Fabric)

- **`FishingLootHandler`** — Bukkit `LootDropEvent` cancel, **`getExpToDrop() == 0`** early return for **bonus** only, and **`EntityLoot`**: not replicated on Fabric (see `SPRINT_09_MANUAL_VALIDATION.md`).
- Bukkit **`PlayerFishEvent.State.CAUGHT_FISH`** timing vs **`Stats.FISH_CAUGHT`** in mixin — close enough for “successful reel”; not byte-for-byte.

---

## 8. Notable `fabric/…` classes (verify — table may trail the codebase)

**Cross-play (Geyser / Floodgate):** anything that shows **chat, action bar, title, boss bar, inventory menu, or command feedback** is a **user-visible** surface — validate **Java and Bedrock** client paths; track in **`fabric/PARITY_AUDIT_LEDGER_PHASE1.md`** and **`fabric/PARITY_BACKLOG.md`**.

| Class / area | Role | Reference comparison |
|--------------|------|------------------------|
| `FabricApiProvider` + `FabricApiLootBridge` | Exposes `LootManager` to API / matchers | Backs `FabricLootManager` (skill loot YAML); used by **`FabricLootItemMatcher`** for `loot_pool`. |
| `FabricLootManager` | Skill loot tables / loaders | **Reference:** `BukkitLootManager` — still compare block/mob **handler** coverage. |
| `FabricItemRegistry` | Custom items (registry path) | **Reference:** Bukkit NBT stack differs; edge cases: **`fabric/PARITY_BACKLOG.md`**. |
| `FabricModifierManager` | Item/armor modifiers (schedules) | **Reference:** Bukkit `ItemListener` / armor — may still differ. |
| `FabricMenuHelper` | Menus | **Sprint 27–33** — partial: format map + **`FabricMenuRegistry`/`FabricMenuLoaders`** + **`FabricBuiltInMenuConfig`** (list + **level_progression** / **stat_info** `granite`); full **Slate** merge/templates still **`MENU_AND_COMMANDS_PORT.md`**. **Bedrock:** validate chest GUIs + reload via Geyser. |
| `FabricUiProvider` / `FabricBossBarManager` | UI: titles, action bar, boss bar | **Reference:** Bukkit `BukkitUiProvider` options — **validate both client paths** for formatted text. |
| `FabricUser` (permissions) | `fabric-permissions-api`; Floodgate + LP | **Bedrock:** confirm permission nodes for linked accounts. See **`fabric/PARITY_BACKLOG.md`**. |
| `FabricTraitManager` / `FabricAbilityManager` / `FabricManaAbilityManager` | Register implementations | Many handlers exist — diff vs Bukkit; **not** “empty” in the sense of a blank class (verify `FabricAuraSkills#finishEnable`). |
| `FabricEventHandler` / `LevelManager` | Common events, XP | Bukkit still fires more explicit events; Fabric may use `addXp` without the same event API. |
| `HookManager` | Soft-dependencies (reference) | **Reference** may register optional hooks; default Granite scope is **in-repo** behavior on Fabric, not third-party plugin portfolios. |

**Bukkit `AuraSkills#registerEvents()`** (non-leveler) lists **`BlockLootHandler`**, **`FishingLootHandler`**, **`MobLootHandler`**, region listeners, armor, requirements, and **optional** reference-only listeners. **Fabric** covers the **in-scope** subset via handlers/mixins — **gaps** live in **`PARITY_BACKLOG.md`** (and *Out of scope* for non-Granite ecosystems). Reconcile this table when **`fabric/…` changes significantly.

---

## 9. Commands

**Fabric (current):** Brigadier in `fabric/.../FabricAuraSkillsCommands.java` — see **`fabric/MOD_COMMANDS.txt`** (e.g. root `/auraskills` usage, `/auraskills help`, `/auraskills version|skills|reload|save`, top-level `/skills` alias). This is a **partial** surface until the **reference command/menu capability** program in **`PAPER_PARITY_PROGRAM.md`** delivers the full tree (on **Fabric**; final **Granite** public names **TBD**).

**Reference (Bukkit module):** Full ACF command tree in `bukkit/.../commands/CommandRegistrar.java` and related classes, plus menus via `MenuRegistrar` — the **capability inventory** to implement on Fabric. **Planned delivery:** **`fabric/MENU_AND_COMMANDS_PORT.md`** (Slate on Fabric + ACF Brigadier) as part of that program.

---

## 10. Build verification checklist (post–cache wipe)

Performed successfully in a clean-cache audit:

1. `.\gradlew.bat -PfabricOnly=true :fabric:build --no-daemon` → **BUILD SUCCESSFUL** (includes `remapJar`).
2. `.\gradlew.bat build --no-daemon` → **BUILD SUCCESSFUL** (`api` tests, `bukkit` tests, `common` tests, `fabric` build).

**Runtime checklist** (manual — not automated here):

- Server starts with mod + Fabric API.
- Config folder populates under `config/auraskills/`.
- Join, gain XP from a **known** source (e.g. break block, kill mob, fish).
- With **Geyser + Floodgate** enabled: a **Bedrock** join can progress skills and see **chat / action bar / title** surfaces (or document gaps).
- After any **MC/Yarn** upgrade: re-test fishing and mixin load (watch log for mixin apply errors).

---

## 11. Roadmap: Reference-baseline features on Fabric (sequenced)

**Product target:** **Fabric-native** **Granite Skills** with **equivalent capability** to the **`bukkit` reference** — see **`fabric/PAPER_PARITY_PROGRAM.md`**. Phases order work by **dependency and risk** (levelers, persistence, anti-exploit before large UI). Phase D/E (**requirements/permissions** where in-repo, **Slate** + full commands) are **in scope**; they are **sequenced**, not optional. Third-party plugin ecosystems: **out of default scope** per **`PARITY_BACKLOG.md`**.

**Class names** in the tables point at the **reference** implementation under `bukkit/`.

### Phase A — XP sources (levelers)

| Priority | Bukkit / reference (audit source) | Fabric (runtime) goal |
|----------|-----------------|-------------|
| High | `FishingLeveler` + `FishingLootHandler` | **Sprint 09** — bonus loot / open-water / pool-`else` and HIGH-before-MONITOR order on Fabric; Bukkit `LootDropEvent` / `EntityLoot` / `getExpToDrop` gate: optional / follow-up. |
| High | `BlockLeveler` + `BlockLevelerHelper` | Block **state** matching (`states` / `after_states` in YAML); **placed-block** / `check_replace` if configs depend on it. |
| High | `EntityLeveler` | Spawner detection (mixins + `FabricSpawnerMobTracker`); kill/damage triggers + attribution (**Sprint 07**). Remaining: transform/split spawner tag copy, `EntityXpGainEvent`-class API, full reference edge cases. Third-party mob plugins: **out of default scope**. |
| Medium | `StatisticLeveler` | **Sprint 10** — `FabricStatisticSourceResolver` + `FabricStatisticXpHandler`: aggregate + per-entity + dropped; Bukkit-enum-only config still maps via `custom/<name>`. Residual: strict enum-name-only parity, scoreboard criteria if any; **Partial** (manual `SPRINT_10_MANUAL_VALIDATION.md`). |
| Medium | `AnvilLeveler`, `EnchantingLeveler`, `GrindstoneLeveler` | **Anvil (Sprint 16)** — `FabricAnvilTakeContext` + `onTake`/`onTakeOutput`; **Enchanting (Sprint 14)** — `enchantmentPower` for level-cost unit, spectator; **Grindstone (Sprint 15)** — `allowsGrindstoneClickForXp` + server-only `onSlotClick` track; **Partial** (Bukkit event cancel, WG-style region API). |
| Medium | `BrewingLeveler` | **Sprint 11** — `BREW` owner + `TAKEOUT` + hopper **stale-state** clear on `setStack`; TAKEOUT XP still only on **player** GUI. **Partial** (optional edge cases: external automation mods). |
| Medium | `ItemConsumeLeveler` | **Sprint 17** — `LivingEntity#consumeItem` (commit) + javadoc; **Partial** (Bukkit event cancel, region) vs `shouldBlockXp` |
| Medium | `PotionSplashLeveler` | **Sprint 18** — `FabricPotionSplashHandler#lacksPotionEffectForParity` + gates; **Partial** (Bukkit `ProjectileHitEvent` cancel, region) vs `shouldBlockXp` |
| Medium | `ManaAbilityUseLeveler` | **Sprint 12** — provider inventory complete + source-type gate; Bukkit `ManaAbilityActivateEvent` **cancel** = **N/A** on Fabric. **Partial** (event API / plugin rewrites). |
| Ongoing | `DamageLeveler` | **Sprint 08** aligned `FabricCombatXpHandler` to reference `DamageLeveler` (order, shield, `ThrownItemEntity` damagers) + `EntityLeveler` `DAMAGE` resolver / `forEntityDamageOnly` lookup. Remaining: Bukkit `DamageListener` **cancel** / **last-damage** correction (optional config) if product requires it. |

### Phase B — Gameplay systems (traits, abilities, mana)

| Bukkit / reference | Fabric (runtime) goal |
|-----------------|-------------|
| `BukkitTraitManager.registerTraitImplementations()` | **Sprint 19–22:** all reference trait groups on Fabric, including core **`DoubleDropTrait`** — **`SPRINT_22_DOUBLE_DROP_PARITY.md`**. Optional Bukkit **WorldGuard** / **`LootDropEvent`** — out of default Granite scope. |
| `BukkitAbilityManager` | **Sprint 23–26** — 14/14 `*Abilities` groups; farming `Geneticist` + `GROWTH_AURA` (S25 bonemeal + bamboo/vines — **`SPRINT_25_FARMING_GROWTH_AURA.md`**) + **`SPRINT_26_ABILITY_PROOF_MATRIX.md`** (per-family status + `AS-019` justification). |
| `BukkitManaAbilityManager.registerProviders()` | Port mana abilities (`Replenish`, `Treecapitator`, …) with Fabric events/mixins. |

### Phase C — Items, modifiers, requirements

| Bukkit / reference | Fabric (runtime) goal |
|-----------------|-------------|
| `ItemListener`, `ArmorListener`, `RequirementListener` | Harden `FabricItemRegistry` / `FabricModifierManager` vs **Bukkit** item/armor behavior; armor equip and requirements (see `PARITY_BACKLOG`). |
| `BukkitLootManager` + block/mob **loot** handlers | **`FabricBlockLootHandler`** (block) + **`FabricMobLootHandler`** (mob) + **`FabricLootManager`**; **`FabricMobContextProvider`**; **`FabricRegionManager`** for block **`check_replace`**. Gaps: no Bukkit `LootDropEvent` cancel; plugin-only loot gating **out of default scope** (see ledger **`EVT-005`–`007`**, sprints 02–04). |

### Phase D — Requirements, permissions, and reference-only hooks

| Bukkit / reference | Fabric (runtime) goal |
|-----------------|-------------|
| `RegionListener`, region requirements, `RegionBlockListener` | **In-repo** behavior: `FabricRegionManager`, `FabricPlacedBlockBreakHandler`, `FabricRegionBlockIntegrity`, etc. Third-party “region mod” bridges: **out of default scope** (see **`PARITY_BACKLOG.md`**). |
| `JobsListener` / reference jobs + economy | **Out of default Granite scope** — not a planned integration surface here. |
| `FabricUser` permission checks | **Done (baseline):** fabric-permissions-api + JiJ; use a **Fabric permission provider** in production for real deny/allow. |
| Optional reference hooks (chat/protocol/placeholders) | **Out of default scope** unless the owner re-opens; prefer **Fabric-native** text surfaces and document gaps. |

### Phase E — UI and commands

| Bukkit / reference | Fabric (runtime) goal |
|-----------------|-------------|
| `MenuRegistrar`, Slate menus | **Sprint 28–32** — built-in `menus/*.yml` **granite** slice + `FabricMenuRegistry` + click detail flows; full **Slate** `loadMenus` / merge dirs still in flight. **Acceptance:** **Java + Bedrock (Geyser/Floodgate)** for inventory UX + reload. |
| `BukkitUiProvider`, boss bar, action bar | Complete **`FabricUiProvider`** / **`FabricBossBarManager`** / (if present) **action bar** paths to **reference** options. **Validate** text surfaces on **both** client paths. |
| ACF command tree | Port to Brigadier / **acf-brigadier** ( **`MENU_AND_COMMANDS_PORT.md`**) — capability vs **Bukkit** `CommandRegistrar`, **Granite** names **TBD**. **Cross-play:** command feedback for **Java and Bedrock**. |

### Phase F — API and events

| Goal | Notes |
|------|--------|
| Fire `EntityXpGainEvent`, `DamageXpGainEvent`, etc. from Fabric | **Low priority** until other mods consume these on **Fabric**. **Bukkit** `BukkitLevelManager` fires from plugin path; Fabric uses **`LevelManager.addXp`** without the same Bukkit event surface. |
| `AuraSkillsBukkit` / `api-bukkit` | Not applicable; ensure **`api`** surface is satisfied for mod consumers when/if needed. |

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
| Mod entry | `fabric/src/main/java/.../FabricAuraSkillsMod.java` |
| Plugin core | `fabric/src/main/java/.../FabricAuraSkills.java` |
| Level / XP registration | `fabric/src/main/java/.../FabricLevelManager.java` |
| Fishing mixin | `fabric/src/main/java/.../mixin/FishingBobberEntityMixin.java` |
| Mixin config | `fabric/src/main/resources/auraskills.mixins.json` |
| Mod metadata | `fabric/src/main/resources/fabric.mod.json` |
| Build | `fabric/build.gradle.kts` |
| Parity shortlist | `fabric/PARITY_BACKLOG.md` |

---

## 13. Summary for the next owner

1. **The Fabric mod is a thin bridge:** most behavior still lives in **`common`**; Fabric supplies **events, mixins, commands, storage path, matchers, and permission checks**. **Product target:** **Fabric-native Granite Skills** for **Java + Bedrock (Geyser/Floodgate)** with **reference-level capability** — see **`fabric/PAPER_PARITY_PROGRAM.md`**.
2. **Gaps until the program is complete:** many **reference-only** listeners, full command/menu surface, and third-party hooks are still **partial or missing** on Fabric — `fabric/PARITY_BACKLOG.md` and **§4–5** in this file.
3. **Permissions:** **fabric-permissions-api** is bundled; skill XP uses **allow-by-default** on skill nodes so **vanilla** Fabric (no provider) still works — use a **Fabric permission provider** to deny nodes and to manage command nodes; test **Floodgate-linked** Bedrock if you rely on per-player nodes.
4. **Fishing XP is mixin-based** and sensitive to **Yarn** changes; keep **loot** and **loot_pool** behavior aligned with **Bukkit** fish/loot paths as work continues.
5. **Builds:** use **`-PfabricOnly=true`** when the **Bukkit** `reference` toolchain or Gradle transforms break; use **full `./gradlew build`** for release confidence.
6. **Roadmap:** use **Phases A–G** with **sequencing** from **§11**; see **`fabric/PAPER_PARITY_PROGRAM.md`**. Prioritize **levelers + anti-exploit + persistence** first, then **traits/rewards/loot depth**, then **in-repo requirements + UI/commands**; **user-visible** items need **Java + Bedrock** acceptance per §0.1 / `PARITY_BACKLOG`.

---

*Document generated for repository handoff. Update this file when major wiring or parity milestones change.*
