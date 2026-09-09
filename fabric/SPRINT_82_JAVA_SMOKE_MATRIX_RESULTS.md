# Sprint 82 — Java dedicated-server smoke matrix (execution results)

| Field | Value |
|--------|--------|
| **Sprint** | **82** — **runtime validation only** (no default feature work) |
| **Status** | **NRP (operator)** — in-repo / CI **cannot** join a world or run commands. Fill tables below and commit, or link PR + `latest.log`. |
| **Minimum artifact** | **`FabricAuraSkills-2.3.114.jar`** (includes Sprints **79** text, **81–81C** action bar). Repo doc milestone **`2.3.115`**: matrix protocol. **Sprint 83 re-test target:** **`FabricAuraSkills-2.3.116.jar`** — run **at least** the Sprint 83 operator minimum (see **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`**), then continue the rest of this matrix. Always record the **exact** JAR name in **Environment**. |
| **Bedrock / Geyser** | **Explicitly not tested** this sprint; see [PARITY_BACKLOG.md](PARITY_BACKLOG.md) *Bedrock / Geyser validation follow-up*. |

**Related:** Canonical minimum set lives in **`SPRINT_78_JAVA_SMOKE_EXECUTION_RESULTS.md`**. **Sprint 80** = text re-verify on 2.3.109+. This doc (**82**) = **full matrix** re-execution on **2.3.114+** after **81C** closed the **Java** smart action bar lane.

**Sprint 83 (code):** **Accepted** in-repo (**2026-04-26**); **`FabricGraniteText`** + **`/mana view`** self vs **other** fix — see **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`**, artifact **`FabricAuraSkills-2.3.116.jar`**. **Operator:** re-test matrix rows first: **`/auraskills`**, **`/auraskills help`**, **`/skills`**, **`/stats`**, **`/mana`**, **`/mana view`**, **`/mana view <player>`** — then continue down the matrix. **Known gap:** deeper **menu item** names/lore may still use **`Text.translatable`**; raw keys there are a **follow-up** pass (same helper pattern), not a Sprint 83 regression if newly observed.

---

## Environment (fill when you run)

| Item | Your value |
|------|------------|
| **Test date** | |
| **Minecraft** | e.g. 1.21.11 |
| **Fabric Loader** | e.g. 0.18.4 |
| **Fabric API** | e.g. 0.141.3+1.21.11 |
| **Java** | e.g. 21 |
| **FabricAuraSkills JAR** | e.g. `FabricAuraSkills-2.3.114.jar` or current build |
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
| Mod / log: **Fabric AuraSkills** enabled | | |
| `Done (...)! For help, type "help"` (or equivalent) | | |
| No fatal **AuraSkills** startup | | |
| No **anvil** mixin crash (76A) | | |
| No **TreeFeature** mixin crash (76B) | | |
| No **FabricWorldManager** immutable set (76C) | | |
| No missing text / Adventure classes | | |

---

## Action bar product model (reference — 81C Java Pass)

| Mode | Expected |
|------|----------|
| `off` | Granite H/M **hidden** on idle action bar |
| `always` | Persistent idle H/M line when enabled |
| `smart` | Temporary windows only; no sticky “always” after triggers |

**After** any `/abtoggle` tests, return to **`/abtoggle smart`** unless intentionally leaving another mode.

---

## Core roots (Java player)

| Command / action | Result | Player-facing | Console |
|-------------------|--------|---------------|--------|
| `/auraskills` | | | |
| `/auraskills help` | | | |
| `/skills` | | | |
| `/sk` | | | |
| `/skill` | | | |
| `/stats` | | | |
| `/openmenu skills` | | | |
| `/openmenu stats` | | | |

**Expect:** commands exist, help readable, menus open, **no** console errors, **no** item steal on GUI.

---

## Menu smoke (Java player)

Substitute **`<skill>`** / **`<stat>`** with IDs **enabled** on your `config` (e.g. `farming`, `strength`).

| Action | Result | Notes |
|--------|--------|--------|
| `/skills` | | |
| `/stats` | | |
| `/farming` (or enabled shortcut) | | |
| `/mining` (or second shortcut) | | |
| `/openmenu level_progression <skill>` | | |
| `/openmenu stat_info <stat>` | | |
| `/sources <skill>` | | |
| `/openmenu sources <skill>` | | |
| `/openmenu unclaimed_items` | | |
| `/openmenu leaderboard` | | |
| `/openmenu leaderboard average` | | |
| `/openmenu leaderboard <skill>` | | |
| `/openmenu abilities` | | |
| `/openmenu abilities <skill>` | | |

**Expect:** open or clear empty/unsupported; clicks **no** throw; prev/next/back where present; read-only no steal; chat fallback if documented.

---

## Command smoke (representative)

| Command | Result | Notes |
|---------|--------|--------|
| `/mana` | | |
| `/mana view` (self; same as `/mana` for players) | | |
| `/mana view <player>` (requires `auraskills.command.mana.other`) | | |
| `/xp view <player> <skill>` | | |
| `/skilllevel view <player> <skill>` | | |
| `/top` | | |
| `/top average` | | |
| `/skilltop <skill>` | | |
| `/rank` | | |
| `/skillrank <skill>` | | |
| `/multiplier` | | |
| `/toggle` (cycle if used) or `/abtoggle` | | |
| `/abtoggle status` | | |
| `/abtoggle smart` | | |
| `/abtoggle always` | | |
| `/abtoggle off` | | |
| `/abtoggle smart` (restore) | | |
| `/lang en_us` (or a loaded locale) | | |
| `/profile skills <player>` | | |
| `/profile stats <player>` | | |
| `/sources <skill>` | | |

**Expect:** no crash; readable output; **no raw** MiniMessage tags (`<green>`, `<aqua>`, …); permissions fail clean; bad args clean.

---

## Two-player target tests (if two Java players online)

| Action | Result | Notes |
|--------|--------|--------|
| `/openmenu skills <other>` | | |
| `/openmenu stats <other>` | | |
| `/openmenu leaderboard <other>` | | |
| `/openmenu leaderboard <other> <skill>` | | |
| `/openmenu abilities <other> <skill>` | | |
| `/profile skills <other>` | | |
| `/profile stats <other>` | | |
| `/mana view <other>` | | |

**Or:** **Skip (single player)** — note here:

---

## Safe admin / data (read-first; disposable only for mutations)

| Command | Result | Notes |
|---------|--------|--------|
| `/storage info` | | |
| `/backup info` | | |
| `/backup list` | | |
| `/preset list` | | |
| `/backup create` (optional, disposable) | | |

**Do not** run **restore** / **preset load** on production. Disposable-only for destructive confirms.

---

## Confirmation helper (disposable; no real restore)

| Test | Result | Notes |
|------|--------|--------|
| `/backup load fake.yml confirm` (no pending) | | expect **no** mutation, clear error |
| If disposable backups: preview → **confirm** with **mismatch** | | no restore |

---

## Item / unclaimed (disposable items only)

| Test | Result | Notes |
|------|--------|--------|
| `/item register <key>` | | |
| `/item give <player> <key>` | | |
| `/claimitems` | | |
| `/claimitems all` | | |
| `/openmenu unclaimed_items` | | |

---

## Per-skill shortcuts (as enabled in config)

| Root | Result | Notes |
|------|--------|--------|
| `/farming` | | |
| `/mining` | | |
| `/fishing` | | |
| `/foraging` | | |
| (optional: rest of enabled set) | | |

---

## Text rendering (while testing)

Watch for **literal** `raw` tags: `<green>`, `<red>`, `<aqua>`, `<bold>`, `<reset>`, broken separators, unformatted level-up / action bar / title.

**Sprint 83 (2.3.116):** On a **vanilla** Java client, also watch for **literal** `auraskills.granite.*` **translation keys** in chat and **built-in** menu **titles** / **opened** lines — those paths should be **fixed**. If you still see raw keys, note whether they are **menu item titles/lore** (documented **follow-up**, not necessarily blockers for the Sprint 83 row pass).

| Observation | Y / N | Where |
|-------------|--------|--------|

---

## Action bar — smart / Litematica (81C)

| Step | Result | Notes |
|------|--------|--------|
| `/abtoggle smart` | | |
| H/M only in **windows**; **idle** no spam | | |
| Litematica (or other) text **when idle** | | |
| `/abtoggle off` — no Granite H/M | | |
| `/abtoggle always` — persistent idle line | | |
| `/abtoggle smart` (restore) | | |

---

## Console watch (aggregate)

| Issue | Seen Y/N | Log snippet / line |
|-------|----------|---------------------|
| Exceptions | | |
| Missing translation | | |
| Menu click | | |
| Permission | | |
| Text adapter | | |
| Action bar / storage / item / packet | | |

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

**Failures (document, link issues):**



**Bugs fixed during Sprint 82 (small scope only):**



**Known issues left open:**



**Recommended next sprint:**



**Screenshots / logs (optional):** (paths or PR)
