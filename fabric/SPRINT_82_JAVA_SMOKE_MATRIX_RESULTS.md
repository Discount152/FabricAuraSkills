# Sprint 82 — Java dedicated-server smoke matrix (execution results)

| Field | Value |
|--------|--------|
| **Sprint** | **82** — **runtime validation only** (no default feature work) |
| **Status** | **Pass (operator) — Java matrix closed enough to move on.** Core/menu/action-bar, XP/skilllevel **view**, and **permissioned-staff** **`storage`** / **`backup`** / **`preset`** / prefixed **`item`** roots validated in-game with **`auraskills.*`** (LuckPerms). **Intentional gaps:** destructive backup/preset paths, **`item register`/`give`** on disposable data only; **menu lore** raw keys = **watch-list**. **Bedrock/Geyser:** **Sprint 87** — **`SPRINT_87_MANUAL_BEDROCK_GEYSER_VALIDATION.md`** (**`2.3.118+`**); outcome recorded in **`PARITY_BACKLOG.md`** after operator run. |
| **Minimum artifact** | **`FabricAuraSkills-2.3.114.jar`** (includes Sprints **79** text, **81–81C** action bar). Repo doc milestone **`2.3.115`**: matrix protocol. **Sprint 83 re-test target:** **`FabricAuraSkills-2.3.116.jar`** — run **at least** the Sprint 83 operator minimum (see **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`**), then continue the rest of this matrix. Always record the **exact** JAR name in **Environment**. |
| **Bedrock / Geyser** | **Sprint 87** — see **`SPRINT_87_MANUAL_BEDROCK_GEYSER_VALIDATION.md`** and [PARITY_BACKLOG.md](PARITY_BACKLOG.md) *Bedrock / Geyser validation follow-up*. (This **82** doc is **Java** execution; Bedrock outcome recorded after operator runs **87**.) |


**Related:** Canonical minimum set lives in **`SPRINT_78_JAVA_SMOKE_EXECUTION_RESULTS.md`**. **Sprint 80** = text re-verify on 2.3.109+. This doc (**82**) = **full matrix** re-execution on **2.3.114+** after **81C** closed the **Java** smart action bar lane.

**Sprint 83 (code):** **Accepted** in-repo (**2026-04-26**); **`FabricGraniteText`** + **`/mana view`** self vs **other** fix — see **`SPRINT_83_MANUAL_JAVA_SMOKE_PATCH_TRANSLATIONS_MANA.md`**, artifact **`FabricAuraSkills-2.3.116.jar`**. Core smoke rows below reclassified after follow-up validation; matrix separates **Java pass** results from **permission/OP pending** retests (not “Brigadier = code bug” by default). **Known gap:** deeper **menu item** names/lore may still use **`Text.translatable`**; raw keys there are a **follow-up** pass (same helper pattern), not a Sprint 83 regression if newly observed.

**Sprint 84 (code):** **`SPRINT_84_MANUAL_COMMAND_SMOKE_PATCH.md`** — XP/skilllevel **`FabricGraniteText`**, item/xp root registration, admin **`requires`**; validate on **`2.3.117+`**.

**Sprint 85 (code):** **`SPRINT_85_MANUAL_COMMAND_TREE_PATCH.md`** — **`item`** / **`storage`** / **`backup`** / **`preset`** root executors + permission gates (**`2.3.118+`**).

**Sprint 86 (docs / matrix):** Permission context (**`auraskills.*`** globally via LuckPerms); readable plain text = **Pass**. **Final in-game staff sweep:** **`/storage`**, **`/storage info`**, **`/backup`**, **`/backup info`**, **`/backup list`**, **`/preset`**, **`/preset list`**, **`/auraskills|/skills|/sk|/skill item`** — all **Pass as permissioned staff** (see admin + item tables).

**Sprint 87 (runtime — Bedrock/Geyser):** **`SPRINT_87_MANUAL_BEDROCK_GEYSER_VALIDATION.md`** — operator validation on **Geyser + Floodgate**; minimum artifact **`FabricAuraSkills-2.3.118.jar`** (or newer). Java matrix rows here remain **Java-only** until **87** results are recorded in **`PARITY_BACKLOG.md`**.

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
| **Console / RCON** | **Y** — same admin/item roots confirmed earlier from console; **final sweep** also confirmed **in-game** as permissioned staff |
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
| `/storage` (bare root) | Pass as permissioned staff | Readable **usage** (**`storage.usage_root`**) or subcommand suggestions. **`auraskills.command.storage`** (or **`auraskills.*`**). Normal players without storage perms stay **denied/hidden**. |
| `/storage info` | Pass as permissioned staff | Readable storage diagnostics (loaded profiles, **`sql.enabled`**, Sprint 63–64 explainer). **Plain English = Pass.** Also under **`/auraskills|/skills|/sk|/skill storage info`**. |
| `/backup` (bare root) | Pass as permissioned staff | Readable **usage**; **`backup`** or **`backup.load`** perm family. Normal players without backup perms stay **denied/hidden**. |
| `/backup info` | Pass as permissioned staff | Readable backup help/diagnostics. |
| `/backup list` | Pass as permissioned staff | Readable list output. |
| `/preset` (bare root) | Pass as permissioned staff | Readable **usage**. **`auraskills.command.preset`**. Normal players stay **denied/hidden**. |
| `/preset list` | Pass as permissioned staff | Readable preset list output. |
| `/backup create` (optional, disposable) | Skip (not run this pass) | **Disposable data only** if executed; not required to close the Java smoke matrix. Run on a throwaway server/world if needed. |

**Do not** run **restore** / **preset load** on production. Staff **`mergeskills`** / **`removeallmodifiers`** **confirm** steps remain **console/RCON-only** by design — not part of this matrix closeout.

**Normal players** should remain **denied** or **hidden** for **`storage`**, **`backup`**, **`preset`**, and **`item`** admin trees unless intentionally granted those nodes.

---

## Confirmation helper (disposable; no real restore)

| Test | Result | Notes |
|------|--------|--------|
| `/backup load fake.yml confirm` (no pending) | Skip (not run this pass) | When tested on disposable data: expect no mutation and a clear no-pending/invalid-file style error. |
| If disposable backups: preview → **confirm** with **mismatch** | Skip | Requires disposable backup data; not run in this pass. |

---

## Item / unclaimed (disposable items only)

| Test | Result | Notes |
|------|--------|--------|
| `/auraskills item` (bare; usage / suggestions) | Pass as permissioned staff | Readable **usage** (**`item.usage_root`**) / subtree. **Top-level `/item`** is **vanilla**; **Granite** item is only **`/auraskills|/skills|/sk|/skill item`**. Normal players without **`item.*`** perms stay **denied/hidden**. |
| `/skills item` | Pass as permissioned staff | Same subtree as **`/auraskills item`**. |
| `/sk item` | Pass as permissioned staff | Same. |
| `/skill item` | Pass as permissioned staff | Same. |
| `/auraskills item register <key>` | Skip (not run this pass) | Subtree: **`modifier`**, **`trait`**, **`ignore`**, **`requirement`**, **`multiplier`**, **`register`**, **`unregister`**, **`give`**. Run on **disposable** items/keys when validating. |
| `/auraskills item give <player> <key>` | Skip (not run this pass) | Disposable item / safe target only when tested. |
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

**Console / RCON:** **`storage info`**, **`backup info`**, **`preset list`**, **`auraskills item`** were confirmed from console earlier. **Final in-game staff sweep** (Sprint 86 closeout): same family of roots + **`/storage`**, **`/backup`**, **`/preset`** bare roots, **`/backup list`**, prefixed **`item`** on all alias roots — **Pass as permissioned staff** (see tables).

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
| Startup | Pass | | |
| Core roots | Pass | | |
| Menus | Pass | | |
| Commands (incl. staff roots) | Pass | | |
| Two-player | Pass | | |
| Admin (safe read paths) | Pass | | |
| Item roots (prefixed) | Pass | | |
| Text / action bar | Pass | | |
| Destructive / disposable-only | | | Skip unless run on disposable data |

**Matrix closeout narrative (Sprint 86 final in-game staff sweep):**

- **Java** core / menu / action-bar / representative commands / **permissioned-staff** **`storage`**, **`backup`**, **`preset`**, and prefixed **`item`** roots: **Pass** (tester had **`auraskills.*`** globally via LuckPerms).
- **Destructive** paths (**`backup load`/`restore`**, **`preset load`**, **`backup create`**, **`item register`/`give`**): **Skip** in this matrix unless explicitly run on **disposable** data — not required to consider the Java smoke matrix “closed enough.”
- **Bedrock / Geyser:** **Not tested** — see **Recommended next**.
- **Deeper menu item / lore** for raw **`auraskills.granite.*`**: **watch-list** only (follow-up sprint), not a matrix **Fail**.

### Intentional follow-ups (not matrix failures)

- **`/backup load` / `restore`**, **`/preset load`**, preview→**confirm** flows — exercise only on **disposable** worlds; confirmation-helper rows stay **Pending/Skip** until then.
- **`item register` / `give`** — **Skip** until disposable item/key test.
- Deeper **menu** item names/lore — optional **`FabricGraniteText`** sweep (**post–87** or separate doc sprint) if raw keys are seen.
- **Bedrock/Geyser** — **Sprint 87** manual (**`SPRINT_87_MANUAL_BEDROCK_GEYSER_VALIDATION.md`**); update **`PARITY_BACKLOG.md`** when complete.

**Bugs fixed during Sprint 82 (small scope only):**

No code changes during Sprint 82 matrix editing. Sprint **83** — core **`FabricGraniteText`** + **`/mana view`**. Sprint **84** — XP/skilllevel **view-line** translation + command syntax docs; Sprint **85** — root usage for **`item`**/**`storage`**/**`backup`**/**`preset`** + admin **`requires`** (see **`SPRINT_85_MANUAL_COMMAND_TREE_PATCH.md`**).

**Known issues left open:**

- **Normal players** must remain **denied/hidden** for staff admin commands unless granted explicit nodes — production defaults should not mirror the **`auraskills.*`** tester setup.
- Deeper Fabric **menu** item names/lore may still show raw translation keys if they use `Text.translatable`; **`FabricGraniteText`** pattern when observed (lane after **87** or separate sprint).
- **Bedrock/Geyser** — run **`SPRINT_87_MANUAL_BEDROCK_GEYSER_VALIDATION.md`**; this **82** file stays Java baseline.

**Recommended next sprint:**

- **Sprint 87 — Bedrock/Geyser validation pass** — **`SPRINT_87_MANUAL_BEDROCK_GEYSER_VALIDATION.md`** (artifact **`2.3.118+`**).
- **Later:** Menu item/lore **`FabricGraniteText`** sweep (watch-list from **82**), **after** or in parallel with **87** **only** if resourced — not the same lane as **87**.

Top-level **`/item`** and **`/xp`** remain **vanilla**; Granite **`item`** and **`xp`** are **prefixed only**.

**Screenshots / logs (optional):** (paths or PR)
