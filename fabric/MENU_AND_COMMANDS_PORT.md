# Menus + commands on Fabric (reference: Bukkit ACF + Slate) — **Path A: full Slate port** (locked plan)

**Runtime:** **Fabric** only. This work implements **Fabric-native** menus and commands. The **reference** for *what* to cover is the **existing `bukkit` module** (ACF command tree, Slate menus) — **not** a goal of shipping Bukkit or Paper. **Final public** command roots, aliases, and **Granite Skills** copy are **TBD**; see **[PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md)**. This path is a **required** program track, not a side quest.

**Cross-platform acceptance:** Menus and commands are **high risk** for **Java vs Bedrock** differences. The shipped mod must remain usable for **Bedrock players joining through Geyser + Floodgate** on the Fabric server — not only for direct **Java** clients. Do **not** assume Java-only inventory packets, click semantics, or chat UX; **validate** (or **document** known gaps) for the Bedrock path. See **§Geyser / Bedrock** in [PARITY_BACKLOG.md](PARITY_BACKLOG.md).

This document is the **engineering plan** for shipping **menus and the full command tree together** (one release goal), by **porting Slate** to Fabric instead of writing a second menu engine.

Work still lands in **multiple commits/PRs**; “one big push” means **one coordinated program**, not a single patch.

---

## Locked decisions

| Topic | Choice |
|--------|--------|
| Menus | **Path A — full Slate port** (same engine concepts as Bukkit Slate, implemented for Fabric / `net.minecraft`). **Acceptance** includes **Java + Bedrock (Geyser/Floodgate)** where UIs can diverge. |
| Commands | **Same capability coverage** as the **reference** ACF tree in the `bukkit` module, implemented on **Fabric** using **ACF Brigadier** (`co.aikar:acf-brigadier`) where practical, from **`CommandRegistrationCallback`**. Final **Granite Skills** public names/permissions: **TBD**; deliver **equivalent** admin/player capabilities first. **Validate** command feedback for **both** client paths. |
| Delivery | **Menus + commands** ship in the **same release wave** (no “commands only” release first). |

---

## Where Slate source actually lives

Slate is **not only a Maven jar** — it is an open-source project:

- **https://github.com/Archy-X/Slate**  
- Bukkit AuraSkills depends on **`dev.aurelium:slate:1.2.2`** (see `api-bukkit/build.gradle.kts`).

**Porting Slate properly** means editing **that codebase** (or a fork of it), adding a **Fabric backend** (screen handlers, `ItemStack`, `Text`, scheduler, click events), and publishing something like **`dev.aurelium:slate-fabric`** — *or* vendoring the ported sources into this monorepo. Keeping the port **next to upstream Slate** reduces drift when Slate gets bugfixes.

---

## What AuraSkills uses from Slate today (scope hint)

AuraSkills Bukkit touches many Slate packages. A Fabric port must cover at least these **namespaces** (from `import dev.aurelium.slate…` usage in this repo):

- **Core:** `dev.aurelium.slate.Slate`, `…menu.LoadedMenu`, `…menu.ActiveMenu`, `…builder.BuiltMenu`, `…builder.MenuBuilder`, `…builder.TemplateBuilder`, `…builder.ItemBuilder`
- **Inventory / clicks:** `…inv.InventoryManager`, `…inv.SmartInventory`, `…inv.ClickableItem`, `…inv.content.*`, `…action.trigger.ClickTrigger`
- **Layout:** `…position.*`, `…inv.content.SlotPos`
- **Contexts / templates:** `…context.*`, `…info.*`, `…item.MenuItem`, `…item.TemplateItem`, `…item.provider.*`, `…function.*`
- **Text / options:** `…text.TextFormatter`, `…option.SlateOptions*`, `…scheduler.Scheduler`
- **Util:** `…util.SkullCreator`, `…util.VersionUtil`

That is **the real port surface** — not just “open a chest and set items.”

---

## Commands (reference ACF → Fabric)

- **Reference aliases (Bukkit `CommandRegistrar`):** `skills|sk|skill` (as `%skills_alias%`). **Final Granite** roots/aliases: **TBD**; use reference for **capability** coverage.
- **Coverage target:** `bukkit/.../CommandRegistrar.java` + every registered class (`SkillsRootCommand`, `StatsCommand`, `SkillCommand`, `XpCommand`, …) — **Fabric-native** handlers.
- **Permissions:** reference uses `plugin.yml` / `auraskills.command.*` — treat as the **compatibility** baseline until the **Granite** public permission / node naming pass.
- **Implementation:** **`acf-brigadier`** + Fabric registration bridge; enable ACF unstable APIs as required (reference: `enableUnstableAPI("help")` on Bukkit).

---

## FabricAuraSkills module wiring (after Slate-Fabric exists)

