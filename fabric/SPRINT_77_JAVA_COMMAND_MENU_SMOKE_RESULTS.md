# Sprint 77 — Java dedicated-server command/menu smoke (process doc)

**Scope:** This sprint records **what was proven** on a **real** dedicated Fabric server with the **artifacts and environment below**. It does **not** claim every row in the **Sprint 75** matrix was executed in that session. **Bedrock / Geyser / Floodgate players** were **out of scope** for command/menu UI validation (Floodgate + Geyser **started**; no Bedrock command/menu pass).

**Sprint 76C vs this run:** The **`FabricWorldManager#loadWorlds`** mutable-set fix ( **`blockedCheckBlockReplaceWorlds = Sets.newConcurrentHashSet()`** ) is in **`FabricWorldManager.java`** in-repo and is part of the **`2.3.106`** code line. A clean boot with **`FabricAuraSkills-2.3.106.jar`** is **consistent** with that fix; no separate 76C “re-run” is required for documentation if source and JAR match.

---

## Server environment (operator-reported)

| Item | Value |
|------|--------|
| **Minecraft** | 1.21.11 |
| **Fabric Loader** | 0.18.4 |
| **Fabric API** | 0.141.3+1.21.11 |
| **Java** | 21 |
| **Cross-play mods** | Geyser + Floodgate (started successfully) |

## Exact JARs / mods (operator-reported)

| Mod / artifact | Version / note |
|----------------|----------------|
| **auraskills** (FabricAuraSkills) | **2.3.106** |
| **granitelib** (external, separate `mods/` JAR) | **2.3.104** (mod list line; resolves prior **0.1.0-alpha.1** deploy confusion) |
| **JiJ** | Operator used **external** GraniteLib; **not** “JiJ-only” for this test — intentional for version clarity. |

## Startup log reference (proven)

```text
Fabric AuraSkills enabled
Done (7.091s)! For help, type "help"
```

- No fatal **`FabricWorldManager.loadWorlds`** in this run.
- No return of the **Sprint 76A** / **76B** mixin fatals in the provided narrative.

**Attach the full `latest.log` from the smoke session to the issue/PR** when available (in-repo: not embedded here).

## Java test players (operator-reported)

| Player | Notes |
|--------|--------|
| **RedPaint_** | Joined; **Skills** menu opened successfully |
| **Deskount** | Joined; **Skills** menu opened successfully |

---

## Results summary (this session)

| Area | Result | Notes |
|------|--------|--------|
| **Dedicated startup** | **PASS** | Reached **Done**; **Fabric AuraSkills enabled** |
| **GraniteLib external** | **PASS (layout)** | **2.3.104** in mod list; old **0.1.0-alpha.1** issue **not** seen |
| **Java — Skills built-in menu** | **PASS** (sample) | **Both** players opened **Skills** menu |
| **Sprint 75 full command matrix** | **NRP** | Not all commands/menus in the checklist were reported run in this session |
| **Bedrock / Geyser command-menu UX** | **Not tested** | Explicitly **out of Sprint 77** scope |
| **Console during tests** | **NRP** | Operator should attach stack traces for any follow-up sprints |

---

## Command groups — pass/fail/skip (fill from tests)

*Default: rows not reported in the operator log are **Skip (not recorded)**. Replace with **Pass** / **Fail** / **Skip (reason)** as you run them.*

### Core roots + help (spec)

| Command | Result | Notes |
|---------|--------|--------|
| `/auraskills` | Skip (not recorded) | |
| `/auraskills help` | Skip (not recorded) | |
| `/skills` | **Pass** (indirect) | alias family active; both players used UI |
| `/sk` | Skip (not recorded) | |
| `/skill` | Skip (not recorded) | |
| `/stats` | Skip (not recorded) | |
| `/openmenu skills` | **Pass** (sample) | both players opened Skills |
| `/openmenu stats` | Skip (not recorded) | |

### Menus (spec)

| Step | Result | Notes |
|------|--------|--------|
| `/skills` → GUI | **Pass** (sample) | |
| `/stats` | Skip (not recorded) | |
| `/farming` … skill shortcuts | Skip (not recorded) | |
| `/openmenu level_progression farming` | Skip (not recorded) | |
| `/openmenu stat_info strength` | Skip (not recorded) | |
| `/sources farming` | Skip (not recorded) | |
| `/openmenu sources farming` | Skip (not recorded) | |
| `/openmenu unclaimed_items` | Skip (not recorded) | |
| `/openmenu leaderboard` … | Skip (not recorded) | |
| `/openmenu abilities` … | Skip (not recorded) | |

### Representative commands (spec)

| Command | Result | Notes |
|---------|--------|--------|
| `/mana`, `/xp view`, `/skilllevel view`, leaderboards, `/multiplier`, `/toggle`, `/abtoggle`, `/lang`, `/profile`, `/sources` | Skip (not recorded) | |
| **Two-player targets** (openmenu/profile/mana view) | Skip (not recorded) | |
| **Safe admin:** `/storage info`, `/backup info`, `/backup list`, `/preset list` | Skip (not recorded) | |
| **Confirmation:** `/backup load fake.yml confirm` | Skip (not recorded) | use **disposable** data only if run |
| **Item/claim** | Skip (not recorded) | **disposable items only** if run |

---

## Bugs found

| ID | Description | Severe? |
|----|-------------|--------|
| — | *None in the reported startup + Skills sample* | — |

## Fixes made (repo)

*None in Sprint 77 — documentation-only. **2.3.106** code already includes **76C** world manager fix; **2.3.107** records this process doc in **`projectVersion` / `Changelog`.*

## Known limitations

- **Full** command/menu + console-watch matrix is **not** complete in this file until operators fill the tables or link a filled **`SPRINT_75_MANUAL_JAVA_DEDICATED_ACCEPTANCE.md`**.
- **External GraniteLib 2.3.104** vs **auraskills 2.3.106**: acceptable for a **version skew** test; for release, prefer **matching** or documented **compatibility** policy.
- **Geyser/Floodgate** were running; **no** Bedrock command/menu pass this sprint.

## Next recommended step

1. **Sprint 78 (Java minimum smoke execution):** Run the **minimum set** on the disposable host and fill **`SPRINT_78_JAVA_SMOKE_EXECUTION_RESULTS.md`** and the **Sprint 78** tables in **`SPRINT_75_MANUAL_JAVA_DEDICATED_ACCEPTANCE.md`**. **In-repo Cursor** cannot execute in-game commands; operators own **Pass/Fail/Pending** replacement.
2. **Product:** When **Java** matrix is **green enough**, plan a **Bedrock** command-menu smoke (Geyser/Floodgate **players**) per **PAPER_PARITY_PROGRAM.md** §5 — **not** Sprint 78.
3. **No** further “startup-blocker” sprint for **`FabricWorldManager`** unless a **new** log with a **regression** appears against current source.
