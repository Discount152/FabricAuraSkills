# FabricAuraSkills — Phase 1 parity audit (repo-anchored)

> **Runtime / product:** The **shipped** server mod runs on **Java Fabric** only; it must support **Java players** and **Bedrock players through Geyser + Floodgate** (see [PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md) — *Client / player access*). This ledger does **not** imply Paper or Bukkit at runtime. It maps **Granite Skills** (Fabric) against the **reference `bukkit` module** — **capability inventory**, not a second platform. **AuraSkills**-named commands/menus are **scope references**, not final **Granite** surface.

**Purpose:** Expand the seeded **`fabricauraskills_parity_audit_sheet.xlsx`** into a **repo-anchored** planning spine using this file as the **canonical Pass 1** list (bootstrap + commands + menus + listener map). The **workbook** is the editable **dashboard** (charts, filters); it must not become a second source of truth — **if a row disagrees, this markdown in-repo wins** until reconciled.

**Sprint 77 / 78 (process) + 79 (code) + 80 (operator re-run) + 81 (idle action bar) + 81A (legacy migration) + 82 (Java full smoke) + 83 (smoke patch) + 84 (admin permissioned retest) + 85 (command tree roots) + 86 (matrix closeout / LuckPerms) + 87 (Bedrock/Geyser validation):** **Startup** was **PASS** for **2.3.106** (Sprint 77). **Sprint 78** minimum table **NRP**. **Sprint 79** fixed **MiniMessage** pipeline ( **`SPRINT_79_MANUAL_TEXT_RENDERING.md`**, JAR **2.3.109**). **Sprint 80** — in-game **2.3.109** smoke + text proof: **`SPRINT_80_JAVA_TEXT_SMOKE_RESULTS.md`**, doc **`2.3.110`**, **NRP** until operator. **Sprint 81** — **smart** idle health/mana action bar: **`SPRINT_81_MANUAL_SMART_ACTION_BAR.md`**, JAR **2.3.111**; **Bedrock** **NRP**. **Sprint 81A** — legacy **`idle` → `idle_mode`**, **`/abtoggle` subcommands**: **`SPRINT_81A_MANUAL_ACTION_BAR_LEGACY_MIGRATION.md`**, JAR **2.3.112**. **Sprint 81B** — stable **`!wasInWindow`**: **`SPRINT_81B_…`**, JAR **2.3.113**. **Sprint 81C** — **`SmartIdleObsDisplay`**: **`SPRINT_81C_MANUAL_SMART_ACTION_BAR_STUCK_FIX.md`**, JAR **2.3.114**. **Sprint 82** — full **Java dedicated** command/menu/admin matrix: **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`**, doc **`2.3.115`**, **NRP** until operator (artifact test **`2.3.114+`**). **Sprint 83** — **Code accepted** (**2026-04-26**); **`FabricGraniteText`**, **`/mana view` self** + **`view <p>`** **OTHER**; **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`**, JAR **`FabricAuraSkills-2.3.116.jar`**. **Sprint 82** matrix **Partial**: core/menu/action-bar/mana rows **Pass**; **`/xp view`** / **`/skilllevel view`** — operator **Pass** on **`2.3.117+`** (readable, no raw keys). **Sprint 84** (**`SPRINT_84_MANUAL_COMMAND_SMOKE_PATCH.md`**, **`2.3.117`**): **`FabricGraniteText`**, no top-level **`/item`**/**`/xp`**, admin **`permissionOrNonPlayerSource`**, **`/storage mergeskills`** **`requires`**. **Sprint 85** (**`SPRINT_85_MANUAL_COMMAND_TREE_PATCH.md`**, **`2.3.118`**): bare-root **`item`**, **`storage`**, **`backup`**, **`preset`** — root **`.executes`** + **`requires`**. **Sprint 86 (docs):** **`SPRINT_82`** matrix **closed enough** for **Java** — **`auraskills.*`** (LuckPerms) on tester; final in-game **Pass** for **`storage`**, **`backup`**, **`preset`**, prefixed **`item`** roots; destructive rows **Skip** until disposable runs. **Sprint 87:** **`SPRINT_87_MANUAL_BEDROCK_GEYSER_VALIDATION.md`** — **Bedrock/Geyser** validation manual **prepared**; **runtime pass deferred** (no available Bedrock tester). **Sprint 88:** **`SPRINT_88_MANUAL_MENU_ITEM_LORE_TRANSLATION_SWEEP.md`** — built-in **`Fabric*MenuGui`** titles/items/lore + list paging + **`openmenu`** menu feedback: **`FabricGraniteText`** (no raw **`auraskills.granite.*`** via **`Text.translatable`** on those paths); tests extended; **`2.3.119`**. **Sprint 89:** **`SPRINT_89_MANUAL_SOURCES_CHAT_TRANSLATION.md`** — **`sendChatListing`**, **`sources.empty`**, **`sources.bad_sort`** → **`FabricGraniteText`**; **`2.3.120`**. **Sprint 90:** **`SPRINT_90_MANUAL_GLOBAL_GRANITE_TEXT_AUDIT.md`** — global Fabric **`Text.translatable("auraskills.granite…")`** removal on command surfaces; **`FabricGraniteJavaSourcesTranslatableScanTest`**; **`2.3.121`**. **Operator** Java checklist **Pass**. **Sprint 91:** **`SPRINT_91_MANUAL_FABRIC_RELEASE_READINESS.md`** — **76A** mixin **`VarHandle`** + **`slate-fabric`** **`${version}`** expansion committed; release/install docs; **`2.3.122`**. **Sprint 92:** **`RELEASE_NOTES_2.3.122.md`**, **`SPRINT_92_RELEASE_CANDIDATE_PREP.md`** — RC documentation; **`v2.3.122-fabric-rc1`** (JiJ-default **install wording** — **not** final packaging). **Sprint 93:** **`SPRINT_93_TWO_JAR_RELEASE_AND_PARITY_RESET.md`**, **`PARITY_COMPLETION_ROADMAP.md`** — **canonical two-JAR** install (gameplay + **`granitelib-fabric`**), **`depends.granitelib`**, opt-in **`embedGraniteJij`** only; **`2.3.123`**, **`RELEASE_NOTES_2.3.123.md`**, recommended **`v2.3.123-fabric-rc2`**. **Sprint 94:** **`SPRINT_94_GRANITELIB_SLATE_PARITY_PLAN.md`** — **Path A** planning only: Slate vs GraniteLib inventories, gaps, menu-by-menu table, phases **A–H**, **Sprint 95** Spike 0; **no** code; **MENU-*** rows stay **Partial** until implementation sprints. **Sprint 95:** **`SPRINT_95_GRANITELIB_TEMPLATE_SPIKE0.md`** — GraniteLib **`template.model` / `template.load`**, v0 YAML + **`GraniteMenuTemplateYamlLoader`**, tests; **no** production menu wiring; **AS-006** / **MENU-*** still **Partial**. **Sprint 96:** **`SPRINT_96_MANUAL_STAT_INFO_TEMPLATE_PROTOTYPE.md`** — bundled **`stat_info.yml`** template path + **`FabricStatInfoTemplateSupport`**; **safe** built-in fallback; **`2.3.124`**. **Sprint 97:** **`SPRINT_97_MANUAL_STAT_INFO_TEMPLATE_PARITY.md`** — trait lore append + **`custom_model_data`**; **`2.3.125`**. **AS-006** / **MENU-*** still **Partial** (not Slate-complete). **Next:** **Sprint 98** — **`sources`** template or further menu work; **87** when a Bedrock tester exists.

**Sprint 106 update:** Operator disk YAML **`config/auraskills/granite/templates/<menu_id>.yml`** loads before bundled **`assets/auraskills/granite/templates/*.yml`** when valid (**`FabricGraniteMenuTemplateLoader`**); invalid disk → **once-per-path** warn + bundled; **`SPRINT_106_MANUAL_DISK_TEMPLATE_LOADING.md`**; **`FabricAuraSkills-2.3.133.jar`**. **Sprint 107:** **`reloadFromDisk()`** clears template loader diagnostics debounce (**`SPRINT_107_MANUAL_TEMPLATE_RELOAD_LIFECYCLE.md`**); **`FabricAuraSkills-2.3.134.jar`**. **Sprint 108:** **`/auraskills templates export`** (**`SPRINT_108_MANUAL_TEMPLATE_EXPORT.md`**, **`auraskills.command.templates`**); **`FabricAuraSkills-2.3.135.jar`**. **Sprint 109:** **`templates export overwrite`** + **`… confirm`** (**`SPRINT_109_MANUAL_TEMPLATE_EXPORT_OVERWRITE.md`**); **`FabricAuraSkills-2.3.136.jar`**. **Sprint 110:** **`FabricGraniteMenuTemplateRegistry`**, **`templates status`** (**`SPRINT_110_MANUAL_TEMPLATE_REGISTRY_LIFECYCLE.md`**); **`FabricAuraSkills-2.3.137.jar`**. **Sprint 111:** registry **diagnostics** for **`templates status`** (**`SPRINT_111_MANUAL_TEMPLATE_STATUS_DIAGNOSTICS.md`**); **`FabricAuraSkills-2.3.138.jar`**. **Sprint 112 / 113:** **Sprint 112** — menu **visual/layout** review (**`SPRINT_112_MENU_AESTHETIC_REVIEW.md`**, docs + operator screenshots **NRP**). **Sprint 113** — template **`action:`** contract (**`SPRINT_113_MANUAL_TEMPLATE_ACTION_CONTRACT.md`**; **`Changelog`** may say Sprint 112); **`FabricAuraSkills-2.3.139.jar`**. **Sprint 114** — skills/stats **hub tooltip polish** (**`SPRINT_114_SKILLS_STATS_TOOLTIP_POLISH.md`**); **`FabricAuraSkills-2.3.143.jar`** — **`TOOLTIP_DISPLAY`** + compact hub copy + **brewing stand** Alchemy. **Sprint 115** — **hub tooltip + data** (**`SPRINT_115_SKILLS_STATS_TOOLTIP_AND_DATA_FIX.md`**); **`FabricAuraSkills-2.3.144.jar`** — formatted template text (no raw **`&`**), **`User`** skill map id resolution, **lifetime XP** hub line, **strip** food/durability components on menu icons. **Sprint 116** — **level_progression** template + fallback visual polish (**`SPRINT_116_LEVEL_PROGRESSION_VISUAL_POLISH.md`**); **`FabricAuraSkills-2.3.145.jar`** — hub-aligned title, state icons (lime / XP bottle / gray), compact row lore, control items, **`FabricMenuTooltipPresentation`** on progression stacks; built-in fallback title **`%s Levels`** and knowledge-book sources + suppression. **MENU-*** / Slate rows remain **Partial**. **Bedrock/Geyser** still **not** run-time tested.

**Sprint 120 (repair / visual QA gate):** **`SPRINT_120_MENU_PRESENTATION_REPAIR_GATE.md`** — fixes Sprint **119** failed screenshot pass: Stat Info / Sources / Abilities / Leaderboard presentation (titles, lore via **`FabricTextFormatter`**/**`FabricTextAdapter`**, Iron Door back where specified, quiet filler policy, bundled **`leaderboard.yml`** without glass fill / gold nugget rows). **`FabricAuraSkills-2.3.149.jar`**. **Acceptance** for this lane: **build PASS + operator screenshot PASS** — not build alone. **Bedrock/Geyser** still **not** tested.

