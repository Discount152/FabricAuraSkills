# Sprint 19 — Bukkit vs Fabric `TraitManager` parity audit (repo-anchored)

**Runtime:** Java **Fabric** only. **Reference:** in-repo `bukkit` module (`BukkitTraitManager.registerTraitImplementations()`), not a runtime dependency.

**Batch 1 (this sprint):** implement **`EXPERIENCE_BONUS`** on Fabric to match Bukkit `ExperienceBonusTrait` (`PlayerExpChangeEvent` → `event.setAmount((int)(amount * (1 + bonus)))`, disabled world skip).

## 1. Bukkit: registered classes

Source: `bukkit/src/main/java/dev/aurelium/auraskills/bukkit/trait/BukkitTraitManager.java`

| # | Class | Trait(s) / notes |
|---|--------|------------------|
| 1 | `HpTrait` | HP |
| 2 | `HealthRegenTraits` | Health regen (event-driven) |
| 3 | `ManaRegenTrait` | Mana regen |
| 4 | `LuckTrait` | Luck attribute |
| 5 | `DoubleDropTrait` | Double drop (block break / loot) |
| 6 | `AttackDamageTrait` | Attack damage |
| 7 | `ExperienceBonusTrait` | **Vanilla XP point gain** (`PlayerExpChangeEvent`) |
| 8 | `AnvilDiscountTrait` | Anvil `PrepareAnvilEvent` / repair cost |
| 9 | `MaxManaTrait` | Max mana |
| 10 | `DamageReductionTrait` | Damage reduction |
| 11 | `CritChanceTrait` | Crit chance |
| 12 | `CritDamageTrait` | Crit damage |
| 13 | `MovementSpeedTrait` | Movement speed |
| 14 | `GatheringLuckTraits` | Per-skill gathering luck |

**Count:** 14 handler registrations.

## 2. Fabric: registered classes (after Sprint 20; order in `FabricTraitManager` may differ)

Source: `fabric/src/main/java/dev/aurelium/auraskills/fabric/FabricTraitManager.java`

| # | Class | Trait(s) / notes |
|---|--------|------------------|
| 1 | `FabricLuckTrait` | Luck |
| 2 | `FabricHealthRegenTraits` | `HUNGER_REGEN` + `SATURATION_REGEN` (Sprint 20) |
| 3 | `FabricHpTrait` | HP |
| 4 | `FabricMovementSpeedTrait` | Movement speed |
| 5 | `FabricMaxManaTrait` | Max mana |
| 6 | `FabricManaRegenTrait` | Mana regen |
| 7 | `FabricCritChanceTrait` | Crit chance |
| 8 | `FabricCritDamageTrait` | Crit damage |
| 9 | `FabricAttackDamageTrait` | Attack damage |
| 10 | `FabricDamageReductionTrait` | Damage reduction |
| 11 | `FabricAnvilDiscountTrait` | **ANVIL_DISCOUNT** (Sprint 21) |
| 12 | `FabricExperienceBonusTrait` | **EXPERIENCE_BONUS** (Sprint 19) |
| 13 | `FabricGatheringLuckTraits` | Gathering luck |

**Count:** 13 handler groups.

## 3. Direct diff (reference naming)

| Category | Bukkit | Fabric |
|----------|--------|--------|
| **Equivalent / implemented** | `HpTrait`, `LuckTrait`, `ManaRegenTrait`, `AttackDamageTrait`, `MaxManaTrait`, `DamageReductionTrait`, `CritChanceTrait`, `CritDamageTrait`, `MovementSpeedTrait`, `GatheringLuckTraits` | `Fabric*Trait` counterparts registered |
| **Implemented in Sprint 19** | `ExperienceBonusTrait` | `FabricExperienceBonusTrait` + `PlayerEntityExperienceBonusMixin` + `FabricExperienceBonus#scaleForPlayer` |
| **Implemented in Sprint 20** | `HealthRegenTraits` | `FabricHealthRegenTraits` + `HungerManagerHealthRegenMixin` + `FabricHealthRegen` — see **`SPRINT_20_HEALTH_REGEN_PARITY.md`** |
| **Implemented in Sprint 21** | `AnvilDiscountTrait` | `FabricAnvilDiscountTrait` + `FabricAnvilDiscount` + `AnvilScreenHandlerAnvilDiscountMixin` — see **`SPRINT_21_ANVIL_DISCOUNT_PARITY.md`** |
| **Missing on Fabric (reference still has)** | `DoubleDropTrait` | — (reference: block/loot/WorldGuard-style hooks; **default product scope** = no third-party Bukkit stack; see `PARITY_BACKLOG.md` *Out of scope*) |
| **Regression proof** | All trait math + attribute apply/remove paths, login → equip → reload | Spot-check **Sprint 19** in-game: vanilla XP orbs, mining, negative deltas (e.g. enchant cost), disabled worlds |

**Behavioral note:** Bukkit’s `PlayerExpChangeEvent` tracks **point** changes; Fabric hooks **`PlayerEntity#addExperience(int)`** (logical server) so the same **int delta** is scaled once. Code paths that change XP **without** `addExperience` are outside this hook (and may differ from Spigot in edge cases; treat as *Needs Regression Proof* if reported).

## 4. Parity status recommendation

| Row / topic | Status |
|-------------|--------|
| `Traits.EXPERIENCE_BONUS` (batch 1) | **Equivalent** to Bukkit reference for the `PlayerExpChangeEvent` math + disabled world skip (see manual validation) |
| Full `BukkitTraitManager` set | **Partial** — one reference handler group still absent on Fabric (`DoubleDropTrait`; see table) |
| Cross-play (Geyser/Floodgate) | **No extra UI;** server-side math only — **Needs Regression Proof** in production if anything odd is reported for Bedrock clients |

## 5. Build evidence (Sprint 19)

- **`./gradlew -PfabricOnly=true :fabric:build`** — compiles, tests, remaps jar, `verifyFabricReleaseBundle` OK (see chat / CI log).
