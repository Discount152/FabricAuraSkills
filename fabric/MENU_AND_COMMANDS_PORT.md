# Menus + commands on Fabric (reference: Bukkit ACF + Slate) — **Path A: full Slate port** (locked plan)

**Runtime:** **Fabric** only. This work implements **Fabric-native** menus and commands. The **reference** for *what* to cover is the **existing `bukkit` module** (ACF command tree, Slate menus) — **not** a goal of shipping Bukkit or Paper. **Final public** command roots, aliases, and **Granite Skills** copy are **TBD**; see **[PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md)**. This path is a **required** program track, not a side quest.

**Sprint 93 correction:** The **built-in `Fabric*MenuGui`** set is a **functional partial** of the reference menu experience. It is **Java-smoke adequate**, **not** “Slate parity complete.” Full **Slate / YAML / `MenuFileManager`** parity is **open**; see **`PARITY_COMPLETION_ROADMAP.md`**.

**Cross-platform acceptance:** Menus and commands are **high risk** for **Java vs Bedrock** differences. The shipped mod must remain usable for **Bedrock players joining through Geyser + Floodgate** on the Fabric server — not only for direct **Java** clients. Do **not** assume Java-only inventory packets, click semantics, or chat UX; **validate** (or **document** known gaps) for the Bedrock path. See **§Geyser / Bedrock** in [PARITY_BACKLOG.md](PARITY_BACKLOG.md).

**Sprint 82 (process):** **Java** dedicated-server **command + menu** smoke matrix execution — see **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`**. **Runtime validation** on **`2.3.114+`**; does **not** add commands/menus. **Bedrock** not in scope for 82. Update **[COMMAND_PARITY_AUDIT.md](COMMAND_PARITY_AUDIT.md)** only if 82 **changes** command surface (bugs fixed) vs prior audit.

**Sprint 83 (code — accepted 2026-04-26):** **`FabricGraniteText`** — server **literal** text for **Sprint 82**-reported `auraskills.granite.*` in **usage/help**, **Skills/Stats** built-in **titles** / **“opened”** feedback ( **`FabricMenuRegistry`**, **`FabricBrigadierCommands`**, **`FabricOpenMenuCommands`**). **`/mana view`** **no-arg** = **self**; **`/mana view <player>`** still **`mana.other`**. **Operator:** core **Sprint 82** rows **validated** on **`2.3.116+`**; deeper **menu item** lore may still use **`translatable`** (**follow-up**). **Doc:** **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`**.

**Sprint 84 (code + operator):** **`SPRINT_84_MANUAL_COMMAND_SMOKE_PATCH.md`** — **`FabricGraniteText`** for **`/xp view`** / **`/skilllevel view`**; no top-level **`/item`** or **`/xp`** (vanilla collision); **`permissionOrNonPlayerSource`** for **`/storage`**, **`/backup`**, **`/preset`**; **`/storage mergeskills`** permission on literal. Re-validate **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`** on **`2.3.117+`**.

**Sprint 85 (code):** **`SPRINT_85_MANUAL_COMMAND_TREE_PATCH.md`** — root **`.executes`** + **`requires`** for **`item`**, **`storage`**, **`backup`**, **`preset`** (bare-root parse). Artifact **`2.3.118+`**.

**Sprint 86 (docs):** **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`** — LuckPerms **`auraskills.*`**; final in-game staff **Pass** for **`storage`** / **`backup`** / **`preset`** / prefixed **`item`** roots; **Java** smoke matrix closeout (destructive paths **Skip** until disposable). No command code in Sprint 86.

**Sprint 87:** **`SPRINT_87_MANUAL_BEDROCK_GEYSER_VALIDATION.md`** — **Bedrock/Geyser** validation is **planned and documented**; **runtime pass deferred** until a **Bedrock** tester is available. **Does not block** Java/Fabric menu polish.

**Sprint 88 (code):** **`SPRINT_88_MANUAL_MENU_ITEM_LORE_TRANSLATION_SWEEP.md`** — built-in menu **item names/lore**, **titles** (incl. paging subtitle), **navigation** controls, **leaderboard**/**abilities**/**sources** rows, and **`openmenu`** menu-open feedback use **`FabricGraniteText`** so **vanilla Java** clients do not see raw **`auraskills.granite.*`** in inventory packets. **`2.3.119+`**.

**Sprint 89 (code):** **`SPRINT_89_MANUAL_SOURCES_CHAT_TRANSLATION.md`** — **`/sources`** **paged chat** (console/RCON + GUI fallback) and **`sources.bad_sort`** use **`FabricGraniteText`**. **`2.3.120+`**.

**Sprint 90 (code):** **`SPRINT_90_MANUAL_GLOBAL_GRANITE_TEXT_AUDIT.md`** — all remaining Fabric **command** paths that used **`Text.translatable("auraskills.granite…")`** now use **`FabricGraniteText`**; **`FabricGraniteJavaSourcesTranslatableScanTest`** guards the full **`dev.aurelium.auraskills.fabric`** main tree. **`2.3.121+`**.

**Sprint 91 (code + docs):** **`SPRINT_91_MANUAL_FABRIC_RELEASE_READINESS.md`** — release artifact map, **`README`** server install, committed **76A** anvil mixin + **`slate-fabric`** version expansion, tree hygiene. **`2.3.122+`**.

**Sprint 92 (docs):** **`RELEASE_NOTES_2.3.122.md`**, **`SPRINT_92_RELEASE_CANDIDATE_PREP.md`** — **2.3.122** RC checkpoint; **`v2.3.122-fabric-rc1`**. **Packaging superseded** by **Sprint 93**.

**Sprint 93 (packaging + parity reset):** **`SPRINT_93_TWO_JAR_RELEASE_AND_PARITY_RESET.md`**, **`PARITY_COMPLETION_ROADMAP.md`**, **`RELEASE_NOTES_2.3.123.md`** — **two JARs** canonical; built-in menus explicitly **partial** vs Slate. **`2.3.123+`**, **`v2.3.123-fabric-rc2`**.

**Sprint 94 (planning only — Path A spine):** **`SPRINT_94_GRANITELIB_SLATE_PARITY_PLAN.md`** — Slate capability inventory, GraniteLib/Fabric reality, gap + menu-by-menu tables, implementation phases **A–H**, risks, future acceptance criteria, **Sprint 95** (Spike 0: template model + YAML schema v0 + first migration target). **Does not** implement menus or change commands; **Bedrock/Geyser** still deferred.

**Sprint 95 (GraniteLib Spike 0 — code):** **`SPRINT_95_GRANITELIB_TEMPLATE_SPIKE0.md`** — `dev.granitelib.fabric.template`: **`GraniteMenuTemplate`** + read-only **YAML** loader (**SnakeYAML** JiJ), validation, unit tests; **no** live **`Fabric*MenuGui`** replacement. **Slate/YAML parity** still **open**.

**Recommended next (Java-side):** **Sprint 96** — optional template path for **`stat_info`** (see Sprint 95) + built-in fallback; deploy **two-JAR** **`2.3.123`** RC as needed; **Sprint 87** when a **Bedrock** tester exists.

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

- **Sprint 39+ — live status table:** [`COMMAND_PARITY_AUDIT.md`](COMMAND_PARITY_AUDIT.md) (Bukkit vs Fabric: present / **Partial** / **Missing** / N/A). Update when adding roots or subcommands.
- **Sprint 39+ — shared Brigadier scaffold:** `dev.aurelium.auraskills.fabric.command` — `FabricBrigadierCommands#register` (entry), `FabricCommandPermissions` (string constants), `FabricCommandRequirements` (`requires` + player checks), `FabricCommandPluginState` (instance/runtime readiness). **`FabricAuraSkillsCommands`** only registers the callback and delegates to the entry class.
- **Reference aliases (Bukkit `CommandRegistrar`):** `skills|sk|skill` (as `%skills_alias%`). **Final Granite** roots/aliases: **TBD**; use reference for **capability** coverage. Fabric does **not** yet register `sk` / `skill` (follow-up).
- **Coverage target:** `bukkit/.../CommandRegistrar.java` + every registered class (`SkillsRootCommand`, `StatsCommand`, `SkillCommand`, `XpCommand`, …) — **Fabric-native** handlers.
- **Permissions:** reference uses `plugin.yml` / `auraskills.command.*` — treat as the **compatibility** baseline until the **Granite** public permission / node naming pass. **Sprint 39** centralizes string constants in **`FabricCommandPermissions`**.
- **Implementation:** long-term: **`acf-brigadier`** + Fabric registration bridge as in plan above; current code uses **vanilla Brigadier** with shared helpers (no ACF on Fabric in this path yet).

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

