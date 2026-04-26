# Sprint 74 — confirmation flows: reference inventory + Fabric baseline

**Scope:** Granite Skills (Fabric) + in-repo Bukkit **reference** only. No Slate `MenuFileManager` parity, no gameplay changes, no new top-level commands.

## Part A — reference + Fabric inventory

| Item | Bukkit / reference | Chat vs menu | Timeout / scope | Pending payload | Fabric equivalent (pre–Sprint 74) | Post–Sprint 74 |
|------|-------------------|--------------|------------------|----------------|----------------------------------|----------------|
| **ConfirmManager** | `bukkit/.../ConfirmManager.java` — `Map<UUID, String>`, one string per **issuer**; first call to `requiresConfirmation(issuer, arg, requireConsole?)` **stores** `arg` and returns **true** (caller should **not** run mutation); second call with **same** `arg` returns **false** (proceed). `remove(issuer)` after success. **Non-player:** if `!requireConsole` returns **false** immediately (no two-step). **30s** scheduled removal of map entry if value still matches. | **Chat** (ACF / repeat command) — no dedicated **confirm** inventory menus found under `bukkit/.../menus` with “confirm” in the name. | **30s**; **per-issuer** (`UUID`); not global. | The **command argument string** the user must repeat (e.g. path-like token for backup/preset). | **Literal `confirm`** under Brigadier for **backup load**, **preset load**, **storage** mutations — no shared pending-key check between preview and confirm. | **`FabricCommandConfirmation`**: per **subject** (`ServerPlayer` UUID, or a single **console/RCON** slot), **30s** expiry, **key** must match on confirm. |
| **BackupCommand** | Uses `ConfirmManager` for load-style flows (double-typed). | Chat | Bukkit 30s map | String identifying the target | `FabricBackupCommands` preview + `... confirm` | Pending key = resolved **`.yml` basename** (`File#getName()`). |
| **PresetCommand** | Uses `ConfirmManager` with `requireConsole` as needed. | Chat | Bukkit 30s map | Preset/zip arg | `FabricPresetCommands` preview + `confirm` | Pending key = resolved **`.zip` basename** from safe path. |
| **StorageCommand** (ref) | Console-only for destructive subcommands; no inventory confirm in reference grep. | Chat | n/a in ref for Fab-style preview | n/a | `FabricStorageCommands` **mergeskills** / **removeallmodifiers** + confirm (console/RCON for **confirm**); preview allowed in-game. | **Merge** key = `storage:mergeskills:<fromKey>:<toKey>` (lowercased skill id keys). **removeallmodifiers** = constant `storage:removeallmodifiers`. |
| **Slate / YAML** | Various menus may *say* “confirm” in text; not shipped as a unified **confirm** GUI in this sprint. | n/a | n/a | n/a | Not ported | Unchanged; **AS-007** remains **Slate+commands**-scoped for full “confirm framework” if ever needed. |

**Recommendation:** Keep **Brigadier `confirm` literals** (clearer for ops than “run the same line twice”); add **`FabricCommandConfirmation`** so preview and confirm use the same pending **key** and **30s** window, without inventing a **menu**-based confirm layer.

## Part B — bounded implementation (`FabricCommandConfirmation`)

- **Class:** `dev.aurelium.auraskills.fabric.command.FabricCommandConfirmation`.
- **API:** `registerPending(plugin, source, key)` after a **successful preview**; `verifyPendingOrError(source, expectedKey)` at the start of **confirm** (clears on success; sends **`confirmation.no_pending`** or **`confirmation.mismatch`** on failure).
- **Subject:** `ServerPlayer` UUID, or `CONSOLE_SUBJECT` (all non-entity sources), matching “one pending op per console” risk profile.
- **Wired in:** `FabricBackupCommands` (load/restore), `FabricPresetCommands` (load), `FabricStorageCommands` (mergeskills, removeallmodifiers).
- **Messages:** `auraskills.granite.command.confirmation.no_pending`, `mismatch` in `en_us.json`.
- **Semantics:** No change to **backup**/**preset**/**storage** mutation **logic**; only an extra **gate** on confirm.
- **Parity note:** Bukkit = **same command twice**; Fabric = **preview** + **literal `confirm`** + **pending key** (stricter in spirit; aligns with 30s + arg matching).

**Manual checks:** Re-run cases from `SPRINT_61_MANUAL_BACKUP_LOAD.md`, `SPRINT_62_MANUAL_PRESET.md`, `SPRINT_64_MANUAL_STORAGE_ACTIONS.md`, plus: (1) run **confirm** without preview → `no_pending`; (2) preview A, change args to B and **confirm** → `mismatch`; (3) wait **>30s** after preview → `no_pending`.

**Build:** `.\gradlew -PfabricOnly=true :fabric:test`.

**Bedrock:** Geyser players run commands **as** players; **storage confirm** still **console/RCON**-only by design. No change this sprint.
