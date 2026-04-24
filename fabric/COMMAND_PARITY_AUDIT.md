# Bukkit (reference) vs Fabric — command parity (Granite)

**Runtime product:** **Fabric** + **Geyser/Floodgate**; **Bukkit** `CommandRegistrar` + ACF is the **in-repo reference only**, not a second shipped runtime.

**Sprint 39** establishes this audit and a **shared Brigadier scaffold** (`fabric/.../command/FabricBrigadierCommands` and helpers). **Sprint 40** adds **shared online player + skill parsing** (`FabricCommandPlayerTarget`, `FabricCommandSkillParser`, `FabricCommandSuggestionProviders`) and the first **`xp`** batch. **Sprint 41** adds the bounded **`/mana`** family (self + `view` + add/set/remove), shared **`FabricCommandAmountRules`** and **`FabricCommandMutationFeedback`**, and perms `auraskills.command.mana.*` (Sprint 41). **Sprints 42–43** add the bounded **`/skilllevel`** line (view, add, set, remove, **setall**, **reset**) + **`FabricCommandSkillLevelApply`**, and perms `auraskills.command.skilllevel.view`, `auraskills.command.skill.setlevel`, `auraskills.command.skill.reset`. **Sprint 44** adds **`/sk`**, **`/skill`** (alias default → **Skills** + **`openmenu`**) and **bounded** **`openmenu`** for built-in **skills** / **stats** (top-level **`/openmenu`**, **`/auraskills openmenu`**, JSON/properties **not** ported). **Sprint 45** extends **`openmenu`** with **typed** **skill** / **stat** args for **level_progression** and **stat_info** (same `auraskills.command.openmenu`), reusing **Fabric** menu **registry** + **`FabricMenuContext`** (no Bukkit `JsonArg` / `page` parity). **Sprint 46** chains the existing **`xp`**, **`mana`**, and **`skilllevel`** **Brigadier** families under **`/sk`** and **`/skill`** (same `*Root()` builders as the standalone top-levels — new node trees, shared executors). **Sprint 47** adds **leaderboard** / **rank** top-levels (**`/top`**, **`/skilltop`**, **`/rank`**, **`/skillrank`**) and mirrors them on **`/auraskills`** and alias roots, backed by **common** **`LeaderboardManager`**. **Sprint 48** adds **player utility** roots **`/multiplier`**, **`/toggle`**, **`/abtoggle`**, **`/lang <language>`** (and mirrors), using **common** **`User`** + **`MessageProvider`** + **`LevelManager`**, `auraskills.command.*` perms. **Sprint 49** adds **`/top average [page]`**, **`/updateleaderboards [samples]`** (async refresh + feedback), and **offline-ish** display names via **`MinecraftServer#getApiServices()#nameToIdCache()`** when cached (else **`?`**). **Sprint 50** adds **player-scoped** **`/modifier`** and **`/trait`** (**add** / **remove** / **list** / **removeall**; **online** targets; shared **`auraskills.command.modifier`**; **not** `addtemp`, item/armor trees). **Sprint 51** adds **held-item** **`/item`**, **`/armor`** — **`modifier`**|**`trait`** subnodes (**add** / **remove** / **list** / **removeall**; **main hand**; **`FabricSkillsItemWriter`** PDC + legacy strip; perms **`auraskills.command.item.modifier`**, **`auraskills.command.armor.modifier`**). **Not** in Sprint 51: **item**/**armor** `requirement`/**multiplier**/**ignore** subnodes. **Final** public **Granite** command names/aliases are **not frozen** in this document.

**Reference root:** ACF `skills_alias` = `skills|sk|skill` (`CommandRegistrar#registerReplacements`) → commands attach under **`/skills`**, `/sk`, `/skill` **plus** dedicated classes noted below.

---

## 1) Status key

| Status | Meaning |
|--------|---------|
| **OK** | Implemented on Fabric with behavior aligned to the reference for the covered surface |
| **Partial** | Present but narrowed, stubbed, or missing subcommands/arguments vs reference |
| **Missing** | Not on Fabric |
| **N/A (ref)** | Reference-only or out of default Granite scope (e.g. Jobs bridge) — still listed for traceability |
| **Proof** | Code present; needs regression/ cross-client proof |

---

## 2) Top-level and `/auraskills` (Fabric)

