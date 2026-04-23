# Sprint 04 — Placed-block tracking / `check_replace` — manual validation

**Reference:** Bukkit `BukkitRegionManager` + `RegionBlockListener` (place + chunk load + save timer + break remove). See `bukkit/.../region/`.

**Fabric:** `FabricRegionManager` + `FabricRegionLifecycle` (chunk load + 5m async save) + `BlockItemPlacedBlockMixin` + `FabricPlacedBlockBreakHandler`. Data: `config/auraskills/regiondata/<world>/r.<x>.<z>.asrg` (same as Bukkit).

## Config

- **`check_block_replace.enabled`** (global) — `Option.CHECK_BLOCK_REPLACE_ENABLED`.
- **`check_block_replace.blocked_worlds`** — worlds where placement is **not** recorded (Bukkit `RegionBlockListener#checkPlace`).

## Java (Fabric dedicated server)

1. **Record + suppress:** With `check_replace: true` on a block source, place a matching block (e.g. ore), break it: **no** base block XP and **no** skill table loot. Confirm `regiondata` gains entries after place (may be async for new regions).
2. **Lucky Miner / direct ore:** For ores that drop minerals without silk (see Bukkit / `FabricMiningAbilities#dropsMineralDirectly`), breaking a **player-placed** ore should still run **gathering luck** / direct-mineral behavior, but **not** award base `BlockXpSource` XP (Bukkit `BlockLeveler` parity).
3. **Tall plants / sugar cane:** With tracking active, `FabricBlockLevelerHelper` multipliers for cane/bamboo/cactus/kelp should respect placed segments (compare to Bukkit `BlockLevelerHelper` expectations).
4. **Speed Mine / Treecapitator:** When `CHECK_BLOCK_REPLACE_ENABLED` is on, player-placed blocks in-range should suppress activation per existing checks (now use `isPlacedBlock`).
5. **World list:** Add your test world to `check_block_replace.blocked_worlds`, place+mine in that world: placement should **not** be tracked (no new suppression from mod data in that world).
6. **Persistence:** Place blocks, `stop` server, restart, break same area: if chunks reload `regiondata`, suppression should still apply (same file format as Bukkit).

## Bedrock (Geyser / Floodgate)

- Placement/break are server-side; no separate client path. Re-run (1) on a linked Bedrock account if you use permission splits.

## Automated tests

- None added for region NBT in `:fabric` (in-world and disk I/O heavy).

## Gaps vs Bukkit `RegionBlockListener` (intentional / backlog)

- **Pistons** (extend / retract) updating placed flags.  
- **Falling** sand/gravel re-tag after landing.  
- **Structure grow** (trees) removing placed at grow Y.  
- **Tall-plant** chain removal above a broken block (Bukkit `checkTallPlant`) and **support** tick cleanup (`checkSupportBelow` / `checkSupportSide`).

When those matter for your pack, treat as a follow-up sprint to extend `FabricPlacedBlock*` handlers.
