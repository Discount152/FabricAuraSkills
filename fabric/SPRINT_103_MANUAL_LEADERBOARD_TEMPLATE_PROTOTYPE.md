# Sprint 103 — Manual validation: GraniteLib template-backed `leaderboard` prototype

## Purpose

Prototype a bundled **v0 YAML** GraniteLib template for the **leaderboard** menu: ranked rows, power / average / skill modes, paging, refresh, and **Back** → **Skills**, with **strict fallback** to **`FabricLeaderboardMenuGui` built-in** on any load/validation/placeholder/material failure. Does **not** claim full Slate / `MenuFileManager` parity.

## Template path

- **`assets/auraskills/granite/templates/leaderboard.yml`**
- **`FabricLeaderboardTemplateSupport.TEMPLATE_RESOURCE_PATH`**

## Placeholders supported

| Placeholder | Meaning |
|-------------|---------|
| `{leaderboard_title}` | Resolved title base (same strings as built-in lang keys, server-side). |
| `{leaderboard_type}` | **`Power`**, **`Skill average`**, or skill display name (skill mode). |
| `{skill}` | Skill id (`SimpleName`) in skill mode; **empty** for power/average. |
| `{skill_display}` | **`Skill average`** in average mode; skill display in skill mode; **empty** in power mode. |
| `{rank}` | Global 1-based rank for the row. |
| `{player}` | **`FabricCommandLeaderboardText.resolvePlayerName`**, or **`?`**. |
| `{value}` | Same formatting as built-in: **level** for power/skill; **two-decimal average** for average mode. |
| `{page}` | Current page (1-based). |
| `{total_pages}` | Page count (1-based; **`maxPageIndex + 1`**). |

Not a global placeholder registry: expansion is local to this menu only.

## Dynamic row behavior

- Grid item: YAML key **`entry`** or **`action: leaderboard_row`**.
- Slots must **exactly** match **`FabricLeaderboardMenuGui.builtinEntrySlots()`** (10 slots: two rows of five).
- For each visible **`SkillValue`** on the page, the next slot is used; stack **material / name / lore** come from the template; placeholders expanded; **read-only** (no row click action).

Bundled template uses **`minecraft:paper`** for rows (same as built-in); no player skin / skull NBT.

## Average vs skill mode

Unchanged **`FabricOpenMenuLeaderboard`** / **`FabricLeaderboardMenuGui.open`** behavior:

- `/openmenu leaderboard` → power.
- `/openmenu leaderboard average` → average.
- `/openmenu leaderboard <skill>` → skill board.

Console / target-player variants unchanged.

## Refresh

**Refresh** (**slot 49**, **`action: refresh`**) re-opens the same view at the **same 1-based page** (`FabricLeaderboardMenuGui.open(…, fPage1)`), matching built-in re-read of the current cached slice (no **`updateleaderboards`** in the click handler).

## Back and paging

- **Back** → **`FabricAuraSkillsMenus.open(SKILLS, …)`** (same as built-in).
- **Previous** / **Next** only when **`maxPage0 > 0`**; stacks shown only when the control is active (same semantics as built-in).

## Fallback behavior

Template path returns **`false`** (warn log, no crash) when: missing resource, YAML invalid, wrong **`id`** / **`rows`**, missing entry definition, wrong entry slot count/order, nav keys/actions/slot positions wrong, unknown item id, unresolved `{...}` after expansion, or unexpected runtime exception. **`openBuiltIn`** then runs so the player always gets a menu.

## Files changed (typical)

- `fabric/.../FabricLeaderboardMenuGui.java`, `FabricLeaderboardTemplateSupport.java`, `FabricLeaderboardPlaceholders.java`
- `assets/auraskills/granite/templates/leaderboard.yml`
- `slate-fabric/.../GraniteTemplateActionNames.java`, `GraniteTemplateItems.java`, tests
- `Changelog.md`, `gradle.properties`, parity / menu port docs

## Tests run

```bat
.\gradlew -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build
```

Optional: **`verifyFabricReleaseBundle`** when cutting a release artifact.

## Java validation checklist (operator)

On a **vanilla Java** client, with **`FabricAuraSkills-2.3.131.jar`** (or newer sprint artifact):

1. `/openmenu leaderboard`
2. `/openmenu leaderboard average`
3. `/openmenu leaderboard farming` (or another enabled skill)
4. `/openmenu leaderboard mining`

If target-player paths are safe:

5. `/openmenu leaderboard <player> average`
6. `/openmenu leaderboard <player> farming`

**Expect:** menu opens; title readable; ranks / names / values readable; **no** raw `auraskills.granite.*` keys in inventory UI; **no** literal `{rank}` / `{player}` / `{value}` in item text; previous/next when multiple pages; refresh re-opens same page; back to **Skills**; **no** fallback warnings in console unless deliberately breaking the template.

**Chat unchanged:** `/top`, `/top average`, `/skilltop <skill>`, `/rank`, `/skillrank`, `/updateleaderboards`.

## Known limitations

- No disk override, hot reload, full Slate action engine, or global placeholder registry.
- Template visuals (fill, plain names in YAML) may differ from built-in translatable control labels.
- **Bedrock / Geyser:** deferred — **not** tested this sprint.

## Recommended next sprint

**`unclaimed_items`** template prototype, **disk override**, or **template hot reload** — per Path A backlog; full **`MenuFileManager`** parity remains open.
