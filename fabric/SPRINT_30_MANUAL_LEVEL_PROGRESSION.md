# Sprint 30 — manual validation (Skills → Level Progression + clickable menus)

**Scope:** `GraniteClickable9x6Handler` + **Skills** top-slot click → **Level Progression** detail + **Back** to Skills. **Environments:** dedicated Fabric server, **Java** and **Bedrock (Geyser + Floodgate)**.

## Java

1. Run `/skills` (or `/auraskills skills` with permission). Large chest (6×9) opens; books list enabled skills.
2. **Click** the first skill book in the **top** (container) grid. **Level Progression** opens: title includes skill name; lore shows level, XP in this level, progress to next (or “Max level”); **Back** is an arrow at the bottom **center-ish** (slot 49). Player **inventory** still allows normal moves; top grid does not steal items.
3. Click **Back**. **Skills** menu reopens. Repeat for another skill.
4. If a skill line shows “No permission”, clicking that book must **not** open Level Progression.

## Bedrock / Geyser

1. Same steps on a **Bedrock** client through Geyser + Floodgate.
2. Confirm: opening **Skills** and **Level Progression** shows the same chest layout; **click** on a book opens the detail; **Back** returns to **Skills** without a stuck/blank screen. Note any Geyser-specific slot quirks; file issues separately.

## Regression (Stats)

- `/stats` still opens; **top** area remains read-only (no click handler); player inventory works.

## Build (dev)

- From repo root: `gradlew -PfabricOnly=true :fabric:build` (or `:granitelib-fabric:compileJava` + `:fabric:compileJava` if splitting).
