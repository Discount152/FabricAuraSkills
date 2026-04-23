# Sprint 09 — Fishing XP + fishing loot (manual validation)

**Goal:** After Sprint 09, Bukkit **`FishingLootHandler` (HIGH) → `FishingLeveler` (MONITOR)** order is approximated: bonus `loot/fishing` pools run **before** base fishing XP, and base XP re-matches the catch **after** bonus may replace the hooked item. Open-water and rare/epic behavior align with the reference when configs match.

| # | Check | Pass criteria |
|---|--------|---------------|
| 1 | Base XP after bonus | Reel a normal fish: observe **one** `FishingXpSource` base XP; bonus roll (if any) does not double base XP. |
| 2 | Replaced catch | If bonus **replaces** the hooked `ItemEntity` stack (Bukkit `giveFishingItemLoot`), final base XP (if any) should match a **`FishingXpSource`** for the **new** item, or no XP if nothing matches. |
| 3 | No source match, rare with TH | Catches that do **not** match any `sources/fishing` row (if testable) can still get **rare** pool rolls when **Treasure Hunter** is enabled; **default** pool rows require a matched source (Bukkit: `else if (originalSource != null)`). |
| 4 | Open water | A pool with **`require_open_water: true`** only rolls when `FishingBobberEntity#isInOpenWater` is true (3×3 water per vanilla rules). |
| 5 | Still water | Confined pool: pool with open-water off still rolls. |
| 6 | Geyser/Floodgate | If available, repeat **1** and **4** for a linked Bedrock player. |

**Not claimed in Sprint 09 (Bukkit / reference only):** `PlayerFishEvent.getExpToDrop() == 0` skipped **FishingLootHandler**; Fabric does not gate `tryRollBonusLoot` on vanilla XP orb. **Entity loot** entries in `loot/fishing.yml` — **N/A** (`FabricUnsupportedEntityLootParser`). **LootDropEvent** cancel.

**Build evidence:** `gradlew :fabric:build`
