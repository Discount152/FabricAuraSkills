# Sprint 10 — Statistic XP / `StatisticLeveler` mapping (manual validation)

**Goal:** After Sprint 10, `statistic` sources in `sources/*.yml` resolve to the intended vanilla **stats** for aggregate combat stats (`mob_kills`, `damage_dealt`, …), per-entity **killed** / **killed_by**, and item **dropped**, without mis-mapping. Poll cadence, `minimum_increase`, and skill XP math are unchanged (same as Bukkit timer leveler pattern).

| # | Check | Pass criteria |
|---|--------|---------------|
| 1 | `custom/mob_kills` or bare `mob_kills` | Kill a few mobs: XP only when the configured stat source’s skill should gain, and the increment tracks **mob_kills** (not a silent null stat). |
| 2 | `custom/damage_dealt` | Deal damage: XP accrues in proportion to `damage_dealt` delta × configured multiplier. |
| 3 | `killed/minecraft:zombie` (or another type) | Kills of that type only: stat resolves to per-type **kills**; other entity kills should not move this source’s tracker (unless you add another source). |
| 4 | `killed_by/minecraft:skeleton` | Deaths **to** skeletons increment this stat; verify in-game and that mapping does not throw (invalid `minecraft:` ids are rejected — no stat, no XP). |
| 5 | `dropped/minecraft:rotten_flesh` | Drop that item: XP when `Stats.DROPPED` for that item increases. |
| 6 | Geyser/Floodgate | If available, repeat **1** and **2** for a linked Bedrock player (same `ServerPlayerEntity` stat handler path). |

**Not automated:** `:fabric` unit tests run **without** a bootstrapped Minecraft registry (see `FabricModuleSanityTest`); **resolution** of `Registries.ENTITY_TYPE` / items is **manual** or future game-test / server smoke.

**Intentional product note:** A server can still configure both an **entity `DEATH`** / **`DAMAGE`** leveler and a **statistic** source for overlapping behavior (e.g. `mob_kills` + kill XP). The mod does not deduplicate; avoid double XP in YAML.

**Build evidence:** `gradlew :fabric:build`