## Sprint 33 — Detail menus: `level_progression` + `stat_info` YAML (shipped, partial)

| Piece | Where |
|-------|--------|
| **Files** | **`menus/level_progression.yml`**, **`menus/stat_info.yml`** (embedded + **`generateUserFile`**) with **`granite:`** (same shape as Sprint 32) |
| **Defaults** | **`FabricMenuYamlSlice#defaultLevelProgression`**, **`defaultStatInfo`** — format keys for main title, lore lines, back/back_lore, no_permission (level progress only) |
| **Wiring** | **`FabricLevelProgressionMenuGui` / `FabricStatInfoMenuGui`**: `enabled` (redirect to **Skills** / **Stats** + message if off), `title_key`, all **`format.*`** via **`resolveFormatKey`**; **Stat Info** still requires **`Option.MENUS_STAT_INFO_ENABLED`** first |
| **Manual** | **`SPRINT_33_MANUAL_DETAIL_MENU_YAML.md`** |

**Not in Sprint 33:** Bukkit `StatInfoMenu` **modifier** grid, `level_progression` **reward** templates, Slate **context** plumbing.

## Sprint 34 — `granite.layout` slot positions (shipped, partial)

| Piece | Where |
|-------|--------|
| **Keys** | Under **`granite.layout`:** **`list_start_slot`**, **`list_end_slot_exclusive`** (list menus, half-open range), **`main_slot`**, **`back_slot`** (detail menus; list keys stored but ignored for layout placement) |
| **Validation** | **`FabricMenuSlotLayout#validate`**: out-of-range integers → per-field **fallback** to the menu’s code default; **`list_end` ≤ `list_start`** → default **list** range; **`main_slot` == `back_slot`** → default **main** + **back**; warnings when **`FabricAuraSkills`** is non-null in **`parseGranite`** |
| **Wiring** | **`FabricSkillsMenuGui` / `FabricStatsMenuGui`**: list fill + clicks use **`[list_start, list_end)`**; **`FabricLevelProgressionMenuGui` / `FabricStatInfoMenuGui`**: book + arrow slots |
| **Manual** | **`SPRINT_34_MANUAL_BUILT_IN_MENU_LAYOUT.md`** |

**Not in Sprint 34:** full Slate **position** / **paging** / **template** resolution; custom **item** types per row from YAML.

## Sprint 35 — Context menus on the registry (shipped, partial)

| Piece | Where |
|-------|--------|
| **Context** | **`FabricMenuContext`**: `none()`, `ofSkill(Skill)`, `ofStat(Stat)` — optional fields for a future Slate-style context row without claiming full `ContextManager` parity |
| **Opener** | **`FabricMenuOpener#open(…, Text title, FabricMenuContext context)`** — list GUIs ignore `context`; `level_progression` / `stat_info` build titles in-GUI; registry passes **`Text.empty()`** for those ids after validating context |
| **Registry** | **`#open(menuId, player, plugin)`** delegates to **`open(…, FabricMenuContext.none())`**. Opening **`level_progression` / `stat_info`** without a **Skill** / **Stat** logs a **warn** and no-ops. **`FabricMenuLoaders`**: `skills`, `stats`, `level_progression`, `stat_info` (startup log lists **four** ids) |
| **Public API** | **`FabricAuraSkillsMenus#open(…, FabricMenuContext)`**; existing **`openLevelProgression` / `openStatInfo`** unchanged for callers in **Skills/Stats** GUIs |
| **Manual** | **`SPRINT_35_MANUAL_CONTEXT_MENUS.md`** |

**Not in Sprint 35:** arbitrary placeholder strings, Bukkit `ContextManager` / `SortType` coverage, or registering more contextual menus.

## Sprint 36 — `granite.items` (built-in item ids) (shipped, partial)

| Piece | Where |
|-------|--------|
| **Keys** | Under **`granite.items`:** **`list`**, **`main`**, **`back`** — vanilla **item** registry ids; unprefixed keys use **`minecraft:`** (e.g. `book` → `minecraft:book`) at parse time where valid |
| **Parse** | Invalid **identifier format** in YAML → log **warn** (if server **plugin** present) and keep **code default** id string; does not require the item to exist at **reload** time (mods can register later) |
| **Runtime** | **`FabricMenuItemDefinitions#resolveItem`**: if id missing from **`Registries.ITEM`**, or **`air`**, fall back to **book** (list + main) or **arrow** (back) and **warn** |
| **Wiring** | **Skills/Stats** — row stacks use **`list`**; **Level Progression / Stat Info** — **`main`** and **`back`**; unused keys on a menu (e.g. `main` on Skills) are still stored on the **slice** for a consistent YAML shape |
| **Manual** | **`SPRINT_36_MANUAL_BUILT_IN_MENU_ITEMS.md`** |

**Not in Sprint 36:** NBT, count, `ItemBuilder` / template items, or different item **per** list row.

## Sprint 37 — Paged Skills + Stats (shipped, partial)

| Piece | Where |
|-------|--------|
| **Model** | List **content** is `list_start`..`min(list_end, 45)` (slots 0–44 max); the **last row** (45–53) holds **optional** **Previous** / **Next** when `total` does not fit in one page |
| **Context** | **`FabricMenuContext#listPage`** (0-based); use **`#withListPage(n)`** or **`#none().withListPage(n)`** for registry reopen |
| **YAML** | **`granite.paging`:** `prev_slot`, `next_slot` (defaults **45** / **53**); validated to avoid **overlap** with the list **content** region; prev ≠ next or defaults apply |
| **UI** | **Arrow** items + translated name/lore; **disabled** direction leaves an **empty** slot. Window title appends a **(Page x of y)** suffix when more than one page |
| **Shared** | **`FabricListMenuPaging`**, **`FabricMenuPagingConfig`**; suitable for more list menus later — **Sprint 37** wires **Skills** and **Stats** only |
| **Manual** | **`SPRINT_37_MANUAL_PAGED_LIST_MENUS.md`** |

**Not in Sprint 37:** per-page size above 45, middle-number jump, or **Level Progression / Stat Info** pagination.

## Sprint 38 — list page through detail **Back** (shipped, partial)

| Piece | Where |
|-------|--------|
| **Carry** | Opening **Level Progression** from **Skills** (or **Stat Info** from **Stats**) uses **`FabricMenuContext.ofSkill(skill).withListPage(currentPage)`** / **`ofStat(stat).withListPage(currentPage)`** — same `listPage` field as registry paging (Sprint 37) |
| **API** | **`FabricAuraSkillsMenus#openLevelProgression(…, int returnSkillsListPage)`**, **`openStatInfo(…, int returnStatsListPage)`**; 3-arg overloads default to page **0**; **`open(Id, player, plugin, context)`** for list reopen |
| **Back** | **`FabricLevelProgressionMenuGui`** / **`FabricStatInfoMenuGui`**: **`open(SKILLS/STATS, …, none().withListPage(context.listPage()))`**; disabled-YAML / config paths match |
| **Clamp** | Unchanged: **Skills** / **Stats** GUIs already **`FabricListMenuPaging.clampPage(…)`** on open |

**Not in Sprint 38:** new menus, middle-number jump, or detail-menu-internal paging.

## Sprint 39 — command parity audit + shared Brigadier scaffold (shipped, partial)

| Piece | Where |
|-------|--------|
| **Audit** | **`COMMAND_PARITY_AUDIT.md`** — Bukkit `CommandRegistrar` + registered classes vs Fabric |
| **Scaffold** | **`command/FabricBrigadierCommands`**, **`FabricCommandPermissions`**, **`FabricCommandRequirements`**, **`FabricCommandPluginState`**; thin **`FabricAuraSkillsCommands`** |
| **First batch** | Root **`/mana`** (self display only) — Bukkit `ManaCommand` @Default; subcommands still **Missing** in audit |
| **Lang** | **`auraskills.granite.command.auraskills_usage`**, **`…help`** for root/help lines (no naming freeze) |
| **Manual** | **`SPRINT_39_MANUAL_COMMANDS.md`** |

