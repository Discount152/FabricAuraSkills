# Sprint 79 — Fabric MiniMessage / text rendering (manual validation)

**Build / version:** `projectVersion` **2.3.109**; artifact `fabric/build/libs/FabricAuraSkills-2.3.109.jar`.

**Bug:** Player-facing text showed **literal** MiniMessage tags (e.g. `<green>`, `<bold>`, `<red>20/20`, `<aqua>…`) in **chat** (skill level-up) and **action bar** (health/mana), because Fabric did not parse the same way as Bukkit `TextFormatter`.

## Root cause (in-repo)

1. **`FabricMessageProvider#stringToComponent`** used only **`LegacyComponentSerializer`**. YAML and common code use **MiniMessage**-style and mixed tags; those were not deserialized—tags stayed literal in **`Component`**, then **`FabricTextAdapter`** JSON → **`Text`**, or `Text.literal` paths showed raw characters.
2. **`FabricUiProvider#sendActionBar`** used **`Text.literal(message)`** for strings that still contained MiniMessage or legacy (after `applyFormatting` / `getMsg` paths in **`ActionBarManager`**).
3. **`sendTitle`** incorrectly sent to **chat** via **`Text.literal`**, and did not use the formatting pipeline. Replaced with **`TitleFadeS2CPacket`**, **`TitleS2CPacket`**, **`SubtitleS2CPacket`** + **`FabricTextAdapter.toNativeText`**.

**Fix (aligned with Bukkit `BukkitMessageProvider` + `BukkitUiProvider` intent):**

- **`FabricMessageProvider`:** delegate to **`FabricTextFormatter#toComponent`** / **`toString`** (same as **`BukkitMessageProvider` ↔ `TextFormatter`**).
- **`FabricUiProvider`:** `sendActionBar` / `sendTitle` use **`stringToComponent` + `FabricTextAdapter.toNativeText`**.

**Files changed:** `FabricMessageProvider.java`, `FabricUiProvider.java`; **tests:** `FabricTextFormatterTest.java`.

**Dependencies:** no **`build.gradle.kts`** change (MiniMessage already bundled).

## Affected paths (summary)

| Path | Before | After |
|------|--------|--------|
| **Level-up chat** | `stringToComponent` wrong | `FabricTextFormatter` MiniMessage |
| **Idle / XP / ability action bar** | `Text.literal` | `FabricTextAdapter` + parsed component |
| **Level-up title/subtitle** | wrong (chat) + literal | S2C title packets + adapter |

**Note:** `clearComponentCache()` on message reload; restart or **`/auraskills reload`** so **`MessageProvider`** cache reparses with the new `stringToComponent`.

## Java test steps (operator)

1. **Deploy** `FabricAuraSkills-2.3.109.jar` on disposable **1.21.11** dedicated Fabric.
2. **Level-up:** train a skill until **level-up** message — expect **no** raw `<green>` / `<strikethrough>` / `<bold>` in **chat**; separators and **reset** should look styled, not as angle-bracket text.
3. **Action bar:** enable **idle** / **HP+mana** action bar; expect **red** / **aqua** styling without literal `<red>` / `<aqua>` in the bar.
4. **Title:** on level-up, if titles enabled, expect **title overlay**, not a **chat** line, with formatted text (if present in config).
5. **Command output:** e.g. **`/auraskills help`** — should not regress; **legacy `§` / `&`** in messages should still work via `FabricTextFormatter`.

**Pass / fail (fill when run):**

| Check | Pass | Notes |
|-------|------|--------|
| Level-up chat formatted | | |
| Action bar HP/mana formatted | | |
| Title not sent as plain chat | | |
| No regression on plain string messages | | |

**Bedrock / Geyser:** **not** tested this sprint; cross-play text may still differ (Geyser). Document separately.

## Known limitations

- **`FabricTextAdapter`**: on rare **Gson → `TextCodecs` parse** failures, logs a warning and falls back to `Text.literal` of a **legacy** string from `FabricTextFormatter#toString` (should not contain raw MiniMessage if input was valid).
- **Sprint 78** matrix: **operator re-run** still **pending** for full sign-off.

## Screenshot reference

Sprint 78 / operator captures showing raw tags — use **after** 2.3.109 as **before/after** proof when available.
