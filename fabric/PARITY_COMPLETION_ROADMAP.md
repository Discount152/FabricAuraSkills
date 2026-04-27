# Parity completion roadmap — Granite Skills / FabricAuraSkills vs AuraSkills + Slate

This file resets **honest accounting**: what is **stable**, **partial**, **missing**, or **deferred**, without equating “Java smoke passes” with **full reference parity**.

**Product intent:** **Granite Skills** (gameplay Fabric mod) + **GraniteLib** (reusable Fabric library, `id` **`granitelib`**) shipping as **two installable JARs**, with **AuraSkills + Slate** in the **`bukkit`** module as the **capability reference** (not a second runtime).

**Bedrock/Geyser:** validation **deferred** — do **not** claim release-grade cross-play until **`SPRINT_87`** (or equivalent) is executed.

---

## 1. Runtime stability (Java dedicated server)

| Area | Status | Notes |
|------|--------|--------|
| Startup / mixin safety | **Good** | Includes Sprint **76A–76C** fixes; see sprint manuals. |
| Core skills / XP / storage | **Good / evolving** | Shared `common` brain; edge cases may differ from Bukkit. |
| Java smoke matrix | **Closed enough** | **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`** — destructive rows **Skip** until disposable data. |

---

## 2. Command parity (Bukkit ACF reference → Fabric Brigadier)

| Bucket | Status |
|--------|--------|
| **Complete (for covered subtrees)** | Many admin/player commands **exist** on Fabric with aligned intent (see **`COMMAND_PARITY_AUDIT.md`** table). |
| **Partial** | **Most** roots: narrowed args, different alias topology, shorter help, `SkillsRoot` vs built-in menus, etc. |
| **Missing / N/A** | Reference-only or **out-of-scope** integrations (see **`PARITY_BACKLOG.md`**). |
| **JobsCommand** | **Deferred / out of active scope** unless the owner explicitly reopens it. |

**Source of truth for per-command rows:** **`fabric/COMMAND_PARITY_AUDIT.md`**.

---

## 3. Menu parity (Slate / YAML / `MenuFileManager`)

| Reality | Status |
|---------|--------|
| **Built-in Fabric menus** (`Fabric*MenuGui`, registry) | **Functional partial replacement** — sufficient for **Java smoke** and many operator flows. |
| **Full Slate parity** (YAML-driven menus, `MenuFileManager`, full template/context/action surface) | **Not complete** — **`MENU_AND_COMMANDS_PORT.md`** Path A remains the **program target**. |

**Per-menu snapshot (built-in Granite layer today):**

| Menu / flow | Built-in Fabric | Full Slate/YAML parity |
|-------------|-----------------|-------------------------|
| skills | **Present** | **Partial** |
| stats | **Present** | **Partial** |
| level_progression | **Present** | **Partial** |
| stat_info | **Present** | **Partial** |
| sources | **Present** | **Partial** |
| unclaimed_items | **Present** | **Partial** |
| leaderboard | **Present** | **Partial** |
| abilities | **Present** | **Partial** |
| confirm flows (backup/preset/storage) | **Brigadier `confirm`** (not inventory GUI framework) | **Partial** vs reference **ConfirmManager** model |
| File-backed menus / arbitrary Slate menus | **Missing** | **Open** |

**Rule:** Do **not** describe built-in menus as “Slate parity complete” or “menus done.”

---

## 4. GraniteLib / Slate-style framework parity

GraniteLib (`:granitelib-fabric`, on-disk **`slate-fabric/`) is a **real library** (`granitelib` mod id); it must remain **installable standalone** for reuse and honest versioning.

| Track | Status |
|-------|--------|
| **Container / click plumbing** (e.g. `GraniteContainerMenus`) | **Partial** — enough for current built-ins. |
| **Template loading, YAML layouts, `MenuFileManager`** | **In progress** — v0 YAML + loader (**95**); **`stat_info`** / **`sources`** / **`level_progression`** / **`skills`** / **`stats`** / **`abilities`** / **`leaderboard`** / **`unclaimed_items`** template paths (**96–98**, **100–105**); **Sprint 106** — **disk-first** **`config/auraskills/granite/templates/<id>.yml`** via **`FabricGraniteMenuTemplateLoader`**; **Sprint 107** — diagnostics debounce (**`SPRINT_107_MANUAL_TEMPLATE_RELOAD_LIFECYCLE.md`**); **Sprint 108** — **`templates export`** (**`SPRINT_108_MANUAL_TEMPLATE_EXPORT.md`**); **Sprint 109** — guarded **overwrite** (**`SPRINT_109_MANUAL_TEMPLATE_EXPORT_OVERWRITE.md`**); **Sprint 110** — **`FabricGraniteMenuTemplateRegistry`** + **`templates status`** (**`SPRINT_110_MANUAL_TEMPLATE_REGISTRY_LIFECYCLE.md`**); **Sprint 111** — registry **diagnostics** / multiline **`templates status`** (**`SPRINT_111_MANUAL_TEMPLATE_STATUS_DIAGNOSTICS.md`**); **Sprint 112** - per-menu **`action`** contract (**`SPRINT_112_MANUAL_TEMPLATE_ACTION_CONTRACT.md`**); **Sprint 99** — **`GraniteTemplatePlaceholders`** / **`FabricGraniteTemplateStacks`**; **not** full Slate / `MenuFileManager` |
| **Contexts, paging, global items, rich click actions** | **Not complete** |
| **Public API + docs for external mods** | **Partial / evolving** |

**Sprint 94 (planning only):** **`SPRINT_94_GRANITELIB_SLATE_PARITY_PLAN.md`** — dedicated **Path A** roadmap: Slate vs GraniteLib capability inventories, gap table, menu-by-menu classification, phased implementation (**A–H**), risks, future acceptance criteria, and **Sprint 95** (Spike 0) recommendation. **No code** in Sprint 94; **Bedrock/Geyser** remains deferred.

**Sprint 95 (Spike 0 — GraniteLib code):** **`SPRINT_95_GRANITELIB_TEMPLATE_SPIKE0.md`** — `dev.granitelib.fabric.template.{model,load}`: immutable **`GraniteMenuTemplate`** / items, **SnakeYAML** read-only loader + validation, unit tests; **JiJ** SnakeYAML in **`granitelib-fabric`**. **No** production menu behavior change; **Slate parity** still **not** complete.

**Sprint 96 (`stat_info` prototype):** **`SPRINT_96_MANUAL_STAT_INFO_TEMPLATE_PROTOTYPE.md`** — bundled **`assets/auraskills/granite/templates/stat_info.yml`**; **`FabricStatInfoTemplateSupport`** + minimal placeholders; **safe fallback** to built-in **`FabricStatInfoMenuGui`**. **Not** full Slate parity; **Bedrock** still deferred.

**Sprint 97 (`stat_info` template parity):** **`SPRINT_97_MANUAL_STAT_INFO_TEMPLATE_PARITY.md`** — **Option 2** trait lore appended after static YAML; **`FabricStatInfoItemComponents`** applies **`custom_model_data`** via **`CustomModelDataComponent`**; trait failure → **built-in fallback**. **Slate** still **not** complete.

**Sprint 98 (`sources` template prototype — dynamic rows):** **`SPRINT_98_MANUAL_SOURCES_TEMPLATE_PROTOTYPE.md`** — bundled **`sources.yml`**; **`FabricSourcesTemplateSupport`** + **`FabricSourcesPlaceholders`**; template **first**, then **`FabricSourcesMenuGui` built-in**; chat **`FabricSourcesOpenSupport`** unchanged. First **dynamic** GraniteLib menu stress test. **Slate** still **not** complete.

**Sprint 99 (template runtime consolidation):** **`SPRINT_99_TEMPLATE_RUNTIME_CONSOLIDATION.md`** — **`GraniteTemplatePlaceholders`**, **`GraniteTemplateActionNames`**, **`GraniteTemplateItems`**, **`GraniteTemplateResources`** (GraniteLib); **`FabricGraniteTemplateStacks`** (strict items + stacks); refactors **`stat_info`** / **`sources`** support only (**no** new menu). **Slate** still **not** complete.

**Sprint 100 (`level_progression` template prototype — level grid + paging):** **`SPRINT_100_MANUAL_LEVEL_PROGRESSION_TEMPLATE_PROTOTYPE.md`** — bundled **`assets/auraskills/granite/templates/level_progression.yml`**; **`FabricLevelProgressionTemplateSupport`** + **`FabricLevelProgressionPlaceholders`**; **28** level slots + **previous**/**next** over **1..maxLevel**, **back**/**sources**/**abilities** wired like built-in intent; **`FabricMenuContext#progressionPage`**; strict fallback to **`FabricLevelProgressionMenuGui` built-in**. **Slate** still **not** complete; **Bedrock** deferred.