**Not in Sprint 39:** `sk`/`skill` aliases, ACF context/completion parity, or large subcommand trees.

## Sprint 40 — player/skill arguments + **xp** batch 1 (shipped, partial)

| Piece | Where |
|-------|--------|
| **Args** | **`FabricCommandPlayerTarget`** (online name resolve + filter), **`FabricCommandSkillParser`** (registry key / `NamespacedId`), **`FabricCommandSuggestionProviders`** (players + enabled skill keys) |
| **XP** | **`FabricXpCommands#xpRoot`**: **`/xp`** and **`/auraskills xp`** — **view** `<player> <skill>`; **add** / **set** / **remove** `<player> <skill> <amount>`; uses **`LevelManager#addXp` / `#setXp`** like Bukkit |
| **Perms** | **`FabricCommandPermissions`:** `XP_VIEW` (default allow), `XP_ADD` / `XP_SET` / `XP_REMOVE` (default deny — staff / LP) |
| **Manual** | **`SPRINT_40_MANUAL_XP_COMMANDS.md`** |

**Not in Sprint 40:** `silent` flag, `skills` root prefix for `xp`, or full ACF **Skill** context parity.

## Sprint 41 — **mana** command family + shared numeric feedback (shipped, partial)

| Piece | Where |
|-------|--------|
| **Mana** | **`FabricManaCommands#manaCommandRoot`**: **`/mana`** and **`/auraskills mana`** — default **self** (`auraskills.command.mana` + fabric-perms check in executor); **`view`**, **`add` / `set` / `remove` `<player> <amount>`** (online targets only) |
| **Shared** | **`FabricCommandAmountRules`** (positive for **add**; non-negative for **set**/**remove**); **`FabricCommandMutationFeedback`** (prefixed `MessageBuilder` to source for **xp** + **mana** success lines) |
| **Perms** | **`auraskills.command.mana`**, **`.other`**, **`.add`**, **`.set`**, **`.remove`** (names aligned with Bukkit; defaults like reference **op** for subcommands) |
| **Lang** | **`…permission_denied`**, **`…mana.console_specify`**, **usage** string mentions **`mana`**, **`xp`** sprints |
| **Manual** | **`SPRINT_41_MANUAL_MANA_COMMANDS.md`** |

**Not in Sprint 41:** ACF `silent` / `allowOverMax` on **mana**; **offline** target UUIDs; non-Geyser-specific command UX beyond shared translatables and prefix formatting.

## Sprint 42 — **skill level** command family (shipped, partial)

| Piece | Where |
|-------|--------|
| **Level** | **`FabricSkillLevelCommands#skillLevelRoot`**: **`/skilllevel`** and **`/auraskills skilllevel`** — **view** + **add** / **set** / **remove** (same player + skill arg pattern as **xp**; **add**/**remove** use a positive **count**; **set** takes absolute level); **`FabricCommandSkillLevelApply.setSkillLevel`** (stats, permissions, reward revert/level-up, `FabricModifierManager`) like Bukkit `SkillCommand` |
| **Shared** | **`FabricCommandAmountRules.requirePositiveIntForAdd`** (add/remove level **count**); **`FabricCommandMutationFeedback`** for **`SKILL_SETLEVEL_SET`**, **`SKILL_AT_LEAST`** |
| **Perms** | **`auraskills.command.skilllevel.view`** (read); **`auraskills.command.skill.setlevel`** (add/set/remove — same string as Bukkit) |
| **Lang** | **`…level.count_positive`**, **`…skilllevel.view_line`**, **usage** / **help** list **`skilllevel`** |
| **Manual** | **`SPRINT_42_MANUAL_SKILLLEVEL_COMMANDS.md`** |

**Not in Sprint 42:** ACF `setall` / `reset` / `skills`+`skill` Bukkit path; max-level capping; offline targets.

## Sprint 43 — **skilllevel** `setall` + `reset` (shipped, partial)

| Piece | Where |
|-------|--------|
| **setall** | **`skilllevel setall <player> <level>`** — `auraskills.command.skill.setlevel`; **`FabricCommandSkillLevelApply.setAllEnabledSkillsToLevel`** (Bukkit order: per enabled skill revert/level-up, then one stats / modifiers / permissions) |
| **reset** | **`skilllevel reset <player>`** (all skills) and **`… reset <player> <skill>`** — `auraskills.command.skill.reset`; uses **`User#resetSkill`**, then **`afterSkillResetApplyModifiers`**; feedback **`SKILL_SETALL_SET`** / **`SKILL_SETLEVEL_SET`** |
| **Manual** | **`SPRINT_43_MANUAL_SKILLLEVEL_SETALL_RESET.md`** (full **skilllevel** family checklist) |
| **Perms** | Adds **`auraskills.command.skill.reset`** |

**Not in Sprint 43:** ACF `skills\|sk\|skill` + `skill` path; max-level capping; offline targets; changing the **`/skilllevel`** root/alias design.

## Sprint 44 — alias roots + bounded **openmenu** (shipped, partial)

| Piece | Where |
|-------|--------|
| **Aliases** | **`/sk`** and **`/skill`**: default → same as **`/skills`** (opens **Skills**); requires `auraskills.command.skills` in the default executor (like **mana** self). Child **`openmenu`** uses `auraskills.command.openmenu`. |
| **openmenu** | **`FabricOpenMenuCommands`**, **`FabricCommandBuiltInMenuId`**: **`/openmenu`**, **`/auraskills openmenu`**, **`/sk openmenu`**, **`/skill openmenu`** — `openmenu <skills\|stats> [onlineTarget]`. Routes to **`FabricAuraSkillsMenus.open`**. |
| **Shared** | **`FabricCommandSuggestionProviders.builtInOpenMenuIds`** |
| **Manual** | **`SPRINT_44_MANUAL_ALIASES_OPENMENU.md`** |

**Not in Sprint 44:** arbitrary Slate menu names; JSON **properties** / **page** args; **level_progression** / **stat_info** without context; full `SkillsRootCommand` subtree under aliases.

## Sprint 45 — **openmenu** + **level_progression** / **stat_info** (shipped, partial)

| Piece | Where |
|-------|--------|
| **openmenu** | **`FabricOpenMenuCommands`**: Brigadier **literals** for **`skills`**, **`stats`**, **`level_progression`**, **`stat_info`**. **Detail:** `openmenu level_progression [<onlinePlayer>] <skillkey>`; `openmenu stat_info [<onlinePlayer>] <statkey>`. Same permission **`auraskills.command.openmenu`**. |
| **Routing** | **`FabricAuraSkillsMenus.openLevelProgression`** / **`openStatInfo`** (registry + **`FabricMenuContext`**); not a second code path. |
| **Parsers** | **`FabricCommandSkillParser`**; **`FabricCommandStatParser`**. |
| **Suggestions** | **`FabricCommandSuggestionProviders`**: `builtInOpenMenuIds` (string contexts); **`enabledStatKeys`**; existing **`enabledSkillKeys`**, **`onlinePlayers`**. |
| **Lang** | Console lines for list vs context menus; **`unknown_skill`**, **`unknown_stat`**, **`opened_for_context`**, self-opened lines for detail menus. |
| **Manual** | **`SPRINT_45_MANUAL_OPENMENU_CONTEXT_MENUS.md`** |

**Not in Sprint 45:** Bukkit **JSON** `properties` and **`page`** on `openmenu`; custom Slate `menuName`; optional **list page** as a command argument.

## Sprint 46 — `/sk` and `/skill` **compatibility** subtree (shipped, partial)

