# Sprint 141 - Runtime Order and State Proof

## Goal

Stop assumption-driven patching. Add ordered runtime proofs for farming crop mutation timing, agility statistic deltas, and mining multi-break lifecycle, then fix only proven failure points.

## Runtime Evidence Used

- Farming showed `capture_source=pre_use_mixin` with `block_state_raw=...age=0`, so capture timing/order had to be proven.
- Defense/Fighting continued to show `addxp_mutation state_mutated=true`, confirming central addXp path was healthy.
- Vein mining still failed while standing mining worked, indicating multi-break lifecycle-specific failure.

## Changes

### 1) Farming hook-order probes + earliest mature state selection

- Added `event=crop_order_probe` diagnostics with:
  - `probe`, `sequence`, `player`, `pos`, `block_state_raw`, `age`, `hand`, `item`, `rightclickharvest_present`.
- Probe capture points now include:
  - `interact_block_head` (ServerPlayerInteractionManager.interactBlock HEAD mixin)
  - `before_useblock_handler` (FabricBlockInteractHandler callback entry)
  - `use_block_callback` (FabricCropInteractionHarvestXp callback entry)
  - `try_break_head` (ServerPlayerInteractionManager.tryBreakBlock HEAD mixin)
  - `break_before_event` and `break_after_event` (PlayerBlockBreakEvents in FabricBlockBreakHandler)
- `FabricCropPreUseCapture` now stores a short sequence of captures per player+world+pos and resolves using the earliest mature state in the valid window (`*_earliest_mature` capture source) instead of last-write-wins.

### 2) Agility statistic runtime proof + proven fix

- `event=agility_stat_delta` now logs the required fields:
  - `runtime_stat_id`, `source_id`, `before_stat`, `after_stat`, `delta`, `stored_remainder`, `threshold`, `xp`, `source_match`, `award_called`, `reason`.
- Fixed statistic accumulation robustness:
  - Tracker key now uses stable source key (`sourceId|configuredStat`) instead of object identity.
  - Added explicit remainder accumulation per source key so deltas are not lost between polls.
  - Added `computeProgress(...)` helper for deterministic threshold/remainder behavior.

### 3) Vein mining lifecycle proof instrumentation

- Added `event=multi_break_lifecycle` with `phase` values:
  - `entry`, `job_queued`, `tick`, `award`, `complete`.
- Logged required lifecycle fields:
  - `player`, `sneaking`, `initial_block`, `snapshot_count`, `removed_count`, `matched_count`, `xp_awarded_blocks`, `dedupe_skipped_count`, `placed_block_skipped_count`, `reason`.
- Added helper reason classifier `reasonFromCounts(...)` to keep closeout reason mapping explicit and testable.

## Files Changed

- `fabric/src/main/java/dev/aurelium/auraskills/fabric/FabricXpDiagnostics.java`
- `fabric/src/main/java/dev/aurelium/auraskills/fabric/FabricCropPreUseCapture.java`
- `fabric/src/main/java/dev/aurelium/auraskills/fabric/mixin/ServerPlayerInteractionManagerCropPreUseMixin.java`
- `fabric/src/main/java/dev/aurelium/auraskills/fabric/mixin/ServerPlayerInteractionManagerTryBreakPreCaptureMixin.java`
- `fabric/src/main/java/dev/aurelium/auraskills/fabric/FabricCropInteractionHarvestXp.java`
- `fabric/src/main/java/dev/aurelium/auraskills/fabric/FabricBlockInteractHandler.java`
- `fabric/src/main/java/dev/aurelium/auraskills/fabric/FabricBlockBreakHandler.java`
- `fabric/src/main/java/dev/aurelium/auraskills/fabric/FabricStatisticXpHandler.java`
- `fabric/src/test/java/dev/aurelium/auraskills/fabric/FabricS141RuntimeOrderAndStateProofTest.java`
- `gradle.properties`

## Tests Added

- `FabricS141RuntimeOrderAndStateProofTest`
  - crop probe ordering prefers earliest mature capture
  - age=0 behavior when no mature capture exists
  - agility stat delta accumulation and threshold award behavior
  - multi-break lifecycle reason classification

## Manual Validation Checklist

With `/auraskills xpdiag on`:

- Farming:
  - mature wheat/carrot/potato with empty hand and hoe
  - verify `crop_order_probe` sequence and first mature capture point
  - verify `interaction_harvest_probe` reaches `source_match=true`, `award_called=true` on mature states
- Agility:
  - walk, sprint, swim, jump loops
  - verify `agility_stat_delta` shows non-zero `delta`, accumulating `stored_remainder`, and non-zero `xp` once threshold is met
- Mining:
  - standing single-block mining
  - crouch vein mining with OreHarvester
  - verify `multi_break_lifecycle` phases and final `complete` reason/award counts

## Scope Confirmation

- `/sources` rendering untouched.
- Ability lore untouched.
- Leaderboard untouched.
- Fishing path untouched (no fishing logic changes in this sprint).
- Global addXp behavior untouched except diagnostics/log observability context.