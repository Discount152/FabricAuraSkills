# Sprint 24 — `GROWTH_AURA` batch 2 (manual)

**Prereq:** `SPRINT_23_MANUAL_VALIDATION.md` (Geneticist + basic growth aura).

## New block types to spot-check (each: survival, world not blocked, ability enabled, player within **radius**)

1. **Cactus** — natural random-tick age on sand.
2. **Sugar cane** — same (water-adjacent).
3. **Cocoa** (on log sides) — pod age 0 → 1 → 2.
4. **Pumpkin / melon stem** — stem `age` toward fruit; RNG-heavy, long sample if needed.
5. **Sweet berry bush** — age toward berries.
6. **Kelp** (underwater column) — any kelp block that only advances `age` in `randomTick` on that pos.
7. **Lush cave — cave vines (head/body)** — berry/length growth; confirm no log errors.

## Excluded from validation here

- **Bamboo** (see parity doc: not mixed in).
- Relying on **bonemeal** for the signal (prefer natural tick in line with `randomTick` inject).

## Regression

- Re-check **wheat** / **nether wart** (Sprint 23 paths) after unified `age` code.
