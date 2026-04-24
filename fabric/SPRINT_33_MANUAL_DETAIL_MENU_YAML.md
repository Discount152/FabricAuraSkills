# Sprint 33 — manual validation (detail menu YAML: level_progression + stat_info)

**Paths:** server config folder **`menus/level_progression.yml`**, **`menus/stat_info.yml`** (created from the JAR if missing). **`granite.enabled: false`** disables the **detail** screen and returns the player to **Skills** or **Stats** with the standard disabled message.

**Stat Info** also requires **`menus.stat_info.enabled`** `true` in main config (Bukkit parity); YAML `granite` is evaluated after that gate.

## Java

1. Edit **`menus/level_progression.yml`**: set **`granite.title_key`** to a new translation key; add the string to **`en_us.json`**; **`/auraskills reload`**; **Skills** → click a skill → **Level Progression** title should update.
2. Change **`format.back`** / **`format.back_lore`** to custom keys, reload, reopen — **Back** item name/lore follow.
3. Set **`granite.enabled: false`** in **`level_progression.yml`**, reload, open **Level Progression** from **Skills** — should show **built_in_disabled** and return to **Skills** (or open **Skills** on next tick).
4. Repeat 1–3 for **`menus/stat_info.yml`** and **Stats** → **Stat Info** (with **`menus.stat_info.enabled` `true`**).
5. **Bad YAML:** introduce a parse error in one file, reload — log warns; menu should use **code defaults** (opens normally with default keys).
6. **Regression:** **Back** on both detail menus still returns to **Skills** / **Stats**.

## Bedrock / Geyser

- After title/back key changes and reload, open both detail paths on Bedrock; no stuck screens.

## Build

- `gradlew -PfabricOnly=true :fabric:build`
