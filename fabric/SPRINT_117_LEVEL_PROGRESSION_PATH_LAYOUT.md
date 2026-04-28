# Sprint 117 — Level progression snake-path layout (manual)

**Version:** `FabricAuraSkills-2.3.146.jar` (`gradle.properties` `projectVersion`).

**Scope:** Template `level_progression` layout and control placement only (no XP math, reward logic, or Sources mechanics beyond slot positions).

## Purpose

Lock bundled Granite `level_progression` to a 24-cell snake **path** per page, row 1 inert, fixed bottom controls.

## Old vs new

- **Before (Sprint 100–116):** 28-slot grid (same order as `sources`).
- **Sprint 117:** 24 path slots + strict control slots; paging by 24 levels.

## Path slots (order = level index on page)

`9, 18, 27, 36, 37, 38, 29, 20, 11, 12, 13, 22, 31, 40, 41, 42, 33, 24, 15, 16, 17, 26, 35, 44`

- First: **9** (level 1 page 1 = row 2 col 1 visually). Last: **44**.
- Java: `FabricLevelProgressionLayout.LEVEL_PATH_SLOTS`, `PAGE_SIZE = 24`.

## Slot map (6x9, zero-based rows)

- **Row 1:** slots 0–8 — empty/inert (no fill applied).
- **Rows 2–5:** path cells get level stacks; off-path cells use template fill (`glass_pane`) when defined, else empty. Unused path cells on the last page: same fill/empty rule.
- **Row 6 (controls):** slots 45–53; only 45, 48, 49, 52, 53 used as below; 46, 47, 50, 51 stay empty.

## Control placement

- **45** — Back (Skills hub, list page preserved)
- **48** — Sources (knowledge book)
- **49** — Abilities (amethyst shard) — **required** in template
- **52** — Previous page (hidden if single page)
- **53** — Next page (hidden if single page)

Previous is immediately left of Next; Next is bottom-right.

## Sprint 116 preserved

Title style, lime dye / XP bottle / gray dye states, compact `{level_detail}` / `{level_status}`, tooltip suppression — unchanged.

## Strict validation

`FabricLevelProgressionTemplateSupport` requires level slots to match `LEVEL_PATH_SLOTS` exactly and navigation slots to match the table above. Mismatch or missing `abilities` → warn → `FabricLevelProgressionMenuGui` built-in.

## Built-in fallback (**Sprint 118**)

**Before 2.3.147:** built-in **`FabricLevelProgressionMenuGui`** used a legacy compact layout (hopper **50** for sources).

**2.3.147+:** built-in **`openBuiltInSnake`** matches **`FabricLevelProgressionLayout`** (same snake path, **24** cells/page, controls **45/48/49/52/53**). See **`SPRINT_118_LEVEL_PROGRESSION_PATH_POLISH_AND_FALLBACK_PARITY.md`**.

## Tests / build

`.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle`

- **PASS** for `FabricAuraSkills-2.3.146.jar` (agent run).
- Tests: `FabricLevelProgressionBundledTemplateParseTest`, `layoutConstantsMatchSprint117`.

## Bedrock / Geyser

**Not** tested. Do not claim cross-play menu validation from this sprint.
