# Sprint 02 — Block loot handler (EVT-005) — manual validation

**Reference:** Bukkit `bukkit/.../loot/handler/BlockLootHandler.java` (rolls on `BlockBreakEvent` at `HIGHEST`; `override_vanilla_loot` uses `setDropItems(false)`).

**Fabric:** `FabricBlockLootHandler` — pool roll in `PlayerBlockBreakEvents.BEFORE`; item/command delivery in `AFTER`. If `override_vanilla_loot` is true for the winning pool, vanilla item entities near the block center are removed in `AFTER`, then skill loot is applied (so custom drops are not deleted with vanilla).

## Config / data

- Skill loot tables under `config/auraskills/loot/` (same family as Bukkit).
- Optional: `loot_directly_to_inventory` (Bukkit telekinesis-style) via `FabricLootManager#toInventory` (main hand only for the check).

## Java (Fabric dedicated server)

1. **Basic roll:** Break a block that matches a `BREAK` block source with a non-empty skill loot table; confirm bonus item or command executes when pool chance succeeds (tune `base_chance` / luck in test data if needed).
2. **Per-skill providers:** For Excavation, confirm `rare` / `epic` pools respect source tags (Metal Detector / Lucky Spades) the same way as on Bukkit. For mining/foraging/farming, confirm non-rare pool `LootDropCause` paths still roll (providers only override `getCause` for the default `SkillLootProvider` branches).
3. **override_vanilla_loot:** Configure a pool with `override_vanilla_loot: true` on a block that normally drops items (e.g. ore). On success, confirm **no** vanilla drops remain and skill loot (item or command) still applies. **Caveat:** removal is a small AABB at block center; unusual drop offsets may differ from Bukkit.
4. **Creative / disabled world:** In creative (when `disable_in_creative` applies) or a disabled/blocked world, confirm **no** skill loot (matches `failsChecks` behavior on Bukkit; Fabric has no WorldGuard `CUSTOM_LOOT` yet).

## Bedrock (Geyser / Floodgate)

- Same server path as Java (`ServerPlayerEntity`). Re-run steps 1–3 on a linked Bedrock account; loot is server-side. **Chat/command** styling for loot messages follows Fabric message resolution (no Java-only client mod required).

## Automated tests

- None added in this module for in-world block break loot (same as other Fabric gameplay handlers). Build: `.\gradlew -PfabricOnly=true :fabric:build`.

## Known differences vs Bukkit

- **WorldGuard** `CUSTOM_LOOT` flag: not applied on Fabric.
- **Slimefun** block check: not applied (no hook on Fabric).
- **Placed block `check_replace`:** `RegionManager#isPlacedBlock` is not implemented; the condition is wired for a future `isPlacedBlock` stub.
- **override_vanilla_loot:** implemented by clearing `ItemEntity` near the break position after vanilla spawn, not by cancelling pre-drop (Fabric has no Bukkit `setDropItems`).
