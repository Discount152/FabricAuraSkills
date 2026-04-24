# Sprint 49 — manual validation: **top average**, **updateleaderboards**, row names

**Scope:** **`/top average`**, **`/top average <page>`**, **`/updateleaderboards`**, **`/updateleaderboards <samples>`** and mirrors on **`/auraskills`**, **`/sk`**, **`/skill`**. **Java** and **Geyser** (server-side chat). Existing **`/top`**, **`/skilltop`**, **`/rank`**, **`/skillrank`** regression.

**Perms:** **`auraskills.command.top`** (average is under **`/top`**), **`auraskills.command.updateleaderboards`** (Bukkit default **op**; Fabric **`requires(..., false)`** — use **LuckPerms** / **op** to grant).

**Name display:** Offline rows show the **Mojang user-cache** name when the server has a **`PlayerConfigEntry`** for that **UUID** (players who have joined are typical). If there is no cache line, the placeholder **`?`** appears — **Geyser** clients see the same text as **Java**.

## Commands

- **`/top average`** — first page, **skill average** leaderboard (formatted average level).
- **`/top average 2`** — second page (if enough entries).
- **`/updateleaderboards`** — one refresh, then **`UPDATELEADERBOARDS_UPDATED`**-style line.
- **`/updateleaderboards 3`** — **three** runs, then **updated** + **Average update time over 3 samples: _n_ ms** (Granite **caps** at **64**; Bukkit allowed larger **int**).

**Prefix tests:** **`/sk top average`**, **`/auraskills updateleaderboards`**, **`/skill top average 1`**.

## 1) Java (desktop)

1. After **leaderboard data** exists, run **`/top average`** — **average** header + rows; **`{level}`** column is **two-decimal** style (see **`NumberUtil.format2`** on **`SkillValue#xp`**, same as Bukkit **`sendLeaderboardEntries`**).
2. Run **`/top average 2`** if the list is long enough — ranks continue (**11+** for page 2).
3. Run **`/updateleaderboards`** — after a short delay, **success** message (async).
4. Run **`/updateleaderboards 5`** — **updated** + **average ms** line with **5** samples.
5. Confirm **`/top`**, **`/skilltop farming`**, **`/rank`**, **`/skillrank farming`** still behave (**Sprint 47**).
6. With **LuckPerms**, **deny** `auraskills.command.updateleaderboards` — command blocked; **allow** for ops.

## 2) Bedrock / Geyser

7. Run the same strings from chat; no **Java-only** GUI assumptions.

## 3) Row names

8. For a **known offline** player who **previously joined**, leaderboard lines should show their **last cached** name when **usercache** has them; **never-joined** UUIDs in data can still show **`?`**.

## 4) Concurrent update

9. If possible, run **`/updateleaderboards`** while a periodic **5m** **LeaderboardManager** run might overlap — you may occasionally see **already_updating** (same as Bukkit).

## Checklist

- [ ] **Regression:** **Sprint 48** utility commands unchanged.
- [ ] **Permission:** `auraskills.command.updateleaderboards` defaults like **Bukkit** **op** in your pack.
