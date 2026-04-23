# Sprint 14 — Enchanting table XP / `EnchantingLeveler` (manual validation)

**Goal:** Successful **button** enchant (slots 0–2) only; **`exp_requirement`** unit scales by **table level cost** (`enchantmentPower` / Bukkit `getExpLevelCost`); **`enchant_level`** unit scales by **sum of added** enchantment levels; **no** XP in **spectator**.

| # | Check | Pass criteria |
|---|--------|---------------|
| 1 | Successful enchant | Pick a row, enchant: XP once (not when only **hovering** / updating previews). |
| 2 | **`enchant_level` unit** | Enchants that add more total level (e.g. multiple on book in edge cases) scale XP by **delta** in components. |
| 3 | **`exp_requirement` unit** | XP ∝ **level** cost shown for the chosen **row** (1 / 2 / 3), matching the table cost field. |
| 4 | Spectator | In spectator, a successful action path should **not** apply (or table unusable) — at minimum **no** AuraSkills **addXp** for enchanting in spectator. |
| 5 | `shouldBlockXp` | `blocked_worlds` / `disabled_worlds` / perms / creative (when config) still block, like other levelers. |
| 6 | Geyser/Floodgate | If available, one enchant on Bedrock client. |

**Gaps (documented, not in default Fabric):** Bukkit `EnchantItemEvent` **isCancelled** if `check_cancelled` on skill; **LevelerContext** **region** blocks — **N/A** / partial vs `shouldBlockXp` only. **`EVT-023` = Partial**.

**Build evidence:** `gradlew :fabric:build`
