# Sprint 22 — Bukkit vs Fabric `DoubleDropTrait` (core in-scope)

**Runtime:** Java **Fabric** only. **Reference:** `bukkit/.../trait/DoubleDropTrait.java` (not a runtime dependency).

**Scope:** **Granite** default product = Fabric-native core behavior. **Not** in scope: **WorldGuard** / third-party Bukkit **region** hooks, **api-bukkit** `LootDropEvent` fire/cancel, or any Paper plugin–specific loot pipeline.

## 1. Bukkit (reference) — what it does

| Area | Behavior |
|------|----------|
| **Trigger** | `BlockBreakEvent`, **`EventPriority.HIGHEST`** (runs after most listeners; must not be **cancelled**). |
| **Gates (before math)** | Trait enabled; not **cancelled**; **`isInBlockedWorld`**, optional **WorldGuard** `FlagKey.XP_GAIN` block, **`isDropItems`**, not **CREATIVE**, **not placed block** (`RegionManager#isPlacedBlock`). |
| **Blocks** | Fixed list: **STONE, COBBLESTONE, SAND, GRAVEL, DIRT, GRASS_BLOCK, ANDESITE, DIORITE, GRANITE**. |
| **Chance** | `effectiveTraitLevel`, capped by `max_percent` option, `rand < chance/100`. |
| **Drops** | `block.getDrops(tool)`; per stack: if **silk** on tool — **STONE** and **GRASS_BLOCK** map to that block’s item, else **clone**; `LootManager#toInventory(mainHand)` → `LootDropEvent` (**Cause.LUCK_DOUBLE_DROP**) then **`ItemUtils.giveBlockLoot`**. |

## 2. Fabric (this sprint) — what is implemented

| Area | Implementation |
|------|-----------------|
| **Trigger** | `PlayerBlockBreakEvents.AFTER` — after vanilla break and drops. |
| **Gates** | `Traits.DOUBLE_DROP` enabled; not **creative** / not **spectator**; `GameRules.DO_TILE_DROPS` true; `WorldManager#isBlockedWorld` (`FabricUser.worldNameCompat`); `FabricRegionManager#isPlacedBlock` (must see placed flag **before** `FabricPlacedBlockBreakHandler` clears it — `FabricLevelManager` registers `FabricDoubleDrop.register` **before** placed-break handler). |
| **Blocks** | Same fixed list via `BlockState` (`Blocks.*`). |
| **Chance** | `getEffectiveTraitLevel` + `max_percent` cap; same `min(effective, cap)/100` roll. |
| **Drops** | `Block.getDroppedStacks` with current **broken** `BlockState` at `pos` (post-break context). Silk rewrites for **stone** and **grass block** only. Extra items via `FabricLootManager#toInventory` (inventory vs world spawn) matching Bukkit’s give path. |

**Out of scope (explicit, not in default Granite product):**

- **WorldGuard** `XP_GAIN` (and any Bukkit `HookManager` region checks).
- **`LootDropEvent`** construction, cancel, and listeners (no Bukkit `PluginManager` on Fabric).
- Bukkit’s **`!event.isDropItems()`** has no single Fabric API equivalent; **DO_TILE_DROPS** + **post-break** path approximates “drops allowed” (see caveats below).

**Parity status recommendation**

| View | Status |
|------|--------|
| **In-scope core (block list, chance, cap, placed/blocked world, silk stone/grass, toInventory / spawn extra)** | **Equivalent** to the reference for server-authoritative outcomes on Fabric. |
| **Full Bukkit class including optional plugin/event surfaces** | **Partial** (by design; see out-of-scope list). |
| **Cross-play (Geyser/Floodgate)** | **Needs Regression Proof** in production — no client-specific logic; if Bedrock break timing differs, validate in a short session. |

## 3. Files

- **`FabricDoubleDrop`**, **`FabricDoubleDropTrait`**, **`FabricTraitManager`**, **`FabricLevelManager`** (registration order).

## 4. Build evidence (Sprint 22)

- **`./gradlew -PfabricOnly=true :fabric:build`**
