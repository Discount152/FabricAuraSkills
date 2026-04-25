# Changelog

Changelog for versions since 2.0.0.

## 2.3.94

### Changes

- **Fabric: `ManaAbilityCommand` (Sprint 66):** **`/auraskills manaability`**, **`/skills` / `/sk` / `/skill manaability`** — **`resetcooldown <player> <ability> [fill_mana]`** (Brigadier **bool**). Matches Bukkit’s **only** subcommand: clear **cooldown** when **&gt; 0** and not **activated**, optional **max** **mana** fill; **`auraskills.command.manaability.resetcooldown`** (default **deny**). **In-game:** target must be **another** online player (**ACF** `@Flags("other")`); **console/RCON** may target any online player. **`FabricManaAbilityCommands`**, **`FabricCommandManaAbilityParser`**, **`enabledManaAbilities`** suggestions. **No** **`ManaAbilityRefreshEvent`** on Fabric (api-bukkit). **`/abtoggle`** unchanged (action bar **IDLE** only). **Docs** `SPRINT_66_MANUAL_MANA_ABILITY_COMMAND.md`, parity / MENU / ledger / backlog / **Sprint 65** reconciliation, **`projectVersion` 2.3.93 → 2.3.94**. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`. **Parity:** **Partial** — reference has **one** method; no top-level `/manaability`.

## 2.3.93

### Changes

- **Fabric: command reconciliation (Sprint 65):** **Fixed** Bukkit `skills|sk|skill` drift — **`/skills`** was missing the same subtrees as **`/sk`** and **`/skill`** ( **openmenu**, leaderboards, utility, **xp** / **mana** / **skilllevel**, **modifier** / **trait**, **item** / **armor**, then **claimitems** through **storage**). **`FabricBrigadierCommands#skillsAliasSharedTree`** now wires **one** shared chain for all three literals. **Logger** and **en_us** **help** / **usage** strings refreshed; **`FabricCommandPermissions#STORAGE`** javadoc (S64). **Docs** `SPRINT_65_COMMAND_RECONCILIATION.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, `PARITY_AUDIT_LEDGER` CMD-001, `PARITY_BACKLOG`, **`projectVersion` 2.3.92 → 2.3.93**. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`. **No** new command families, menu, or gameplay changes.

## 2.3.92

### Changes

- **Fabric: `/storage` mutations (Sprint 64):** **`/storage mergeskills <from> <to>`** (preview) + **`/storage mergeskills <from> <to> confirm`**; **`/storage removeallmodifiers`** (preview) + **`/storage removeallmodifiers confirm`**. Bukkit **reference** semantics for **mergeskills** (higher level → set target + copy XP) and **removeallmodifiers** (clear **UserState** **stat** + **trait** maps only) via **`StorageProvider#loadStates`** + **`applyState`**. **Guards** vs reference: two-step (no mutation without literal **`confirm`**); **pre-**`UserState` **backup** (`BackupProvider#saveBackupSync(true)`) **required** — aborts if skipped (**max_users**) or fails; **no** **force**; **confirm** from **dedicated console or RCON only** (in-game preview allowed with **`auraskills.command.storage`**). **`FabricCommandStorageUtil#refreshOnlineUsersFromStorage`** after mutations. **Lang** `storage.*` keys. **Docs** `SPRINT_64_MANUAL_STORAGE_ACTIONS.md`, parity docs, **`projectVersion` 2.3.91 → 2.3.92**. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`. **Parity:** **Partial** — Bukkit had **entire** merge/remove on **console** only (no preview/backup); Fabric adds **safer** operator flow; **no** `storage` **info|save|reload** on Bukkit.

## 2.3.91

### Changes

- **Fabric: `/storage` (Sprint 63):** **`/storage info`**, **`/storage save`**, **`/storage reload`** (explainer only — no storage hot-swap); mirrors + top-level **`/storage`**. **`auraskills.command.storage`** (default **deny**). **`FabricStorageCommands`**: **info** shows backend kind, provider FQMN, data dir **name**, **`sql.enabled`**, loaded user count — **no** JDBC secrets; **save** calls **`saveAllUserProfiles`**. **`/sk`/`/skill`** trees now also include **`preset`** subtree (fix). **Docs** `SPRINT_63_MANUAL_STORAGE.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, **CMD-001** / **CMD-012**, `PARITY_BACKLOG`, **`projectVersion` 2.3.90 → 2.3.91**. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`. **Parity:** **Partial** — Bukkit **`StorageCommand`** = **`mergeskills`** / **`removeallmodifiers`** (console only); Fabric has **info/save/reload** guidance instead; **no** migrate.

## 2.3.90

### Changes

- **Fabric: `/preset` (Sprint 62):** **`/preset list`**, **`/preset info <name.zip>`** (read-only), **`/preset load <name.zip> confirm`** (preview: **`/preset load <name.zip>`**). Mirrors: **`/auraskills`**, **`/skills`**, **`/sk`**, **`/skill`**, top-level **`/preset`**. **`auraskills.command.preset`** (default **deny**). **`FabricPresetNameValidator`**, tab **`presets/*.zip`**, common **`PresetManager#preparePreset` / `loadPreset`**. **Pre-apply** **`BackupProvider#saveBackupSync(true)`** (refuse on skip/fail); then **`reloadFromDisk`**. **Tests:** **`FabricPresetNameValidatorTest`**, permissions. **Docs** `SPRINT_62_MANUAL_PRESET.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, `PARITY_AUDIT_LEDGER` CMD-001/011, `PARITY_BACKLOG`, **`projectVersion` 2.3.89 → 2.3.90**. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`. **Parity:** **Partial** (Bukkit: **`preset load` only** + ACF double-typed confirm; ref **`plugin.yml`** default **true**). **Not in scope:** **StorageCommand**, preset create/delete/remote, userdata mutation.

## 2.3.89

### Changes

- **Fabric: `/backup load|restore` (Sprint 61):** Two-step **`/backup load <basename.yml> confirm`** ( **`restore`** alias for **`load`**); same mirrors as Sprint 60 + top-level **`/backup`**. **`auraskills.command.backup.load`** (default **deny**) for load/restore; **`auraskills.command.backup`** unchanged for create|save|list|info. **`FabricBackupPathValidator`** — basename-only **`.yml`**, no traversal; resolve under **`backups/`** only. Preview then confirm; **pre-restore** via **`BackupProvider#saveBackupSync(true)`** — aborts on skip/failure. **`loadBackupAsync`** + online **`User`** refresh from storage where possible + caveat string. **Common:** **`BackupProvider`** v1 **`loadV1`** completion callback; progress log divisor guard. **Tests:** path validator, **`FabricCommandPermissionsTest`**. **Docs** `SPRINT_61_MANUAL_BACKUP_LOAD.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, `PARITY_AUDIT_LEDGER_PHASE1`, `PARITY_BACKLOG`, **`projectVersion` 2.3.88 → 2.3.89**. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`. **Not in scope:** delete/prune, **`--force`**, full datadir restore, SQL dump. **Parity:** **Partial** vs Bukkit (confirmation flow, split perms vs ref **plugin.yml** defaults).

## 2.3.88

### Changes

- **Fabric: `/backup` (Sprint 60):** **`/backup create`**, **`/backup save`** (Bukkit **save** alias), **`/backup list`**, **`/backup info`**; mirrors on **`/auraskills`**, **`/skills`**, **`/sk`**, **`/skill`**, + top-level **`/backup`**; **`auraskills.command.backup`** (default **deny** with fabric-perms). Uses common **`BackupProvider`** (**`saveBackupAsync`**, **`saveBackupSync`**) — timestamped **`.yml`** under **`backups/`**, **UserState**-shaped data from **`StorageProvider#loadStates`**; **no** **load/restore**/**delete** this sprint. **Common:** `BackupProvider#saveBackupAsync` now **`completeExceptionally`** on failure. **docs** `SPRINT_60_MANUAL_BACKUP.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, `PARITY_AUDIT_LEDGER_PHASE1` (CMD-001, CMD-009), `PARITY_BACKLOG`, **`projectVersion` 2.3.87 → 2.3.88**. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`. **Bukkit:** `backup save\|load` with split perms; Fabric **list**+**info**; product **acceptance** still needs real **Java** + **Bedrock/Geyser** proof per project rules.

## 2.3.87

### Changes

- **Fabric: `/profile` (Sprint 59):** **`/profile skills|stats <player>`** and mirrors on **`/auraskills`**, **`/skills`**, **`/sk`**, **`/skill`**, + top-level **`/profile`**; **`auraskills.command.profile`** (default **allow**). **Read-only** inspection of the same **`UserState`** model as **Sprint 58** **`/transfer`**: **skills/XP** ( **`commands.profile.*`** messages), **stats** with reward-derived **base** + **modifier** + **total** (Bukkit **profile stats**), then **Granite** lines for **mana**, **raw stat/trait** persistent modifiers, and an explicit “not a full account” disclaimer. **`FabricUserDataRead`**: name → UUID (**online** + **`NameToIdCache`**); offline **YAML** without **`userdata/<uuid>.yml`** rejected when on **file** storage. **Bukkit** has **no** top-level `/profile` and **async** offline **load**; Bukkit **stats** has **no** mana/modifier **footer**. **Docs** `SPRINT_59_MANUAL_PROFILE.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, `PARITY_AUDIT_LEDGER_PHASE1` (CMD-001, CMD-008), `PARITY_BACKLOG`, **`projectVersion` 2.3.86 → 2.3.87**. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`.

## 2.3.86

### Changes

- **Fabric: `/transfer` (Sprint 58):** **`/transfer <from-uuid> <to-uuid>`** and mirrors on **`/skills`**, **`/sk`**, **`/skill`**, **`/auraskills`**, + top-level; **`auraskills.command.transfer`**. **Bounded** to **`UserState`** (skills, XP, persistent stat/trait modifiers, mana) using **`User#getState`**, **`StorageProvider#loadState` / `applyState`**, **`User#applyState`**; **`FabricUserDataTransfer`**, **`RewardManager#updatePermissions`** (online target). **YAML** offline: requires `userdata/<from>.yml` before load; **self-transfer** rejected; follow-up about **source** file unchanged. **Not** in `UserState`: jobs, unclaimed, ability data, etc. **Docs** `SPRINT_58_MANUAL_TRANSFER.md`, audit / MENU / ledger / backlog, **`projectVersion` 2.3.85 → 2.3.86**. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`.

## 2.3.85

### Changes

- **Fabric: `sources` + `resethealth` (Sprint 57):** **`/sources <skill> [sort]`** (and under **`/skills`**, **`/sk`**, **`/skill`**, **`/auraskills`**) — paged **chat** listing of configured **`XpSource`**s with **`User`** locale, sorting **`ascending|descending|alphabetical|reverse_alphabetical`**; **`auraskills.command.sources`**. Bukkit **opens the Slate `sources` menu**; Fabric does **not** (bounded text). **`/resethealth`** top-level (same mirrors) + **`auraskills.command.resethealth`**: **console/RCON only**; walks **`playerdata` *.dat`**, strips **`auraskills:`** NBT **attribute** modifiers (1.21+ path), same spirit as Bukkit **`Uninstaller`**. Helpers: **`FabricSourceSortType`**, **`FabricOfflinePlayerDataAuraStripping`**, **`FabricServerPlayerDataPaths`**. **Lang** `sources.*`, `resethealth.*`. **Perms** test. **Docs** `SPRINT_57_MANUAL_SOURCES_RESETHEALTH.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, `PARITY_AUDIT_LEDGER_PHASE1` (CMD-001, **MENU-007** note), `PARITY_BACKLOG`. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`. **Not in this sprint:** Slate **`sources` menu**; Bukkit 1.20– **legacy** `Attributes` NBT in **`resethealth`**; **`transfer`**, storage, profile.

## 2.3.84

### Changes

- **Fabric: `claimitems` + shared merge (Sprint 56):** **`/claimitems`**, **`/skills|/sk|/skill|/auraskills claimitems`**, `auraskills.command.claimitems` (Bukkit default **true**). **Merges** the **`User`** unclaimed list into the player inventory in order, using **`FabricItemRegistry#tryMergeRegisteredItemCount`** (also used by **`addRegisteredItemToInventory`** for **`/item give`** → overflow). Bukkit **opens Slate UnclaimedItemsMenu**; Fabric is a **one-shot** command. **Perms** test + **lang** `claimitems.*`. **Docs** `SPRINT_56_MANUAL_CLAIMITEMS.md`, audit / MENU / ledger / backlog, **`projectVersion` 2.3.83 → 2.3.84**. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`.

## 2.3.83

### Changes

- **Fabric: `item register` + `item unregister` + `item give` (Sprint 55):** Bukkit-shaped **registry** commands on **`/item`**: **`register <key>`** (main hand → **`FabricItemRegistry`**, **`items.yml`**), **`unregister <key>`**, **`give <player> <key> [amount]`** (amount optional; **-1** = template size). **`addRegisteredItemToInventory`** (sync) refactors **`ItemRegistry#giveItem`**; unclaimed + **`setShouldSave`** on overflow. **Perms** **`auraskills.command.item.register`**, **`auraskills.command.item.give`**. **Suggestions:** **`registeredItemKeys`**. **Lang** `item.bad_key`. **Docs** `SPRINT_55_MANUAL_ITEM_REGISTER_GIVE.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, ledger CMD-001/006/007/021, `PARITY_BACKLOG`, **`projectVersion` 2.3.82 → 2.3.83**. **Not in scope:** Bukkit **ExternalItemProvider**; **`claimitems`**. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`.

## 2.3.82

### Changes

- **Fabric: `armor requirement` + `armor multiplier` (Sprint 54):** Bukkit-shaped **main-hand** **`/armor requirement`** / **`/armor multiplier`** (same subcommands as **`/item`**: **add** / **remove** / **list** / **removeall**; optional **lore** on add/remove for requirement & multiplier add). **Refactor:** **`RequirementMsg`**, **`MultiplierMsg`**, and shared **`requirement*`** / **`multiplier*`** methods with **`ModifierType`** — item paths delegate to same logic. **Perms** **`auraskills.command.armor.requirement`**, **`auraskills.command.armor.multiplier`**. **`armor` root** no longer has a single permission: **`modifier`** & **`trait`** use **`armor.modifier`**; new subtrees use their own nodes (mirrors item split). **Docs** `SPRINT_54_MANUAL_ARMOR_REQ_MULT.md`, audit/MENU/ledger/backlog, **`projectVersion` 2.3.81 → 2.3.82**. **Not in scope:** item **register** / **give**. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`.

## 2.3.81

### Changes

- **Fabric: `item requirement` + `item multiplier` (Sprint 53):** Bukkit-shaped **held main-hand** **`/item requirement`** (**add** `<skill> <level>` [lore], **remove** [lore], **list**, **removeall**) and **`/item multiplier`** (**add** `<skill|global> <value>` [lore], **remove**, **list**, **removeall**). **PDC** `auraskills:item_requirements` / `auraskills:item_multipliers` via **`FabricSkillsItemWriter.putRequirements` / `putMultipliers`** (legacy **`AuraSkills/…/Requirements|Multipliers`** strip); **`FabricSkillsItemReader`** unchanged. **Lore:** **`FabricItemModifierLore`** (requirement + multiplier append). **Perms** **`auraskills.command.item.requirement`**, **`auraskills.command.item.multiplier`**. Suggestions: **`enabledSkillKeysOrGlobal`**. **Tests** permissions. **Docs** `SPRINT_53_MANUAL_ITEM_REQ_MULT.md`, audit/MENU/ledger/backlog, **`projectVersion` 2.3.80 → 2.3.81**. **Not in this sprint:** **armor** `requirement` / `multiplier` (reference has them; Fabric next); **item** **register** / **give**. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`.

## 2.3.80

