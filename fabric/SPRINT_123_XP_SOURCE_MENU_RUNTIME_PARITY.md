# Sprint 123 — XP source menu runtime parity

## Purpose

Restore **`/sources`** data so each skill shows the same embedded **`sources/<skill>.yml`** defaults the JAR ships unless the server overrides them. This sprint fixes **merge logic** and a **missing forging embedded file** — not Granite menu layout, not leaderboard design, not Bedrock/Geyser.

## Root cause

1. **`SourceLoader.loadSources`** only merged entries for keys present under the **disk** **`sources:`** map. When that map was **empty**, **no** embedded defaults were merged → **`LoadedSkill.sources()`** empty → **`FabricSourcesOpenSupport`** emitted **`auraskills.granite.command.sources.empty`**. If the user file lists **at least one** source id, only those ids are merged (upstream behavior) so explicit removals stay possible.

2. **Forging** had **no** `common/src/main/resources/sources/forging.yml`, so **`generateConfigs()`** never wrote **`sources/forging.yml`** on bootstrap and forging stayed empty on clean configs.

## Fixes

- **`common/.../SourceLoader.java`**: when **`userSources` is empty**, iterate **embedded** source names; when non-empty, iterate **user** names only.

- **`common/src/main/resources/sources/forging.yml`**: bundled anvil + grindstone sources (`file_version`).

## Tests

- **`FabricEmbeddedSourceYamlClasspathTest`**: non-null classpath YAML for skills that ship **`common/.../sources/*.yml`** (this fork does **not** yet bundle **endurance** / **sorcery** / **healing** defaults); asserts **`defense.yml`** **`mob_damage`** + **`player_damage`**; asserts **`forging.yml`** **`type: anvil`** + **`type: grindstone`**.

## Out of scope

- **Leaderboard** (still power-only in this branch) — **unchanged**.

## Build

`.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle`

## Operator checklist

Run **`/sources farming`**, **`/sources forging`**, **`/sources mining`**, **`/sources excavation`**, **`/sources defense`**. Expect **non-empty** source rows; defense should list **both** mob-damage and player-damage entries from YAML.

## References

**Changelog.md** (2.3.152), **MENU_AND_COMMANDS_PORT.md**, **PARITY_BACKLOG.md**, **PARITY_AUDIT_LEDGER_PHASE1.md**, **PARITY_COMPLETION_ROADMAP.md**.
