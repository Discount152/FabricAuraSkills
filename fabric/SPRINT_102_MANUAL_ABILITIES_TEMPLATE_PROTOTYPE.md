# Sprint 102 — Manual: template-backed `abilities` menu

| Field | Value |
|--------|--------|
| **Sprint** | **102** — **`abilities`** skill-scoped passive + mana display |
| **JAR** | **`FabricAuraSkills-2.3.130.jar`** (+ **`granitelib-fabric`**) |
| **Bedrock / Geyser** | **Deferred / not tested** |

## Purpose

After list menus (**Sprint 101**), **abilities** is the next bounded **skill-context** surface: multiple **passive** rows plus an optional **mana ability**, with **no** paging. This sprint adds a **template-first** path with the same **strict fallback** to **`FabricAbilitiesMenuGui` built-in**.

## Template path (classpath)

**`FabricAbilitiesTemplateSupport.TEMPLATE_RESOURCE_PATH`** → `assets/auraskills/granite/templates/abilities.yml`

## Placeholders supported

| Token | Meaning |
|--------|---------|
| `{skill}` | Skill id simple name |
| `{skill_display}` | Localized skill display name |
| `{ability}` | Passive ability id simple name |
| `{ability_display}` | Ability display name |
| `{ability_description}` | Processed base description (level-aware **`TextUtil.replace`** + truncation like built-in lore) |
| `{ability_unlock_level}` | Unlock sentence when locked (**`abilities_unlocks_at`**); when unlocked: **level** line or **maxed** string |
| `{ability_status}` | **Locked** / **Level: …** / **Maxed for this skill** (server **`FabricGraniteText`** literals) |
| `{mana_ability}` | Mana ability id simple name |
| `{mana_ability_display}` | Mana ability display name |
| `{mana_ability_description}` | Processed mana description (+ unlock line when locked, + maxed when maxed) |
| `{mana_ability_status}` | Same pattern as passive **`{ability_status}`** |

Unresolved `{...}` after expansion → **built-in** menu.

## Dynamic passive ability row behavior

- Template item: key **`ability`** or **`action: ability_row`**.
- **Exactly 14** **`slots`**, same order as **`FabricAbilitiesMenuGui.builtinAbilityContentSlots()`** (built-in content band).
- One stack per **enabled** passive ability, in skill order, up to capacity: **13** if a mana ability will be shown, **14** if not (matches built-in reservation rule).
- If **more** passives fit in built-in than the template allows → **fallback** (warn log).
- **Read-only** stacks (no gameplay activation change).

## Mana ability behavior

- Template item: key **`mana_ability`** or **`action: mana_ability_row`**; **exactly one** slot in bundled YAML (**31**).
- **Rendered** only when the skill has an **enabled**, **loaded** mana ability (same **`showMana`** rule as built-in).
- **If no mana ability:** no mana stack (slot shows **fill** if configured).
- **Built-in difference:** built-in places mana in the **next** slot immediately after passives; the template uses a **fixed** YAML slot. The bundled layout uses **31** so it does **not** overlap the **14** passive slots; operators editing YAML must keep **mana** slot **disjoint** from passive slots in use.

## Back behavior

- **`back`** item: **`action: back`**, slot **45** (validated — matches **`FabricAbilitiesMenuGui.SLOT_BACK`**).
- Click → **`FabricAuraSkillsMenus.openLevelProgression(skill, …, returnToSkillsListPage)`** (same as built-in).

## Fallback behavior

Aborts to **`FabricAbilitiesMenuGui.openBuiltIn`** on: missing file, invalid YAML, wrong **`id`** / **`rows`**, missing / invalid **ability** grid, slot list ≠ built-in order, wrong passive count vs capacity, **mana** slot overlaps a passive slot, missing **mana_ability** / **back** items or wrong actions, back not on slot **45**, unresolved placeholders, unknown **material**, runtime failure. **Warn** log; **no** crash.

## Files changed (summary)

| Area | Files |
|------|--------|
| Template | **`abilities.yml`** |
| GraniteLib | **`GraniteTemplateActionNames`**, **`GraniteTemplateItems`** |
| Fabric | **`FabricAbilitiesTemplateSupport`**, **`FabricAbilitiesPlaceholders`**, **`FabricAbilitiesMenuGui`** |
| Tests | **`FabricAbilitiesBundledTemplateParseTest`**, granitelib tests, translatable scan |

## Tests run

```text
.\gradlew -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build
```

## Java validation checklist

1. **`/openmenu abilities`**, **`/openmenu abilities <skill>`** (enabled skills).
2. **Expected:** Menu opens; passives + mana (if any); **no** raw **`auraskills.granite.*`** on items; **no** raw **`{ability_display}`** etc.; **Back** → **level progression**.
3. **Negative (local):** break YAML → **built-in** menu; **do not** commit broken template.

## Known limitations

- **Not** disk override, **not** hot reload, **not** full action engine, **not** global placeholder registry.
- **Mana slot** is **fixed** in YAML vs **dynamic** next-slot in built-in.
- **Template** uses **`book`** / **`nether_star`** materials; built-in uses **dye** / **shard** / **star** by unlock state — visual parity is **not** identical.
- **Bedrock** not validated.

## Operator validation

**Pending** on **`FabricAuraSkills-2.3.130.jar`**.

## Recommended next sprint

**Leaderboard** template prototype or **Path A** infrastructure (disk YAML) — still **not** **`MenuFileManager`** complete.
