# Granite Skills — quick start (ship checklist)

## Build the mod JAR

From the repository root:

```bash
./gradlew -PfabricOnly=true :fabric:build
```

Output: `fabric/build/libs/GraniteSkills-<version>.jar` (version from root `gradle.properties` → `projectVersion`). Use **`-PfabricOnly=true`** if the full multi-project build fails on Bukkit/Gradle transforms.

## Install

1. Server: **Minecraft Java 26.3**, **Java 25**, **Fabric Loader ≥ 0.20.1**, **Fabric API**. **fabric-permissions-api** is bundled inside the jar.
2. Put the **GraniteSkills** JAR in the server `mods/` folder. The mod id is still `auraskills`.
3. Start once to generate `config/auraskills/` (same layout as Paper).

## Permissions (production)

- Skill XP: `auraskills.skill.<skillname>` (default **allow** if unset — tighten with LuckPerms Fabric).
- Commands: `auraskills.command.version`, `auraskills.command.skills` (default allow); **`auraskills.command.reload`** and **`auraskills.command.save`** default **deny** until granted (ops / LuckPerms).

### Optional: statistic sources in YAML

Besides vanilla custom stats (`walk_one_cm`), Fabric accepts **`mined/minecraft:stone`**, **`used/minecraft:diamond_pickaxe`**, **`broken/...`**, **`picked_up/...`**, or **`custom/walk_one_cm`** (see `FabricStatisticXpHandler`).

## Commands

| Command | Purpose |
|--------|---------|
| `/auraskills version` or `/graniteskills version` | Version + data folder path |
| `/auraskills skills` or `/graniteskills skills` | Player: skill levels summary |
| `/auraskills reload` or `/graniteskills reload` | Reload `config.yml`, messages, `skills.yml` / stats / sources / loot / rewards / XP tables; refresh traits for **online** players. Does **not** re-hook Fabric events. |
| `/auraskills save` or `/graniteskills save` | Flush **all loaded** user profiles to disk |

After editing YAML under `config/auraskills/`, run **`/auraskills reload`** (or restart).

## What “usable” means vs Paper

**Fabric core port** (default `sources/*.y` XP paths, combat/fishing/brewing/block/mana flows, YAML storage or SQL, `/auraskills reload|save`, permissions) is **complete** for a dedicated Fabric skills server. See `fabric/PARITY_BACKLOG.md` for remaining **Paper parity** items (Slate menus, WorldGuard, Vault-style economy hooks, full item/NBT modifiers, extra statistic types, boss bar polish, …) — those are follow-ups, not blockers to ship gameplay.

## Player guide (in-game)

For a **plain-language walkthrough** of skills, XP, mana abilities, commands, permissions, and Fabric differences, see **`PLAYER_GUIDE.md`** (this folder).

## Support

If Gradle fails on `ExternalDependencyInstrumentingArtifactTransform`, see `fix-gradle-transforms-cache.cmd` in the repo root (handoff §2–3).
