# Sprint 130 - Global XP award failure triage + fix

**Version:** 2.3.159

## Global root cause

The global XP outage was caused in the shared gate path:

- `FabricXpHandlerUtil.shouldBlockXp(...)` (introduced in Sprint 129)

When user data was not already present in `UserManager` map, Sprint 129's new null-user branch returned `true` (`user_not_loaded`) and blocked awards before `addXp` for all source handlers.

Because all XP handlers call this gate, the failure appeared across all skills.

## Whether Sprint 129 introduced or exposed it

Introduced as a blocking behavior in Sprint 129:

- previously there was no `user_not_loaded -> block` branch
- Sprint 129 added this safety check and turned a map-miss into a hard block

## XPDIAG evidence model

Diagnostics now provide shared-path evidence:

- gate blocked reasons (`source_type_disabled`, `missing_skill_permission`, etc.)
- gate pass signal (`checks_passed`)
- central award call signal from `FabricLevelManager.addXp` (`addxp_called`)

This gives:

- event reached a handler
- source/gate path reached
- blocked vs allowed reason
- `addXp` invoked evidence

## Shared path fix

1. `FabricUserManager.getUser(ServerPlayerEntity)` now persists created users into `playerDataMap` (not transient-only).
2. `FabricXpHandlerUtil.shouldBlockXp(...)` no longer blocks simply because `getUser(UUID)` missed once:
   - it resolves user lazily via `FabricUserManager.getUser(player)` when needed
   - permission check uses Fabric permissions API directly with default allow (`true`) for parity
3. Diagnostics improved:
   - `checks_passed` in shared gate
   - `addxp_called` in `FabricLevelManager.addXp`

## Skills audited

Shared gate and award path checked for:

- fishing, mining, farming, foraging, excavation
- combat paths (entity/damage/defense/archery)
- statistic, jumping, enchanting, brewing, anvil/grindstone, item-consume, mana-ability-use

All these source handlers converge on the fixed shared gate + level-manager award path.

## Tests

Added/retained helper-level coverage:

- `FabricXpDiagnosticsS129Test` (diagnostic toggle passivity)
- `FabricFishingXpPipelineS129Test` (bundled fishing source config sanity)

Manual runtime validation remains required for full world/event integration.

## Scope confirmation

- `/sources` untouched
- ability lore untouched
- leaderboard untouched
