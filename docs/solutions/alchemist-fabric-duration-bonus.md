# Alchemist Fabric duration bonus (AC-ABILITY-ALCHEMIST)

## Problem
Fabric registered Alchemist but did not stamp brew-time duration bonuses or apply them on drink (Bukkit `AlchemyAbilities` parity gap).

## Root cause
`FabricAlchemyAbilities` only hosted Wise Effect / Life Steal hooks; no `duration_bonus` CustomData path or brew/consume wiring.

## Fix
Store `graniteskills:duration_bonus` under `PublicBukkitValues` on brew completion for the stand owner; apply on drink via `FabricItemConsumeHandler`; lore from `AbilityMessage.ALCHEMIST_LORE`.

## Verify
Dedicated-server `alchemist-probe-result.txt` on GraniteSkills-2.3.223: gate below unlock, +108 ticks at level 1, drink 3708, +252 at level 2 → PASS.
