# Sprint 100 — Manual: template-backed `level_progression` prototype

| Field | Value |
|--------|--------|
| **Sprint** | **100** — **`level_progression`** template path + paged level grid |
| **JAR** | **`FabricAuraSkills-2.3.128.jar`** (+ **`granitelib-fabric`**) |
| **Bedrock / Geyser** | **Deferred / not tested** |

## Purpose

After **`stat_info`** (mostly static) and **`sources`** (dynamic grid), **`level_progression`** is the next high-traffic surface. This sprint adds a **template-first** path with a **paged 7×4 level grid**, reusing **Sprint 99** helpers, with **strict fallback** to the existing **single-card** built-in **`FabricLevelProgressionMenuGui`**.

## Template path (classpath)

**`FabricLevelProgressionTemplateSupport.TEMPLATE_RESOURCE_PATH`** → `assets/auraskills/granite/templates/level_progression.yml`

## Placeholders supported

| Token | Meaning |
|--------|---------|
| `{skill}` | Skill id simple name |
| `{skill_display}` | Localized skill display name |
| `{level}` | Level number shown in that grid cell (1…maxLevel) |
| `{level_status}` | **Unlocked** / **Current** / **Locked** (vs player’s current skill level; **`FabricGraniteText`**) |
| `{level_xp_required}` | XP required for **that** row’s tier (**`XpRequirements#getXpRequired(skill, level+1)`**), formatted; **`-`** on max-level row |
| `{current_xp}` | Player’s current skill XP (same on every row for that view) |
| `{page}` | 1-based progression grid page |
| `{total_pages}` | 1-based total progression pages |

Unresolved `{...}` after expansion → template path aborts → **built-in** menu.

## Dynamic level row behavior

- Template item: key **`level`** or **`action: level_row`** (**`GraniteTemplateActionNames.LEVEL_ROW`**).
- **28** slots, same order as **`FabricSourcesMenuGui.builtinSourceGridSlots()`**.
- Levels **1…`skill.getMaxLevel()`** fill the grid in order; **28** levels per page; read-only stacks (no per-level clicks).
- **No** change to reward math, XP storage, or level rules — display only.

## Navigation behavior

| Control | Action constant | Behavior |
|---------|-------------------|----------|
| Back | **`back`** | **`FabricAuraSkillsMenus.open(SKILLS, …, listPage)`** |
| Previous / Next | **`previous_page`** / **`next_page`** | Re-open **`level_progression`** via registry with same skill + **`listPage`**, **`progressionPage`** ± 1 |
| Sources | **`open_sources`** | Same as built-in hopper: empty list → **`sources.empty`** message; else **`openSources`** (ascending, page 0, same return **`listPage`**) |
| Abilities | **`open_abilities`** | **`FabricAuraSkillsMenus.openAbilities`** (same return **`listPage`**). Built-in **did not** expose this; template path **does** when **`abilities`** is in YAML. |

**`FabricMenuContext.progressionPage`:** 0-based index for the level grid; other menus ignore it (default **0**).

## Fallback behavior

Template path **aborts** (warn log, built-in) on: missing file, invalid YAML, **`id`** ≠ **`level_progression`**, **`rows`** ≠ **6**, missing / invalid level grid, slot count ≠ **28**, slot order mismatch, missing **back / previous / next / sources** or wrong actions, invalid **abilities** item if present, unresolved placeholders, unknown **material**, or runtime failure.

**Built-in** path unchanged in intent: main summary + back + hopper sources at slot **50** (config-driven main/back slots from **`menus/level_progression.yml`** **`granite:`** slice).

## Files changed (summary)

| Area | Files |
|------|--------|
| Template | **`fabric/src/main/resources/assets/auraskills/granite/templates/level_progression.yml`** |
| GraniteLib | **`GraniteTemplateActionNames.java`**, **`GraniteTemplateItems.java`** (`findLevelGridDefinition`) |
| Fabric | **`FabricLevelProgressionTemplateSupport.java`**, **`FabricLevelProgressionPlaceholders.java`**, **`FabricLevelProgressionMenuGui.java`**, **`FabricMenuContext.java`** |
| Lang | **`en_us.json`** — **`level_progression_status_*`** |
| Tests | **`FabricLevelProgressionBundledTemplateParseTest`**, **`FabricMenuContextTest`**, granitelib tests |

## Tests run

```text
.\gradlew -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build
```

## Java validation checklist

On a **Java** client (skills enabled):

1. `/openmenu level_progression <skill>` and skill shortcut (e.g. `/farming`) if configured.
2. **Expected:** Menu opens; **title** readable; **level** cells show **numbers + status + XP lines**; no raw **`auraskills.granite.*`** on items; no raw **`{level}`** etc.
3. **Previous/Next** when more than **28** levels; **Back** restores **Skills** list page; **Sources** matches built-in hopper behavior; **Abilities** opens abilities menu.
4. **Negative (local only):** break YAML → **built-in** menu; do **not** commit broken template.

## Known limitations

- **Not** Slate / disk / hot reload / full action engine.
- **Template** UX is a **level grid**; **built-in** fallback remains the **single** main item — intentional prototype split.
- **Abilities** button exists on **template** path only (unless built-in is extended later).
- **Disabled** skill edge cases for abilities click are **not** fully mirrored ( **`FabricAbilitiesMenuGui`** may no-op).

## Operator validation

**Pending** on **`FabricAuraSkills-2.3.128.jar`**.

## Recommended next sprint

Bounded template on **`skills`** / **`stats`** list rows, or incremental **placeholder** depth — still **not** **`MenuFileManager`** complete.
