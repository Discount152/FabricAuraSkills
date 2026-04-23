# Sprint 07 — Entity kill / spawner parity (manual validation)

**Goal:** Confirm entity **kill** XP and **mob skill loot** use the same **killer attribution** and (where configured) **damage-cause gating** as the reference Bukkit `EntityLeveler` + `getSource` path, with **spawner multiplier** and **bleed** edge cases.

**Prereq:** `fighting` / `archery` entity sources in default YAML; optional **non-1.0** `spawner_multiplier` in `skills.yml` for a quick spawner test.

| # | Check | Pass criteria |
|---|--------|---------------|
| 1 | Melee kill | Killing a mob with a **sword** awards **Fighting** kill XP and does **not** require creative/exploited path. |
| 2 | Bow / trident / crossbow | Killing with **player-owned projectile** awards kill XP and mob loot (if table rolls) — **no** “only direct attacker” false negative. |
| 3 | Splash / bottle | Kill with **splash** or **lingering** potion direct damage (thrown) attributes to **shooter** for mob loot; kill XP uses **DEATH** + **PROJECTILE** + mapped **DamageCause** (regression: previously only `getAttacker() == player` → missed many thrown entities). |
| 4 | Spawner | Mob from a **vanilla spawner** with `spawner_multiplier < 1` in config shows **reduced** kill XP and damage XP vs same mob from natural spawn. |
| 5 | No double DEATH | Observe (debug / XP messages if enabled) that **one** kill-XP application occurs per death for a source that only has **DEATH** (not a duplicate listener bug). *Configured* DEATH + DAMAGE on **different** source rows is reference behavior, not a bug. |
| 6 | Bleed kill | With **Bleed** active, a kill from **bleed DoT** (last damage `generic` / `CUSTOM`) still credits **Fighting** kill XP and mob loot to the **bleed applier** (Bukkit PDC-style attacker map; see `FabricBleedListener`). |
| 7 | Bedrock / Geyser | If available: same scenarios **1–3** with a Floodgate player — `ServerPlayerEntity` path should match Java. |

**Known / backlog:** Bukkit `EntityTransformEvent` copies **spawner** NBT to **slime splits** and **drowned** conversions. Fabric currently tracks spawner mobs by **UUID** in `FabricSpawnerMobTracker` and does **not** propagate the tag to transform/split children (possible mismatch for rare farm cases). `rosestacker:spawner_spawned` (Bukkit) is **out of default scope** here.

**Build evidence:** `gradlew :fabric:build` (remap + bundle checks as configured).