| Piece | Where |
|-------|--------|
| **Routing** | **`FabricBrigadierCommands#aliasSkillsRoot`**: under **`/sk`** and **`/skill`**, after `openmenu`, chains **`.then(`**`FabricXpCommands.xpRoot())`**, **`.then(`**`FabricManaCommands.manaCommandRoot())`**, **`.then(`**`FabricSkillLevelCommands.skillLevelRoot())`**. Each `*Root()` call builds a **new** `LiteralArgumentBuilder` tree (same `Command<>` executors; no second implementation). |
| **Standalone roots** | **`/xp`**, **`/mana`**, **`/skilllevel`**, **`/openmenu`**, **`/auraskills …`** **unchanged**. |
| **Caveat** | Reference **`/sk skill <sub>`** (ACF) is **not** mirrored: Fabric’s **`/skill`/`/sk`** are **literal** roots, so the compatibility word for **SkillCommand**-family work is **`/sk skilllevel …`**, not **`/sk skill`**. |
| **Manual** | **`SPRINT_46_MANUAL_SK_SKILL_COMPAT_SUBTREE.md`** |
| **Lang** | **usage** / **help** strings (surface `/sk\|skill <xp\|mana\|skilllevel\|openmenu>`) |

**Not in Sprint 46:** ACF **SkillCommand** at **`/sk skill`**, or any new **gameplay** systems. (**Top/rank** — **Sprint 47**, see below.)

## Sprint 47 — **leaderboard** and **rank** (shipped, partial)

| Piece | Where |
|-------|--------|
| **Commands** | **`FabricLeaderboardCommands`**: **`/top`**, **`/top <page>`** — power; **`/skilltop <skill>`**, **`/skilltop <skill> <page>`** — per-skill; **`/rank`** — self power + all enabled skills; **`/skillrank <skill>`** — one line. **`auraskills.command.top`**, **`auraskills.command.rank`**. |
| **Mirrors** | Same `*Root()` builders under **`/auraskills`**, **`/sk`**, **`/skill`**. |
| **Shared** | **`FabricCommandLeaderboardText`**: 1-based global index for paged sublists, **`?`** for **non-online** display names in this sprint. **`LeaderboardManager`**, **`CommandMessage`**, **`FabricCommandSkillParser`**. |
| **Manual** | **`SPRINT_47_MANUAL_LEADERBOARD_RANK.md`** |
| **Not in Sprint 47** | **`top average`**, Bukkit’s full `String[]` overload for **`top`**, **`/skilltop` as alias of** **`/top`**, **offline name** lookup, **`updateleaderboards`**. |

## Sprint 48 — **multiplier**, **toggle**, **lang** (shipped, partial)

| Piece | Where |
|-------|--------|
| **Commands** | **`FabricPlayerUtilityCommands`**: **`/multiplier`** (self) and **`/multiplier <player>`**; **`/toggle`** and **`/abtoggle`** ( **Sprint 81** — cycles **idle** action bar **smart → always → off**; **Sprint 81A** — optional **`status`**, **`smart`**, **`always`**, **`off`**; **Sprint 81B–C** — **smart** idle **visibility** in **`ActionBarManager`** (**81C** = **`SmartIdleObsDisplay`**, not commands); `userdata` / SQL **`idle_mode`**; same perm **`abtoggle`**); **`/lang <code>`** with suggestions from **`MessageProvider#getLanguageCodes`**. |
| **Mirrors** | **`/auraskills`**, **`/sk`**, **`/skill`**, plus top-level. |
| **Shared** | **`FabricCommandLocaleArgs`**; **`setShouldSave(true)`** on **lang** + **toggle** after mutation. |
| **Manual** | **`SPRINT_48_MANUAL_PLAYER_UTILITY.md`** |
| **Not in Sprint 48** | Rich **Bukkit** **`CommandManager#setPlayerLocale`** client sync; full **LP** **multiplier** perm walk on **Fabric** (see **`FabricUser`**). |

## Sprint 49 — **top average**, **updateleaderboards**, name cache (shipped, partial)

| Piece | Where |
|-------|--------|
| **Commands** | **`FabricLeaderboardCommands`**: **`/top average`**, **`/top average <page>`** (skill-average leaderboard via **`LeaderboardManager#getAverageLeaderboard`**); **`/updateleaderboards`**, **`/updateleaderboards <samples>`** (**1–64**), **`auraskills.command.updateleaderboards`**. |
| **Mirrors** | Same `*Root()` trees under **`/auraskills`**, **`/sk`**, **`/skill`**, plus top-level **`/updateleaderboards`**. |
| **Shared** | **`FabricCommandLeaderboardText#resolvePlayerName`**: **online** → live name; else **`ApiServices#nameToIdCache#getByUuid`** → **`PlayerConfigEntry#name`**; else **`?`**. **Average** rows: **`TextUtil.replace`** + **`NumberUtil.format2(sv.xp())`**. |
| **Manual** | **`SPRINT_49_MANUAL_LEADERBOARD_BATCH2.md`** |
| **Not in Sprint 49** | Bukkit **unbounded** `samples` on **`updateleaderboards`** (Fabric caps **64**); **Mojang** name fetch for UUIDs not in **usercache**; **`UserState`** has no **username** field. |

## Sprint 50 — **modifier** + **trait** admin (player-scoped) (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/modifier`**, **`/trait`** (standalone + **`/auraskills`**, + **`/sk`**, **`/skill`**) — **add** (optional **operation** + **stack**), **remove**, **list**, **removeall**; **online** targets via **`FabricCommandPlayerTarget`** |
| **Shared helpers** | **`FabricCommandTraitParser`**, **`FabricCommandModifierStackName`**, **`FabricCommandModifierText`**, `FabricCommandSuggestionProviders#enabledTraitKeys` |
| **State** | **`User#addStatModifier` / `addTraitModifier`**, **`setShouldSave(true)`**, **`FabricModifierManager#applyModifiers`** (same pattern as Bukkit user mutations) |
| **Permission** | **`auraskills.command.modifier`** (see **`FabricCommandPermissions#MODIFIER`**) — default **deny** for unprivileged fabric-perms |

| **Not in Sprint 50** | Bukkit **`addtemp`**, **item** / **armor** modifier subcommands, **offline** targets, ACF **silent** — see **`fabric/COMMAND_PARITY_AUDIT.md`**. **Manual:** **`SPRINT_50_MANUAL_MODIFIER_TRAIT.md`**. **Ledger** CMD-005 / CMD-015: **Partial**. |

## Sprint 51 — **item** + **armor** held stat/trait modifier commands (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/item`**, **`/armor`** (standalone + **`/auraskills`**, + **`/sk`**, **`/skill`**) — **`item modifier\|trait`** and **`armor modifier\|trait`**: **add** ([op] + **lore**), **remove** (stat + optional lore; trait remove w/out lore), **list**, **removeall**; **player**-only, **main-hand** item |
| **State** | **`FabricSkillsItemWriter`** (PDC list; strips legacy `AuraSkills/Modifiers/…` for the same `ModifierType` on write) + **`FabricItemModifierLore`** (prepend lore / stat-based strip on remove) + **`User`** refresh via **`FabricModifierManager#applyModifiers`** |
| **Perms** | `auraskills.command.item.modifier`, `auraskills.command.armor.modifier` (Bukkit `plugin.yml` names) |

