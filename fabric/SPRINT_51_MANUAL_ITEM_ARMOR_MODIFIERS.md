# Sprint 51 — manual validation: `/item` and `/armor` (held stat/trait modifiers)

**Runtime:** Fabric dedicated server; **Java** and (recommended) **Bedrock (Geyser/Floodgate)**. **Perms:** `auraskills.command.item.modifier`, `auraskills.command.armor.modifier` (Bukkit names; default **deny** on fabric-perms for staff use).

## Preconditions

- Player with permission on **one** of the two perm nodes to match the command tree in use.
- A **non-empty** item in the **main hand** (reference uses “item held”).
- **Stats** and **traits** that exist in config (suggestions use enabled keys).

## Java

1. Hold e.g. a **sword**; run `/item modifier add <stat> 1` — expect success, **NBT** on item under `custom_data` / `PublicBukkitValues` (`auraskills:item_modifiers` list) and (if not disabled) **lore** line.
2. `/item modifier list` — lists modifiers; `/item modifier remove <stat> true|false` — removes stat row and optionally lore.
3. `/item modifier removeall` — clears all **stat** modifiers on the held item.
4. **Traits:** `/item trait add <trait> 1` … `remove`, `list`, `removeall` (see messages).
5. **Armor** tree: same steps with a **chestplate/helmet/etc.** in hand using `/armor modifier …` and `/armor trait …` (writes `auraskills:armor_modifiers` / `auraskills:armor_trait_modifiers` and clears **legacy** `AuraSkills/Modifiers/Armor` for that type when PDC is written).
6. After changes, check **stats/traits in-game** update — commands call **`ModifierManager#applyModifiers`** for the user.

## Bedrock / Geyser

- Run **add** / **list** from Bedrock; feedback is server-side **Text**; confirm translatable and literal lines show.

## Permissions

- Without **item**/**armor** perms, commands do not run (`requires`).

## Persistence

- `custom_data` is on the item stack — item must remain in inventory; **drop** and **pick up** should preserve modifiers.

## Not in this sprint (reference has more)

- **Item:** requirement/multiplier/ignore/**register**/**give**; **not** in Sprint 51.
- **Armor:** requirement/**multiplier**; **not** in Sprint 51.

## Risks

- **Legacy+ PDC** items: first PDC write **strips** legacy `Modifiers/Item|Armor` in `custom_data` to avoid **double** application with the new list form.
