<h1 style="text-align:center;">FabricAuraSkills</h1>

<p style="text-align:center;">
  Fabric dedicated-server port of the <strong>AuraSkills</strong> RPG skills system, maintained in
  <a href="https://github.com/Discount152/FabricAuraSkills">this repository</a>.
  <strong>Granite Skills</strong> gameplay (<strong>FabricAuraSkills</strong>) and the <strong>GraniteLib</strong> Fabric library (<code>:granitelib-fabric</code>, on-disk <code>slate-fabric/</code>) are <strong>two separate mods</strong> — install <strong>both</strong> JARs on dedicated servers (see <strong>Server install</strong> and <code>fabric/PARITY_COMPLETION_ROADMAP.md</code>).
</p>

<p style="text-align:center;">
  <a href="https://github.com/Discount152/FabricAuraSkills/releases">Releases</a>
  &nbsp;&nbsp;|&nbsp;&nbsp;
  <a href="https://github.com/Discount152/FabricAuraSkills/issues">Issues</a>
</p>

[![GitHub Release](https://img.shields.io/github/v/release/Discount152/FabricAuraSkills?style=flat-square)](https://github.com/Discount152/FabricAuraSkills/releases/latest)

## About

**AuraSkills** (formerly *Aurelium Skills*) is a skills/stats/abilities system originally shipped as a **Bukkit/Spigot** plugin. **Archy-X** continues that line on Spigot; the official site and plugin downloads are on [aurelium.dev](https://aurelium.dev/auraskills) with [wiki](https://wiki.aurelium.dev/auraskills) documentation.

**This repository** is the **Fabric** dedicated-server build: the **gameplay mod** (**FabricAuraSkills**, mod id **`auraskills`**) plus the **GraniteLib** library mod (**`granitelib-fabric-*.jar`**, mod id **`granitelib`**). Install **both** remapped JARs from a release or local Gradle build. Optional **`-PembedGraniteJij=true`** can nest GraniteLib for legacy/dev convenience only — **not** the canonical product install (see **`fabric/SPRINT_93_TWO_JAR_RELEASE_AND_PARITY_RESET.md`**).

Features mirror the original design at a high level (skills, stats, abilities, menus, rewards, loot); see the upstream wiki for conceptual detail and [server requirements](https://wiki.aurelium.dev/auraskills/server-requirements) where they still apply to game-version concepts.

## Building

Use **this** source tree (not the upstream Spigot repository):

```text
git clone https://github.com/Discount152/FabricAuraSkills.git
cd FabricAuraSkills
```

**Fabric mods (typical — two JARs):**

```text
# Windows
.\gradlew.bat -PfabricOnly=true :granitelib-fabric:build
.\gradlew.bat -PfabricOnly=true :fabric:build
```

- Gameplay: `fabric/build/libs/FabricAuraSkills-<projectVersion>.jar`
- GraniteLib: `slate-fabric/build/libs/granitelib-fabric-<projectVersion>.jar`

**Legacy single-jar / JiJ (dev only):** `.\gradlew.bat -PfabricOnly=true -PembedGraniteJij=true :fabric:build`

A full multi-loader build may use `./gradlew clean build` depending on your environment; see `settings.gradle.kts` and [CONTRIBUTING.md](CONTRIBUTING.md) for the layout you need.

## Server install (operators)

1. Install **both** JARs in **`mods/`**: **Granite Skills** gameplay **`FabricAuraSkills-<version>.jar`** and **GraniteLib** **`granitelib-fabric-<version>.jar`** (see **`projectVersion`** in `gradle.properties` for the **filename** line; GraniteLib’s **mod version** inside `fabric.mod.json` is **`granitelibVersion`**).
2. Use a compatible **Fabric Loader**, **Fabric API**, and **Minecraft** version (see `supportedMCVersions` / `releaseTitle` in `gradle.properties`).
3. **Do not** leave **stale** older GraniteLib or gameplay JARs in `mods/`. **Do not** mix **external GraniteLib** with a build that still **Jar-in-Jars** GraniteLib (**`-PembedGraniteJij=true`**) — duplicate **`granitelib`** mod risk.
4. **Full restart** after replacing JARs. Check **`latest.log`** for **`auraskills`** and **`granitelib`** loading with expected versions.
5. Operator checklists: **`fabric/SPRINT_82_JAVA_SMOKE_MATRIX_RESULTS.md`**, **`fabric/SPRINT_90_MANUAL_GLOBAL_GRANITE_TEXT_AUDIT.md`**, **`fabric/PARITY_COMPLETION_ROADMAP.md`**, **`fabric/SPRINT_106_MANUAL_DISK_TEMPLATE_LOADING.md`**, **`fabric/SPRINT_107_MANUAL_TEMPLATE_RELOAD_LIFECYCLE.md`**, **`fabric/SPRINT_108_MANUAL_TEMPLATE_EXPORT.md`** (**`/auraskills templates export`** for bundled YAML → **`config/auraskills/granite/templates/`**; **`/auraskills reload`** resets template diagnostics).
6. **Release notes:** **`fabric/RELEASE_NOTES_2.3.123.md`** (current two-JAR RC policy). Historical **`fabric/RELEASE_NOTES_2.3.122.md`** is **superseded** for packaging. Optional tags: **`v2.3.123-fabric-rc2`** (recommended for two-JAR RC); **`v2.3.122-fabric-rc1`** documents an earlier checkpoint with **JiJ-default** wording — **not** final packaging policy.

## Upstream Bukkit/Spigot API (reference)

The **Bukkit** API, Javadocs, and Maven coordinates for `dev.aurelium:auraskills-api-bukkit` describe the **original plugin**, not the Fabric mod in this repository. This port does not ship the same API surface. For upstream plugin development, see the [API wiki](https://wiki.aurelium.dev/auraskills/api) and [Javadocs](https://docs.aurelium.dev/auraskills-api-bukkit/) published for that product line.

## Contributing

We welcome contributions. Read [CONTRIBUTING.md](CONTRIBUTING.md) for environment setup and guidelines before opening a pull request.
