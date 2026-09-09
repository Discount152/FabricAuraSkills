# Sprint 120 — Menu presentation repair gate

**Purpose:** Corrective sprint after Sprint **119** built when **operator visual QA failed**. No new gameplay features, no Sources redesign lane, no architecture Programs — only **presentation** fixes until screenshots pass.

**Artifact:** `FabricAuraSkills-2.3.149.jar` (`projectVersion` in `gradle.properties`).

**Acceptance gate (visual sprints):** `Gradle verify PASS` **and** `operator screenshot PASS`. Build-only is insufficient.

## Failed QA themes (baseline)

- Raw MiniMessage / legacy tags in **Stat Info** trait/lore; stat icon showed vanilla item mechanics; text overflow; glass/filler noise; inconsistent **Back** (arrow vs door).
- **Sources:** overloaded title; broken row names (`!!REMOVE!!`, internal ids); sort info belonged on control, not title.
- **Abilities:** oversized / raw tooltip text; repetitive icons; **Back** standard.
- **Leaderboard:** gold nugget rows / named glass / layout noise; Power-only scope unclear.
- **Global:** filler policy, tooltip suppression, **Iron Door** for parent **Back**, arrows only for paging.

## Render paths touched (code)

| Area | Title | Rows / lore | Back | Tooltip suppression | Notes |
|------|-------|-------------|------|--------------------|--------|
| Stat Info | `FabricMenuTitles.statInfoTitle` | `FabricMenuLoreFormatting`, `FabricStatsMenuGui.addTraitLoreIfPresent` → formatter/adapter | IRON_DOOR (built-in / template wiring) | `FabricMenuTooltipPresentation` on panel icon | Template: `FabricStatInfoTemplateSupport`, `FabricMenuInventoryPrep` when no fill |
| Sources | `FabricMenuTitles.sourcesMenuTitle` (short); sort on hopper (`sources_sort_current`) | `FabricSourceTypeLabels`, `FabricSourcesListSupport` filters `!!REMOVE!!`, placeholders | IRON_DOOR | Rows + controls | `FabricSourcesTemplateSupport`, `FabricSourcesMenuGui` |
| Abilities | `FabricMenuTitles.abilitiesMenuTitle` | `splitDesc` + `FabricMenuLoreFormatting`; passive icon variety; mana distinct items | IRON_DOOR | On ability stacks | Lazy `PassiveIconsUnlocked` avoids JUnit `Items` bootstrap |
| Leaderboard | `FabricMenuTitles.fromLeaderboardContextLine` + paging | Built-in `entryStack` uses **PAPER**; template **`leaderboard.yml`**: paper rows, **no** fill | IRON_DOOR (built-in); YAML back iron_door | `putControl` + entry stacks | Power / average / skill modes unchanged; skill-specific board = existing API surface |

## Bundled YAML

- `leaderboard.yml`: removed **gray_stained_glass** fill; **gold_nugget** → **paper**; plain row naming; iron_door back.

## Lang (`en_us.json`)

Added / updated: `stat_info_title_suffix`, `sources_title_suffix`, `sources_sort_current`, `sources_placeholder_name`, `source_type.*`, Sprint **88/119** keys restored where tests expect them (`level_progression_levels_suffix`, `stat_info_title_prefix`, `sources_title_secondary`, `abilities_title_suffix`, `unclaimed_items_title`). Back copy: **+ Stats** / **+ Levels** / **+ Skills** with matching lore lines.

## Tests / build

Command (PASS on agent run):

`.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle`

- `FabricGraniteEnUsKeysTest`: new `sprint120MenuPresentationRepairKeysPresentInEnUs`.
- `FabricLeaderboardBundledTemplateParseTest`: template has **no** `fill`.
- `FabricAbilitiesBundledTemplateParseTest`: `FabricAbilitiesMenuGui` static init no longer loads `Items` during plain JUnit.

## Screenshot checklist (operator)

After installing **2.3.149**:

1. `/stats` → **Strength**, **Regeneration** — no raw `<...>` tags; stat icon tooltip clean; title dark gray bold + gray suffix; back = iron door; no named glass panes; lore wraps reasonably.
2. `/sources farming`, `/sources mining` — short title; sort info on sort button; rows readable; no `!!REMOVE!!`; back = iron door.
3. `/openmenu abilities farming` — descriptions wrapped; no raw tags; icons varied; back = iron door.
4. `/openmenu leaderboard` — rows paper (or template parity); no gold nugget; no gray glass fill from bundle; back = iron door; paging arrows only for pages.
5. `/openmenu level_progression fighting` — title/progression path still sane (not a regression target for this sprint).

## Known limitations / deferrals

- **Player heads** on leaderboard rows when safe: not implemented in this sprint; **paper** / template parity documented here.
- **Power-only** `/openmenu leaderboard` default: unchanged; multi-mode entry exists in code — document for operators; separate behavior sprint if product wants different default.
- **Bedrock / Geyser:** not tested.
- **Full Slate / `MenuFileManager`:** still open.

## Workflow note

Going forward, **visual/UI sprints** close on **build + screenshots**, never **build alone**.
