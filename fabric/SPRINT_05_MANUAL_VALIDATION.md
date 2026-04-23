# Sprint 05 — RegionBlockListener parity subset / placed-block integrity — manual validation

**Reference:** `bukkit/.../region/RegionBlockListener.java` (pistons, sand fall, structure grow, tall-plant + support).

**Fabric:** `FabricRegionBlockIntegrity` + `PistonHandlerRegionMixin`, `FallingBlockEntityRegionMixin`, `TreeFeatureGrowContextMixin`, `TreeFeatureSetBlockMixin`, extended `FabricPlacedBlockBreakHandler`.

## Preconditions

- **`check_block_replace.enabled`** on; a block source with **`check_replace: true`** in skills sources.
- **`regiondata`** path as in Sprint 04 (`config/auraskills/regiondata/...`).

## 1. Pistons

1. Place a **tracked** block (e.g. ore with `check_replace`) in the path of a **pushing** piston. Extend the piston: the placed flag should **move** with the block (breaking the block at the **new** position should still suppress XP/loot if still “placed” in data).
2. **Retract** with **sticky** piston pulling blocks: flags should **follow** moved blocks (multi-segment line). If something looks off, note world + direction (rare edge).

## 2. Falling sand / gravel

1. Place **sand**, **red sand**, or **gravel** with tracking, with **unsupported** block below (air or fluid). When it **falls**, the **start** position should **lose** the placed flag; when it **lands** as the same block type, the **landing** cell should **regain** the placed flag (Bukkit parity).
2. Intercept fall with **cobweb**: timer should **stop** without re-tagging abusively (no infinite poll).

## 3. Tree grow (bone meal / growth)

1. Place **tracked** blocks at the **same Y** as a **sapling** (e.g. decoration in the trunk layer). **Bone meal** the tree: placed flags at that **Y** where the feature **sets** blocks should be **cleared** for those positions (`TreeFeature` path only).

## 4. Tall plants (sugar cane / bamboo / cactus)

1. Stack **placed** sugar cane (or bamboo/cactus). Break the **bottom** block: **only** the **continuous** placed segment **above** matching the same material should **lose** flags (stops at a segment that was never player-placed).

## 5. Support (below / side)

1. Use a block source with **`support_block: below`** (or **`side`**) in YAML. Place the block with tracking, then break its **support** (block **under** or **beside** per type). After a short delay (~1 tick / 50 ms), the **dependent** block should **lose** its placed flag if it **updated** to a different XP source or air (Bukkit `isDifferentSource` behavior).

## 6. Regression: XP / loot / abilities

1. **Normal** break (non-placed): still awards **block XP** and **skill loot** as before.
2. **Placed** block: still **suppresses** base block XP and table loot; **Speed Mine** / **Treecapitator** still respect `isPlacedBlock`.

## Bedrock (Geyser / Floodgate)

- All checks are server-side; optional **smoke** repeat for (1)–(2) on Bedrock.

## Automated tests

- **`FabricTreeGrowContextTest`**: thread-local grow-Y context for tree generation.

## Known gaps / caveats

- **Huge mushroom** and other **non-`TreeFeature`** structure growth: not wired the same as Bukkit **`StructureGrowEvent`** for every feature type.
- **External region plugins** implementing “blocked check replace” like the reference: **out of default Granite scope** (use `check_block_replace.blocked_worlds` and server rules).
- **Piston** logic follows **vanilla** `PistonHandler` moved/broken lists; if a mod changes piston behavior without going through this path, parity is not guaranteed.