**Sprint 121D (global menu pipeline enforcement):** **`SPRINT_121D_GLOBAL_MENU_PIPELINE_ENFORCEMENT.md`** — **all** Granite menu Java paths share **`FabricMenuLoreFormatting`** (42-char wrap), **`FabricMenuItemPresentation`**, and **`FabricMenuTooltipPresentation`**; built-in **level progression** uses **empty** off-path slots; **`FabricMenuJavaPipelineScanTest`** guards glass-pane / raw MiniMessage string regressions in listed sources. **`FabricAuraSkills-2.3.150.jar`**. **MENU-*** ledger rows stay **Partial** vs **full Slate**; this sprint is **presentation consistency**, not **`MenuFileManager`** completion. **Bedrock/Geyser** still **not** tested.

**Reference anchor files (Bukkit module — audit source, not runtime):** `bukkit/.../AuraSkills.java`, `CommandRegistrar.java`, `MenuRegistrar.java` (plus current Fabric mappings in `fabric/...`).

**Status values** (use in `Audit Matrix` → Status): `Missing` | `Stub` | `Partial` | `Equivalent` | `Needs Regression Proof` — prefer **Partial** or **Needs Regression Proof** over **Stub** when Fabric already implements real logic.

**Blocker tags** (use in Blocker / dependency column): `Slate-Fabric` | `ACF/Brigadier` | `Out of scope` | `Geyser/Bedrock` | `Common seam` | `None` — use **`Out of scope`** for **third-party Bukkit/Paper plugin ecosystems** that are not part of the default Granite Skills / GraniteLib product; **`Common seam`** for **in-repo** shared API (e.g. `api-bukkit` reference) without implying an external plugin roadmap.

### Row family interpretation

| Family | Parity focus | Granite / final UX | Java vs Bedrock (Geyser / Floodgate) |
|--------|----------------|-------------------|--------------------------------------|
| `AS-*` | Hard lifecycle and architecture parity; mostly branding-neutral. | Rebrand impact usually **None** except user-visible bootstrap messages. | Check any **server-visible** message or UI default that Bedrock may render differently. |
| `CMD-*` | **Capability inventory** — reference vs Fabric; map ACF classes. | **Not** the final public namespace. | **High risk:** command feedback, suggestions, permissions; validate **both** client paths. |
| `MENU-*` | Flow + Slate behavior parity. | Titles, lore, button copy TBD for Granite. | **High risk:** inventory/GUI; must work on **Bedrock via Geyser** or be **documented** as limited. |
| `EVT-*` | Hard gameplay parity (XP, loot, death, regions, anti-exploit). | Mostly branding-neutral. | Re-check any **action bar, title, chat, or particles** for cross-platform display. |

---

