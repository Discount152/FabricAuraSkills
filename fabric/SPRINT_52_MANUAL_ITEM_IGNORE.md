# Sprint 52 — manual validation: `/item ignore`

**Perms:** `auraskills.command.item.ignore` (default **deny** for fabric-perms unless granted). **Different** from `auraskills.command.item.modifier` (modifier/trait subtrees).

## Preconditions

- Player with **`item.ignore`** **true** (without **`item.modifier`** you can still run **`/item ignore …`** if you only grant **ignore**).
- Non-empty **main-hand** item for **add** / **remove**.

## Java

1. Hold a **bow** (or any item). Run **`/item ignore add`** — expect success message (**`ITEM_IGNORE_ADD_ADDED`**).
2. Use a mana-ability path that checks ignore (e.g. **Charged Shot** / **Sharp Hook** on Fabric — see `FabricManaAbilityItemTags` / `FabricChargedShot` / `FabricSharpHook`). Behavior should **skip** ability logic when the tag is set (same as Bukkit **`shouldIgnoreItem`**).
3. **`/item ignore remove`** — clears **`auraskills:ignore_interact`** from **`custom_data` / `PublicBukkitValues`**; expect **`ITEM_IGNORE_REMOVE_REMOVED`**.
4. **Empty hand** — expect **`item_armor_mod.empty_hand`** (shared with Sprint 51 held-item commands).
5. **Console** — expect **`item_armor_mod.players_only`**.

## Bedrock / Geyser

- Run **add** / **remove** from Bedrock; messages use server **Text** (no Java-only client assumptions).

## Persistence

- Tag lives on the **ItemStack**; dropping / picking up the item should preserve it if NBT is preserved (normal stack behavior).

## Risks

- **`item` root** has **no** single permission: **`modifier`**, **`trait`**, and **`ignore`** each have their own **`requires`** — grant only what staff need.
