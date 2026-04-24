# Sprint 26 — Manual regression matrix (ability lane)

**Server:** dedicated Fabric, **Java + Geyser/Floodgate** for cross-play rows. **Skill levels:** use test profiles or high skill unlocks as needed so abilities are **not** disabled by `failsChecks`.

**Watch for (all sections):** duplicate activations, **mana** desync, **cooldown** stuck/cleared wrong, or **Java vs Bedrock** only repro.

## 1. Farming (priority — GROWTH_AURA tail)

| # | Action | Pass criteria |
|---|--------|----------------|
| 1 | Bone-meal a **wheat** block near max age with **GROWTH_AURA** unlocked | Extra `age` steps can occur; no crash; block eventually matches vanilla-valid states. |
| 2 | **Bamboo** column random tick (or bone meal per S25 doc) with aura | Extra growth can apply; no infinite loop / chunk spam. |
| 3 | **Weeping / twisting vines** with aura | Growth bonus can apply; compare to **without** aura on same Y setup. |
| 4 | **Cactus / sugar cane** | Same as S24/25 validation — one extra stage possible after natural tick. |
| 5 | **Documented not in scope** (optional negative check) | **Chorus** / **tree feature** grow: expect **no** GROWTH_AURA bonus (S25); confirm **no** error. |
| 6 | **Geneticist** | Eat plant-based food; extra saturation when unlocked (with Sprint 17 consume path). |

## 2. Fighting + Bleed

| # | Action | Pass criteria |
|---|--------|----------------|
| 1 | **Bleed** on sword vs mob/player | DoT applies; **no** double application per hit window; **BLEED** messages sane. |
| 2 | **Parry** | Swing sword → take melee hit in window → reduced damage; **PARRY** cooldown not stuck. |
| 3 | **Sword master / first strike** | Modifiers change displayed/computed damage; **FIGHTER** not doubly applied. |

## 3. Mining / Foraging / Excavation (combat-adjacent + tools)

| # | Action | Pass criteria |
|---|--------|----------------|
| 1 | Mining **ore** with pick (relevant **LUCKY_MINER** / tool abilities per config) | XP + luck traits behave; no double break XP. |
| 2 | Foraging **logs** with axe | Same — pipeline modifiers once per break. |
| 3 | **Shovel** excavation block | **Ability** procs not duplicated on tall plants / multi-block if configured. |

## 4. Archery

| # | Action | Pass criteria |
|---|--------|----------------|
| 1 | Bow/crossbow **headshot** or configured ability procs | **FabricDamageMeta** path; no double damage stack from ability layer. |
| 2 | **Bedrock** client bow use | Same XP/damage class as Java (Floodgate identity). |

## 5. Defense

| # | Action | Pass criteria |
|---|--------|----------------|
| 1 | Block with shield + incoming melee | **Defense** path applies; `AFTER_DAMAGE` / **shield** order feels consistent; no invuln exploit. |

## 6. Mana-related abilities (already registered on Fabric)

| # | Action | Pass criteria |
|---|--------|----------------|
| 1 | Cast a **mana ability** (e.g. from `FabricManaAbilityManager` providers) | Mana **deducts once**; **XP** from `FabricManaAbilityUseXpHandler` if configured — no double **XP** tick. |
| 2 | Cooldown + **mana** bar / message | Matches config; no desync after relog. |

## 7. Sign-off

- [ ] **Java** client — sections 1–6 touched.
- [ ] **Bedrock (Geyser/Floodgate)** — sections 1, 2, 4, 5 spot-checks.
- [ ] No **duplicate activation** or **cooldown** regression observed.

**Notes / failures:** (free text for tester)
