# Sprint 08 — DamageLeveler / combat damage path (manual validation)

**Goal:** Verify player **`DamageXpSource`** and mob **`EntityXpSource` + `DAMAGE`** behavior matches the reference Bukkit `DamageLeveler` / `EntityLeveler#resolveDamager` after Sprint 08 hardening.

| # | Check | Pass criteria |
|---|--------|---------------|
| 1 | Defense + shield | With **`allow_shield_blocking: false`**, no defense XP while **holding block** (even if a hit partially slips through and `AFTER_DAMAGE` does not set `blocked`). |
| 2 | Self-harm | No defense XP for own arrow / own splash potion / own thrown pearl **self** damage. |
| 3 | Cooldown | With a `DamageXpSource` that has **cooldown_ms**, repeated hits in the window do **not** stack XP. |
| 4 | Melee on mob (Fighting) | Sword hits on a **natural** mob award **DAMAGE**-trigger XP; **sweep** still counts (attack/sweep causes in resolver). |
| 5 | Bow / trident on mob | **PROJECTILE** `EntityXpSource` still awards; trident/arrow are `ProjectileEntity`. |
| 6 | Splash / potion on mob | Splash potion (direct entity is `PotionEntity`, not a Yarn `ProjectileEntity`) still awards; with **`source.entity.give_alchemy_on_potion_combat`** and Alchemy **enabled**, **THROWN_POTION** can route to Alchemy per config (compare to same setting on Bukkit). |
| 7 | Spawner | Spawner mobs still use **`spawner_multiplier`** on the **DAMAGE** path. |
| 8 | Bleed | **Bleed** DoT on a mob still attributes **DAMAGE** XP to the bleed applier when the resolver is otherwise generic. |
| 9 | Geyser/Floodgate | If available, repeat **1 / 2 / 4** on a linked Bedrock player. |

**Out of default scope (reference only):** Bukkit `DamageListener` `setCancelled` / `DAMAGE_CORRECT_LAST_DAMAGE` reschedules — not claimed parity here.

**Build evidence:** `gradlew :fabric:build`.
