# Sprint 131 - Mod compatibility XP (Fabric)

Version 2.3.160. Scope: Fabric gameplay XP. Does not change /sources, ability lore, leaderboard, or GraniteLib architecture.

## Mods

- Veinminer / OreHarvester / FallingTree: only the first break may fire PlayerBlockBreakEvents; extras are compensated at end of same tick via FabricModCompatMultiBreakXp.
- RightClickHarvest: FabricCropInteractionHarvestXp awards break-style farming XP after a 1-tick state check; FabricXpHarvestDedupe avoids double award with break.
- Rsponge-style fishing: FabricFishingXpHandler optional minecraft:fishes tag fallback; unknown loot logs reason=no_matching_source when xpdiag is on.

## Config

mod_compat.multi_break_xp (enabled, radius), mod_compat.interaction_harvest_xp.enabled, mod_compat.fishing.tag_fallback_for_fish.enabled in config.yml.

## Manual checks

Use /auraskills xpdiag on. Verify single vs multi break, tree cascade, mature crop break vs right-click, immature no XP, fishing vanilla vs mod fish vs unknown.
