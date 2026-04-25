# Sprint 65 — command parity reconciliation (Fabric)

**Build:** **≥ 2.3.93** (`gradle.properties` → `projectVersion`).

**Goal:** One **hygiene** pass after Sprints **39–64** — align the **Fabric** Brigadier tree, **help** strings, and **parity docs** with **reality**, without new gameplay or large features.

## Reference inventory (Bukkit)

`bukkit/.../CommandRegistrar#registerBaseCommands` registers (Paper/ACF):

`SkillsRootCommand`, `StatsCommand`, `SkillCommand`, `ManaCommand`, `ModifierCommand`, `ItemCommand`, `ArmorCommand`, `ProfileCommand`, `BackupCommand`, `XpCommand`, `PresetCommand`, `StorageCommand`, `OpenMenuCommand`, `ManaAbilityCommand`, `TraitCommand`, `JobsCommand`, `AntiAfkCommand`.

**ACF** `skills_alias` = **`skills|sk|skill`** — one class **`SkillsRootCommand`** backs all three literals.

## Fabric vs reference — coverage summary

| Question | Answer |
|----------|--------|
| **1. Which Bukkit base commands have Fabric equivalents?** | **Most** of `registerBaseCommands` except **`ManaAbilityCommand`**, **`JobsCommand`**, **`AntiAfkCommand`**; **per-skill** `SkillCommands.*` gated by config — **Missing** on Fabric. |
| **2. Still Missing?** | **ManaAbility**, **Jobs**, **AntiAfk**, **per-skill skill commands** (see `COMMAND_PARITY_AUDIT` table). |
| **3. Partial?** | **Most** ported families — see audit **Status** column (Slate menus, ACF flags, async, etc.). |
| **4. Equivalent?** | **Rare** — **Partial** is the normal label; differences are documented per row. |
| **5. Extra Fabric convenience roots?** | **Top-level** **`/backup`**, **`/preset`**, **`/storage`**, **`/profile`**, **`/transfer`**, **`/claimitems`**, **`/openmenu`**, **`/mana`**, **`/skilllevel`**, **`/xp`**, **`/top`**, **`/skilltop`**, **`/rank`**, **`/skillrank`**, **`/updateleaderboards`**, **`/multiplier`**, **`/toggle`**, **`/abtoggle`**, **`/lang`**, **`/modifier`**, **`/trait`**, **`/item`**, **`/armor`**, **`/sources`**, **`/resethealth`** — not all exist as Bukkit **dedicated** top-levels; see matrix. |
| **6. Root/mirror gaps fixed in S65?** | **`/skills`** had a **reduced** subtree vs **`/sk`** / **`/skill`**; now **shared** via **`FabricBrigadierCommands#skillsAliasSharedTree`**. |

## Root / mirror matrix (post–Sprint 65)

**Legend:** **yes** = same **Brigadier literal subcommand** exists under that root (or the root **is** the family). **—** = not applicable or **intentionally** not mirrored (e.g. **`/stats`** is its own root, not ` /skills stats `).

