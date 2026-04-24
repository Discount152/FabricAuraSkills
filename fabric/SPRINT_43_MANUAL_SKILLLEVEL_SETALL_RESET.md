# Sprint 43 — manual validation: **skilllevel** `setall` + `reset` (Fabric / Geyser)

This extends the **Sprint 42** **skilllevel** lane with **`setall`** and **`reset`**, matching Bukkit `SkillCommand` capability on **online** players.

## Full command list (Sprint 42–43)

| Command | Permission (typical default) |
|---------|------------------------------|
| `skilllevel view <player> <skill>` | `auraskills.command.skilllevel.view` (allow) |
| `skilllevel add …` / `set …` / `remove …` | `auraskills.command.skill.setlevel` (deny) |
| `skilllevel setall …` | `auraskills.command.skill.setlevel` (deny) |
| `skilllevel reset …` | `auraskills.command.skill.reset` (deny) |

Also under **`/auraskills skilllevel …`**.

## Preconditions

- Build **≥ 2.3.71** (Sprint 43) with prior **Sprint 42** behavior present.
- Two online players if testing cross-target.
- LuckPerms or fabric-perms to flip the nodes above.

## **setall**

1. Grant **`auraskills.command.skill.setlevel`**.
2. Run **`/skilllevel setall <OnlineName> <level>`** with **level ≥ start level** from config.
3. Expect **`SKILL_SETALL_SET`**-style success (all **enabled** skills set; per Bukkit, global stats/modifiers/permissions after batch reward steps).
4. Use **level &lt; start** — expect **`SKILL_AT_LEAST`** (same as single **set**).

## **reset**

1. Grant **`auraskills.command.skill.reset`**.
2. **`/skilllevel reset <player> <skill>`** — single skill to start; expect **`SKILL_SETLEVEL_SET`** with **start level**; uses **`User#resetSkill`** then modifier re-apply (Bukkit order).
3. **`/skilllevel reset <player>`** (no skill) — resets **every** skill in the registry (Bukkit loop); expect **`SKILL_SETALL_SET`** with level = last iteration’s start (matches reference).
4. Bad skill key — unknown skill message.
5. Offline name — player not found.

## Java + Bedrock

- Chat commands only; no client-only-only feedback (uses common `MessageBuilder` / prefix where applicable).
- Geyser: type full commands if tab differs.

## Pass / fail

- [ ] **setall** at/above start level updates all enabled skills and sends **setall** message.
- [ ] **reset** one vs all matches expected messages and visible level changes.
- [ ] **Permissions** gate **setall** vs **reset** independently of **view**.

See also **`SPRINT_42_MANUAL_SKILLLEVEL_COMMANDS.md`** for **view** / **add** / **set** / **remove**.
