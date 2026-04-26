# FabricAuraSkills 2.3.123 Release Candidate Notes

**Gameplay artifact:** `FabricAuraSkills-2.3.123.jar`  
**GraniteLib artifact:** `granitelib-fabric-2.3.123.jar` (from **`slate-fabric/build/libs/`**, Gradle **`:granitelib-fabric`**)  
**Status:** Java/Fabric validation pass on **two-JAR** layout; **Bedrock/Geyser not yet validated**.

## Packaging (canonical)

**Install both** mods in `mods/`:

1. **Granite Skills** gameplay — `FabricAuraSkills-2.3.123.jar`
2. **GraniteLib** library — `granitelib-fabric-2.3.123.jar`

The gameplay mod declares **`depends: granitelib`** (**`>=0.1.0-alpha.2`**, matching `fabric.mod.json` inside GraniteLib). **Do not** deploy a **stale** GraniteLib JAR alongside a newer gameplay build.

**Do not** combine **external GraniteLib** + **Jar-in-Jar GraniteLib** — use **one** policy only. Default release build **does not** nest GraniteLib inside the gameplay JAR.

**Legacy / dev single-jar** (non-canonical): rebuild with **`-PembedGraniteJij=true`** — see **`fabric/SPRINT_93_TWO_JAR_RELEASE_AND_PARITY_RESET.md`**.

## Highlights (inherits 2.3.122 line + Sprint 93)

- Same Java-smoke / text / menu **built-in** work as **2.3.122 RC1**, plus **Sprint 93** packaging correction: **two real JARs**, honest **`depends`**, **`verifyFabricReleaseBundle`** enforces **no** nested GraniteLib unless `embedGraniteJij`.
- **Parity framing:** **`fabric/PARITY_COMPLETION_ROADMAP.md`** — built-in menus are **partial** vs full **Slate**; capability parity remains **open**.

## Install checklist

1. Copy **both** JARs into `mods/`.
2. Remove old `FabricAuraSkills-*.jar` and old `granitelib-fabric-*.jar` versions you are replacing.
3. **Full restart** the server.
4. Confirm **`latest.log`** loads **`auraskills`** **`2.3.123`** and **`granitelib`** **`0.1.0-alpha.2`** (GraniteLib **mod version** from its `fabric.mod.json`; filename uses root **`projectVersion`**).

**Maintainer build:**

```text
.\gradlew -PfabricOnly=true :granitelib-fabric:build
.\gradlew -PfabricOnly=true :fabric:test :fabric:build :fabric:verifyFabricReleaseBundle
```

## Known limitations

- **Bedrock/Geyser** — deferred.
- **Full Slate/YAML `MenuFileManager` parity** — not complete.
- **Destructive** backup/preset restore — disposable data only.
- **JobsCommand** — out of active scope unless owner reopens.

## RC tag

- Recommended: **`v2.3.123-fabric-rc2`** (two-JAR policy). **`v2.3.122-fabric-rc1`** remains a historical checkpoint but documented **JiJ-as-default** install language that **Sprint 93** supersedes for final policy.
