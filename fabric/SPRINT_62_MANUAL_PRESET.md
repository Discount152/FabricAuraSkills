# Sprint 62 — manual validation: `/preset` (bounded list / info / load)

**Build:** **≥ 2.3.90** (see `gradle.properties` → `projectVersion`).

**Scope:** Fabric **Granite Skills** dedicated server. Presets are **`presets/<name>.zip`** archives with a root **`preset.yml`** listing **`file_version: 1`** and **`files:`** entries (see common **`PresetManager`**). This sprint **does not** implement **StorageCommand**, preset **authoring** in-game, or remote download.

## Command roots

- **`/preset …`** (top-level)
- **`/auraskills preset …`**
- **`/skills preset …`**
- **`/sk preset …`**
- **`/skill preset …`**

## Permission

- **`auraskills.command.preset`** — **list**, **info**, **load** (default **deny** / op-style with fabric-perms or LuckPerms).

Reference **`plugin.yml`** defaults **true** for **`auraskills.command.preset`**; Fabric uses **deny** by default — document when granting access.

## Subcommands

| Subcommand | Behavior |
|------------|----------|
| **`list`** | Read-only. Lists **`*.zip`** basenames in **`presets/`**, A–Z, capped (see chat truncation line). |
| **`info <name.zip>`** | Read-only. Shows planned **append/merge/replace** lines from the zip’s **`preset.yml`** (no writes). |
| **`load <name.zip>`** | Preview. Shows **`commands.preset.load.confirm`** (from **`messages_*.yml`**) + Granite line to re-run with **`confirm`**. **No** mutation. |
| **`load <name.zip> confirm`** | **Pre-apply** **`BackupProvider#saveBackupSync(true)`** (UserState safety snapshot). If **skipped** (**max_users**) or **fails** → **abort**. Then **`PresetManager#loadPreset`**, then **`reloadFromDisk()`**. |

## Valid / invalid names

- **OK:** `legacy.zip` (if present on disk and valid zip layout).
- **Rejected:** `../x.zip`, `a/b.zip`, `x.yml`, `.hidden.zip`, `C:\path.zip` style tokens, empty string, embedded **NUL**.

## Java dedicated server

1. Place a valid **`legacy.zip`** (or test zip) under **`config/auraskills/presets/`** (or your configured data dir’s **`presets/`**).
2. Grant **`auraskills.command.preset`**.
3. **`/preset list`** — see the zip name in sorted order.
4. **`/preset info legacy.zip`** — read-only entry list; **no** file changes.
5. **`/preset load legacy.zip`** — preview only.
6. **`/preset load legacy.zip confirm`** — expect pre-backup, apply, reload feedback (or a clear **skip**/**error**).

## Console / RCON

- Same commands. **All** use the **`confirm`** literal (stricter than reference Bukkit, where the console can skip the ACF “type twice” gate).

## Bedrock / Geyser

- Chat-based; expect similar text. **Do not** claim “Bedrock tested” without on-device proof (see **`FABRIC_PORT_HANDOFF.md`** / product rules).

## Pre-apply backup

- **UserState** snapshot via **`BackupProvider`** into **`backups/*.yml`** — **not** a full **config+messages+loot+menus** host backup. If the cap **`automatic_backups.max_users`** causes **skip**, preset apply is **refused**.

## Config reload

- After zip extraction/merge, **`reloadFromDisk()`** reloads config/skill data/menus as implemented on Fabric. If something still needs a full JVM **restart** (rare; reference message mentions restart after load), do that in a maintenance window.

## Rollback

- Restore the plugin folder from a **host** backup, or re-run from the newest **`backups/*.yml`** and fix configs manually. **Sprint 62** does not add preset **undo**.

## Pass / fail checklist

- [ ] **Unsafe** names rejected with **Granite** errors (no path escape).
- [ ] **list** / **info** are read-only.
- [ ] **load** without **confirm** does not modify configs.
- [ ] **confirm** refuses when pre-apply backup **skips** or **fails**.
- [ ] Success path shows **created/modified/replaced/skipped** lists (from **`PresetLoadResult`**) and reload note.

## Risks

- **Config cannon:** a malicious or broken zip can still damage configs if **`preset.yml` lists destructive paths** — trust zip **provenance**; keep **host** backups.
- **Parity** stays **Partial**: Fabric adds **list**+**info**; confirmation UX differs from Bukkit ACF.

See **`fabric/COMMAND_PARITY_AUDIT.md`**.
