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

**Narrative + tables:** `fabric/SPRINT_77_JAVA_COMMAND_MENU_SMOKE_RESULTS.md`

**Proven in the Sprint 77 operator sample (not exhaustive):** two **Java** players (**RedPaint_**, **Deskount**) both opened the **Skills** menu successfully after clean startup.

**Full matrix:** Complete the **Pass/Fail/Skip** tables in **`SPRINT_77_…md`** and/or the sections below; watch **console** for exceptions during each command.

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

Use **Pass** / **Fail** / **Skip** + **Notes** (include console lines if **Fail**).

### Core command roots (Java player)

| Check | Result | Notes |
|------|--------|--------|
| `/auraskills`, `/auraskills help` | | |
| `/skills`, `/sk`, `/skill` | | |
| `/stats` | | |
| `/openmenu skills`, `/openmenu stats` | | (Sprint 77 sample: Skills **Pass** for 2 players) |

### Menus and openmenu (Java player)

| Check | Result | Notes |
|------|--------|--------|
| `/skills`, `/stats` GUIs | | |
| Skill shortcuts, `/openmenu level_progression`, `/openmenu stat_info` | | |
| `/sources`, `/openmenu sources` | | |
| `/openmenu unclaimed_items`, leaderboards, abilities | | |

### Representative commands (Java player)

| Check | Result | Notes |
|------|--------|--------|
| `/mana`, `/xp`, `/skilllevel`, leaderboards, `/multiplier`, `/toggle`, `/abtoggle`, `/lang`, `/profile`, `/sources` | | |
| **Two-player** openmenu / profile / `/mana view` | | (players **RedPaint_** & **Deskount** available in Sprint 77 log) |

### Safe admin (console or op)

| Check | Result | Notes |
|------|--------|--------|
| `/storage info` | | |
| `/backup info`, `/backup list` | | |
| `/preset list` | | |
| **Confirmation flows** (preview/confirm) | | **Disposable** data only for destructive tests |

### Item / claim (disposable items)

| Check | Result | Notes |
|------|--------|--------|
| `/item register` / `give`, `/claimitems`, unclaimed menu | | |

---

## Pass / fail gate

- **Startup:** use **Sprint 77** / this doc **Startup validation** when current.
- **Feature acceptance:** this checklist is **in progress** until operators fill the tables or file known gaps in **`SPRINT_77`** / backlog.

**Definition of “done” for full Sprint 75 product acceptance:** all **targeted** rows **Pass** or **Skip (documented)**, and **no** unexplained **ERROR**/stack traces in console for covered commands. **Bedrock** remains a **separate** acceptance track.
