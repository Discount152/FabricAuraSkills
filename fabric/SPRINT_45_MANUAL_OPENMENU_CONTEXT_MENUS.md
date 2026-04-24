# Sprint 45 — manual validation: **`openmenu`** + **level_progression** / **stat_info**

**Scope:** Fabric **bounded** `openmenu` with **typed** **skill** / **stat** args for context-heavy built-in menus (Java + Geyser chat). **No** JSON `properties` (Bukkit Slate); use **`/openmenu <literal> …`** (Brigadier literals for menu kind).

**Environment:** 2+ online players, permissions `auraskills.command.openmenu` (+ `auraskills.command.stats` for `/stats` if testing alongside).

## Syntax (Granite / Fabric)

| Menu | Self | Target / console |
|------|------|------------------|
| **skills** | `/openmenu skills` | `/openmenu skills <onlinePlayer>` |
| **stats** | `/openmenu stats` | `/openmenu stats <onlinePlayer>` |
| **level_progression** | `/openmenu level_progression <skillKey>` | `/openmenu level_progression <onlinePlayer> <skillKey>` |
| **stat_info** | `/openmenu stat_info <statKey>` | `/openmenu stat_info <onlinePlayer> <statKey>` |

Also: **`/auraskills openmenu …`**, **`/sk openmenu …`**, **`/skill openmenu …`** (same tree).

- **Context** routes through **`FabricAuraSkillsMenus.openLevelProgression`** / **`openStatInfo`** (registry + **`FabricMenuContext`**). **List page** for “Back” defaults to **0** (not a command arg in this sprint).
- **Unknown** skill/stat: **`auraskills.granite.command.openmenu.unknown_skill`** / **`unknown_stat`**.
- **Console:** cannot use self-only one-arg forms; use **`/openmenu level_progression <player> <skill>`** and **`/openmenu stat_info <player> <stat>`** (and **`skills` / `stats`** with target as before).

## Java (desktop)

1. Open **level progression** for self: e.g. **`/openmenu level_progression farming`** — correct **Skill** focus; **Back** returns to **Skills** list (page 0).
2. Open **stat info** for self: e.g. **`/openmenu stat_info strength`** (or an enabled stat key) — **Back** to **Stats** list.
3. As player A, run **`/openmenu level_progression B farming`** with B online — menu opens for **B**; A sees **`opened_for_context`**; B sees the GUI.
4. Invalid skill: **`/openmenu level_progression not_a_real_skill_zzz`** — error, no GUI.
5. Invalid stat: **`/openmenu stat_info not_a_stat`** — error.

## Bedrock (Geyser / Floodgate)

6. Same commands in chat; suggestions depend on the client (server may still offer literals + keys where supported).

## Permissions

7. Deny `auraskills.command.openmenu` — **`/openmenu level_progression farming`** should fail permission check like other `openmenu` forms.

## Checklist

- [ ] All four built-in menu kinds open as expected when enabled in config.
- [ ] No duplicate menu stack / wrong skill or stat on open.
- [ ] Target opens do not swap executor/target context incorrectly.
