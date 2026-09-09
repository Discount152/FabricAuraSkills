# Sprint 136: Fishing XP via FishingBobberEntity#use (no stat mixin)

Version: 2.3.164 (from 2.3.163).

## Crash root cause

Dedicated server mixin apply failed on PlayerEntityIncreaseStatFishingMixin: wrong PlayerEntity#increaseStat descriptor for MC 1.21.11 (fragile stat hook).

## Changes

- Removed PlayerEntityIncreaseStatFishingMixin from auraskills.mixins.json; deleted Java file.
- Awards from FishingBobberEntityMixin at use() RETURN -> FabricFishingXpHandler.onBobberUseReturn; then FabricFishingBobberUseContext.leave and FabricFishingCatchHolder.clear.
- peekCaughtStack() == peek(); take() only for resolve/award path.
- logFishingEntryHead + logFishingEntryResult for xpdiag fishing_entry lines.
- One startup log line in FabricAuraSkills.finishEnable.

## Tests

FabricS136FishingBobberUseHookTest (mixin JSON, API, yaml). ItemStack peek/take without consuming: manual (unit tests lack MC item registry bootstrap).

## Manual validation

Boot OK; xpdiag on/off per sprint checklist; unknown loot -> no_matching_source.

## Scope

/sources, ability lore, leaderboard, GraniteLib, menu layout, farming, vein mining unchanged.
