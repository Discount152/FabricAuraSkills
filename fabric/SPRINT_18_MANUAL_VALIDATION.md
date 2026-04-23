# Sprint 18 — Potion splash XP / `PotionSplashLeveler` (manual validation)

**Goal:** `addXp` only when a **thrown** potion (splash or **lingering**) **collides** (see `PotionEntity#onCollision` hook), the **shooter** is a **`ServerPlayerEntity`**, the stack has **at least one** potion effect (Bukkit: non-empty `getEffects()`; Fabric: not `lacksPotionEffectForParity`), a **matching** `PotionSplashXpSource` exists, and **spectator** / disabled source type / `shouldBlockXp` do not apply.

| # | Check | Pass criteria |
|---|--------|---------------|
| 1 | Splash potion | Throw splash potion at block/entity with a matching stack in YAML: **one** XP per throw/collision, not on merely drawing arm back. |
| 2 | Lingering | Throw **lingering** potion: **one** award at **PotionEntity** collision (cloud tick damage is a separate system). |
| 3 | No effects | Potion with **no** effects in components (e.g. edge/special cases): **no** splash XP. |
| 4 | No source row | Thrown item matches **no** `PotionSplashXpSource` filter: **no** XP. |
| 5 | Non-player | Dispenser, witch, or no owner: **no** XP. |
| 6 | Spectator | In spectator, throw if the game allows: **no** `addXp` for `POTION_SPLASH`. |
| 7 | Type disabled | With potion-splash sources disabled: **no** XP. |
| 8 | `shouldBlockXp` | World / perms / creative (when config) still block. |
| 9 | Geyser/Floodgate | If available, one splash on Bedrock. |

**Bukkit gaps (N/A on default Fabric):** `PotionSplashEvent` / `LingeringPotionSplashEvent` **isCancelled** if `check_cancelled`; **LevelerContext** world **region** — **N/A** / `shouldBlockXp` only. **`EVT-027` = Partial**.

**Build evidence:** `gradlew :fabric:build` (and `:fabric:test`).