| **Not in Sprint 51** | Reference item **register**/**give**; **requirement** / **multiplier** / **ignore** — Sprints **52–53**. **Manual:** `SPRINT_51_MANUAL_ITEM_ARMOR_MODIFIERS.md`. |

## Sprint 52 — **`/item ignore`** (held mana-ability ignore) (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/item ignore add`**, **`/item ignore remove`** (main hand; same mirrors as **`/item`**) |
| **State** | **`FabricItemIgnoreTags`** — `custom_data` / `PublicBukkitValues` **`auraskills:ignore_interact`** (Bukkit PDC parity); **`FabricManaAbilityItemTags`** delegates read to same class |
| **Perms** | **`auraskills.command.item.ignore`** (separate from **`item.modifier`**) |

| **Not in Sprint 52** | **item** `requirement` / `multiplier` ( **Sprint 53**), **register** / **give**; **ArmorCommand** `ignore` (none in reference). **Manual:** `SPRINT_52_MANUAL_ITEM_IGNORE.md`. |

## Sprint 53 — **`/item requirement`** + **`/item multiplier`** (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/item requirement`** — **add** `<skill> <level> [lore]`, **remove** `<skill> [lore]`, **list**, **removeall**; **`/item multiplier`** — **add** `<skill or global> <value> [lore]`, **remove** `<target>`, **list**, **removeall**; **main hand**; same mirrors as **`/item`** |
| **State** | **`FabricSkillsItemWriter#putRequirements` / `#putMultipliers`** — PDC `auraskills:item_requirements` / `auraskills:item_multipliers`; strip legacy `AuraSkills/…/Requirements` & `…/Multipliers` for `ModifierType.ITEM` on write; **`FabricSkillsItemReader`** (unchanged) |
| **Lore** | **`FabricItemModifierLore`** — requirement line append; multiplier line append (matches Bukkit **add** lines); requirement **remove** uses **Requires** + skill **name** heuristic (same as Bukkit) |
| **Perms** | **`auraskills.command.item.requirement`**, **`auraskills.command.item.multiplier`** (separate from **modifier** / **ignore**) |
| **Suggestions** | **`FabricCommandSuggestionProviders#enabledSkillKeysOrGlobal`** (multiplier **add** / **remove** first arg) |

| **Not in Sprint 53** | **Armor** `requirement` / `multiplier` — **Sprint 54**; **item** **register** / **give**. **Manual:** `SPRINT_53_MANUAL_ITEM_REQ_MULT.md`. |

## Sprint 54 — **`/armor requirement`** + **`/armor multiplier`** (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | Same subcommand shapes as **`/item requirement`** / **`/item multiplier`**, on **`/armor`** (standalone + **`/auraskills`**, **`/sk`**, **`/skill`**); **main hand** (Bukkit **`@Flags("itemheld")`**) |
| **State** | **`FabricSkillsItemWriter`** with **`ModifierType.ARMOR`** → PDC `auraskills:armor_requirements` / `auraskills:armor_multipliers`; shared **`RequirementMsg` / `MultiplierMsg`** + **`requirementAdd`**, **`multiplierAdd`**, … |
| **Perms** | **`auraskills.command.armor.requirement`**, **`auraskills.command.armor.multiplier`**; **`modifier`/`trait`** subtrees still **`auraskills.command.armor.modifier`** — **`/armor` root** has **no** single permission (**Sprint 54**, mirrors **`/item`**) |
| **Manual** | `SPRINT_54_MANUAL_ARMOR_REQ_MULT.md` |

| **Not in Sprint 54** | **Item** **register** / **give**; out-of-lane work (storage, profiles, …). |

## Sprint 55 — **`/item register`**, **`/item unregister`**, **`/item give`** (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/item register <key>`** / **`/item unregister <key>`** (main hand for **register**; both **player**-only) — same mirrors as other **`/item`** roots; **`/item give <player> <key> [amount]`** (console allowed when target is online) |
| **State** | **`FabricItemRegistry`** + **`items.yml`** (`saveToDisk` / `loadFromDisk` on register/unregister; **reload** reloads from disk). **`addRegisteredItemToInventory`** (sync) shared with **`ItemRegistry#giveItem`**; unclaimed + **`setShouldSave`** on overflow |
| **Perms** | **`auraskills.command.item.register`** (register+unregister, Bukkit), **`auraskills.command.item.give`** |
| **Suggestions** | **`FabricCommandSuggestionProviders#registeredItemKeys`** — **`/item unregister`**, second arg of **`/item give`** |

| **Not in Sprint 55** | Bukkit **ExternalItemProvider** / dynamic namespaces; **`/claimitems`** — **Sprint 56**; **N/A** for armor. **Manual:** `SPRINT_55_MANUAL_ITEM_REGISTER_GIVE.md`. |

## Sprint 56 — **`/claimitems`** (unclaimed → inventory) (shipped, partial; **Sprint 71** extends)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **Orig. Sprint 56:** one-shot **merge** into inventory. **Sprint 71:** default `/claimitems` opens **`FabricUnclaimedItemsMenuGui`**; **`/claimitems all`** keeps merge-only. **`/skills\|/sk\|/skill\|/auraskills claimitems`**, `auraskills.command.claimitems` (Bukkit: default **true** under `command.user`); **player**-only; empty list → **`CLAIMITEMS_NO_ITEMS`** |
| **State** | **`User#getUnclaimedItems()`**; **`tryMergeRegisteredItemCount`** in **`FabricItemRegistry`** (shared with **`/item give`** / **`addRegisteredItemToInventory`**); **`setShouldSave`** on list change; **`FabricUnclaimedItemsMerge`** (**Sprint 71**) |
| **Not in Sprint 56 (historical)** | Bukkit **Slate** per-slot menu — see **Sprint 71** + **`fabric/COMMAND_PARITY_AUDIT.md`**. **Sprint 56 manual (merge):** `SPRINT_56_MANUAL_CLAIMITEMS.md`. |

## Sprint 57 — **`/sources`**, **`/resethealth`** (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/sources <skill> [sort]`** + mirrors on **`/skills`**, **`/sk`**, **`/skill`**, **`/auraskills`**, and top-level **`/sources`** — **`auraskills.command.sources`**; **`/resethealth`** + same mirrors, **`auraskills.command.resethealth`** (default **deny** in fabric-perms; **op**-style) — **dedicated server console or RCON only** |
| **Data** | **`/sources`**: **`SkillManager#getSkill` → `LoadedSkill#sources`**, `FabricSourceSortType` + `FabricCommandSkillParser` + `User` locale. **`/resethealth`**: **`FabricServerPlayerDataPaths`** → **`MinecraftServer#getSavePath(WorldSavePath.PLAYERDATA)`**; gzip NBT read/write; **`FabricOfflinePlayerDataAuraStripping`** strips `attributes[].modifiers` with **`id` starting with `auraskills:`** (1.21+), skips **online** player UUIDs |
| **Not in Sprint 57** | Bukkit **Slate** **`sources` menu** (tab UI); Bukkit pre-1.21 **`Attributes` / named modifier** NBT in **`Uninstaller`**, Paper 26+ `players/data` path — see **`SPRINT_57_MANUAL_SOURCES_RESETHEALTH.md`**. **Still missing** **storage** / **profile** command families (not this sprint) |

## Sprint 58 — **`/transfer`** (`UserState` move) (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/transfer <from-uuid> <to-uuid>`** + mirrors on **`/skills`**, **`/sk`**, **`/skill`**, **`/auraskills`**, top-level **`/transfer`** — **`auraskills.command.transfer`** (default **deny** / op-style) |
| **Data** | **`UserState`** only (skills, XP, persistent stat/trait modifiers, mana); **`User#applyState`** for **online** target + **`StorageProvider#applyState`** for **offline** target; **`FabricUserDataTransfer`** (online check, YAML file check, post-apply **save**); **`RewardManager#updatePermissions`** after online apply |
| **Not in Sprint 58** | Full **profile** parity (jobs, unclaimed items, ability data, locale, action bar, … are **not** in `UserState`); **backup**/**profile**/**storage** admin commands — see **`SPRINT_58_MANUAL_TRANSFER.md`** |

## Sprint 59 — **`/profile`** (read-only `UserState` inspection) (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/profile skills <player>`** / **`/profile stats <player>`** + mirrors on **`/auraskills`**, **`/skills`**, **`/sk`**, **`/skill`**, and top-level **`/profile`** — **`auraskills.command.profile`** (default **true**, like Bukkit `plugin.yml`) |
| **Data** | **`UserState`** (same scope as **Sprint 58** **`/transfer`**): **skills** uses **`messages_*`** **`commands.profile.skills`**, **stats** uses **`commands.profile.stats`** + **Granite** footer: **mana**, **raw stat/trait** modifier lines + disclaimer. **Name → UUID:** online (case-insensitive) then **`NameToIdCache#findByName`**. **Offline (YAML):** no **`userdata/<uuid>.yml`** when **file** storage — error (aligned with **transfer** pre-check) |
| **Not in Sprint 59** | Bukkit **async** `loadState` (Fabric **sync**); **Slate** / full “profile” product surface; **backup** / **preset** / **storage** mutation — see **`SPRINT_59_MANUAL_PROFILE.md`**. Bukkit **stats** view does not append mana + modifier dumps |

## Sprint 60 — **`/backup`** (create / list / info) (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/backup create`**, **`/backup save`** (Bukkit **`save`** alias), **`/backup list`**, **`/backup info`** + mirrors on **`/auraskills`**, **`/skills`**, **`/sk`**, **`/skill`**, top-level **`/backup`** — **`auraskills.command.backup`** (default **deny** / op-style) |
| **Data** | Common **`BackupProvider#saveBackupAsync` / `saveBackupSync`** — timestamped **`.yml`** in **`backups/`**, **UserState** export via **`StorageProvider#loadStates`**; online users **saveSafely** first. **`BackupProvider#saveBackupAsync`**: async failures now **complete** the future with **`completeExceptionally`** (common fix). **List:** newest-first **`.yml`** names, size, mtime; capped lines |
| **Not in Sprint 60** | **`backup load` / restore** — shipped in **Sprint 61** (see below). Delete, schedules, SQL dump, full datadir **config+messages+loot** copy — see **`SPRINT_60_MANUAL_BACKUP.md`**. Bukkit ref uses **`auraskills.command.backup.save` / `.load`**; Fabric **Sprint 60** used one node for create/list/info; **Sprint 61** adds **`auraskills.command.backup.load`** |

## Sprint 61 — **`/backup load|restore`** (guarded UserState restore) (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/backup load <basename.yml>`** (preview) → **`/backup load <basename.yml> confirm`** (mutate); **`restore`** is an alias of **`load`**. Same mirrors + top-level **`/backup`** as Sprint 60. Tab suggests **`backups/*.yml`** basenames. **`auraskills.command.backup.load`** (default **deny**). |
| **Data** | **`FabricBackupPathValidator`**: single-segment **`.yml`** only; resolved path must stay under **`backups/`**. Pre-restore: **`saveBackupSync(true)`**; refuse if skipped (**max_users**) or fails. Then common **`loadBackupAsync`**; online **`User`**: reload from **storage** where possible + **`online_refreshed_note`**. **Not** a full **config/messages/loot/menus** datadir restore. |
| **Not in Sprint 61** | **Delete/prune** backups, **scheduled** backups, **SQL** dump, **`--force`**, arbitrary host paths, full server/datadir restore — see **`SPRINT_61_MANUAL_BACKUP_LOAD.md`**. **Parity** remains **Partial** vs Bukkit (confirmation shape, permission defaults, no delete). |

## Sprint 62 — **`/preset`** (config zip presets) (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/preset list`**, **`/preset info <name.zip>`** (read-only), **`/preset load <name.zip>`** (preview) → **`/preset load <name.zip> confirm`**. Same mirrors on **`/auraskills`**, **`/skills`**, **`/sk`**, **`/skill`**, top-level **`/preset`**. **`auraskills.command.preset`** (default **deny**). Tab suggests **`presets/*.zip`**. |
| **Data** | Common **`PresetManager#preparePreset` / `loadPreset`** (zip with root **`preset.yml`**, `files/`). Pre-apply: **`BackupProvider#saveBackupSync(true)`** (UserState safety snapshot); then extract/merge; then **`reloadFromDisk()`**. Does **not** delete userdata or run **Storage** migrations. |
| **Not in Sprint 62** | **StorageCommand** — see **Sprint 63** below; preset create/delete/download; **Bukkit**-style “type the same command twice” confirm (Fabric uses **literal `confirm`**); see **`SPRINT_62_MANUAL_PRESET.md`**. **Parity** **Partial** vs reference (**list**+**info** are Fabric-only extras). |

## Sprint 63 — **`/storage`** (info / save / reload explainer) (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/storage info`**, **`/storage save`**, **`/storage reload`** + mirrors on **`/auraskills`**, **`/skills`**, **`/sk`**, **`/skill`**, top-level **`/storage`**. **`auraskills.command.storage`** (default **deny**). |
| **Data** | **info** — active **`StorageProvider`** (YAML **file** vs **SQL** pooled), FQMN, data directory **basename** only in chat, **`sql.enabled`** flag, loaded user count; **no** JDBC URL, host, user, password. **save** — **`saveAllUserProfiles()`** (same as **`/auraskills save`** for online users). **reload** — **not** a storage hot-swap; points to **`/auraskills reload`** for config/skill reload and **restart** to change backend. |
| **Not in Sprint 63** | Live **migrate** YAML↔SQL; **Bukkit** ref **`mergeskills`** / **`removeallmodifiers`** — see **Sprint 64**; SQL **dump**/admin — see **`SPRINT_63_MANUAL_STORAGE.md`**. |

