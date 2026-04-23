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

## 2. Fabric: registered classes (after Sprint 19)

Source: `fabric/src/main/java/dev/aurelium/auraskills/fabric/FabricTraitManager.java`

| # | Class | Trait(s) / notes |
|---|--------|------------------|
| 1 | `FabricLuckTrait` | Luck |
| 2 | `FabricHpTrait` | HP |
| 3 | `FabricMovementSpeedTrait` | Movement speed |
| 4 | `FabricMaxManaTrait` | Max mana |
| 5 | `FabricManaRegenTrait` | Mana regen |
| 6 | `FabricCritChanceTrait` | Crit chance |
| 7 | `FabricCritDamageTrait` | Crit damage |
| 8 | `FabricAttackDamageTrait` | Attack damage |
| 9 | `FabricDamageReductionTrait` | Damage reduction |
| 10 | `FabricExperienceBonusTrait` | **EXPERIENCE_BONUS** (Sprint 19) |
| 11 | `FabricGatheringLuckTraits` | Gathering luck |

**Count:** 11 handler groups.

## 3. Direct diff (reference naming)

| Category | Bukkit | Fabric |
|----------|--------|--------|
| **Equivalent / implemented** | `HpTrait`, `LuckTrait`, `ManaRegenTrait`, `AttackDamageTrait`, `MaxManaTrait`, `DamageReductionTrait`, `CritChanceTrait`, `CritDamageTrait`, `MovementSpeedTrait`, `GatheringLuckTraits` | `Fabric*Trait` counterparts registered |
| **Implemented in Sprint 19** | `ExperienceBonusTrait` | `FabricExperienceBonusTrait` + `PlayerEntityExperienceBonusMixin` + `FabricExperienceBonus#scaleForPlayer` |
| **Missing on Fabric (reference still has; not in batch 1)** | `HealthRegenTraits` | — |
| | `DoubleDropTrait` | — (reference: block/loot/WorldGuard-style hooks; **default product scope** = no third-party Bukkit stack; see `PARITY_BACKLOG.md` *Out of scope*) |
| | `AnvilDiscountTrait` | — (follow-up: `ForgingScreenHandler` / anvil result cost; compare `PrepareAnvilEvent` behavior) |
| **Regression proof** | All trait math + attribute apply/remove paths, login → equip → reload | Spot-check **Sprint 19** in-game: vanilla XP orbs, mining, negative deltas (e.g. enchant cost), disabled worlds |

**Behavioral note:** Bukkit’s `PlayerExpChangeEvent` tracks **point** changes; Fabric hooks **`PlayerEntity#addExperience(int)`** (logical server) so the same **int delta** is scaled once. Code paths that change XP **without** `addExperience` are outside this hook (and may differ from Spigot in edge cases; treat as *Needs Regression Proof* if reported).

## 4. Parity status recommendation

| Row / topic | Status |
|-------------|--------|
| `Traits.EXPERIENCE_BONUS` (batch 1) | **Equivalent** to Bukkit reference for the `PlayerExpChangeEvent` math + disabled world skip (see manual validation) |
| Full `BukkitTraitManager` set | **Partial** — three reference handler groups still absent on Fabric (see table above) |
| Cross-play (Geyser/Floodgate) | **No extra UI;** server-side math only — **Needs Regression Proof** in production if anything odd is reported for Bedrock clients |

## 5. Build evidence (Sprint 19)

- **`./gradlew -PfabricOnly=true :fabric:build`** — compiles, tests, remaps jar, `verifyFabricReleaseBundle` OK (see chat / CI log).