### Changes

- **Fabric: `item ignore` (Sprint 52):** **`/item ignore add`**, **`/item ignore remove`** on **main hand**; **`FabricItemIgnoreTags`** (`auraskills:ignore_interact` in PDC; **`FabricManaAbilityItemTags`** delegates). **`/item`/`modifier`/`trait`** now **`requires`** **`item.modifier`** per subtree; **`item ignore`** uses **`auraskills.command.item.ignore`**. **Tests** permissions. **Docs** `SPRINT_52_MANUAL_ITEM_IGNORE.md`, audit/MENU/ledger/backlog, **`projectVersion` 2.3.79 → 2.3.80**. **Not in scope:** `requirement` / `multiplier` / `register` / `give`. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`.

## 2.3.79

### Changes

- **Fabric: item/armor held modifier commands (Sprint 51):** **`FabricItemArmorModifierCommands`** — **`/item`**, **`/armor`** (standalone, **`/auraskills`**, **`/sk`**, **`/skill`**) with **`item|armor` → `modifier|trait` →** **add** / **remove** / **list** / **removeall** (main hand). **`FabricSkillsItemWriter`** (PDC list, legacy strip), **`FabricItemModifierLore`**, **`FabricCommandModifierText`** (item list lines). Perms **`auraskills.command.item.modifier`**, **`auraskills.command.armor.modifier`**. **Lang** `item_armor_mod.*`, help. **Tests** permissions. **Docs** `SPRINT_51_MANUAL_ITEM_ARMOR_MODIFIERS.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, `PARITY_BACKLOG`, **ledger** CMD-001/006/007, **`projectVersion` 2.3.78 → 2.3.79**. **Not in scope:** `requirement` / `multiplier` / `ignore` / `register` / `give` (reference has more for **Item** / **Armor**). **Build:** `.\gradlew -PfabricOnly=true :fabric:test`.

## 2.3.78

### Changes

- **Fabric: player modifier commands (Sprint 50):** **`FabricPlayerModifierCommands`** — **`/modifier`** and **`/trait`** (standalone, **`/auraskills`**, **`/sk`**, **`/skill`**) with **add** (optional **operation** + **stack**), **remove**, **list**, **removeall**; **online** targets; **`User`** mutations + **`FabricModifierManager#applyModifiers`**. **`FabricCommandTraitParser`**, **`FabricCommandModifierStackName`**, **`FabricCommandModifierText`**, **`FabricCommandSuggestionProviders#enabledTraitKeys`**. Permission **`auraskills.command.modifier`**. **Tests:** `FabricCommandModifierStackNameTest`, permissions. **Lang** help/usage. **Docs** `SPRINT_50_MANUAL_MODIFIER_TRAIT.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, `PARITY_BACKLOG`, **ledger** CMD-001/005/015/021, **`projectVersion` 2.3.77 → 2.3.78**. **Not in scope:** Bukkit **addtemp**, item/armor modifier commands, **offline** targets, ACF **silent**. **Build:** `.\gradlew -PfabricOnly=true :fabric:test`.

## 2.3.77

### Changes

- **Fabric: leaderboard batch 2 (Sprint 49):** **`/top average`**, **`/top average <page>`**; **`/updateleaderboards`**, **`/updateleaderboards <samples>`** (**1–64**); async **`LeaderboardManager#updateLeaderboards`** with **server-thread** feedback. **`FabricCommandLeaderboardText#resolvePlayerName`** — **name-to-id cache** via **`MinecraftServer#getApiServices()#nameToIdCache`**, else **`?`**. Perms **`auraskills.command.updateleaderboards`**. **Lang** `updateleaderboards.samples`, help. **Tests** permissions. **Docs** `SPRINT_49_MANUAL_LEADERBOARD_BATCH2.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, ledger/backlog, **`projectVersion` 2.3.76 → 2.3.77**. **Not in scope:** unbounded **samples**; offline names with **no** **usercache** entry; usernames in **`UserState`**.

## 2.3.76

### Changes

- **Fabric: player utility commands (Sprint 48):** **`FabricPlayerUtilityCommands`** — **`/multiplier`**, **`/multiplier <onlinePlayer>`**, **`/toggle`**, **`/abtoggle`**, **`/lang <language>`**; **`FabricCommandLocaleArgs`**; **`FabricCommandSuggestionProviders.languageCodes`**. Perms **`auraskills.command.multiplier`**, **`abtoggle`**, **`lang`**. Mirrors on **`/auraskills`**, **`/sk`**, **`/skill`**. **Lang** `utility.players_only`, help. **Tests** `FabricCommandLocaleArgsTest`, permissions. **Docs** `SPRINT_48_MANUAL_PLAYER_UTILITY.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, ledger/backlog, **`projectVersion` 2.3.75 → 2.3.76**. **Caveat:** **`FabricUser#getPermissionMultiplier`** is still **stub** (often **0**); **item** multipliers are real. **Not in scope:** full **LuckPerms**-style permission **multiplier** aggregation on Fabric; ACF **`@Flags("other")`** beyond **online** target for **multiplier**.

## 2.3.75

### Changes

- **Fabric: leaderboard / rank commands (Sprint 47):** **`FabricLeaderboardCommands`** — **`/top`**, **`/skilltop <skill> [page]`**, **`/rank`**, **`/skillrank <skill>`**; **`FabricCommandLeaderboardText`** (paging rank index, online-only name resolution → **`?`** offline in this sprint); perms **`auraskills.command.top`**, **`auraskills.command.rank`** (Bukkit names). Registered on **`/auraskills`**, **`/sk`**, **`/skill`**, and top-level. **Lang** `leaderboard.players_only`, help. **Tests** `FabricCommandLeaderboardTextTest`, permissions. **Docs** `SPRINT_47_MANUAL_LEADERBOARD_RANK.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, ledger/backlog, **`projectVersion` 2.3.74 → 2.3.75**. **Not in scope:** **`top average`** / full Bukkit **`onTop`** arg matrix; offline name resolution via profile cache; **`updateleaderboards`** command.

## 2.3.74

### Changes

- **Fabric: `/sk` and `/skill` compatibility subtree (Sprint 46):** **`FabricBrigadierCommands#aliasSkillsRoot`** chains existing **`FabricXpCommands.xpRoot()`**, **`FabricManaCommands.manaCommandRoot()`**, and **`FabricSkillLevelCommands.skillLevelRoot()`** (each call = new node tree) alongside **`openmenu`**. **Lang** help/usage. **Docs** `SPRINT_46_MANUAL_SK_SKILL_COMPAT_SUBTREE.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, `PARITY_BACKLOG`, ledger **CMD-001/003/004/010/019**. **`projectVersion` 2.3.73 → 2.3.74**. **Not in scope:** ACF literal **`/sk skill`**; new gameplay; freezing final public Granite names. **Tests:** no new automated test (Brigadier trees require full registries in this module’s unit test JVM); use manual doc.

## 2.3.73

### Changes

- **Fabric: `openmenu` + context built-ins (Sprint 45):** **`FabricOpenMenuCommands`** — Brigadier **literals** for **`skills`**, **`stats`**, **`level_progression`**, **`stat_info`**; **`openmenu level_progression [<player>] <skill>`** and **`openmenu stat_info [<player>] <stat>`** (optional target = online player; one-arg forms are self-only). Uses **`FabricAuraSkillsMenus.openLevelProgression`** / **`openStatInfo`** and **`FabricCommandSkillParser`**, new **`FabricCommandStatParser`**, **`FabricCommandSuggestionProviders.enabledStatKeys`**. **`FabricCommandBuiltInMenuId`**, **`openmenu` lang** (console, unknown skill/stat, `opened_for_context`, detail self-opened lines). **Tests** `FabricCommandBuiltInMenuIdTest`. **Docs** `SPRINT_45_MANUAL_OPENMENU_CONTEXT_MENUS.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, `PARITY_BACKLOG`, CMD-013/021 ledger, **`projectVersion` 2.3.72 → 2.3.73**. **Not in scope:** arbitrary custom Slate menu names, JSON **properties** / **page** args (Bukkit), non-builtin menu ids on `openmenu`.

## 2.3.72

### Changes

- **Fabric: command/menu access (Sprint 44):** **`/sk`**, **`/skill`** (default **Skills**; `auraskills.command.skills`); **`FabricOpenMenuCommands`** — **`/openmenu`**, **`/auraskills openmenu`**, **`/sk|skill openmenu`** for built-in **skills** / **stats** + optional online target; **`FabricCommandBuiltInMenuId`**, **`FabricCommandSuggestionProviders.builtInOpenMenuIds`**, **`auraskills.command.openmenu`**. **Lang** openmenu + usage/help. **Tests** `FabricCommandBuiltInMenuIdTest`, permissions. **Docs** `SPRINT_44_MANUAL_ALIASES_OPENMENU.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, CMD-001/013/019 ledger, **`projectVersion` 2.3.71 → 2.3.72**. **Not in scope:** arbitrary Slate menu names, JSON properties/page, context menus without **Skill**/**Stat** context.

## 2.3.71

### Changes

- **Fabric: skilllevel `setall` + `reset` (Sprint 43):** **`FabricSkillLevelCommands`** — **`skilllevel setall <player> <level>`** (`auraskills.command.skill.setlevel`); **`skilllevel reset <player>`** / **`… <player> <skill>`** (`auraskills.command.skill.reset`). **`FabricCommandSkillLevelApply.setAllEnabledSkillsToLevel`** (Bukkit batch order); **`User#resetSkill`** + **`afterSkillResetApplyModifiers`**. **Docs** `SPRINT_43_MANUAL_SKILLLEVEL_SETALL_RESET.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, ledger **CMD-003**, backlog, **usage** string. **Tests** permissions. **`projectVersion` 2.3.70 → 2.3.71**. **Not in scope:** ACF `skills|sk|skill`+`skill` path; offline targets.

## 2.3.70

### Changes

- **Fabric: skill **level** command family (Sprint 42):** **`FabricSkillLevelCommands`** — **`/skilllevel`** and **`/auraskills skilllevel`**: **view**; **add** / **set** / **remove** (online + skill); **`FabricCommandSkillLevelApply.setSkillLevel`** (stats, rewards, modifiers) aligned with Bukkit `SkillCommand`. **Shared:** **`FabricCommandAmountRules.requirePositiveIntForAdd`**, **`FabricCommandMutationFeedback`** for **`SKILL_SETLEVEL_SET`** / **`SKILL_AT_LEAST`**. **Perms** `auraskills.command.skilllevel.view`, `auraskills.command.skill.setlevel` (Bukkit name for mutations). **Lang** `…level.count_positive`, `…skilllevel.view_line`, usage/help. **Tests** `FabricCommandSkillLevelApplyTest`, permissions list. **Docs** `SPRINT_42_MANUAL_SKILLLEVEL_COMMANDS.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, ledger **CMD-003**, backlog, **`projectVersion` 2.3.69 → 2.3.70**. **Not in scope:** `setall` / `reset` / ACF `skills|sk|skill` + `skill` subcommand path; offline targets.

## 2.3.69

### Changes

- **Fabric: bounded **mana** command family + shared numeric helpers (Sprint 41):** **`FabricManaCommands`** — **`/mana`** and **`/auraskills mana`**: self display; **`view <player>`**; **`add` / `set` / `remove` `<player> <amount>`** (online targets, `Traits.MAX_MANA` overflow like reference). **Shared:** **`FabricCommandAmountRules`**, **`FabricCommandMutationFeedback`** (used by **xp** success lines too). **Perms** `auraskills.command.mana` / **`.other`** / **`.add`** / **`.set`** / **`.remove`**. **Lang** `…permission_denied`, **`…mana.console_specify`**, usage string. **Docs** `SPRINT_41_MANUAL_MANA_COMMANDS.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, parity ledger, backlog. **Tests** `FabricCommandPermissionsTest` (new nodes). **`projectVersion` 2.3.68 → 2.3.69**. **Not in scope:** ACF `silent` / `allowOverMax` on **mana**; offline targets.

## 2.3.68

### Changes

- **Fabric: shared player/skill command args + `xp` batch 1 (Sprint 40):** **`FabricCommandPlayerTarget`**, **`FabricCommandSkillParser`**, **`FabricCommandSuggestionProviders`**; **`FabricXpCommands`** — **`/xp`** and **`/auraskills xp`** **view** / **add** / **set** / **remove** (`LevelManager`). **Perms** `auraskills.command.xp.view` / `.add` / `.set` / `.remove`. **Tests** `FabricCommandPlayerTargetTest`, permissions list. **Docs** `SPRINT_40_MANUAL_XP_COMMANDS.md`, `COMMAND_PARITY_AUDIT`, `MENU_AND_COMMANDS_PORT`, parity, backlog, handoff. **`projectVersion` 2.3.67 → 2.3.68**. Not in scope: `silent`, `skills\|sk\|skill` **xp** prefix like Bukkit.

## 2.3.67

### Changes

- **Fabric: command parity audit + shared Brigadier scaffold (Sprint 39):** **`fabric/COMMAND_PARITY_AUDIT.md`** (Bukkit `CommandRegistrar` vs Fabric). **`dev.aurelium.auraskills.fabric.command`:** **`FabricBrigadierCommands#register`**, **`FabricCommandPermissions`**, **`FabricCommandRequirements`**, **`FabricCommandPluginState`**; **`FabricAuraSkillsCommands`** delegates. **First batch:** root **`/mana`** (self **MANA_DISPLAY**; admin subcommands not ported). **Lang** `auraskills.granite.command.auraskills_usage` + **`help`**. **Tests** `FabricCommandPermissionsTest`. **Docs** `SPRINT_39_MANUAL_COMMANDS.md`, `MENU_AND_COMMANDS_PORT`, parity, backlog, handoff. **`projectVersion` 2.3.66 → 2.3.67**. Not in scope: ACF, full tree, or **`sk`/`skill`** alias registration.

## 2.3.66

### Changes

- **Fabric: list page context through detail menus (Sprint 38):** Opening **Level Progression** or **Stat Info** from a paged list stores the current `listPage` on **`FabricMenuContext`** (`ofSkill` / `ofStat` + `withListPage`). **Back** and YAML-disabled early exits reopen **Skills** / **Stats** with `none().withListPage(savedPage)`; the list GUI **clamps** the page if totals shrink. **`FabricAuraSkillsMenus`:** `openLevelProgression` / `openStatInfo` with `return*ListPage`, `open(Id, player, plugin, context)`. **Tests** `FabricMenuContextTest` (skill/stat + list page). **Docs** `SPRINT_38_MANUAL_DETAIL_LIST_PAGE_CONTEXT.md`, `MENU_AND_COMMANDS_PORT`, Sprint 37 manual note, parity, backlog, handoff. **`projectVersion` 2.3.65 → 2.3.66**. Not in scope: new menus or other paging features.

## 2.3.65

### Changes

- **Fabric: paged **Skills** + **Stats** list menus (Sprint 37):** **`FabricListMenuPaging`** (list content capped below slot 45, prev/next, title suffix); **`FabricMenuPagingConfig`** + **`granite.paging`**. **`FabricMenuContext#listPage`** + **`#withListPage`**. Registry reopens the same id with a new page; list clicks resolve **global** index. **Lang** + **`menus/skills.yml` / `stats.yml`**. **Tests** `FabricListMenuPagingTest`, `FabricMenuContextTest`, `FabricBuiltInMenuConfigTest#parse_pagingPrevNext`. **Docs** `SPRINT_37_MANUAL_PAGED_LIST_MENUS.md`, `MENU_AND_COMMANDS_PORT`, parity, backlog, handoff. **`projectVersion` 2.3.64 → 2.3.65**. Not in scope: full Slate list templates or non-top paging.

## 2.3.64

### Changes

- **Fabric: bounded `granite.items` for built-in menus (Sprint 36):** **`FabricMenuItemDefinitions`** — `list`, `main`, `back` as **Minecraft item registry ids** (namespace:path or vanilla path); **YAML parse** normalizes invalid **format** to code defaults; **runtime** uses `Registries.ITEM` with fallback to **book** / **arrow** and **warn** on unknown id. **`FabricMenuYamlSlice`** + **`FabricBuiltInMenuConfig#parse`**. **GUIs:** Skills/Stats row item; Level Progression / Stat Info main + back. **Resources / tests** for `menus/*.yml` and **`FabricBuiltInMenuConfigTest`**. **Docs** `SPRINT_36_MANUAL_BUILT_IN_MENU_ITEMS.md`, `MENU_AND_COMMANDS_PORT`, parity, backlog, handoff. **`projectVersion` 2.3.63 → 2.3.64**. Not in scope: Slate **ItemBuilder**, NBT, per-row templates.

## 2.3.63

### Changes

- **Fabric: context-aware menu registry for `level_progression` + `stat_info` (Sprint 35):** **`FabricMenuContext`** (`none` / `ofSkill` / `ofStat`); **`FabricMenuOpener`** and **`FabricMenuRegistry#open(…, FabricMenuContext)`**; static **Skills/Stats** receive unused empty context. **`FabricMenuLoaders`** registers **four** ids; **`FabricAuraSkillsMenus#open(String,…,context)`**; **`openLevelProgression` / `openStatInfo`** delegate to the registry. **Tests:** `FabricMenuContextTest`, **`FabricMenuRegistryTest`**. **Docs** `SPRINT_35_MANUAL_CONTEXT_MENUS.md`, `MENU_AND_COMMANDS_PORT`, parity, backlog, handoff. **`projectVersion` 2.3.62 → 2.3.63**. Not in scope: full Slate **PlaceholderHelper** or template **context** parity.

## 2.3.62

### Changes

- **Fabric: bounded `granite.layout` for built-in menus (Sprint 34):** **`FabricMenuSlotLayout`** — `list_start_slot`, `list_end_slot_exclusive`, `main_slot`, `back_slot` (0–53 top grid) with **clamp** + **list range** + **main==back** fallbacks and log **warns**. **`FabricMenuYamlSlice`** + **`FabricBuiltInMenuConfig#parse`**. **GUIs:** **`FabricSkillsMenuGui`** / **`FabricStatsMenuGui`** (list range); **`FabricLevelProgressionMenuGui`** / **`FabricStatInfoMenuGui`** (main/back). **Resources:** all four **`menus/*.yml`** include documented **`layout`**. **Tests:** `FabricBuiltInMenuConfigTest` (layout, bad list range, main==back). **Docs** `SPRINT_34_MANUAL_BUILT_IN_MENU_LAYOUT.md`, `MENU_AND_COMMANDS_PORT`, parity ledger, backlog, handoff. **`projectVersion` 2.3.61 → 2.3.62**. Not in scope: full Slate **template** / **SlotPos** engine.

## 2.3.61

### Changes

- **Fabric: YAML `granite` for Level Progression + Stat Info (Sprint 33):** **`menus/level_progression.yml`**, **`menus/stat_info.yml`** (embedded + **`generateUserFile`**). **`FabricMenuYamlSlice`**: `defaultLevelProgression` / `defaultStatInfo`, **`resolveFormatKey`**. **`FabricBuiltInMenuConfig`**: loads both ids; **`parse`** merges **all** `granite.format.*` keys. Detail GUIs use YAML for title, lore, back; when **`granite.enabled: false`**, message + return to **Skills**/**Stats**. **`FabricStatInfoMenuGui`**: YAML checked after **`MENUS_STAT_INFO_ENABLED`**. **Tests:** `FabricBuiltInMenuConfigTest#parse_mergesAllFormatKeys`. **Docs** `SPRINT_33_MANUAL_DETAIL_MENU_YAML.md`. **`projectVersion` 2.3.60 → 2.3.61**. Not in scope: full reward/modifier **Slate** templates.