**Sprint 101 (`skills` / `stats` list template prototypes):** **`SPRINT_101_MANUAL_SKILLS_STATS_TEMPLATE_PROTOTYPE.md`** — bundled **`skills.yml`** / **`stats.yml`**; **`FabricSkillsTemplateSupport`** / **`FabricStatsTemplateSupport`** + local placeholder expanders; **28**-slot grids + **45**/**53** paging; row clicks match built-in (**level progression** / **stat_info** gate); strict fallback to **`FabricSkillsMenuGui`** / **`FabricStatsMenuGui` built-in**. **Slate** still **not** complete; **Bedrock** deferred.

**Sprint 102 (`abilities` template prototype):** **`SPRINT_102_MANUAL_ABILITIES_TEMPLATE_PROTOTYPE.md`** — bundled **`abilities.yml`**; **`FabricAbilitiesTemplateSupport`**; passive grid matches built-in **14** slot order; **mana** + **back** wiring; strict fallback to **`FabricAbilitiesMenuGui` built-in**. **Slate** still **not** complete; **Bedrock** deferred.

**Sprint 103 (`leaderboard` template prototype):** **`SPRINT_103_MANUAL_LEADERBOARD_TEMPLATE_PROTOTYPE.md`** — bundled **`leaderboard.yml`**; **`FabricLeaderboardTemplateSupport`** + **`FabricLeaderboardPlaceholders`**; **10** ranked rows + paging + **refresh**; **paper** entry material like built-in; nav slots match **45**/**48**/**49**/**50**; strict fallback to **`FabricLeaderboardMenuGui` built-in**. **Slate** still **not** complete; **Bedrock** deferred.

