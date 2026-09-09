# Sprint 129 - XP award pipeline audit + fishing fix (Fabric)

**Version:** 2.3.158

## Fishing root cause

Fishing XP was gated in `FishingBobberEntityMixin` by:

- `stat.equals(Stats.CUSTOM.getOrCreateStat(Stats.FISH_CAUGHT))`

On Fabric/Yarn runtime this equality is not reliable for the wrapped stat instance identity path used in `FishingBobberEntity#use`, so valid fish-caught events could fail the check and short-circuit XP.

### Fixed path

- Hook remains `FishingBobberEntityMixin#auraskills$afterFishStat` (redirect around `PlayerEntity#increaseStat` inside `FishingBobberEntity#use`).
- New guard uses value comparison in `FabricFishingXpHandler.isFishCaughtStat(Stat<?>)`:
  - `Stats.FISH_CAUGHT.equals(stat.getValue())`

This keeps Fabric behavior aligned with AuraSkills Bukkit `FishingLeveler` intent: award on valid fish-caught event once a catch item is resolved and matched.

## Runtime pipeline (Fishing)

1. Player reels catch -> vanilla `FishingBobberEntity#use`
2. Mixin captures caught stack (`FabricFishingCatchHolder`) and observes stat increment
3. Stat check (`isFishCaughtStat`) gates only fishing-caught path
4. `FabricFishingXpHandler.findFishingSource` resolves configured fishing source from catch item
5. Bonus loot roll (`FabricFishingLootHandler.tryRollBonusLoot`) runs before final XP resolution
6. Post-loot catch stack is re-read and source matched again
7. XP gates: source enabled, world/permission/creative checks (`FabricXpHandlerUtil.shouldBlockXp`)
8. Award call: `LevelManager.addXp(user, skill, source, source.getXp())`
9. User XP/level update + UI feedback handled by common `LevelManager`
10. Persistence follows normal user save lifecycle

## AuraSkills comparison summary

- Bukkit reference: `FishingLeveler` listens to `PlayerFishEvent` `CAUGHT_FISH`, resolves item source, runs leveler checks, then awards XP.
- Fabric equivalent: fishing bobber mixin around stat increment + captured caught `ItemStack`, source lookup, and same level-manager award.
- Bonus-loot ordering remains parity-oriented (loot handler before base award resolution).

## All-skills audit summary

Audited active XP entry paths:

- `BLOCK` (`FabricBlockBreakHandler`, `FabricBlockInteractHandler`, `FabricBlockLootHandler`)
- `ENTITY`/combat (`FabricEntityKillHandler`, `FabricCombatXpHandler`, `FabricMobLootHandler`)
- `DAMAGE` (`FabricCombatXpHandler`)
- `FISHING` (`FishingBobberEntityMixin`, `FabricFishingXpHandler`, `FabricFishingLootHandler`)
- `STATISTIC` (`FabricStatisticXpHandler` + `FabricStatisticSourceResolver`)
- `JUMPING` (`FabricJumpingXpHandler`)
- `ITEM_CONSUME` (`FabricItemConsumeHandler`)
- `POTION_SPLASH` (`FabricPotionSplashHandler`)
- `ANVIL` (`FabricAnvilXpHandler`)
- `GRINDSTONE` (`FabricGrindstoneXpHandler`)
- `ENCHANTING` (`FabricEnchantingXpHandler`)
- `BREWING` (`FabricBrewingXpHandler`)
- `MANA_ABILITY_USE` (`FabricManaAbilityUseXpHandler`)

Findings:

- Fishing had the concrete stat-identity comparison failure above.
- Other audited paths use source lookup + `shouldBlockXp` + `addXp` flow and did not show the same stat-comparison bug pattern.

## XP diagnostics

Added operator-safe runtime toggle:

- `/auraskills xpdiag on`
- `/auraskills xpdiag off`
- `/auraskills xpdiag status`

When enabled, diagnostics log XP decisions with:

- event
- player
- skill
- source type
- source id (if known)
- xp value
- `award_called` true/false
- reason

Implemented via:

- `FabricXpDiagnostics`
- `FabricXpDiagnosticsCommands`
- `FabricXpHandlerUtil` block-reason logging
- `FabricFishingXpHandler` decision logging for source match/award path

## Tests

Added:

- `FabricFishingXpPipelineS129Test` (bundled fishing source YAML sanity coverage)
- `FabricXpDiagnosticsS129Test` (diagnostic toggle behavior)

Limitations:

- Full in-game fishing reel integration test is not available in current unit harness.
- Verified by compile/test + operator manual checklist.

## Scope confirmation

- `/sources`: untouched
- Ability lore pipeline: untouched
- Leaderboard: untouched
