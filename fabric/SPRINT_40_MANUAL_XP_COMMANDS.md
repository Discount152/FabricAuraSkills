# Sprint 40 — manual validation (**xp** + shared arguments)

**Scope:** **`/xp`** and **`/auraskills xp`** — **view** / **add** / **set** / **remove** with **online** `<player>`, enabled **`<skill>`** key, and **amount** where required. **Clients:** **Java** and **Bedrock (Geyser/Floodgate)**.

## Prereq

- At least **two** online accounts (or one + console) to test **other-player** targets.
- Permission plugin optional; defaults: **view** allowed broadly; **add/set/remove** denied until granted (**op** / LP).

## Java

1. **`/xp view <you> <skill>`** — shows level + XP for that online player + skill (translation **`auraskills.granite.command.xp.view_line`**).
2. **`/xp add <player> <skill> <amount>`** — with **`auraskills.command.xp.add`** granted: target gains XP; feedback uses **`CommandMessage.XP_ADD_ADDED`** (prefix + message). Repeat for **set** / **remove** (`XP_SET` / `XP_REMOVE`).
3. **Invalid player** — `/xp view nobody123 farming` → **player_not_found** (no crash).
4. **Invalid skill** — unknown key → **`UNKNOWN_SKILL`**-style error.
5. **add** with amount **0** or negative → **amount_positive** error and **no** XP change.
6. **remove** more than current XP — clamps to 0 (same idea as Bukkit).

## Bedrock / Geyser

- Repeat **1–2**; chat feedback should still show server-side.

## Checklist

| Check | Java | Bedrock |
|-------|------|---------|
| xp view | [ ] | [ ] |
| xp add/set/remove (staff) | [ ] | [ ] |
| bad player / bad skill | [ ] | [ ] |
