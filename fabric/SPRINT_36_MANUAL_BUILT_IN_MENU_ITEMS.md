# Sprint 36 — manual validation (`granite.items` for built-in menus)

**Scope:** `granite.items.list`, `granite.items.main`, `granite.items.back` in each **`menus/*.yml`**. **Skills/Stats** use **`list`** for every row. **Level Progression** and **Stat Info** use **`main`** and **`back`**. **Runtime:** Fabric; **clients:** Java and Bedrock (Geyser + Floodgate) if available.

## Prereq

- Config folder contains (or will generate) **`menus/skills.yml`**, etc., with the **`items:`** block from the JAR defaults unless you are testing custom ids.

## Java

1. **Skills / Stats — list item:** With defaults, rows show **book**-type items. Set **`list: "minecraft:paper"`** in **`menus/skills.yml`** and **`/auraskills reload`** — Skills rows should show **paper**. Revert and reload to confirm.
2. **Level Progression — main + back:** From **Skills**, open a skill. Set **`main`** and **`back`** in **`menus/level_progression.yml`** (e.g. **`minecraft:emerald`** and **`minecraft:stick`**) and **reload** — main panel and back control use those items. **Back** should still return to **Skills**.

## Bedrock / Geyser

- Repeat the visible item checks. Chest slot icons must match the chosen materials (Bedrock may still differ in minor ways; log any new gap in the backlog).

## Invalid id fallback

1. **Bad YAML format:** e.g. **`list: "!!!not valid"`** in **`menus/stats.yml`** — expect a **warn** in the server log on **reload** and UI falls back to **book**-style list rows (or **emerald/arrow** where tested for detail menus after fixing YAML).
2. **Unknown but well-formed id:** e.g. a mod item id for a mod not loaded — at **open** time, expect a **warn** and fallback to the code default item.

## Reload

- After each YAML edit, **`/auraskills reload`**, then re-open the menu. Confirm no **crash** and item presentation matches the **last reloaded** config.

## Checklist

| Check | Java | Bedrock |
|-------|------|---------|
| Skills list item change | [ ] | [ ] |
| Stats list item change | [ ] | [ ] |
| Level Progression main/back | [ ] | [ ] |
| Stat Info main/back | [ ] | [ ] |
| Bad id → warn + fallback | [ ] | [ ] (optional) |
| After reload | [ ] | [ ] (optional) |
