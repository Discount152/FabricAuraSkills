# Sprint 80 — Java dedicated smoke re-run (2.3.109) + Sprint 79 text verification

**Sprint 81 ( **2.3.111** JAR, code ):** The **health/mana** idle line uses **smart** / **always** / **off** and **`/abtoggle`**; see **`SPRINT_81_MANUAL_SMART_ACTION_BAR.md`**. **Sprint 81A (2.3.112+):** legacy **`idle`-only** migration + **`/abtoggle status|smart|always|off`** — **`SPRINT_81A_MANUAL_ACTION_BAR_LEGACY_MIGRATION.md`**. **Sprint 81B (2.3.113):** first **stable** `!wasInWindow` guard — **`SPRINT_81B_MANUAL_SMART_ACTION_BAR_LEVELUP_FIX.md`**. **Sprint 81C (2.3.114+):** **`SmartIdleObsDisplay`** — **`SPRINT_81C_MANUAL_SMART_ACTION_BAR_STUCK_FIX.md`** ( **runtime accepted** 2026-04-21; action bar **off / always / smart** lane closed for **Java** ). **Sprint 80** procedures below still apply to **Sprint 79** text checks on the **2.3.109** or newer artifact; for **Litematica** / **action bar** coexistence, use **`2.3.114+`** for smart-idle + trigger behavior. **Sprint 82:** Full matrix — **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`** (artifact **`2.3.114+`**, operator **NRP** until filled).

**Next:** broader **Java** smoke completion or a **Bedrock** validation sprint (not part of 81C).

**Purpose:** On a **disposable** dedicated Fabric **Java** server, deploy **`FabricAuraSkills-2.3.109.jar`**, then **re-run** the **minimum** command/menu matrix from **Sprint 78** and **visually** confirm the **Sprint 79** MiniMessage / text pipeline (level-up **chat**, **health/mana** action bar, **title** path).

**In-repo / Cursor limit:** This file is filled by **operator** test runs. **CI cannot** open Minecraft. Default row state: **Pending (operator)** until you replace with **Pass** / **Fail** / **Skip (reason)** and commit or attach a PR with **`latest.log`**.

**Bedrock / Geyser / Floodgate players:** **out of scope** (infra may be present; no Bedrock command/menu or text validation this sprint).

**Artifact (build):** `fabric/build/libs/FabricAuraSkills-2.3.109.jar` (built at **`projectVersion` 2.3.109**; this sprint’s **doc** revision is **`2.3.110`** in `gradle.properties` / `Changelog`).

**Nuance (version skew):** **`projectVersion` 2.3.110** in the repo is **docs-only** (Sprint 80 protocol commit). The **intended** runtime mod for this checklist is still **`FabricAuraSkills-2.3.109.jar`**, which contains the **Sprint 79** text fix. **Do not** require a **2.3.110** JAR for Sprint 80 unless you **deliberately** build a new artifact from a tree where **`projectVersion` is 2.3.110** and intend that as the test binary.

---

## Required environment (target)

| Item | Value |
|------|--------|
| **Minecraft** | 1.21.11 |
| **Fabric Loader** | 0.18.4 |
| **Fabric API** | 0.141.3+1.21.11 |
| **Java** | 21 |
| **auraskills (mod list)** | **2.3.109** |
| **GraniteLib** | **External** e.g. **2.3.104** (split install) **or** **JiJ-only** — document which you use |

---

## Deployment note (Sprint 79 + message cache)

**Prefer a full server restart** for the **first** validation pass after upgrading to **2.3.109**, so **`MessageProvider`** re-loads messages and the **component cache** reparses with the new **`stringToComponent`** ( **`FabricTextFormatter`** ).

A later pass may use **`/auraskills reload`** (or equivalent) **after** clean-boot proof; document if reload alone was used and whether any stale formatting appeared.

---

## Startup validation

| Check | Result | Notes / log line |
|------|--------|------------------|
| Mod list shows **auraskills 2.3.109** | **Pending (operator)** | |
| `Fabric AuraSkills enabled` | **Pending (operator)** | |
| `Done (...)! For help, type "help"` | **Pending (operator)** | |
| **Warnings** (non-fatal) | **Pending (operator)** | e.g. other mods, refmaps |

---

## Text rendering (Sprint 79) — **Java** player

### Level-up **chat**

| Check | Result | Notes / screenshot ref |
|------|--------|------------------------|
| Trigger a **skill level-up** (natural or **safe** admin/XP test) | **Pending (operator)** | |
| **No** raw `<green>`, `<dark_aqua>`, `<bold>`, `<gold>`, `<reset>`, strikethrough tag text | **Pending (operator)** | |
| Separators, colors, **reward** / **ability** lines **readable** | **Pending (operator)** | |

### Health / mana **action bar** (idle or configured idle bar)

| Check | Result | Notes |
|------|--------|--------|
| **No** raw `<red>`, `<aqua>` (or other bracket tags) in the bar | **Pending (operator)** | |
| HP and mana read as **styled**, not “recipe card” | **Pending (operator)** | |
| **No** new **`FabricTextAdapter` warn** lines while bar updates | **Pending (operator)** | |

### Title / subtitle (level-up or other trigger)

| Check | Result | Notes |
|------|--------|--------|
| Title appears as **overlay**, **not** as **chat** text | **Pending (operator)** | Sprint 79 uses S2C title packets |
| **No** raw MiniMessage in title/subtitle | **Pending (operator)** | |
| If not practical to trigger | **Skip** | *Reason:* |

---

## Minimum Java smoke set (Sprint 78 / 80 re-run)

Same commands as **`SPRINT_78_JAVA_SMOKE_EXECUTION_RESULTS.md`**. You may **sync** the two tables to one source of truth or duplicate results here and cross-link.

| Area | Result summary |
|------|------------------|
| **Core** `/auraskills`, `/auraskills help`, `/skills`, `/sk`, `/skill`, `/stats`, `/openmenu` … | **Pending (operator)** |
| **Menus** `openmenu` … farming, `stat_info`, `sources`, `unclaimed`, `leaderboard`, `abilities` | **Pending (operator)** |
| **Stats** `/mana`, `/top`, … `/profile`, **safe** `/storage` / `/backup` / `/preset` | **Pending (operator)** |
| **Two-player** (if two Java clients available) | **Pending (operator)** |

---

## Console watch

Record **any** of: stack traces, **missing lang** key, **raw** `<` **tags** in unexpected places, menu click **errors`, **permission** denials, **`FabricTextAdapter` warn**.

---

## Summary (fill when complete)

| Metric | Count |
|--------|------|
| **Pass** | |
| **Fail** | |
| **Skip** | |

**Known open issues (after your run):** *(none / list)*

---

## Next sprint (suggestion)

- If **Sprint 80** is **green** for text + minimum smoke: continue filling **Sprint 75** extended matrix or start **Bedrock** smoke when ready (**separate** sprint).
- If **raw tags** reappear: capture **`latest.log`**, **screenshot**, and open a follow-up (possible **`FabricTextAdapter`** JSON edge case or config string bypassing **MessageProvider**).
