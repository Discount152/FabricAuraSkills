# Sprint 114 — Skills / Stats hub tooltip polish (FabricAuraSkills-2.3.143+)

## Purpose

Polish bundled Granite **skills** and **stats** hub tooltips after Sprint 113 layout lock-in: compact hub copy, **brewing stand** for Alchemy, and **TOOLTIP_DISPLAY** to hide vanilla item mechanics while keeping custom **ITEM_NAME** and **LORE**.

## Issues fixed (from 2.3.142 screenshots)

- Vanilla lines: attack damage, attack speed, durability, armor/tool stats, potion internals on grid icons.
- Alchemy used potion; now **minecraft:brewing_stand** via **FabricMenuPresentation**.
- Stats hub used long or noisy lore; hub uses **STAT_HUB_SHORT_DESCRIPTIONS** one-liners and **▸ Details**.

## Skills row format

- Skill name (raw display)
- `Level n · XP x`
- `x in this level`
- blank
- `▸ Levels`

## Stats row format

- Stat name (raw display)
- `Value: …`
- Short description (locked English strings)
- blank
- `▸ Details`

## Suppression

**FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip** — **TooltipDisplayComponent** + listed **DataComponentTypes** to hide mechanics; applied to hub rows and utility items on skills/stats templates.

## Files

**FabricMenuPresentation**, **FabricMenuTooltipPresentation**, **FabricSkillsTemplateSupport**, **FabricStatsTemplateSupport**, **stats.yml** (comment), tests, **Changelog**, **gradle.properties**, parity docs.

## Tests / build

`.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle`

(Fill PASS/FAIL after run.)

## Operator checklist

Hover on **/skills**: Fighting, Mining, Alchemy, Your Stats, Leaderboard, Close — no vanilla mechanics; Alchemy is brewing stand.

Hover on **/stats**: Strength, Crit Chance, Crit Damage, Health, Wisdom, Speed, Back, Close — short descriptions; **▸ Details**.

## Known limitations

Built-in fallback **FabricSkillsMenuGui** / **FabricStatsMenuGui** unchanged if template fails. Hub stat blurbs are English literals in code.

## Bedrock / Geyser

Not tested; deferred.

## Next step

Operator screenshots on **2.3.143** JAR.

## Build result (2026-04-26)

`.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle` — **PASS** (`verifyFabricReleaseBundle`: OK for `FabricAuraSkills-2.3.143.jar`).
