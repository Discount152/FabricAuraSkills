# Sprint 64 — manual validation: `/storage mergeskills` + `/storage removeallmodifiers`

**Build:** **≥ 2.3.92** (see `gradle.properties` → `projectVersion`).

**Scope:** Fabric **Granite Skills** dedicated server. **Sprint 64** adds **guarded** storage **mutations** aligned with the **in-repo Bukkit** `StorageCommand` reference, with **extra** safety: **two-step** flow, **pre-UserState backup** (`BackupProvider#saveBackupSync(true)`), and **console/RCON-only** **`confirm`**.

**Out of scope:** storage **migration**, SQL dump, provider hot-swap, **`force`**, user repair tools, scheduled backups.

## Command roots

- **`/storage …`** (top-level)
- **`/auraskills storage …`**
- **`/skills storage …`**
- **`/sk storage …`**
- **`/skill storage …`**

## Permission

- **`auraskills.command.storage`** — **all** storage subcommands including previews and (when allowed) confirm steps (default **deny** in fabric-perms-style checks).
- **Split nodes** (e.g. `auraskills.command.storage.mergeskills`) were **not** added this sprint; one node covers **info | save | reload | mergeskills | removeallmodifiers**.

## Console-only vs in-game

| Step | In-game (player) | Dedicated **console** / **RCON** |
|------|------------------|-----------------------------------|
| **mergeskills** preview | Allowed with **permission** | Allowed |
| **mergeskills** … **confirm** | **Rejected** (error: `confirm_console_only`) | Allowed |
| **removeallmodifiers** preview | Allowed with **permission** | Allowed |
| **removeallmodifiers** **confirm** | **Rejected** | Allowed |

Bukkit reference: **entire** **`mergeskills`** and **`removeallmodifiers`** are **console-only** (no in-game at all). Fabric **deliberately** allows **read-only** previews in-world for staff who hold **`auraskills.command.storage`**.

## Expected behavior (reference-aligned)

### `mergeskills`

- **Preview:** ` /storage mergeskills <fromSkill> <toSkill> ` — no writes.
- **Confirm:** ` /storage mergeskills <fromSkill> <toSkill> confirm ` — after **successful** pre-backup, loads **all** `UserState`s (`loadStates(false, false)`). For each user, both skills must be present. If **source level &gt; target level**, set target **level** to source level and target **XP** to source’s XP, then `applyState`. Counts: merged / applyState failed / missing either skill / skipped (source ≤ target).

### `removeallmodifiers`

- **Preview:** ` /storage removeallmodifiers ` — no writes.
- **Confirm:** ` /storage removeallmodifiers confirm ` — after pre-backup, for each `UserState`, if **stat** and **trait** modifier maps are both empty, skip; else apply a copy of the state with **empty** stat/trait maps (skills/XP/mana unchanged).

## Pre-action backup

- Both **confirm** paths call **`saveBackupSync(true)`** first.
- If backup returns **null** (e.g. **`automatic_backups.max_users`**) or **throws**, the command **aborts** and prints **`mutation_prereq_skipped`** or **`mutation_prereq_failed`**.
- Success lines include the **pre-backup** **`.yml` basename** (not a full path in chat).

## Online players

- After a successful **confirm** run, **`FabricCommandStorageUtil#refreshOnlineUsersFromStorage`** reloads each online user from `StorageProvider#loadState` and applies to memory + permissions.
- If that fails for a player, the server log warns; the **`mutation_online_note`** string tells operators rejoin if needed.

## Java dedicated server — test steps

1. Set **`automatic_backups.max_users`** high enough (or use a test server with few profiles) so pre-backup is **not** skipped.
2. Grant **`auraskills.command.storage`** to a test **OP** and ensure normal players **lack** it (default deny).
3. **mergeskills — preview (in-game, if desired):** run preview with two **enabled** skill keys; expect **no** file changes and a **long** preview line. Try **invalid** / **disabled** key → `merge_unknown_from` / `merge_unknown_to`. Same from and to id → `merge_same_skill`.
4. **mergeskills — confirm (console):** from **console**, run the same `… confirm` line; expect a **UserState** backup **basename** in the summary, then counter lines, then `mutation_online_note`.
5. **In-game confirm attempt:** as a **player** with perm, type **`/storage … confirm`**; expect **`confirm_console_only`** and **no** mutation.
6. **removeallmodifiers —** repeat preview/confirm (console) pattern; with empty modifier maps, expect high **skipped** count and low **updated**.

## Console / RCON

- **Confirm** must be tested from **console** or **RCON**; RCON is treated as a **non-entity** source like the dedicated console.

## Bedrock / Geyser (chat)

- If chat commands are routed the same as Java, **previews** may work; **`confirm`** from a **bedrock** client still runs **as a player** (has an entity) and should be **rejected** — use **RCON** or a **Java** console to **confirm** unless you verify otherwise. **This doc does not claim Bedrock was fully tested** unless you record it.

## Rollback (backup restore)

1. Note the **pre-backup** **`.yml` basename** from the success message.
2. Use **`/backup load <basename> confirm`** (Sprint 61) **or** the documented backup restore path from **`SPRINT_61_MANUAL_BACKUP_LOAD.md`** to apply that **UserState** snapshot — **not** a full world rollback.

## Pass / fail checklist

- [ ] **No** mutation without the literal subcommand **`confirm`**.
- [ ] **Confirm** rejected **in-world** (player) with a clear error.
- [ ] **Pre-backup** runs before mutation; **skip/fail** → **no** storage writes in the main loop.
- [ ] **Unknown** / **disabled** skills rejected on **preview** and **confirm** with the same skill keys.
- [ ] **Same** source and target skill rejected.
- [ ] **merge_done** and **remove_mod_done** include **pre-backup** **basename** and **counts** as designed.
- [ ] **Online** users refreshed or **`mutation_online_note`** appears after mutation.

## Risks / caveats

- **All stored users** are scanned — large databases may take time; **test on a copy** when possible.
- **mergeskills** does not delete the **source** skill row; it only overwrites the **target** when the reference rule applies (from level &gt; to level). Align expectations with the **Bukkit** reference, not a generic “rekey skill id” tool.
- **Bukkit** had **no** pre-backup and **no** `confirm` — Fabric is **stricter** by design.

See **`fabric/COMMAND_PARITY_AUDIT.md`**.
