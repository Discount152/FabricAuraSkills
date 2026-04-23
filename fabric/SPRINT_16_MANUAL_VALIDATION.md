# Sprint 16 — Anvil XP / `AnvilLeveler` (manual validation)

**Goal:** `addXp` only when the player **successfully takes** the anvil **result**; left/right + `{repair_cost}` (Bukkit `AnvilView#getRepairCost` / Fabric `getLevelCost`) match a configured **`AnvilXpSource`**, using **pre-consumption** inputs and cost (see `FabricAnvilTakeContext` + `onTake` HEAD capture). **Spectators** get no anvil XP.

| # | Check | Pass criteria |
|---|--------|---------------|
| 1 | Book + book | Combine two **enchanted books**; XP once, multiplier reflects level cost. |
| 2 | Tool + book | Merge book into item; XP when taking result; YAML `left`/`right` filters match. |
| 3 | Repair | Repair with same material; XP when `repair_cost` / multiplier makes sense. |
| 4 | Rename only | Rename with **0** (or free) level cost: still no crash; XP only if a source row matches. |
| 5 | No matching source | Operation that matches **no** YAML anvil row: **no** AuraSkills anvil XP. |
| 6 | Spectator | In spectator: **no** `addXp` for anvil. |
| 7 | `shouldBlockXp` | Blocked world / perms / creative (when config) block like other levelers. |
| 8 | Geyser/Floodgate | If available, one anvil combine on Bedrock. |

**Bukkit gaps (N/A on default Fabric):** `InventoryClickEvent` **isCancelled** if `check_cancelled` on skill; **LevelerContext** world **region** — **N/A** / `shouldBlockXp` only. **Full** `failsClickChecks` (click type, cursor) is on `InventoryClickEvent`; Fabric path is **`onTake`/`onTakeOutput`**, which only runs on a real output take (see Javadoc) — not preview-only.

**Build evidence:** `gradlew :fabric:build`. (No new unit tests: `ItemStack` in test requires an MC bootstrap; see `FabricGrindstoneXpHandlerTest` pattern for slot-only tests.)
