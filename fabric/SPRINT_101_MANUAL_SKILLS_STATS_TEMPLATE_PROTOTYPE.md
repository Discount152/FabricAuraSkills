# Sprint 101 — Manual: template-backed `skills` and `stats` list menus

| Field | Value |
|--------|--------|
| **Sprint** | **101** — **`skills`** + **`stats`** lobby lists on GraniteLib v0 templates |
| **JAR** | **`FabricAuraSkills-2.3.129.jar`** (+ **`granitelib-fabric`**) |
| **Bedrock / Geyser** | **Deferred / not tested** |

## Purpose

**Sprint 96–100** proved the template path on detail and secondary menus. **Sprint 101** applies the same **template-first, strict fallback** pattern to the two **highest-traffic list surfaces**: **Skills** and **Stats**.

## Template paths (classpath)

| Menu | Constant | Resource |
|------|-----------|----------|
| **skills** | **`FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH`** | `assets/auraskills/granite/templates/skills.yml` |
| **stats** | **`FabricStatsTemplateSupport.TEMPLATE_RESOURCE_PATH`** | `assets/auraskills/granite/templates/stats.yml` |

Bundled under **`fabric/src/main/resources/`**.

## Placeholders supported

### Skills (`FabricSkillsPlaceholders`)

| Token | Meaning |
|--------|---------|
| `{skill}` | Skill id simple name |
| `{skill_display}` | Localized skill display name |
| `{skill_level}` | Integer skill level |
| `{skill_xp}` | XP in current level (**`User#getSkillXp`**, formatted) |
| `{skill_xp_in_level}` | Same numeric string as **`{skill_xp}`** (alias for authors) |
| `{page}` | 1-based list page |
| `{total_pages}` | 1-based total pages |

### Stats (`FabricStatsPlaceholders`)

| Token | Meaning |
|--------|---------|
| `{stat}` | Stat id simple name |
| `{stat_display}` | Localized stat display name |
| `{stat_value}` | **`FabricStatsMenuGui.formatStatLevel`** (trait-direct or stat level) |
| `{stat_description}` | Stat description, newline → space; **truncated ~200** chars like built-in list |
| `{page}` | 1-based list page |
| `{total_pages}` | 1-based total pages |

Any remaining `{...}` after expansion aborts the template path → **built-in** menu.

## Dynamic row behavior

- **Skills:** item key **`skill`** or **`action: skill_row`**. **28** slots, same order as **`FabricSourcesMenuGui.builtinSourceGridSlots()`**. One stack per **enabled** skill on the page. If the player **lacks** skill permission, the same extra lore line as built-in is **appended** after template lore.
- **Stats:** item key **`stat`** or **`action: stat_row`**. Same **28**-slot grid. One stack per **enabled** stat on the page.

## Click / navigation behavior

| Control | Action | Behavior |
|---------|--------|----------|
| **Previous / Next** | **`previous_page`** / **`next_page`** | **`FabricMenuRegistry.open`** **`skills`** / **`stats`** with **`listPage`** ± 1 (slots **45** / **53**) |
| **Skill row** | **`skill_row`** | **`FabricAuraSkillsMenus.openLevelProgression(skill, …, listPage)`** — **no-op** without permission |
| **Stat row** | **`stat_row`** | **`FabricAuraSkillsMenus.openStatInfo`** only if **`menus.stat_info.enabled`** — else **no-op** (built-in parity) |

## Fallback behavior

Aborts to **existing** **`FabricSkillsMenuGui`** / **`FabricStatsMenuGui`** on: missing file, invalid YAML, wrong **`id`** / **`rows`**, missing or invalid row grid, slot count ≠ **28**, slot order mismatch, missing **`previous`** / **`next`** or wrong actions, unresolved placeholders, unknown **material**, or runtime failure. **Warn** log; **no** server crash.

## Files changed (summary)

| Area | Files |
|------|--------|
| Templates | **`skills.yml`**, **`stats.yml`** |
| GraniteLib | **`GraniteTemplateActionNames`**, **`GraniteTemplateItems`** |
| Fabric | **`FabricSkillsTemplateSupport`**, **`FabricSkillsPlaceholders`**, **`FabricStatsTemplateSupport`**, **`FabricStatsPlaceholders`**, **`FabricSkillsMenuGui`**, **`FabricStatsMenuGui`** |
| Tests | **`FabricSkillsStatsBundledTemplateParseTest`**, granitelib template tests, **`FabricMenuGuiTranslatableScanTest`** paths |

## Tests run

```text
.\gradlew -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build
```

## Java validation checklist

On a **Java** client:

1. **`/skills`**, **`/sk`**, **`/skill`**, **`/stats`**, **`/openmenu skills`**, **`/openmenu stats`** (as configured).
2. **Expected:** Menus open; **28**-slot **7×4** grid; titles readable; **no** raw **`auraskills.granite.*`** on template-driven strings; **no** raw **`{skill_level}`** / **`{stat_value}`** etc.
3. **Skill row** → **level progression**; **stat row** → **stat_info** when enabled; **Back** from child menus still restores **list** page.
4. **Negative (local):** corrupt bundled YAML → **built-in** list menu; **do not** commit broken templates.

## Known limitations

- **Not** disk override, **not** hot reload, **not** full action engine, **not** global placeholder registry.
- **Template stats rows** do **not** auto-append **trait** lines (built-in list does); use **`{stat_description}`** / YAML lore or rely on **built-in** fallback for full trait parity.
- **Paging** uses **28** items per page on the template path; **built-in** list uses the **YAML slice** list range (default up to **45** slots in rows 0–4). Operators may see different page counts between paths if template fails vs succeeds.
- **Bedrock** inventory behavior **not** validated.

## Operator validation

**Pending** on **`FabricAuraSkills-2.3.129.jar`**.

## Recommended next sprint

Bounded template for **abilities** or **leaderboard**, or **placeholder** depth — still **not** **`MenuFileManager`** complete.
