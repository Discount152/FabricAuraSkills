# Sprint 26 — Ability lane proof + targeted closure (Granite / Fabric)

**Scope:** In-repo **Bukkit** module is the **reference** only. **Runtime** is **Java Fabric** + **Java and Bedrock** via Geyser/Floodgate. No third-party Bukkit plugin work.

**Baseline:** Sprint 23 ability audit, Sprints 24–25 farming `GROWTH_AURA` work (see `SPRINT_23_ABILITY_PARITY.md`, `SPRINT_24_FARMING_GROWTH_AURA.md`, `SPRINT_25_FARMING_GROWTH_AURA.md`).

## 1. What was re-audited

- `bukkit/.../BukkitAbilityManager#registerAbilityImplementations` vs `fabric/.../FabricAbilityManager#registerAbilityImplementations`: **14/14** one-to-one `*Abilities` class pairs, same order; **Fighting** uses **`FabricFightingAbilities` + `FabricBleedListener`** (bleed is not a separate Bukkit `*Abilities` class).
- **Farming** post-S25: `FabricFarmingAbilities` + `Geneticist` (consume) + `GROWTH_AURA` mixins and bonemeal path — remaining gaps are **documented in Sprint 25** (chorus, full tree grow, some bamboo), not re-derived here.
- **Damage / combat-adjacent** abilities: Bukkit `EntityDamageByEntityEvent` / `DamageEvent` vs Fabric **`FabricDamageMeta`** + **`FabricDamagePipeline`** + contributors (`FabricDamagePipelineContributor` where implemented).

## 2. Classifications (per ability **family**)

| Ability family (Bukkit class) | Parity | Blocker (if not Equivalent) | Notes |
|--------------------------------|--------|-------------------------------|--------|
| `FishingAbilities` | **Partial** | **Needs Regression Proof** | Registration **Equivalent**; per-ability effects split across traits/loot/fishing XP (`FabricFishingAbilities` body empty — logic elsewhere). In-game + Bedrock spot-check. |
| `EnduranceAbilities` | **Partial** | **Needs Regression Proof** | Registration **Equivalent**; thin `FabricEnduranceAbilities` — health/stamina style hooks distributed; proof vs Bukkit. |
| `AgilityAbilities` | **Partial** | **Needs Regression Proof** | Registration **Equivalent**; jump/fall/damage path validation vs Bukkit `PlayerMoveEvent` / fall handling. |
| `AlchemyAbilities` | **Partial** | **Needs Regression Proof** | Registration **Equivalent**; potion/throw paths elsewhere (Sprint 18 splash, etc.). |
| `EnchantingAbilities` | **Partial** | **Needs Regression Proof** | Registration **Equivalent**; table XP in Sprint 14 path; full ability set proof. |
| `HealingAbilities` | **Partial** | **Needs Regression Proof** | Registration **Equivalent**; regen overlap with trait Sprint 20; proof. |
| `ForgingAbilities` | **Partial** | **Needs Regression Proof** | Registration **Equivalent**; anvil/ grindstone sprints; proof. |
| `FightingAbilities` (+ **Bleed**) | **Partial** | **Needs Regression Proof** + some **real tail** in proof | `FabricFightingAbilities` + **`FabricBleedListener`**; parry/sword/DoT in Fabric pipeline. Validate **no double bleed tick**, **cooldown** vs mana where applicable, Java + Geyser. |
| `ArcheryAbilities` | **Partial** | **Needs Regression Proof** | `FabricArcheryAbilities` + pipeline; Bukkit `DamageEvent` order differs — proof. |
| `FarmingAbilities` | **Partial** | **Real implementation gaps (narrow)** + **Needs Regression Proof** | **Geneticist** + broad **`GROWTH_AURA`**: S23–S25. **Still Partial** for documented tails: **chorus**, **structure tree** growth, some **bamboo/edge** cases — see **`SPRINT_25_FARMING_GROWTH_AURA.md`**. **Not Missing** for core crops/columns covered there. |
| `ForagingAbilities` | **Partial** | **Needs Regression Proof** | Pipeline + block break/loot; Bukkit event cancel / plugin = **N/A** on Fabric by default. |
| `MiningAbilities` | **Partial** | **Needs Regression Proof** | Same as foraging. |
| `ExcavationAbilities` | **Partial** | **Needs Regression Proof** | Same. |
| `DefenseAbilities` | **Partial** | **Needs Regression Proof** | `FabricDefenseAbilities` + `ALLOW_DAMAGE` / `AFTER_DAMAGE` hooks; order vs Bukkit. |

### 2.1 `Missing` — none (this audit)

No audited **family** is classed **Missing**: every Bukkit per-skill `*Abilities` group has a Fabric counterpart registered, and there is a **stated** implementation path (class + pipeline / traits / mixins). Remaining work is **proof**, **documented narrow farming tails**, or **out-of-default-scope** (Bukkit-only plugin `DamageEvent` cancel, etc.).

## 3. Blocker type summary

| Blocker | Families affected |
|--------|---------------------|
| **Real implementation gap (still in scope)** | **Farming** only, and **only** the items explicitly **deferred** in `SPRINT_25_FARMING_GROWTH_AURA.md` (chorus, full tree, niche bamboo). Treated as **accepted Partial** until a future sprint; **not** forced in Sprint 26 (brittle / niche). |
| **Lack of proof only** | All families — cross-play, duplicate activation, cooldown/mana, combat math. |
| **Niche / platform** | Bukkit **`DamageEvent`** / **`BlockGrowEvent`** / plugin cancels; **Geyser** client differences — **document**, do not block “Equivalent” for **registration** row AS-019. |

## 4. Sprint 26 implementation decision

- **No additional ability implementation** was merged in Sprint 26: the **highest-confidence remaining gap** after S25 is **farming’s documented tail** (chorus / trees / edge bamboo) — **intentionally not** implemented this sprint (risk vs value; see Sprint 25 doc).
- **Deliverable = proof matrix + manual regression + doc reconciliation** so the lane is **code-bound vs proof-bound** explicit.

## 5. `AS-019` (ledger row) — status recommendation

| Field | Value |
|-------|--------|
| **Registration (`registerAbilityImplementations`)** | **Equivalent** (14/14, order + Fighting bleed wiring). |
| **Per-family runtime behavior** | **Mixed**: **Farming = Partial** with **narrow documented gaps**; others **Partial** pending **Needs Regression Proof**, not “Missing.” |
| **Row AS-019** | **Partial** + **Needs Regression Proof** — **strengthened vs Sprint 23**: the lane is now **matrix-backed**; remaining work is **regression** + optional **future** farming tail, not unknown registration debt. |

## 6. Build evidence

- `.\gradlew -PfabricOnly=true :fabric:build` (Sprint 26)