| Reference | Fabric | Status | Notes |
|-----------|--------|--------|--------|
| `SkillsRootCommand` default → open `skills` menu | `/auraskills skills`, `/skills`, **`/sk`**, **`/skill`** (Sprint 44) → **Skills**; **Sprint 46+** — compatibility subtrees; **Sprint 47–49** — leaderboard (+ **updateleaderboards**, **top average**) + **multiplier** / **toggle** / **abtoggle** / **lang** on top-level and under **`/auraskills`** / **`/sk|skill`** | Partial | Bukkit: Slate `skills` menu; Fabric: built-in registry; **not** full `SkillsRootCommand` (e.g. `claimitems`) |
| `StatsCommand` @Default @CommandAlias("stats") | `/auraskills stats`, `/stats` | Partial | Same as above for **Stats** |
| `SkillsRootCommand` `reload` | `/auraskills reload` | Partial | Staff default deny; `reloadFromDisk` |
| `SkillsRootCommand` `save` | `/auraskills save` | Partial | `saveAllUserProfiles` |
| `SkillsRootCommand` `version` | `/auraskills version` | Partial | Data dir path; not full Bukkit `version` text |
| `SkillsRootCommand` `help` (ACF `CommandHelp`) | `/auraskills help` | Partial | Short line; Bukkit has full ACF help pages |
| `ManaCommand` (common) | `/mana`, `/auraskills mana`, **`/sk|skill mana`** (**Sprint 46** — same `mana` subtree as standalone) | Partial | **Sprint 41:** default self (`MANA_DISPLAY`); `view` / `add` / `set` / `remove` (online `player` + `amount`); no ACF `silent` / `allowOverMax` flags — reference overflow rules via `Traits.MAX_MANA` only |
| `SkillsRootCommand` `top`, `skilltop` (ACF overload: page / skill / `average`) | **`/top`**, **`/top average [page]`**, **`/skilltop <skill> [page]`** (+ mirrors), `auraskills.command.top` | Partial | **Sprint 49** — **`top average`** (paged; **`NumberUtil.format2`** on average skill level); no single-word **`/skilltop` == `/top`** (Granite: **`/top`** = power, **`/skilltop <skill>`** = per-skill) |
| `SkillsRootCommand` `rank`, `skillrank` (ACF: same handler) | **`/rank`** (all skills) + **`/skillrank <skill>`** (+ mirrors), `auraskills.command.rank` | Partial | **Sprint 47** — two roots vs one overloaded subcommand; **ranks** need **`LeaderboardManager`** data (periodic **common** sorter) |
| `SkillsRootCommand` `lang` | **`/lang <code>`** (+ mirrors), `auraskills.command.lang` | Partial | **Sprint 48** — **`FabricCommandLocaleArgs`**; suggestions from **loaded** message codes; **reload** to add languages |
| `SkillsRootCommand` `multiplier` | **`/multiplier`**, **`/multiplier <player>`** (+ mirrors), `auraskills.command.multiplier` | Partial | **Sprint 48** — **LP**-style `auraskills.multiplier.*` on **Fabric** still **stubbed** in **`FabricUser#getPermissionMultiplier`** (often **0**); **item** multipliers from **User** + **`LevelManager`** are **live** |
| `SkillsRootCommand` `toggle`, `abtoggle` (ACF alias) | **`/toggle`**, **`/abtoggle`** (separate **Brigadier** literals; + mirrors), `auraskills.command.abtoggle` | Partial | **Sprint 48** — **IDLE** action bar only; **`setShouldSave(true)`**; gated by **config** `action_bar.enabled` |
| `SkillsRootCommand` `resethealth` | — | Missing | |
| `SkillsRootCommand` `sources` | — | Missing | |
| `SkillsRootCommand` `claimitems` | — | Missing | |
| `SkillsRootCommand` `transfer` | — | Missing | |
| `SkillsRootCommand` `updateleaderboards` | **`/updateleaderboards [samples]`** (+ mirrors), `auraskills.command.updateleaderboards` (default **op** / fabric-perms **`false`**) | Partial | **Sprint 49** — runs **`LeaderboardManager#updateLeaderboards`** on **async** pool, messages on **server** thread; optional **1–64** samples + average **ms** line (Bukkit used unbounded **int**; capped here) |
| `SkillCommand` `skill setlevel\|addlevel\|setall\|reset` (+ Fabric **`remove`**) | `/skilllevel`, `/auraskills skilllevel`, **`/sk\|skill skilllevel`** (Sprint 46) — **view** / **add** / **set** / **remove** / **setall** / **reset** (online) | Partial | Bukkit: `skills\|sk\|skill` + `skill` sub, not the **`/skilllevel`** *word*; Fabric **compatibility** uses **`/sk skilllevel`** (Sprint 46), not a literal **`/sk skill <sub>`**; Fabric-only **`remove`**; full ACF **`skill`/`SkillCommand`** subnode parity still open |
| `XpCommand` `xp add\|set\|remove` | `/auraskills xp …`, `/xp …`, **`/sk\|skill xp …`** (Sprint 46) — **add** / **set** / **remove** `<player> <skill> <amount>`; permissions `auraskills.command.xp.*` (defaults like Bukkit **op** for mutations) | Partial | **Sprint 40** + **Sprint 46** alias line; no `silent` flag |
| `XpCommand` (extra) | `/xp view …`, **`/sk\|skill xp view …`** (Sprint 46) | Partial | **Sprint 40:** read-only; not in reference Bukkit file; uses `auraskills.command.xp.view` (default **allow**) — lock down with LP if needed |
| `ModifierCommand` | **`/modifier`**, **`/auraskills modifier`**, **`/sk\|skill modifier …`**, `auraskills.command.modifier` (default **deny** on fabric-perms) | Partial | **Sprint 50** — **stat** modifiers on **online** players: **add** (`player` `stat` `name` `value` [operation] [stack]), **remove**, **list**, **removeall**; uses **`User`** + **`FabricModifierManager#applyModifiers`**; **not** Bukkit **addtemp** / item modifiers |
| `TraitCommand` | **`/trait`**, **`/auraskills trait`**, **`/sk\|skill trait …`**, same `auraskills.command.modifier` | Partial | **Sprint 50** — same shape for **trait** id + name + value (optional operation + stack); **not** item/trait item pipelines |
| `ItemCommand` (large tree) | **`/item`**, `/auraskills item`, `/sk\|skill item` — `modifier\|trait` + **add** / **remove** / **list** / **removeall** (held **item**; stat/trait + optional **operation** + **lore**); `auraskills.command.item.modifier` | Partial | **Sprint 51** — **stat** + **trait** modifier path only; **not** `requirement` / `multiplier` / `ignore` / `register` / `give` |
| `ArmorCommand` | **`/armor`**, same mirrors; `auraskills.command.armor.modifier` | Partial | **Sprint 51** — same as **Item** for **armor** `ModifierType` (held “armor”); **not** `requirement` / `multiplier` (reference has these) |
| `ProfileCommand` | — | Missing | |
| `BackupCommand` | — | Missing | |
| `PresetCommand` | — | Missing | |
| `StorageCommand` | — | Missing | |
| `OpenMenuCommand` (Slate) | **Bounded:** `/openmenu`, `/auraskills openmenu`, `/sk|skill openmenu` — **literals** **`skills`**, **`stats`**, **`level_progression`**, **`stat_info`**; list menus + optional **online** target; detail menus: **`openmenu level_progression [<player>] <skill>`**, **`openmenu stat_info [<player>] <stat>`**; `auraskills.command.openmenu` | Partial | **Sprint 45** — no arbitrary custom `menuName`, no JSON `properties` / `page` (Bukkit); no other Slate file names on `openmenu` |
| `ManaAbilityCommand` | — | Missing | |
| `JobsCommand` | — | N/A (ref) | Out of default Granite product scope (see `PARITY_BACKLOG`) |
| `AntiAfkCommand` | — | Missing | |
| `SkillCommands.Farming` … `Forging` (per root) | — | Missing | Gated in reference by `ENABLE_SKILL_COMMANDS` + per-skill config |

