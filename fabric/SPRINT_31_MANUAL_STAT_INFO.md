# Sprint 31 — manual validation (Stats → Stat Info)

**Environments:** Fabric dedicated server, **Java** and **Bedrock (Geyser + Floodgate)**. Config: **`menus.stat_info.enabled`** `true` for interactive Stats (default matches reference intent; if `false`, Stats remains read-only and this sprint’s click path is off).

## Java

1. Set **`menus.stat_info.enabled`** to **`true`** (if not already).
2. Run **`/stats`** (or **`/auraskills stats`**) with permission. Large chest opens with stat books.
3. Verify lore includes **“Click: stat details”** on each stat line when stat_info is enabled.
4. **Click** a stat book in the **top** grid. **Stat Info** opens: title and item show the stat; lore includes value/level, description (possibly wrapped), and trait lines where applicable; **Back** (arrow, slot 49) returns to **Stats**.
5. Set **`menus.stat_info.enabled`** to **`false`**, re-run `/stats`. **No** “Click: stat details” line; top grid behaves **read-only** (Bukkit `StatsMenu` gate parity).
6. **Regression (Sprint 30):** `/skills` → click skill → Level Progression → **Back** → `/stats` or parallel **Stats** flow unchanged.

## Bedrock / Geyser

1. Repeat steps 2–4 on a **Bedrock** client. Note any click/slot or title quirks; file issues separately.
2. With **`menus.stat_info.enabled`** `false`, confirm Stats still opens and does not mis-route clicks (no handler; top slots inert as before).

## Build

- `gradlew -PfabricOnly=true :fabric:build`
