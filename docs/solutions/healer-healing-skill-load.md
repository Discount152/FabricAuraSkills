# Healer requires Healing skill in skills.yml

## Problem
`Abilities.HEALER` is the Healing XP multiplier (`Skills.HEALING`), but `skills.yml` had no `graniteskills/healing` section, so the ability never loaded and `getXpMultiplierAbility()` stayed null.

## Fix
Restore `graniteskills/healing` with `life_essence`, `healer`, and `revival` (enabled). Keep alchemy’s existing `life_steal` / `golden_heart` listings for now.

## Verify
Dedicated-server `healer-probe-result.txt` on `GraniteSkills-2.3.228.jar`: unlock gate 100 XP, L1 110, L2 120.
