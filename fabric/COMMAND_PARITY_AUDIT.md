# Bukkit (reference) vs Fabric — command parity (Granite)

**Runtime product:** **Fabric** + **Geyser/Floodgate**; **Bukkit** `CommandRegistrar` + ACF is the **in-repo reference only**, not a second shipped runtime.

**Sprint 39** establishes this audit and a **shared Brigadier scaffold** (`fabric/.../command/FabricBrigadierCommands` and helpers). **Sprint 40** adds **shared online player + skill parsing** (`FabricCommandPlayerTarget`, `FabricCommandSkillParser`, `FabricCommandSuggestionProviders`) and the first **`xp`** batch. **Sprint 41** adds the bounded **`/mana`** family (self + `view` + add/set/remove), shared **`FabricCommandAmountRules`** and **`FabricCommandMutationFeedback`**, and perms `auraskills.command.mana.*` (Sprint 41). **Sprints 42–43** add the bounded **`/skilllevel`** line (view, add, set, remove, **setall**, **reset**) + **`FabricCommandSkillLevelApply`**, and perms `auraskills.command.skilllevel.view`, `auraskills.command.skill.setlevel`, `auraskills.command.skill.reset`. **Sprint 44** adds **`/sk`**, **`/skill`** (alias default → **Skills** + **`openmenu`**) and **bounded** **`openmenu`** for built-in **skills** / **stats** (top-level **`/openmenu`**, **`/auraskills openmenu`**, JSON/properties **not** ported). **Sprint 45** extends **`openmenu`** with **typed** **skill** / **stat** args for **level_progression** and **stat_info** (same `auraskills.command.openmenu`), reusing **Fabric** menu **registry** + **`FabricMenuContext`** (no Bukkit `JsonArg` / `page` parity). **Sprint 46** chains the existing **`xp`**, **`mana`**, and **`skilllevel`** **Brigadier** families under **`/sk`** and **`/skill`** (same `*Root()` builders as the standalone top-levels — new node trees, shared executors). **Final** public **Granite** command names/aliases are **not frozen** in this document.

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
| `SkillsRootCommand` default → open `skills` menu | `/auraskills skills`, `/skills`, **`/sk`**, **`/skill`** (Sprint 44) → **Skills**; **Sprint 46** — **`/sk\|skill xp|mana|skilllevel|openmenu …`** matches the same family shapes as the standalone roots | Partial | Bukkit: Slate `skills` menu; Fabric: built-in registry; **not** full `SkillsRootCommand` tree (top, rank, etc.) |
| `StatsCommand` @Default @CommandAlias("stats") | `/auraskills stats`, `/stats` | Partial | Same as above for **Stats** |
| `SkillsRootCommand` `reload` | `/auraskills reload` | Partial | Staff default deny; `reloadFromDisk` |
| `SkillsRootCommand` `save` | `/auraskills save` | Partial | `saveAllUserProfiles` |
| `SkillsRootCommand` `version` | `/auraskills version` | Partial | Data dir path; not full Bukkit `version` text |
| `SkillsRootCommand` `help` (ACF `CommandHelp`) | `/auraskills help` | Partial | Short line; Bukkit has full ACF help pages |
| `ManaCommand` (common) | `/mana`, `/auraskills mana`, **`/sk|skill mana`** (**Sprint 46** — same `mana` subtree as standalone) | Partial | **Sprint 41:** default self (`MANA_DISPLAY`); `view` / `add` / `set` / `remove` (online `player` + `amount`); no ACF `silent` / `allowOverMax` flags — reference overflow rules via `Traits.MAX_MANA` only |
| `SkillsRootCommand` `top`, `skilltop` | — | Missing | Needs leaderboard UX + args |
| `SkillsRootCommand` `rank`, `skillrank` | — | Missing | |
| `SkillsRootCommand` `lang` | — | Missing | |
| `SkillsRootCommand` `multiplier` | — | Missing | |
| `SkillsRootCommand` `toggle`, `abtoggle` | — | Missing | |
| `SkillsRootCommand` `resethealth` | — | Missing | |
| `SkillsRootCommand` `sources` | — | Missing | |
| `SkillsRootCommand` `claimitems` | — | Missing | |
| `SkillsRootCommand` `transfer` | — | Missing | |
| `SkillsRootCommand` `updateleaderboards` | — | Missing | |
| `SkillCommand` `skill setlevel\|addlevel\|setall\|reset` (+ Fabric **`remove`**) | `/skilllevel`, `/auraskills skilllevel`, **`/sk\|skill skilllevel`** (Sprint 46) — **view** / **add** / **set** / **remove** / **setall** / **reset** (online) | Partial | Bukkit: `skills\|sk\|skill` + `skill` sub, not the **`/skilllevel`** *word*; Fabric **compatibility** uses **`/sk skilllevel`** (Sprint 46), not a literal **`/sk skill <sub>`**; Fabric-only **`remove`**; full ACF **`skill`/`SkillCommand`** subnode parity still open |
| `XpCommand` `xp add\|set\|remove` | `/auraskills xp …`, `/xp …`, **`/sk\|skill xp …`** (Sprint 46) — **add** / **set** / **remove** `<player> <skill> <amount>`; permissions `auraskills.command.xp.*` (defaults like Bukkit **op** for mutations) | Partial | **Sprint 40** + **Sprint 46** alias line; no `silent` flag |
| `XpCommand` (extra) | `/xp view …`, **`/sk\|skill xp view …`** (Sprint 46) | Partial | **Sprint 40:** read-only; not in reference Bukkit file; uses `auraskills.command.xp.view` (default **allow**) — lock down with LP if needed |
| `ModifierCommand` | — | Missing | |
| `TraitCommand` | — | Missing | |
| `ItemCommand` (large tree) | — | Missing | |
| `ArmorCommand` | — | Missing | |
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
| Context resolvers (Skill, Stat, User, …) | **Partial** — **Sprint 40:** `FabricCommandSkillParser` + `FabricCommandPlayerTarget` + `FabricCommandSuggestionProviders` (sync suggestions); **Sprint 45:** `FabricCommandStatParser` + `enabledStatKeys` for **`openmenu stat_info`**, not full ACF (no Trait/UUID/JsonArg) |
| Async completions | **Missing** (Fabric uses server-thread sync suggestions) |
| `CommandHelp` | **Missing** |

---

## 4) Next batches (suggested)

1. Align **`help`** permission node with Bukkit and/or add structured multi-line help.
2. **`/auraskills top`** (or `/skills top` alias) using existing `LeaderboardManager` (already on Fabric).
3. **`/xp`:** optional **`silent`**, Stat arguments elsewhere; ACF-prefixed **`/skills skill <subcommand>`** (Fabric uses **`/sk skilllevel …`**, not **`/sk skill`**, for **skilllevel** in Sprint 46).
4. Revisit **`/xp view`** default (currently **true** for `auraskills.command.xp.view`) for PvP servers.
5. **Optional ACF flags** for **`/mana`:** `silent` / `allowOverMax` (Bukkit) — not in Sprint 41 scope.

Document owner: keep this file in sync when adding Fabric commands; update `fabric/MENU_AND_COMMANDS_PORT.md` §Sprint for shipped batches.
