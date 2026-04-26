# Sprint 84 — Admin / permissioned Java command retest (operator)

| Field | Value |
|--------|--------|
| **Sprint 84** | **Process** — distinguish **real bugs** from **permission-gated Brigadier** (“incorrect argument” / missing suggestions when the sender **does not** hold the nodes for that branch). |
| **Status** | **NRP (operator)** — in-repo agents cannot run in-game commands; fill **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`** (and **`SPRINT_78`** minimum rows if you touch them) after this pass. |
| **Artifact** | **`FabricAuraSkills-2.3.116.jar`** or newer (same baseline as Sprint **83** core validation). Record exact JAR in **Environment** on the **82** matrix. |
| **Depends on** | **Sprint 82** matrix — **partial / operator-validated**: core roots, menu smoke, action bar, mana self + **staff** `mana.other`, two-player menus/profiles, claimitems/unclaimed **Pass**; rows below were left **Pending** pending proper permission context. **Bedrock / Geyser:** **not in scope** for **84**. |

## Why this sprint exists

Sprint **82** first-pass results mixed **true failures** with **Brigadier hiding branches** for players without **`auraskills.command.*`** (or OP). Re-testing **as console, as OP, and as a permissioned non-OP player** shows whether the command tree and feedback are correct once the sender is allowed to see the branch.

## Test personas (use all three where applicable)

| Persona | Role |
|---------|------|
| **Server console / RCON** | Bypasses normal player permission checks where the implementation treats console as privileged; use for **admin** families. |
| **OP in-game** | Fast baseline: broad permission; confirms literals and parsing for privileged senders. |
| **Non-OP + explicit grants** | Use **LuckPerms** (or your provider) to grant only the relevant **`auraskills.command.…`** nodes — proves non-OP paths and messages. |

## Command checklist (only these in Sprint 84)

Substitute **`<player>`**, **`<skill>`**, **`<key>`** with valid disposable-test values. Record **Pass / Fail / Skip** per persona column; in **Notes**, say whether a **Fail** looks like a **real bug** vs **still missing permission**.

| Command | Console / RCON | OP in-game | Permissioned non-OP | Notes |
|---------|-----------------|------------|---------------------|--------|
| `/storage info` | | | | Expect storage permission or OP; map to `auraskills.command.storage` (or equivalent) when testing non-OP. |
| `/backup info` | | | | |
| `/backup list` | | | | |
| `/preset list` | | | | |
| `/backup create` | | | | **Disposable** server only; optional row. |
| `/backup load fake.yml confirm` (no pending backup) | | | | Expect **no** mutation and a clear error (no pending / invalid file). |
| `/item register <key>` | | | | |
| `/item give <player> <key>` | | | | |
| `/xp view <player> <skill>` | | | | Retest syntax + **xp**-related permissions; prior matrix row was **Pending retest**. |
| `/skilllevel view <player> <skill>` | | | | Verify plain-text output is **correct**, not only readable. |

## After this pass

1. Update **`fabric/SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`** — replace **Pending retest** with **Pass**, **Fail** (real bug + link/issue), or **Denied as expected** where appropriate.
2. If a **Fail** is confirmed with correct permissions, file a **code** follow-up (outside this doc).
3. **`SPRINT_78_JAVA_SMOKE_EXECUTION_RESULTS.md`** — sync any overlapping minimum rows.

## Related

- **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`** — canonical full matrix.
- **`COMMAND_PARITY_AUDIT.md`** — command surface / permission names.
- **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`** — Sprint **83** scope (not repeated here).