## Sprint 64 — **`/storage mergeskills`**, **`/storage removeallmodifiers`** (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | Same roots as S63. **`/storage mergeskills <from> <to>`** (preview) → **`/storage mergeskills <from> <to> confirm`**. **`/storage removeallmodifiers`** (preview) → **`/storage removeallmodifiers confirm`**. All under **`auraskills.command.storage`**. **Confirm** subcommands: **dedicated server console or RCON only** (in-game `confirm` is rejected with a message). |
| **Data** | **mergeskills** — for each `UserState` from **`loadStates`**, if **both** skills exist and **source level &gt; target level**, set target level to source level, set target **XP** to source **XP** (Bukkit reference), then **`StorageProvider#applyState`**. Counters: merged / applyState failed / missing either skill / skipped (source ≤ target). **removeallmodifiers** — if both stat and trait maps empty, skip; else **`UserState`** copy with **empty** stat and trait maps, same skills/XP/mana, **`applyState`**. **Pre-mutation** **`BackupProvider#saveBackupSync(true)`** — if **null** (e.g. **max_users**) or **throws**, **abort** (no **force**). **Online:** **`FabricCommandStorageUtil#refreshOnlineUsersFromStorage`**. |
| **Not in Sprint 64** | Live **migrate**; SQL dump/export; **force**; provider hot-swap; Bukkit’s **entire** subcommand = **console-only** (Fabric allows **in-game preview** with permission). Manual: **`SPRINT_64_MANUAL_STORAGE_ACTIONS.md`**. |

## Sprint 65 — command reconciliation (`/skills` = `/sk` = `/skill` subtree) (shipped)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Change** | **Bugfix / hygiene:** **`/skills`** was missing the same Brigadier subtrees as **`/sk`** and **`/skill`** (everything before **claimitems** in the alias chain: **openmenu**, leaderboards, utility, **xp/mana/skilllevel**, **modifier/trait**, **item/armor**). **Sprint 65** introduces **`FabricBrigadierCommands#skillsAliasSharedTree`** and uses it for all three literals so **Bukkit** `CommandRegistrar` **`skills\|sk\|skill`** cannot drift. |
| **Not in Sprint 65** | New command families, menus, **help** node alignment with Bukkit, **permission** default changes, gameplay. Manual: **`SPRINT_65_COMMAND_RECONCILIATION.md`**. |

## Sprint 66 — **`/manaability resetcooldown`** (Bukkit `ManaAbilityCommand`) (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/auraskills manaability`**, **`/skills\|/sk\|/skill manaability`** **resetcooldown** `<player> <ability> [fill_mana]` — **no** dedicated top-level **`/manaability`** (matches reference: only under **skills** alias + **`/auraskills`** on Fabric). **`auraskills.command.manaability.resetcooldown`** (default **deny**). **In-game** sender: target must be **another** online player (ACF `@Flags("other")`); **console/RCON** may target any online player. |
| **Data** | **Other**’s **`User`**: if cooldown **&gt; 0** and not **activated**, set **0**; optional **boolean** `fill_mana` → **max** mana. **`user.setShouldSave(true)`**. **No** **`ManaAbilityRefreshEvent`** (Bukkit / api-bukkit). |
| **vs `/abtoggle`** | **`/abtoggle`** / **`/toggle`** = **Sprint 48/81** **IDLE** **action bar** policy (**Sprint 81** = **smart/always/off**); **independent** of this admin **cooldown** tool. |
| **Not in Sprint 66** | **`ManaAbilityRefreshEvent`**; other **ManaAbility** subcommands (reference has **only** **resetcooldown**); **Jobs**; menus. Manual: **`SPRINT_66_MANUAL_MANA_ABILITY_COMMAND.md`**. |

