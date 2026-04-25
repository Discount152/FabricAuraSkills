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
| **Commands** | **`FabricPlayerUtilityCommands`**: **`/multiplier`** (self) and **`/multiplier <player>`**; **`/toggle`** and **`/abtoggle`** (same **action bar IDLE** logic, **Bukkit** shared perm **`abtoggle`**); **`/lang <code>`** with suggestions from **`MessageProvider#getLanguageCodes`**. |
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

## Sprint 56 — **`/claimitems`** (unclaimed → inventory) (shipped, partial)

| Area | Shipped on Fabric (bounded) |
|------|-----------------------------|
| **Commands** | **`/claimitems`**, **`/skills\|/sk\|/skill\|/auraskills claimitems`**, `auraskills.command.claimitems` (Bukkit: default **true** under `command.user`); **player**-only; empty list → **`CLAIMITEMS_NO_ITEMS`** |
| **State** | **`User#getUnclaimedItems()`**; **`tryMergeRegisteredItemCount`** in **`FabricItemRegistry`** (shared with **`/item give`** / **`addRegisteredItemToInventory`**); **`setShouldSave`** on list change |
| **Not in Sprint 56** | Bukkit **Slate** **UnclaimedItemsMenu** (per-slot **click** UI); see **`fabric/COMMAND_PARITY_AUDIT.md`**. **Manual:** `SPRINT_56_MANUAL_CLAIMITEMS.md`. |
