# Sprint 42 — manual validation: **skilllevel** commands (Fabric / Geyser)

**Scope:** **`/skilllevel`** and **`/auraskills skilllevel`** (`FabricSkillLevelCommands`). Validates online targeting, skill keys, level mutations, and permissions on **Java** and **Bedrock (Geyser)** chat commands.

## Preconditions

- Build **≥ 2.3.70** (or branch with Sprint 42).
- Two online players if testing **view** / mutations on another.
- LuckPerms (or equivalent): **`auraskills.command.skilllevel.view`**, **`auraskills.command.skill.setlevel`**.

## View

1. Grant **`auraskills.command.skilllevel.view`** (default allow in Fabric perm checks).
2. **`/skilllevel view <OnlineName> <skillKey>`** (e.g. `farming`) — expect one line: name, skill display, **level**, **XP** in that level (see **`auraskills.granite.command.skilllevel.view_line`**).
3. Unknown skill key — expect unknown-skill feedback (same path as **xp**).
4. Offline name — expect **player not found** (same key as **xp**).

Repeat as **`/auraskills skilllevel view …`**.

## Add / set / remove (mutations)

Grant **`auraskills.command.skill.setlevel`** (matches Bukkit; default deny like staff).

| Command | Notes |
|---------|--------|
| **`/skilllevel add <player> <skill> <count>`** | **count** must be **≥ 1**. Adds to current level, then applies full Bukkit-style side effects (stats, rewards, modifiers). Success: common **`SKILL_SETLEVEL_SET`** (final level). |
| **`/skilllevel set <player> <skill> <level>`** | Absolute level; must be **≥** configured **start level**; else **`SKILL_AT_LEAST`**. |
| **`/skilllevel remove <player> <skill> <count>`** | Subtract **count** levels, **floor** at start level. Success message shows **resulting** level. |

- **add** with `0` or negative **count** → **`…level.count_positive`**.
- **remove** with invalid count → same error as **add** (positive count only).

## Permissions

- Revoke **`skilllevel.view`** — **view** should **not** run.
- Hold **`view`** but not **`skill.setlevel`** — **add**/**set**/**remove** denied.
- Console: should work if the server allows console and permissions grant (same as other Fabric admin commands).

## Geyser / Bedrock

- Type full command; tab completion may differ from Java — behavior should match (permission gates + feedback text).

## Pass / fail

- [ ] **view** shows sensible level + XP.
- [ ] **set** enforces start level; **add**/**remove** use positive counts and sensible results.
- [ ] **Unknown** skill and **not online** target fail cleanly.
- [ ] **Permission** matrix above holds.
