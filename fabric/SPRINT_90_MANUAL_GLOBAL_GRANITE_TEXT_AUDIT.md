# Sprint 90 — Manual: global Fabric Granite `Text.translatable` audit

| Field | Value |
|--------|--------|
| **Sprint** | **90** — Fabric-side **`auraskills.granite.*`** translation key audit |
| **Build / artifact** | **`FabricAuraSkills-2.3.121.jar`** (`projectVersion` **2.3.121**) |
| **Bedrock / Geyser** | **Not tested** — deferred (**`SPRINT_87_MANUAL_BEDROCK_GEYSER_VALIDATION.md`**) |

## Search / audit scope

- **Grep / scan:** `fabric/src/main/java/dev/aurelium/auraskills/fabric/**` for `Text.translatable("auraskills.granite` (and equivalent).
- **Automated guard:** **`FabricGraniteJavaSourcesTranslatableScanTest`** — walks the same tree and fails on the banned snippet.
- **Prior sprints (context):** **83** core usage/help/menus/mana; **84** xp/skilllevel view; **88** built-in menu GUIs; **89** `/sources` paged chat + `bad_sort`.

## Files audited / converted (call sites)

All remaining **`Text.translatable(...)`** usages targeting **`auraskills.granite.*`** in the audited tree were replaced with **`FabricGraniteText.key(...)`** in:

- **`FabricStorageCommands`**
- **`FabricBackupCommands`**
- **`FabricPresetCommands`**
- **`FabricItemArmorModifierCommands`**
- **`FabricPlayerUtilityCommands`**
- **`FabricCommandConfirmation`**
- **`FabricClaimItemsCommands`**
- **`FabricSkillShortcutCommands`**
- **`FabricAntiAfkCommands`**
- **`FabricManaAbilityCommands`**
- **`FabricProfileCommands`** (including dynamic header keys via **`FabricGraniteText.key(headerKey)`**)
- **`FabricTransferCommands`**
- **`FabricResetHealthCommands`**
- **`FabricPlayerModifierCommands`**
- **`FabricLeaderboardCommands`**
- **`FabricCommandAmountRules`**

**Sort logic, permissions, storage/backup/preset behavior:** unchanged — text resolution only.

## Call sites left / allowlist

- **None** under **`src/main/java/dev/aurelium/auraskills/fabric`** for **`Text.translatable("auraskills.granite…")`** — enforced by **`FabricGraniteJavaSourcesTranslatableScanTest`**.
- **`FabricGraniteText.java`** Javadoc still mentions **`Text#translatable`** for explanation only (not a runtime call).
- **Outside Fabric module:** not part of this sprint (e.g. **`common`**, **`bukkit`**, other modules may still reference Granite keys in non-Fabric code paths).

## Tests added / updated

- **`FabricGraniteJavaSourcesTranslatableScanTest`** — full-tree regression guard.
- **`FabricGraniteEnUsKeysTest.sprint90GlobalCommandGraniteKeysPresentInEnUs`** — representative **`en_us.json`** keys for profile, confirmation, amount rules, storage info header, reset health, item/armor mod errors.
- **`FabricMenuGuiTranslatableScanTest`** — unchanged list (still guards menu + openmenu + sources files; global scan covers overlaps).

## Build / test (maintainer)

```text
.\gradlew -PfabricOnly=true :fabric:test :fabric:build
```

**Bukkit/common:** avoid touching; if unavoidable: `.\gradlew :bukkit:compileJava`.

## Java operator validation (vanilla client + console)

Spot-check that feedback is **readable** (no raw **`auraskills.granite.*`** keys, no raw MiniMessage tags in Granite-resolved lines):

| Area | Suggested commands |
|------|---------------------|
| Storage | `/storage`, `/storage info` |
| Backup | `/backup`, `/backup info`, `/backup list` |
| Preset | `/preset`, `/preset list` |
| Item | `/auraskills item` (usage / errors as applicable) |
| XP / skilllevel | `/auraskills xp view <player> <skill>`, `/skilllevel view <player> <skill>` |
| Sources | `/sources <skill>`, `/sources <skill> ascending` |
| Profile / misc | `/profile skills <player>`, `/profile stats <player>` if used |

**Console / RCON:** e.g. `sources farming`, `sources farming ascending` (no leading slash if your setup uses that form).

## Known limitations

- **Bedrock/Geyser:** not validated this sprint.
- **Non-Fabric code paths** (other modules, resources-only) were not exhaustively audited here.
- **`FabricGraniteText`** uses server **`Language`** + **`String.format`** for `%s`-style patterns; complex client-only translation features are out of scope.

## Checklist (operator)

- [ ] JAR **`FabricAuraSkills-2.3.121.jar`** (or newer with these commits) on test server
- [ ] Full restart after swap
- [ ] Java client: storage / backup / preset / item / xp / skilllevel / sources / profile samples — **no raw granite keys** in chat or menus tested
- [ ] Console/RCON: `/sources` chat path if used — **no raw keys**
- [ ] Bedrock: **Skip** (deferred)

## Recommended next sprint

- **Sprint 87** when a Bedrock tester exists, **or**
- Broader **cross-module** Granite key consistency (e.g. `common` message paths) if product scope expands — separate from Fabric raw-key closure.
