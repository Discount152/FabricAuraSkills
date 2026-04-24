# Sprint 50 — manual validation: `/modifier` and `/trait` (player-scoped)

**Runtime:** Fabric dedicated server, **Java** and (recommended) **Bedrock via Geyser + Floodgate**. **Permission:** `auraskills.command.modifier` (default **deny** in fabric-perms unless granted).

## Preconditions

- Two online accounts: one **op** or with `auraskills.command.modifier` **true**; one target player online.
- Known **stat** and **trait** keys from config (tab-complete suggests **enabled** stats/traits).
- ** `/auraskills reload`** after config edits if you change enabled skills/stats/traits (same as other admin flows).

## Java client

1. As admin, run **`/modifier add <player> <stat> <name> 1`** — expect success line and target stats updating after `applyModifiers`.
2. Run **`/modifier add <player> <stat> <name> 0.1 MULTIPLY true`** (or add second modifier with same base name to exercise **stack** naming **`(2)`**).
3. **`/modifier list`** (in-game, self) vs **`/modifier list <player>`** (all stats for that online player) vs **`/modifier list <player> <stat>`** (one stat’s modifiers). Repeat for **`/trait list`** with **trait** id as the last arg.
4. **`/modifier remove <player> <name>`** — expect removed message; stat should reflect removal after apply.
5. **`/trait`** — repeat analogous **add** / **list** / **remove** / **removeall** for a **trait** id.

## Bedrock / Geyser

- Run the same **add** / **list** / **remove** steps from a Bedrock client (Floodgate). Command feedback is **server-side** `Text` / translatables; confirm messages appear and are readable.
- If permission is denied, confirm the **denial** string matches other Granite commands (no Java-only client assumptions).

## Player targeting

- **Wrong name:** `No online player: …` (same pattern as **xp**).
- **Console:** forms that require a **source player** (e.g. self **list** / **removeall** with no args) should error with the **players-only** message from messages YAML where applicable; otherwise specify **`<player>`** explicitly from console.

## Permissions

- Without **`auraskills.command.modifier`**, subcommands are **not** available (Brigadier `requires`).
- Grant only on staff groups in production; defaults align with Bukkit **op**-style use of **`auraskills.command.modifier`**.

## Persistence / reload

- After **add** / **remove**, **`setShouldSave(true)`** is set — disconnect and rejoin (or check DB/user file) to confirm modifiers persist per existing storage. **`/auraskills reload`** should not silently drop in-memory state without the usual project rules for admin reload; treat **reload** as a smoke check only (see `FabricAuraSkills` reload path).

## Known gaps (by design for this sprint)

- No **`/modifier addtemp`** (or trait temp) / duration — **Partial** vs Bukkit reference.
- **Online-only** resolution for **player** argument — no offline UUIDs.
