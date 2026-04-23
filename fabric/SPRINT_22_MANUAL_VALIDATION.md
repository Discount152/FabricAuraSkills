# Sprint 22 — `DoubleDropTrait` manual validation (Fabric)

**Prereq:** `Traits.DOUBLE_DROP` enabled in config; a skill/leveling path that grants **Farming** (or the configured skill for `double_drop`).

## Core checks (Java; repeat one Bedrock session if available)

1. **Blocked world** — add current dimension to `blocked_worlds` in `config.yml`, reload, break allowlisted stone in that world: **no** double drops. Remove from list, reload: double drops can occur again.
2. **Placed block** — place e.g. **cobblestone** from inventory, then break: **no** double drop. Mine **natural** cobble (or generated structure): double drop **can** apply (chance > 0).
3. **Creative / spectator** — no extra drops in creative; none in spectator.
4. **Gamerule** — `/gamerule doTileDrops false`, break allowlisted block: no double-drop extras (and no normal tile drops).
5. **Allowlist** — only **stone, cobble, sand, gravel, dirt, grass block, andesite, diorite, granite**; break e.g. **oak log**: no double drop from this trait.
6. **Silk touch** — with silk on main hand, break **stone** / **grass block**: on proc, extras should be **stone** / **grass block** items (not wrong clones).
7. **max_percent** — set trait `max_percent` to a low value, confirm proc rate feels capped.
8. **toInventory** — if `to_inventory` (or project equivalent in `options.yml` / loot manager) is enabled for the player’s main-hand context, double-drop extras go to **inventory** when rules allow; otherwise they **spawn** as `ItemEntity` at block center. No duplicate **XP** from this trait (it does not call `addExperience`).

## Not validated here (out of default scope)

- **WorldGuard** / external region plugins.
- **Bukkit** `LootDropEvent` **cancel** (no Fabric event).