1. **Dependency:** `:fabric` depends on **`slate-fabric`** (or the published Maven coordinate).
2. **Resources:** ship **`menus/*.yml`** inside the Fabric jar (copy from `bukkit/src/main/resources/menus/` via Gradle `processResources`, or move menus to **`common`** once both platforms read the same files).
3. **Bootstrap:** on server start, construct **Fabric Slate** with the same options pattern as `MenuOptions` / `MenuRegistrar` on Bukkit (`AuraSkills.java` + `MenuRegistrar`).
4. **Replace stubs:**  
   - Replace **`FabricMenuHelper`** with a real implementation that delegates to Slate (same role as **`SlateMenuHelper`** on Bukkit).  
   - Replace **`FabricAuraSkillsCommands`** piecemeal: root **`/skills`** tree from ACF; keep **`/auraskills`** only if you still want a dev alias, or delegate to the same handlers.
5. **API:** port **`ApiMenuManager`** / Bukkit menu entry points that other code calls.

---

## Suggested execution order (inside the “one big push”)

1. **Submodule or fork** Slate at a pinned commit; create **`slate-fabric`** module in that repo (or in this monorepo under `slate-fabric/`).
2. **Layer map:** Bukkit type → Fabric type (`Player` → `ServerPlayerEntity`, `Inventory` → `ScreenHandler` + syncId, `ItemStack` → MC registry stack, `Component` / MiniMessage → Adventure `Text` / Fabric `Text`).
3. **Minimal vertical slice:** load **one** YAML menu (`skills`) end-to-end (open, click, close) on a dev server.
4. **Fill Slate subsystems:** scheduler, inventory manager, click routing, templates, placeholders, paging.
5. **Port AuraSkills-specific menu code** from `bukkit/.../menus/**` (context providers, `MenuRegistrar`, `UnclaimedItemsMenu`, etc.) to **`fabric/.../menus/**`**, calling the same Slate APIs now backed by Fabric.
6. **Wire ACF Brigadier** and register the full command tree; migrate `/auraskills` subcommands or alias them.
7. **Docs:** update **`fabric/PLAYER_GUIDE.md`**, **`fabric/PARITY_BACKLOG.md`**, **`fabric/FABRIC_QUICKSTART.md`**.

---

## What we are *not* doing

- **Path B** (custom YAML renderer without Slate) is **out of scope** for this initiative.
- There is still **no** separate CurseForge “Slate for Fabric” mod to install; the port is **your code** (Slate + AuraSkills Fabric).

---

## Slate submodule (in this repo)

Upstream Bukkit Slate is vendored as a **git submodule**:

```text
vendor/Slate   →   https://github.com/Archy-X/Slate.git
```

After clone, init submodules:

```text
git submodule update --init --recursive
```

The **`slate-fabric`** Gradle module is **this repo’s** Fabric port skeleton; it is **not** inside the submodule. Port work reads **`vendor/Slate`** and implements APIs under **`slate-fabric/src/main/java/...`**.

---

*When the first `slate-fabric` artifact builds and opens `skills.yml` on a Fabric dev server, update this file with the exact coordinates and any API renames.*

---

## Sprint 27 — Skills menu vertical slice (shipped, partial)

| Piece | Where |
|-------|--------|
| **6×9 container** (vanilla `GENERIC_9X6` — Geyser-friendly large chest) | **`granitelib`**: `GraniteClickable9x6Handler` (`GraniteTopSlotClickHandler` optional; `null` = read-only top), `GraniteContainerMenus#openReadOnly9x6` / `#openClickable9x6` |
| **Skills data + book rows** | **`FabricSkillsMenuGui`** — `/skills` and `/auraskills skills` (temporary; **not** final Granite public command design) |
| **MenuHelper / StatReward** | **`FabricMenuHelper`** — at least `level_progression` / `stat_reward_entry` string |
| **Manual** | **`SPRINT_27_MANUAL_SKILLS_MENU.md`** |

**Not in Sprint 27:** YAML `menus/skills.yml` load, Slate `MenuBuilder`, contexts, `OpenMenuCommand`, other menus. Next slices stack on `GraniteContainerMenus` and shared menu registry when introduced.

## Sprint 28 — Stats menu + `FabricAuraSkillsMenus` (shipped, partial)

| Piece | Where |
|-------|--------|
| **Shared routing** | **`FabricAuraSkillsMenus`** + **`FabricAuraSkillsMenus.Id` (`SKILLS`, `STATS`)** — all menu opens go through one entry point |
| **Stats GUI** | **`FabricStatsMenuGui`** — books per **enabled** stat; name from `stats.player_stat_entry`; trait sub-lines from `stats.trait_entry` + `TraitManager#getMenuDisplay` (same spirit as Bukkit `StatsMenu`) |
| **Temporary open** | `/stats`, `/auraskills stats` — permission **`auraskills.command.stats`**. **Skills** unchanged: `/skills`, `/auraskills skills` via same router |
| **Manual** | **`SPRINT_28_MANUAL_GRANITE_MENUS.md`** (Skills + Stats, Java + Bedrock) |

