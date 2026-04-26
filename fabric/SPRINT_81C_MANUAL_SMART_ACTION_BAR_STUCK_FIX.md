# Sprint 81C — Smart action bar: stop sticky “always-like” display (manual validation)

| Field | Value |
|--------|--------|
| **Sprint 81C status** | **Accepted / validated** (runtime) |
| **Tested artifact** | **`projectVersion` 2.3.114** → `FabricAuraSkills-2.3.114.jar` |
| **Validation date (in-repo)** | **2026-04-21** — operator confirmed; see **Pass / fail** and **Operator result** below. |
| **Depends on** | **81** (smart), **81A** (migration + `/abtoggle` forms), **81B** (first stable-diff guard — **superseded** for remaining stickiness) |

## Final product model (action bar lane — closed)

| Mode | Behavior |
|------|----------|
| **off** | Granite health/mana **hidden** on the idle action bar. |
| **always** | **Persistent** idle health/mana display (legacy-style, when the idle line is active). |
| **smart** | **Temporary** idle health/mana only when **relevant** (visibility windows); no sticky “always-like” hold after triggers once **`SmartIdleObsDisplay`** is in use (**2.3.114+**). |

This closes the **action bar conflict** lane (smart vs Litematica / other mods) for the **Java** Fabric product path in repo.

## Runtime finding (pre-fix vs validated post-fix)

| Mode | Before 81C (bug) | After 81C (validated) |
|------|------------------|------------------------|
| **off** | Hides — mode respected | Hides — unchanged |
| **always** | Stays on — expected | Stays on — unchanged |
| **smart** | Stuck on after health/mana trigger | **Bar appears on trigger, then disappears** after the smart window; Litematica / other action bar text **visible** when Granite is idle; **`/abtoggle status`** remains **Smart** |

**Implication (diagnosis):** The sticky smart bar was a **`visibleUntilMs` extension** issue, not **idle_mode** mutation. **`/abtoggle status`** staying **Smart** while the bar was wrong pointed at **Case B** (visibility), fixed by **81C**.

## Root cause (81C, final)

Sprint **81B** fixed **one** re-extension path: **stable** vs current while already **in** the window. Runtime testing showed the bar could **still** stick. Remaining issues came from the **split model**: separate **`stable*`** and **`inWin*`** baselines, where the **in-window** path could still treat “last frame” vs “current” in ways that re-hit **extend** every tick when combined with `visibleUntil` timing, and from **two** mechanisms fighting over the same clock.

**81C** replaces that with a **single** per-player **last observed** display tuple **`(h, maxH, m, maxM)`** (same integers the idle bar text uses, via `getHp` / `getMaxHp` / `getMana` / `getMaxMana`). **Only when that tuple *changes* from the previous server tick** do we add **`show_after_*_ticks`** to **`visibleUntilMs`**. Identical values on the next tick **do not** extend again. **`SmartIdleObsDisplay`** implements this; **`ActionBarManager#tickSmartIdle`** calls it.

**Config:** Ticks are still **`× 50 ms`** per existing **`MS_PER_TICK`** in **`ActionBarManager`**.

**What 81B still contributed:** Diagnosis that the bug was in **visibleUntil** extension, not mode. The **in-window** branch in pre-81C code is **removed** in favor of the single snapshot.

## Branches (no longer in code)

- ~~**stable* only updates !inWindow**~~ — replaced by **obs*** updated whenever **`SmartIdleObsDisplay.process`** returns a new snapshot (always aligned with the last processed tick after a change, or carried forward when no change).
- ~~**inWin* in-window re-extend**~~ — removed; extension is only from **obs** diff and from **`extendIdleWindow`** (join, world, combat).

## First diagnostic (unchanged from prior sprints)

```text
/abtoggle status
```

- **Smart** + stuck bar before **2.3.114** → visibility logic (this sprint).  
- **Always** when you set Smart → mode mutation (separate bug hunt).  
- **Off** + bar still from Granite → bypass (not seen when Off works).

## Expected behavior (after 81C)

### Smart

- Triggers (level-up, damage, mana use, regen one integer, etc.): bar may show; each **new** distinct displayed 4-tuple can extend the window.
- **Same** 4-tuple on the next **idle** tick: **no** new extension. **`visibleUntilMs`** is **not** pushed forward every 50ms forever.
- After window expires, no idle packets. **Litematica** can use the line when idle.
- **`/abtoggle status`:** **Smart**.

### Always / Off

- Unchanged from product definition.

## Operator validation (Java)

1. Restart; deploy **2.3.114** JAR.
2. Run **`/abtoggle off`** — Granite idle H/M not shown.  
3. Run **`/abtoggle always`** — idle H/M visible on idle path.  
4. Run **`/abtoggle smart`**, then **`/abtoggle status`** — expect **Smart**.  
5. Trigger the health/mana / stat case that used to **stick** the bar.  
6. Wait **longer** than your **`show_after_*_ticks`**-derived duration (e.g. 100 ticks ≈ 5s for a 100-tick setting at 50ms/tick in this code path).  
7. Confirm: bar **stops** without **Off**; **status** still **Smart**; with idle full/stable, Litematica text can show.  
8. Watch **console** for errors.

## Litematica

- After the smart **hidden** state, your tool’s string should be readable when Granite is not sending the idle line.

## Bedrock / Geyser

- **Not** run-time tested for this sprint unless the operator records it. Default **N/A** / **NRP**.

## Pass / fail (operator)

| Check | Result | Notes |
|--------|--------|--------|
| Off / Always / Smart baseline | **Pass** | Per operator sign-off |
| Post-trigger smart hides | **Pass** | After smart visibility window |
| `/abtoggle status` after | **Pass** | Remains **Smart** |
| Litematica idle | **Pass** | Other action bar text when Granite idle |
| Sticky bar resolved | **Pass** | **81C** accepted |

## Operator result (in-repo)

| Field | Value |
|--------|--------|
| **Result** | **Pass** — Sprint **81C** runtime **accepted** |
| **Confirmed** | Smart: bar on when triggered, off after window, Litematica when idle; **off** / **always** as expected. |
| **Bedrock** | **Not** validated this round (unchanged) |

## Remaining limitations

- **Rapid** oscillation of *displayed* integers (e.g. 20↔21 every tick) would still extend each time the tuple changes; that is a **separate** display/rounding issue, not the “same state rediscovered from stable” bug.  
- Non-EN locale gaps for `toggle.*` (same as 81A) still possible.

## Next steps (out of band for 81C)

- **Java smoke matrix:** continue from **`SPRINT_80_JAVA_TEXT_SMOKE_RESULTS.md`** / **`SPRINT_78`** minimum set on current artifacts.  
- **Bedrock / Geyser:** plan a **dedicated** validation sprint when ready — **81C** proof is **Java-only** in this doc.
