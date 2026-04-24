# Sprint 32 — manual validation (built-in `menus/*.yml` granite)

**Environments:** Fabric dedicated server, **Java** and **Bedrock (Geyser + Floodgate)**. Config under the AuraSkills data folder: **`menus/skills.yml`**, **`menus/stats.yml`** (created from the mod JAR on first run if missing).

## Java

1. Start a server with the mod. Confirm **`menus/`** contains **`skills.yml`** and **`stats.yml`** (or open JAR: same paths under `menus/`).
2. Edit **`granite.title_key`** in **`menus/stats.yml`** to a new key, add that key to **`assets/auraskills/lang/en_us.json`**, run **`/auraskills reload`**, open **`/stats`**: window title should match the new string.
3. Set **`granite.enabled: false`** in **`menus/skills.yml`**, reload, run **`/skills`**: you should see the disabled message; menu does not open.
4. Restore **`enabled: true`**, set **`granite.size.rows`** to **`5`** in either file, reload: check server log for a **size mismatch warning**; menu should still open (6×9 GUI; warn-only).
5. Truncate one YAML to invalid syntax (e.g. delete closing quote), reload: log should show **fallback to defaults**; **Skills/Stats** still open with default titles/behavior.
6. **Regression:** **Skills → Level Progression → Back** and **Stats → Stat Info → Back** (when stat_info enabled) still work.

## Bedrock / Geyser

1. After changing **`granite.title_key`** and reload, open **Stats** on Bedrock: title should follow config (same as Java).
2. With **skills** disabled in YAML, confirm no crash and a sensible result (message or no menu).

## Build

- `gradlew -PfabricOnly=true :fabric:build`
