# Sprint 140: Working-vs-broken XP source comparison and mechanism fixes

Version: **2.3.168** (from **2.3.167**).

## Comparison frames used

- Farming: working no-state source (**sugar_cane**) vs broken age-state sources (**wheat/carrots/potatoes**).
- Agility: working **fall_damage** (`damage`) vs broken statistic siblings (`walk/sprint/swim`, `jump` verification path).
- Mining: working standing single-break + closer Foraging/FallingTree-style multi-break vs broken OreHarvester/vein mining extras.

## Farming delta root cause and fix

Root cause: in some runtime orders, captured crop state was already reset (`age=0`) when evaluated for state-gated sources.

Fixes:
- Added earlier pre-break capture at `ServerPlayerInteractionManager#tryBreakBlock` HEAD (`ServerPlayerInteractionManagerTryBreakPreCaptureMixin`).
- `FabricCropPreUseCapture.peek` now accepts short tick drift; `resolveWithSource` returns `capture_source`.
- Harvest diagnostics now include `capture_source` and `block_state_raw`.
- Added compare line `event=xp_source_compare category=farming` when broken age-crop path differs from working no-state reference.

## Agility delta root cause and fix

Root cause: statistic path lacked sufficient delta/match instrumentation and had token normalization drift risk.

Fixes:
- Unified token normalization with `FabricStatisticSourceResolver.normalizeStatisticToken`.
- Added `event=agility_stat_delta` per statistic source with before/after/delta/threshold/converted units.
- Added successful `statistic_resolution` rows when threshold is met and award runs.
- Added compare line `event=xp_source_compare category=agility` (`fall_damage` reference vs statistic source).

## Vein mining delta root cause and fix

Root cause: compensation path could miss slower removal timings; queue/window progression visibility was insufficient to distinguish queue vs removed/match failures.

Fixes:
- Multi-break compensation window extended to 20 ticks.
- Added progression logs: `event=multi_break_job` and `event=multi_break_window_tick`.
- Added `event=multi_break_compare` when mining vein window closes with no awards, to contrast with foraging/falling-tree reference flow.

## XPDIAG improvements

Added:
- `event=xp_source_compare`
- `event=crop_capture_attempt`
- `event=agility_stat_delta`
- `event=multi_break_compare`

Plus existing source-resolution and mutation lines from prior sprints remain.

## Tests updated

- Existing helper tests retained.
- Runtime mechanism fixes are validated mainly through diagnostics and manual matrix because hook-order/timing behavior depends on modded runtime interaction order.

## Manual validation checklist

With `/auraskills xpdiag on`:

Farming:
- sugar cane,
- mature wheat/carrots/potatoes (empty hand + hoe paths).

Agility:
- fall damage,
- walk/sprint/swim threshold crossings,
- jump threshold if configured.

Mining:
- standing single-block mining,
- crouch veinmine ore cluster,
- foraging falling-tree cascade.

Expect valid awards:
- `source_match=true`, `award_called=true`, `state_mutated=true`, `after_skill_xp > before_skill_xp`.

## Known runtime gaps

- Final confirmation of right-click harvest ordering with specific external mod builds remains runtime-dependent and should be verified in the target server modpack.

## Scope confirmation

`/sources` rendering, ability lore, leaderboard, GraniteLib architecture, menu layout unchanged.