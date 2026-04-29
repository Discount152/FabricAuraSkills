# Sprint 122 — Targeted runtime menu fixes (post-2.3.150 audit)

## Purpose

Fix confirmed operator-visible issues when Granite templates were already BUNDLED_LOADED. Work targets formatter output, bundled YAML, stack presentation, abilities lore, and built-in menu defaults — not Bedrock, not full Slate parity, not leaderboard or abilities redesign.

## Code and assets

- **FabricTextFormatter**: legacy `&n` / section `n` map to MiniMessage **underlined** (not the invalid **underline** token). Other legacy codes remain the usual MiniMessage tags (bold, italic, strikethrough, obfuscated, colors, reset).
- **Bundled templates** (`assets/auraskills/granite/templates/*.yml`): removed global fills using **gray_stained_glass_pane** or **glass_pane** so empty inventory cells stay **EMPTY** (no hover).
- Same bundled templates: **back** control uses **minecraft:iron_door** (arrows only on paging rows).
- **FabricGraniteTemplateStacks**: **isBlankTemplateDisplayName** treats null, empty string, single space, and whitespace-only strings as blank; **buildFillStack** / **buildTemplateItemStack** use **FabricMenuItemPresentation.applyQuietFiller** for those names.
- **FabricAbilitiesTemplateSupport**: passive and mana ability body text appended in Java with **FabricMenuLoreFormatting.appendWrappedFormattedLore** (42-character wrap; descriptions not duplicated as raw placeholders in YAML lore).
- **Embedded menus** (`menus/skills.yml`, `menus/stats.yml`, `menus/stat_info.yml`, `menus/level_progression.yml`): **back** material **minecraft:iron_door**. **FabricMenuItemDefinitions.DEFAULT_BACK_ITEM_ID** is **minecraft:iron_door**. Arrows remain for paging only.

## Tests

- **FabricTextFormatterTest**: underline mapping; no literal **underline** tag in mapped MiniMessage.
- **FabricSprint122RuntimeMenuRegressionTest**: template directory scan (no forbidden glass); no **back** block with arrow material in bundled YAML; embedded **menus/*.yml** back id; blank display-name helper.
- **FabricMenuLoreFormattingTest**: multiline plain-body simulation — no chunk longer than 42 characters (except long single tokens per word-wrap rules).

## Build command

`.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle`

## Operator checklist

Exercise: `/stats` (Regeneration, Strength), `/openmenu abilities defense`, `/openmenu abilities excavation`, `/sources fishing`, `/openmenu leaderboard`, `/openmenu level_progression fighting`.

**Pass:** no invalid underline tag visible as text; no gray glass pane name on empty slots; ability descriptions wrap; Back uses iron door; arrows only on paging.

## References

**Changelog.md** (2.3.151), **MENU_AND_COMMANDS_PORT.md**, **PARITY_BACKLOG.md**, **PARITY_COMPLETION_ROADMAP.md**.
