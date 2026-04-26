# Sprint 82 — Java dedicated-server smoke matrix (execution results)

| Field | Value |
|--------|--------|
| **Sprint** | **82** — **runtime validation only** (no default feature work) |
| **Status** | **Partial (operator) — classification accepted (2026-04-26).** **Pass:** core roots, menu smoke, action bar, mana self, mana **other** (**Pass as staff/tester**), two-player menus/profiles, claimitems/unclaimed. **Pending:** admin/data, confirmation helper, item register/give, **`/xp view`**, **`/skilllevel view`** — **not** treated as code failures until retested with proper context. **Next:** **`SPRINT_84_MANUAL_JAVA_ADMIN_PERMISSIONED_RETEST.md`**. **Bedrock/Geyser:** not tested. **Summary counts** pending Sprint **84** + final tally. |
| **Minimum artifact** | **`FabricAuraSkills-2.3.114.jar`** (includes Sprints **79** text, **81–81C** action bar). Repo doc milestone **`2.3.115`**: matrix protocol. **Sprint 83 re-test target:** **`FabricAuraSkills-2.3.116.jar`** — run **at least** the Sprint 83 operator minimum (see **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`**), then continue the rest of this matrix. Always record the **exact** JAR name in **Environment**. |
| **Bedrock / Geyser** | **Explicitly not tested** this sprint; see [PARITY_BACKLOG.md](PARITY_BACKLOG.md) *Bedrock / Geyser validation follow-up*. |


**Related:** Canonical minimum set lives in **`SPRINT_78_JAVA_SMOKE_EXECUTION_RESULTS.md`**. **Sprint 80** = text re-verify on 2.3.109+. This doc (**82**) = **full matrix** re-execution on **2.3.114+** after **81C** closed the **Java** smart action bar lane.

**Sprint 83 (code):** **Accepted** in-repo (**2026-04-26**); **`FabricGraniteText`** + **`/mana view`** self vs **other** fix — see **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`**, artifact **`FabricAuraSkills-2.3.116.jar`**. Core smoke rows below reclassified after follow-up validation; matrix separates **Java pass** results from **permission/OP pending** retests (not “Brigadier = code bug” by default). **Known gap:** deeper **menu item** names/lore may still use **`Text.translatable`**; raw keys there are a **follow-up** pass (same helper pattern), not a Sprint 83 regression if newly observed.

**Sprint 84 (process):** **`SPRINT_84_MANUAL_JAVA_ADMIN_PERMISSIONED_RETEST.md`** — admin/permissioned command retest (console/RCON, OP, permissioned non-OP) for all **Pending** rows in the summary below.

---

## Environment (fill when you run)

| Item | Your value |
|------|------------|
| **Test date** | |
| **Minecraft** | e.g. 1.21.11 (confirm from server) |
| **Fabric Loader** | e.g. 0.18.4 (confirm from server) |
| **Fabric API** | e.g. 0.141.3+1.21.11 (confirm from server) |
| **Java** | e.g. 21 (confirm from host) |
| **FabricAuraSkills JAR** | `FabricAuraSkills-2.3.116.jar` (or newer tested artifact) |
| **GraniteLib** | External JAR + version in `mods/` **or** JiJ-only (release bundle) — describe |
| **Server OS / host** | (optional) |
| **Java player name(s)** | |
| **Second Java player** (two-player tests) | or **Skip (single player)** |
| **Console / RCON** | Y / N |
| **Full restart** before pass | Y / N (recommended after JAR change) |

---

## Startup validation

| Check | Result (Pass / Fail / Skip) | Notes / `latest.log` |
|--------|-----------------------------|------------------------|
| Mod / log: **Fabric AuraSkills** enabled | Pass | |
| `Done (...)! For help, type "help"` (or equivalent) | Pass | |
| No fatal **AuraSkills** startup | Pass | |
| No **anvil** mixin crash (76A) | Pass | |
| No **TreeFeature** mixin crash (76B) | Pass | |
| No **FabricWorldManager** immutable set (76C) | Pass | |
| No missing text / Adventure classes | Pass | |

---

## Action bar product model (reference — 81C Java Pass)

| Mode | Expected | Result |
|------|----------|--------|
| `off` | Granite H/M **hidden** on idle action bar | Pass |
| `always` | Persistent idle H/M line when enabled | Pass |
| `smart` | Temporary windows only; no sticky “always” after triggers | Pass |

**After** any `/abtoggle` tests, return to **`/abtoggle smart`** unless intentionally leaving another mode.

---

## Core roots (Java player)

| Command / action | Result | Player-facing | Console |
|-------------------|--------|---------------|--------|
| `/auraskills` | Pass | Readable usage text shown; no raw `auraskills.granite.command.auraskills_usage` key. | No console issue recorded. |
| `/auraskills help` | Pass | Readable help text shown; no raw `auraskills.granite.command.help` key. | No console issue recorded. |
| `/skills` | Pass | Skills menu opens; title is readable as **Skills**; opened feedback is readable. | No console issue recorded. |
| `/sk` | Pass | Skills menu opens through alias; title is readable as **Skills**; opened feedback is readable. | No console issue recorded. |
| `/skill` | Pass | Skills menu opens through alias; title is readable as **Skills**; opened feedback is readable. | No console issue recorded. |
| `/stats` | Pass | Stats menu opens; title is readable as **Stats**; opened feedback is readable. | No console issue recorded. |
| `/openmenu skills` | Pass | Skills menu opens; title and opened feedback are readable. | No console issue recorded. |
| `/openmenu stats` | Pass | Stats menu opens; title and opened feedback are readable. | No console issue recorded. |

**Expect:** commands exist, help readable, menus open, **no** console errors, **no** item steal on GUI.

---

## Menu smoke (Java player)

Substitute **`<skill>`** / **`<stat>`** with IDs **enabled** on your `config` (e.g. `farming`, `strength`).

| Action | Result | Notes |
|--------|--------|--------|
| `/skills` | Pass | Menu opens. Deeper item names/lore were not fully validated for raw translation keys unless specifically checked. |
| `/stats` | Pass | Menu opens. Deeper item names/lore were not fully validated for raw translation keys unless specifically checked. |
| `/farming` (or enabled shortcut) | Pass | Menu opens. Deeper item names/lore were not fully validated for raw translation keys unless specifically checked. |
| `/mining` (or second shortcut) | Pass | Menu opens. Deeper item names/lore were not fully validated for raw translation keys unless specifically checked. |
| `/openmenu level_progression <skill>` | Pass | Menu opens. Deeper item names/lore were not fully validated for raw translation keys unless specifically checked. |
| `/openmenu stat_info <stat>` | Pass | Menu opens. Deeper item names/lore were not fully validated for raw translation keys unless specifically checked. |
| `/sources <skill>` | Pass | Menu opens. Deeper item names/lore were not fully validated for raw translation keys unless specifically checked. |
| `/openmenu sources <skill>` | Pass | Menu opens. Deeper item names/lore were not fully validated for raw translation keys unless specifically checked. |
| `/openmenu unclaimed_items` | Pass | Menu opens. Deeper item names/lore were not fully validated for raw translation keys unless specifically checked. |
| `/openmenu leaderboard` | Pass | Menu opens. Deeper item names/lore were not fully validated for raw translation keys unless specifically checked. |
| `/openmenu leaderboard average` | Pass | Menu opens. Deeper item names/lore were not fully validated for raw translation keys unless specifically checked. |
| `/openmenu leaderboard <skill>` | Pass | Menu opens. Deeper item names/lore were not fully validated for raw translation keys unless specifically checked. |
| `/openmenu abilities` | Pass | Menu opens. Deeper item names/lore were not fully validated for raw translation keys unless specifically checked. |
| `/openmenu abilities <skill>` | Pass | Menu opens. Deeper item names/lore were not fully validated for raw translation keys unless specifically checked. |

**Expect:** open or clear empty/unsupported; clicks **no** throw; prev/next/back where present; read-only no steal; chat fallback if documented.

---

## Command smoke (representative)

| Command | Result | Notes |
|---------|--------|--------|
| `/mana` | Pass | Works after granting `auraskills.command.mana` to default group. |
| `/mana view` (self; same as `/mana` for players) | Pass | Self mana view works after granting `auraskills.command.mana` to default group. |
| `/mana view <player>` (requires `auraskills.command.mana.other`) | Pass as staff/tester | Requires `auraskills.command.mana.other`; do not grant to default group unless intentionally allowing players to inspect others’ mana. |
| `/xp view <player> <skill>` | Pending retest | Retest with OP/staff permissions or verify required permission/syntax. Current failure may be permission-gated Brigadier behavior. |
| `/skilllevel view <player> <skill>` | Pending retest | Output appeared as plain text; verify expected value and permissions. |
| `/top` | Pass | |
| `/top average` | Pass | |
| `/skilltop <skill>` | Pass | |
| `/rank` | Pass | |
| `/skillrank <skill>` | Pass | |
| `/multiplier` | Pass | |
| `/toggle` (cycle if used) or `/abtoggle` | Pass | |
| `/abtoggle status` | Pass | |
| `/abtoggle smart` | Pass | |
| `/abtoggle always` | Pass | |
| `/abtoggle off` | Pass | |
| `/abtoggle smart` (restore) | Pass | |
| `/lang en_us` (or a loaded locale) | Pass | |
| `/profile skills <player>` | Pass | Output displays correctly. Granite UserState snapshot disclaimer appears; this is expected. |
| `/profile stats <player>` | Pass | Output displays correctly. Granite UserState snapshot disclaimer appears; this is expected. |
| `/sources <skill>` | Pass | Sources GUI opens. Deeper item formatting/lore may need separate follow-up if raw keys are observed. |

**Expect:** no crash; readable output; **no raw** MiniMessage tags (`<green>`, `<aqua>`, …); permissions fail clean; bad args clean.

---

## Two-player target tests (if two Java players online)

| Action | Result | Notes |
|--------|--------|--------|
| `/openmenu skills <other>` | Pass | |
| `/openmenu stats <other>` | Pass | |
| `/openmenu leaderboard <other>` | Pass | |
| `/openmenu leaderboard <other> <skill>` | Pass | |
| `/openmenu abilities <other> <skill>` | Pass | |
| `/profile skills <other>` | Pass | |
| `/profile stats <other>` | Pass | |
| `/mana view <other>` | Pass as staff/tester | Works after granting tester `auraskills.command.mana.other`; permission should remain staff/tester-only. |

**Or:** **Skip (single player)** — note here:

---

## Safe admin / data (read-first; disposable only for mutations)

| Command | Result | Notes |
|---------|--------|--------|
| `/storage info` | Pending retest | Retest as OP/console or with `auraskills.command.storage`; normal players may not see this branch. |
| `/backup info` | Pending retest | Retest as OP/console or with `auraskills.command.backup`; normal players may not see this branch. |
| `/backup list` | Pending retest | Retest as OP/console or with `auraskills.command.backup`; normal players may not see this branch. |
| `/preset list` | Pending retest | Retest as OP/console or with `auraskills.command.preset`; normal players may not see this branch. |
| `/backup create` (optional, disposable) | Pending retest | Disposable data only; retest as OP/console or with `auraskills.command.backup`. |

**Do not** run **restore** / **preset load** on production. Disposable-only for destructive confirms.

---

## Confirmation helper (disposable; no real restore)

| Test | Result | Notes |
|------|--------|--------|
| `/backup load fake.yml confirm` (no pending) | Pending retest | Retest as OP/console or with backup permissions. Expected: no mutation and clear no-pending/invalid-file style error. |
| If disposable backups: preview → **confirm** with **mismatch** | Skip | Requires disposable backup data; not run in this pass. |

---

## Item / unclaimed (disposable items only)

| Test | Result | Notes |
|------|--------|--------|
| `/item register <key>` | Pending retest | Likely staff/admin permission-gated; retest as OP/console or with item command permission. |
| `/item give <player> <key>` | Pending retest | Likely staff/admin permission-gated; retest as OP/console or with item command permission. |
| `/claimitems` | Pass | |
| `/claimitems all` | Pass | |
| `/openmenu unclaimed_items` | Pass | |

---

## Per-skill shortcuts (as enabled in config)

| Root | Result | Notes |
|------|--------|--------|
| `/farming` | Pass | |
| `/mining` | Pass | |
| `/fishing` | Pass | |
| `/foraging` | Pass | |
| (optional: rest of enabled set) | Pass | If remainder of enabled set was exercised in this pass. |

---

## Text rendering (while testing)

Watch for **literal** `raw` tags: `<green>`, `<red>`, `<aqua>`, `<bold>`, `<reset>`, broken separators, unformatted level-up / action bar / title.

**Sprint 83 (2.3.116):** On a **vanilla** Java client, also watch for **literal** `auraskills.granite.*` **translation keys** in chat and **built-in** menu **titles** / **opened** lines — those paths should be **fixed**. If you still see raw keys, note whether they are **menu item titles/lore** (documented **follow-up**, not necessarily blockers for the Sprint 83 row pass).

| Observation | Y / N | Where |
|-------------|--------|--------|
| Sprint 83 fixed core raw `auraskills.granite.*` keys for `/auraskills`, `/auraskills help`, Skills/Stats menu titles, and opened feedback. | Y | Core root retest on FabricAuraSkills-2.3.116+. |
| Deeper menu item names/lore not fully validated. | N/A | Follow-up only if raw keys are observed inside item names/lore. |

---

## Action bar — smart / Litematica (81C)

| Step | Result | Notes |
|------|--------|--------|
| `/abtoggle smart` | Pass | |
| H/M only in **windows**; **idle** no spam | Pass | |
| Litematica (or other) text **when idle** | Pass | |
| `/abtoggle off` — no Granite H/M | Pass | |
| `/abtoggle always` — persistent idle line | Pass | |
| `/abtoggle smart` (restore) | Pass | |

---

## Console watch (aggregate)

Console watch not fully captured in this pass.

| Issue | Seen Y/N | Log snippet / line |
|-------|----------|---------------------|
| Exceptions | Not recorded | |
| Missing translation | Not recorded | |
| Menu click | Not recorded | |
| Permission | Not recorded | |
| Text adapter | Not recorded | |
| Action bar / storage / item / packet | Not recorded | |

---

## Summary

| Category | Pass | Fail | Skip |
|----------|------|------|------|
| Startup | | | |
| Core roots | | | |
| Menus | | | |
| Commands | | | |
| Two-player | | | |
| Admin (safe) | | | |
| Item / unclaimed | | | |
| Text / action bar | | | |

**Summary counts pending final OP/console retest** for admin and item command rows.

### Failures / Pending Retest

- `/xp view <player> <skill>` — Pending retest with correct permission/syntax.
- `/skilllevel view <player> <skill>` — Pending retest; verify plain-text output and permissions.
- `/storage info`, `/backup info`, `/backup list`, `/preset list`, `/backup create` — Pending OP/console or permissioned retest; likely permission-gated.
- `/item register <key>`, `/item give <player> <key>` — Pending OP/console or permissioned retest; likely permission-gated.
- Confirmation helper rows — Pending disposable-data/permissioned retest.

**Bugs fixed during Sprint 82 (small scope only):**

No code changes during Sprint 82 matrix editing. Sprint **83** previously fixed raw Granite translation keys for core surfaces and `/mana view` self permission-gating.

**Known issues left open:**

- Deeper Fabric menu item names/lore may still show raw translation keys if they use `Text.translatable`; fix with `FabricGraniteText` pattern when observed.
- Admin/data and item command rows still need OP/console or permissioned retest.
- Bedrock/Geyser player validation remains not tested.

**Recommended next sprint:**

Sprint **84** — **`SPRINT_84_MANUAL_JAVA_ADMIN_PERMISSIONED_RETEST.md`**: admin/permissioned command retest and **targeted fixes only where a real bug is confirmed** after console/RCON, OP, and permissioned non-OP runs. Scope: **`/storage info`**, **`/backup info`**, **`/backup list`**, **`/preset list`**, **`/backup create`**, **`/backup load fake.yml confirm`**, **`/item register`**, **`/item give`**, **`/xp view`**, **`/skilllevel view`**.

**Screenshots / logs (optional):** (paths or PR)