## 1. Workbook alignment (recommended column use)

| Column (suggested name) | Purpose |
|-------------------------|--------|
| `ID` | Stable key, e.g. `AS-014`, `CMD-003` |
| `Category` | `Bootstrap` \| `Commands` \| `Menus` \| `Listeners` \| `Integrations` \| `API` |
| `Bukkit / baseline reference` | Class or `file#method` (Paper capability inventory) |
| `Current Fabric reference` | Class or `file#method`, or `—` |
| `Parity status` | Same vocabulary as **Status** above |
| `Blocker` | From tags above + free text |
| `Notes` | Behavior gap, test idea, doc link |
| *Optional:* `Final Granite UX impact` | e.g. `None` \| `Rebrand required` \| `Rename later` \| `New command design required` |
| *Optional:* `Final public name / command` | e.g. `TBD` until rebrand sprint |
| *Optional:* `Ship requirement` | e.g. `Parity` \| `Final release` \| `Optional API` |
| *Optional:* `Regression proof` | `Yes` / `No` |
| *Optional:* `Java path status` | e.g. `Untested` \| `Partial` \| `Equivalent` \| `Broken` |
| *Optional:* `Bedrock (Geyser/Floodgate) status` | e.g. `Untested` \| `Partial` \| `Equivalent` \| `Broken` \| `Needs alternate UX` |
| *Optional:* `Bedrock / Geyser impact` | `None` \| `Needs testing` \| `Likely incompatible` \| `Needs alternate UX` \| `Blocked by Geyser/Floodgate` |
| *Optional:* `Cross-platform UX note` | Free text: Java vs Bedrock differences, Floodgate `UUID`/permission caveats, etc. |
| *Optional:* `Bedrock-safe acceptance required` | `Yes` / `No` (use **Yes** for `CMD-*`, `MENU-*`, and visible `EVT-*` / UI rows) |

**Import:** Copy the **CSV block** in §5 into a new sheet (e.g. `Phase1_import`) and `Data > From Text/CSV`, or paste rows into `Audit Matrix`. Adjust column order to match your workbook.

---

## 2. `AuraSkills.java` — bootstrap & lifecycle (ordered)

| ID | Bukkit reference | Fabric reference | Status | Blocker | Notes |
|----|------------------|------------------|--------|---------|-------|
| AS-001 | `ApiAuraSkills` + `ApiRegistrationUtil` | `FabricAuraSkills` + `ApiAuraSkills` + `ApiRegistrationUtil` | Equivalent | None | Core API registered. |
| AS-002 | `ApiAuraSkillsBukkit` + `ApiBukkitRegistrationUtil` + `ApiItemManager` | — | Missing | Common seam | `api-bukkit` / Bukkit menu API not on Fabric. |
| AS-003 | `BukkitLogger` | `FabricLogger` | Equivalent | None | |
| AS-004 | `BukkitMessageProvider` | `FabricMessageProvider` | Partial | None | **Sprint 79** — **`stringToComponent`** / **`applyFormatting`** / **`componentToString`** match Bukkit **`TextFormatter`** via **`FabricTextFormatter`**; **`FabricUiProvider`** action bar + title use **`FabricTextAdapter`**. **`SPRINT_79_MANUAL_TEXT_RENDERING.md`**. **NRP** cross-client proof. |
| AS-005 | Managers/registries (skills, abilities, stats, traits, …) | Same pattern in `FabricAuraSkills` | Partial | None | Shared `common` brain; edge cases may differ. **Sprint 76C** — **`FabricWorldManager#loadWorlds`**: all three world-name sets (`blocked_worlds`, `disabled_worlds`, `check_block_replace.blocked_worlds`) must be **mutable** each load — **`SPRINT_76C_STARTUP_BLOCKER_WORLD_MANAGER.md`**. |
| AS-006 | `Slate` + `MenuOptions` + `InventoryManager` + `MenuFileManager` | `GraniteContainerMenus` + `FabricMenuHelper` + `FabricMenuRegistry` + `FabricMenuLoaders` | Partial | Slate-Fabric + NRP | **Sprint 29** — code registry + `skills`/`stats` **FabricMenuDefinition**; no file **`MenuFileManager`**, no full Slate. |
| AS-007 | `ConfirmManager` | `FabricCommandConfirmation` (**Sprint 74** — chat/Brigadier; **30s** + **per-subject** pending **key**; not inventory) | Partial | Full Slate “confirm UIs” + arbitrary menus | Bukkit: **ACF** double-step string match; **no** confirm **inventory** class in `bukkit/.../menus` grep. Fabric: **literal `confirm`** + shared helper on **backup|preset|storage**; **AS-007** row tracks **Slate**-style **GUI** confirm framework if ever needed. |
| AS-008 | `CommandRegistrar.registerCommands` → `PaperCommandManager` | `FabricAuraSkillsCommands` | Partial | ACF/Brigadier | Small Brigadier subset vs full ACF tree (see §3). |
| AS-009 | `messageProvider.setACFMessages` | — | Missing | ACF/Brigadier | No ACF message integration. |
| AS-010 | `BukkitLevelManager` + `registerLevelers` | `FabricLevelManager` + `registerFabricXpHandlers` | Partial | None | Levelers wired via Fabric handlers/mixins; compare to Bukkit list. **Sprint 10** — `StatisticLeveler`. **Sprint 11** — `BrewingLeveler` hopper. **Sprint 12** — `EVT-021`. **Sprint 13** — `EVT-022` jumping. **Sprint 14** — `EVT-023` enchanting. **Sprint 15** — `EVT-024` grindstone. **Sprint 16** — `EVT-025` anvil. **Sprint 17** — `EVT-026` item consume. **Sprint 18** — `EVT-027` potion splash. |
| AS-011 | `BukkitAntiAfkManager` | `FabricAntiAfkManager` | Partial | None | `common` checks; verify parity with Bukkit listeners. |
| AS-012 | `Metrics` + `MetricsUtil` | — | Missing | Out of scope | Reference optional metrics; not default Granite product scope. |
| AS-013 | `UpdateChecker` | — | Missing | None | Optional product parity. |
| AS-014 | `onLoad` → Bukkit region-flag hook (reference) | — | Missing | Out of scope | `FabricRegionNode` is a stub; third-party region plugins are not a default target. |
| AS-015 | First-tick: `SkillsLoadEvent` | — | Missing | Common seam | No Bukkit event fired; add Fabric/common event if API parity required. |
| AS-016 | First-tick: `requirementManager` after skills | `FabricRequirementManager` in `finishEnable` | Partial | Out of scope | `region` requirement type: no external region provider in default scope; node passes. |
| AS-017 | First-tick: `lootManager.loadLootTables` | `FabricLootManager.loadLootTables` | Partial | None | Compare to Bukkit loot + handler coverage. |
| AS-018 | `traitManager.registerTraitImplementations` | `FabricTraitManager.registerTraitImplementations` | Partial | None | **Sprint 19–22** — all **14** Bukkit `registerTrait` groups have Fabric handlers — **`SPRINT_19_TRAIT_PARITY.md`**, Sprint 20–21 docs, **`SPRINT_22_DOUBLE_DROP_PARITY.md`**. **`DoubleDropTrait`:** **Partial** for optional Bukkit **WorldGuard** / **`LootDropEvent`**; **in-scope core** block-break path — **`Equivalent`**. **Anvil** multi-viewer / `WISDOM` winner — see anvil doc. **Sprint 76A** — dedicated-server **mixin** startup fix for **`AnvilDiscount`**: **`SPRINT_76A_STARTUP_BLOCKER_ANVIL_MIXIN.md`**. **Sprint 76B** — region **`check_replace` / `FabricTreeGrowContext`**: retarget **`TreeFeatureGrowContextMixin`** from abstract **`Feature#generate`** to **`TreeFeature#generate`**: **`SPRINT_76B_STARTUP_BLOCKER_TREE_FEATURE_MIXIN.md`**. **Regression spot-checks** still recommended. |
| AS-019 | `abilityManager.registerAbilityImplementations` | `FabricAbilityManager.registerAbilityImplementations` | Partial | Needs Regression Proof | **Sprint 23–26** — **14/14** registration **Equivalent**; per-family table **`SPRINT_26_ABILITY_PROOF_MATRIX.md`**, manual **`SPRINT_26_MANUAL_ABILITY_REGRESSION.md`**. **Farming** still **Partial** for documented tail (chorus, structure trees, some bamboo) — see **`SPRINT_25_FARMING_GROWTH_AURA.md`**. **Sprint 26** = proof/closure **documentation**; no new ability code. Remaining: **regression proof** (incl. damage pipeline vs Bukkit), Bukkit **DamageEvent** / plugin cancels = **N/A** on default Fabric. |
| AS-020 | `manaAbilityManager.registerProviders` | `FabricManaAbilityManager.registerProviders` | Partial | None | All major abilities + XP hook (see `FabricManaAbilityUseXpHandler` javadoc, Sprint 12). **Gaps:** Bukkit `ManaAbilityActivateEvent` for plugins — **N/A** on Fabric. |
| AS-021 | `registerEvents()` (see §4) | Scattered Fabric API + mixins | Partial | None | Not 1:1 class mapping; see listener table. |
| AS-022 | `registerAndLoadMenus` | `FabricMenuLoaders#registerBuiltInMenus` + `FabricMenuRegistry` + GUI slices + GraniteLib | Partial | Slate-Fabric + NRP | **Sprint 29** — `register` path + menu IDs; no `loadMenus` from disk. **NRP** (Java + Geyser). |
| AS-023 | `registerPriorityEvents` → `PlayerJoinQuit` | `FabricPlayerLifecycle` | Partial | None | Join/disconnect; compare full Bukkit behavior. |
| AS-024 | `leaderboardManager` tasks | Same in `finishEnable` | Partial | None | |
| AS-025 | `statManager.scheduleTemporaryModifierTask` | same | Partial | None | |
| AS-026 | `modifierManager` | `FabricModifierManager` + schedules | Partial | None | |
| AS-027 | NBT API init / `BukkitItemRegistry` storage | `FabricItemRegistry` + codecs | Partial | None | Different persistence model. |

