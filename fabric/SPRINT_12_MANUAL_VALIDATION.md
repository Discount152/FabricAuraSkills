# Sprint 12 — Mana-ability use XP / `ManaAbilityUseLeveler` (manual validation)

**Goal:** Confirm `mana_ability_use` sources (when present in `sources/*.yml`) award XP = `xp × mana consumed` per ability, and that disabling all such sources in enabled skills stops awards (see `SkillManager#isSourceEnabled`).

| # | Check | Pass criteria |
|---|--------|---------------|
| 1 | Source enabled | With at least one `mana_ability_use` row for e.g. **Speed Mine**: activate, observe XP when mana is consumed (`mana` disabled in config = 0 consumed = 0 XP, same as Bukkit). |
| 2 | Source absent | No `mana_ability_use` in any enabled skill → **no** XP from any mana ability. |
| 3 | **Treecapitator / Terraform / Charged Shot / Absorption** | With matching YAML, each ability grants XP on **successful** activation (after real mana use when mana is on). **Charged shot:** XP on the shot, scaled by **pull**-scaled mana. |
| 4 | No double-award | A single activation does not produce two `addXp` for the same use (e.g. one Treecapitator start). |
| 5 | Geyser/Floodgate | If available, one activation per ability type on a Bedrock-linked player. |

**Not in scope:** a public Fabric replacement for Bukkit’s **`ManaAbilityActivateEvent`** (cancel / `setManaUsed` for other plugins). Cross-play: same `ServerPlayerEntity` path; treat as **needs proof** in ledger optional columns if unverified.

**Build evidence:** `gradlew :fabric:build`