## 2.3.60

### Changes

- **Fabric: first YAML slice for built-in Skills + Stats menus (Sprint 32):** **`menus/skills.yml`** and **`menus/stats.yml`** (JAR + generated server copy) with a **`granite:`** block: **`enabled`**, **`title_key`**, **`size`** (6×9 validated, warn on mismatch), **`format.list_open_detail`**. **`FabricBuiltInMenuConfig`** loads disk-or-embedded YAML with safe fallback; **`FabricMenuRegistry`** + list GUIs use it; **`/auraskills reload`** re-reads menu YAML. **Common:** **`FileUtil#loadEmbeddedYamlFileOrNull`**. **Tests:** `FabricBuiltInMenuConfigTest`. **Docs** `SPRINT_32_MANUAL_BUILT_IN_MENU_YAML.md`, `MENU_AND_COMMANDS_PORT`, parity, handoff. **`projectVersion` 2.3.59 → 2.3.60**. Not in scope: full Slate **`loadMenus`**, merge dirs, or full `skills.yml`/`stats.yml` template parity.

## 2.3.59

### Changes

- **Fabric: Stats click + Stat Info detail (Sprint 31):** **`FabricStatsMenuGui`** — when **`menus.stat_info.enabled`**, `GraniteContainerMenus#openClickable9x6` + slot-ordered `Stat` list; otherwise read-only (Bukkit `StatsMenu` gate). **`FabricStatInfoMenuGui`**: one-stat name, value, description, trait lines; **Back** → **Stats** via **`FabricAuraSkillsMenus#open(STATS,…)`**. **`FabricAuraSkillsMenus#openStatInfo(Stat,…)`**; **`FabricMenuKeys#STAT_INFO`**. **Lang** `en_us.json`. **Docs/ledger** + **`SPRINT_31_MANUAL_STAT_INFO.md`**. **Test:** `FabricMenuKeysTest` (`stat_info`). **`projectVersion` 2.3.58 → 2.3.59**. Not in scope: full `stat_info.yml` / `ModifierInstances` grid.

## 2.3.58

### Changes

- **GraniteLib + Fabric: clickable 6×9 top slots + Skills → Level Progression (Sprint 30):** **`granitelib`**: `GraniteClickable9x6Handler` (optional `GraniteTopSlotClickHandler`), `GraniteReadOnly9x6Handler` removed; **`GraniteContainerMenus#openClickable9x6`**; read-only = `topClick == null`. **Fabric**: **`FabricSkillsMenuGui`** (click skill book → `FabricAuraSkillsMenus#openLevelProgression`); **`FabricLevelProgressionMenuGui`** (live level/XP, **Back** → Skills via registry); **`FabricMenuKeys#LEVEL_PROGRESSION`** (naming only). **Lang** `en_us.json`. **Docs/ledger:** `MENU_AND_COMMANDS_PORT.md`, `PARITY_*`, **`SPRINT_30_MANUAL_LEVEL_PROGRESSION.md`**. **Tests:** `FabricMenuKeysTest`. **`granitelibVersion` 0.1.0-alpha.1 → 0.1.0-alpha.2**; **`projectVersion` 2.3.57 → 2.3.58**. Not in scope: full Slate/YAML `level_progression` rewards.

## 2.3.57

### Changes

- **Fabric: menu registry + loader scaffold (Sprint 29):** **`FabricMenuRegistry`**, **`FabricMenuDefinition`**, **`FabricMenuOpener`**, stable **`FabricMenuKeys`** (`skills` / `stats` vs Bukkit `MenuFileManager` names), **`FabricMenuLoaders#registerBuiltInMenus`**. Titles pass through registry → **`Text.translatable`**. **`FabricAuraSkills`**: `getMenuRegistry()`; menus registered in **`enable()`**. **`FabricAuraSkillsMenus`**: `open(String)` + enum dispatch to registry. **Tests:** `FabricMenuRegistryTest`. **Docs/manual** Sprint 29. **`projectVersion` 2.3.56 → 2.3.57**. Full YAML/Slate not in scope.

## 2.3.56

### Changes

- **Granite + Fabric: Stats menu + shared routing (Sprint 28):** **`FabricStatsMenuGui`** (enabled stats from `User` + `formatStatLevel` / trait lore vs Bukkit `StatsMenu` / `stats.yml` keys). **`FabricAuraSkillsMenus` + `Id` enum** as single open path; **`FabricSkillsMenuGui`/`FabricStatsMenuGui`** package-private. **Temporary:** `/stats`, `/auraskills stats` (`auraskills.command.stats`). **`FabricMenuHelper`:** `stats.player_stat_entry`, `stats.trait_entry`. **Lang:** `stats_title` / `stats_opened`. **Docs/manual:** `SPRINT_28_MANUAL_GRANITE_MENUS.md`, ledger MENU-004, **`projectVersion` 2.3.55 → 2.3.56**.

## 2.3.55

### Changes

- **GraniteLib + Fabric: Skills menu GUI (Sprint 27 / first menu vertical slice):** **`granitelib`**: `GraniteReadOnly9x6Handler` (read-only large-chest `GENERIC_9X6` slots) + `GraniteContainerMenus#openReadOnly9x6`. **FabricAuraSkills**: `FabricSkillsMenuGui` — books per **enabled** skill with **level / XP** from `User` (permission line when blocked). **Temporary** open: `/skills` and `/auraskills skills` (same `auraskills.command.skills`). **`FabricMenuHelper`**: in-memory `level_progression.stat_reward_entry` for common `MenuHelper` callers. **Lang:** `assets/auraskills/lang/en_us.json`. **Docs/ledger:** `MENU_AND_COMMANDS_PORT.md`, `PARITY_*`, manual **`SPRINT_27_MANUAL_SKILLS_MENU.md`**. **Test:** `FabricMenuHelperTest`. **`projectVersion` 2.3.54 → 2.3.55**.

## 2.3.54

### Changes

- **Fabric: ability lane proof (Sprint 26 / `BukkitAbilityManager` vs `FabricAbilityManager`):** Re-audited the **14** registered per-skill `*Abilities` groups against the Bukkit reference; added **`SPRINT_26_ABILITY_PROOF_MATRIX.md`** (Bukkit vs Fabric **per-family** classification: **Equivalent** / **Partial** / **Needs Regression Proof** / **Missing**) and **`SPRINT_26_MANUAL_ABILITY_REGRESSION.md`** (farming GROWTH_AURA tail, fighting/bleed, damage-pipeline skills, archery, defense, mana abilities). **No new ability implementation** this sprint: highest-confidence remaining work is **documented** farming tail (chorus / trees / edge bamboo) and **regression proof**; updated **`SPRINT_23_ABILITY_PARITY.md`** §4, ledger **`AS-019`**, **`PARITY_BACKLOG.md`**, **`FABRIC_PORT_HANDOFF.md`**. Test: extra case in **`FabricFarmingAbilitiesTest`**. **`projectVersion` 2.3.53 → 2.3.54**.

## 2.3.53

### Changes

- **Fabric: `GROWTH_AURA` batch 3 (Sprint 25 / `FarmingAbilities` tail):** Shared **`onGrowthAuraAfterAgeAtPos`** (random tick + bone meal) with **`BoneMealItemGrowthAuraMixin`** on **`BoneMealItem#useOnBlock`**. **`FarmingAgeableRandomTickGrowthAuraMixin`**: add **`BambooBlock`**, **`WeepingVinesBlock`**, **`WeepingVinesPlantBlock`**, **`TwistingVinesBlock`**, **`TwistingVinesPlantBlock`**. Documented **not** in scope: chorus, structure trees, all edge bone-meal mod blocks — see **`fabric/SPRINT_25_FARMING_GROWTH_AURA.md`**, manual **`fabric/SPRINT_25_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.52 → 2.3.53**.

## 2.3.52

### Changes

- **Fabric: `GROWTH_AURA` batch 2 (Sprint 24 / `FarmingAbilities` parity):** Generalized `FabricFarmingAbilities` growth aura to any block state with an integer **`"age"`** (Bukkit `Ageable`–equivalent) for **`ageDidNotAdvance` / `applyExtraGrowthAfterAura`**, with **`maxAgeForBlockState`** for crops, nether wart, and generic `IntProperty` domains. Mixin **renamed** to **`FarmingAgeableRandomTickGrowthAuraMixin`**, **replacing** `CropWartBlockGrowthAuraMixin`, adding **`CactusBlock`**, **`SugarCaneBlock`**, **`CocoaBlock`**, **`StemBlock`**, **`SweetBerryBushBlock`**, **`KelpBlock`**, **`KelpPlantBlock`**, **`CaveVinesHeadBlock`**, **`CaveVinesBodyBlock`**. **Not** in scope: bamboo-style column growth, weeping/twisting vines (class mapping), full bonemeal parity — see **`fabric/SPRINT_24_FARMING_GROWTH_AURA.md`**, manual **`fabric/SPRINT_24_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.51 → 2.3.52**.

## 2.3.51

### Changes

- **Fabric: ability parity audit + batch 1 (Sprint 23 / `BukkitAbilityManager` vs `FabricAbilityManager`):** All **14** per-skill `*Abilities` groups register on Fabric in the same order as Bukkit. **Batch 1** closes **`FarmingAbilities` gaps** missing vs reference: **`Abilities.GENETICIST`** (extra saturation for plant-based foods on consumption commit, alongside **`FabricItemConsumeHandler`**) and **`Abilities.GROWTH_AURA`** (extra crop ages after a natural **vanilla** age step on **`CropBlock`** and **`NetherWartBlock`**, 50ms deferred like Bukkit). Mixin: **`CropWartBlockGrowthAuraMixin`**. **`BOUNTIFUL_HARVEST`** / **`FARMER`** remain **gathering/XP-adjacent** in other handlers (as on Bukkit’s split). See **`fabric/SPRINT_23_ABILITY_PARITY.md`**. **Not** in this sprint: Bukkit `BlockGrowEvent` for non-`CropBlock` growth (cactus, sugar cane, stems, etc.). Test: **`FabricFarmingAbilitiesTest`**. Manual: **`fabric/SPRINT_23_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.50 → 2.3.51**.

## 2.3.50

### Changes

- **Fabric: double-drop trait (Sprint 22 / Bukkit `DoubleDropTrait` core):** **`FabricDoubleDropTrait`** + **`FabricDoubleDrop`** (`PlayerBlockBreakEvents.AFTER`) — same **block allowlist** and **max_percent** chance cap as Bukkit, **silk** rewrites for **stone** and **grass block**, `FabricRegionManager#isPlacedBlock` + **blocked world** + **`DO_TILE_DROPS`** + non-creative / non-spectator gates; extra stacks via **`FabricLootManager#toInventory`**. `FabricDoubleDrop#register` runs **before** **`FabricPlacedBlockBreakHandler`** (see `FabricLevelManager`). **Out of default Granite scope:** Bukkit **WorldGuard** and **`LootDropEvent`** (documented in **`fabric/SPRINT_22_DOUBLE_DROP_PARITY.md`**). Test: **`FabricDoubleDropTest`** (percent cap). Manual: **`fabric/SPRINT_22_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.49 → 2.3.50**.

## 2.3.49

### Changes

