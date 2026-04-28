# Sprint 112 — Granite Skills menu visual redesign pass

## Goal

Improve **player-facing polish** of template-backed Granite Skills menus (bundled YAML under `fabric/src/main/resources/assets/auraskills/granite/templates/`) so layouts read as **intentional and readable** instead of prototype/debug UIs—without new gameplay math, Bukkit, Bedrock work, or MenuFileManager parity claims.

**Primary lever:** YAML (materials, names, lore, slots, fill, titles) plus **minimal Java** for safe presentation helpers only.

## Version / artifact

- **FabricAuraSkills** `2.3.141`
- **granitelib-fabric** `0.1.0-alpha.2` (unchanged); companion jar rebuilt with the fabric module.

## Menus reviewed

| Menu | Template | Visually addressed |
|------|----------|-------------------|
| Skills | `skills.yml` | Yes |
| Stats | `stats.yml` | Yes |
| Level progression | `level_progression.yml` | Yes |
| Stat info | `stat_info.yml` | Yes |
| Sources | `sources.yml` | Yes |
| Abilities | `abilities.yml` | Yes |
| Leaderboard | `leaderboard.yml` | Yes |
| Unclaimed items | `unclaimed_items.yml` | Yes |

## Problems observed (baseline) → what we changed

| Issue | Response |
|-------|----------|
| Wall of identical books / flat green progression rows | Per-default-skill vanilla row icons; level rows use **lime / gold / gray** materials by state |
| Weak hierarchy and nav | Gray frame fill; clearer control labels and materials (e.g. iron door style back, comparator sort, compass refresh) |
| Sources repetition | Experience bottle-style row fallback; **merge** configured `menu_item` from `FabricSourceMenuItems` when present |
| Abilities sparse / generic books | Stronger passive block (lapis metaphor), **end crystal** emphasis for mana slot where template allows |
| Long or mechanical titles | Shorter, player-facing title patterns in YAML |

## Menu-by-menu summary (visual)

- **Skills:** Distinct row icons via `FabricMenuPresentation` skill map; softer frame; paging lore reads as navigation, not filler.
- **Stats:** Stat-specific row icons; layout aligned with skills family but distinct lore structure.
- **Level progression:** Tri-state row materials (completed / current / locked); bottom bar labels clarified; less monotonous than single green dye for every row.
- **Stat info:** Central stat panel uses stat icon when known; back affordance clearer.
- **Sources:** Title tightened; sort and paging controls use consistent control language and icons; rows can show source-config icons.
- **Abilities:** Passive grid and mana highlight given stronger visual roles; reduced empty generic feel.
- **Leaderboard:** Rank rows use **gold nugget** metaphor; refresh / back / paging grouped as controls.
- **Unclaimed items:** Reward framing (“Pending Rewards” style); claim-all uses **chest minecart** affordance.

## YAML-only vs code

| Area | Notes |
|------|-------|
| All eight `*.yml` | Materials, display names, lore, fill slots, titles—primary redesign surface. |
| **Java** | **`FabricMenuPresentation`** (new): skill/stat icon maps, level row tri-state, source row merge with registry stacks. **Wiring:** one-line or small hooks in `FabricSkillsTemplateSupport`, `FabricStatsTemplateSupport`, `FabricStatInfoTemplateSupport`, `FabricLevelProgressionTemplateSupport`, `FabricSourcesTemplateSupport`. |
| **Versioning** | `gradle.properties` `projectVersion=2.3.141`; `Changelog.md` entry for this release. |

**No** changes to progression math, sort semantics, paging sizes, or action contract behavior beyond displaying stacks differently.

## Files touched (this sprint)

- `fabric/src/main/resources/assets/auraskills/granite/templates/` — `skills.yml`, `stats.yml`, `level_progression.yml`, `stat_info.yml`, `sources.yml`, `abilities.yml`, `leaderboard.yml`, `unclaimed_items.yml`
- `fabric/src/main/java/dev/aurelium/auraskills/fabric/menu/FabricMenuPresentation.java` (new)
- `FabricSkillsTemplateSupport.java`, `FabricStatsTemplateSupport.java`, `FabricStatInfoTemplateSupport.java`, `FabricLevelProgressionTemplateSupport.java`, `FabricSourcesTemplateSupport.java`
- `gradle.properties`, `Changelog.md`
- `fabric/SPRINT_112_MENU_VISUAL_REDESIGN.md` (this file)

## Build / tests

Command:

```bat
.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle
```

**Result:** `BUILD SUCCESSFUL`; `verifyFabricReleaseBundle: OK (15 nested jars in FabricAuraSkills-2.3.141.jar)`.

## Known remaining visual limitations

- **Legacy formatting codes** in YAML (section-sign color/style prefixes): fine on **Java Edition** clients; **Bedrock / Geyser** may render differently (out of scope this sprint).
- **Custom** skills/stats not in the small ID maps keep the template default icon only until maps or data-driven icons exist.
- **Abilities** slot layout remains fixed by template/validator; mana slot index unchanged.
- **Leaderboard** avoids player skulls (policy); metaphoric items only.

## Screenshots

**Yes — re-capture** all eight menus on **2.3.141** for a second-pass visual critique and comparison to the earlier “prototype” shots.

## Recommended next sprint

- Deeper **Slate / placeholder** parity or optional **resource-pack** row icons (`custom_model_data`) for a more premium look without changing core template actions.
- **Bedrock** formatting pass when Geyser testing is officially in scope.

## Relation to `SPRINT_112_MENU_AESTHETIC_REVIEW.md`

That document was the **structure / checklist** pass; this file records the **visual redesign** implementation for release **2.3.141**.