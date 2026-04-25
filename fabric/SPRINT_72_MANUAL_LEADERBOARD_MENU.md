# Sprint 72 — manual validation: Leaderboard menu (Fabric)

**Build:** `projectVersion` **2.3.100** (see `gradle.properties`).

**Scope:** one lane only — **leaderboard menu** + **`/openmenu leaderboard`**; **no** leaderboard calculation changes, **no** full Slate/`MenuFileManager`, **no** abilities menu.

## Bukkit reference (in-repo)

- **`bukkit/.../LeaderboardMenu.java`** — Slate builder: **`skill`** property, template **`leaderboard_player`** (ranks 1–10, player heads), **back** toward **`level_progression`** in reference flows.
- **`SkillsRootCommand#onTop`** / skill top — **paged chat**, not this inventory menu.
- **No `leaderboard.yml`** in this repository (menu file not inventoried as present).

## Fabric implementation

- **`FabricLeaderboardMenuGui`** — `GENERIC_9X6`-style 6×9 menu (Granite **`openClickable9x6`**): **10** entries per page in slots **0–4** and **9–13** (paper stacks: rank, name, value). Modes: **power** (level), **average** (two-decimal average via **`NumberUtil.format2(sv.xp())`**), **skill** (level). Row 5: **Back** (45) → **Skills** menu; **prev** (48) / **next** (50) when multiple pages; **refresh** (49) reopens the same view (re-reads cached lists; **no** `updateLeaderboards` in click handler).
- **`FabricOpenMenuLeaderboard`** — **`/openmenu leaderboard`** subtree (same permission **`auraskills.command.openmenu`** as other built-ins).

## Command roots to exercise

- **`/openmenu leaderboard`**
- **`/auraskills openmenu leaderboard`**, **`/skills openmenu leaderboard`**, **`/sk openmenu leaderboard`**, **`/skill openmenu leaderboard`**
- **`/top`**, **`/skilltop`**, **`/rank`**, **`/skilltop <skill> 2`**, **`/top average 2`** — should still print **chat** (unchanged).

## Player behavior (Java dedicated server)

1. Ensure **leaderboard data** exists (wait for periodic sort or run **`/updateleaderboards`** as op).
2. **`/openmenu leaderboard`** — opens **power** page **1**; list shows ranks **1–10** (or fewer); names match chat **`/top`** for the same page.
3. **`/openmenu leaderboard 2`** — power board page **2** (if enough entries).
4. **`/openmenu leaderboard average`** and **`/openmenu leaderboard average 2`** — average board.
5. **`/openmenu leaderboard farming`** (or any **enabled** skill key) — skill board for **self** (in-game).
6. If another player is online: **`/openmenu leaderboard <PlayerName>`** — opens the **power** board **on that player’s** client (staff/view flow); executor gets **`opened_for`** if not self.
7. **`/openmenu leaderboard <PlayerName> farming`** — target’s **skill** board; optional trailing page.
8. **`/openmenu leaderboard <PlayerName> average`** / **`average 2`** — target’s **average** board.
9. **Back** returns to **Skills** list (not **level_progression** — differs from Bukkit menubar back).

## Console / RCON

- **Cannot** use self forms that require a player executor for skill board without target — use:
  - **`/openmenu leaderboard <onlinePlayer> average [page]`**
  - **`/openmenu leaderboard <onlinePlayer> <skill> [page]`**
- Verify error string if console runs an invalid self-only shape (**`openmenu.leaderboard_console`** / bad token).

## Skill vs average

- **Skill:** level column (integer), same family as **`/skilltop`**.
- **Average:** two-decimal “level” from **`SkillValue#xp()`** (same as **`/top average`** chat).

## Paging

- **10** entries per menu page, aligned with **`FabricCommandLeaderboardText.ENTRIES_PER_PAGE`** and chat.
- **Prev/Next** disabled on edges; title shows **`list_paging_subtitle`** when more than one page.

## Unresolved names

- Online → live name; else **user cache** if present; else **`?`** (same rules as **`FabricCommandLeaderboardText#resolvePlayerName`**).

## YAML

- **No** Fabric `menus/leaderboard.yml` load in this sprint — **built-in GUI only** → parity **Partial**.

## Java dedicated server — suggested checklist

- [ ] **`/openmenu leaderboard`** opens without error when data exists.
- [ ] Paging **prev/next** matches **`/top`** / **`/skilltop`** for same mode and page.
- [ ] **`/top`** still prints chat and does **not** auto-open GUI.
- [ ] **Console** can open **average** and **skill** boards for an **online** target.
- [ ] **Permission** `auraskills.command.openmenu` denied → no menu.

## Bedrock / Geyser

- **Not run-time tested** this sprint.
- **Risk:** inventory **click** translation may differ; players should fall back to **`/top`**, **`/skilltop`**, **`/rank`** (chat).
- **Status:** **NRP** (needs proof).

## Fallback

- If menu infrastructure fails, players retain **chat** leaderboards and **`/updateleaderboards`** (unchanged).

## Remaining menu gaps (not this sprint)

- **Abilities** menu, full **Slate** **`LeaderboardMenu`** (skulls, **`leaderboard.yml`**), **confirm** framework.

## Pass / fail

| Check | Pass |
|-------|------|
| `:fabric:test` green |  |
| No regression in console **`/top`** / **`/rank`** |  |
| Openmenu wired on all **skills** alias roots |  |

## Risks / caveats

- **GUI vs chat:** Bukkit **`/top`** is chat-only; Fabric adds a **separate** GUI path — intentional.
- **Back target:** **Skills** vs Bukkit **level_progression** — document as product difference.
- **Bedrock** clicks not verified.

**Tests run:** `.\gradlew -PfabricOnly=true :fabric:test` — **BUILD SUCCESSFUL** (see agent run).