---

## 3. `CommandRegistrar` — ACF command surface

**Replacements/conditions/contexts/completions:** part of ACF — **no Fabric port** until **ACF + Brigadier** program lands (`MENU_AND_COMMANDS_PORT.md`).

| ID | Bukkit (registered command class) | Fabric | Status | Blocker |
|----|-----------------------------------|--------|--------|---------|
| CMD-001 | `SkillsRootCommand` | `FabricBrigadierCommands` — **Sprint 65** **`skillsAliasSharedTree`** on **`/skills`**, **`/sk`**, **`/skill`** (fix **`/skills`** drift); **Sprint 66** **`FabricManaAbilityCommands`** ( **`/manaability`** under same tree + **`/auraskills`**); (includes **Sprint 48** utility + **Sprint 49** **`/top average`**, **`/updateleaderboards`**, **Sprint 50** **`/modifier`**, **`/trait`**, **Sprint 51** **`/item`**, **`/armor`**, **Sprint 52** **`/item ignore`**, + prior sprints); **`claimitems`**, **`/claimitems`** (**Sprint 56** merge, **Sprint 71** **`FabricUnclaimedItemsMenuGui`** + **`/claimitems all`** + **`openmenu unclaimed_items`**); **`sources`**, **`/sources`**, **`resethealth`**, **`/resethealth`** (**Sprint 57**); **`transfer`**, **`/transfer`** + **`FabricUserDataTransfer`** (**Sprint 58**); **`profile`**, **`/profile`** + **`FabricUserDataRead`** (**Sprint 59**); **`backup`**, **`/backup`** + **`FabricBackupCommands`** + **`FabricBackupPathValidator`** (**Sprint 60–61**); **`preset`**, **`/preset`** + **`FabricPresetCommands`** (**Sprint 62**); **`storage`**, **`/storage`** + **`FabricStorageCommands`** + **`FabricCommandStorageUtil`** (**Sprint 63–64**); **`fabric/COMMAND_PARITY_AUDIT.md`** | Partial | ACF/Brigadier + Slate-Fabric (full tree) | **Sprint 60–64** — **`backup`** + **`preset`** + **`storage`**; **S64** — guarded **`mergeskills`** / **`removeallmodifiers`**; **S65** — **`/skills` = /sk|skill** subtree; **S66** — **`manaability resetcooldown`**; **S71** — **unclaimed** menu + **`openmenu unclaimed_items`** |
| CMD-002 | `StatsCommand` | `FabricBrigadierCommands` (`/stats` → **Stats** GUI) | Partial | ACF/Brigadier | **Sprint 39** — same scaffold |
| CMD-003 | `SkillCommand` | `FabricSkillLevelCommands` + `FabricCommandSkillLevelApply` — **`/skilllevel`**, **`/auraskills skilllevel`**, **`/sk\|skill skilllevel …`** (Sprint 46) (view, add, set, remove, setall, reset; + Fabric remove) | Partial | ACF/Brigadier | **Sprint 46** — not legacy **`/sk skill`**, same **`skilllevel`** *word* as standalone |
| CMD-004 | `ManaCommand` (common) | `FabricManaCommands` + `FabricBrigadierCommands`: **`/mana`**, **`/auraskills mana`**, **`/sk\|skill mana …`** (Sprint 46) — self, `view`, `add` / `set` / `remove` (online; shared amount rules + mutation feedback) | Partial | ACF/Brigadier + ACF `silent` / `allowOverMax` | **Sprint 41** + **Sprint 46** alias; ACF extra flags not ported |
| CMD-005 | `ModifierCommand` | `FabricPlayerModifierCommands` (stat: **add** / **remove** / **list** / **removeall**; online; optional op + stack) + **`/auraskills`**, **`/sk\|skill`**; **`auraskills.command.modifier`** | Partial | ACF/Brigadier + **addtemp** + item modifiers | **Sprint 50** — Bukkit **player** stat path only |
| CMD-006 | `ItemCommand` | `FabricItemArmorModifierCommands#itemCommandRoot` — `modifier\|trait` (Sprint 51); **`ignore`** (Sprint 52); **`requirement` / `multiplier`** (Sprint 53); **`register` / `unregister` / `give`** (Sprint 55, **`FabricItemRegistry`**, **`addRegisteredItemToInventory`**); perms **modifier**, **ignore**, **requirement**, **multiplier**, **register**, **give** | Partial | ACF/Brigadier + ext. item providers, deep claimitems | **Sprint 55** — **register** / **unregister** / **give**; **not** Bukkit ext. **ExternalItemProvider** |
| CMD-007 | `ArmorCommand` | `FabricItemArmorModifierCommands#armorCommandRoot` — `modifier\|trait` (Sprint 51) + **`requirement` / `multiplier`** (Sprint 54, same writers + **`RequirementMsg`/`MultiplierMsg`**); perms **armor.modifier**, **armor.requirement**, **armor.multiplier** | Partial | ACF/Brigadier (item register/give N/A for armor) | **Sprint 54** — **armor** **requirement** + **multiplier** (main hand) |
| CMD-008 | `ProfileCommand` | **`FabricProfileCommands`** + **`FabricUserDataRead`**; **`fabric/COMMAND_PARITY_AUDIT.md`** | Partial | ACF/Brigadier | **Sprint 59** — bounded **UserState**; Bukkit no top-level **`/profile`**; Bukkit **async** offline load |
| CMD-009 | `BackupCommand` | **`FabricBackupCommands`** + **`FabricBackupPathValidator`** + **`FabricCommandConfirmation`**, common **`BackupProvider`** / **`loadBackupAsync`**; **`fabric/COMMAND_PARITY_AUDIT.md`** | Partial | ACF/Brigadier | **Sprint 60–61** — **create|save|list|info** + **guarded load|restore** (preview + **confirm**; **`auraskills.command.backup.load`**); **Sprint 74** — pending key + **30s** on preview→confirm; **no** delete/schedule/full datadir; Bukkit ref perms/defaults differ |
| CMD-010 | `XpCommand` | `FabricXpCommands` + `FabricCommandSkillParser` / `FabricCommandPlayerTarget` (`/xp`, `/auraskills xp`, **`/sk\|skill xp`**, Sprint 46) | Partial | ACF/Brigadier | **Sprint 40** + **Sprint 46**; no `silent` |
| CMD-011 | `PresetCommand` | **`FabricPresetCommands`**, **`FabricPresetNameValidator`**, **`FabricCommandConfirmation`**, common **`PresetManager`**, pre-apply **`BackupProvider`**, **`fabric/COMMAND_PARITY_AUDIT.md`** | Partial | ACF/Brigadier + Bukkit ACF **double-confirm** vs **`confirm` literal** | **Sprint 62** — **list|info|load…confirm**; **Sprint 74** — pending key + **30s**; ref **load**-only; **no** create/delete/remote **Storage** |
| CMD-012 | `StorageCommand` | **`FabricStorageCommands`**, **`FabricCommandStorageUtil`**, **`FabricCommandConfirmation`**, **`fabric/COMMAND_PARITY_AUDIT.md`** | Partial | ACF/Brigadier + Bukkit had **entire** **mergeskills**/**removeallmodifiers** on **console** only (ref had **no** two-step / pre-backup); ref **no** `storage` **info|save|reload** | **Sprint 63** — **info|save|reload**; **Sprint 64** — **mergeskills** / **removeallmodifiers** (two-step, **confirm** = console/RCON, pre-`UserState` backup); **Sprint 74** — pending key + **30s** on **preview→confirm**; no migrate |
| CMD-013 | `OpenMenuCommand` | **`FabricOpenMenuCommands`** + **`FabricCommandBuiltInMenuId`** — `/openmenu`, `/auraskills openmenu`, `/sk|skill openmenu` (literals: **skills**, **stats**, **level_progression**, **stat_info**, **sources** + typed context, **unclaimed_items**, **leaderboard**, **abilities**) | Partial | Slate arbitrary menus + JSON props + page | **Sprint 45** — **level_progression** / **stat_info**; **Sprint 70** — **`sources`**; **Sprint 71** — **unclaimed_items**; **Sprint 72** — **`leaderboard`**; **Sprint 73** — **`abilities`** (**`FabricOpenMenuAbilities`**); no JSON/page |
| CMD-014 | `ManaAbilityCommand` | **`FabricManaAbilityCommands`**, **`FabricCommandManaAbilityParser`**, `enabledManaAbilities`, **`fabric/COMMAND_PARITY_AUDIT.md`** | Partial | ACF/Brigadier + Bukkit **`ManaAbilityRefreshEvent`** (api-bukkit only) | **Sprint 66** — **resetcooldown** only ( **other** online **Player**); default **deny**; no top-level `/manaability` |
| CMD-015 | `TraitCommand` | `FabricPlayerModifierCommands#traitCommandRoot` (same bounded surface as **CMD-005** for traits) | Partial | ACF/Brigadier + **addtemp** + item path | **Sprint 50** — player **trait** modifier path only |
| CMD-016 | `JobsCommand` | — | Missing | Out of scope + ACF/Brigadier | Reference jobs feature; not default Granite scope. |
| CMD-017 | `AntiAfkCommand` | **`FabricAntiAfkCommands`** — **`antiafk logs`** (`/auraskills`, `/skills|/sk|/skill`); **`auraskills.command.antiafk.logs`**; **`fabric/COMMAND_PARITY_AUDIT.md`** | Partial | ACF/Brigadier + Bukkit **OfflinePlayer#hasPlayedBefore** vs Fabric **online** + **NameToIdCache** | **Sprint 67** — read-only log listing; in-game **other** only; **no** top-level **`/antiafk`** |
| CMD-018 | `registerSkillCommands` (per-skill `SkillCommands.*`) | **`FabricSkillShortcutCommands`** — top-level **`/farming` … `/forging`**, `Option.ENABLE_SKILL_COMMANDS` + **`loadConfigEnabledMap`**, `auraskills.skill.*`, **`openLevelProgression`**; **`fabric/COMMAND_PARITY_AUDIT.md`** | Partial | ACF/Brigadier + Bukkit **Slate** `level_progression` vs **Fabric** built-in | **Sprint 68** — **15** reference aliases; not under `/skills` |
| CMD-019 | ACF: replacements (`skills_alias`, desc keys) | Brigadier **`/sk`**, **`/skill`** (Sprint 44–46: `openmenu` + `xp` + `mana` + `skilllevel`); `desc_*` | Partial | ACF/Brigadier | **Sprint 46** — reference-shaped subtree for implemented families; not full ACF |
| CMD-020 | ACF: contexts (User, Skill, ManaAbility, Stat, Trait, UUID, JsonArg, Duration) | — | Missing | ACF/Brigadier | |
| CMD-021 | ACF: async completions (skills, stats, menu_names, …) | `FabricCommandSuggestionProviders` (sync) | Partial | ACF/Brigadier | **Sprint 40** — **xp** suggestions; **Sprint 45** — `enabledStatKeys` + `builtInOpenMenuIds` (four ids); **Sprint 50** — `enabledTraitKeys` for **`/trait`**; **Sprint 55** — `registeredItemKeys` for **`/item unregister|give`**; **Sprint 66** — **`enabledManaAbilities`** for **`/manaability resetcooldown`** |

**Fabric admin parity (not in Bukkit ACF list):** `reload`, `save` in `FabricAuraSkillsCommands` — **extra**; keep row if tracking “operator surface” separately.

---

## 4. `MenuRegistrar` — Slate menus

| ID | Bukkit (menu / context) | Fabric | Status | Blocker |
|----|-------------------------|--------|--------|---------|
| MENU-001 | Slate `ContextManager`: Skill, Stat, Source, SortType, Ability, ManaAbility, Trait | — | Missing | Slate-Fabric |
| MENU-002 | `PlaceholderHelper` global replacer + locale | — | Missing | Slate-Fabric |
| MENU-003 | Menu `skills` | `FabricSkillsMenuGui` + `GraniteClickable9x6Handler` + `menus/skills.yml` `granite` via **`FabricBuiltInMenuConfig`** | Partial | Slate-Fabric + NRP | **Sprint 27/30/32/34/36/37/38** — YAML + **paged** list + **Back**; **Sprint 101** — bundled template + **28**-slot grid; **Sprint 113** — **locked** **11**-skill hub + leaderboard / head→stats / unclaimed / close (**no** fill); **Sprint 114** — hub **tooltip polish** (`TOOLTIP_DISPLAY` suppression, compact lore, **brewing stand** Alchemy); **`2.3.143`**; built-in fallback remains legacy paged list if template invalid; **not** full Slate. |
| MENU-004 | Menu `stats` | `FabricStatsMenuGui` + `GraniteClickable9x6Handler` (stat_info) + `menus/stats.yml` `granite` | Partial | Slate-Fabric + NRP | **Sprint 28/31/32/34/36/37/38** — YAML paged list; **Sprint 101** — bundled **28**-slot template; **Sprint 113** — **locked** **3×3** + back/close; **Sprint 114** — hub **short** stat blurbs + mechanics suppression; **`2.3.143`**; **not** full Slate. |
| MENU-005 | Menu `level_progression` | `FabricLevelProgressionMenuGui` + **`FabricMenuRegistry`** + `menus/level_progression.yml` `granite` | Partial | Slate-Fabric + NRP | **Sprint 30/33/34/35/36/38** — context + list page for **Back** to Skills + **`granite.items.main`/`back`**; **Sprint 100** — bundled **`level_progression.yml`** template (**`FabricLevelProgressionTemplateSupport`**) + paging + strict built-in fallback; **Sprint 117** — **24**-slot snake **path** + fixed control row (**`FabricLevelProgressionLayout`**); **Sprint 118** — built-in **`openBuiltInSnake`** matches template path (**no** legacy hopper **50**); not full Slate merge; **NRP** Bedrock. |
| MENU-006 | Menu `leaderboard` | **Sprint 72** — **`FabricLeaderboardMenuGui`** + **`/openmenu leaderboard …`** (**`FabricOpenMenuLeaderboard`**); not `FabricMenuLoaders`. **`/top` / `/skilltop`** remain **chat** (Bukkit reference). **Sprint 103** — bundled **`leaderboard.yml`** template (**`FabricLeaderboardTemplateSupport`**); strict built-in fallback; **not** full Slate. | Partial | Slate-Fabric + NRP | Bukkit: **Slate** `LeaderboardMenu` (10 skull rows, skill property, back to **level_progression**); Fabric: **template or paper** rows, **power** / **average** / **skill** modes, **Back** → **Skills**; **NRP** Bedrock |
| MENU-007 | Menu `sources` | **Sprint 57** — paged **chat**; **Sprint 69** — **`FabricSourcesMenuGui`**; **Sprint 70** — **`/openmenu sources …`**, level progression **hopper** → sources (not `FabricMenuLoaders`); **Sprint 98** — bundled **`sources.yml`** template-first; **Sprint 99** — shared **`FabricGraniteTemplateStacks`** / **`GraniteTemplate*`** helpers (no behavior change intended). | Partial | Slate-Fabric + NRP | Bukkit: `sources` **Slate** + `menus/sources.yml` (item templates, sorter lore, `multiplied_xp`); Fabric: template **or** built-in **gray_dye** rows; **not** full Slate; **NRP** Bedrock |
| MENU-008 | Menu `abilities` | **Sprint 73** — **`FabricAbilitiesMenuGui`** + **`/openmenu abilities …`**; not `FabricMenuLoaders`. Per-**skill** `User` display (Bukkit: **`skill`** property + locked/unlocked templates). | Partial | Slate-Fabric + NRP | Bukkit: Slate **components** + **YAML** slots; Fabric: **dye**/**star** / **shard** read-only rows; **Sprint 102** — bundled **`abilities.yml`** template (**`FabricAbilitiesTemplateSupport`**) + strict **`openBuiltIn`** fallback; **NRP** Bedrock |
| MENU-009 | Menu `stat_info` | `FabricStatInfoMenuGui` + **`FabricMenuRegistry`** + `stat_info` YAML + `menus.stat_info.enabled`; **Sprint 99** — template path uses shared **`FabricGraniteTemplateStacks`** / **`GraniteTemplate*`** helpers. | Partial | Slate-Fabric + NRP | **Sprint 31/33/34/35/36/38** — context + list page for **Back** to Stats + **`granite.items.main`/`back`**; not full `stat_info.yml` grid; **NRP** Bedrock. |
| MENU-010 | `ApiMenuManager` non-default menu names | — | Missing | Slate-Fabric + api-bukkit |
| MENU-011 | `UnclaimedItemsMenu` (Slate) | **Sprint 71** — **`FabricUnclaimedItemsMenuGui`** + **`/claimitems`** / **`/openmenu unclaimed_items`**; not `FabricMenuLoaders`. **Sprint 105** — bundled **`unclaimed_items.yml`** (**`FabricUnclaimedItemsTemplateSupport`**); real stacks + template lore; **`FabricUnclaimedItemsMerge`** unchanged; strict built-in fallback. | Partial | Slate-Fabric + NRP | Bukkit: 6×9 up to **54** stacks, per-click merge; Fabric: **36**/page + bottom **claim all**; **`FabricUnclaimedItemsMerge`**; **NRP** Bedrock |

