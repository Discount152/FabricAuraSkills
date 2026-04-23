# Fabric AuraSkills — quick start (ship checklist)

**Install:** the default public layout requires **three** `mods/`: **FabricAuraSkills**, **GraniteLib** (`granitelib`), and **Fabric API** — not FabricAuraSkills alone. **Geyser/Floodgate** / Bedrock in-game behavior is **not** claimed as fully verified in-repo until the [manual acceptance matrix](../docs/architecture/granitelib-manual-acceptance-matrix.md) **B1–B10** (or you state **unverified**). See [release prep & FAQ](../docs/architecture/granitelib-release-prep-faq.md).

## Build the mod JAR

From the repository root:

```bash
./gradlew -PfabricOnly=true :fabric:build
```

Output: `fabric/build/libs/FabricAuraSkills-<version>.jar` (version from root `gradle.properties` → `projectVersion`). Use **`-PfabricOnly=true`** if the full multi-project build fails on Bukkit/Gradle transforms.

## Install

1. Server: **Minecraft Java 1.21.11**, **Java 21**, **Fabric Loader ≥ 0.18.4** (Apex Hosting and similar providers’ `fabric_1.21.11` images often ship **0.18.4** — that works; **0.19.x** is fine too), **Fabric API**, and **GraniteLib** (standalone JAR, mod id `granitelib`, e.g. `granitelib-fabric-0.1.0-alpha.1.jar` from this repo’s `:granitelib-fabric:remapJar` or a release). **FabricAuraSkills** declares `depends: granitelib` — the loader will not start without it.
2. Put **GraniteLib**, **Fabric API**, the **FabricAuraSkills** JAR, and any other **depends** in `mods/` (permissions API may be bundled with FAS; still satisfy `fabric.mod.json`).
3. Start once to generate `config/auraskills/` (same layout as Paper).

## Permissions (production)

- Skill XP: `auraskills.skill.<skillname>` (default **allow** if unset — tighten with LuckPerms Fabric).
- Commands: `auraskills.command.version`, `auraskills.command.skills` (default allow); **`auraskills.command.reload`** and **`auraskills.command.save`** default **deny** until granted (ops / LuckPerms).

### Optional: statistic sources in YAML

Besides vanilla custom stats (`walk_one_cm`, `mob_kills`, `damage_dealt`, …), Fabric accepts **`mined/minecraft:stone`**, **`used/...`**, **`broken/...`**, **`picked_up/...`**, **`dropped/...`**, **`killed/minecraft:zombie`**, **`killed_by/...`**, or **`custom/walk_one_cm`** (see `FabricStatisticSourceResolver` / `FabricStatisticXpHandler`).

## Commands

**Plain list (copy/paste for players / hosts):** see **`MOD_COMMANDS.txt`** in this folder.

| Command | Purpose |
|--------|---------|
| `/auraskills version` | Version + data folder path |
| `/auraskills skills` | Player: skill levels summary |
| `/auraskills reload` | Reload `config.yml`, messages, `skills.yml` / stats / sources / loot / rewards / XP tables; refresh traits for **online** players. Does **not** re-hook Fabric events. |
| `/auraskills save` | Flush **all loaded** user profiles to disk |

After editing YAML under `config/auraskills/`, run **`/auraskills reload`** (or restart).

**Production acceptance (operator):** for the **two-mod** + Fabric API install, see the repo [manual acceptance matrix](../docs/architecture/granitelib-manual-acceptance-matrix.md) (Sprint 97) — in-game **Java** and **Bedrock (Geyser/Floodgate)** steps are **owner-run**; the Fabric default product is **text-first** for **`/auraskills skills`** (no Paper **`/skills`** tree).

## What “usable” means vs Paper

**Fabric core port** (default `sources/*.y` XP paths, combat/fishing/brewing/block/mana flows, YAML storage or SQL, `/auraskills reload|save`, permissions) is **complete** for a dedicated Fabric skills server. See `fabric/PARITY_BACKLOG.md` for remaining **reference** gaps (Slate menus, full command tree, deeper item/NBT parity, boss bar polish, …) and *Out of scope* for unrelated Bukkit-plugin ecosystems — not blockers to ship core gameplay.

## Player guide (in-game)

For a **plain-language walkthrough** of skills, XP, mana abilities, commands, permissions, and Fabric differences, see **`PLAYER_GUIDE.md`** (this folder).

## Support

If Gradle fails on `ExternalDependencyInstrumentingArtifactTransform`, see `fix-gradle-transforms-cache.cmd` in the repo root (handoff §2–3).
