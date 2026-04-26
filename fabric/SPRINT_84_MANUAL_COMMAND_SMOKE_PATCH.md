# Sprint 84 — Java command smoke patch (code + operator checklist)

| Field | Value |
|--------|--------|
| **Sprint 84** | **Code** (in-repo) + **operator** re-validation on **`FabricAuraSkills-2.3.117.jar`**. |
| **JAR** | **`FabricAuraSkills-2.3.117.jar`** (`projectVersion` **2.3.117**). |
| **Bedrock / Geyser** | **Not tested** unless explicitly run. |

## Operator findings (pre-fix)

1. **XP / skilllevel:** Command ran but chat showed raw keys **`auraskills.granite.command.xp.view_line`** and **`auraskills.granite.command.skilllevel.view_line`** — same root cause as Sprint **83**: **`Text.translatable`** on the wire for clients without mod lang.
2. **Item / XP roots:** Prefixed Granite **`item`** / **`xp`** iterations failed or conflicted — **vanilla** registers **`/item`** and **`/xp`**; merged Brigadier hid or broke Granite top-level registration.
3. **Storage / backup / preset:** Failures persisted with tester perms — **console** often lacks explicit LP nodes; **`/storage mergeskills`** had **no** permission on the **`mergeskills`** literal while **`info`** did, so Brigadier could treat **`mergeskills`** as the only visible branch when **`info`** failed, yielding confusing parse errors for **`/storage info`**.

## Code fixes (summary)

| Area | Change |
|------|--------|
| **XP view line** | **`FabricXpCommands`**: **`FabricGraniteText.key`** for view feedback + **`player_not_found`**. |
| **Skilllevel view line** | **`FabricSkillLevelCommands`**: **`FabricGraniteText.key`** for **`skilllevel.view_line`** + **`player_not_found`** paths. |
| **Top-level collision** | **`FabricBrigadierCommands`**: removed **`dispatcher.register`** for **`FabricItemArmorModifierCommands.itemCommandRoot()`** and **`FabricXpCommands.xpRoot()`**. Granite **`item`** / **`xp`** remain under **`/auraskills`**, **`/skills`**, **`/sk`**, **`/skill`**. |
| **Admin permissions** | **`FabricCommandRequirements.permissionOrNonPlayerSource`**: non-player **`ServerCommandSource`** passes **`requires`** (console/RCON). Applied in **`FabricStorageCommands`**, **`FabricBackupCommands`**, **`FabricPresetCommands`**. |
| **Storage Brigadier** | **`.requires(STORAGE)`** on **`literal("mergeskills")`**; removed redundant inner **`requires`** on merge **`to`** argument. |
| **Sk alias** | **`/sk`**, **`/skill`** default deny: **`FabricGraniteText`** for **`permission_denied`**. |

## Canonical syntax (do not use vanilla‑colliding top-level)

**XP view** (Granite; requires **`auraskills.command.xp.view`** for in-game players):

- **`/auraskills xp view <onlinePlayer> <skillKey>`**
- **`/skills xp view <onlinePlayer> <skillKey>`**
- **`/sk xp view <onlinePlayer> <skillKey>`**
- **`/skill xp view <onlinePlayer> <skillKey>`**

Do **not** document **`/xp …`** as Granite — vanilla owns **`/xp`**.

**Skilllevel view:**

- **`/skilllevel view <onlinePlayer> <skillKey>`** (standalone root remains)
- **`/auraskills skilllevel view …`**, **`/skills|/sk|/skill skilllevel view …`**

**Item** (Granite; subcommands **`modifier`**, **`trait`**, **`ignore`**, **`requirement`**, **`multiplier`**, **`register`**, **`unregister`**, **`give`** — see **`FabricItemArmorModifierCommands`**):

- **`/auraskills item …`**, **`/skills item …`**, **`/sk item …`**, **`/skill item …`**

Do **not** document top-level **`/item register`** / **`/item give`** as Granite — vanilla owns **`/item`**.

**Storage / backup / preset** (staff; default-deny perms):

- **`/storage info`**, **`/backup info`**, **`/backup list`**, **`/preset list`**, **`/backup create`** — also under **`/auraskills`**, **`/skills`**, **`/sk`**, **`/skill`** where registered in **`FabricBrigadierCommands`**.

## Operator validation checklist (post‑**2.3.117**)

- [ ] **`/auraskills xp view <player> <skill>`** — readable line, **no** raw **`auraskills.granite.command.xp.view_line`**.
- [ ] **`/skills xp view …`** (or **`/sk`**, **`/skill`**) — same.
- [ ] **`/skilllevel view <player> <skill>`** — readable line, **no** raw **`skilllevel.view_line`**.
- [ ] **`/auraskills item`** (suggestions) — **`modifier`**, **`register`**, etc. — **not** **`/item`** at root.
- [ ] **`/storage info`** as **console** and as **permissioned** in-game tester.
- [ ] **`/backup info`**, **`/backup list`**, **`/preset list`**, **`/backup create`** — same.
- [ ] Normal player **without** admin perms — **denied** or hidden as expected.

## Related

- **`SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`** — update Pass/Fail after your run.
- **`COMMAND_PARITY_AUDIT.md`** — surface notes for **`/xp`** / **`/item`**.
- **`Changelog.md`** — **2.3.117**.
