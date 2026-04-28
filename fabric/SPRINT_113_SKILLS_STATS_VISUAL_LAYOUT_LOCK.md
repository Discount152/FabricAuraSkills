# Sprint 113 — Skills + stats visual layout lock-in

## Purpose

Lock **`/skills`** and **`/stats`** bundled Granite templates to the owner-approved hub layout (fixed cells, utilities, icons, quiet empty slots), without claiming Slate **`MenuFileManager`** parity or Bedrock validation.

## Final skills layout (6×9 `GENERIC_9X6`)

- **Title:** `§8§lYour Skills`
- **No fill:** unused top slots stay **empty**; row **5** (slots **36–44**) always empty; utilities on row **6**.
- **Skill grid (11 cells):** slots **12–14**, **20–24**, **30–32** — order **`LOCKED_SKILL_IDS`** in `FabricSkillsTemplateSupport`.
- **Utilities:** **0** = gold ingot → **power** leaderboard page **1**; **8** = **player head** (Java `ProfileComponent.ofStatic`) → stats menu; **45** = chest minecart → unclaimed **only if** `User#getUnclaimedItems` non-empty; **53** = barrier → close screen.

## Final stats layout

- **Title:** `§8§lYour Stats`
- **No fill:** row **0** empty; row **5** (36–44) empty.
- **3×3 grid:** slots **12–14**, **21–23**, **30–32** — stats order **`LOCKED_STAT_IDS`** in `FabricStatsTemplateSupport`.
- **45** = arrow → **back** to skills; **53** = barrier → close.

## Icon mappings

Implemented in **`FabricMenuPresentation`** (skill/stat rows) per owner list (e.g. bow/archery, iron hoe/farming, diamond sword/strength, target/crit_chance, …).

## Utility behavior

| Control | Action |
|---------|--------|
| Leaderboard | `FabricLeaderboardMenuGui.open(POWER, null, 1)` |
| Stats (head) | Open **`FabricMenuKeys.STATS`** |
| Unclaimed | Open unclaimed GUI page 0 when state non-empty |
| Close | `ServerPlayerEntity#closeHandledScreen` |
| Stats → Back | Open **`FabricMenuKeys.SKILLS`** |

## Empty slots / filler policy

Bundled YAML **omits** `fill`; Java initializes **all 54** top slots to `EMPTY` then places only defined cells.

## Conditional unclaimed

**Implemented:** minecart stack and click route only when **`!user.getUnclaimedItems().isEmpty()`**; slot **45** stays empty otherwise.

## Files changed (summary)

- `slate-fabric/.../GraniteTemplateActionNames.java`, `GraniteTemplateActionNamesTest.java`
- `fabric/.../FabricGraniteMenuTemplateActionContract.java`
- `fabric/.../FabricSkillsTemplateSupport.java`, `FabricStatsTemplateSupport.java`, `FabricMenuPresentation.java`
- `fabric/.../templates/skills.yml`, `stats.yml`
- `fabric/.../FabricSkillsStatsBundledTemplateParseTest.java`, `FabricGraniteMenuTemplateActionContractTest.java`
- `gradle.properties`, `Changelog.md`, `fabric/MENU_AND_COMMANDS_PORT.md`, `fabric/PARITY_BACKLOG.md`, `fabric/PARITY_COMPLETION_ROADMAP.md`, `fabric/PARITY_AUDIT_LEDGER_PHASE1.md`

## Tests / build

`.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle`

**Result:** PASS; `FabricAuraSkills-2.3.142.jar`.

## Known limitations

- **11** curated skills on the hub; **`endurance`**, **`sorcery`**, **`healing`**, **`forging`** are **not** shown here (still in API; other entry points unchanged).
- **6-row** vanilla chest used (row 5 blank) — not a literal **5-row** screen type.
- **Built-in** fallback **`FabricSkillsMenuGui` / `FabricStatsMenuGui`** if template invalid: **legacy** paged list, not this layout.
- **Bedrock/Geyser:** **not** tested; legacy `§` in YAML titles.

## Screenshot checklist

Install **2.3.142**, capture **`/skills`** and **`/stats`**: match titles, utility positions, **3×3** stats, empty quiet zones, head texture, conditional unclaimed.

## Recommended next step

Operator second-pass screenshots; then iterate submenus (`level_progression`, `sources`, …) or resume deeper Slate parity per backlog.
