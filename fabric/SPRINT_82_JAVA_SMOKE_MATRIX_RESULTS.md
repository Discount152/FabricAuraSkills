# Sprint 82 — Java dedicated-server smoke matrix (execution results)

| Field | Value |
|--------|--------|
| **Sprint** | **82** — **runtime validation only** (no default feature work) |
| **Status** | **Partial (operator).** **Pass:** core roots, menu smoke, action bar, mana, two-player menus/profiles, claimitems/unclaimed. **Sprint 84 (code `2.3.117`):** **`/xp view`** / **`/skilllevel view`** raw-key fix; Granite **`item`**/**`xp`** only under **`/auraskills`** and **`/skills|/sk|/skill`**; **`/storage`**, **`/backup`**, **`/preset`** console + Brigadier fixes — see **`SPRINT_84_MANUAL_COMMAND_SMOKE_PATCH.md`**. **Operator:** re-mark admin/item/xp rows on **`FabricAuraSkills-2.3.117.jar`**. **Bedrock/Geyser:** not tested. |
| **Minimum artifact** | **`FabricAuraSkills-2.3.114.jar`** (includes Sprints **79** text, **81–81C** action bar). Repo doc milestone **`2.3.115`**: matrix protocol. **Sprint 83 re-test target:** **`FabricAuraSkills-2.3.116.jar`** — run **at least** the Sprint 83 operator minimum (see **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`**), then continue the rest of this matrix. Always record the **exact** JAR name in **Environment**. |
| **Bedrock / Geyser** | **Explicitly not tested** this sprint; see [PARITY_BACKLOG.md](PARITY_BACKLOG.md) *Bedrock / Geyser validation follow-up*. |


**Related:** Canonical minimum set lives in **`SPRINT_78_JAVA_SMOKE_EXECUTION_RESULTS.md`**. **Sprint 80** = text re-verify on 2.3.109+. This doc (**82**) = **full matrix** re-execution on **2.3.114+** after **81C** closed the **Java** smart action bar lane.

**Sprint 83 (code):** **Accepted** in-repo (**2026-04-26**); **`FabricGraniteText`** + **`/mana view`** self vs **other** fix — see **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`**, artifact **`FabricAuraSkills-2.3.116.jar`**. Core smoke rows below reclassified after follow-up validation; matrix separates **Java pass** results from **permission/OP pending** retests (not “Brigadier = code bug” by default). **Known gap:** deeper **menu item** names/lore may still use **`Text.translatable`**; raw keys there are a **follow-up** pass (same helper pattern), not a Sprint 83 regression if newly observed.

**Sprint 84 (code):** **`SPRINT_84_MANUAL_COMMAND_SMOKE_PATCH.md`** — XP/skilllevel **`FabricGraniteText`**, item/xp root registration, admin **`requires`**; validate on **`2.3.117+`**.

---

## Environment (fill when you run)

| Item | Your value |
|------|------------|
| **Test date** | |
| **Minecraft** | e.g. 1.21.11 (confirm from server) |
| **Fabric Loader** | e.g. 0.18.4 (confirm from server) |
| **Fabric API** | e.g. 0.141.3+1.21.11 (confirm from server) |
| **Java** | e.g. 21 (confirm from host) |
| **FabricAuraSkills JAR** | `FabricAuraSkills-2.3.117.jar` (Sprint **84** command patch; or newer) |
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
| `/auraskills xp view <player> <skill>` | Pending retest | **Sprint 84:** use Granite path only — **no** top-level **`/xp`** (vanilla). Same tree: **`/skills|/sk|/skill xp view …`**. Expect readable line (**`FabricGraniteText`**). Perm: **`auraskills.command.xp.view`**. |
| `/skilllevel view <player> <skill>` | Pending retest | **Sprint 84:** **`FabricGraniteText`** for **`view_line`**. Aliases: **`/auraskills skilllevel view …`**, **`/skills|/sk|/skill skilllevel view …`**. |
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
| `/storage info` | Pending retest | **Sprint 84:** **`permissionOrNonPlayerSource`** (console without LP) + **`mergeskills`** literal **`requires`** — retest console + permissioned player. |
| `/backup info` | Pending retest | **Sprint 84:** console/RCON should pass **`requires`** without LP console meta. |
| `/backup list` | Pending retest | Same. |
| `/preset list` | Pending retest | Same. |
| `/backup create` (optional, disposable) | Pending retest | Same. |

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
| `/auraskills item register <key>` | Pending retest | **No** top-level **`/item`** (vanilla). Use **`/auraskills|/skills|/sk|/skill item register …`**; perms **`auraskills.command.item.register`** / **`item.give`**. |
| `/auraskills item give <player> <key>` | Pending retest | Same. |
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
| Sprint 84 fixed raw keys for **`xp.view_line`** and **`skilllevel.view_line`** (`FabricGraniteText`); Granite **`xp`**/**`item`** only under prefixed roots on **2.3.117+**. | Y | See **`SPRINT_84_MANUAL_COMMAND_SMOKE_PATCH.md`**. |
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

- **`/auraskills xp view`** (and alias roots) — Pending operator on **`2.3.117+`** after **`FabricGraniteText`** fix.
- **`/skilllevel view`** — Pending operator on **`2.3.117+`**.
- **`/storage`**, **`/backup`**, **`/preset`** safe rows — Pending after **`permissionOrNonPlayerSource`** + **`mergeskills`** **`requires`**.
- **`/auraskills item register|give`** — Pending; use prefixed paths only.
- Confirmation helper rows — Pending disposable-data/permissioned retest.

**Bugs fixed during Sprint 82 (small scope only):**

No code changes during Sprint 82 matrix editing. Sprint **83** — core **`FabricGraniteText`** + **`/mana view`**. Sprint **84** ( **`2.3.117`** ) — **`/xp`**, **`/skilllevel`**, **`/item`**/**`/xp`** roots, admin **`requires`** — see **`SPRINT_84_MANUAL_COMMAND_SMOKE_PATCH.md`**.

**Known issues left open:**

- Deeper Fabric menu item names/lore may still show raw translation keys if they use `Text.translatable`; fix with `FabricGraniteText` pattern when observed.
- Operator must re-fill matrix rows on **`2.3.117+`** after Sprint **84** code.
- Bedrock/Geyser player validation remains not tested.

**Recommended next sprint:**

Operator: complete **Pending** rows on **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`** with **`FabricAuraSkills-2.3.117.jar`**. Code follow-up only if operator confirms a **Fail** with correct syntax and permissions.

**Screenshots / logs (optional):** (paths or PR)
