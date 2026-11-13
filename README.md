<h1 style="text-align:center;">Granite Skills</h1>

<p style="text-align:center;">
Standalone skills mod for Minecraft. Fabric dedicated servers use Minecraft 26.3. Mod id <code>graniteskills</code>.
</p>

[![GitHub Release](https://img.shields.io/github/v/release/Discount152/FabricAuraSkills?style=flat-square)](https://github.com/Discount152/FabricAuraSkills/releases/latest)

<p style="text-align: center;font-weight: bold;">
  <a href="https://github.com/Discount152/FabricAuraSkills">Source</a>
  &nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="https://github.com/Discount152/FabricAuraSkills/tree/main/wiki">Wiki</a>
</p>

## About

**Granite Skills** is a Minecraft plugin that adds skills, stats, abilities, and other
RPG-related features. The plugin is fully configurable and customizable, enabling usage on a wide range of server types.

Features include:

- **Skills** - Gain skill XP to level skills through general Minecraft tasks, such as Farming or Mining.
- **Stats** - Get player buffs like increased health and damage by leveling skills, which can be as independent
  modifiers and on items.
- **Abilities** - Skills have passive and active abilities that add gameplay mechanics, plus a full mana system.
- **Menus** - Players can see everything related to skills in fully-configurable inventory GUIs.
- **Rewards** - Customize rewards given for leveling skills, such as running commands or giving items.
- **Loot** - Create custom loot tables for fishing, blocks, and mobs.

See the [official website](https://github.com/Discount152/FabricAuraSkills) and [wiki](https://github.com/Discount152/FabricAuraSkills/tree/main/wiki) for a more
complete list of features. The wiki also contains the list
of [server requirements](https://github.com/Discount152/FabricAuraSkills/tree/main/wiki/server-requirements) to run the plugin.

## Building

GraniteSkills uses Gradle for dependencies and building.

#### Compiling from source

First, clone the project (requires Git to be installed):

```
git clone https://github.com/Discount152/FabricAuraSkills.git
cd GraniteSkills/
```

Then build depending on your operating system:

Linux / macOS

```
./gradlew clean build
```

Windows

```
.\gradlew.bat clean build
```

The output jar can be found in the `build/libs` directory.

## API

GraniteSkills has an extensive developer API.

Read the full API documentation on the [wiki](https://github.com/Discount152/FabricAuraSkills/tree/main/wiki/api), or view
the [Javadocs](https://github.com/Discount152/FabricAuraSkills).

Release versions are published to the Maven central repository. Snapshot versions require adding the Sonatype OSS
repository.

### Maven

```xml

<repository>
    <id>maven-central-snapshots</id>
    <url>https://central.sonatype.com/repository/maven-snapshots/</url>
</repository>
```

```xml

<dependency>
    <groupId>dev.granite</groupId>
    <artifactId>graniteskills-api-bukkit</artifactId>
    <version>2.3.5</version>
    <scope>provided</scope>
</dependency>
```

### Gradle

**Groovy DSL:**

```gradle
repositories {
    mavenCentral()
    maven { url 'https://central.sonatype.com/repository/maven-snapshots/' }
}

dependencies {
    compileOnly 'dev.granite:graniteskills-api-bukkit:2.3.5'
}
```

**Kotlin DSL:**

```Gradle Kotlin DSL
repositories { 
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies { 
    compileOnly("dev.granite:graniteskills-api-bukkit:2.3.5")
}
```

## Contributing

We welcome contributions from the community. Please read the [contributing guide](CONTRIBUTING.md) for instructions
on setting up a development environment and important guidelines you should know before submitting a pull request.
