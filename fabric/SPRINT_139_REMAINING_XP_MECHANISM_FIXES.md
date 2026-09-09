# Sprint 139: Remaining XP mechanism fixes from runtime evidence

Version: **2.3.167** (from **2.3.166**).

## Runtime-validated scope

Working and preserved:
- fishing,
- excavation,
- fighting,
- defense,
- standing mining,
- addxp mutation reporting.

Remaining mechanisms targeted:
- age/state crop farming,
- vein/multi-break mining extras,
- agility statistic sources (walk/sprint/swim).

## Root cause and fixes

### Farming crop state (age/state)

Root cause: crop pre-use capture relied on strict same-tick resolution; in some runtime orders the callback read reset/live state (`age=0`) and logged `mature=false`.

Fixes:
- `FabricCropPreUseCapture.peek` now tolerates short tick drift (up to 2 ticks).
- New `FabricCropPreUseCapture.resolveWithSource` returns state + `capture_source` (`pre_use_mixin` or `fallback_live`).
- `FabricCropInteractionHarvestXp` now uses that result and writes detailed diagnostics including `capture_source` and `block_state_raw`.

### Agility statistic mapping

Root cause: custom statistic token normalization still produced unresolved cases for namespaced forms and edge config edits.

Fix:
- `FabricStatisticSourceResolver.normalizeStatisticToken` canonicalizes plain/namespaced stat ids.
- `resolve` and `customStat` paths use normalized token consistently.

### Multi-break mining completion

Root cause: compensation window too short for some crouch/vein miner removal timings; extras could miss the award window.

Fixes:
- Multi-break job window extended from 5 ticks to 20 ticks.
- Added diagnostics rows for queue/execution progression:
  - `event=multi_break_job` (`job_queued=true`),
  - `event=multi_break_window_tick` (`window_tick=<n>`).

## XPDIAG improvements

- Farming harvest rows now include:
  - `capture_source`,
  - `block_state_raw`.
- Multi-break now emits explicit queue/window progression lines in addition to `multi_break_window` summary counters.

## Tests

Added `FabricS139RemainingXpMechanismFixesTest`:
- crop alias family checks for wheat/carrot/potato,
- agility statistic token normalization (`walk/sprint/swim`),
- fishing material matcher namespaced-token guard.

Runtime gaps (manual): full in-world maturity harvesting and crouch-veinmine completion remain operator validated with `/auraskills xpdiag on`.

## Manual validation checklist

With `/auraskills xpdiag on` validate:
- mature wheat/carrots/potatoes,
- sugar cane,
- walk/sprint/swim stat gain,
- fall damage,
- standing mine,
- crouch veinmine cluster.

Expect valid rows: `source_match=true`, `award_called=true`, `state_mutated=true`, `after_skill_xp > before_skill_xp`.

## Scope confirmation

`/sources` rendering, ability lore, leaderboard, GraniteLib, menu layout unchanged.