**Not in Sprint 28:** `stat_info` click, full Slate, YAML load from `menus/`.

## Sprint 29 — Menu registry + loading scaffold (shipped, partial)

| Piece | Where |
|-------|--------|
| **Registry** | **`FabricMenuRegistry`** — `register` / `get` / `open(id,…)` / `menuIds()` |
| **Definitions** | **`FabricMenuDefinition`** (`id`, `titleTranslationKey`, `FabricMenuOpener`) — code-built, not YAML |
| **Loader** | **`FabricMenuLoaders#registerBuiltInMenus`** — single path that registers `skills` + `stats`; logs at **enable** (reference: `MenuRegistrar` + `MenuFileManager` order of operations, without Slate merge dirs) |
| **Stable IDs** | **`FabricMenuKeys`** — `skills`, `stats` (subset of Bukkit `MenuFileManager.MENU_NAMES`) |
| **Dispatch** | **`FabricAuraSkillsMenus.open(String)`** and **`open(Id)`** → **`FabricAuraSkills#getMenuRegistry()`** |
| **GUI slices** | **`FabricSkillsMenuGui` / `FabricStatsMenuGui`** take **`Text title`** from registry (no hardcoded `Text.translatable` in GUI for window title) |
| **Manual** | **`SPRINT_29_MANUAL_MENU_REGISTRY.md`** |

**Not in Sprint 29:** file-backed YAML menu load, Slate `MenuLoader`, `menuFileManager.loadMenus`, merge directories, reload-time re-registration (future: call loader after reload).

## Sprint 30 — Clickable top slots + Skills → Level Progression (shipped, partial)

| Piece | Where |
|-------|--------|
| **Reusable top-slot primitive** | **`GraniteClickable9x6Handler`** + **`GraniteTopSlotClickHandler`**; **`GraniteContainerMenus#openClickable9x6`**; read-only top grid = `topClick == null` |
| **Flow** | **Skills** top-slot click (permission-checked) → **`FabricAuraSkillsMenus#openLevelProgression(Skill,…)`** → **`FabricLevelProgressionMenuGui`** (live level + XP to next; **Back** → Skills via registry `open(SKILLS)`) — next-tick `executeSync` to avoid re-entrancy |
| **IDs** | **`FabricMenuKeys#LEVEL_PROGRESSION`** (parity name only; not registered without context) |
| **Manual** | **`SPRINT_30_MANUAL_LEVEL_PROGRESSION.md`** |

**Not in Sprint 30:** full Slate click/template/YAML, reward list parity with `level_progression.yml`, registering `level_progression` in **`FabricMenuRegistry`** without a **`Skill`** context.

## Sprint 31 — Stats click + Stat Info detail (shipped, partial)

| Piece | Where |
|-------|--------|
| **Flow** | **Stats** top-slot click (when **`menus.stat_info.enabled`**) → **`FabricAuraSkillsMenus#openStatInfo(Stat,…)`** → **`FabricStatInfoMenuGui`**; **Back** → **Stats** via `open(STATS)`; if stat_info **disabled**, Stats stays **read-only** (matches Bukkit `StatsMenu` gate) |
| **IDs** | **`FabricMenuKeys#STAT_INFO`** (parity; not registered without **`Stat`**) |
| **Manual** | **`SPRINT_31_MANUAL_STAT_INFO.md`** |

**Not in Sprint 31:** full **`stat_info.yml`**, modifier instance grid (`ModifierInstances`), trait paging — same scope discipline as Sprints 30–31.

## Sprint 32 — Built-in `menus/*.yml` granite slice (shipped, partial)

| Piece | Where |
|-------|--------|
| **On-disk + embedded** | **`menus/skills.yml`**, **`menus/stats.yml`** in JAR; **`ConfigurateLoader#generateUserFile`** writes server copy under config folder on first start (same pattern as `config.yml`) |
| **Loader** | **`FabricBuiltInMenuConfig#load`**: `granite.enabled`, `title_key` / `title`, `size.rows`/`columns` (6×9 validated; non-6×9 logged, GUI unchanged), `format.list_open_detail` (translation key for list-row hint) |
| **Wiring** | **`FabricMenuRegistry#open`**: title + `enabled` from YAML; **`FabricSkillsMenuGui` / `FabricStatsMenuGui`**: `list_open_detail` from YAML. **`/auraskills reload`**: re-reads `menus/*.yml` (step **built-in menu YAML**) |
| **Fallback** | Malformed or missing `granite` / file → in-code **defaults** (`FabricMenuYamlSlice#default*`), warn in log; never leaves menus unrunnable without YAML |
| **Manual** | **`SPRINT_32_MANUAL_BUILT_IN_MENU_YAML.md`** |

**Not in Sprint 32:** full Slate `loadMenus` / `MenuFileManager` **merge** dirs, `skills.yml` template/items parity, re-registering **all** menu IDs from file.
