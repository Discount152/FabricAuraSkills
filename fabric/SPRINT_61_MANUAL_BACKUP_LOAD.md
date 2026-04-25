# Sprint 61 — manual validation: `/backup load|restore` (guarded UserState restore)

**Build:** **≥ 2.3.89** (see `gradle.properties` → `projectVersion`).

**Scope:** Fabric **Granite Skills** dedicated server. This sprint adds **guarded** restore of **BackupProvider** **`.yml`** snapshots (**UserState**-shaped data through **`StorageProvider`**). It is **not** a full **config/messages/loot/menus/item registry** or whole **datadir** restore.

## Command roots to exercise

- **`/backup …`** (top-level)
- **`/auraskills backup …`**
- **`/skills backup …`**
- **`/sk backup …`**
- **`/skill backup …`**

## Subcommands and permissions

| Action | Permission | Default |
|--------|------------|---------|
| **`create`**, **`save`**, **`list`**, **`info`** | **`auraskills.command.backup`** | **deny** (staff / LP / fabric-perms) |
| **`load`**, **`restore`** (alias) | **`auraskills.command.backup.load`** | **deny** |

Reference Bukkit **`plugin.yml`** often defaults **`backup.save`** / **`backup.load`** to **true**; Fabric intentionally uses **deny** defaults for staff-style nodes—align with your permission provider.

## Two-step confirmation (required)

1. **`/backup load <basename.yml>`** — **Preview** only: validates the name, checks the file exists under **`backups/`**, shows what will happen and tells you to add **`confirm`**.
2. **`/backup load <basename.yml> confirm`** — Creates a **pre-restore** backup via **`BackupProvider#saveBackupSync(true)`**, then runs **`loadBackupAsync`** on success.

**`restore`** is a literal alias for **`load`** (e.g. **`/backup restore foo.yml confirm`**).

There is **no** `--force** this sprint: if the pre-restore backup is **skipped** (e.g. **`automatic_backups.max_users`**) or **fails**, restore is **refused**.

## Safe filename examples (should work when file exists)

- `backup-2026-04-24_14-30-00.yml`
- `backup-foo.yml`

## Unsafe / rejected examples (must not resolve outside `backups/`)

- `../userdata/player.yml`
- `subdir/backup.yml`
- `backup.txt`
- Paths with **`/`**, **`\`**, **`:`**, or **`..`**
- **NUL** bytes in the token (rejected)

Tab completion should only suggest **`.yml`** basenames from **`backups/`**.

## Java dedicated server

1. Grant **`auraskills.command.backup`** and **`auraskills.command.backup.load`** to the test operator.
2. Ensure at least one **`.yml`** exists under the plugin **`backups/`** (e.g. run **`/backup create`** first).
3. Run **`/backup load <that-file.yml>`** — expect **preview** and **no** data mutation.
4. Run **`/backup load <that-file.yml> confirm`** — expect **pre-restore** message flow, then **success** or a **clear** error (and **details in server log** on failure).
5. With an **online** player whose data is in the backup, confirm **in-memory** refresh or read the **online** caveat line in chat.

## Console / RCON

- Same commands; feedback uses the plugin locale for **`CommandMessage`** / Granite strings where applicable.

## Bedrock / Geyser / Floodgate

- Commands are **chat**-based; behavior should match **Java** on Geyser.
- **Do not** mark “Bedrock tested” unless you **actually** verified on a Bedrock client. Per project rules, document the gap if not verified.

## File storage vs SQL

- **YAML file storage:** restore applies through the same **`StorageProvider`** path as **`BackupProvider`** ( **`loadBackup`** paths).
- **SQL storage:** same **UserState** restore **if** the common loader supports your deployment; very large servers may hit **max_users** on **pre-restore**—restore is **refused** in that case (no force flag). This sprint does **not** add raw **SQL dump** restore.

## Online-player caveat

- After a successful load, the implementation **reloads online `User` objects from storage** where the common API allows. If anything looks wrong, players may need to **rejoin**—see in-game **`online_refreshed_note`**.

## Rollback

- Restore from the **pre-restore** **`.yml`** created immediately before the confirmed load (newest file in **`backups/`** after a failed production attempt, or keep external host backups).
- Revert the mod JAR to **2.3.88** if you must remove the feature; data already written by a successful restore stays on disk until you restore from backup.

## Pass / fail checklist

- [ ] Preview **without** **`confirm`** does **not** mutate data.
- [ ] **`confirm`** runs **only** with **`auraskills.command.backup.load`**.
- [ ] Unsafe names and out-of-tree paths are **rejected** with clear errors.
- [ ] Pre-restore backup **failure** or **skip** blocks restore.
- [ ] Success path shows **UserState-only** scope (not full datadir).
- [ ] **Server log** holds stack traces on failure; chat stays short.

## Risks / caveats

- **Heavy servers:** load can take time; prefer **low-traffic** windows.
- **Partial parity** vs Bukkit: different **confirmation** UX, different **permission** split/defaults, **no** **delete**/prune/schedule, **no** full **datadir** restore.

See **`fabric/COMMAND_PARITY_AUDIT.md`** and **`fabric/SPRINT_SUMMARY_SPEC.md`** for handoff structure.
