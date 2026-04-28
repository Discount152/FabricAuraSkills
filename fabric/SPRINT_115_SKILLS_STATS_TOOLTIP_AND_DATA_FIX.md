# Sprint 115 — Skills / Stats hub data + tooltip polish

**Branch:** `feature/granitelib-module-ids`  
**Jar:** `FabricAuraSkills-2.3.144.jar`  
**Scope:** Targeted fixes only — **no** layout/slot moves, **no** gameplay math or command behavior changes, **no** Bedrock/Geyser validation, **no** Slate/MenuFileManager completion claims.

## Issues fixed

### A — Stats hub Back item showed raw `&` color codes

**Cause:** `FabricGraniteTemplateStacks#toLiteralNoUnresolved` used `Text.literal(expanded)` after placeholder expansion, so YAML strings such as `&eBack to Skills` were not parsed as legacy/MiniMessage styles.

**Fix:** Build Adventure `Component` with `FabricTextFormatter.toComponent(expanded)`, then `FabricTextAdapter.toNativeText(..., plugin.getServer())` for `ItemStack` `ITEM_NAME` / `LORE` lines.

### B — Food stat icons still showed vanilla hunger / food mechanics

**Cause:** Hiding sections via `DataComponentTypes.TOOLTIP_DISPLAY` alone does not stop all client paths from deriving lines from default item components (e.g. golden apple / golden carrot).

**Fix:** `FabricMenuTooltipPresentation#suppressVanillaMechanicsTooltip` now **removes** `FOOD`, `CONSUMABLE`, `USE_REMAINDER`, `USE_COOLDOWN`, and `POTION_CONTENTS` from the stack (menu-only), then applies the existing `TOOLTIP_DISPLAY` hide map.

### C — Tool / weapon / armor hub icons still showed durability (incl. advanced tooltips)

**Cause:** Row icons are built with `new ItemStack(vanillaItem, count)` after the template row swap, inheriting default `MAX_DAMAGE` / `DAMAGE`; advanced tooltips can still show durability from those defaults.

**Fix:** Same suppression helper **removes** `MAX_DAMAGE` and `DAMAGE` from menu stacks before setting `TOOLTIP_DISPLAY`.

### D — Skills hub showed Level 0 · XP 0 despite saved data

**Cause:** `User` stores skill level/XP in `ConcurrentHashMap<Skill, …>`. `CustomSkill` (and other `Skill` impls) do not normalize `equals`/`hashCode`, so a **different** `Skill` instance for the same id could miss `get` and fall back to start level / 0 XP.

**Fix:** `User#getSkillLevel` and `#getSkillXp` try direct map lookup, then scan entries matching `getId().equals(skill.getId())`.

### Hub XP display (line 1 vs line 2)

- Line 1: **lifetime XP** = current in-level XP + sum of `getXpRequired(skill, l)` for `l` from current level down to `startLevel + 1` (matches `LevelManager#levelUpSkill` consumption of `getXpRequired(skill, newLevel)`).
- Line 2: **in this level** = `User#getSkillXp` (progress toward the next tier), unchanged in meaning from commands.

## Files touched (principal)

- `common/.../User.java`
- `fabric/.../FabricGraniteTemplateStacks.java`
- `fabric/.../FabricMenuPresentation.java`
- `fabric/.../FabricMenuTooltipPresentation.java`
- `fabric/.../FabricSkillsTemplateSupport.java`
- `fabric/src/test/.../FabricTextFormatterTest.java`
- `Changelog.md`, `gradle.properties`, parity/port markdown under `fabric/`

## Build / tests

```text
.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle
```

## Manual validation (operator)

Use **advanced tooltips** (F3+H) when checking durability suppression.

- `/skills`: each skill shows **real** level and XP; no false zeroes; no durability / attack lines on tool icons.
- `/stats`: **Back** uses colored text without visible `&`; Health / Regeneration icons show only Granite stat tooltip copy.

## Deferred / not claimed

- **Bedrock / Geyser** — not run-time tested this sprint.
- **Full Slate / MenuFileManager** — still **open** (Path A).