---

## 5. `registerEvents` — Bukkit listeners vs Fabric (high level)

| ID | Bukkit class | Fabric mapping | Status | Notes |
|----|--------------|----------------|--------|-------|
| EVT-001 | `PlayerJoinQuit` | `FabricPlayerLifecycle` | Partial | |
| EVT-002 | `DamageListener` + `DamageLeveler` | `FabricCombatXpHandler` + `FabricDamagePipeline` | Partial | **Sprint 08** — Bukkit `DamageLeveler` order + shield (`isBlocking` only), `DamageXpSource` damagers for `ThrownItemEntity` / potion shooters, `EntityLeveler#resolveDamager` live path via `FabricEntityDamageAttribution` + `FabricEntityXpSourceLookup#forEntityDamageOnly` (incl. `THROWN_POTION`→Alchemy). `DamageListener` cancel/correct last damage: **N/A** / partial on Fabric. |
| EVT-003 | `CriticalHandler` | `FabricCriticalHandler` (via pipeline) | Partial | |
| EVT-004 | `BlockInteractions` | `FabricBlockBreakHandler`, `FabricBlockInteractHandler`, etc. | Partial | |
| EVT-005 | `BlockLootHandler` | `FabricBlockLootHandler` + `FabricBlockXpSourceLookup` + `FabricRegionManager` | Partial | Core pool + `override_vanilla_loot` + `check_replace` via placed-block NBT. Plugin-only loot gating: **out of scope**. |
| EVT-006 | `FishingLootHandler` + `FishingLeveler` | `FabricFishingLootHandler` + `FabricFishingXpHandler` (mixin + `Stats.FISH_CAUGHT`) | Partial | **Sprint 09** — HIGH-then-MONITOR order, null-`originalSource` rare/epic, `isInOpenWater` pool option; Bukkit `LootDropEvent` / `EntityLoot` / `getExpToDrop` gate: **N/A** or follow-up. |
| EVT-007 | `MobLootHandler` | `FabricMobLootHandler` | Partial | No Bukkit `LootDropEvent` cancel; skill from `FabricEntityXpSourceLookup` (Fabric damage cause mapping). Plugin-only gates: **out of scope**. |
| EVT-008 | `RequirementListener` | `FabricRequirementHandlers` | Partial | |
| EVT-009 | `ItemListener` | Item/modifier stack | Partial | |
| EVT-010 | `ArmorListener` | Partial via modifier schedules | Partial | |
| EVT-011 | `ArmorModifierListener` | `FabricItemModifierSchedules` / state | Partial | |
| EVT-012 | `RegionListener` | — | Missing | Out of scope | Reference chunk/region file load; third-party region stack not a default target. |
| EVT-013 | `RegionBlockListener` | `BlockItemPlacedBlockMixin` + `FabricPlacedBlockBreakHandler` + `FabricRegionLifecycle` + `FabricRegionBlockIntegrity` + piston/fall/tree mixins (Sprint 05) | Partial | Piston + fall + `TreeFeature` same-Y + tall/support; external “blocked region” plugins **out of default scope**; mushroom grow & rare edges **Needs Regression Proof** |
| EVT-014 | `PlayerDeath` (`ON_DEATH_RESET_SKILLS` / `ON_DEATH_RESET_XP` / `ON_DEATH_RESET_XP_RATIO`) | `FabricPlayerDeathHandler` | Equivalent | Bukkit `PlayerDeath.java` — `ServerLivingEntityEvents.AFTER_DEATH` for `ServerPlayerEntity`; Geyser/Floodgate = same server player path. |
| EVT-017 | `EntityLeveler` (entity `DEATH` / `DAMAGE` XP + spawner) | `FabricEntityKillHandler` + `FabricCombatXpHandler` + `FabricSpawnerMobTracker` + mixins; `FabricEntityDamageAttribution` + `FabricEntityXpSourceLookup` | Partial | **Sprint 07** — kill/mob-loot killer resolution + `DEATH`-only source lookup + bleed PDC-style map. **Gaps:** Bukkit `onMobTransform` spawner copy (slime/drowned), `EntityXpGainEvent` API, MythicMobs — **out of default scope** or follow-up. |
| EVT-018 | `StatisticLeveler` | `FabricStatisticXpHandler` + `FabricStatisticSourceResolver` (timer + stat resolution) | Partial | **Sprint 10** — `Stats.CUSTOM` known ids + `killed/` + `killed_by/` + `dropped/` + block/item types; Bukkit’s enum-only `Statistic` surface is a subset. Double-count vs entity levelers: **config**; cross-play: **Needs Regression Proof** in optional columns. |
| EVT-019 | `BrewingLeveler` | `FabricBrewingXpHandler` + `FabricBrewingTakeoutTracker` + `FabricBrewingOwnerTracker` + mixins (incl. `LockableContainerBrewingStandSetStackMixin`) | Partial | **Sprint 11** — BREW + owner + TAKEOUT; TAKEOUT **clear** on hopper empty of result slots; TAKEOUT XP only on **player** GUI. Rare automation-mod paths: **Needs Regression Proof** if reported. |
| EVT-021 | `ManaAbilityUseLeveler` | `FabricManaAbilityUseXpHandler` (called from all mana providers after consumption) | Partial | **Sprint 12** — full provider set + `isSourceEnabled(MANA_ABILITY_USE)`; Bukkit `ManaAbilityActivateEvent` **cancel** / third-party rewrites: **N/A** |
| EVT-022 | `JumpingLeveler` | `FabricJumpingXpHandler` (`ServerTickEvents#END_SERVER_TICK`) | Partial | **Sprint 13** — `shouldBlockXp`, spectator, `isClimbing`; Bukkit `PlayerMoveEvent` + `check_cancelled` / `LevelerContext` world blocks: **N/A** / partial |
| EVT-023 | `EnchantingLeveler` | `FabricEnchantingXpHandler` + `EnchantmentScreenHandlerMixin` | Partial | **Sprint 14** — `enchantmentPower` for `exp_requirement` unit, spectator; Bukkit `EnchantItemEvent` **cancel** / `LevelerContext` region: **N/A** / `shouldBlockXp` |
| EVT-024 | `GrindstoneLeveler` | `FabricGrindstoneXpHandler` + `ScreenHandlerGrindstoneXpMixin` | Partial | **Sprint 15** — `failsClickChecks`-style cursor + 36-slot fullness + `SlotActionType` allowlist, spectator; Bukkit `check_cancelled` / `LevelerContext` region: **N/A** / `shouldBlockXp` |
| EVT-025 | `AnvilLeveler` | `FabricAnvilXpHandler` + `ForgingScreenHandlerMixin` + `ForgingScreenHandlerAnvilOnTakeMixin` | Partial | **Sprint 16** — `FabricAnvilTakeContext` + `onTake` HEAD/RETURN (anvil, output slot), spectator; Bukkit `failsClickChecks` on `InventoryClickEvent` / `check_cancelled` / region: **N/A** / `onTakeOutput` + `shouldBlockXp` |
| EVT-026 | `ItemConsumeLeveler` | `FabricItemConsumeHandler` + `LivingEntityConsumeMixin` | Partial | **Sprint 17** — `isSourceEnabled(ITEM_CONSUME)`, spectator, `onConsumeItemCommit` javadoc; Bukkit `PlayerItemConsumeEvent` **cancel** / `LevelerContext` region: **N/A** / `shouldBlockXp` |
| EVT-027 | `PotionSplashLeveler` | `FabricPotionSplashHandler` + `PotionEntityMixin` | Partial | **Sprint 18** — `lacksPotionEffectForParity` (Bukkit empty `getEffects`), `isSourceEnabled`, spectator, splash+lingering javadoc; Bukkit event **cancel** / region: **N/A** / `shouldBlockXp` |
| EVT-015 | `JobsListener` | — | Missing | Out of scope | Reference jobs/economy listener; not default Granite scope. |
| EVT-016 | `BukkitLeaderboardExclusion` (listener) | `FabricLeaderboardExclusion` | Partial | |

