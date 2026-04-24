# Sprint 41 — manual validation: **mana** commands (Fabric / Geyser)

**Scope:** bounded **mana** family on the Fabric command lane (`FabricManaCommands`). **Java** and **Bedrock (Geyser/Floodgate)** should see the same permission gates and comparable feedback (translatables + common prefix formatting).

## Preconditions

- Server with **FabricAuraSkills** build **≥ 2.3.69** (or current branch).
- At least **two online players** (Java + Bedrock if possible) for **view** / target mutations.
- **LuckPerms** (or equivalent) to flip: `auraskills.command.mana`, `auraskills.command.mana.other`, `auraskills.command.mana.add`, `auraskills.command.mana.set`, `auraskills.command.mana.remove`.

## Self mana (default `/mana`)

1. Grant **`auraskills.command.mana`**.
2. Run **`/mana`** (and **`/auraskills mana`** with no args). Expect **own** current/max mana line (common **`MANA_DISPLAY`** path).
3. Revoke **`auraskills.command.mana`**. Expect **permission denied** (translatable **`auraskills.granite.command.permission_denied`**).
4. From **console**, run **`/mana`** (no args). Expect **console** hint to use **`/mana view <player>`** or join the world.

## View another player

1. Grant **`auraskills.command.mana.other`**.
2. **`/mana view <OnlineName>`** — expect **other** display (`MANA_DISPLAY_OTHER`); target’s **locale** used for the built line (issuer still sees feedback).
3. Use a **name that is not online** — expect **player not found** (same key as **xp**: `auraskills.granite.command.xp.player_not_found`).

## Add / set / remove (mutations)

Use an **online** target. Staff perms default **deny** in typical LP templates (match Bukkit **op** defaults); grant as needed.

| Step | Command | Permission | Expect |
|------|---------|------------|--------|
| Add | `/mana add <player> 10` | `…mana.add` | Mana increases (capped by max / `Traits.MAX_MANA` overflow rules). Success uses common **`MANA_ADD_ADDED`**. |
| Set | `/mana set <player> 50` | `…mana.set` | Mana set (capped at max unless overflow allows). **`MANA_SET_SET`**. |
| Remove | `/mana remove <player> 5` | `…mana.remove` | Mana decreases (floors at 0). **`MANA_REMOVE_REMOVED`**. |
| Add with `0` or negative | `/mana add … 0` | — | Error: amount must be **> 0** (`…command.xp.amount_positive` — shared with **xp** add). |
| Set/remove with negative | `/mana set … -1` | — | Error: **≥ 0** (`…command.xp.amount_non_negative`). |

Repeat under **`/auraskills mana …`** (same tree).

## Bedrock / Geyser notes

- Commands are normal Brigadier roots; Geyser forwards **chat** commands. Verify **tab** is optional; type full names if completion differs.
- Confirm **permission_denied** and success lines show as **plain text** (no Java-only-only assumptions beyond what the common `MessageBuilder` + prefix already do for **Bukkit**).

## Pass / fail checklist

- [ ] Self `/mana` works with perm; denied without.
- [ ] Console `/mana` shows **console_specify** message.
- [ ] `view` / `add` / `set` / `remove` behave and match **overflow** expectations for your `max_mana` config.
- [ ] Per-node permissions can be revoked independently (e.g. **view** other without **add**).