**Fabric-only behavior notes**

- **`/auraskills help`** still uses `auraskills.command.version` in `requires` (same as pre–Sprint-39) — Bukkit uses `auraskills.command.help` for the help subcommand. Aligning the Fabric node is a follow-up; see `FabricCommandPermissions#HELP`.
- **Permissions** use **fabric-permissions-api**; defaults match typical **LuckPerms**-style `true` for player commands and `false` for staff-only reload/save.

---

## 3) ACF infrastructure (not on Fabric)

| Reference capability | Fabric |
|---------------------|--------|
| Replacements (`%skills_alias%`, `desc_*`) | **Partial** — **Sprint 44–46:** Brigadier roots **`/sk`**, **`/skill`** (default **Skills** + **`openmenu`**, **`xp`**, **`mana`**, **`skilllevel`** subnodes — same executors as standalone roots; **Sprint 46**); not full ACF `skills\|sk\|skill` tree; `desc_*` still **Missing** |
| Context resolvers (Skill, Stat, User, …) | **Partial** — **Sprint 40:** `FabricCommandSkillParser` + `FabricCommandPlayerTarget` + `FabricCommandSuggestionProviders` (sync suggestions); **Sprint 45:** `FabricCommandStatParser` + `enabledStatKeys` for **`openmenu stat_info`**; **Sprint 50:** `FabricCommandTraitParser` + `enabledTraitKeys` for **`/trait`**, not full ACF (no UUID/JsonArg) |
| Async completions | **Missing** (Fabric uses server-thread sync suggestions) |
| `CommandHelp` | **Missing** |

---

## 4) Next batches (suggested)

1. Align **`help`** permission node with Bukkit and/or add structured multi-line help.
2. **`/xp`:** optional **`silent`**, Stat arguments elsewhere; ACF-prefixed **`/skills skill <subcommand>`** (Fabric uses **`/sk skilllevel …`**, not **`/sk skill`**, for **skilllevel** in Sprint 46).
3. Revisit **`/xp view`** default (currently **true** for `auraskills.command.xp.view`) for PvP servers.
4. **Optional ACF flags** for **`/mana`:** `silent` / `allowOverMax` (Bukkit) — not in Sprint 41 scope.
5. **Leaderboard:** Mojang / user-cache **misses** (never joined, pruned cache) still show **`?`** — no AuraSkills user-file username until stored.

Document owner: keep this file in sync when adding Fabric commands; update `fabric/MENU_AND_COMMANDS_PORT.md` §Sprint for shipped batches.