- **Fabric: anvil discount trait (Sprint 21 / Bukkit `AnvilDiscountTrait`):** **`FabricAnvilDiscountTrait`** + **`FabricAnvilDiscount`** (Evalex `formula` + Bukkit fallback curve, `resetFormula` on trait reload) + **`AnvilScreenHandlerAnvilDiscountMixin`** (`@Inject` TAIL of **`AnvilScreenHandler#updateResult`**) to scale **`Property` level cost** as Bukkit’s **`PrepareAnvilEvent`** (rounded `repairCost * (1 - discount)`; a rounded cost of 0 becomes **1**). Disabled-world and trait-enabled gates; screen **owner** only — Bukkit’s **highest-`WISDOM` among viewers** and **`CustomRegenEvent` N/A** noted in **`fabric/SPRINT_21_ANVIL_DISCOUNT_PARITY.md`**. Reuses SPRINT 16 take/cost path (`getLevelCost` / `FabricAnvilTakeContext`). Test: **`FabricAnvilDiscountTest`**. Manual: **`fabric/SPRINT_21_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.48 → 2.3.49**.

## 2.3.48

### Changes

- **Fabric: health regen traits (Sprint 20 / Bukkit `HealthRegenTraits`):** **`FabricHealthRegenTraits`** registers **`HUNGER_REGEN`** + **`SATURATION_REGEN`**. Natural food regen: **`HungerManagerHealthRegenMixin`** `@Redirect` on the two **`ServerPlayerEntity#heal`** call sites in **`HungerManager#update`**, with **`FabricHealthRegen`** matching Bukkit **`EntityRegainHealthEvent`–style addition (`getBonusTraitLevel`) and disabled-world / **`use_custom_delay`** (skip vanilla `heal`) behavior. Custom-delay regen: timer tasks mirror Bukkit period (**ticks × 50 ms**) and **`base` + bonus** with food decrement; **Bukkit `CustomRegenEvent` is not fired** (api-bukkit; document until Fabric ability hooks exist). Test: **`FabricHealthRegenTest`**. Manual: **`fabric/SPRINT_20_MANUAL_VALIDATION.md`**, diff **`fabric/SPRINT_20_HEALTH_REGEN_PARITY.md`**. **`projectVersion` 2.3.47 → 2.3.48**.

## 2.3.47

### Changes

- **Fabric: trait parity (Sprint 19 / batch 1 — `EXPERIENCE_BONUS`):** Audited `BukkitTraitManager.registerTraitImplementations()` vs `FabricTraitManager.registerTraitImplementations()`. Implemented Bukkit **`ExperienceBonusTrait`** parity: **`FabricExperienceBonusTrait`** registered in **`FabricTraitManager`**, with vanilla point scaling in **`PlayerEntityExperienceBonusMixin`** (`@ModifyVariable` on **`PlayerEntity#addExperience(int)`**, server players only; matches Bukkit `PlayerExpChangeEvent` math and disabled-world skip). Unit test: **`FabricExperienceBonusTest`** (pure math). Audit + diff: **`fabric/SPRINT_19_TRAIT_PARITY.md`**. Manual: **`fabric/SPRINT_19_MANUAL_VALIDATION.md`**. **Remaining reference traits on Fabric:** `HealthRegenTraits`, `DoubleDropTrait`, `AnvilDiscountTrait` (see parity doc). **`projectVersion` 2.3.46 → 2.3.47**.

## 2.3.46

### Changes

- **Fabric: potion splash XP (Sprint 18 / Bukkit `PotionSplashLeveler` hardening):** `FabricPotionSplashHandler` now skips **null** `POTION_CONTENTS` and stacks with no base potion and no custom effects (Bukkit `ThrownPotion#getEffects().isEmpty()` parity) via **`lacksPotionEffectForParity`**, skips when **`SourceTypes.POTION_SPLASH`** is disabled, and skips **spectators**. Mixin and handler Javadoc: splash + **lingering** on the same `PotionEntity` at collision; Bukkit `ProjectileHitEvent` **cancel** / `LevelerContext` **region** remain **N/A** / `shouldBlockXp`. Test: `FabricPotionSplashHandlerTest`. Manual: **`fabric/SPRINT_18_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.45 → 2.3.46**.

## 2.3.45

### Changes

- **Fabric: item consume XP (Sprint 17 / Bukkit `ItemConsumeLeveler` hardening):** `FabricItemConsumeHandler` (renamed entrypoint from `onConsumeStarting` to `onConsumeItemCommit`) now skips **spectators** and bails when **`SourceTypes.ITEM_CONSUME`** is disabled, before item matching. Mixin and handler Javadoc describe Bukkit `PlayerItemConsumeEvent` + `HIGHEST` vs `LivingEntity#consumeItem` (consumption <strong>commit</strong>, not use-tick / interrupted start-use). Bukkit `PlayerItemConsumeEvent#isCancelled` / `LevelerContext` location region: still **N/A** / `shouldBlockXp` on Fabric. Manual: **`fabric/SPRINT_17_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.44 → 2.3.45**.

## 2.3.44

### Changes

- **Fabric: anvil XP (Sprint 16 / Bukkit `AnvilLeveler` hardening):** `FabricAnvilTakeContext` captures **left/right** stacks and **`getLevelCost()`** at **`ForgingScreenHandler#onTake`** HEAD for the **output** slot (via **`ForgingScreenHandlerAnvilOnTakeMixin`**, anvil-only; Loom cannot remap `onTake` on `AnvilScreenHandler` alone), so `FabricLootItemMatcher` + `{repair_cost}` match the same operation the player is taking. **`onTake` RETURN** clears any unconsumed pending take. **`onTakeOutput`:** **spectator** skip; fallback to current slots if no snapshot. Manual: **`fabric/SPRINT_16_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.43 → 2.3.44**.

## 2.3.43

### Changes

- **Fabric: grindstone XP (Sprint 15 / Bukkit `GrindstoneLeveler` hardening):** `ScreenHandlerGrindstoneXpMixin` now tracks **server-side** result-slot clicks only; before awarding, `FabricGrindstoneXpHandler#allowsGrindstoneClickForXp` mirrors Bukkit `SourceLeveler#failsClickChecks` (empty **cursor** at click start, **main+hotbar full** gating for non-`PICKUP`/`PICKUP_ALL`-style actions, allow **shift/number key/drop**-style takes only when storage has room). **Spectators** do not receive grindstone XP. `FabricItemInventoryUtil#isPlayerStorageFull` matches Bukkit 36-slot storage fullness. Manual: **`fabric/SPRINT_15_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.42 → 2.3.43**.

## 2.3.42

### Changes

- **Fabric: enchanting table XP (Sprint 14 / Bukkit `EnchantingLeveler` hardening):** For YAML **`unit: {sources.units.exp_requirement}`** (and the legacy Bukkit-typo string), `FabricEnchantingXpHandler` now uses **`EnchantmentScreenHandler#enchantmentPower[buttonId]`** to match Bukkit **`EnchantItemEvent#getExpLevelCost()`**, with **fallback** to `xpLevelBefore - player.experienceLevel` if the field is **0** (e.g. edge timing). **Spectators** do not receive enchanting XP. Mixin + handler Javadoc describe **return-true only** = successful enchant (not preview). Manual: **`fabric/SPRINT_14_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.41 → 2.3.42**.

## 2.3.41

### Changes

- **Fabric: jumping XP (Sprint 13 / Bukkit `JumpingLeveler` hardening):** `FabricJumpingXpHandler` now uses **`FabricXpHandlerUtil.shouldBlockXp(..., JUMPING)`** (shared permission / creative / world / source-type gate with other levelers) instead of a one-off check. **Spectators** and **climbing** players (`LivingEntity#isClimbing`) are skipped before velocity matching to cut false positives; ladder-at-feet and vanilla jump-impulse heuristics remain. **Bukkit** `PlayerMoveEvent` **cancel** / location-API blocks are still **N/A** on the default Fabric path. Manual: **`fabric/SPRINT_13_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.40 → 2.3.41**.

## 2.3.40

### Changes

- **Fabric: mana-ability use XP (Sprint 12 / Bukkit `ManaAbilityUseLeveler` hardening):** `FabricManaAbilityUseXpHandler` now returns early when **`SourceTypes.MANA_ABILITY_USE`** is not in use (same “no such sources in enabled skills” idea as Bukkit’s empty source list + leveler inactivity). In-repo, **all** `FabricManaAbilityManager` providers — Replenish, Speed Mine, Lightning Blade, **Treecapitator**, **Terraform**, **Charged Shot**, **Absorption**, and **Sharp Hook** (mixin) — already called `onManaAbilityActivated` after consumption; this sprint **documents** the full set and rewrites stale handoff text. Bukkit’s **`ManaAbilityActivateEvent` cancel** / plugin-modified `manaUsed` is **N/A** on Fabric (no event bus). Manual: **`fabric/SPRINT_12_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.39 → 2.3.40**.

## 2.3.39

### Changes

- **Fabric: brewing XP (Sprint 11 / Bukkit `BrewingLeveler` hardening):** **`BREW`** still requires a tracked stand owner and an **online** player (unchanged; `FabricBrewingOwnerTracker` + place / use-to-claim). **`TAKEOUT`:** new **`LockableContainerBrewingStandSetStackMixin`** on `LockableContainerBlockEntity#setStack` clears per-slot `TAKEOUT` metadata when a bottle result slot 0–2 becomes **empty** without a brewing GUI click—e.g. **hopper** extraction—so **stale** `brewed` / ingredient history cannot later mis-award a **player** takeout. Reference also only grants TAKEOUT on **player** inventory actions, not hoppers. `FabricBrewingTakeoutTracker.BrewingSlot#clearTakeoutTracking` + manual **`fabric/SPRINT_11_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.38 → 2.3.39**.

## 2.3.38

### Changes

- **Fabric: statistic XP source mapping (Sprint 10 / Bukkit `StatisticLeveler` hardening):** Extracted **`FabricStatisticSourceResolver`** (stat string → vanilla `Stat`). New typed forms: **`killed/<entity>`**, **`killed_by/<entity>`** (`Stats.KILLED` / `Stats.KILLED_BY`), **`dropped/<item>`** (`Stats.DROPPED`). **Known `Stats.CUSTOM` ids** resolved without relying on `hasStat` alone: `damage_dealt`, `damage_taken`, damage shield/resist/absorb variants, `mob_kills`, `player_kills`, `deaths` (see resolver Javadoc). **`FabricStatisticXpHandler`** delegates to the resolver. Duplicate awards vs entity-kill/damage levelers remain a **configuration** concern if both are enabled for the same action. Manual: **`fabric/SPRINT_10_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.37 → 2.3.38**.

## 2.3.37

### Changes

- **Fabric: fishing XP + bonus loot (Sprint 09 / Bukkit `FishingLeveler` + `FishingLootHandler` hardening):** **Bonus loot** runs **before** base fishing XP (Bukkit: `PlayerFishEvent` **HIGH** then **MONITOR**). `FabricFishingXpHandler` re-resolves the catch after `tryRollBonusLoot` and re-matches a `FishingXpSource` so base XP uses the **final** `ItemStack` (reference `getSource` at monitor; bonus may replace the hooked item). **Rare/epic** pools can run when the catch does **not** match any `FishingXpSource` (same as Bukkit: `originalSource` null → `skill` defaults to `Fishing`, pool `else` only sets `source` when `originalSource` was non-null). `FabricFishingLootHandler` open-water + `require_open_water` unchanged; Bukkit `entity` loot and `getExpToDrop` bonus gate still **N/A** on Fabric (see `SPRINT_09_MANUAL_VALIDATION.md`). Exposed `FabricFishingXpHandler#findFishingSource`. Manual: **`fabric/SPRINT_09_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.36 → 2.3.37**.

## 2.3.36

### Changes

- **Fabric: DamageLeveler / entity-DAMAGE path (Sprint 08 / Bukkit `DamageLeveler` + `EntityLeveler#resolveDamager` subset):** **`FabricCombatXpHandler`** — Bukkit **`onDamage` order** (matched source → per-source **cooldown** → **self-inflict** → **defense** shield: **`player.isBlocking()`** only, not Fabric `blocked` after damage) → damage / `mustSurvive` / defense min–max. **`DamageXpSource` damager / excluded** matching now handles **`ThrownItemEntity`** (splash / lingering / egg / snowball / ender pearl) for `include_projectiles` like Bukkit’s `Projectile` + **shooter**. **Entity `DAMAGE` trigger:** new **`FabricEntityDamageAttribution#resolveForEntityLevelerDamage`** (melee with attack/sweep causes only, `ProjectileEntity`, `PotionEntity` with optional `THROWN_POTION` for Alchemy, other thrown); **bleed** fallback unchanged; no loose **`getAttacker()`** for this path (Bukkit `resolveDamager` has no such fallback). **`FabricEntityXpSourceLookup#forEntityDamageOnly`** centralizes `DAMAGE` + `THROWN_POTION`→Alchemy. **`FabricXpHandlerUtil.shouldBlockXp`** for both branches. Manual: **`fabric/SPRINT_08_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.35 → 2.3.36**.

## 2.3.35

### Changes

- **Fabric: entity death / spawner parity (Sprint 07 / Bukkit `EntityLeveler#onEntityDeath` subset):** Shared **`FabricEntityDamageAttribution#resolveKillerOnDeath`** for **`FabricEntityKillHandler`** and **`FabricMobLootHandler`**: direct **player** melee, **player-owned** `ProjectileEntity`, and **`ThrownItemEntity`** (splash/lingering potion and other thrown “non-projectile” projectiles in vanilla NMS), with **`getAttacker()`** fallback. **`FabricBleedListener`**: `BLEED_DAMAGER_BY_VICTIM` + unload cleanup so a **bleed** kill (generic/DoT) keeps credit for death XP and mob loot (Bukkit `getBleedDamager` / PDC parity). **`FabricEntityKillHandler`** now uses **`FabricEntityXpSourceLookup#forEntityDeathKill`** ( **`DEATH` only**, **no DAMAGE fallback** ) with **`FabricDamageCauseMapper`** cause filtering + **`FabricXpHandlerUtil#shouldBlockXp`**. **`FabricEntityXpSourceLookup`**: Bukkit `THROWN_POTION` → **Alchemy** `SkillSource` path aligned with `EntityLeveler#getSource`. **Imports:** `ServerEntityEvents` from **`net.fabricmc.fabric.api.event.lifecycle.v1`** (correct Fabric API location). Manual: **`fabric/SPRINT_07_MANUAL_VALIDATION.md`**. **`projectVersion` 2.3.34 → 2.3.35**.

## 2.3.34

### Changes

- **Docs (Sprint 06): scope hard reset** — Active planning and control files (**`fabric/PAPER_PARITY_PROGRAM.md`**, **`fabric/PARITY_BACKLOG.md`**, **`fabric/PARITY_AUDIT_LEDGER_PHASE1.md`**, **`FABRIC_PORT_HANDOFF.md`**, **`fabric/RELEASE_GATES.md`**, sprint manual notes, **`fabric/PLAYER_GUIDE.md`**, **`fabric/FABRIC_QUICKSTART.md`**) no longer frame third-party Bukkit/Paper **ecosystems** as default roadmap. **Default product scope:** **Granite Skills** + **GraniteLib** on **Fabric** with **Java + Bedrock (Geyser/Floodgate)**; **`bukkit`** remains **reference only**. Ledger blocker tag **`Out of scope`** replaces integration-bridge language for non-Granite work. **Code comments:** `FabricRegionManager`, `FabricRequirementNodes` (region node), `FabricConfigProvider` — de-slanged. **`projectVersion` 2.3.33 → 2.3.34**.

## 2.3.33

### Changes

- **Fabric: placed-block integrity hardening (Sprint 05 / Bukkit `RegionBlockListener` subset):** **`PistonHandlerRegionMixin`** after **`PistonHandler#calculatePush`** transfers `check_replace` placed flags for moved blocks, clears flags for piston-broken blocks. **`FallingBlockEntityRegionMixin`** on **`spawnFromBlock`**: sand/red sand/gravel that was player-placed and loses support removes the flag, then polls (50 ms, Bukkit-style) to re-add if the block lands as the same type (cobweb in path cancels; 200-step cap). **`TreeFeatureGrowContextMixin`** + **`TreeFeatureSetBlockMixin`**: during **`TreeFeature#generate`**, clear placed at block positions that match the sapling **Y** (Bukkit **`StructureGrowEvent`** same-level rule). **`FabricPlacedBlockBreakHandler`** chains **`FabricRegionBlockIntegrity#afterPlacedBlockRemovedOnBreak`**: sugar cane / bamboo / cactus stack cleanup above a break, and 50 ms delayed **BELOW** / **SIDE** support checks using **`FabricBlockXpSourceLookup`** + source equality (Bukkit **`isDifferentSource`**). **`projectVersion` 2.3.32 → 2.3.33**. Manual: **`fabric/SPRINT_05_MANUAL_VALIDATION.md`**. **Gaps:** mushroom / non-`TreeFeature` structure grow, WorldGuard blocked regions, full Bukkit piston edge-for-edge (retract path) if any discrepancy appears in playtests.

