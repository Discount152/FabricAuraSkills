# Sprint 56 — manual validation: `/claimitems`

**Reference (Bukkit):** `SkillsRootCommand#onClaimItems` → **Slate** **UnclaimedItemsMenu** (54 slots, per-click **claim**). **Fabric (Granite):** one-shot **merge** of the **`User` unclaimed list** into the player’s main+hotbar inventory using **`tryMergeRegisteredItemCount`**.

**Permissions:** `auraskills.command.claimitems` (Bukkit: default **true** under `auraskills.command.user` — verify with your permission plugin.)

## Register & overflow setup

1. **Admin:** `/item register test_key` with a small stack, then `/item give <self> test_key` with a **full** inventory to push overflow into **unclaimed** (or use any path that appends to **`unclaimed_items`** in player data).  
2. **Confirm** you see the **UNCLAIMED_ITEM** (leveler) message when overflow occurs.

## Java — `claimitems`

1. With **at least one** unclaimed entry, run **`/claimitems`**, **`/skills claimitems`**, or **`/sk claimitems`**.  
2. **Expect:** items **merge** into empty slots; unclaimed list shrinks; **`/auraskills claimitems`** also works.  
3. **Nothing to claim:** with an **empty** unclaimed list, expect **`commands.claimitems.no_items`** (common **`CLAIMITEMS_NO_ITEMS`**), same family as Bukkit.  
4. **Full inventory:** with unclaimed still present, fill every main+hotbar slot (36), run **`/claimitems`** — expect no delivery (`touch == 0`) and a **“inventory full”** line (`menus.unclaimed_items.inventory_full` / **`MenuMessage.INVENTORY_FULL`**), list **unchanged**.  
5. **Partial:** have **one** unclaimed line with a **count larger than** one empty slot can absorb for that stack (e.g. 64 in one slot) — one pass may **shrink** the `KeyIntPair` value; run again after freeing space to clear.  
6. **Stale key:** if an unclaimed entry’s key is **not** in **`items.yml`**, **`clearInvalidItems`** (start of command) and in-loop checks should **drop** it.

## Bedrock / Geyser

- Type **`/claimitems`** or **`/sk claimitems`** in chat. No **Java-only** UI is required. Tab completion: **N/A** (no args).

## Persistence

- After a successful run that **mutates** the unclaimed list, **disconnect/reconnect** (or use **`/auraskills save`**) and confirm unclaimed state **matches** what you expect in storage.

## Known gaps (by design in this sprint)

- No **Slate** chest UI; no **per-click** **claim** like Bukkit’s menu.