## Sprint 67 — **`/antiafk logs`** (Bukkit `AntiAfkCommand`) (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/auraskills antiafk`**, **`/skills\|/sk\|/skill antiafk`** **logs** `<player> [page] [perPage]` (defaults **1** / **10**). **No** top-level **`/antiafk`**. **`auraskills.command.antiafk.logs`** (default **deny**). **In-game:** **other** player name only (ACF `@Flags("other")`); **console/RCON** any resolvable name. |
| **Data** | **Read-only** listing: **`StorageProvider#loadAntiAfkLogs(uuid)`** + in-session **`User#getSessionAntiAfkLogs`**. Does **not** invoke **`AntiAfkManager`** (Bukkit does not for **logs**). Async load + sync feedback like reference when cache empty / offline. |
| **Gaps (Partial)** | Name → UUID: **online** + **`NameToIdCache`**, not Bukkit **`OfflinePlayer#hasPlayedBefore`**. **Bedrock** chat: multi-line string; not separately verified this sprint. |
| **Not in Sprint 67** | **Jobs**; per-skill **SkillCommands**; anti-AFK **algorithm** or detection changes. Manual: **`SPRINT_67_MANUAL_ANTIAFK_COMMAND.md`**. |

## Sprint 68 — per-skill **SkillCommands** (Bukkit `SkillCommands` top-level) (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Bukkit reference** | `SkillCommands` inner classes **`FarmingCommand` … `ForgingCommand`**, each **`@CommandAlias` = skill id**; **one** `onCommand(Player)`: `LevelProgressionOpener#open` → Slate `level_progression` menu. **Register** when **`config.yml`** `enable_skill_commands` and **`CommandRegistrar#registerSkillCommand`** ( **`skills.<id>.options.enabled` in `skills.yml`**). **15** skills (all **`Skills` enum** values in reference). **No** `@CommandPermission` on the reference classes; **player-only**. |
| **Fabric** | **`FabricSkillShortcutCommands`**: one Brigadier top-level per alias ( **`/farming` … `/forging`**) and **`#openLevelProgression`** to **built-in** `level_progression` (same as **`/openmenu level_progression <skill>`** for self, but **separate** command tree: **not** a child of `openmenu` / not **`auraskills.command.openmenu`**). **Gating:** `Option.ENABLE_SKILL_COMMANDS` + **`loadConfigEnabledMap`**; **`requires`** uses **`Permissions.check(…, "auraskills.skill.<name>", true)`**; **console/RCON** use **`auraskills.granite.command.utility.players_only`**. **No** post-open chat line (Bukkit: menu only). |
| **Gaps (Partial)** | Bukkit: **Slate**; Fabric: **Granite** YAML built-in. Bedrock: inventory **NRP** — not verified this sprint. |
| **Not in Sprint 68** | **Jobs**; new menu engine; new mechanics. Manual: **`SPRINT_68_MANUAL_SKILL_COMMANDS.md`**. |

## Sprint 69 — **menu** reconciliation inventory + **sources** GUI baseline (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Command ↔ UI** | **`/sources <skill> [sort]`** (+ **`/auraskills`**, **`/skills`**, **`/sk`**, **`/skill` mirrors**): **in-game** opens **`FabricSourcesMenuGui`** (`GraniteContainerMenus#openClickable9x6`); **console/RCON** keep **paged chat** (28 lines/page). **`FabricSourcesListSupport`** shares **version-valid** filter + **sort** with both paths. **Fallback:** if opening the screen throws, **`command.sources.menu_fallback`** + **chat** list. |
| **Not registered** | **`sources`** is **not** in **`FabricMenuLoaders` / `FabricMenuRegistry`** — **`FabricAuraSkillsMenus#openSources`** from **`/sources`** and **`/openmenu sources`** (**Sprint 70**). |
| **Parity (Partial)** | Bukkit: **`SourcesMenu`**, **`menus/sources.yml`**, per-source custom **`ItemRegistry` stacks`, **`multiplied_xp` component**, income. Fabric: **gray_dye** rows, fixed control row (**Back** slot 45 → `openLevelProgression` with list page **0**; **Prev/Next**; **Sort** hopper + **`FabricSourceSortType#nextCyclic()`**). |
| **Manual** | **`SPRINT_69_MANUAL_MENU_SOURCES.md`** (menu inventory from **`MenuRegistrar`** + known gaps) |

| **Not in Sprint 69** | Full **Slate**; **leaderboard** / **abilities** menus; **unclaimed** — see **Sprint 71**; **MenuFileManager** merge; **new** XP or sort rules. |

## Sprint 70 — **openmenu sources** + level progression → sources (**shipped**, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **openmenu** | Literal **`sources`**: **`openmenu sources <skill> [sort]`** (in-game self); **`openmenu sources <player> <skill> [sort]`** (console + in-game; **online** target). **`FabricCommandBuiltInMenuId`**, **`FabricCommandSuggestionProviders#builtInOpenMenuIds`**, **`FabricSourcesOpenSupport`** (shared with **`/sources`**). **`auraskills.command.openmenu`** only. |
| **Back / list page** | **Level progression** hopper at **slot 50** opens **sources** with **`context.listPage()`** so **Back** on sources returns to **level progression** with the same **Skills** return page; **`/sources`** / plain **`openmenu`** still use return page **0**. |
| **Manual** | **`SPRINT_70_MANUAL_SOURCES_OPENMENU.md`** |

| **Not in Sprint 70** | Full **`sources.yml`** load; **Slate** `sources` template items; **leaderboard** / **abilities**; **unclaimed** — see **Sprint 71**; **MenuFileManager** merge. |

## Sprint 71 — **Unclaimed items** menu (`/claimitems`, `/openmenu unclaimed_items`) (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Bukkit ref** | `bukkit/.../UnclaimedItemsMenu.java` — 6×9 Slate, one slot per `KeyIntPair`, click to merge one stack, close when empty; no paging (≤54 stacks visible). **No** separate `unclaimed_items.yml` in repo. |
| **Commands** | **`/claimitems`** (in-game) opens **`FabricUnclaimedItemsMenuGui`** when the list is non-empty; **`/claimitems all`** = prior **Sprint 56** one-shot merge. **`/openmenu unclaimed_items`** (self) and **`/openmenu unclaimed_items <player>`** (opens **on the target**). **`auraskills.command.claimitems`** (default); **`openmenu`** still **`auraskills.command.openmenu`**. **Console/RCON** on `/claimitems`: still **player-only** (unchanged). |
| **State** | **`FabricUnclaimedItemsMerge`** — **`mergeAllInOrder`** + **`mergeOneAtListIndex`**, same **`tryMergeRegisteredItemCount`** safety as before. Paging: **36** entries/page; **claim all** on slot 49. |
| **Gaps (Partial)** | No Slate/YAML; simplified vs full **ItemBuilder** lores; Bukkit can show **>54** only by scrolling? (Bukkit: 54); Fabric pages at 36+controls. **Bedrock** inventory clicks **NRP** — not run-time tested. |
| **Manual** | **`SPRINT_71_MANUAL_UNCLAIMED_ITEMS_MENU.md`** |

## Sprint 72 — **Leaderboard** menu + **`/openmenu leaderboard`** (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Bukkit ref** | `bukkit/.../LeaderboardMenu.java` — Slate 6×9, **skill** property, **leaderboard_player** template (10 rows, skulls), **back** → **level_progression**. Reference **`/top`** is **paged chat**, not this menu. **No** `leaderboard.yml` in repo. |
| **Menu** | **`FabricLeaderboardMenuGui`** — 6×9 Granite, **10** entries/page (two rows), **power** / **average** / **skill** modes, prev/next (48/50), refresh (49, re-read slice), **Back** → **Skills** (45). Same name resolution / rank math as **`FabricCommandLeaderboardText`**. |
| **openmenu** | **`openmenu leaderboard`** — self: default **power**; **`average`** + optional page; **`[page]`** for power; token = **online player** (opens **power** on **that** client) **or** **skill** (self **skill** board; in-game); **`token <skill> [page]`**, **`token average [page]`**, **`token <target_skill> [page]`** for target+skill. **`FabricCommandBuiltInMenuId`**, **`builtInOpenMenuIds`**. Console: **`leaderboard <player> average [page]`** or **`<player> <skill> [page]`** (no self skill-without-target from console). |
| **Commands unchanged** | **`/top`**, **`/skilltop`**, **`/rank`**, **`/skillrank`**, **`/updateleaderboards`** — still **chat** for players and **console/RCON** (Sprints 47–49). |
| **Gaps (Partial)** | Not Slate skull grid; no YAML **`leaderboard.yml`**; **Back** differs (Skills vs level_progression). **Bedrock** GUI clicks **NRP** — not run-time tested. |
| **Manual** | **`SPRINT_72_MANUAL_LEADERBOARD_MENU.md`** |

