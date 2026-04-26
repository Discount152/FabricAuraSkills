# Sprint 75 — Java dedicated-server acceptance (commands + menus)

**Goal:** Run **Granite Skills** on a **real** Fabric **dedicated** server and check that the command and menu work from **Sprints 57–74** matches expectations. This doc is the **checklist**; fill **`Result` / `Notes`** when you execute it.

**Out of scope for the original Sprint 75 charter:** Bedrock / Geyser / Floodgate **player** command and menu pass (see [PAPER_PARITY_PROGRAM.md](PAPER_PARITY_PROGRAM.md) §5 for cross-platform product expectations; track Bedrock in separate sprints). **Geyser/Floodgate** may be **installed** on the same server for infra parity.

---

## Startup validation (Sprint 76A–C + operator proof)

**Status:** **PASS** (as of **Sprint 77** process documentation).

| Item | Value |
|------|--------|
| **FabricAuraSkills JAR** | **2.3.106** (includes **Sprint 76C** `FabricWorldManager` mutable-set fix in source) |
| **GraniteLib** (tested layout) | External **`granitelib-fabric` 2.3.104** in `mods/` (operator test); JiJ-only layout remains supported — see `fabric/SPRINT_77_JAVA_COMMAND_MENU_SMOKE_RESULTS.md` |
| **Minecraft** | 1.21.11 |
| **Fabric Loader** | 0.18.4 |
| **Fabric API** | 0.141.3+1.21.11 |
| **Java** | 21 |
| **Proof** | `Fabric AuraSkills enabled` and `Done (...)` in log; see **`SPRINT_77_JAVA_COMMAND_MENU_SMOKE_RESULTS.md`** |

**Sprint 76A / 76B / 76C** startup hotfixes are **superseded** for acceptance gating by the above **PASS** (dedicated **Done** on current artifacts). If startup **fails** again, capture **`latest.log`** and open a new blocker sprint; do not assume 76C is unused — the fix lives in **`FabricWorldManager#loadWorlds`** in-repo.

---

## Java command/menu smoke (Sprint 77)

**Narrative + early tables:** `fabric/SPRINT_77_JAVA_COMMAND_MENU_SMOKE_RESULTS.md`

**Proven in the Sprint 77 operator sample (not exhaustive):** two **Java** players (**RedPaint_**, **Deskount**) both opened the **Skills** menu successfully after clean startup; **`/skills`** / **`/openmenu skills`** — **Pass** (see Sprint 77).

## Sprint 78 — minimum Java smoke (canonical execution record)

**Primary doc:** `fabric/SPRINT_78_JAVA_SMOKE_EXECUTION_RESULTS.md` — **minimum** command/menu set, **two-player** rows, **known issues** (e.g. raw MiniMessage), **fixes** (if any). Replace **Pending (operator)** in that file with **Pass** / **Fail** / **Skip** when you run the disposable host. **Sprint 78 in-repo** cannot execute in-game; **this** checklist and **`SPRINT_78_…md`** should match after your session.

**Watch console** for every command: **ERROR**, mixin, menu click, **missing translation** key, **permission** denials, **raw MiniMessage** in chat/action bar ( **known issue** — record, do **not** treat as a Sprint 78 code fix unless approved).

## Sprint 80 — re-run on **2.3.109** + **Sprint 79** text proof

**Doc:** `fabric/SPRINT_80_JAVA_TEXT_SMOKE_RESULTS.md` — deploy **`FabricAuraSkills-2.3.109.jar`**, **full restart** recommended first (message cache + **Sprint 79** parsing), then verify **no raw** MiniMessage in **level-up chat**, **action bar**, and **title**; re-run the **Sprint 78** minimum set. **In-repo** cannot execute; operators fill **Pass**/**Fail**/**Skip**.

**After Sprint 79:** re-check the **Sprint 78** / **Sprint 75** tables for any row previously marked for **raw tags**; expect **Pass** for engine paths on **2.3.109+** unless a follow-up bug is found.

## Sprint 82 — full Java smoke matrix (dedicated server)

**Doc:** `fabric/SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md` — run the **full** command + menu + safe-admin + item matrix on **`FabricAuraSkills-2.3.114+`** (includes **81C** smart action bar validation baseline). **Runtime only**; no feature scope by default. **Bedrock** not in scope. When 82 is complete, this acceptance gate can cite the **82** summary as the current **Java** proof.

## Sprint 83 — smoke patch (translation wire + `/mana view`)

**Doc:** `fabric/SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md` — deploy **`2.3.116+`** to fix **raw** `auraskills.granite.*` on **vanilla** clients and **`/mana view`** (self) without **`mana.other`**. Re-run affected **Sprint 78** / **82** rows. **Bedrock** not tested.

