# Sprint 11 — Brewing `BREW` + `TAKEOUT` (manual validation)

**Goal:** After Sprint 11, **owner / claim** behavior for `BREW` is unchanged; **`TAKEOUT`** tracking is cleared when a result slot (0–2) is emptied by **automation** (e.g. hopper), avoiding stale takeout state after non-GUI extraction.

| # | Check | Pass criteria |
|---|--------|---------------|
| 1 | `BREW` + owner online | Tracked owner (`BlockItem` place or first `onUse` claim), ingredient matches a `BREW` source: XP when the owner is **online**; **no** XP if owner is offline (hopper-only brew is consistent). |
| 2 | `BREW` + unclaimed stand | No owner UUID → no `BREW` XP (same as Bukkit gate on metadata). |
| 3 | `TAKEOUT` — manual take | Take a brewed potion from 0–2 in the stand GUI: XP per YAML; tracking clears after `clearTakeoutTracking()`. |
| 4 | **Hopper pulls potion** | Brew, then let a **hopper** pull a finished potion from a result slot. Open GUI later, brew again, take by hand: **no** spurious TAKEOUT from **old** brew metadata; second takeout behaves correctly for the new brew. |
| 5 | Geyser/Floodgate | If available, repeat **1** and **3** for a linked Bedrock player. |

**Out of reference parity (by design, same as Bukkit):** TAKEOUT XP on **only** `ScreenHandler` slot actions—**no** XP when hoppers or other automation remove items. Sprint 11 only fixes **stale** internal flags after that extraction.

**Build evidence:** `gradlew :fabric:build` (mixin: `LockableContainerBrewingStandSetStackMixin` on `LockableContainerBlockEntity#setStack`).
