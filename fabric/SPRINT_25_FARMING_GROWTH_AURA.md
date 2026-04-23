# Sprint 25 — `GROWTH_AURA` tail: bonemeal, bamboo, vines (closure)

**Builds on:** `SPRINT_24_FARMING_GROWTH_AURA.md`, `FarmingAbilities` Bukkit reference (`BlockGrowEvent` + `Ageable` only).

## 1. Bukkit reference (unchanged)

`handleGrowthAura` only runs for `newState` that is Bukkit `Ageable`. That can come from **natural** `BlockGrowEvent` or from **fertilize**-style `BlockGrowEvent` when Spigot fires it for the **same** `Ageable` new state on the event block (e.g. bone meal on crops).

## 2. What Sprint 24 still left

| Topic | S24 state |
|-------|-----------|
| **Bonemeal** | `randomTick` only; many crop/stem age advances are **not** from `randomTick` in the same moment as player bone meal. |
| **Bamboo** | Omitted from mixin (column growth concern). |
| **Weeping / twisting** | Wrong Yarn class names in an earlier attempt (`*VineBlock` singular; correct is **`WeepingVinesBlock`**, **`TwistingVinesBlock`**, and **`*PlantBlock`**). |

## 3. Fabric Sprint 25 — implemented

| Change | Role |
|--------|------|
| **`onGrowthAuraAfterAgeAtPos`** in `FabricFarmingAbilities` | Shared entry for any **same-position** `age` increase with **same** block class. `onNaturalAgingAfterRandomTick` delegates to it. |
| **`BoneMealItemGrowthAuraMixin`** | `useOnBlock` **HEAD** captures `BlockState` at `ItemUsageContext#getBlockPos` on `ServerWorld`; **TAIL** reads state again, calls `onGrowthAuraAfterAgeAtPos` if `age` advanced. Skips when block type changes (dirt to grass, tree growth, etc.). |
| **`FarmingAgeableRandomTickGrowthAuraMixin`** | Adds **`BambooBlock`**, **`WeepingVinesBlock`**, **`WeepingVinesPlantBlock`**, **`TwistingVinesBlock`**, **`TwistingVinesPlantBlock`**. Still uses `randomTick` HEAD/TAIL. |

**Bamboo note:** In-place `age` steps on a bamboo block (when vanilla only advances `age` at that `pos`) match the same `age` pipeline. When vanilla **places a new block above** on the same tick, the **growing** position may be the new block, not a same-tick `age` step on the old block; those cases are **not** guaranteed to match Bukkit, but the common **randomTick** in-place `age` path is covered.

**Bonemeal + randomTick in one tick:** Theoretically both could run; Bukkit can emit two `BlockGrowEvent`–style cases; we do not dedupe. Low practical risk.

## 4. Explicitly still out of scope (final)

| Item | Rationale |
|------|-----------|
| **Chorus plant/flower** | End-game, different model; not “farming” baseline. |
| **Sapling / tree / structure growth** | Multi-block, not a single `age` on one `pos` the way field crops are. |
| **Every** bone-meal edge case (e.g. some modded blocks) | Only vanilla `BoneMealItem#useOnBlock` path is targeted. |
| **Bamboo column growth** (new segment **above** without same-block `age` on clicked pos) | May still not trigger aura on the **old** pos; Bukkit `BlockGrowEvent` semantics vary; **Partial** by design. |

## 5. Parity view

| | |
|--|--|
| **Bukkit `GROWTH_AURA` for common `Ageable` + field crop parity** | **Partial → much closer**; bonemeal + randomTick + key blocks covered. |
| **Bit-for-bit all Spigot `BlockGrowEvent` sources** | **Partial** (see §4). |
| **AS-019** | **Partial** + **Needs regression proof** (in-game, optional Bedrock). |

## 6. Build evidence

- `.\gradlew -PfabricOnly=true :fabric:build`
