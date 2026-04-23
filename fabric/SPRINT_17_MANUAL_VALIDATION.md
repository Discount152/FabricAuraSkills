# Sprint 17 — Item consume XP / `ItemConsumeLeveler` (manual validation)

**Goal:** `addXp` only on a **real** item consumption **commit** (food, potion, milk, etc.), using the **pre-commit** `ItemStack` for YAML matching (same as Bukkit `getItem()`), with **no** consume XP in **spectator** and **no** work when the whole source type is disabled.

| # | Check | Pass criteria |
|---|--------|---------------|
| 1 | Configured item | With an `item_consume` row in skills YAML, eat/drink: **one** XP per completion (not on starting to eat in survival). |
| 2 | No row | Consuming an item with **no** matching `ItemConsumeXpSource`: **no** consume XP. |
| 3 | Spectator | In spectator, consuming (if the game allows): **no** AuraSkills `addXp` for `ITEM_CONSUME`. |
| 4 | Type disabled | With **`ITEM_CONSUME`**-using skills or sources disabled: **no** consume XP. |
| 5 | `shouldBlockXp` | Blocked world / perms / creative (when config) still block. |
| 6 | Geyser/Floodgate | If available, one food/potion consume on Bedrock. |

**Bukkit gaps (N/A on default Fabric):** `PlayerItemConsumeEvent` **isCancelled** if `check_cancelled` on skill; **LevelerContext** world **region** — **N/A** / `shouldBlockXp` only. The reference leveler has **no** explicit `disabled()` call; Fabric uses **`isSourceEnabled(ITEM_CONSUME)`** + `shouldBlockXp` for a cleaner gate. **`EVT-026` = Partial**.

**Build evidence:** `gradlew :fabric:build` (and `:fabric:test`).
