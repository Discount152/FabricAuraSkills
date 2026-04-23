# Sprint 23 — `FabricFarmingAbilities` batch 1 (manual)

## Geneticist (Farming `GENETICIST`)

1. Level Farming enough to have **Geneticist** with a **non-zero** value; ensure ability is not disabled in config.
2. In survival, stand in an **enabled** (non-blocked) world; not creative.
3. Eat **plant-based** food (e.g. **bread** or **apple**): **saturation** should increase by the **ability value / 10** (same formula as Bukkit) on top of normal food. Compare eating **meat** (e.g. cooked beef): no Geneticist extra from this path.
4. **Bedrock (optional):** eat the same item types; server-only math — should match.

## Growth aura (Farming `GROWTH_AURA`)

1. With **Growth aura** leveled, plant **wheat** (or any **`CropBlock`**) in range of your character. Wait for **natural** random-tick age increases (the mixin runs at **`randomTick` TAIL** when the crop’s `age` actually increases — **bonemeal** may use a different code path, so it is a weak signal for this check).
2. In practice: compare a row with a high **Growth aura** player standing nearby to a **control** (no Farming/aura) over **many** growth ticks; extra stages are **probabilistic** — confirm no errors in log and, over time, a visible trend if sample size is large enough.
3. **Nether wart:** repeat in the Nether for **nether wart**; extra age steps can appear (RNG).
4. **Placed in blocked/disabled world** (per `WorldManager` / ability checks): no aura benefit.

## Not covered here (Sprint 23)

- Cactus, sugar cane, melons/pumpkin **stems** — not hooked (see parity doc).
- Bukkit `DamageEvent` plugin pipeline — N/A on Fabric.
