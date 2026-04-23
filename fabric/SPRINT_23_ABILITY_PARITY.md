# Sprint 23 — Bukkit vs Fabric `AbilityManager` parity (audit + batch 1)

**Runtime:** **Java Fabric** only. **Reference:** `bukkit/.../ability/BukkitAbilityManager.java` and `common/.../AbilityManager` — not Bukkit at runtime.

## 1. Registration set (1:1 class pairs)

`registerAbilityImplementations()` registers **one implementation class per skill** that bundles that skill’s **passive/always-on** abilities. Order on Fabric matches Bukkit (with **`FabricBleedListener.register`** in the `Fighting` slot — bleed is not a separate Bukkit `*Abilities` class; it is the Fabric equivalent of the reference fighting bleed listener wiring).

| # | Bukkit | Fabric |
|---|--------|--------|
| 1 | `FishingAbilities` | `FabricFishingAbilities` |
| 2 | `EnduranceAbilities` | `FabricEnduranceAbilities` |
| 3 | `AgilityAbilities` | `FabricAgilityAbilities` |
| 4 | `AlchemyAbilities` | `FabricAlchemyAbilities` |
| 5 | `EnchantingAbilities` | `FabricEnchantingAbilities` |
| 6 | `HealingAbilities` | `FabricHealingAbilities` |
| 7 | `ForgingAbilities` | `FabricForgingAbilities` |
| 8 | `FightingAbilities` | `FabricFightingAbilities` + **`FabricBleedListener`** |
| 9 | `ArcheryAbilities` | `FabricArcheryAbilities` |
| 10 | `FarmingAbilities` | `FabricFarmingAbilities` |
| 11 | `ForagingAbilities` | `FabricForagingAbilities` |
| 12 | `MiningAbilities` | `FabricMiningAbilities` |
| 13 | `ExcavationAbilities` | `FabricExcavationAbilities` |
| 14 | `DefenseAbilities` | `FabricDefenseAbilities` |

**Diff summary**

| Category | Note |
|----------|------|
| **Present on both** | All 14 groups. |
| **Fabric-only structure** | `FabricAbilityManager#applyAbilityAttackDamage` / `applyAbilityDefenseDamage` for `FabricDamageMeta` (no Bukkit `DamageEvent` — server pipeline). |
| **Batch 1 (this sprint)** | **`FarmingAbilities`**: Bukkit had **`Geneticist`** (consume) and **`GROWTH_AURA`** (`BlockGrowEvent`); Fabric previously had **`SCYTHE_MASTER`** (hoe damage) only. **Implemented:** `Geneticist` + `GROWTH_AURA` core. |
| **`BOUNTIFUL_HARVEST` / `FARMER` in constructor** | Bukkit’s `FarmingAbilities` file does not register listeners for them; **`BOUNTIFUL_HARVEST`** is used from gathering/luck style paths (see **`FabricGatheringLuckTraits`** on Fabric). **No new code** this sprint. |
| **GROWTH_AURA coverage** | **Sprint 23 (batch 1):** `CropBlock` + `NetherWartBlock`. **Sprint 24 (batch 2):** extended to cactus, sugar cane, cocoa, stem, sweet berry, kelp, cave vines, unified **`age`** handling — see **`SPRINT_24_FARMING_GROWTH_AURA.md`**. **Still out / Partial:** bamboo, some bonemeal paths, weeping/twisting vines (mapping), etc. |
| **Multiplayer aura** | Bukkit **loops every player in range** to `handleGrowthAura` (each can roll/schedule). Fabric matches: **each** player may schedule a 50ms task; in dense farms this can re-apply the same block multiple times in one grow tick (same as reference behavior). |
| **Needs regression proof** | Other skills: spot-check in-game; Bedrock for consume + any client-visible effect. |

## 2. Build evidence (Sprint 23)

- `.\gradlew -PfabricOnly=true :fabric:build`

## 3. Parity status recommendation

| Topic | Status |
|-------|--------|
| **14 registration entries** | **Equivalent** to Bukkit’s list. |
| **Farming in-scope batch 1** | **Partial → stronger**; growth aura for **Crop + NetherWart** only. |
| **Row AS-019 overall** | **Partial** (full per-skill proof still **Needs Regression Proof**). |
