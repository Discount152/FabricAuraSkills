# Sprint 47 — manual validation: **leaderboard** and **rank**

**Scope:** **`/top`**, **`/skilltop`**, **`/rank`**, **`/skillrank`** and the same under **`/auraskills`**, **`/sk`**, **`/skill`**. **Java** and **Geyser** (server-side command strings). Perms: **`auraskills.command.top`**, **`auraskills.command.rank`** (default like Bukkit **`plugin.yml`** **true** for players, confirm with your permission plugin).

**Note:** In **Sprint 47**, **offline** leaderboard entries show the placeholder **`?`** for the **player column** (online players show real names). Resolving **cached** profile names is a follow-up.

## Commands

- **`/top`** — first **power** page (10 entries).
- **`/top <page>`** — paged **power** (page ≥ 1).
- **`/skilltop <skillkey>`** — first page for that **skill** (use enabled skill keys; **`FabricCommandSkillParser`**).
- **`/skilltop <skillkey> <page>`** — paged **per-skill** top.
- **`/rank`** — **players only**; power rank + one line per **enabled** skill (same **shape** as Bukkit `onRank` lines).
- **`/skillrank <skillkey>`** — **players only**; one **RANK_ENTRY**-style line for that skill.

**Prefix tests:** **`/sk top`**, **`/auraskills top`**, **`/skill skilltop farming`**, etc.

## 1) Java (desktop)

1. With a few online players and **data**, run **`/top`** — expect header + up to 10 **power** rows.
2. Run **`/top 2`** (if list is long enough) — different slice; ranks continue **11+**.
3. Run **`/skilltop farming`** — **skill** header + entries by **farming** level.
4. Run **`/rank`** as a player — **RANK** header, **power** line, then **each** enabled skill.
5. Run **`/skillrank farming`** — **single** line for **Farming** (or your skill’s display name).
6. **Console** **`/rank`** — error **`auraskills.granite.command.leaderboard.players_only`**.

## 2) Bedrock / Geyser

7. Type the same commands in chat; ensure no client-only **Java** assumptions beyond normal chat output.

## 3) Permissions

8. **Deny** `auraskills.command.top` — **`/top`** should not run (and **`/sk top`** the same).
9. **Deny** `auraskills.command.rank` — **`/rank`** and **`/skillrank`** blocked.

## Checklist

- [ ] **LeaderboardManager** has been run at least once (server uptime / **common** 5**m** task); empty lists are still valid.
- [ ] **Wrong** skill key → **TOP_USAGE**-style help (message **`commands.top.usage`** from **`CommandMessage#TOP_USAGE`** / messages merge).
- [ ] **Regression:** **`/xp`**, **openmenu**, and prior sprints’ commands still work.
