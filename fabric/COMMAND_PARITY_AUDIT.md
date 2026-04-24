# Bukkit (reference) vs Fabric — command parity (Granite)

**Runtime product:** **Fabric** + **Geyser/Floodgate**; **Bukkit** `CommandRegistrar` + ACF is the **in-repo reference only**, not a second shipped runtime.

**Sprint 39** establishes this audit and a **shared Brigadier scaffold** (`fabric/.../command/FabricBrigadierCommands` and helpers). **Sprint 40** adds **shared online player + skill parsing** (`FabricCommandPlayerTarget`, `FabricCommandSkillParser`, `FabricCommandSuggestionProviders`) and the first **`xp`** batch. **Final** public **Granite** command names/aliases are **not frozen** in this document.

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
| `SkillsRootCommand` default → open `skills` menu | `/auraskills skills`, `/skills` → `FabricAuraSkillsMenus` **Skills** | Partial | Bukkit: Slate `skills` menu; Fabric: built-in registry GUI — capability, not 1:1 Slate |
| `StatsCommand` @Default @CommandAlias("stats") | `/auraskills stats`, `/stats` | Partial | Same as above for **Stats** |
| `SkillsRootCommand` `reload` | `/auraskills reload` | Partial | Staff default deny; `reloadFromDisk` |
| `SkillsRootCommand` `save` | `/auraskills save` | Partial | `saveAllUserProfiles` |
| `SkillsRootCommand` `version` | `/auraskills version` | Partial | Data dir path; not full Bukkit `version` text |
| `SkillsRootCommand` `help` (ACF `CommandHelp`) | `/auraskills help` | Partial | Short line; Bukkit has full ACF help pages |
| `ManaCommand` @Default | `/mana` | Partial | **Sprint 39:** self mana via `MessageBuilder` + `MANA_DISPLAY`; add/remove/set **Missing** |
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
| `SkillCommand` `skill setlevel\|addlevel\|setall\|reset` | — | Missing | |
| `XpCommand` `xp add\|set\|remove` | `/auraskills xp …`, `/xp …` — **add** / **set** / **remove** `<player> <skill> <amount>`; permissions `auraskills.command.xp.*` (defaults like Bukkit **op** for mutations) | Partial | **Sprint 40:** no `silent` flag; no `skills\|sk\|skill` prefix (use `/xp` or `/auraskills xp`) |
| `XpCommand` (extra) | `/xp view <player> <skill>` | Partial | **Sprint 40:** read-only; not in reference Bukkit file; uses `auraskills.command.xp.view` (default **allow**) — lock down with LP if needed |
| `ModifierCommand` | — | Missing | |
| `TraitCommand` | — | Missing | |
| `ItemCommand` (large tree) | — | Missing | |
| `ArmorCommand` | — | Missing | |
| `ProfileCommand` | — | Missing | |
| `BackupCommand` | — | Missing | |
| `PresetCommand` | — | Missing | |
| `StorageCommand` | — | Missing | |
| `OpenMenuCommand` | — | Missing | Tied to Slate `openmenu` + menus |
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
| Replacements (`%skills_alias%`, `desc_*`) | **Missing** — hardcoded/translation keys for now |
| Context resolvers (Skill, Stat, User, …) | **Partial** — **Sprint 40:** `FabricCommandSkillParser` + `FabricCommandPlayerTarget` + `FabricCommandSuggestionProviders` (sync suggestions); not full ACF (no Stat/Trait/UUID/JsonArg) |
| Async completions | **Missing** (Fabric uses server-thread sync suggestions) |
| `CommandHelp` | **Missing** |

---

## 4) Next batches (suggested)

1. Align **`help`** permission node with Bukkit and/or add structured multi-line help.
2. **`/auraskills top`** (or `/skills top` alias) using existing `LeaderboardManager` (already on Fabric).
3. **`/mana` subcommands** (add/set/remove) with same permission pattern as Bukkit.
4. **`/xp`:** optional **`silent`**, Stat arguments elsewhere, and **`skills` / `sk` / `skill`** alias roots to mirror Bukkit.
5. Revisit **`/xp view`** default (currently **true** for `auraskills.command.xp.view`) for PvP servers.

Document owner: keep this file in sync when adding Fabric commands; update `fabric/MENU_AND_COMMANDS_PORT.md` §Sprint for shipped batches.
