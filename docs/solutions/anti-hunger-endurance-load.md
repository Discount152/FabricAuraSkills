# Anti Hunger load + prove (AC-ABILITY-ANTI-HUNGER)

## Problem
Anti Hunger was implemented (`PlayerFoodExhaustionMixin` + `shouldIgnoreHunger`) but never loaded: `skills.yml` omitted Endurance, so `Abilities.ANTI_HUNGER` threw "not loaded".

## Fix
Restored `graniteskills/endurance` with anti_hunger/runner/golden_heal/recovery/meal_steal; corrected Agility to sugar_rush/thunder_fall. Added deterministic `shouldIgnoreHunger(player, roll)`.

## Verify
`anti-hunger-probe-result.txt` on GraniteSkills-2.3.225: unlock gate, 5% boundary rolls, 100% exhaustion cancel, 10% scaling → PASS.
