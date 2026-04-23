# Sprint 13 — Jumping XP / `JumpingLeveler` (manual validation)

**Goal:** `jumping` source XP still follows **interval** × vanilla jump-impulse matching; Sprint 13 adds **shared** `shouldBlockXp`, **no XP in spectator**, and **no count while climb-holding** (vines/ladders scaffolds) where `isClimbing` is true.

| # | Check | Pass criteria |
|---|--------|---------------|
| 1 | Normal jumps | With a `jumping` source and interval **N**: after **N** valid jump impulses, one XP award; **not** on every tick while mid-air. |
| 2 | **Spectator** | In `/gamemode spectator`, jumping motion does **not** grant `jumping` XP. |
| 3 | **Climbable** | Climbing a **vine** / **ladder** / **scaffold** (when `isClimbing` is true) does **not** increment the jump counter, even if vertical velocity momentarily looks jump-like. |
| 4 | Ladder at feet | Stand with feet-in-ladder block (reference rule): no jump count (unchanged). |
| 5 | World / perms / creative | Matches other sources: `blocked_worlds` / `disabled_worlds`, no skill permission, creative when `disable_in_creative_mode` → no XP. |
| 6 | Geyser/Floodgate | If available, repeat **1** for a linked Bedrock player. |

**Out of default Fabric parity (reference may differ):** Bukkit `LevelerContext` world **region** plugin checks; `PlayerMoveEvent` **isCancelled** if `check_cancelled` (jumping source option). `EVT-022` = **Partial**.

**Build evidence:** `gradlew :fabric:build`