## 2.3.32

### Changes

- **Fabric: placed-block tracking + `check_replace` foundation (Sprint 04):** **`FabricRegionManager`** (common `regiondata/*.asrg` NBT, same 32×32 region model as Bukkit) with **`BlockItemPlacedBlockMixin`** (`handleBlockPlace` when a block type matches a `BREAK` `BlockXpSource` with `check_replace`), **`ServerChunkEvents.CHUNK_LOAD`** + **async save** (5 min, Bukkit `RegionListener`), break cleanup via **`FabricPlacedBlockBreakHandler`** (registered last on `PlayerBlockBreakEvents.AFTER`). Wired into **block break XP** (`FabricBlockBreakHandler`—placed blocks skip base XP, **Lucky Miner** direct-ore path still gets **`FabricInteractGatheringLuck`**), **block skill loot** (`FabricBlockLootHandler`), **`FabricBlockLevelerHelper`**, **Speed Mine**, and **Treecapitator**. **`FabricWorldManager`** loads **`check_block_replace.blocked_worlds`**; **`FabricUser#worldNameCompat(ServerWorld)`** for dimension names. **`projectVersion` 2.3.31 → 2.3.32**. Manual: **`fabric/SPRINT_04_MANUAL_VALIDATION.md`**. **Not** full Bukkit `RegionBlockListener` parity (pistons, falling sand, structure grow, support-block tick cleanup remain backlog).

## 2.3.31

### Changes

- **Fabric: mob skill loot (EVT-007 / Bukkit `MobLootHandler`):** `FabricMobLootHandler` registers on `ServerLivingEntityEvents.AFTER_DEATH`, resolves **player** killers (melee or `ProjectileEntity` owner), rolls the default **`mob`** loot table (`NamespacedId.fromDefault("mob")` / `loot/mob.yml`) with **DEATH → DAMAGE** entity source resolution via **`FabricEntityXpSourceLookup`**, **`mobs:`** contexts via **`FabricMobContext` + `FabricMobContextProvider`**, and item/command delivery aligned with **`FabricBlockLootHandler`** (telekinesis-style to-inventory option, optional loot-table XP). **`projectVersion` 2.3.30 → 2.3.31**. Manual: **`fabric/SPRINT_03_MANUAL_VALIDATION.md`**. Same caveats as block loot: no Bukkit `LootDropEvent`, no WorldGuard `CUSTOM_LOOT` flag.

## 2.3.30

### Changes

- **Fabric: block skill loot (EVT-005 / Bukkit `BlockLootHandler`):** `FabricBlockLootHandler` rolls pool loot on `PlayerBlockBreakEvents` (Excavation / Mining / Foraging / Farming providers; item + command loot; `override_vanilla_loot` removes vanilla `ItemEntity` drops near the block in `AFTER` then applies skill drops). Shared block XP source resolution: **`FabricBlockXpSourceLookup`**. **`projectVersion` 2.3.29 → 2.3.30**. Manual: **`fabric/SPRINT_02_MANUAL_VALIDATION.md`**. WorldGuard “custom loot” and Slimefun block guards are not replicated on Fabric; placed-block `check_replace` still waits on placed-block tracking.

## 2.3.29

### Changes

- **Fabric: player death config parity (EVT-014):** `on_death.reset_skills`, `on_death.reset_xp`, and `on_death.reset_xp_ratio` are applied on `ServerLivingEntityEvents.AFTER_DEATH` for `ServerPlayerEntity` via **`FabricPlayerDeathHandler`** (same control flow as Bukkit `PlayerDeath`). **`projectVersion` 2.3.28 → 2.3.29**. Manual steps: `fabric/SPRINT_01_MANUAL_VALIDATION.md`.

## 2.3.28

### Changes

- **Docs: Fabric runtime + Granite + reference baseline + cross-play:** Parity program **H1** is **“Capability & reference-baseline program (Fabric)”** (file **`PAPER_PARITY_PROGRAM.md`**). **Runtime = Java Fabric only**; **`bukkit` = reference inventory**; **shipped = Granite Skills / GraniteLib**; **supported clients = Java + Bedrock via Geyser + Floodgate** (see **`fabric/PAPER_PARITY_PROGRAM.md`**, **`fabric/PARITY_BACKLOG.md`** §Geyser, **`FABRIC_PORT_HANDOFF.md` §0.1/0.3**, ledger **Bedrock** optional columns, **`MENU_AND_COMMANDS_PORT.md`**). **Ledger** = truth, **workbook** = dashboard.
- **Docs: `FABRIC_PORT_HANDOFF.md` deep pass:** Replaced remaining **“vs Paper”**-style phrasing with **Bukkit `reference` / monorepo** language; **§8** stub table **rewritten** (removed obsolete **`FabricNoOpLootManager`** story; **§7.5** `loot_pool` now describes **`FabricLootManager`** path); **§6–7, §11** phases and **§13** summary aligned with **Java + Geyser/Bedrock** acceptance and `PARITY` docs.
- **Release gates:** New **`fabric/RELEASE_GATES.md`** — separates **Gate A** (runnable), **Gate B** (limited production with known gaps), **Gate C** (release-grade: reference + cross-play + final Granite UX). Linked from **`PAPER_PARITY_PROGRAM.md`** and **`FABRIC_PORT_HANDOFF.md`**. Stops a single word “deployable” from standing in for “100% complete.”
- **Chat / action bar / titles:** The Fabric port was using only legacy (§) Adventure deserialization while messages and `messages_*.yml` use **MiniMessage** (e.g. `<aqua>`, `<bold>`) like the Paper mod. `FabricMessageProvider` and `FabricPlatformUtil` now use a **MiniMessage** pipeline aligned with the upstream **Slate** `TextFormatter` (Bukkit). **Adventure** components are turned into NMS `Text` via `GsonComponentSerializer` + `TextCodecs` so colors and styles reach the client (not raw tag strings, not lossy `TextLiteral` of MiniMessage as plain text). **Action bar** and **UI** string paths in `FabricUiProvider` use the same path.
- **Commands:** **Root** `/auraskills` (no subcommand) now shows a **usage** line. **`/auraskills help`** lists subcommands. **Top-level `/skills`** is registered as an alias of **`/auraskills skills`** (same permission) for players who expect a Paper-style `/skills` command. **`projectVersion` 2.3.27 → 2.3.28**.

## 2.3.27

### Changes

- **Fabric entrypoint:** `FabricAuraSkillsMod` now uses the **`main`** entrypoint with **`ModInitializer`** instead of the **`server`** entrypoint with **`DedicatedServerModInitializer`**. The dedicated-server initializer does **not** run for a **singleplayer** or **Open to LAN (integrated) server** world, so chat commands ( `/auraskills` …) never registered there. The **`main`** entrypoint runs in those environments so the command tree and server lifecycle run when an integrated server starts. **Dedicated** Fabric servers are unchanged in behavior. Log line: `Registering /auraskills (version, skills, reload, save)` when commands attach to the server dispatcher. **`projectVersion` 2.3.26 → 2.3.27**.

## 2.3.26

### Changes

- **GraniteLib dependency cutover (Sprint 95):** **FabricAuraSkills** now declares **`depends: granitelib`** ( **`>=0.1.0-alpha.1`**) in **`fabric.mod.json`**. The **default** remapped JAR **does not** Jar-in-Jar-embed **GraniteLib**; install **`granitelib-fabric-0.1.0-alpha.1.jar`** (or compatible) alongside the gameplay mod, **Fabric API**, etc. **Transitional** opt-in: **`-PembedGraniteJij=true`** on **`:fabric`** re-enables the **legacy** nested **`granitelib-fabric-*.jar`** for local or emergency testing only. **No** gameplay, menu, or YAML changes. **GraniteLib** version metadata unchanged. **`projectVersion` 2.3.25 → 2.3.26**.

## 2.3.25

### Changes

- **Fabric / no-JiJ test build path:** **`-PnoGraniteJij=true`** on **`:fabric`** omits the **`granitelib-fabric-*.jar`** nested Jar-in-Jar; **`verifyFabricReleaseBundle`** then **requires** that no nested `granitelib` JAR is present, so strict **Test C** (external `granitelib` only) can be exercised without changing the default release layout. Default builds **unchanged** (still embed JiJ). **No** new **`depends: granitelib`**, **no** gameplay, menu, or YAML changes. **GraniteLib** version still **`0.1.0-alpha.1`**. **`projectVersion` 2.3.24 → 2.3.25** for this build plumbing.

## 2.3.24

### Changes

- **Fabric / JiJ:** Bundle **runtime** dependencies that are **transitive in Gradle** but were **not** nested under **`META-INF/jars/`**, which broke a **plain** Fabric dedicated server before **`Done!`**: **`net.kyori:examination-api:1.3.0`** ( **`adventure-api`** / **`Examinable`**), **`io.leangen.geantyref:geantyref:1.3.11`** ( **Configurate** / **`TypeToken`**), **`org.yaml:snakeyaml:1.28`** ( **configurate-yaml** ). **`verifyFabricReleaseBundle`** requires the nested JARs. **No** `depends: granitelib` change, **no** JiJ removal, **no** gameplay or **Phase1YamlMenu** changes. **GraniteLib** standalone metadata remains **`0.1.0-alpha.1`**. **`projectVersion` 2.3.23 → 2.3.24**.

## 2.3.23

### Changes

- **GraniteLib standalone metadata:** Added **`slate-fabric/src/main/resources/fabric.mod.json`** with mod **`id` `granitelib`**, **`granitelibVersion=0.1.0-alpha.1`** in **`gradle.properties`**, and **`:granitelib-fabric`** `version` + `processResources` so **`${version}`** expands for the remapped JAR. **FabricAuraSkills** still JiJ-embeds **`granitelib-fabric-*.jar`**; **`verifyFabricReleaseBundle`** unchanged (prefix match). **No** `depends: granitelib` on the gameplay mod yet, **no** JiJ removal, **no** gameplay or menu behavior changes. **`projectVersion` (FabricAuraSkills)** **2.3.22 → 2.3.23** to reflect the shipping layout change to the embedded library version line.

## 2.3.22

### Changes

- **Fabric `fabric.mod.json`:** Updated `name`, `description`, `authors`, and added `contact` (homepage, sources, issues) to match fork-accurate **FabricAuraSkills** / **Discount152** branding. **`id`**, **`license`**, **`depends`**, **`entrypoints`**, **`mixins`**, and **`environment`** unchanged. No gameplay or menu behavior changes.

## 2.3.21

### Changes

