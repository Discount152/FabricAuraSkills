# Sprint 78 — Java dedicated-server minimum smoke (execution results)

**Purpose:** Record **pass / fail / skip** for the **minimum Java smoke set** from the disposable dedicated host, with **player-facing** outcome, **console** observations, and **known issues** (not fixed in Sprint 78 unless listed under **Fixes**).

**Baseline (Sprint 77, unchanged):** MC **1.21.11**, Fabric Loader **0.18.4**, Fabric API **0.141.3+1.21.11**, **Java 21**, **FabricAuraSkills 2.3.106**, **external GraniteLib 2.3.104**, Geyser + Floodgate present, two **Java** players, **both opened Skills** menu, startup **Done**.

**In-repo / Cursor limit:** A machine in the repo **cannot** join a world or run `/commands`. Rows below default to **Pending (operator)** until a human run replaces them with **Pass** / **Fail** / **Skip (reason)**. Filling this table **is** the Sprint 78 execution—commit when your session is complete.

**Bedrock / Geyser / Floodgate players:** **not tested** for command or menu behavior this sprint (infra may still be up).

**Doc revision:** `projectVersion` **2.3.108** (Sprint 78 protocol + this file).

**Sprint 80:** Re-execute this matrix on **`FabricAuraSkills-2.3.109.jar`** and record **Sprint 79** text fix proof — see **`SPRINT_80_JAVA_TEXT_SMOKE_RESULTS.md`**. **Full server restart** preferred before first run after upgrading from pre-79 JARs.

**Sprint 82:** Full **Java dedicated** smoke matrix execution on **`2.3.114+`** (post–**81C** action bar) — use **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`** as the **primary** results record; keep this file’s **minimum** rows in sync or reference the 82 summary when 82 is **Pass**-complete.

**Sprint 83:** **Code accepted** (**2026-04-26**); **raw** `auraskills.granite.*` on **vanilla** Java clients + **`/mana view`** Brigadier — fixed in **`FabricAuraSkills-2.3.116.jar`** — see **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`**. **Operator:** re-test the listed rows, then continue the minimum table / **Sprint 82** matrix.

---

## Artifacts to record when you run (copy into Notes)

| Field | Your value |
|------|------------|
| **FabricAuraSkills JAR** | e.g. 2.3.106 (or rebuilt **2.3.108** doc line) |
| **GraniteLib** | e.g. external 2.3.104 or JiJ policy |
| **Log snapshot** | Optional path to `latest.log` / PR |
| **Test date** | |

---

## Minimum smoke — single Java player (run in order you prefer)

