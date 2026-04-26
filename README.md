<h1 style="text-align:center;">FabricAuraSkills</h1>

<p style="text-align:center;">
  Fabric dedicated-server port of the <strong>AuraSkills</strong> RPG skills system, maintained in
  <a href="https://github.com/Discount152/FabricAuraSkills">this repository</a>.
  The mod embeds <strong>GraniteLib</strong> (<code>:granitelib-fabric</code>) for menu and layout work (Jar-in-Jar; see <code>docs/architecture/</code>).
</p>

<p style="text-align:center;">
  <a href="https://github.com/Discount152/FabricAuraSkills/releases">Releases</a>
  &nbsp;&nbsp;|&nbsp;&nbsp;
  <a href="https://github.com/Discount152/FabricAuraSkills/issues">Issues</a>
</p>

[![GitHub Release](https://img.shields.io/github/v/release/Discount152/FabricAuraSkills?style=flat-square)](https://github.com/Discount152/FabricAuraSkills/releases/latest)

## About

**AuraSkills** (formerly *Aurelium Skills*) is a skills/stats/abilities system originally shipped as a **Bukkit/Spigot** plugin. **Archy-X** continues that line on Spigot; the official site and plugin downloads are on [aurelium.dev](https://aurelium.dev/auraskills) with [wiki](https://wiki.aurelium.dev/auraskills) documentation.

**This repository** is the **Fabric** dedicated-server build (**FabricAuraSkills**). It is a separate artifact from the Spigot plugin: install the JAR from this repo’s releases for Fabric servers. **GraniteLib** is the embedded library inside the mod JAR; it is **not** a standalone published Fabric mod in this release model ([distribution ADR](docs/architecture/granitelib-distribution-adr.md)).

Features mirror the original design at a high level (skills, stats, abilities, menus, rewards, loot); see the upstream wiki for conceptual detail and [server requirements](https://wiki.aurelium.dev/auraskills/server-requirements) where they still apply to game-version concepts.

## Building

Use **this** source tree (not the upstream Spigot repository):

```text
git clone https://github.com/Discount152/FabricAuraSkills.git
cd FabricAuraSkills
```

**Fabric mod (typical):**

```text
# Windows
.\gradlew.bat :fabric:build -PfabricOnly=true
```

The remapped Fabric mod JAR is produced under `fabric/build/libs/` (name includes `FabricAuraSkills` and the current `projectVersion` in `gradle.properties`).

A full multi-loader build may use `./gradlew clean build` depending on your environment; see `settings.gradle.kts` and [CONTRIBUTING.md](CONTRIBUTING.md) for the layout you need.

## Server install (operators)

1. Build or download **`FabricAuraSkills-<version>.jar`** (version matches **`projectVersion`** in `gradle.properties`; release builds should record the exact filename in your runbook).
2. Place the JAR in the dedicated server’s **`mods/`** folder alongside a compatible **Fabric Loader**, **Fabric API**, and **Minecraft** version (see `supportedMCVersions` / `releaseTitle` in `gradle.properties` and your server’s `fabric.mod.json` dependency pins).
3. **GraniteLib** is **Jar-in-Jar** inside the main mod — do **not** add a separate `granitelib` mod JAR unless you are deliberately testing a standalone GraniteLib build.
4. Optional: `:fabric:archiveFabricModJar` (or a full `:fabric:build`) can also copy timestamped JARs under **`fabric-mod-jar-history/`** for rollback notes — see `fabric/build.gradle.kts`.
5. Operator smoke and text-rendering checklists: **`fabric/SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`**, **`fabric/SPRINT_90_MANUAL_GLOBAL_GRANITE_TEXT_AUDIT.md`**, **`fabric/SPRINT_91_MANUAL_FABRIC_RELEASE_READINESS.md`**.

## Upstream Bukkit/Spigot API (reference)

The **Bukkit** API, Javadocs, and Maven coordinates for `dev.aurelium:auraskills-api-bukkit` describe the **original plugin**, not the Fabric mod in this repository. This port does not ship the same API surface. For upstream plugin development, see the [API wiki](https://wiki.aurelium.dev/auraskills/api) and [Javadocs](https://docs.aurelium.dev/auraskills-api-bukkit/) published for that product line.

## Contributing

We welcome contributions. Read [CONTRIBUTING.md](CONTRIBUTING.md) for environment setup and guidelines before opening a pull request.
