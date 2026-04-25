# Sprint 69 — manual validation: menu parity inventory + sources menu baseline

**Build:** `projectVersion` **2.3.97** (see `gradle.properties`).

**Scope:** one lane only — **menus**; **no** new command families, **no** Jobs, **no** full Slate, **no** `MenuFileManager` merge parity.

## Part A — menu inventory (Bukkit `MenuRegistrar` + Fabric)

Reference registration: `bukkit/.../menus/MenuRegistrar.java` — `Slate#buildMenu` for the default set below, plus **API** **non-default** names (`ApiMenuManager#getNonDefaultMenuNames`).

| Menu id (Bukkit) | Bukkit class | Default YAML (reference) | What opens it (reference) | Fabric equivalent | Status | Bedrock / Geyser | Notes / limitations |
|------------------|--------------|----------------------------|----------------------------|-------------------|--------|------------------|------------------------|
| `skills` | `SkillsMenu` | `menus/skills.yml` | Default `/skills` (ACF), Skills root | `FabricSkillsMenuGui` + `FabricMenuLoaders` | **Partial** | **NRP** | YAML `granite` slice; not full Slate `skills` |
| `stats` | `StatsMenu` | `menus/stats.yml` | `/stats` | `FabricStatsMenuGui` + registry | **Partial** | **NRP** | Same pattern as `skills` |
| `level_progression` | `LevelProgressionMenu` | `menus/level_progression.yml` | Skill click in Skills, SkillCommands, etc. | `FabricLevelProgressionMenuGui` + registry + `FabricMenuContext#ofSkill` | **Partial** | **NRP** | Not full Bukkit reward tables / file templates |
| `stat_info` | `StatInfoMenu` | `menus/stat_info.yml` | Stat click in Stats | `FabricStatInfoMenuGui` + registry + `FabricMenuContext#ofStat` | **Partial** | **NRP** | Gated by `stat_info` enabled in YAML |
| `sources` | `SourcesMenu` | `menus/sources.yml` | Bukkit `onSources` → Slate; `/sources` with skill | **Sprint 69:** `FabricSourcesMenuGui` via **`FabricSourcesCommands` only** (not in `FabricMenuLoaders`) | **Partial** | **NRP** | No YAML load for this menu; gray_dye; no Bukkit per-source `ItemRegistry` / `multiplied_xp` income |
| `leaderboard` | `LeaderboardMenu` | `menus/leaderboard.yml` (if present) | Bukkit menubar / flows | **Sprint 72:** `FabricLeaderboardMenuGui` + `/openmenu leaderboard` (not YAML-loaded) | **Partial** | **NRP** | Bukkit ref **`/top`** is chat; Fabric GUI differs (paper rows, Skills back) — see **`SPRINT_72_MANUAL_LEADERBOARD_MENU.md`** |
| `abilities` | `AbilitiesMenu` | `menus/abilities.yml` (if present) | Abilities entry | — | **Missing** | **NRP** | — |
| *custom / API* | (external builders) | varies | `ApiMenuManager` | — | **Missing** | — | Not tracked in Fabric registry |

**Other reference UI (not in `MenuRegistrar` default seven):**

- **Unclaimed items** — Bukkit `UnclaimedItemsMenu` + **`/claimitems`**; Fabric: **chat/merge** command only (**Sprint 56**), no Slate **UnclaimedItemsMenu**. **Status: Partial / Missing (UI).**
- **Confirm flows** — Bukkit `ConfirmManager`; Fabric: not ported as inventory. **Status: Missing.**
- **Profile / account** — Bukkit has richer account UX; Fabric **`/profile`** is **read-only** chat (**Sprint 59**). **Status: Partial (non-menu).**

**Sprint 70** adds bounded **`/openmenu sources …`**, **`FabricSourcesOpenSupport`**, and a **level progression** hopper (slot 50) that passes **`listPage()`** to **`openSources`** so **Back** can restore the **Skills** list page. See **`SPRINT_70_MANUAL_SOURCES_OPENMENU.md`**.

## Part B — sources menu (Sprint 69 baseline)

### Behavior