---

## 6. Proposed first implementation batch (dependency + risk)

**Goal:** maximum unlock toward “Paper-like” server with **one coordinated spine**.

| Order | Work unit | Rationale | Risk |
|-------|-----------|-----------|------|
| **0** | **Player death reset** — `ON_DEATH_RESET_SKILLS` / `ON_DEATH_RESET_XP` (mirror `PlayerDeath.java`) | Config parity; `EVT-014`; **no** Slate/ACF dependency | Low — isolated listener |
| **1** | **Slate-Fabric vertical slice** — one menu (`skills`) open/close, one YAML, `Text`/scheduler bridge | Unblocks `OpenMenuCommand`, `MENU-*`, `CMD-013` | High — sets architecture |
| **2** | **ACF + Brigadier** — `CommandRegistrar` skeleton: `SkillsRootCommand` + `OpenMenuCommand` + shared contexts (`Skill`, `User`) | Unlocks `CMD-001`–`CMD-021` incrementally. **Caveat:** match **Paper command *capability***, not a premature freeze of final **Granite Skills** public names/aliases (see [PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md)). | Medium — dependency on (1) for menu commands |
| **3** | **Loot: `BlockLootHandler` / `MobLootHandler` parity** | Gameplay feel; `EVT-005`, `EVT-007` | Medium — no Slate dependency |
| **4** | **Requirement** `region` + `FabricRegionNode` clarity | Document stub vs reference; `AS-014`, `AS-016`, `EVT-012` | Low — no external mod API in default scope |
| **5** | **SkillsLoadEvent (or common event)** on Fabric if API consumers need it | `AS-015` | Low–medium |

