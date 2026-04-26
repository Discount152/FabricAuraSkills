# Sprint 85 — Java command tree patch (item / storage / backup / preset roots)

| Field | Value |
|--------|--------|
| **Sprint 85** | **Code** (in-repo) + **operator** validation on **`FabricAuraSkills-2.3.118.jar`** (or newer). |
| **Bedrock / Geyser** | **Not tested** unless explicitly run. |

## Operator context (post–Sprint 84)

**Sprint 84** (`2.3.117`) fixed:

- **`/auraskills|/skills|/sk|/skill xp view …`** and **`/skilllevel view …`** — readable chat (**`FabricGraniteText`**); no raw **`auraskills.granite.command.xp.view_line`** / **`skilllevel.view_line`**.

**Still failing on `2.3.117` (screenshots):**

- **`/auraskills item`** (and **`/skills|/sk|/skill item`**) — Brigadier “Incorrect argument … **`/auraskills item` ←[HERE]**”.
- **`/storage`**, **`/backup`**, **`/preset`** with **no subcommand** — parse stopped at root.

## Root cause (code)

Brigadier **literal** nodes had **only** **`.then(…)`** children and **no** root **`.executes(…)`**. For a single-token input like **`/storage`**, the parser matched the literal but had **no** completion at that node → **incorrect argument** at end of input. The **`item`** literal under **`/auraskills`** behaved the same for **`/auraskills item`**.

**`backup`:** children split between **`auraskills.command.backup`** and **`auraskills.command.backup.load`**. The root had **no** **`requires`**, but the **no-subcommand** case still failed for the same “no executor” reason. **Sprint 85** adds **`backupFamilyAccess()`** (**`BACKUP`** **or** **`BACKUP_LOAD`** **or** non-player) on the root.

**`item`:** Subcommands already used distinct **`item.*`** perms. The root had **no** gate and **no** executor. **Sprint 85** adds **`anyItemSubcommandPermission()`** (any implemented **`item.*`** **or** non-player) + usage feedback.

## Code changes (files)

| File | Change |
|------|--------|
| **`FabricCommandRequirements.java`** | **`backupFamilyAccess()`**, **`anyItemSubcommandPermission()`**. |
| **`FabricItemArmorModifierCommands.java`** | **`item`**: **`.requires(anyItemSubcommandPermission)`**, **`.executes(executeItemRootUsage)`**. |
| **`FabricStorageCommands.java`** | **`storage`**: root **`permissionOrNonPlayerSource(STORAGE)`** + **`executeRootUsage`**. |
| **`FabricBackupCommands.java`** | **`backup`**: **`backupFamilyAccess()`** + **`executeRootUsage`**. |
| **`FabricPresetCommands.java`** | **`preset`**: root **`permissionOrNonPlayerSource(PRESET)`** + **`executeRootUsage`** (children unchanged). |
| **`FabricBrigadierCommands.java`** | Log string: Sprint **84–85** root usage note. |
| **`en_us.json`** | **`item.usage_root`**, **`storage.usage_root`**, **`backup.usage_root`**, **`preset.usage_root`**. |
| **`FabricGraniteEnUsKeysTest.java`** | Asserts the four **`usage_root`** keys exist. |

**Tests / build:** Full **`FabricBrigadierCommands.register`** in JUnit is **not** used — **`CommandManager`** static init requires a **bootstrapped** game. **`.\gradlew -PfabricOnly=true :fabric:test :fabric:build`** — **PASS** in-repo.

## Final supported syntax

- **Vanilla:** top-level **`/item`** and **`/xp`** are **Minecraft** commands — **not** Granite.
- **Granite item:** **`/auraskills item …`**, **`/skills item …`**, **`/sk item …`**, **`/skill item …`** only.
- **Granite XP:** **`/auraskills xp …`**, **`/skills|/sk|/skill xp …`** only (same for **view**).

**Storage** ( **`auraskills.command.storage`**, default deny; console/RCON passes **`requires`** without LP):

