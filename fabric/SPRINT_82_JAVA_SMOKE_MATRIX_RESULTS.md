# Sprint 82 — Java dedicated-server smoke matrix (execution results)

| Field | Value |
|--------|--------|
| **Sprint** | **82** — **runtime validation only** (no default feature work) |
| **Status** | **Partial (operator).** **Pass:** core/menu/action-bar rows, **`/auraskills`** usage root, **`/storage info`** as permissioned staff, XP/skilllevel **view** (readable lines, no raw keys), console/RCON admin roots (**`storage info`**, **`backup info`**, **`preset list`**, **`auraskills item`**). **Sprint 86 (docs):** clarified that **readable plain English** (including admin diagnostics) is **Pass**, not a failure; earlier smoke confusion was largely **LuckPerms** — tester **Deskount** now has **`auraskills.*`** globally. **Remaining:** final in-game permissioned-staff confirmation for bare **`/storage`**, **`/backup`**, **`/preset`**, prefixed **`item`**, backup list/create, item subcommands — see **Failures / Pending Retest**. **Bedrock/Geyser:** not tested. |
| **Minimum artifact** | **`FabricAuraSkills-2.3.114.jar`** (includes Sprints **79** text, **81–81C** action bar). Repo doc milestone **`2.3.115`**: matrix protocol. **Sprint 83 re-test target:** **`FabricAuraSkills-2.3.116.jar`** — run **at least** the Sprint 83 operator minimum (see **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`**), then continue the rest of this matrix. Always record the **exact** JAR name in **Environment**. |
| **Bedrock / Geyser** | **Explicitly not tested** this sprint; see [PARITY_BACKLOG.md](PARITY_BACKLOG.md) *Bedrock / Geyser validation follow-up*. |


**Related:** Canonical minimum set lives in **`SPRINT_78_JAVA_SMOKE_EXECUTION_RESULTS.md`**. **Sprint 80** = text re-verify on 2.3.109+. This doc (**82**) = **full matrix** re-execution on **2.3.114+** after **81C** closed the **Java** smart action bar lane.

**Sprint 83 (code):** **Accepted** in-repo (**2026-04-26**); **`FabricGraniteText`** + **`/mana view`** self vs **other** fix — see **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`**, artifact **`FabricAuraSkills-2.3.116.jar`**. Core smoke rows below reclassified after follow-up validation; matrix separates **Java pass** results from **permission/OP pending** retests (not “Brigadier = code bug” by default). **Known gap:** deeper **menu item** names/lore may still use **`Text.translatable`**; raw keys there are a **follow-up** pass (same helper pattern), not a Sprint 83 regression if newly observed.

**Sprint 84 (code):** **`SPRINT_84_MANUAL_COMMAND_SMOKE_PATCH.md`** — XP/skilllevel **`FabricGraniteText`**, item/xp root registration, admin **`requires`**; validate on **`2.3.117+`**.

**Sprint 85 (code):** **`SPRINT_85_MANUAL_COMMAND_TREE_PATCH.md`** — **`item`** / **`storage`** / **`backup`** / **`preset`** root executors + permission gates (**`2.3.118+`**).

**Sprint 86 (docs / matrix):** Permission context documented (**`auraskills.*`** for **Deskount**); matrix rows reclassified — readable plain text = **Pass**; **Pending** = not yet re-run in-game after permission fix, not assumed **Fail**.

### Matrix classification (Sprint 86)

| Runtime result | Matrix classification |
|----------------|------------------------|
| Readable plain English text | **Pass** |
| Menu opens with readable title | **Pass** |
| Raw `auraskills.granite.*` key | **Fail** — translation issue |
| Raw MiniMessage tags (`<green>`, …) | **Fail** — formatting issue |
| Unknown/incomplete command for permissioned staff | **Fail** — command/root issue |
| Hidden/denied for normal non-staff player | **Denied/hidden as expected** |

---

## Environment (fill when you run)

| Item | Your value |
|------|------------|
| **Test date** | (not re-confirmed in Sprint 86 doc pass) |
| **Minecraft** | (not re-confirmed in this pass — use server `version` or fill when you run) |
| **Fabric Loader** | (not re-confirmed in this pass) |
| **Fabric API** | (not re-confirmed in this pass) |
| **Java** | (not re-confirmed in this pass) |
| **FabricAuraSkills JAR** | **`FabricAuraSkills-2.3.118.jar`** (Sprint **85**; confirmed operator target) |
| **Tester permission context** | **Deskount:** **`auraskills.*`** = **true** globally via **LuckPerms** (`lp user … permission info` shows **`auraskills.*`** plus explicit **`auraskills.command.mana`** / **`mana.other`**). Use this account for permissioned-staff command rows. |
| **GraniteLib** | External JAR + version in `mods/` **or** JiJ-only (release bundle) — describe |
| **Server OS / host** | (optional) |
| **Java player name(s)** | e.g. Deskount (staff tester) |
| **Second Java player** (two-player tests) | or **Skip (single player)** |
| **Console / RCON** | **Y** — **`storage info`**, **`backup info`**, **`preset list`**, **`auraskills item`** confirmed from console |
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
| `/auraskills` | Pass | **Usage root:** readable usage text (starts with **Use: /auraskills** and lists mirror roots) is **expected**; this command does **not** open a menu. No raw `auraskills.granite.command.auraskills_usage` key. | No console issue recorded. |
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
| `/mana view <player>` (requires `auraskills.command.mana.other`) | Pass as staff/tester | Requires **`auraskills.command.mana.other`**; staff tester has it via **`auraskills.*`**. Do not grant to default group unless intentionally allowing players to inspect others’ mana. |
| `/auraskills xp view <player> <skill>` | Pass | **Granite path only** — **no** top-level **`/xp`** (vanilla owns **`/xp`**). Example readable line: **`Deskount — Agility: level 0, XP 32.2`**. **No** raw **`auraskills.granite.command.xp.view_line`**. |
| `/skills xp view <player> <skill>` | Pass | Same tree; same expectations as **`/auraskills xp view`**. |
| `/sk xp view <player> <skill>` | Pass | Same. |
| `/skill xp view <player> <skill>` | Pass | Same. |
| `/skilllevel view <player> <skill>` | Pass | Example: **`Deskount — Agility: level 0, XP in level 32.2`**. **No** raw **`auraskills.granite.command.skilllevel.view_line`**. Aliases: **`/auraskills skilllevel view …`**, **`/skills|/sk|/skill skilllevel view …`**. |
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
| `/mana view <other>` | Pass as staff/tester | Requires **`auraskills.command.mana.other`**; tester has it through **`auraskills.*`**. Keep **`mana.other`** staff-only for production defaults. |

**Or:** **Skip (single player)** — note here:

---

## Safe admin / data (read-first; disposable only for mutations)

| Command | Result | Notes |
|---------|--------|--------|
| `/storage` (bare root) | Pending final in-game retest as permissioned staff | **Sprint 85** added root usage executor. **Console/RCON** can run admin roots without LP. In-game: retest with **`auraskills.*`**; readable **usage** line = **Pass**. |
| `/storage info` | Pass as permissioned staff | Readable storage diagnostics in-game (e.g. loaded profiles count, **`sql.enabled`**, Sprint 63–64 explainer). **Plain English diagnostics are expected**, not a failure. **`auraskills.*`** granted for tester. Also **`/auraskills|/skills|/sk|/skill storage info`**. **Console/RCON:** **Pass** observed. |
| `/backup` (bare root) | Pending final in-game retest as permissioned staff | **Sprint 85** root usage. Retest in-game after **`auraskills.*`**. |
| `/backup info` | Pass (console/RCON); Pending final in-game confirmation as permissioned staff | **Console/RCON:** **Pass** observed. In-game: record separately when run. |
| `/backup list` | Pending final in-game retest as permissioned staff | **Disposable data** only for any destructive follow-ups. |
| `/preset` (bare root) | Pending final in-game retest as permissioned staff | **Sprint 85** root usage. |
| `/preset list` | Pass (console/RCON); Pending final in-game confirmation as permissioned staff | **Console/RCON:** **Pass** observed. In-game: record separately when run. |
| `/backup create` (optional, disposable) | Pending final in-game retest as permissioned staff | **Disposable data only.** |

**Do not** run **restore** / **preset load** on production. Disposable-only for destructive confirms.

---

## Confirmation helper (disposable; no real restore)

| Test | Result | Notes |
|------|--------|--------|
| `/backup load fake.yml confirm` (no pending) | Pending disposable-data / permissioned-staff retest | Retest as OP/console or with backup permissions. Expected: no mutation and clear no-pending/invalid-file style error. |
| If disposable backups: preview → **confirm** with **mismatch** | Skip | Requires disposable backup data; not run in this pass. |

---

## Item / unclaimed (disposable items only)

| Test | Result | Notes |
|------|--------|--------|
| `/auraskills item` (bare; usage / suggestions) | Pass (console/RCON); Pending final in-game retest as permissioned staff | **Console/RCON:** **Pass** observed for **`auraskills item`**. **In-game:** retest with **`auraskills.*`**; readable **usage** = **Pass**. **Top-level `/item`** is **vanilla** (shows **`modify`** / **`replace`**); **Granite** item is **only** under **`/auraskills|/skills|/sk|/skill item`**. **Sprint 85** fixed bare-root parse (**`item.usage_root`**). |
| `/skills item`, `/sk item`, `/skill item` | Pending final in-game retest as permissioned staff | Same subtree as **`/auraskills item`**; confirm after **`auraskills.*`**. |
| `/auraskills item register <key>` | Pending final in-game retest as permissioned staff | Do **not** mark **Fail** until retested with **`auraskills.*`**. Subtree includes **`modifier`**, **`trait`**, **`ignore`**, **`requirement`**, **`multiplier`**, **`register`**, **`unregister`**, **`give`**. |
| `/auraskills item give <player> <key>` | Pending final in-game retest as permissioned staff | Disposable item / safe target only. |
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
| Sprint 84 fixed raw keys for **`xp.view_line`** and **`skilllevel.view_line`** (`FabricGraniteText`); operator observed **readable** view lines (e.g. XP and “XP in level” variants). Granite **`xp`**/**`item`** only under prefixed roots on **2.3.117+**. | Y | See **`SPRINT_84_MANUAL_COMMAND_SMOKE_PATCH.md`**. |
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

**Console / RCON (Sprint 86):** **`storage info`**, **`backup info`**, **`preset list`**, and **`auraskills item`** roots were **confirmed working** from console/RCON. In-game permissioned-staff results for the same commands should still be recorded in the rows above when run.

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

**Summary:** Core/menu/action-bar/XP/skilllevel/**`/storage info`** rows validated for permissioned staff (**`auraskills.*`** on **Deskount**) and key console/RCON admin roots. **Pending** rows are **not** failures — they await final in-game confirmation after the permission setup clarification.

### Failures / Pending Retest

- **`/storage`** bare root — **Pending** final in-game retest as permissioned staff unless already confirmed (readable **usage** = **Pass**).
- **`/backup`**, **`/backup list`**, **`/backup create`** — **Pending** final in-game retest as permissioned staff; **`create`** is disposable-data only. **`/backup info`:** **Console/RCON Pass** observed; in-game staff confirmation still to record if not yet run.
- **`/preset`**, **`/preset list`** — **Pending** final in-game retest as permissioned staff unless already confirmed. **`preset list`:** **Console/RCON Pass** observed; in-game staff confirmation still to record if not yet run.
- **`/auraskills|/skills|/sk|/skill item`** — **`auraskills item`:** **Console/RCON Pass** observed; prefixed in-game **`item`** paths — **Pending** final retest as permissioned staff unless already confirmed.
- **`item register` / `item give`** — **Pending** disposable-data / permissioned-staff retest (do **not** mark **Fail** until retested with **`auraskills.*`**).
- Confirmation helper rows — **Pending** disposable-data / permissioned-staff retest.
- Deeper menu item names/lore — follow-up only if raw Granite keys are observed.
- **Bedrock/Geyser** player validation remains not tested.

**Bugs fixed during Sprint 82 (small scope only):**

No code changes during Sprint 82 matrix editing. Sprint **83** — core **`FabricGraniteText`** + **`/mana view`**. Sprint **84** — XP/skilllevel **view-line** translation + command syntax docs; Sprint **85** — root usage for **`item`**/**`storage`**/**`backup`**/**`preset`** + admin **`requires`** (see **`SPRINT_85_MANUAL_COMMAND_TREE_PATCH.md`**).

**Known issues left open:**

- Remaining admin/item rows need **final in-game permissioned-staff confirmation** after tester was granted **`auraskills.*`** globally (LuckPerms).
- Deeper Fabric menu item names/lore may still show raw translation keys if they use `Text.translatable`; fix with `FabricGraniteText` pattern when observed.
- Bedrock/Geyser player validation remains not tested.

**Recommended next (operator — completes Sprint 86 validation):**

Run the **Pending** rows in-game as **Deskount** (or equivalent staff account with **`auraskills.*`**) on **`FabricAuraSkills-2.3.118.jar`** (or newer): bare **`/storage`**, **`/backup`**, **`/preset`**, prefixed **`item`**, **`backup list`**, **`backup create`** (disposable), **`item register`/`give`** (disposable). Record **Pass** when output is readable plain text. **No code** unless a command still **fails** with correct permissions. Top-level **`/item`** and **`/xp`** remain **vanilla**; Granite **`item`** and **`xp`** are **prefixed only**.

**Screenshots / logs (optional):** (paths or PR)