- **Branding / documentation:** README and `gradle.properties` now use this repository ([Discount152/FabricAuraSkills](https://github.com/Discount152/FabricAuraSkills)) for clone instructions, `repoBaseUrl`, and `releaseTitle` (FabricAuraSkills). Document GraniteLib as embedded **:granitelib-fabric** (JiJ) and point Spigot/Bukkit API content to an explicit upstream reference section. No gameplay, `fabric.mod.json`, or license/attribution file changes in this release.

## 2.3.20

### Changes

- **Fabric / embedded GraniteLib:** Renamed the Java package for `:granitelib-fabric` from `dev.aurelium.slate.fabric` to `dev.granitelib.fabric`. Renamed `SlateFabricBootstrap` to `GraniteFabricBootstrap`. No gameplay or menu behavior changes.

## 2.3.19

### Changes

- **Fabric / Gradle:** Rename Gradle project **`:slate-fabric`** → **`:granitelib-fabric`** with `projectDir` → `slate-fabric/` (no directory rename). Library JAR archive name **granitelib-fabric-*.jar**; **Jar-in-Jar** now includes the library in the remapped mod; **`verifyFabricReleaseBundle`** requires the nested `granitelib-fabric-*.jar`. **Note:** Upstream `main` does not include `slate-platform`; a future module **`:granitelib-platform`** is a separate change when that subproject exists.

## 2.3.12

### New Features

- Add support for Paper 26.1.1 version and world format

### Bug Fixes

- Fix resethealth command not working in 26.1+
- Fix FancyHolograms damage holograms not being removed

## 2.3.11

### New Features

- Add 26.1 support
- Add {skill_name} placeholder to rewards for skill display name

### Changes

- Job selection now works without an economy plugin
- Update Czech, Polish, and Chinese Simplified messages

### Bug Fixes

- Fix Life Steal ability healing players that are dead

## 2.3.10

### New Features

- Add 1.21.11 support
- Add nautilus, zombie nautilus, parched, camel, and camel husk to Fighting and Archery sources

### Bug Fixes

- Fix CustomRegenEvent may only be triggered synchronously
- Fix error when changing some alchemy source menu_item material

## 2.3.9

### New Features

- Add `allow_bone_meal` option to block sources
  - If false, crops that have been fertilized with bone meal will not give skill XP (true by default)
  - Only applies to block sources with `interact` trigger like sweet_berry_bush
  - This reverts default behavior to before 2.3.8
- Support FancyHolograms for damage holograms
  - Must be enabled in config with `hooks.FancyHolograms.enabled`
- Add menu item and template variants
  - Variants are a more powerful version of contexts that can show different items for a single template/item
    based on properties and context filters
  - Variants are defined with a `variants` list of mappings on an item or template
  - Each variant can have the following filtering keys that match an item to a variant:
    - A single `context` or list of `contexts` to filter for templates (if a list, any context can be matched)
    - A `properties` mapping with a set of key-value properties to filter for (if multiple, all properties must be matched)
  - A variant can have the following keys that define the actual item appearance for a given variant:
    - Any keys that define a base item, such as `material`, `key`, and meta keys like `enchantments`
    - A `pos` or `group` and `order`
    - A `display_name` and `lore`
- Allow duplicate of the same template context items using `pos` list

### Changes

- Make backup loading and saving async
  - Backups load much faster by loading users in parallel
- Remove delayed attribute updates for non-Folia servers introduced in 2.3.8 to fix issues
- Fishing command loot now overrides vanilla item loot, instead of both being given

### Bug Fixes

- Fix SQL migration error on older MySQL versions
- Fix SQL key too long using prefix index for modifiers table
- Fix NPE with Nexo when breaking custom blocks
- Fix damage_reduction trait not disabling correctly
- Fix error removing Fleeting on server shutdown
- Fix loot tables giving XP without skill permission
- Fix level up title not working on newer versions

## 2.3.8

### New Features

- Add 1.21.9 and 1.21.10 support
- Add Folia support
- Add `call_block_break_event` option to Treecapitator
- Add `collect` trigger for block sources
  - Triggers when specific blocks (beehive, beenest, cauldron) are harvested with an applicable tool (shears, bottles, or buckets)

### Bug Fixes

- Fix PlaceholderAPI not being registered on startup sometimes
- Fix `spawner_multiplier` for drowned transformed from zombies

## 2.3.7

### New Features

- Add average stat placeholders
  - Add average_stat, average_stat_int, and average_stat_1 placeholders for the player's average stat level
- Add options for Treecapitator and Terraform to consume durability
  - The `durability_multiplier` option determines how much durability should be decreased per block broken
    (1 = 1 durability per block). The default is 0 (disabled).
  - The `max_limit_durability` determines if the number of blocks broken should always be equal or less than the item's durability.
    The default value of false makes the ability only use the `max_blocks` value to limit blocks broken.

### Bug Fixes

- Fix Valor, Stamina, and item modifiers not applying in off-hand
- Fix hide_tooltip not working on items
- Fix some menu items not showing with Nexo

## 2.3.6

### New Features

- Add loot requirements to tables, pools, and loot
  - The `requirements` option can be set on the loot table, a pool, or loot entry
  - Requirements accept the already available Block Requirements types
- The following new requirement types have been added
  - `world`, `biome`, `region`, `item`, and `enchantment`
  - Each type has specific keys below that must be added to define type behavior. These keys are added in the same indent level as `type`.
  - The `world` type requires the player to be in the specified world. Keys:
    - `world` - The name of the world the player is required to be in
  - The `biome` type requires the player to be in the specified biome. Keys:
    - `biome` - The name of the biome the player is required to be in
  - The `region` type requires the player to be in the specified WorldGuard region. Keys:
    - `region` - The name of the WorldGuard region the player is required to be in
  - The `item` type requires the player to hold a specific item. Keys:
    - `item` - The material name of the item the player is required to hold
  - The `enchantment` type requires the player to have a specific enchantment on their held item. Keys:
    - `enchantment` - The name of the enchantment to add a requirement for
    - `level` - The required enchantment level, either singular or a min-max (optional)
    - Examples of valid levels are `1` and `1-3`
- Add new options and tags to customize Treecapitator
  - Add `trunks` tag to the Foraging sources file to configure which sources can activate Treecapitator
  - Add `treecapitator_applicable` tag to the Foraging sources file to configure which sources are broken by Treecapitator
  - Add `max_blocks` option to block sources to set the max number of blocks Treecapitator can break for this trunk type
- Add experimental config options
  - Experimental options are not included in the config by default
  - Each option you want to enable must be manually added in an `experimental` section
  - Add experimental option `experimental.optimize_leaderboard_updating`
    - Whether to enable optimized fetching of user data from the SQL database for leaderboard updates. When enabled, only users updated in the database since the last leaderboard
      update will be fetched after the initial load.

### Bug Fixes

- Fix Nexo hook

### API Changes

- Add SkillsUser#sendAbiltyMessage

## 2.3.5

### Bug Fixes

- Fix item loot not stacking with normal items
- Fix Fishing sources that specify loot_pool not working
- Fix item reward amount value not working

## 2.3.4

### New Features

- Add 1.21.6/1.21.7 support
- Add chance-based enchantments to loot
  - An element in an `enchantments` list can also now be a mapping instead of a string
  - This mapping has the following keys:
    - `name` - The name of the enchantment
    - `level` - The level of the enchantment, supports ranges
    - `chance` - The chance that this enchantment is added to the item, from 0-1 (defaults to 1)
  - The existing string enchantment format of `unbreaking 3` and `unbreaking 1-3` still works
- Add configurable damaged loot
  - The `damage` option can be set on an item loot entry to set the ratio of total durability that should be removed from the given item
    - For example, an item with `damage: 0.4` will have 40% less durability than max, or be at 60% of total durability
  - A random amount of damage can be assigned using the `damage.min` and `damage.max` options, which will select a random damage ratio between the values every time the loot is dropped
- Add skill addlevel command to add to a player's skill level
  - Syntax: `/skills skill addlevel <player> <skill> <level>`
- Add income placeholder to source template in sources menu
- Add option to subtract a percentage of skill XP on death
- Add support for start/end wildcards in source tags

### Changes

- Include item multiplier value in multiplier command
- Change BlockBreakEvent event priority for requirements to lowest
- Improve WorldEdit wand exclusion from Treecapitator

### Bug Fixes

- Fix stat rewards applying when skill permission is negated
- Fix mana not resetting when entering disabled world
- Fix Repairing ability not adding repair cost
- Fix NPE if StatModifier stat is null
- Fix NPE in ArmorModifierListener
- Fix No Debuff ability not working for some effects
- Fix tooltips not hidden on some menu fill items
- Fix requirements being applied in disabled worlds

## 2.3.3

### Bug Fixes

- Fix languages with country codes not working
- Fix NoSuchFieldError when holding items in 1.20-1.20.3
- Fix NPE when applying modifiers

## 2.3.2

### New Features

- Add auraskills.leaderboard.exclude permission to hide players from the leaderboard
  - Setting this to online players requires a relog to take effect (leaderboard still takes time to update)

### Bug Fixes

- Remove legacy item conversion to fix item stacking
- Fix stat info menu not opening with disabled traits

## 2.3.1

### New Features

- Add menus.stat_info.enabled option to disable stat_info menu in config.yml

### Bug Fixes

- Fix item duplication when auto_convert_from_legacy is true

## 2.3.0

### Breaking Changes

- The plugin now requires at least Java 21
- Support for Minecraft 1.18.x and 1.19.x has been dropped

### New Features

- Add 1.21.5 support
- Add stat_info menu
  - This allows players to see exactly how their stat and trait levels are calculated
  - Accessed by clicking any of the stat items (glass panes) in the stats menu (/stats)
  - The menu is tab-based and shows stat modifiers by default. Trait modifiers can be viewed by clicking on the tab buttons on the right.
  - Each tab shows any base value, value from skill rewards, and custom modifiers
- Add percentage-based stat and trait modifiers
  - Modifiers now have an operation value that defines how they are applied
  - The `add_percent` operation multiplies the stat level by 1+value/100
    - For example, a modifier with value 50 multiplies the stat by 1.5
    - The values for all `add_percent` modifiers for a stat are added together before multiplying the stat (additive multiplier)
  - The `multiply` operation directly multiplies the stat level by the value
    - For example, a modifier with value 2 doubles the stat level
    - Each `multiply` modifier is applied separately (multiplicative modifier)
  - All existing modifiers use the `add` operation, which is the basic addition to the stat level
    - All `add` modifiers are applied before any `add_percent` or `multiply` modifiers
  - The following existing commands have been modified to add an operation argument:
    - `/skills modifier add <player> <stat> <name> <value> [operation] [silent] [stack]`
    - `/skills trait add <player> <trait> <name> <value> [operation] [silent] [stack]`
    - `/skills item modifier add <player> <stat> <value> [operation] [lore]`
    - `/skills item trait add <player> <trait> <value> [operation] [lore]`
    - `/skills armor modifier add <player> <stat> <value> [operation] [lore]`
    - `/skills armor trait add <player> <trait> <value> [operation] [lore]`
    - The `operation` argument is optional and defaults to `add`
- Add temporary modifiers
  - Temporary modifiers are automatically removed after certain amount of time
  - Use the following commands to add temporary stat and trait modifiers:
    - `/skills modifier addtemp <player> <stat> <name> <value> <duration> [pauseOffline] [operation] [silent] [stack]`
    - `/skills trait addtemp <player> <trait> <name> <value> <duration> [pauseOffline] [operation] [silent] [stack]`
    - The `duration` argument is in the format `[years]y[months]mo[days]d[hours]h[minutes]m[seconds]s`
      - Examples of valid durations are `5s`, `10m`, and `4d2m1s`
    - The `pauseOffline` argument defines whether the remaining duration should stop counting down when the player is offline (defaults to false)
- Add block requirements
  - Block requirements are customizable restrictions on breaking, placing, or harvesting blocks
  - Requirements are defined in requirement.blocks.list in config.yml
  - Each element in the list can have the following keys:
    - `material` - The name of the block to add requirements for (required)
    - `allow_break` - Whether to ignore requirements on block break (defaults to false)
    - `allow_place` - Whether to ignore requirements on block place (defaults to false)
    - `allow_harvest` - Whether to ignore requirements on block harvest (defaults to false)
    - When none of the above allow options are defined, the block cannot be broken, placed, or harvested when requirements are not met
    - `requirements` - A map list of the requirement nodes for this block. Each element in the list can have the following keys:
      - `type` - The type of requirement, which can be `skill_level`, `permission`, `excluded_world`, or `stat`
      - `message` - The error message to send to the player when the requirement is not met. Supports MiniMessage and PlaceholderAPI (optional).
      - Each type has specific keys below that must be added to define type behavior. These keys are added in the same indent level as `type`.
      - The `skill_level` type requires the player to be at least a specific level in a skill. Keys:
        - `skill` - The name of the skill to add a level requirement for
        - `level` - The minimum skill level the player must be
      - The `permission` type requires the player to have a specific permission node. Keys:
        - `permission` - The permission node required
      - The `excluded_world` type defines a list of worlds that will make the requirement fail if the player is in one of them. Keys:
        - `worlds` - The list of worlds to not allow the player to be in
      - The `stat` type requires the player to be at least a specific stat level. Keys:
        - `stat` - The name of the stat to add a level requirement for
        - `value` - The minimum stat value that the player must be
  - General options related to the block requirement system are in requirement.blocks:
    - `enabled` - Whether block requirements are checked at all
    - `bypass_in_creative_mode` - Whether to ignore block requirements for players in creative mode (defaults to true)
    - `bypass_if_op` - Whether to ignore block requirements for players that are op (defaults to false)
- Add addition protections against removing items from menus
  - Enable by setting `menus.removal_protection` in config.yml to true (false by default)
- Add `speed_reduction` option to Stun ability
- Add translatable messages for command descriptions and update notification
- Add `item_model` support in menu and loot items
- Add `percent_format` and `current_xp_format` options to skills menu
- Add `xp_format` option to sources menu
- Add `format` option to stat reward to configure decimals
- Add Nexo support

### Changes

- Update Polish, Chinese Simplified, and German messages
- Storing modifiers with SQL has been moved from the key_values table to a new modifiers table
  - Existing modifiers will be migrated automatically
  - A new schema_migrations table stores previously applied migrations
- Remove Oraxen support

### Bug Fixes

- Fix default item options like glow being saved when updating files
- Fix errors when Anvil Discount is not loaded

## 2.2.8

### Changes

- Update French, Russian, and Brazilian Portuguese messages

### Bug Fixes

- Fix Jumper and Runner abilities applying to the wrong skill
- Fix error when parsing some damage source cause values
- Fix idle action bar message not updating on reload
- Fix skull profile missing error when opening leaderboard menu
- Update stats on join on the main thread to fix errors

### API Changes

- Add api methods to load and save offline users
  - Use UserManager#loadUser to load a user into memory
  - Use SkillsUser#save to save the user to storage asynchronously

## 2.2.7

### New Features

- Add 1.21.4 support
- Add pale garden blocks to Foraging sources
- Add support for external items anywhere that accepts an item key

### Changes

- Change MythicDamageEvent priority from high to normal
- Support {color} placeholder in leveled_by component of stats menu
- Make anvil discount trait formula configurable in stats.yml
- Change update checker to use Modrinth with loader and version filters

### Bug Fixes

- Fix slimes and groups of mobs attacking rapidly with no cooldown
- Fix hex colors not working in loot entry message
- Fix xp-gain and custom-loot WorldGuard flags with Fishing loot
- Fix offline player skins not working in leaderboard menu
- Fix double plus in lore when adding some trait modifiers

## 2.2.6

### Bug Fixes

- Fix IncompatibleClassChangeError on 1.21.1 and below caused by 2.2.5 update

## 2.2.5

### New Features

- Add 1.21.3 support

### Changes

- The `{skill}` and `{skill_key}` placeholders can now be used anywhere in the level progression menu

### Bug Fixes

- Prevent errors from abnormal server version strings

## 2.2.4

### New Features

- Add more permission nodes with child permissions
  - auraskills.* contains permissions for all commands
  - auraskills.command.user contains permissions users have by default
  - auraskills.skill.* contains permissions to use all skills
  - auraskills.command.item.* contains permissions related to item modifier creation
  - auraskills.command.armor.* contains permissions related to armor modifier creation
  - auraskills.command.admin contains permissions for all admin/op commands

### Changes

- Respect cancelled drops when applying block luck
- Only show loaded skills in profile command
- Update Chinese Simplified, Chinese Traditional, and Indonesian messages

### Bug Fixes

- Fix health value must be between error when changing worlds
- Fix boss bar progress must be between error when max skill level
- Fix NPE when updating leaderboards on MySQL

### API Changes

- SkillsUser#setSkillLevel will now refresh stats, permissions, rewards, and item modifiers
- Add setSkillLevel method with boolean parameter to control whether to refresh stats
- Add CustomManaAbilityBuilder#infoFormats to set the list of menu format names to use in the menu description

## 2.2.3

### New Features

- Add new damage source options
  - Add excluded_damager/excluded_damagers to prevent XP gain from specific entity types
  - Add cooldown_ms to define a delay before XP can be gained again in milliseconds (200 by default)
  - Add damagers option to define a list of valid damagers
- Add job selection cooldown option (jobs.selection.cooldown_sec)

### Changes

- Update German, Polish, and Italian messages
- Make all region loading async

### Bug Fixes

- Fix error loading menu on Spigot 1.21
- Fix typos in excavation loot file
- Implement error handling for global requirement material names
- Fix trait_experience_bonus placeholder not working
- Fix boss bar progress must be between error

## 2.2.2

### New Features

- Add 1.21.1 support
- Add use_permission_cache option to disable LuckPerms multiplier permission cache

### Bug Fixes

- Fix anti-AFK being force enabled after reload
- Skip sending item give command message if empty

## 2.2.1

### Bug Fixes

- Fix cannot measure distance error in PositionHandler
- Fix logs table not being created

## 2.2.0

### New Features

- Add anti-AFK system for preventing AFK XP gain
  - This is an optional feature that detects and blocks repetitive AFK skill XP gain, encouraging active play
  - Must be manually enabled with the `anti_afk.enabled` option in the main config
  - There are 8 types of checks that can block AFK XP gain in the following skills: Farming, Foraging, Mining, Fishing, Excavation, Fighting, Archery, and Defense
  - The checks work in one of three ways: detecting identical player coordinates, facing direction (pitch/yaw), or the entity being involved
  - Checks are configured in the `anti_afk.checks` section
    - Specific checks can be toggled with the `enabled` option
    - The `min_count` option is the number of identical XP gain conditions in a row for the player to be blocked from gaining XP
    - The `max_distance` option for some checks is the threshold for which movement below that value is still considered identical
  - Logging for failing anti-AFK checks can be enabled with the `anti_afk.logging_enabled` option
    - The
      `log_threshold` is an expression determining the count of identical conditions required for logging an event. The min_count variable can be used in this expression to reference the min_count in the checks section for a check type.
    - Logs will be sent to any online player with the auraskills.antiafk.notify permission (op by default)
    - Logs are saved to storage can can be viewed for any player with the `/skills antiafk logs <player> [page] [perPage]` command (auraskills.command.antiafk.logs permission)
  - Important disclaimer: A player failing checks and being logged does not necessarily mean they are cheating or using a macro, as false positives can be common for legitmate players, especially if min_count is low. An example is a player using a mob farm manually can still trigger anti-AFK checks. Always check logged players manually before taking any action.
- Add entity loot for fishing loot tables
  - The new entity loot type adds the ability to catch entity with fishing
  - Defined by a loot entry with `type: entity`
  - The `entity` key defines the type of entity caught
    - Vanilla entities/mobs are defined by name (e.g. `entity: zombie`)
    - MythicMobs can be used with the `mythicmobs:` prefix (e.g. `entity: mythicmobs:SkeletonKing`)
  - The `health` key overrides the entity max health attribute
  - The `damage` key overrides the entity attack damage attribute
  - The `level` key can be used to define the AuraMobs or MythicMobs level
  - The default vertical and horizontal velocities when caught can be overridden with `velocity.horizontal` and `velocity.vertical`
  - The keys `hand`, `off_hand`, `feet`, `legs`, `chest`, and `head` are used to define equipment on the spawned entity
    - The value is a mapping in the same item format as regular item loot (Using keys `material`, `enchantments`, etc.)
- Add boss bar animation using delay when upgrading progress
  - This allows players to visually see the change in progress when a new boss bar is shown
  - Can be toggled with the `boss_bar.animate_progress` option in the main config
- Add support for multiple commands on one loot entry
  - Use a `commands` list of strings on a command loot entry instead of `command`

### Changes

- Optimize leaderboard updating for SQL
- Optimize SQL user loading and saving
  - A user is now loaded in a single statement with subqueries
  - Saving key_values will now utilize batched statements

### Bug Fixes

- Fix disabled abilities still showing in abilities menu
- Fix level up title not working in 1.20.5+
- Fix XP Convert giving extremely high experience values
- Fix boss bar income showing for jobs enabled without economy

### API Changes

- Relocate and create Configurate API wrapper (breaking change)
  - ConfigurationNode has been replaced with the ConfigNode wrapper class in api and api-bukkit
  - Configurate is no longer included in the api module as a dependency
  - Any plugins working with ConfigurationNode in the API will break and must be updated (such as using LootParser, creating custom source types, or ItemManager#parseItem)
  - Updating is done by simply renaming ConfigurationNode mentions to ConfigNode, as most methods are copied over exactly
  - This change was made to fix critical loading errors with plugins like Nova
- Add trait modifier methods to ItemManager
  - Deprecate addModifier, getModifiers, and removeModifier to be replaced by addStatModifier, getStatModifiers, and removeStatModifier
- Add action bar methods to SkillsUser for sending and pausing action bars

## 2.1.5

### New Features

- Add Oraxen support
  - Oraxen note blocks defined in sources will now give XP properly
  - Custom blocks can now be easily defined in sources by prefixing the `block` option of a block source with `oraxen:`
    - For example, `block: oraxen:mythril_ore` will automatically register the block state from Oraxen without needing to manually define a `state`
  - Oraxen items can be directly used in any source `menu_item` option using a string key prefixed with `oraxen:`
    - For example, `menu_item: oraxen:mythril` will use the exact item defined in Oraxen, including any NBT
    - This is defined as a string value directly on the `menu_item` key rather than the map section used for normal menu items
  - Custom loot from Oraxen blocks will drop extra items correctly with Luck traits like Mining Luck
- Add requirement.override_global option to main config
  - If true, global requirements will be ignored if item-specific requirements are defined
- Add mana.cooldown_timer_period option to main config
  - Increasing can help reduce lag caused by TimerCountdown for mana abilities
- Add sql.pool options to main config
  - The new options maximum_pool_size, minimum_idle, connection_timeout, max_lifetime, and keepalive_time are used to configure the Hikari connection pool
  - These should not be changed unless you have issues with connection stability and know what you are doing

### Bug Fixes

- Fix power leaderboard not adding custom skills
- Fix leaderboard still showing for disabled skills
- Fix global requirements and world options not being updated on skills reload
- Fix roman placeholders with value 0 not working
- Fix ManaAbilityRefreshEvent being called invalidly
- Fix hp keep_full_on_increase not working sometimes
- Fix snow golem and mooshroom not giving XP in 1.20.5+

## 2.1.4

### New Features

- Add jobs income batching options to reduce economy lag
  - If jobs.income.batching.enabled is true, accumulated income will be given in an interval instead of immediately
  - The interval_ms option controls the minimum delay between when money is given
  - If display_individual is true, the boss bar will display the original individual income instead of batched

### Changes

- Optimize checking for disabled sources using cache

### Bug Fixes

- Fix XP duplication exploit with land claiming plugins
- Fix resethealth command not working on 1.21
- Fix modifier is already applied error with stun
- Fix NoSuchMethodError on 1.19.4
- Fix PlaceholderAPI not working in level up message
- Fix negative XP gain after negative multipliers

## 2.1.3

### New Features

- Minecraft 1.21 support
  - Add bogged and breeze to Fighting and Archery sources
- Add new action_bar options to main config
  - format_last - If true, parsing of MiniMessage will happen after placeholders are replaced on each send. This makes MiniMessage gradients work at the cost of performance (false by default).
  - update_async - If true, the idle action bar will be updated and sent asynchronously. This is an experimental option (false by default).

### Changes

- The option modifier.auto_convert_from_legacy is now false by default

### Bug Fixes

- Fix Lightning Blade attribute modifier not being removed
- Fix backup debug message spam
- Fix disabled traits not being fully disabled sometimes
- Fix hex colors not working in level up title and chat message
- Fix hex colors not working in action bars

## 2.1.2

### New Features

- Add roman and int placeholders for abilities (thanks Erik)
  - `%auraskills_[ability]_roman%` - Gets ability level as a Roman numeral
  - `%auraskills_[ability]_value_int%` - Gets ability value rounded to an integer
  - `%auraskills_[ability]_value_2_int%` - Gets secondary ability value rounded to an integer if it exists
  - `%auraskills_mability_[ability]_roman%` - Gets mana ability level as a Roman numeral
  - `%auraskills_mability_[ability]_value_int%` - Gets mana ability value rounded to an integer
- Add correct_over_max_level option to main config to prevent skill levels being over max level on join (false by default)
- Add shape option to menu fill items
  - Available values are border, top_row, bottom_row, left_column, and right_column

### Changes

- Revert unnecessary AdvancedEnchantments event ignores, fixing compatibility
- Update German and Chinese Simplified messages

### Bug Fixes

- Fix split slimes and magma cubes not applying spawner_multiplier
- Fix double reloading on addStatModifier (thanks Erik)
  - This fixes extra health resets with plugins like MMOItems
- Fix Charged Shot not applying damage bonus
- Fix NoSuchFieldError on older versions when using mana abilities
- Fix RoseStacker spawner mobs not working with spawner_multiplier
- Fix armor add command not accepting decimal values
- Fix level progression total page count sometimes being wrong
- Fix food level decreasing at full health with use_custom_delay true

## 2.1.1

### New Features

- Add auraskills.jobs.block.[skill] permission to prevent skills from being selected as jobs if set to true (ex: auraskills.jobs.block.mining)
- Add `%auraskils_jobs_active_[skill]%` placeholder that returns true/false whether the job is active (ex: %auraskills_jobs_active_farming%)

### Changes

- Support property placeholders in menu placeholder conditions, such as {skill} for the skill name in certain menus
- Slightly tweaked the jobs item lore style in the level_progression menu

### Bug Fixes

- Fix grindstone experience exploit
- Fix NoSuchMethodError on older versions

## 2.1.0

### New Features

- Add jobs system
  - Jobs is an optional feature that allows players to select skills as jobs to earn money while gaining XP.
  - To use jobs, the jobs.enabled option must be set to true in config.yml
  - Income for all XP sources is configured under jobs.income
    - If use_xp is true, sources will give a multiple of the XP gained based on default.income_per_xp
    - If use_expression is true, sources will give XP based on the result of default.expression
      - The available variables include xp, base_xp (value without multipliers), level (skill level), power, and skill_average
    - If use_final_xp is set to false, the calculation for income_per_xp will exclude all XP multipliers
  - Job selection is configured under jobs.selection
    - Players select a skill as a job using a new item in the level progression menu of a skill (gold_ingot by default)
    - If require_selection is set to false, players will gain income for all skills and selection will be hidden
    - The default_job_limit is the maximum number of skills that can be selected as jobs at the same time
    - The limit can be changed per-player using the auraskills.jobs.limit.[number] permission node (ex: auraskills.jobs.limit.4)
    - The disable_unselected_xp option will block gaining XP in all skills that are not active jobs if set to true
  - Income can also be configured per-source inside the sources files
    - Keys can be added to both the default section or an individual source
    - The following keys can be added to a source:
      - income_per_xp - The income to give per XP gained, works the same as the value in config.yml
      - income - A fixed decimal value for the money to give
      - income_expression - An expression to calculate the income, works the same as the expression in config.yml
  - Menu files will automatically update to add the new items and components to show job selection
    - However, you must manually add a new line `- component: skill_job_active` to the lore of the skill template in menus/skills.yml under `- component: max_level`
    - Everything will still work if this line isn't manually added, players just won't see the "Active Job" displayed in the skills menu
  - New jobs commands:
    - `/skills jobs add <job> [user]` - Joins a specific job for yourself or a different player
    - `/skills jobs remove <job> [user]` - Quits a specific job for yourself or a different player
    - `/skills jobs removeall [user]` - Quits all jobs for yourself or a different player
    - When user is not specified, these commands use the auraskills.command.jobs permission (op by default)
    - To specify a different user, the sender must also have the auraskills.command.jobs.other permission (op by default)
  - New jobs PlaceholderAPI placeholders:
    - `%auraskills_jobs_list%` - Lists active jobs in a comma separated list of default skill names in all lowercase
    - `%auraskills_jobs_list_formatted%` - Lists active jobs in a comma separated list with the skill display name of the config default_language
    - `%auraskills_jobs_count%` - Gets the number of jobs the player currently has active
    - `%auraskills_jobs_limit%` - Gets the maximum number of jobs the player is allowed to have active at the same time
- Add trait commands for adding trait modifiers directly to players
  - `/skills trait add <player> <trait> <name> <value> [silent] [stack]` - Adds a trait modifier to a player with the given name
  - `/skills trait remove <player> <name> [silent]` - Removes a trait modifier from a player with the given name
  - `/skills trait list [player] [trait]` - Lists trait modifiers of a player
  - `/skills trait removeall [player] [trait] [silent]` - Removes all trait modifiers from a player
  - The functionality of these commands is almost identical to the modifier commands for stat modifiers
  - These commands use the existing auraskills.command.modifier permission
- Add item and armor trait commands for adding trait modifiers to items
  - `/skills item|armor trait add <trait> <value> [lore]` - Adds a trait modifier to the held item, with lore by default
  - `/skills item|armor trait remove <trait>` - Removes a trait modifier from the item held
  - `/skills item|armor trait list` - Lists all trait modifiers on the item held
  - `/skills item|armor trait removeall` - Removes all trait modifiers from the item held
  - Where item|armor is shown above, only item or armor should be specified, not both
  - These commands use the existing auraskills.command.item.modifier or auraskills.command.armor.modifier permissions
  - Like stat modifiers, trait modifiers use PDC and do not depend on lore to function
- Add item ignore commands to ignore interactions on specific items
  - This allows you to disable mana abilities on custom items to allow separate right-click functionality
  - `/skills item ignore add` - Adds the tag that ignores the held item from mana ability interactions
  - `/skills item ignore remove` - Removes the tag that ignores the held item from mana ability interactions
- Add format_title option to menus to allow disabling color and MiniMessage parsing of menu titles
  - To use, manually add it to the options section of a menu file (create it if it doesn't exist)
- Add PlaceholderAPI support to level up chat message
- Add `%auraskills_xp_bar_[skill]%` placeholder that gets the XP progress bar shown in the menus

### Changes

- Optimize menus to significantly improve performance for idle open menus
- Change the default skills and level_progression menu files to fully use block style YAML syntax
- The default contexts materials of the skill template in the level_progression menu now match the materials in the skills menu
- Add symbol placeholder support to item modifier lore message
- Automatic backups will now be skipped if the server has too many users (uses new max_users config option)
- Add ensure_scaling_disabled option to hp trait in stats.yml to disable health scaling if the trait is disabled (true by default)
- Update Japanese, Polish, and Chinese Simplified messages

### Bug Fixes

- Fix Bleed and Absorption particle errors on 1.20.5+
- Fix mana add command always setting mana to max
- Fix hex colors in ability messages not working
- Fix added contexts sections breaking some level_progression templates
- Fix hp action_bar_scaling not applying to menus and chat
- Fix multiplier permissions set to false not being ignored
- Fix Disenchanter not working when shift clicking to remove an item with a different item on the cursor
- Fix hide_attributes flag not working in 1.20.5+
- Fix Alchemist duration lore stacking on the same potion

### API Changes

- Add job methods to SkillsUser
- Add Trait#getMenuDisplay to get the trait value formatted as shown in the stats menu
- Fix MenuManager#buildMenu not working when called multiple times to extend the same menu

## 2.0.9

Note: If you are using the 15 skill legacy preset and previously updated to 2.0.8, your sources files for alchemy, agility, and enchanting may have been forcefully updated to include duplicate sources due to a bug in source updating. While this update fixes the bug going forward, you may have to manually remove sources that are duplicated with healing, endurance, and forging, respectively.

### New Features

- Add menu conditions system
  - Conditions on items are used to add requirements for viewing or clicking an item
  - View conditions will hide the item if not all conditions are met
  - View conditions are added with a `view_conditions` key under an item
  - Click conditions will prevent click actions or built-in click behavior from running if not all conditions are met
  - Click conditions are added using the `on_click_conditions` key, or for a specific button trigger like `on_right_click_conditions`
  - The syntax for any condition key is a map list of conditions that all must be met. This is similar to the click actions syntax.
  - Permission condition type:
    - Permission conditions check if the player has a permission
    - Uses `type: permission` (optional due to auto type detection)
    - Must specify a `permission` key for the permission node to check
    - An optional boolean `value` can be specified (defaults to true)
  - Placeholder condition type:
    - Placeholder conditions compare two values that can contain PlaceholderAPI placeholders
    - Uses `type: placeholder` (optional due to auto type detection)
    - Must specify a `placeholder` string key as the left side value to be compared
    - Must specify a `value` string key as the right side value to be compared
    - An optional `compare` string can be specified for the type of comparison operation to perform (defaults to equals)
      - `equals` checks for numerical or string equality
      - `greater_than` checks if `placeholder` is strictly greater than `value`
      - `greater_than_or_equals` checks if `placeholder` is greater than or equal to `value`
      - `less_than` checks if `placeholder` is strictly less than `value`
      - `less_than_or_equals` checks if `placeholder` is less than or equal to `value`
    - Any `compare` other than `equals` requires both `placeholder` and `value` to be evaluated to doubles
- Add menu `on_open` and `on_close` actions for running actions when a menu is opened or closed
  - These are map lists of actions defined directly in the top-level of the menu file
  - `on_close` will run even if the player switches to a different menu immediately
- Add sound menu action
  - Plays a sound for a player
  - Uses `type: sound` (optional due to auto type detection)
  - Must specify a `sound` string key as the sound type to player. This uses the vanilla names matching the /playsound command.
  - An optional `category` string can be specified (defaults to master)
  - An optional `volume` number can be specified (defaults to 1)
  - An optional `pitch` number can be specified (defaults to 1)
- Add syntax to duplicate default menu items with placeholders
  - Creating a new item with a name in the format `item_name(1)` will replace all the display_name and lore placeholders exactly like the default `item_name`.
  - Any number can be used within the parenthesis
  - This is useful for duplicating default items across multiple slots with different materials and custom_model_data for resource packs
- Add syntax for duplicating the exact same menu item easily across multiple slots
  - Specify a list of slot values with the `pos` key instead of a single value

### Changes

- Click action types will now be automatically detected, so specifying a `type` is no longer required

### Bug Fixes

- Fix duplicate source updating for legacy preset
- Fix command actions not working without PlaceholderAPI

## 2.0.8

### New Features

- Minecraft 1.20.5 and 1.20.6 support
- Add hide_tooltip option to items in menus/loot to completely hide the tooltip
  - Tooltips are hidden automatically for menu fill items
  - Only works on 1.20.5+

### Changes

- Various optimizations for servers with high player counts
  - Optimize BlockLeveler with cache for sources
  - Optimize multiplier permission lookup with cache using LuckPerms events
  - Optimize Treecapitator
- Treecapitator now gives XP for all blocks broken by default
  - This can be disabled in mana_abilities.yml by setting give_xp to false
- Update Korean messages

### Bug Fixes

- Fix hex colors in skill names not working fully in menus
- Fix mana ability is not loaded error
- Fix Grappler ignoring region protection

### API Changes

- Slate is now relocated in the AuraSkills jar, so if you use dev.aurelium.slate packages with the api, you should relocate them to dev.aurelium.auraskills.slate using the Gradle Shadow or Maven Shade plugin
- Add MainConfig#getStartLevel and getHighestMaxLevel
- Add MenuManager#registerGlobalReplacer

## 2.0.7

### New Features

- Add trait placeholders
  - `%auraskills_trait_[trait]%` gets the effective level of a trait
  - `%auraskills_trait_[trait]_bonus%` gets the bonus level of a trait (level excluding the base value)
  - `%auraskills_trait_[trait]_menu%` gets the trait in the same format displayed in the stats menu
- Add mana ability placeholders (thanks Erik)
  - `%auraskills_mability_[ability]%` gets the player's mana ability level
  - `%auraskills_mability_[ability]_value%` gets the mana ability value
  - `%auraskills_mability_[ability]_active%` returns true if mana ability is active, false otherwise
- Add MythicMobs hook to fix issues with damage (thanks Erik)
  - Add takeMana mechanic
    - Syntax: takeMana{m=number}
  - Add giveSkillXP mechanic
    - Syntax: giveSkillXP{xp=number,s=skill}
  - Add hasMana condition for MythicCrucible
    - Syntax: hasMana{m=number}
- Add cause and excluded_cause options to entity source
  - These options can be optionally added to filter DamageCause
- Add directly_to_inventory option to config.yml to make all bonus item drops from luck traits and loot always add directly to the player's inventory instead of being dropped in the world
  - This option does not affect vanilla item drops

### Changes

- Bleed no longer creates invulnerable damage frames
- Add Turkish, Finnish, and Thai messages

### Bug Fixes

- Fix disabled abilities showing in level progression menu items
- Fix hex colors in stat names not working in menus
- Fix the WorldGuard hook not loading blocked_check_replace_regions option
- Fix user data being reset in some cases
- Fix block luck not applying to players in Adventure mode
- Fix CustomSkill not showing defined messages in menus

### API Changes

- Add openMenu methods and registerContext to MenuManager
- Add parseItem and parseMultipleItems to ItemManager, allowing parsing of ItemStack from a ConfigurationNode
- Add passesFilter to ItemManager
- Add loot API for accessing existing loot tables and registering new loot types
  - New loot tables can be loaded by using NamespacedRegistry#setLootDirectory
- Add SourceManager API for getting sources of a type
- Remove the extra repositories that were required to load the dependency in 2.0.6

## 2.0.6

### New Features

- Add support for custom menus
  - Create a new file in the menus folder to create a custom menu
  - Follows the same format as existing menus, though templates and components will not work
  - Supports PlaceholderAPI and click actions like existing menus
- Add openmenu command
  - Syntax: `/skills openmenu <menuName> [player] [properties] [page]`
  - If player is not specified, the menu opens for the sender
  - The properties argument is a JSON string required to open some menus
    - For example, `{"skill":"Skill:mining"}` should be passed when opening the level_progression, abilities, sources, and leaderboard menu
  - The page argument starts at 0 for the first page
  - Requires the auraskills.command.openmenu permission (defaults to op)
- Add ability placeholders
  - `%auraskills_[ability]%` gets the ability level
  - `%auraskills_[ability]_value%` gets the ability value
  - `%auraskills_[ability]_value_2%` gets the secondary value of the ability if there is one
  - Replace `[ability]` with the default English name of an ability in lowercase
  - AuraSkills placeholders will now show in the tab completion of /papi parse

### Bug Fixes

- Disable level up chat message for empty string
- Fix Minecraft version parsing error on initial releases like 1.20
- Placed saplings that grow into logs will now give XP
- Fix track option not working in level_progression menu
- Fix LootDropEvent Cause being unknown for some loot tables
- Fix ProtocolLib error with Geyser players
- Fix Terraform crash with AdvancedEnchantments

### API Changes

- Add MenuManager API to extend existing and create custom menus
  - Accessible with AuraSkillsBukkit#getMenuManager
  - Can be used to define behavior for custom items and templates in default or custom menus
  - Add NamespacedRegistry#setMenuDirectory for loading external menu files, which are automatically merged with default menu files before being loaded
- Make TerraformBlockBreakEvent accessible in the API

## 2.0.5

### New Features

- Add track and use_track options to sources menu
  - Used to rearrange the source items in any way rather than just a rectangle
- Add custom_model_data option to source item filter meta
  - To use, add it to a custom XP source item filter section for filtering a specific model data

### Changes

- Update messages and move identical messages across languages to global.yml

### Bug Fixes

- Fix Regions API errors
- Fix hex colors not working in titles and boss bar
- Fix trailing color codes not working in stat symbols
- Fix user skill levels being below start_level when increased
- Fix multiplier command displaying incorrect values
- Fix newlines not working in reward chat messages
- Fix Retrieval detecting tridents as arrows
- Fix skill reset command not using config start_level
- Fix Lightning Blade being activated by thorns
- Fix Alchemist replacing lore and not working for custom effects
- Fix hex colors not working in skill messages
- Fix errors with WorldGuard 7.1

## 2.0.4

### New Features

- Add xp_format and percent_format boss bar options
- Add options to sources menu to change source template area

### Changes

- Reduce jar size by removing unnecessary libraries
- Update it and ru messages (regen required to get changes)

### Bug Fixes

- Fix mana ability cooldowns not being saved when re-logging
- Fix placeholders not working after PlaceholderAPI reload
- Fix NoSuchMethodError on older versions
- Fix long delay in leaderboard updating on startup
- Fix acf section of messages file not applying
- Fix abilities showing in menus and messages above max_level
- Fix trait max_mana is not loaded error
- Fix mana ability is not loaded error
- Fix formatting not working in menu titles
- Fix stack overflow when opening skills menu with too much XP
- Fix mana command help not showing parameter names

## 2.0.3

### New Features

- Add formula option to damage_reduction trait
- Add scale_xp_with_health entity source option
  - This is true by default for entity sources with damage trigger
- Add mana.enabled option to more easily disable mana

### Bug Fixes

- Fix skills menu migration breaking
- Fix permission-blocked skills increasing stat levels
- Fix entity sources with damage trigger not scaling XP with damage
- Fix Bleed not giving XP on entity death/damage
- Fix disabled traits still showing in stats menu
- Fix skill name commands not working

### API Changes

- Add DamageXpGainEvent

## 2.0.2

### New Features

- Add excluded_enchantments option for Enchanted Strength in abilities.yml
- Add lore_wrapping_width option in config.yml

### Bug Fixes

- Fix NPCs being unable to attack mobs
- Fix Fleeting not being removed after heal commands
- Fix console error when using Vault without an economy plugin

## 2.0.1

### New Features

- Add formula support in money rewards

### Bug Fixes

- Fix item modifier NBT conversion breaking items
- Fix action_bar.enabled option not fully working
- Fix message file headers
- Fix rank command error

## 2.0.0

### New Features

- Customizable XP sources
  - New config files for configuring sources of each skill in the sources folder.
  - Each source now has a specific type and options that fully define when XP is gained. For example, all sources for breaking blocks have type block.
  - New sources can be added just like default ones, with the type options allowing much more customization and control.
  - Examples of what is possible:
    - Gaining XP when a player breaks a block matching any specific block state (allows custom blocks).
    - Gaining XP for increasing any Minecraft statistic, like the existing running and walking sources.
    - Gaining XP for consuming a specific item.
    - Gaining XP for enchanting or brewing a custom item.
    - Gaining XP for fishing up a custom item.
  - Sources are no longer tied to their default skill, they can be switched around to any skill.
- Modular skill system
  - The new skills.yml file is where individual skills are now configured.
  - Abilities and mana abilities are no longer tied to their default skill, they can be moved between skills.
  - Custom skills, stats, abilities, and mana abilities can be added using the new API.
- New abilities
  - New Farming ability: Growth Aura
    - Crops with growth stages within 30 blocks of you grow {value}% faster.
    - Unlocks at farming 5 by default.
  - New Archery ability: Retrieval
    - Arrows you miss will be instantly retrieved after 3s up to {value} blocks away.
    - Unlocks at Archery 1 by default.
  - New Fighting ability: Parry
    - Missing a sword swing within 0.25s before being hit reduces damage by {value}% and cancels knockback.
    - Unlocks at Fighting 1 by default.
- New stats
  - Crit Damage, Crit Chance, and Speed have been added as individual stats.
  - The old Crit Chance Archery ability and Crit Chance Fighting ability have been replaced with normal stat rewards, allowing a new ability to be added for each.
  - The Speed stat always shows the player's accurate speed, where 100 = 100% (normal) speed. While skills don't directly increase speed by default, you can use it in rewards or modifiers.
- Traits are a new system that makes stats more modular and configurable.
  - A trait represents a single mechanic that a stat changes/improves. For example, the Wisdom stat increases experience gain, reduces anvil costs, and increases max mana.
  - These aspects are now represented as the individual traits experience_bonus, anvil_discount, and max_mana.
  - Traits can be moved between stats.
  - Both stats and traits can be easily disabled with one option, instead of having to remove every stat reward.
  - When linking a trait to a stat, the modifier option determines how much of the trait you gain for each stat level.
  - When a trait has a modifier of 1 (meaning it's the same number of the stat), it can be displayed instead of the stat value.
  - This allows the health stat to always display the accurate HP amount you have in menus, including the default 20 HP and HP from other plugins.
- Luck Rework
  - Instead of increasing the Minecraft Luck attribute, which most players don't understand, Luck now increases the new traits Farming Luck, Foraging Luck, Mining Luck, Fishing Luck, and Excavation Luck.
  - These traits increase the chance for extra item drops of any XP source of those skills.
  - When a player's luck trait is 150 for example, they have a guaranteed +1 drop and a 50% chance for +2 drops.
  - Luck traits are also increased by the specific ability in each gathering skill, such as Bountiful Harvest.
  - The Triple Harvest Farming ability has been removed, since Bountiful Harvest just increases Farming Luck, doing the same thing.
- Mob and Farming loot tables
  - Custom loot tables can now be created for killing mobs and harvesting farming crops.
  - Create the loot/mob.yml file to add mob loot tables and specify specific mob types with the mobs pool option.
  - Any skill with block sources can now have their own custom loot tables, including farming. Create a file named after the skill, such as loot/farming.yml.

### Changes

- The plugin has been renamed from AureliumSkills to AuraSkills. This means the data folder is now plugins/AuraSkills.
  - Why? The new name is shorter, simpler, and more meaningful. The original plugin name didn't have a meaning when first made, and was pretty long to say and type.
  - For plugins that hook into AureliumSkills, the renaming means compatibility with Beta versions won't break, and a new hook system can be implemented by developers with the brand new and improved API.
  - Plugins will be able to support both Beta and 2.0 at the same time easily.
  - The new data folder allows migration to be implemented more easily, without overriding existing files. This means any problems from migration can be reverted.
  - PlaceholderAPI placeholders now use the prefix %auraskills_. Old placeholders using %aureliumskills_ will still work, but you should gradually convert your placeholders over to the new prefix.
  - All permissions have been renamed and reorganized. Old ones will not work. See the permissions list for the up-to-date list.
- Skill merging and removal
  - Because some skills were too slow to level up and had abilities that weren't very useful, some skills have been merged/removed by default. This brings the total skill count from 15 to 11.
  - The existing 15 skills can still be used by applying the legacy config preset using /skills preset load legacy.zip.
  - If you migrated from Beta, the legacy preset will be automatically applied and you will still have 15 skills. The points below will not apply.
  - Endurance has been merged into Agility.
  - Healing has been merged into Alchemy.
  - Forging has been merged into Enchanting.
  - The XP sources from the removed skills (Endurance, Healing, and Forging) have been added to the skills they merged with, so you can still gain XP in the same way.
  - The abilities deemed most useful out of the two skills have been kept, the rest have been not included in default configs.
  - No abilities have been deleted, you can still add them back in skills.yml.
  - Sorcery has been removed from defaults since it never had any abilities.
- Config format
  - The format and location of config files has signifcantly changed.
  - All keys in config.yml (Main config) now use underscores instead of hyphens.
  - Skill configuration has been moved to skills.yml.
  - Stat configuration has been moved to stats.yml.
  - Ability configuration has been moved to abilities.yml.
  - Mana ability configuration has been moved to mana_abilities.yml.
  - Sources configuration has been moved to the sources folder.
- Message format
  - Messages in the menus section, as well as skill, stat, and ability descriptions no longer contain formatting. Formatting is now fully done in the menus configuration.
  - Legacy color codes with & have been replaced with MiniMessage tags. Legacy codes will still work if you want to use them.
  - Newlines (\n) have been removed from most messages since menus will insert them automatically through lore wrapping.
  - Messages that were the same for every language have been moved to a new messages/global.yml file.
  - The messages for block and mob names have been removed, as these messages are now replaced on the client-side depending on the language the client is using.
- Menu format
  - Legacy color codes with & have been replaced with MiniMessage tags. Legacy codes will still work if you want to use them.
  - Added menu components, which are lines for certain templates that are inserted into the lore.
  - Added a formats section that defines formats for some items. Most of these were moved from the messages file.
  - Added different lore line types, with options for automatically wrapping to new lines if the current line is too long.
  - Added groups, which allow contexts to be defined by an order within an area of the menu, instead of a static position.
  - Placeholders that replace directly from the messages file are now in double curly braces like {{this}}.
  - Placeholders in single curly braces are data placeholders replaced by the plugin directly, usually numbers.
  - Context sections for templates are now under a contexts section instead of directly under the template.
  - The unlocked, locked, and in_progress templates in the level_progression menu now have their context from 1-max_level of the skill instead of 1-items_per_page.
- Default config changes
  - Added a start_level option to the main config to define what skill levels players start at by default. Players now start at skill level 0, instead of 1.
  - boss_bar.mode has been changed to single.
  - enable_roman_numerals is now false.
  - Added options to configure the critical hologram colors; the default damage_holograms.colors.critical.digits now start at white instead of gray.
  - auto_save.enabled is now true.
  - The max level of all skills is now 100 instead of 97.
  - Abilities of some skills have been changed due to the skill merging.
  - The modifier for health and hp is now 1, so the health stat matches the Minecraft HP value.
  - The health stat reward values in the rewards files have been changed to 0.4 to account for this. This means players will see +0.4 Health when leveling, which is accurate to the action bar.
  - The hearts option in the hp trait has been changed to show more visual hearts earlier to account for less overall possible HP.
  - Mana regeneration has been heavily reduced to make mana an actual relevant cost (0.1 to 0.02).
  - The modifier for Strength has been decreased from 0.5 to 0.4
  - Modifiers for some traits have been scaled differently from the existing stat modifier values.
  - Base mana regen is now 0.1 per second instead of 1.
  - Overflow mana (over max mana) is no longer allowed.
- Item NBT format
  - The way item/armor modifiers, requirements, and multipliers are stored on items has changed.
  - The new format uses the built-in PersistentDataContainer API instead of an external library dependent on NMS code.
  - Old items should be automatically converted to the new format when held.
- Damage
  - Most damage modifiers, except Strength, are now additive instead of multiplicative.
  - This means the percent increases for Crit Damage, tool master abilities, and First Strike will be added together before multiplying the damage.

### Technical Changes

- Codebase structure
  - The codebase has been split into multiple modules to allow the api to be independent from implementation.
  - The package for the plugin classes is now dev.aurelium.auraskills instead of com.archyx.aureliumskills.
- API
  - The 2.0 releases adds a brand-new API that is much more capable and extensive than before. It also supports adding custom content (skills, stats, abilities).
- The plugin is now licensed under GPL3. For one, this means that forks or modifications to the plugin that you distribute must also have their source-code available.