---

## CI / agent build evidence (in-repo, not a live server)

These commands are expected to pass from a clean tree (release discipline; **not** a substitute for the checklist):

| Command | Result |
|--------|--------|
| `.\gradlew -PfabricOnly=true :fabric:test` | See **`projectVersion`** in root **`gradle.properties`** at doc revision |
| `.\gradlew -PfabricOnly=true :fabric:build` | Produces `fabric/build/libs/FabricAuraSkills-<projectVersion>.jar` |
| `.\gradlew -PfabricOnly=true :granitelib-fabric:build` | Produces `slate-fabric/build/libs/granitelib-fabric-<version>.jar` (see Gradle **version** mapping) |

**Primary artifact (operators):** `fabric/build/libs/FabricAuraSkills-<projectVersion>.jar`. **`verifyFabricReleaseBundle`** confirms nested JIs when using the default bundle path.

**Note:** The operator test used **auraskills 2.3.106** and **external GraniteLib 2.3.104**; align versions to your support policy for production.

---

## Test server profile (fill in)

| Field | Value |
|------|--------|
| Host | |
| World | **Disposable** recommended |
| Permission backend | e.g. LuckPerms (optional) |

---

## Checklist (fill `Result` / `Notes`)

**Authoritative row-by-row table for the Sprint 78 minimum set:** `SPRINT_78_JAVA_SMOKE_EXECUTION_RESULTS.md` — when you run tests, **copy** the final **Result** / **Notes** back into the tables below, or keep a single source of truth in **78** only.

Use **Pass** / **Fail** / **Skip** + **Notes** (include console lines if **Fail**).

### Sprint 78 minimum set (Java player) — sync from `SPRINT_78…` when run

| Check | Result | Notes |
|------|--------|--------|
| `/auraskills`, `/auraskills help` | **Pending (operator)** | |
| `/skills` | **Pass** | Sprint 77; menu OK |
| `/sk`, `/skill` | **Pending (operator)** | |
| `/stats` | **Pending (operator)** | |
| `/openmenu skills` | **Pass** | Sprint 77; two players |
| `/openmenu stats` | **Pending (operator)** | |
| `/farming`, `/mining` | **Pending (operator)** | adjust id if not enabled |
| `/openmenu level_progression farming`, `/openmenu stat_info strength` | **Pending (operator)** | |
| `/sources farming`, `/openmenu sources farming` | **Pending (operator)** | |
| `/openmenu unclaimed_items` | **Pending (operator)** | |
| `/openmenu leaderboard` (+ average, farming) | **Pending (operator)** | |
| `/openmenu abilities` (+ farming) | **Pending (operator)** | |
| `/mana`, `/top`, `/top average`, `/skilltop farming`, `/rank`, `/skillrank farming` | **Pending (operator)** | |
| `/profile skills <player>`, `/profile stats <player>` | **Pending (operator)** | |
| `/storage info`, `/backup info`, `/backup list`, `/preset list` | **Pending (operator)** | |
| **Two-player:** `openmenu` skills/stats/leaderboard/abilities + `/mana view <other>` | **Pending (operator)** | e.g. RedPaint_ / Deskount |

### Extended / optional (if time)

| Check | Result | Notes |
|------|--------|--------|
| `/xp`, `/skilllevel`, `/multiplier`, `/toggle`, `/abtoggle`, `/lang` | | |
| **Confirmation** (backup/preset/storage) | | **Disposable** data only |
| **Item** / **claim** | | **Disposable** items only |

---

## Pass / fail gate

- **Startup:** use **Sprint 77** / this doc **Startup validation** when current.
- **Sprint 78 minimum smoke:** use **`SPRINT_78_JAVA_SMOKE_EXECUTION_RESULTS.md`**; **Sprint 78** is **closed** for Java minimum-set purposes when that file has no **Pending** in the **minimum** table (or every row is **Skip** with reason).
- **Feature acceptance (full 75):** this checklist is **in progress** until all **targeted** rows (including **extended**) **Pass** or **Skip (documented)**.
- **Known issue:** **raw MiniMessage** in chat / bars — see Sprint 78 doc; may be **Pass (with known issue)** for messaging — **separate** formatting sprint, not a blocker to **recording** smoke results.

**Definition of “done” for full Sprint 75 product acceptance:** all **targeted** rows **Pass** or **Skip (documented)**, and **no** unexplained **ERROR** / stack traces in console for covered commands (except **documented** known text-rendering). **Bedrock** remains a **separate** acceptance track.