**Sprint 104 (working-tree containment):** **`SPRINT_104_WORKING_TREE_CONTAINMENT.md`** — hygiene only: inventory **post–Sprint 103** modified/untracked paths; resolve **phantom** tracked modifications (index refresh); document **optional** clone-local excludes for scratch docs; confirm **two-JAR** / **`depends.granitelib`** policy on committed tree. **No** Path A menu code; **no** version bump.

**Sprint 105 (`unclaimed_items` template prototype):** **`SPRINT_105_MANUAL_UNCLAIMED_ITEMS_TEMPLATE_PROTOTYPE.md`** — bundled **`unclaimed_items.yml`**; **`FabricUnclaimedItemsTemplateSupport`** + **`FabricUnclaimedItemsPlaceholders`**; **36** slots (**0–35**); rows use **real** registered **`ItemStack`s** + YAML **lore** overlay; **claim_item** / **claim_all** / paging actions; all merges via existing **`FabricUnclaimedItemsMerge`**; strict **`FabricUnclaimedItemsMenuGui` built-in** fallback. **Slate** still **not** complete; **Bedrock** deferred.

**Sprint 106 (disk template loading prototype):** **`SPRINT_106_MANUAL_DISK_TEMPLATE_LOADING.md`** — **`config/auraskills/granite/templates/<menu_id>.yml`** loads before bundled **`assets/auraskills/granite/templates/*.yml`** when valid; **`FabricGraniteMenuTemplateLoader`**; invalid disk → once-per-path **warn** + bundled; **no** auto-copy / file watcher; eight **`Fabric*TemplateSupport`** menus. **Slate** / **`MenuFileManager`** still **not** complete; **Bedrock** deferred.