| Command / action | Result | Player-facing | Console / other notes |
|-------------------|--------|---------------|------------------------|
| `/auraskills` | **Pending (operator)** | | |
| `/auraskills help` | **Pending (operator)** | | |
| `/skills` | **Pass** (Sprint 77) | menu works | 77 sample |
| `/sk` | **Pending (operator)** | | |
| `/skill` | **Pending (operator)** | | |
| `/stats` | **Pending (operator)** | | |
| `/openmenu skills` | **Pass** (Sprint 77) | | two players, 77 |
| `/openmenu stats` | **Pending (operator)** | | |
| `/farming` | **Pending (operator)** | | use **enabled** skill id from `config` if not `farming` |
| `/mining` | **Pending (operator)** | | |
| `/openmenu level_progression farming` | **Pending (operator)** | | substitute **`<skill>`** as needed |
| `/openmenu stat_info strength` | **Pending (operator)** | | substitute **`<stat>`** as needed |
| `/sources farming` | **Pending (operator)** | | |
| `/openmenu sources farming` | **Pending (operator)** | | |
| `/openmenu unclaimed_items` | **Pending (operator)** | | |
| `/openmenu leaderboard` | **Pending (operator)** | | |
| `/openmenu leaderboard average` | **Pending (operator)** | | |
| `/openmenu leaderboard farming` | **Pending (operator)** | | |
| `/openmenu abilities` | **Pending (operator)** | | |
| `/openmenu abilities farming` | **Pending (operator)** | | |
| `/mana` | **Pending (operator)** | | |
| `/mana view` | **Pending (operator)** | | self: same as `/mana` (**2.3.116+**); see **SPRINT_83** |
| `/auraskills xp view <player> <skill>` | **Pass (operator)** | | **Sprint 84+:** Granite path only (**no** top-level **`/xp`**); readable line — see **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`** |
| `/top` | **Pending (operator)** | | |
| `/top average` | **Pending (operator)** | | |
| `/skilltop farming` | **Pending (operator)** | | |
| `/rank` | **Pending (operator)** | | |
| `/skillrank farming` | **Pending (operator)** | | |
| `/profile skills <player>` | **Pending (operator)** | | self or other (online) per Brigadier rules |
| `/profile stats <player>` | **Pending (operator)** | | |
| `/storage info` | **Pending (operator)** | | **Sprint 85** (**2.3.118+**): bare **`/storage`** + prefixed roots — **`SPRINT_85_MANUAL_COMMAND_TREE_PATCH.md`** |
| `/backup info` | **Pending (operator)** | | Same |
| `/backup list` | **Pending (operator)** | | Same |
| `/preset list` | **Pending (operator)** | | Same |

### Two players online (if available, e.g. RedPaint_ / Deskount)

| Command / action | Result | Notes |
|-------------------|--------|--------|
| `/openmenu skills <otherPlayer>` | **Pending (operator)** | target receives screen where designed |
| `/openmenu stats <otherPlayer>` | **Pending (operator)** | |
| `/openmenu leaderboard <otherPlayer>` | **Pending (operator)** | |
| `/openmenu abilities <otherPlayer> farming` | **Pending (operator)** | |
| `/mana view <otherPlayer>` | **Pending (operator)** | |

---

## Summary (fill after run)

| Metric | Count |
|--------|------|
| **Pass** | |
| **Fail** | |
| **Skip** | |
| **Pending** | |

**Blocking failures for “Sprint 78 closed”:** none until operator replaces all **Pending** in the **minimum** set with a terminal state or documents **Skip** with reason.

---

## Known issues (record; **not fixed in Sprint 78** unless product approves a hotfix)

### 1) Raw MiniMessage in player-facing text (Java) — **addressed in Sprint 79 (code)**

**Observed (Sprint 78 / screenshots):** Literal tags such as **`<green>`**, **`<bold>`**, **`<red>20/20`**, **`<aqua>… Mana`**) in level-up chat and action bar / HUD.

**Sprint 79 fix (in-repo):** **`FabricMessageProvider`** now matches **Bukkit** — `stringToComponent` / `applyFormatting` / `componentToString` go through **`FabricTextFormatter`** (MiniMessage + legacy), not legacy-only. **`FabricUiProvider#sendActionBar`** and **`sendTitle`** use **`FabricTextAdapter.toNativeText(...)`** instead of **`Text.literal`**. See **`SPRINT_79_MANUAL_TEXT_RENDERING.md`**, **`projectVersion` 2.3.109**.

**Retest:** Re-run the minimum smoke on **2.3.109+** JARs and re-mark any row that was **Fail (raw tags)** as **Pass** or file a follow-up.

### 2) Raw `auraskills.granite.*` translation keys (vanilla client) — **addressed in Sprint 83 (code accepted 2026-04-26)**

**Observed (Sprint 82):** Chat and **built-in** menu titles/feedback showed **literal** keys such as **`auraskills.granite.command.auraskills_usage`**, **`…help`**, **`…menu.skills_title`**, **`…stats_title`**, **`…skills_opened`**, **`…stats_opened`**.

**Cause:** `Text.translatable` is resolved on the **client**; vanilla clients lack mod `lang` assets.

**Sprint 83 fix:** **`FabricGraniteText`** + server **literal** text for the hot paths; **`FabricAuraSkills-2.3.116.jar`**. See **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`**. **Follow-up:** deeper **menu item** lore may still show keys.

**Retest:** Minimum smoke on **`2.3.116+`** with a **vanilla** Java client (no client mod).

### 3) `/mana view` Brigadier failure (Sprint 82) — **addressed in Sprint 83 (code accepted 2026-04-26)**

**Cause:** **`MANA_OTHER`** on the entire **`view`** subcommand hid **`view`** from players who only had **`auraskills.command.mana`**. **`/mana view <player>`** still requires **`mana.other`**; **`/mana view`** with **no** target reuses **self** mana (same as **`/mana`**).

**Doc:** **SPRINT_83** manual; **COMMAND_PARITY_AUDIT** (Mana row).

### 4) Other

| ID | Description | Block release? |
|----|-------------|----------------|
| – | (none filed yet) | |

---

## Fixes applied in code (Sprint 78)

*None in this in-repo pass — no live failures captured here.*

If you fix a small bug discovered during your run, list commit / file and re-run `.\gradlew -PfabricOnly=true :fabric:test` (+ `:fabric:build` if `fabric` sources changed).

---

## Recommended next sprint

- **After 2.3.109:** Re-run **Sprint 78** minimum table on a disposable host to confirm no raw tags (or document remaining edge cases on **`FabricTextAdapter` Gson** fallback).
- **Bedrock / Geyser** command-menu smoke—**separate** sprint; **out of Sprint 78** scope.