**Not in default Granite roadmap:** optional reference metrics (`AS-012`), `UpdateChecker` (`AS-013`), reference jobs (`EVT-015` / `CMD-016`); full `api-bukkit` (`AS-002`) remains **common seam** for reference types only.

---

## 7. CSV (paste / import)

```csv
ID,Category,Bukkit reference,Fabric reference,Status,Blocker,Notes
AS-001,Bootstrap,ApiRegistration,FabricAuraSkills+ApiRegistrationUtil,Equivalent,None,Core API
AS-002,Bootstrap,ApiBukkit+ApiItemManager,—,Missing,Common seam,api-bukkit
AS-006,Bootstrap,Slate+MenuFileManager+InventoryManager,GraniteContainerMenus+FabricMenuRegistry+FabricMenuLoaders,Partial,Slate-Fabric+Sprint29,
AS-008,Bootstrap,CommandRegistrar→PaperCommandManager,FabricAuraSkillsCommands,Partial,ACF/Brigadier,Small command set
AS-012,Bootstrap,Metrics+MetricsUtil,—,Missing,Out of scope,Optional reference metrics
AS-014,Bootstrap,onLoad region flags hook ref,—,Missing,Out of scope,Stub FabricRegionNode
AS-015,Bootstrap,SkillsLoadEvent,—,Missing,Common seam,API event
AS-022,Bootstrap,registerAndLoadMenus,FabricMenuLoaders+Registry+GraniteLib,Partial,Slate-Fabric+Sprint29,
CMD-001,Commands,SkillsRootCommand,FabricAuraSkillsCommands,Partial,ACF/Brigadier+Slate-Fabric,Aliases differ
CMD-013,Commands,OpenMenuCommand,—,Missing,Slate-Fabric+ACF/Brigadier,
MENU-003,Menus,Menu skills,—,Missing,Slate-Fabric,
EVT-005,Listeners,BlockLootHandler,FabricBlockLootHandler+FabricRegionManager,Partial,None,Plugin loot gates out of scope; check_replace NBT; Sprint 04
EVT-002,Listeners,DamageListener+DamageLeveler,FabricCombatXpHandler+pipeline,Partial,None,Sprint 08: resolver+lookup+damagers
EVT-006,Listeners,FishingLootHandler+FishingLeveler,FabricFishing handlers,Partial,None,Sprint 09 order+open water
EVT-007,Listeners,MobLootHandler,FabricMobLootHandler,Partial,No Bukkit LootDropEvent cancel; Sprint 03,
EVT-014,Listeners,PlayerDeath,FabricPlayerDeathHandler,Equivalent,None,on_death.* config; see Sprint 01 manual
EVT-017,Listeners,EntityLeveler,FabricEntityKillHandler+Combat+Spawner+Sprint07,Partial,None,Entity death XP + attribution; see Sprint 07 manual; gaps: transform, API event
EVT-018,Listeners,StatisticLeveler,FabricStatisticXpHandler+FabricStatisticSourceResolver,Partial,None,Sprint 10: custom+killed/dropped; manual SPRINT_10
EVT-019,Listeners,BrewingLeveler,FabricBrewing handlers+setStack mixin,Partial,None,Sprint 11 TAKEOUT hopper clear; manual SPRINT_11
EVT-021,Listeners,ManaAbilityUseLeveler,FabricManaAbilityUseXpHandler+providers,Partial,None,Sprint 12 source gate; Bukkit event N/A; manual SPRINT_12
EVT-022,Listeners,JumpingLeveler,FabricJumpingXpHandler,Partial,None,Sprint 13 shouldBlockXp+climb; manual SPRINT_13
EVT-023,Listeners,EnchantingLeveler,FabricEnchantingXpHandler+mixin,Partial,None,Sprint 14 exp cost from table; manual SPRINT_14
EVT-024,Listeners,GrindstoneLeveler,FabricGrindstoneXpHandler+mixin,Partial,None,Sprint 15 click gates; manual SPRINT_15
EVT-025,Listeners,AnvilLeveler,FabricAnvilXpHandler+anvil onTake mixin,Partial,None,Sprint 16 take snapshot; manual SPRINT_16
EVT-026,Listeners,ItemConsumeLeveler,FabricItemConsumeHandler+LivingEntityConsumeMixin,Partial,None,Sprint 17 commit+javadoc; manual SPRINT_17
EVT-027,Listeners,PotionSplashLeveler,FabricPotionSplashHandler+PotionEntityMixin,Partial,None,Sprint 18 empty effects gate; manual SPRINT_18
EVT-012,Listeners,RegionListener,—,Missing,Out of scope,Ref chunk load; ext region plugins N/A
EVT-013,Listeners,RegionBlockListener,Placed block mixin+break+chunk load+Sprint05 integrity,Partial,Piston/fall/TreeFeature; ext region plugins N/A,
```

*(Extend this CSV with all rows from §2–5 as you grow the sheet.)*

---

## 8. Caveat (from your handoff)

This pass **does not** claim line-by-line audit of every Bukkit class — only **registration surfaces** for the three entry files. **Next pass:** every `*Leveler`, every `HookManager` hook, `plugin.yml` permissions vs Fabric permission checks.

**External workbook path:** `d:\Downloads\fabricauraskills_parity_audit_sheet.xlsx` was not in the repo; keep the `.xlsx` as the master and **import §7** or **link** this file from `Read Me` as the Phase 1 source.
