# Sprint 53 — manual validation: `/item requirement` and `/item multiplier`

**Runtime:** Fabric dedicated server; **Java** and **Bedrock (Geyser/Floodgate)**. **Perms (Bukkit names):**

- `auraskills.command.item.requirement` — default **deny** on fabric-perms for staff
- `auraskills.command.item.multiplier` — same

These are **independent** from `auraskills.command.item.modifier` and `auraskills.command.item.ignore`.

## Preconditions

- Non-empty **main hand** (same as Sprint 51 item commands)
- For **requirement** / **multiplier** **add**, grant only the relevant perm to verify separation from **modifier**

## Java — requirement

1. Hold any item. **`/item requirement add farming 10`** — expect success; NBT has `PublicBukkitValues` `auraskills:item_requirements` compound (skill id keys → int level).
2. **`/item requirement list`** — one line per skill/level; header from messages.
3. **`/item requirement remove farming true`** (or default two-arg) — removed from PDC; with **lore**, requirement lore line should follow Bukkit **Requires** + skill name heuristic.
4. **`/item requirement removeall`** — requirements cleared.
5. **Duplicate** skill on **add** — `ITEM_REQUIREMENT_ADD_ALREADY_EXISTS`.
6. **Remove** missing skill — `ITEM_REQUIREMENT_REMOVE_DOES_NOT_EXIST`.
7. **Empty hand** — `item_armor_mod.empty_hand` / **not player** for console.

## Java — multiplier

1. **`/item multiplier add global 25`** — success; PDC `auraskills:item_multipliers` with `auraskills:global` (or reader-equivalent) double.
2. **`/item multiplier add mining 10`** — per-skill entry (after removing global if only one global slot desired — Bukkit allows one global; duplicate global → `ITEM_MULTIPLIER_ADD_ALREADY_EXISTS`).
3. **`/item multiplier list`** — `MULTIPLIER_GLOBAL` + skill display names; entries use `ITEM_MULTIPLIER_LIST_*` messages.
4. **`/item multiplier remove global`** / **`remove <skill>`** — `REMOVED` or `DOES_NOT_EXIST`.
5. **`/item multiplier removeall`** — all multipliers cleared.
6. Invalid target (not a skill, not `global`) — *Target must be valid skill name or global* (plain English server error; safe on Bedrock).

## Bedrock / Geyser

- Run a subset; feedback is **Text** (no Java-only client formatting).

## Persistence / reload

- Data is on the **ItemStack** `custom_data` — drops/pickup preserve with normal NBT.
- **Reload** does not strip item NBT; global skill configs affect **parsing** of skill ids, not already-written NBT.

## What is still not on Fabric (this sprint)

- **`/armor requirement`** / **`/armor multiplier`** (Bukkit has them) — not implemented in Sprint 53.
- **Item** **register** / **give** — still out of scope for this equipment batch.

## Risks

- **Multiplier** invalid-target message is a **fixed English string** (not a `MessageKey`); operators may want a lang entry in a follow-up.
- **Requirement remove lore** uses the same **Requires** + capitalized **enum name** heuristic as Bukkit; non-English `commands.*` may need a stricter line match later.
