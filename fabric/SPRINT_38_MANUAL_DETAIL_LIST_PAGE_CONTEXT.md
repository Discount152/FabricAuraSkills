# Sprint 38 — manual validation (list page through **Level Progression** / **Stat Info** **Back**)

**Scope:** From a **paged** **Skills** or **Stats** list, opening a detail view must record the **current 0-based list page**; **Back** (or a YAML-disabled early return to the list) must restore **that** page, not page 0. If the number of list entries or config shrinks so the saved page is out of range, the open path **clamps** to the last valid page (no crash, no wrong item index).

**Runtime:** Fabric **Granite Skills**; **clients:** Java and Bedrock (Geyser + Floodgate) if available.

## Prereq

- Same as Sprint 37: enough enabled skills/stats to require **2+** pages (or a small **`list_end_slot_exclusive`** in **`layout`** for testing).

## Java

1. **Skills → Level Progression → Back:** Open **Skills**, go to **page 3** (or 2+). Open **Level Progression** for a skill on that page. Press **Back**. Confirm the **Skills** title shows the **same** page (e.g. “Page 3 of N”) and the same skills as before.
2. **Stats → Stat Info → Back** (with **`menus.stat_info.enabled`** and detail YAML enabled where applicable): Same as (1) on the **Stats** menu.
3. **Clamp after shrink:** With the player notionally on **page 2**, run **`/auraskills reload`** after changing config/YAML so only **one** page of skills/stats remains. Open **Skills**/**Stats** from a command (default context = page 0) or from detail if you can still reach it; when returning from detail with a **stale** high page, the UI should show **page 1 of 1** (or last valid), not an empty or inconsistent grid.

## Bedrock / Geyser

- Repeat the **Back** cases on a Bedrock client; **Back** and paging arrows should behave like Java for this flow.

## Checklist

| Check | Java | Bedrock |
|-------|------|---------|
| Skills page 2+ → Level Progression → **Back** same page | [ ] | [ ] |
| Stats page 2+ → Stat Info → **Back** same page | [ ] | [ ] |
| Reload / entry shrink → no crash; page clamped | [ ] | [ ] |
