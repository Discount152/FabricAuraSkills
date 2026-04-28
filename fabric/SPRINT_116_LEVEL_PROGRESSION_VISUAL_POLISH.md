# Sprint 116 — Level progression menu visual polish

**Branch:** `feature/granitelib-module-ids`  
**Jar:** `FabricAuraSkills-2.3.145.jar`  
**Scope:** `level_progression` template + built-in fallback visuals and copy only. **No** XP math, rewards, commands, or Bedrock work.

## Purpose

Align the first submenu after **Skills** (`/skills` -> skill -> **level progression**) with the approved Skills/Stats hub language: quiet fill, clear title (**Skill name + Levels**), distinct level states, short tooltips, navigation that reads cleanly, and **Sprint 115**-style suppression of vanilla item mechanics on symbolic icons.

## Visual / copy changes

### Title

- Template: MiniMessage `<gold><bold>{skill_display}</bold></gold> <gray>Levels</gray>` (+ paging subtitle when multi-page).
- Built-in fallback: lang key `auraskills.granite.menu.level_progression_title` -> `%s Levels`.

### Level row state icons (Java)

| State | Icon |
|-------|------|
| Completed (display level < user level) | Lime dye |
| Current | Experience bottle |
| Locked | Gray dye |

Implemented in `FabricMenuPresentation#levelProgressionRowIcon` (template rows still swap material after YAML base stack is built).

### Level row tooltip (template)

Compact three-line lore:

1. Status: **Completed** / **Current Level** / **Locked** (`en_us.json`).
2. Blank separator.
3. **Completed / locked:** `XP required: <amount>` (tier appropriate to state — locked uses threshold to reach that level; completed uses tier cleared while at that level).
4. **Current (not maxed):** `Progress: <current_xp> / <required_for_next>`.
5. **Current (maxed):** `Max level reached`.

Placeholder `{level_detail}` is filled in `FabricLevelProgressionTemplateSupport#addLevelRowPlaceholders`.

### Fill

- `minecraft:glass_pane` with blank display name (space).

### Controls (template YAML)

| Control | Material | Slots (bundled) |
|---------|----------|-----------------|
| Back | Arrow | 45 |
| Previous | Arrow | 48 (when page > 0) |
| Next | Arrow | 49 (when more pages) |
| Sources | Knowledge book | 50 |
| Abilities | Amethyst shard | 51 |

Actions unchanged: **back** -> Skills, **open_sources**, **open_abilities**, **previous_page**, **next_page**.

### Tooltip suppression

`FabricMenuTooltipPresentation#suppressVanillaMechanicsTooltip` on fill, every level row after icon swap, and all control stacks on the template path.

Built-in fallback: same helper on main (book), back (arrow), sources (**knowledge book**; was hopper).

## Files touched (principal)

- `fabric/src/main/resources/assets/auraskills/granite/templates/level_progression.yml`
- `fabric/.../FabricLevelProgressionTemplateSupport.java`
- `fabric/.../FabricLevelProgressionPlaceholders.java`
- `fabric/.../FabricLevelProgressionMenuGui.java`
- `fabric/src/main/resources/assets/auraskills/lang/en_us.json`
- Tests: `FabricLevelProgressionBundledTemplateParseTest`, `FabricGraniteEnUsKeysTest`
- `Changelog.md`, `gradle.properties`, parity/port markdown

## Tests / build

```text
.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle
```

## Screenshot checklist (operator)

- Open via `/skills` -> skill or `/openmenu level_progression <player> <skill>`.
- Title reads like **Farming Levels** (plus page suffix if paged).
- Completed = lime, current = XP bottle, locked = gray.
- Tooltips: compact, no vanilla durability/food/potion noise on icons.
- Back, Sources, Abilities, Previous/Next behave as before.
- Empty slots show quiet glass (or empty if fill removed in a custom disk template).

## Known limitations

- Operators with **disk-overridden** `level_progression.yml` must add `{level_detail}` to level row lore if they sync with the new bundled format; otherwise brace placeholders may linger.
- **Bedrock/Geyser** not validated this sprint.
- Full **Slate/MenuFileManager** parity remains open.

## Recommended next step

Operator screenshot pass on **2.3.145**, then continue hub-adjacent polish (e.g. **sources** / **abilities** rows) or Path A framework work as planned.