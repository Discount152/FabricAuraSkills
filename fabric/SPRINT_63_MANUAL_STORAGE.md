# Sprint 63 — manual validation: `/storage`

**Build:** **≥ 2.3.91** (see `gradle.properties` → `projectVersion`).

**Scope:** Fabric **Granite Skills** dedicated server. **Sprint 63** adds **`/storage info`**, **`/storage save`**, and **`/storage reload`** (non-mutating explainer). It does **not** add live YAML↔SQL migration, SQL dump tooling, or the Bukkit reference **`mergeskills`** / **`removeallmodifiers`** console commands.

## Command roots

- **`/storage …`** (top-level)
- **`/auraskills storage …`**
- **`/skills storage …`**
- **`/sk storage …`**
- **`/skill storage …`**

## Permission

- **`auraskills.command.storage`** — all subcommands (default **deny** in fabric-perms-style checks).

Reference **`plugin.yml`** uses **`auraskills.command.storage: true` by default; Fabric default **deny** is intentional for staff gating.

## Subcommands

| Subcommand | Behavior |
|------------|----------|
| **`info`** | Read-only. Backend summary (**YAML file** vs **SQL** pooled), provider **FQMN** (for logs/support), data directory **basename** (not a full absolute path in chat), **`sql.enabled`** from config, count of **loaded** users. **Never** prints JDBC URL, host, user, or password. |
| **`save`** | Writes all **loaded** online user profiles with **`StorageProvider#save`**, same as **`/auraskills save`**. |
| **`reload`** | **Does not** reload the world or hot-swap **`StorageProvider`**. Explains: use **`/auraskills reload`** for config/skill data from disk; **restart** to apply **`sql.enabled`** or change backend. |

## Java dedicated server

1. Grant **`auraskills.command.storage`**.
2. Run **`/storage info`** with YAML storage — expect **FileStorageProvider**-style class name and no secrets.
3. (Optional) Enable **`sql.enabled`** in **`config.yml`**, **restart** — then **`/storage info`** should show **SQL** provider. Confirm **no** host/password in chat.
4. With online players, run **`/storage save`** — expect count of saved profiles (same spirit as **`/auraskills save`**).
5. Run **`/storage reload`** — expect only the **guidance** string, not a second full **reload** duplicating **`/auraskills reload`**.

## Console / RCON

- Same behavior; full paths for debugging remain in **server log** (e.g. version command / logger), not required in chat for **`info`**.

## Bedrock / Geyser

- Chat-based. **Not** a substitute for on-device **Bedrock** proof unless you actually test there.

## Rollback

- Revert the mod JAR; no migration command is applied by S63. User data is only **saved** on **`save`**; **no** new backup is created by **`/storage save`** (same as top-level **save**).

## Pass / fail

- [ ] **info** never prints credentials or full JDBC.
- [ ] **save** fails gracefully with a short error + log on I/O error.
- [ ] **reload** does not change storage type or call **`reloadFromDisk`**.

## Risks

- **save** only covers **loaded** users (like **`/auraskills save`**). Offline players are not enumerated here.
- Switching **YAML ↔ SQL** needs **config** change + **restart**; there is **no** `/storage migrate` in S63.

See **`fabric/COMMAND_PARITY_AUDIT.md`**.