- **Java in-game player:** `/sources <skill> [sort]` (and mirror roots) opens a **6×9** `GENERIC_9X6` screen (`dev.granitelib.fabric.menu.GraniteContainerMenus#openClickable9x6`).
- **Dedicated server console / RCON / non-entity source:** same command resolves skill + sort, prints **paged chat** (28 lines per “page” of lines), same as pre–Sprint-69 text path. **No** `players_only` error for console anymore.
- **Empty list** (no version-valid `XpSource`s for the skill): **chat** `sources.empty` for **all** senders; **no** GUI.
- **Unknown / disabled skill:** `UNKNOWN_SKILL` message (player locale or server default language for console).
- **Sort** tokens: `ascending`, `descending`, `alphabetical`, `reverse_alphabetical` — unchanged from **Sprint 57**.
- **Menu controls** (Bukkit `sources.yml` default positions): **Back** (slot 45) → `FabricAuraSkillsMenus#openLevelProgression(skill, player, plugin, 0)`; **Previous** 48 / **Next** 50 when multi-page; **Sort** 49 → `FabricSourceSortType#nextCyclic()` and reopen page **0**.

### Bukkit `menus/sources.yml` (inventory only — not loaded on Fabric for this menu)

| Area | Reference | Fabric Sprint 69 |
|------|-----------|-------------------|
| Title placeholders | `{{sources_title}}`, `current_page` / `total_pages` | `en_us` `auraskills.granite.menu.sources_title` + `list_paging_subtitle` when paged |
| `options.items_per_page` | 28 | 28 in GUI + chat |
| `options.source_start` / `source_end` | 1,1 – 4,8 (7×4) | Same slot math in code |
| `items` (back, prev, next, sorter) | arrow / hopper, positions row 5 | Same slots; translatable names |
| `templates.source` | `gray_dye` + `ItemRegistry` override | **Always** `Items.GRAY_DYE` in baseline |
| `components.multiplied_xp` | per-player multipliers, income | **Not** implemented — out of scope |
| `fill` | black glass | **No** fill row — empty slots default air |

**Unsupported on Fabric (documented as Partial):** Bukkit **filters** some sources (e.g. `xp==0` or missing `SourceMenuItems` in reference `SourcesMenu`); Fabric uses **version-valid** `XpSource` set only, matching **`FabricSourcesListSupport`** and the **Sprint 57** command semantics for listing.

## Command roots to regression-test

- `/sources <skill>`
- `/sources <skill> <sort>`
- `/auraskills sources ...`
- `/skills sources ...` / `/sk sources ...` / `/skill sources ...`
- **Console:** `sources <skill> [sort]` from dedicated server console
- **Permission:** `auraskills.command.sources` (default **true** in reference user pattern)

## Pass / fail checklist (Java dedicated server)

- [ ] Player with permission: **GUI** opens when skill has **≥1** version-valid source
- [ ] **Console:** **no** menu; paged **chat** list only
- [ ] **Sort** in menu hopper: cycles all four orders; list order matches chat for same sort
- [ ] **Back** returns to **level progression** for that skill
- [ ] **Paging:** >28 sources split across pages; prev/next
- [ ] **Disabled** skill: parse fails → `UNKNOWN_SKILL` (not menu)
- [ ] Forced **fallback** (only if you inject a throw): `menu_fallback` + chat list (normal runs should not need this)

## Bedrock / Geyser (not tested this sprint)

- **Shape:** `GENERIC_9X6` is the same inventory class used elsewhere in Granite (**NRP** for cross-play).
- **Clicks:** must be verified on a real Geyser+Floodgate server before **release-grade** “Equivalent”; document issues (sort/paging/back) if any.
- **If clicks fail:** players can still use **console-style** workarounds only if an operator runs commands, or a future chat fallback could be revisited — not changed here beyond exception **fallback** for Java `openHandledScreen` failures.

## Remaining menu gaps (post–69)

- Full **Slate** + `menus/sources.yml` **load** for `sources`
- **Leaderboard**, **abilities** menus
- **Unclaimed** inventory UI
- **Confirm** UIs, **openmenu** for arbitrary Slate ids
- **Bedrock** proof matrix for all **inventory** rows

## Risks / caveats

- **Back** to level progression does not restore a **non-zero** skills list page (command path always passes `0`); Bukkit can carry richer menu state via Slate.
- **No** automated MC GUI test; rely on **manual** pass list above.
- **Bukkit** may exclude **0 XP** sources from the list template; **Fabric** lists all **version-valid** sources (same as S57 chat).

## Build / test

```text
.\gradlew -PfabricOnly=true :fabric:test
```

**Expected:** all tests pass including **`FabricSourceSortTypeTest`**.
