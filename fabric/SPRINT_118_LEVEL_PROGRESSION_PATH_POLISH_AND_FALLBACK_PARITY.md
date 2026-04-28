# Sprint 118 — Level progression path polish + built-in fallback parity (manual)

**Version:** `FabricAuraSkills-2.3.147.jar` (`gradle.properties` `projectVersion`).

**Scope:** Template `level_progression` title/filler styling, quiet filler tooltips, and built-in `FabricLevelProgressionMenuGui` layout parity with `FabricLevelProgressionLayout`. No XP math, reward logic, or Sources behavior beyond existing control slots.

## Purpose

- Restore Sprint **116** title treatment when Sprint **117** screenshots showed paging/title mismatch.
- Stop **Glass Pane** / vanilla id noise on hover for inert filler cells.
- Remove legacy compact built-in fallback (hopper **50**) so invalid templates still show the approved snake path.

## Issues after Sprint 117 (operator screenshots)

1. Skill + paging title color/style drift vs Sprint **116** approved direction.
2. Off-path **glass_pane** cells showed vanilla names on hover.

## Title color / style (template path)

- YAML title remains: `<gold><bold>{skill_display}</bold></gold> <gray>Levels</gray>`.
- Paging suffix **(Page X of Y)** is **not** mixed into MiniMessage in a way that loses gray; Java appends **`auraskills.granite.menu.list_paging_subtitle`** as **gray literal** text via `FabricLevelProgressionMenuContent#appendGrayPagingSubtitle`.
- Built-in title uses the same colors: **gold bold** skill display, **gray** ` Levels`, **gray** paging when multi-page (`#builtInMenuTitle`).

## Blank-name filler panes

- `FabricLevelProgressionMenuContent#applyQuietMenuFiller`: `ITEM_NAME` = `Text.literal(" ")` + `FabricMenuTooltipPresentation#suppressVanillaMechanicsTooltip`.
- Applied to: template fill base, each fill copy placed off-path / unused path, and built-in glass for off-path / padding path cells.

## Built-in snake-layout parity

- `FabricLevelProgressionMenuGui#openBuiltInSnake` uses `FabricLevelProgressionLayout.PAGE_SIZE`, `LEVEL_PATH_SLOTS`, and control constants **45 / 48 / 49 / 52 / 53**.
- Row **1** empty; level rows use Sprint **116** compact placeholders + `FabricMenuPresentation#levelProgressionRowIcon` (lime dye / experience bottle / gray dye).
- No `SLOT_OPEN_SOURCES` / hopper **50** for sources.

## Final path slots

`9, 18, 27, 36, 37, 38, 29, 20, 11, 12, 13, 22, 31, 40, 41, 42, 33, 24, 15, 16, 17, 26, 35, 44`

## Control placement

| Slot | Role |
|------|------|
| 45 | Back |
| 48 | Sources |
| 49 | Abilities |
| 52 | Previous (if multi-page) |
| 53 | Next (if multi-page) |

## Tests / build

`.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle`

- **Fabric:** `FabricLevelProgressionSprint118Test`, `FabricLevelProgressionBundledTemplateParseTest` (Sprint 117 path contract).

## Screenshot checklist (operator)

**Template path (valid `level_progression.yml`):**

- [ ] Snake path visible, **24** levels/page.
- [ ] Controls at **45 / 48 / 49 / 52 / 53**.
- [ ] Title: gold bold skill name, gray `Levels` + gray paging.
- [ ] Hover off-path glass: no **Glass Pane** / registry id.

**Forced fallback (break disk template temporarily, then restore):**

- [ ] Same snake path and controls as template.
- [ ] No hopper-at-**50** sources opener in built-in.

## Known limitations

- **Bedrock / Geyser:** not tested; do not claim cross-play menu validation.
- **Slate / MenuFileManager:** full parity still open.

## Recommended next step

Continue deferred Bedrock/Geyser checklist when a tester is available; otherwise next menu/template work per product backlog (not part of this sprint).
