# Sprint 68 — per-skill `SkillCommands.*` (reference inventory + Fabric)

**Build:** `projectVersion` **2.3.96** (`gradle.properties`).

## Part A — Bukkit reference inventory (before implementation)

| Bukkit class | `@CommandAlias` (root) | `Skill` | Config gate | ACF default action | `CommandPermission` on class | Bukkit `plugin.yml` |
|--------------|-------------------------|---------|-------------|--------------------|--------------------|--------------------|
| `FarmingCommand` | `farming` | `Skills.FARMING` | `enable_skill_commands` **and** per-skill `skills.farming.options.enabled` | `onCommand(Player)` → `openMenu` → `LevelProgressionOpener#open` | (none) | `auraskills.skill.farming` (default `false` per-skill) |
| `ForagingCommand` | `foraging` | `FORAGING` | same pattern | same | (none) | `auraskills.skill.foraging` |
| `MiningCommand` | `mining` | `MINING` | | | | `auraskills.skill.mining` |
| `FishingCommand` | `fishing` | `FISHING` | | | | `…fishing` |
| `ExcavationCommand` | `excavation` | `EXCAVATION` | | | | `…excavation` |
| `ArcheryCommand` | `archery` | `ARCHERY` | | | | `…archery` |
| `DefenseCommand` | `defense` | `DEFENSE` | | | | `…defense` |
| `FightingCommand` | `fighting` | `FIGHTING` | | | | `…fighting` |
| `EnduranceCommand` | `endurance` | `ENDURANCE` | | | | `…endurance` |
| `AgilityCommand` | `agility` | `AGILITY` | | | | `…agility` |
| `AlchemyCommand` | `alchemy` | `ALCHEMY` | | | | `…alchemy` |
| `EnchantingCommand` | `enchanting` | `ENCHANTING` | | | | `…enchanting` |
| `SorceryCommand` | `sorcery` | `SORCERY` | | | | `…sorcery` |
| `HealingCommand` | `healing` | `HEALING` | | | | `…healing` |
| `ForgingCommand` | `forging` | `FORGING` | | | | `…forging` |

- **Count:** **15** inner classes, matching **15** values in the **`Skills`** API enum in this repo.  
- **Not** registered under `skills|sk|skill` — separate **top-level** ACF command registration (`CommandRegistrar#registerSkillCommands`).  
- **If skill disabled in YAML:** the command is **not** registered.  
- **If skill enabled** but `skill.isEnabled()` is false in `openMenu`:** ACF `UNKNOWN_COMMAND`-style error.  
- **Console:** not a valid sender (Bukkit: `Player` only).

**Fabric overlap (pre-S68):** **`/openmenu level_progression <skill>`** (and `/auraskills openmenu` / `sk openmenu` subtree) with **`openmenu` permission** — same *effect* (level progression for a skill) but a **different** command path and **different** staff permission. Per-skill shortcuts are **additions**, not `openmenu` aliases.

## Part B — Fabric implementation (Sprint 68)

| Item | |
|------|---|
| **Class** | `FabricSkillShortcutCommands` |
| **Roots** | One literal per row above: `/farming` … `/forging` (not under `/skills` or `/auraskills`) |
| **Global config** | `config.yml` `enable_skill_commands` → `Option.ENABLE_SKILL_COMMANDS` — when `false`, roots are **not visible** in Brigadier (`requires` = false) |
| **Per-skill** | `SkillManager#loadConfigEnabledMap()` — when `false` for that skill, that root is hidden (matches reference “do not register”) |
| **Per-skill permission** | `auraskills.skill.<lowercase_skill_enum>`; default **true** in `Permissions.check` (same as `FabricUser#hasSkillPermission`) — reference uses **plugin.yml** default **false** for each skill; operators grant via LP. Document as **Partial** if you expect strict Bukkit default deny on raw Fabric. |
| **Player vs console** | **Players** with gate + permission: opens **`FabricAuraSkillsMenus#openLevelProgression`**. **Console:** `auraskills.granite.command.utility.players_only`. No invented console targets (reference: none). |
| **Menu vs chat** | **Menu** only, **no** success chat (Bukkit `LevelProgressionOpener` does not print a line). |
| **Slate** | Bukkit uses **Slate** `level_progression`; Fabric uses **built-in** Granite menu for the same id — **Partial** for UI. |

**Bedrock / Geyser:** opens the same **server-side** `level_progression` inventory as other entry points — NRP and layout caveats in parity docs apply; **not** separately tested this sprint (do not claim in release notes as verified).

**Automated test:** `FabricSkillShortcutCommandsTest` — 15 unique literals, matches `Skills.values().length`.

**Manual dedicated-server (Java) checklist**

- [ ] With `enable_skill_commands: true` and skill enabled in `skills.yml`, run `/mining` (or another enabled skill) — **level progression** opens.
- [ ] `enable_skill_commands: false` — shortcut should **not** appear in tab (root hidden) or, if you force-parse, not applicable.
- [ ] Disable a skill in `skills.yml` — that alias hidden.
- [ ] **Console** `mining` from console — **players only** line.
- [ ] **Permission** — deny `auraskills.skill.mining` for a test user and confirm denied.

**Risks / caveats**

- **`loadConfigEnabledMap`** on every **Brigadier `requires` / execute** re-reads YAML — same cost pattern as gating; acceptable for S68.
- **Reload:** root visibility updates when commands are re-registered; depends on when Fabric re-runs command registration (not guaranteed on `/auraskills reload` only — if behavior report, document follow-up). **MVP:** in-process execute checks stay consistent after config reload in memory if you restart or if registration re-fires.

**Remaining gaps (command lane)**

- **`JobsCommand`** — out of default product scope.  
- **Bukkit** `SkillCommand` (admin `/skill setlevel` tree) is separate from this sprint — see **`/skilllevel`** in Fabric.  
- **ACF** `unknown_command` for disabled `skill.isEnabled()` — Fabric uses `skill_root.unavailable` translatable; wording differs slightly.

**Pass / fail for sprint**

- [x] Reference inventory in this file **before** implementation narrative (Part A).  
- [x] `.\gradlew -PfabricOnly=true :fabric:test` passes.  
- [x] Parity / backlog / Changelog / version updated.
