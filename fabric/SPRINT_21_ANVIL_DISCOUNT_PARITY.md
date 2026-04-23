# Sprint 21 — Bukkit `AnvilDiscountTrait` vs Fabric (narrow diff)

**Reference:** `bukkit/.../trait/AnvilDiscountTrait.java`  
**Fabric:** `trait/FabricAnvilDiscountTrait`, `FabricAnvilDiscount`, `mixin/AnvilScreenHandlerAnvilDiscountMixin` (after SPRINT 16 anvil take / level-cost wiring).

## Reference behavior (Bukkit)

1. **`PrepareAnvilEvent`**, after vanilla prepares the result.
2. If **`Traits.ANVIL_DISCOUNT`** is not enabled, return.
3. Among **`event.getViewers()`**, if any is a **Player** in a **disabled** skills world, **return** (no discount for the whole event).
4. Otherwise choose the **Player** with the highest **`Stats.WISDOM`** level; use that **`User`**.
5. **`traitValue` = `getEffectiveTraitLevel(ANVIL_DISCOUNT)`**.
6. **`getDiscount(traitValue)`** from optional YAML `formula` (EvalEx, `value` = trait level) or Bukkit fallback: `-1.025^(-traitValue) + 1` (discount fraction in [0,1]).
7. **`int cost = round(repairCost * (1 - discount))`**; if `cost > 0` set that on the view, else set **1** (minimum displayed/charged level cost in reference).

**Minecraft 1.21+ (Bukkit):** uses `AnvilView#setRepairCost` (MC 1.21.1+ branch in reference).

## Fabric (Sprint 21)

1. After **`AnvilScreenHandler#updateResult()`** (`@Inject` **TAIL**), read **`Property#get()`** (vanilla level cost for this tick).
2. Same enabled / disabled-world checks (single **screen owner** = `ForgingScreenHandler#player` as **`ServerPlayerEntity`** on logical server; **no client**).
3. **`User`** from **`getUserManager().getUser(player.getUuid())`** — **no Bukkit `HumanEntity` list**; we do **not** scan multiple viewers and do **not** pick the highest **Wisdom** among several players. *Gap:* if a mod ever exposes multi-viewer anvils, parity with the reference’s *“highest WIS”* pick would need an explicit list of `ServerPlayerEntity` candidates.
4. Same **`getDiscountForTraitValue`** and **`getEffectiveTraitLevel(ANVIL_DISCOUNT)`** as Bukkit; **`resetFormula()`** on trait **reload** (like Bukkit reload executor).
5. Write discounted cost back with **`Property#set`**.

**Alignment with SPRINT 16:** `getLevelCost()` / `FabricAnvilTakeContext` and **`FabricAnvilXpHandler`** use the **same** `Property` as vanilla — shown cost, charged cost, and **`{repair_cost}`** in sources stay consistent if nothing else rewrites the cost after our inject.

## Status

- **In-repo, single-owner anvil, logical server:** **Equivalent** to the reference for cost math, formula, min cost **1**, and world/trait gates, modulo **viewer / Wisdom** (documented).
- **Multi-viewer or strict Bukkit viewer-order semantics:** **Needs Regression Proof** or follow-up.
- **Full Bukkit `BukkitTraitManager` (traits):** only **`DoubleDropTrait`** remains (see **`SPRINT_19_TRAIT_PARITY.md`**, *Out of scope* in backlog).
