# Sprint 24 — `GROWTH_AURA` non-crop / extended `randomTick` coverage

**Prerequisite:** `fabric/SPRINT_23_ABILITY_PARITY.md` (Bukkit `FarmingAbilities` / Fabric `FabricFarmingAbilities`).

## 1. Bukkit reference (what can fire)

Bukkit: `onCropGrowth(BlockGrowEvent e)` (MONITOR) when **natural** growth occurs, then for each **player in radius**:

- Only if `event.getNewState().getBlockData() instanceof org.bukkit.block.data.Ageable`.

So the reference behavior applies to every **Bukkit `BlockGrowEvent` whose new state is `Ageable`**, not only one block class. In practice that includes: **crops, nether wart, cactus, sugar cane, cocoa, stems, sweet berry, kelp, cave vines,** and other `Ageable` block data types that receive this event; it **does not** add extra stages for non-`Ageable` new states (e.g. a block that replaced the space without an age property the same way).

## 2. Fabric (this sprint) — what was already covered (Sprint 23)

- **`randomTick` TAIL** inject with HEAD/TAIL state compare for an increased **`age`** on the same block.
- Mixin on **`CropBlock`**, **`NetherWartBlock`** only.

## 3. Fabric (Sprint 24) — what was added

**Unified logic** in `FabricFarmingAbilities`:

- **`ageDidNotAdvance` / `applyExtraGrowthAfterAura`** use the block’s **integer `"age"`** `IntProperty` (Bukkit `Ageable` equivalent) instead of only `CropBlock` / `NetherWart` branches.
- **`maxAgeForBlockState`**: `CropBlock#getMaxAge`, `NetherWartBlock.MAX_AGE`, else max over `IntProperty.getValues()`.

**Mixin** (`FarmingAgeableRandomTickGrowthAuraMixin` — replaces `CropWartBlockGrowthAuraMixin`):

- **Still mixed in:** `CropBlock`, `NetherWartBlock`
- **Added:** `CactusBlock`, `SugarCaneBlock`, `CocoaBlock`, `StemBlock` (pumpkin/melon stem), `SweetBerryBushBlock`, `KelpBlock`, `KelpPlantBlock`, `CaveVinesHeadBlock`, `CaveVinesBodyBlock`

All listed classes **override** `randomTick(BlockState, ServerWorld, BlockPos, Random)` with the same descriptor so one inject set applies.

## 4. Explicitly not mixed in (documented)

| Item | Reason |
|------|--------|
| **Bamboo** / **Bamboo sprout** | Common growth path places **new block columns**; same-block `age` + TAIL is a weak or misleading match to Bukkit `BlockGrowEvent` for bamboo; would need a dedicated, reviewed hook. |
| **Chorus plant / flower** | End-game; growth model differs. |
| **Weeping / twisting vines (1.21.11)** | No suitable `*VineBlock` public class in Yarn for this project’s mappings (would need mapping-specific follow-up or a `Block` pointcut). |
| **Bonemeal** | Bukkit’s event may fire for some bonemeal growth; this sprint remains aligned with **Sprint 23’s** `randomTick`–only hook (not a full `BlockFertilizeEvent` path). |
| **Sapling / tree / huge mushroom structure growth** | `StructureGrowEvent` / multi-block, not the same as single-block `age` in `FarmingAbilities` on Bukkit for typical field crops. |

## 5. Follow-up (Sprint 25)

See **`SPRINT_25_FARMING_GROWTH_AURA.md`**: bone meal, bamboo, weeping/twisting vines, shared `onGrowthAuraAfterAgeAtPos`.

## 6. Parity view (Sprint 24 snapshot)

| | Status |
|---|--------|
| **Bukkit `GROWTH_AURA` for common field + underwater + lush cave Ageable `BlockGrowEvent` cases** | **Partial → stronger**; major vanilla `Ageable` + `randomTick` block classes are in the mixin list. |
| **Line-for-line with every Spigot `BlockGrowEvent` case** | **Partial** (bamboo, bonemeal nuance, mapping-specific vines, etc.). |
| **Bedrock (Geyser/Floodgate)** | **Needs regression proof** (server math only; no client-only code). |

## 7. Build evidence

- `.\gradlew -PfabricOnly=true :fabric:build`