## Sprint 73 — **Abilities** menu + **`/openmenu abilities`** (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Bukkit ref** | `bukkit/.../AbilitiesMenu.java` — Slate: **`skill`** property, **locked_** / **unlocked_** ability + mana templates, components for **your_level** / **desc**, **back** → **level_progression** via **`GlobalItems`**. **No** `menus/abilities.yml` in repo (generated at runtime like other user files). |
| **Menu** | **`FabricAbilitiesMenuGui`** — 6×9 Granite, passive **Ability** rows + one **ManaAbility** slot when present, dye/star icon **read-only** rows, **Back** (45) → **`openLevelProgression`** (`returnToSkillsListPage` **0** from openmenu unless wired later). |
| **openmenu** | **`openmenu abilities`** (in-game: no-arg = first enabled skill **alphabetically** by key; **`<skill>`**; **`<token>`** disambiguates **skill** vs **online player** + default skill), **`abilities <player> <skill>`**. **`auraskills.command.openmenu`**. Console cannot use no-arg self form. |
| **Gaps (Partial)** | Not Slate/YAML slot maps; no per-template **ItemBuilder** skulls; **Bedrock** clicks **NRP**. |
| **Manual** | **`SPRINT_73_MANUAL_ABILITIES_MENU.md`** |

## Sprint 74 — **Confirmation** command wiring (`FabricCommandConfirmation`) (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Bukkit ref** | **`ConfirmManager`** — **per-issuer** `String` in a map, **30s** expiry, **ACF** “run twice with the same arg” (no separate confirm **inventory** menu in `bukkit/.../menus` for these flows). |
| **Command lane** | **`FabricCommandConfirmation`**: after a successful **preview**, register a **pending key**; **`confirm` executors** call **`verifyPendingOrError`** (clears on success). **backup** = resolved **`.yml` basename**; **preset** = resolved **`.zip` basename**; **mergeskills** = `storage:mergeskills:<fromKey>:<toKey>`; **removeallmodifiers** = constant. **Console/RCON** share one pending slot (see class javadoc). **No** new **menu**; **not** `MenuFileManager` / full Slate. |
| **Related** | **`SPRINT_74_MANUAL_CONFIRMATION_FLOWS.md`**, **COMMAND_PARITY_AUDIT**, **ledger** **AS-007** / **CMD-009/011/012** updates. |

## Sprint 75 — **Java dedicated-server** acceptance (process, not code)

| Area | Notes |
|------|--------|
| **Scope** | Real **dedicated** Fabric server + **Java** clients; verify **Sprints 57–74** commands/menus. **Not** Geyser/Bedrock **player** pass for this charter (see **Sprint 77** / **PAPER_PARITY_PROGRAM** §5). |
| **Doc** | **`SPRINT_75_MANUAL_JAVA_DEDICATED_ACCEPTANCE.md`** — checklist + **`Result` / `Notes`**. **Startup validation:** **PASS** on **FabricAuraSkills 2.3.106** + external **GraniteLib 2.3.104** (operator); see **`SPRINT_77_JAVA_COMMAND_MENU_SMOKE_RESULTS.md`**. **Full** command/menu matrix: **NRP** until filled. |

## Sprint 76A — **Dedicated-server startup** (anvil mixin + GraniteLib version text)

| Area | Notes |
|------|--------|
| **Blocker** | Mixin **apply** failed on **`AnvilScreenHandler`** with **`@Shadow` `field_22482`**; log often says **“No refMap loaded”** — see **`SPRINT_76A_STARTUP_BLOCKER_ANVIL_MIXIN.md`**. |
| **Fix** | **VarHandle**s for `ForgingScreenHandler#player` and `AnvilScreenHandler#levelCost` (Yarn + intermediary); **GraniteLib** **`${version}`** → **`processResources`** `expand` in **`slate-fabric/build.gradle.kts`** (**`granitelibVersion`**, e.g. **0.1.0-alpha.2**). |
| **Unblocks** | **Sprint 75** Java smoke once **`FabricAuraSkills-<ver>.jar`** boots on the test host. |

## Sprint 76B — **Dedicated-server startup** (tree / region `TreeFeature` mixin)

| Area | Notes |
|------|--------|
| **Blocker** | Mixin **apply** failed on **`TreeFeatureGrowContextMixin`**: **NPE** preparing **`@Inject` at HEAD** on **`Feature#generate`** (abstract in **`Feature.class`**) — see **`SPRINT_76B_STARTUP_BLOCKER_TREE_FEATURE_MIXIN.md`**. |
| **Fix** | **`@Mixin(TreeFeature.class)`** + full JVM **`generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z`** at **HEAD** / **RETURN**; behavior unchanged ( **`FabricTreeGrowContext`** ). |
| **Unblocks** | Next dedicated startup gate for **Sprint 75** once operator confirms **“Done”** with **2.3.105+** (or current **`projectVersion`**). |

## Sprint 76C — **Dedicated-server startup** (`FabricWorldManager` world lists)

| Area | Notes |
|------|--------|
| **Blocker** | **`FabricWorldManager#loadWorlds`** — **`UnsupportedOperationException`** on **`add`** to immutable **`Set.of()`** for **`check_block_replace.blocked_worlds`** — see **`SPRINT_76C_STARTUP_BLOCKER_WORLD_MANAGER.md`**. |
| **Fix** | **`blockedCheckBlockReplaceWorlds = Sets.newConcurrentHashSet()`** in **`loadWorlds`**, same as the other two sets. |
| **Unblocks** | **Sprint 75** once the server **boots** with **2.3.106+** (or current **`projectVersion`**) and operator confirms **“Done”**. |

## Sprint 77 — **Java dedicated command/menu smoke** (runtime validation, not code)

| Area | Notes |
|------|--------|
| **Scope** | **Java** players on a **dedicated** server; exercise **Sprint 75** checklist / representative commands and **built-in** menus; **no** new features. **Bedrock** command-menu UX **not** in scope. |
| **Doc** | **`SPRINT_77_JAVA_COMMAND_MENU_SMOKE_RESULTS.md`** — environment, JARs, startup proof, **Pass** on **Skills** menu for **two** Java players; full matrix **NRP** until operators complete tables. |
| **Links** | **`SPRINT_75_MANUAL_JAVA_DEDICATED_ACCEPTANCE.md`** (startup **PASS** + extended checklist). |

## Sprint 78 — **Java minimum smoke execution** (operator checklist; not in-repo)

| Area | Notes |
|------|--------|
| **Scope** | Run **`SPRINT_75`** / **`SPRINT_78`** **minimum** command+menu set on disposable dedicated server; **watch console**; **no** new features. **Bedrock** not tested. |
| **Doc** | **`SPRINT_78_JAVA_SMOKE_EXECUTION_RESULTS.md`** — pass/fail/pending; **Sprint 79** fixed **raw MiniMessage** in engine paths—re-test on **2.3.109+**. |
| **Links** | **`SPRINT_75_MANUAL_JAVA_DEDICATED_ACCEPTANCE.md`** (sync tables). |

## Sprint 79 — **Fabric text / MiniMessage rendering** (code)

| Area | Notes |
|------|--------|
| **Scope** | **Single lane:** `MessageProvider` + UI **action bar** / **level-up title** use **`FabricTextFormatter`** + **`FabricTextAdapter`**, not raw **`Text.literal`** for MiniMessage strings. **No** gameplay/XP change. **Bedrock** not run-time tested. |
| **Doc** | **`SPRINT_79_MANUAL_TEXT_RENDERING.md`**. **`projectVersion` 2.3.109** (code + JAR). |

## Sprint 80 — **Java smoke re-run + text fix verification** (operator, 2.3.109)

| Area | Notes |
|------|--------|
| **Scope** | Deploy **2.3.109** JAR, **full restart** first (Sprint 79 + cache); verify **no raw** MiniMessage in level-up / action bar / title; **Sprint 78** minimum set + console watch. **Not** a feature sprint. **Bedrock** not tested. |
| **Doc** | **`SPRINT_80_JAVA_TEXT_SMOKE_RESULTS.md`**, **`SPRINT_75_…`**, **`SPRINT_78_…`**. **`projectVersion` 2.3.110** = doc-only revision. |