**Sprint 107 (template reload lifecycle):** **`SPRINT_107_MANUAL_TEMPLATE_RELOAD_LIFECYCLE.md`** — **`reloadFromDisk()`** clears loader debounce; **Sprint 110** superseded per-open disk reads for menus with **`FabricGraniteMenuTemplateRegistry#reloadAll`**. **No** file watcher. **Slate** / **`MenuFileManager`** still **not** complete; **Bedrock** deferred.

**Sprint 108 (bundled template export):** **`SPRINT_108_MANUAL_TEMPLATE_EXPORT.md`** — **`/auraskills templates export`** and **`/skills|/sk|/skill`** mirrors; **`auraskills.command.templates`**; **`FabricGraniteMenuTemplateExporter`** + **`FabricGraniteBundledMenuTemplateIds`** — writes **only missing** files under **`granite/templates/`**; **no** overwrite; **no** `reloadFromDisk()` side effect. **Slate** / **`MenuFileManager`** still **not** complete; **Bedrock** deferred.

**Sprint 109 (guarded template overwrite):** **`SPRINT_109_MANUAL_TEMPLATE_EXPORT_OVERWRITE.md`** — **`templates export overwrite`** (**preview**, no disk writes; **console/RCON** preview **arms** **`FabricCommandConfirmation`**), **`templates export overwrite confirm`** (**console/RCON only**) — **`created` / `overwritten` / `failed`**; safe export unchanged. **Slate** / **`MenuFileManager`** still **not** complete; **Bedrock** deferred.

**Sprint 110 (template registry / lifecycle):** **`SPRINT_110_MANUAL_TEMPLATE_REGISTRY_LIFECYCLE.md`** — **`FabricGraniteMenuTemplateRegistry`**, **`FabricGraniteMenuTemplateBindings`**, **`loadManagedCore`**, **`templates status`**; **`reloadFromDisk`** refreshes cached **`GraniteMenuTemplate`** instances; disk edits require reload. **Slate** / **`MenuFileManager`** still **not** complete; **Bedrock** deferred.

## 5. Packaging / release architecture

| Policy | Canonical |
|--------|-----------|
| **Production install** | **Two JARs:** `FabricAuraSkills-<version>.jar` + `granitelib-fabric-<version>.jar` (paths from Gradle; see **`SPRINT_93_TWO_JAR_RELEASE_AND_PARITY_RESET.md`**). |
| **Gameplay mod `fabric.mod.json`** | Declares **`depends.granitelib`** (see live JSON). |
| **JiJ GraniteLib** | **Non-canonical** — opt-in **only** via **`-PembedGraniteJij=true`** for legacy/dev convenience; **not** the final product story. |

---

## 6. Bedrock / Geyser

| Status | Notes |
|--------|--------|
| **Deferred** | **`SPRINT_87_MANUAL_BEDROCK_GEYSER_VALIDATION.md`** prepared; **not** executed. |

---

## 7. Recommended sequencing (high level)

1. **Hold** “final release” until **two-JAR** policy is what operators run in production and docs match.
2. **Execute** Bedrock validation when a tester exists.
3. **Drive** Slate/GraniteLib framework work as its own program (Path A) using **`SPRINT_94`**+ docs, not as a side effect of smoke fixes. **Sprint 105** templates **`unclaimed_items`** (real stacks + claim merge). **Sprint 108–110** **`templates export`** + guarded **overwrite** + **registry lifecycle** (**`reload`** refreshes cache). **Next:** deeper **`MenuFileManager`** / Slate action surface — still **not** full **Slate-complete**.
4. **Revisit** Jobs / economy bridges only if the owner reopens scope.