| Command family | Top-level | /auraskills | /skills | /sk | /skill | Primary permission(s) | Status |
|----------------|----------:|-------------|--------:|----:|-------:|------------------------|--------|
| Default **skills** menu | — | **skills** literal | default | default | default | `auraskills.command.skills` | Partial |
| **stats** menu | **/stats** | **stats** literal | — | — | — | `auraskills.command.stats` | Partial |
| **reload** / **save** | — | **yes** | — | — | — | `reload` / `save` | Partial |
| **help** / **version** | — | **yes** | — | — | — | `version` for **help** node (known quirk) | Partial |
| **openmenu** | **/openmenu** | **yes** | **yes** | **yes** | **yes** | `auraskills.command.openmenu` | Partial |
| **top** / **skilltop** / **rank** / **skillrank** / **updateleaderboards** | **/top**, **/skilltop**, **/rank**, **/skillrank**, **/updateleaderboards** | **yes** | **yes** | **yes** | **yes** | `top`, `rank`, `updateleaderboards` | Partial |
| **multiplier** / **toggle** / **abtoggle** / **lang** | **/multiplier**, **/toggle**, **/abtoggle**, **/lang** | **yes** | **yes** | **yes** | **yes** | `multiplier`, `abtoggle`, `lang` | Partial |
| **modifier** (player stat) / **trait** | **/modifier**, **/trait** | **yes** | **yes** | **yes** | **yes** | `auraskills.command.modifier` | Partial |
| **item** / **armor** | **/item**, **/armor** | **yes** | **yes** | **yes** | **yes** | `item.*`, `armor.*` (see `FabricCommandPermissions`) | Partial |
| **xp** | **/xp** | **yes** | **yes** | **yes** | **yes** | `auraskills.command.xp.*` | Partial |
| **mana** | **/mana** | **yes** | **yes** | **yes** | **yes** | `auraskills.command.mana.*` | Partial |
| **skilllevel** | **/skilllevel** | **yes** | **yes** | **yes** | **yes** | `skilllevel.view`, `skill.setlevel`, `skill.reset` | Partial |
| **claimitems** | **/claimitems** | **yes** | **yes** | **yes** | **yes** | `auraskills.command.claimitems` | Partial |
| **sources** | **/sources** | **yes** | **yes** | **yes** | **yes** | `auraskills.command.sources` | Partial |
| **resethealth** | **/resethealth** | **yes** | **yes** | **yes** | **yes** | `auraskills.command.resethealth` | Partial |
| **transfer** | **/transfer** | **yes** | **yes** | **yes** | **yes** | `auraskills.command.transfer` | Partial |
| **profile** | **/profile** | **yes** | **yes** | **yes** | **yes** | `auraskills.command.profile` | Partial |
| **backup** | **/backup** | **yes** | **yes** | **yes** | **yes** | `backup`, `backup.load` | Partial |
| **preset** | **/preset** | **yes** | **yes** | **yes** | **yes** | `auraskills.command.preset` | Partial |
| **storage** | **/storage** | **yes** | **yes** | **yes** | **yes** | `auraskills.command.storage` | Partial |

**Notes**

- **`/auraskills`** includes **staff** **`reload`** and **`save`**; **`/skills`** / **`/sk`** / **`/skill`** do **not** — use **`/auraskills`** (by design; matches **Bukkit** putting **reload** / **save** on the main plugin command, not on every alias).
- **Sprint 65** code change: **`skillsAliasSharedTree`** — **`/skills`** now matches **`/sk`** / **`/skill`** for all **SkillsRootCommand**-shaped subtrees.

## Permission summary

- All nodes in **`FabricCommandPermissions`** use the **`auraskills.command.*`** prefix (see **`FabricCommandPermissionsTest`**).
- **Staff / data** commands: default **deny** in **`FabricCommandRequirements`** where the sprint specified (e.g. **backup**, **preset**, **storage**, **modifier**).
- **Deviations from Bukkit `plugin.yml` defaults** remain documented in **`COMMAND_PARITY_AUDIT`** per family (Fabric often **deny**-by-default for admin tools).

## Fixes made (Sprint 65)

1. **`FabricBrigadierCommands`:** Introduced **`skillsAliasSharedTree`**; **`/skills`**, **`/sk`**, **`/skill`** register the **same** `.then()` chain.
2. **Logger** one-line registration message updated (**Sprint 65** tag + clarity).
3. **`en_us.json`:** **`auraskills_usage`** and **`help`** strings updated to describe **`/skills` = `/sk` = `/skill`** and list major families without stale “**only** claimitems…” wording.
4. **`FabricCommandPermissions`:** **`STORAGE`** javadoc references **Sprint 64** mutations.
5. **Parity docs** + **Changelog** + **version** bump.

## Remaining **Missing** (command lane, not S65 scope)

- **`ManaAbilityCommand`**, **`JobsCommand`**, **`AntiAfkCommand`**, **per-skill** **`SkillCommands.*`** — see **`COMMAND_PARITY_AUDIT`**.

## Remaining **Partial** (representative)

- Almost all ported rows — **menus** (Slate vs Fabric built-in), **ACF** help, **async** completions, **`/auraskills help`** using **`version`** permission, etc.

## Java / Bedrock validation

- **No** new product **proof** in this sprint — **reconciliation** + **unit** test run only.

## Next lane (suggestion)

- **Menu** slate parity or **ManaAbility** — pick per **PARITY_BACKLOG** / product priority; **not** part of S65.

## Pass / fail

- [x] **`.\gradlew -PfabricOnly=true :fabric:test`** run on the ship commit.
- [x] **`/skills <sub>`** exists for the same **sub** list as **`/sk <sub>`** (code review + shared tree).

See **`fabric/COMMAND_PARITY_AUDIT.md`**.
