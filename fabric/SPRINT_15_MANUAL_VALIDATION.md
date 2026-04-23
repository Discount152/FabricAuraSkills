# Sprint 15 — Grindstone XP / `GrindstoneLeveler` (manual validation)

**Goal:** XP only on a **real** result-slot take (stack leaves or count drops), with **Bukkit `failsClickChecks`-style** gating: **empty cursor** at click start, **no XP for spectators**, and **no award** for non-takeout actions (e.g. creative **clone**) or **shift/number key/drop**-style takes when **main+hotbar (36) is full** (matches reference `ItemUtils#isInventoryFull` behavior for non-left/right clicks).

| # | Check | Pass criteria |
|---|--------|---------------|
| 1 | Normal take | Disenchant an item, take result with **left click**: one XP event (no double from a single take). |
| 2 | Half / multi-take | **Right-click** (half) or two takes from a stacked result: XP when each take is a real removal (compare to expected config). |
| 3 | Shift with space | **Shift-click** result into a **non-full** inventory: XP when take succeeds. |
| 4 | Shift with full 36 | **36 non-empty** main+hotbar slots, **shift** result: **no** AuraSkills grindstone XP (Bukkit `failsClickChecks` parity for non-left/right + full). |
| 5 | Hotbar with full 36 | Number-key **swap** to hotbar with full storage: **no** XP (same Bukkit rule). |
| 6 | Item on cursor | With non-empty **cursor** stack, click result: **no** XP. |
| 7 | Creative clone | In creative, **pick block / clone** on the result slot: **no** XP (`SlotActionType.CLONE` not in whitelist). |
| 8 | Spectator | **No** `addXp` for grindstone in **spectator** (if the GUI path exists). |
| 9 | `shouldBlockXp` | Blocked world / perms / creative (when config) still block, like other levelers. |
| 10 | Geyser/Floodgate | If available, one disenchant + take on Bedrock client. |

**Gaps (documented, not in default Fabric):** Bukkit `InventoryClickEvent` **isCancelled** if `check_cancelled` on skill; **LevelerContext** world **region** blocks — **N/A** / `shouldBlockXp` and world name only. **`EVT-024` = Partial**.

**Build evidence:** `gradlew :fabric:build` (and `:fabric:test` for `FabricGrindstoneXpHandlerTest`).
