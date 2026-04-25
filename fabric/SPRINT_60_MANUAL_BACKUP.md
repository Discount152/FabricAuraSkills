# Sprint 60 — manual validation: `/backup` (bounded create / list / info)

**Build:** **≥ 2.3.88** (see `gradle.properties` → `projectVersion`).

**Scope:** Fabric **Granite Skills** dedicated server. **Sprint 60** does **not** implement **restore** (`backup load` in Bukkit) or **delete** — this manual is for **create|save|list|info** only.

## Command roots to exercise

- **`/backup …`** (top-level, Fabric convenience)
- **`/auraskills backup …`**
- **`/skills backup …`**
- **`/sk backup …`**
- **`/skill backup …`**

## Subcommands (all require **`auraskills.command.backup`**)

| Subcommand | Role |
|------------|------|
| **`create`** | Run **`BackupProvider`** **save** (async); writes **one** new **`.yml`** under the plugin **`backups/`** folder with a **timestamped** name. |
| **`save`** | Same as **`create`** (Bukkit name). |
| **`list`** | Lists **`.yml`** files in **`backups/`**, **newest first**, with size and time; output is **capped** in chat. |
| **`info`** | Explains what Granite **backup** does and does **not** cover (see in-game text + this doc). |

**Default permission:** **deny** (staff/op with LuckPerms or fabric permission provider) — **unlike** reference **`plugin.yml`** where **`backup.save` / `backup.load`** default **true**; Fabric uses a **single** node **`auraskills.command.backup`** for this sprint’s subcommands.

## Permission setup

- Grant **`auraskills.command.backup`** to the test operator (e.g. LuckPerms on Fabric, or op if your test server grants all).

## Java dedicated server

1. Start the server with the **Sprint 60** JAR, configs present.
2. Run **`/backup info`** — read the text; confirm **no** claim of full config/loot/messages backup.
3. Run **`/backup list`** on a fresh world — expect **no backups** or only prior files.
4. Run **`/backup create`** (or **`/backup save`**) — expect “saving” then success with **`.yml`** file **name** (not a full host path in player chat).
5. Run **`/backup list`** again — the new file appears **newest first**.
6. Optional: with **many** users, if **`automatic_backups.max_users`** is exceeded, **create** may **skip** (null result) and show the **too many users** line — this is **common** `BackupProvider` behavior.

## Console (RCON / dedicated server console)

- Same commands; feedback uses the plugin **default** locale for formatted messages from **`messages_*.yml`** where used.

## Bedrock / Geyser / Floodgate

- Commands are **chat**-based; use the same text as **Java** on Geyser.
- **This sprint does not substitute for on-device Geyser proof** — per project handoff, **ship-grade** acceptance requires **real** **Bedrock** validation **or** an **explicit** documented exception. Do **not** mark “Bedrock tested” in release notes from this checklist alone.

## File storage vs SQL

- **File (YAML) storage:** **`loadStates`** enumerates user data as implemented in **`FileStorageProvider`**.
- **MySQL / SQL:** **`SqlStorageProvider`** still uses **`loadStates`** for backup content; very large player counts can hit **skip** ( **`automatic_backups.max_users`** ). **Sprint 60** does **not** add a separate **SQL dump** or **mysqldump** integration.

## Unsupported / out of scope (fail-safe)

- **Restore / load** from a backup file — **not** in this sprint (Bukkit has **`backup load <file>`** with confirmation).
- **Delete** old backups, **cloud** upload, **scheduled** backups.
- **Full** **`config/`** or **`messages/`** file tree copy — the **`BackupProvider`** file is **UserState**-oriented, not a full mod datadir mirror.

## Pass / fail checklist

- [ ] **`/backup info`** returns accurate limitation text.
- [ ] **`/backup create`** creates a new **`.yml`** in **`backups/`** and reports success.
- [ ] **`/backup list`** lists backups **newest first** with **filename** only (no raw **absolute** paths required for players).
- [ ] **Without** permission, commands are **denied**.
- [ ] **Exception** during async save surfaces as **user-visible** error and **server log** (not a hung future).

## Rollback

- Revert the Sprint 60 commit; remove new **`.yml`** files from **`backups/`** if you need disk clean-up (they are **add-only**; **create** does not delete source data).

## Risks / caveats

- **Heavy servers:** **create** can take noticeable time; prefer **low-traffic** window or **console** (per **`/backup info`**).
- **Max users** skip: tune **`automatic_backups.max_users`** or rely on **external** backups.
- **Parity:** **Partial** — no **load**, different **permission** model vs Bukkit split nodes.

See **`fabric/SPRINT_SUMMARY_SPEC.md`** for sprint handoff structure.