- **`/storage`** — usage; **`/storage info`**, **`save`**, **`reload`**, **`mergeskills …`**, **`removeallmodifiers …`**
- Prefixed: **`/auraskills|/skills|/sk|/skill storage …`**

**Backup** (**`auraskills.command.backup`** for create/save/list/info; **`auraskills.command.backup.load`** for load/restore):

- **`/backup`** — usage (if player has either backup perm family)
- **`/backup create`**, **`save`**, **`list`**, **`info`**, **`load|restore <file> [confirm]`**
- Prefixed: **`/auraskills|/skills|/sk|/skill backup …`**

**Preset** (**`auraskills.command.preset`**):

- **`/preset`** — usage; **`list`**, **`info <name.zip>`**, **`load <name.zip> [confirm]`**
- Prefixed: **`/auraskills|/skills|/sk|/skill preset …`**

**Item subtree** (existing): **`modifier`**, **`trait`**, **`ignore`**, **`requirement`**, **`multiplier`**, **`register`**, **`unregister`**, **`give`** — see **`FabricItemArmorModifierCommands`**.

## Permission requirements (summary)

| Area | Nodes |
|------|--------|
| Item (any subcommand) | At least one of **`auraskills.command.item.modifier`**, **`.ignore`**, **`.requirement`**, **`.multiplier`**, **`.register`**, **`.give`** for **players**; bare **`item`** shows usage if any pass. |
| Storage | **`auraskills.command.storage`** (or non-player). |
| Backup | **`auraskills.command.backup`** and/or **`auraskills.command.backup.load`** (or non-player) for root usage; subcommands still enforce specific nodes. |
| Preset | **`auraskills.command.preset`** (or non-player). |

Normal players without these nodes should see **no** suggestion path / **permission denied** as before.

## Operator validation checklist (`2.3.118+`)

**Item**

- [ ] **`/auraskills item`** — usage or suggestions; not “incorrect argument” at **`item`**.
- [ ] **`/skills item`**, **`/sk item`**, **`/skill item`** — same.
- [ ] **`/auraskills item register <key>`** (disposable item / test key).
- [ ] **`/auraskills item give <player> <key>`** (safe target, disposable).

**Storage**

- [ ] **`/storage`** — usage.
- [ ] **`/storage info`** (staff + console).
- [ ] **`/auraskills storage info`**, **`/skills storage info`**, **`/sk storage info`**, **`/skill storage info`**.

**Backup**

- [ ] **`/backup`** — usage.
- [ ] **`/backup info`**, **`/backup list`**, **`/backup create`** (disposable).
- [ ] Prefixed **`backup info`** under **`/auraskills|/skills|/sk|/skill`**.

**Preset**

- [ ] **`/preset`** — usage.
- [ ] **`/preset list`**
- [ ] Prefixed **`preset list`**.

**XP / skilllevel regression**

- [ ] **`/auraskills xp view <player> <skill>`** — still **no** raw translation keys.
- [ ] **`/skilllevel view <player> <skill>`** — same.

**Normal player**

- [ ] Without staff **`item`**, **`storage`**, **`backup`**, **`preset`** perms — denied or hidden as expected.

## Sprint 86 — permission context and matrix rules (docs only)

Early smoke runs were often blocked by **LuckPerms** (missing **`auraskills.command.*`** nodes), not by Brigadier bugs. After granting **`auraskills.*`** globally to the tester (**Deskount**), in-game staff commands should be re-evaluated.

- **Readable plain English** (including multi-line admin diagnostics) is **Pass**, not a failure.
- **`/auraskills`** with a long **usage** line is **expected**; it is **not** a menu command.
- **Console/RCON** **`storage info`**, **`backup info`**, **`preset list`**, **`auraskills item`** were **confirmed**; in-game rows are updated separately in **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`**.
- Do **not** treat **Pending** (not yet re-run after **`auraskills.*`**) as **Fail**.

## Related

- **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`**
- **`SPRINT_84_MANUAL_COMMAND_SMOKE_PATCH.md`**
- **`COMMAND_PARITY_AUDIT.md`**
- **`Changelog.md`** — **2.3.118**
