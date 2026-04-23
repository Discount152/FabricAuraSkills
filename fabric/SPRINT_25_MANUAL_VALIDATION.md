# Sprint 25 — `GROWTH_AURA` batch 3 (manual)

**Prereq:** Sprints 23–24 + `SPRINT_24_MANUAL_VALIDATION.md` / `SPRINT_25_FARMING_GROWTH_AURA.md`.

## A. Bone meal (same as random tick, `age` at target pos)

1. **Wheat / beetroot** (or other crop) not fully grown: use bone meal on the crop block. With **Farming** + **Growth aura** in range, you should not see log errors; extra `age` may apply after 50ms (RNG).
2. **Dirt to grass** (or sapling to tree): no extra aura from the **dirt to grass** or **tree** path (block type or structure changes).
3. **Cocoa** on a log: bone meal a young pod; `age` should be able to advance; verify no spam errors.

## B. Random tick (regression of S24 + new blocks)

1. **Bamboo** — stand in range, let bamboo get random-tick `age` increases where only `age` changes on the same block (where applicable in vanilla).
2. **Weeping / twisting vines (Nether)** — natural `age` growth in range; no errors.

## C. S24 regression

Re-check **cactus**, **sugarcane**, **kelp**, **cave vines** briefly after unified entrypoint.

## D. Geyser/Bedrock (optional)

All logic is server-side; if anything client-specific appears, file separately.
